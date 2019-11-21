package org.kohsuke.github.junit;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.common.FileSource;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import com.github.tomakehurst.wiremock.extension.Parameters;
import com.github.tomakehurst.wiremock.extension.ResponseTransformer;
import com.github.tomakehurst.wiremock.http.*;
import com.github.tomakehurst.wiremock.matching.RequestPatternBuilder;
import com.github.tomakehurst.wiremock.verification.*;
import com.google.gson.*;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Type;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collection;
import java.util.Map;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static com.github.tomakehurst.wiremock.common.Gzip.unGzipToString;

/**
 * The standard WireMockRule eagerly initializes a WireMockServer. This version suptakes a laze approach allowing us to
 * automatically isolate snapshots for each method.
 *
 * @author Liam Newman
 */
public class GitHubWireMockRule extends WireMockMultiServerRule {

    // By default the wiremock tests will run without proxy or taking a snapshot.
    // The tests will use only the stubbed data and will fail if requests are made for missing data.
    // You can use the proxy without taking a snapshot while writing and debugging tests.
    // You cannot take a snapshot without proxying.
    private final static boolean takeSnapshot = System.getProperty("test.github.takeSnapshot", "false") != "false";
    private final static boolean useProxy = takeSnapshot
            || System.getProperty("test.github.useProxy", "false") != "false";

    public GitHubWireMockRule() {
        this(WireMockConfiguration.options());
    }

    public GitHubWireMockRule(WireMockConfiguration options) {
        this(options, true);
    }

    public GitHubWireMockRule(WireMockConfiguration options, boolean failOnUnmatchedRequests) {
        super(options, failOnUnmatchedRequests);
    }

    public WireMockServer apiServer() {
        return servers.get("default");
    }

    public WireMockServer rawServer() {
        return servers.get("raw");
    }

    public WireMockServer uploadsServer() {
        return servers.get("uploads");
    }

    public boolean isUseProxy() {
        return GitHubWireMockRule.useProxy;
    }

    public boolean isTakeSnapshot() {
        return GitHubWireMockRule.takeSnapshot;
    }

    @Override
    protected void initializeServers() {
        super.initializeServers();
        initializeServer("uploads");
        initializeServer("raw");
        initializeServer("default", new GitHubApiResponseTransformer(this));
    }

    @Override
    protected void before() {
        super.before();
        if (isUseProxy()) {
            this.uploadsServer().stubFor(proxyAllTo("https://uploads.github.com").atPriority(100));
            this.apiServer().stubFor(proxyAllTo("https://api.github.com").atPriority(100));
            this.rawServer().stubFor(proxyAllTo("https://raw.githubusercontent.com").atPriority(100));
        }
    }

    @Override
    protected void after() {
        super.after();
        if (isTakeSnapshot()) {
            this.apiServer()
                    .snapshotRecord(recordSpec().forTarget("https://api.github.com")
                            .captureHeader("If-None-Match")
                            .extractTextBodiesOver(255));

            this.rawServer()
                    .snapshotRecord(recordSpec().forTarget("https://raw.githubusercontent.com")
                            .captureHeader("If-None-Match")
                            .extractTextBodiesOver(255));

            this.uploadsServer()
                    .snapshotRecord(recordSpec().forTarget("https://uploads.github.com")
                            .captureHeader("If-None-Match")
                            .extractTextBodiesOver(255));

            // After taking the snapshot, format the output
            formatJsonFiles(new File(this.apiServer().getOptions().filesRoot().getPath()).toPath());

            // For raw server, only fix up mapping files
            formatJsonFiles(new File(this.rawServer().getOptions().filesRoot().child("mappings").getPath()).toPath());

            // For uploads server, only fix up mapping files
            formatJsonFiles(
                    new File(this.uploadsServer().getOptions().filesRoot().child("mappings").getPath()).toPath());
        }
    }

    public int getRequestCount() {
        return getRequestCount(apiServer());
    }

