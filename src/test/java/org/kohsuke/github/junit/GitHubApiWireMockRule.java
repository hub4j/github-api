package org.kohsuke.github.junit;

import com.github.tomakehurst.wiremock.common.FileSource;
import com.github.tomakehurst.wiremock.common.InputStreamSource;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import com.github.tomakehurst.wiremock.extension.Parameters;
import com.github.tomakehurst.wiremock.extension.ResponseTransformer;
import com.github.tomakehurst.wiremock.http.HttpHeader;
import com.github.tomakehurst.wiremock.http.HttpHeaders;
import com.github.tomakehurst.wiremock.http.Request;
import com.github.tomakehurst.wiremock.http.Response;
import com.google.gson.*;
import com.jcraft.jsch.IO;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Type;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collection;
import java.util.Map;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static com.github.tomakehurst.wiremock.client.WireMock.status;
import static com.github.tomakehurst.wiremock.common.Gzip.gzip;
import static com.github.tomakehurst.wiremock.common.Gzip.unGzipToString;

/**
 * @author Liam Newman
 */
public class GitHubApiWireMockRule extends WireMockRule {

    // By default the wiremock tests will run without proxy or taking a snapshot.
    // The tests will use only the stubbed data and will fail if requests are made for missing data.
    // You can use the proxy without taking a snapshot while writing and debugging tests.
    // You cannot take a snapshot without proxying.
    private final static boolean takeSnapshot = System.getProperty("test.github.takeSnapshot", "false") != "false";
    private final static boolean useProxy = takeSnapshot || System.getProperty("test.github.useProxy", "false") != "false";

    public GitHubApiWireMockRule(WireMockConfiguration options) {
        this(options, true);
    }

    public GitHubApiWireMockRule(WireMockConfiguration options, boolean failOnUnmatchedRequests) {
        super(options
                .extensions(
                    new ResponseTransformer() {
                        @Override
                        public Response transform(Request request, Response response, FileSource files,
                                                  Parameters parameters) {
                            Response.Builder builder = Response.Builder.like(response);
                            Collection<HttpHeader> headers = response.getHeaders().all();
                            HttpHeader linkHeader = response.getHeaders().getHeader("Link");
                            if (linkHeader.isPresent()) {
                                headers.removeIf(item -> item.keyEquals("Link"));
                                headers.add(HttpHeader.httpHeader("Link", linkHeader.firstValue()
                                    .replace("https://api.github.com/",
                                    "http://localhost:" + request.getPort() + "/")));
                            }

                            if ("application/json"
                                .equals(response.getHeaders().getContentTypeHeader().mimeTypePart())) {

                                String body;
                                if (response.getHeaders().getHeader("Content-Encoding").containsValue("gzip")) {
                                    headers.removeIf(item -> item.keyEquals("Content-Encoding"));
                                    body = unGzipToString(response.getBody());
                                } else {
                                    body = response.getBodyAsString();
                                }

                                builder.body(body
                                    .replace("https://api.github.com/",
                                    "http://localhost:" + request.getPort() + "/"));

                            }
                            builder.headers(new HttpHeaders(headers));

                            return builder.build();
                        }

                        @Override
                        public String getName() {
                            return "github-api-url-rewrite";
                        }
                    }),
            failOnUnmatchedRequests);
    }

    public boolean isUseProxy() {
        return GitHubApiWireMockRule.useProxy;
    }

    public boolean isTakeSnapshot() {
        return GitHubApiWireMockRule.takeSnapshot;
    }

    @Override
    protected void before() {
        super.before();
        if (isUseProxy()) {
            this.stubFor(
                proxyAllTo("https://api.github.com/")
                    .atPriority(100)
            );
        } else {
            // Just to be super clear
            this.stubFor(
                any(urlPathMatching(".*"))
                    .willReturn(status(500).withBody("Stubbed data not found. Set test.github.use-proxy to have WireMock proxy to github"))
                    .atPriority(100));
        }
    }

    @Override
    protected void after() {
        super.after();
        // To reformat everything
        //formatJsonFiles(new File("src/test/resources").toPath());

        if (isTakeSnapshot()) {
            this.snapshotRecord(recordSpec()
                .forTarget("https://api.github.com")
                .captureHeader("If-None-Match")
                .extractTextBodiesOver(255));

            // After taking the snapshot, format the output
            formatJsonFiles(new File(this.getOptions().filesRoot().getPath()).toPath());
        }
    }

    private void formatJsonFiles(Path path) {
        // The more consistent we can make the json output the more meaningful it will be.
        // TODO: For understandability, rename the files to include the response order
        Gson g = new Gson().newBuilder().serializeNulls().disableHtmlEscaping().setPrettyPrinting()
            .registerTypeAdapter(Double.class,  new JsonSerializer<Double>() {
                @Override
                public JsonElement serialize(Double src, Type typeOfSrc, JsonSerializationContext context) {
                    if(src == src.longValue())
                        return new JsonPrimitive(src.longValue());
                    return new JsonPrimitive(src);
                }
            })
            .create();

        try {
            Files.walk(path)
                .forEach(filePath -> {
                    try {
                        if (filePath.toString().endsWith(".json")) {
                            String fileText = new String(Files.readAllBytes(filePath));
                            // while recording responses we replaced all github calls localhost
                            // now we reverse that for storage.
                            fileText = fileText.replace(this.baseUrl(),
                                "https://api.github.com");
                            // Can be Array or Map
                            Object parsedObject = g.fromJson(fileText, Object.class);
                            if (parsedObject instanceof Map && filePath.toString().contains("mappings")) {
                                filePath = renameMappingFile(filePath, (Map<String, Object>) parsedObject);
                            }
                            fileText = g.toJson(parsedObject);
                            Files.write(filePath, fileText.getBytes());
                        }
                    } catch (Exception e) {
                        throw new RuntimeException("Files could not be written", e);
                    }
                });
        } catch (IOException e) {
            throw new RuntimeException("Files could not be written");
        }
    }

    private Path renameMappingFile(Path filePath, Map<String, Object> parsedObject) throws IOException {
        Path targetPath = filePath;
        String id = (String)parsedObject.getOrDefault("id", null);
        Long insertionIndex = ((Double)parsedObject.getOrDefault("insertionIndex", 0.0)).longValue();
        if (id != null && insertionIndex > 0) {
            String filePathString = filePath.toString();
            if (filePathString.contains(id)) {
                targetPath = new File(filePathString.replace(id, insertionIndex.toString() + "-" + id.substring(0, 6))).toPath();
                Files.move(filePath, targetPath);
            }
        }

        return targetPath;
    }
}