    public static int getRequestCount(WireMockServer server) {
        return server.countRequestsMatching(RequestPatternBuilder.allRequests().build()).getCount();
    }

    private void formatJsonFiles(Path path) {
        // The more consistent we can make the json output the more meaningful it will be.
        Gson g = new Gson().newBuilder()
                .serializeNulls()
                .disableHtmlEscaping()
                .setPrettyPrinting()
                .registerTypeAdapter(Double.class, new JsonSerializer<Double>() {
                    @Override
                    public JsonElement serialize(Double src, Type typeOfSrc, JsonSerializationContext context) {
                        // Gson by default output numbers as doubles - 0.0
                        // Remove the tailing .0, as most most numbers are integer value
                        if (src == src.longValue())
                            return new JsonPrimitive(src.longValue());
                        return new JsonPrimitive(src);
                    }
                })
                .create();

        try {
            Files.walk(path).forEach(filePath -> {
                try {
                    if (filePath.toString().endsWith(".json")) {
                        String fileText = new String(Files.readAllBytes(filePath));
                        // while recording responses we replaced all github calls localhost
                        // now we reverse that for storage.
                        fileText = fileText.replace(this.apiServer().baseUrl(), "https://api.github.com")
                                .replace(this.uploadsServer().baseUrl(), "https://uploads.github.com")
                                .replace(this.rawServer().baseUrl(), "https://raw.githubusercontent.com");
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
        // Shorten the file names
        // For understandability, rename the files to include the response order
        Path targetPath = filePath;
        String id = (String) parsedObject.getOrDefault("id", null);
        Long insertionIndex = ((Double) parsedObject.getOrDefault("insertionIndex", 0.0)).longValue();
        if (id != null && insertionIndex > 0) {
            String filePathString = filePath.toString();
            if (filePathString.contains(id)) {
                targetPath = new File(filePathString.replace(id, insertionIndex.toString() + "-" + id.substring(0, 6)))
                        .toPath();
                Files.move(filePath, targetPath);
            }
        }

        return targetPath;
    }

    /**
     * A number of modifications are needed as runtime to make responses target the WireMock server and not accidentally
     * switch to using the live github servers.
     */
    private static class GitHubApiResponseTransformer extends ResponseTransformer {
        private final GitHubWireMockRule rule;

        public GitHubApiResponseTransformer(GitHubWireMockRule rule) {
            this.rule = rule;
        }

        @Override
        public Response transform(Request request, Response response, FileSource files, Parameters parameters) {
            Response.Builder builder = Response.Builder.like(response);
            Collection<HttpHeader> headers = response.getHeaders().all();

            fixListTraversalHeader(response, headers);

            if ("application/json".equals(response.getHeaders().getContentTypeHeader().mimeTypePart())) {

                String body;
                body = getBodyAsString(response, headers);

                builder.body(body.replace("https://api.github.com", rule.apiServer().baseUrl())
                        .replace("https://uploads.github.com", rule.uploadsServer().baseUrl())
                        .replace("https://raw.githubusercontent.com", rule.rawServer().baseUrl()));

            }
            builder.headers(new HttpHeaders(headers));

            return builder.build();
        }

        private String getBodyAsString(Response response, Collection<HttpHeader> headers) {
            String body;
            if (response.getHeaders().getHeader("Content-Encoding").containsValue("gzip")) {
                headers.removeIf(item -> item.keyEquals("Content-Encoding"));
                body = unGzipToString(response.getBody());
            } else {
                body = response.getBodyAsString();
            }
            return body;
        }

        private void fixListTraversalHeader(Response response, Collection<HttpHeader> headers) {
            HttpHeader linkHeader = response.getHeaders().getHeader("Link");
            if (linkHeader.isPresent()) {
                headers.removeIf(item -> item.keyEquals("Link"));
                headers.add(HttpHeader.httpHeader("Link",
                        linkHeader.firstValue().replace("https://api.github.com", rule.apiServer().baseUrl())));
            }
        }

        @Override
        public String getName() {
            return "github-api-url-rewrite";
        }
    }

}
