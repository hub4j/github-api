package org.kohsuke.github.junit;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.common.FileSource;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import com.github.tomakehurst.wiremock.extension.Parameters;
import com.github.tomakehurst.wiremock.extension.ResponseTransformer;
import com.github.tomakehurst.wiremock.http.*;
import com.github.tomakehurst.wiremock.matching.RequestPatternBuilder;
import com.google.gson.*;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Type;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import javax.annotation.Nonnull;

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
        initializeServer("default", new GitHubApiResponseTransformer(this));

        // only start non-api servers if we might need them
        if (new File(apiServer().getOptions().filesRoot().getPath() + "_raw").exists() || isUseProxy()) {
            initializeServer("raw");
        }
        if (new File(apiServer().getOptions().filesRoot().getPath() + "_uploads").exists() || isUseProxy()) {
            initializeServer("uploads");
        }
    }

    @Override
    protected void before() {
        super.before();
        if (!isUseProxy()) {
            return;
        }

        this.apiServer().stubFor(proxyAllTo("https://api.github.com").atPriority(100));

        if (this.rawServer() != null) {
            this.rawServer().stubFor(proxyAllTo("https://raw.githubusercontent.com").atPriority(100));
        }

        if (this.uploadsServer() != null) {
            this.uploadsServer().stubFor(proxyAllTo("https://uploads.github.com").atPriority(100));
        }
    }

    @Override
    protected void after() {
        super.after();
        if (!isTakeSnapshot()) {
            return;
        }

        recordSnapshot(this.apiServer(), "https://api.github.com", false);

        // For raw server, only fix up mapping files
        recordSnapshot(this.rawServer(), "https://raw.githubusercontent.com", true);

        recordSnapshot(this.uploadsServer(), "https://uploads.github.com", false);
    }

    private void recordSnapshot(WireMockServer server, String target, boolean isRawServer) {
        if (server != null) {

            server.snapshotRecord(recordSpec().forTarget(target)
                    // "If-None-Match" header used for ETag matching for caching connections
                    .captureHeader("If-None-Match")
                    // "If-Modified-Since" header used for ETag matching for caching connections
                    .captureHeader("If-Modified-Since")
                    .captureHeader("Cache-Control")
                    // "Accept" header is used to specify previews. If it changes expected data may not be retrieved.
                    .captureHeader("Accept")
                    // This is required, or some requests will return data from unexpected stubs
                    // For example, if you update "title" and "body", and then update just "title" to the same value
                    // the mock framework will treat those two requests as equivalent, which we do not want.
                    .chooseBodyMatchTypeAutomatically(true, false, false)
                    .extractTextBodiesOver(255));

            // After taking the snapshot, format the output
            formatTestResources(new File(this.apiServer().getOptions().filesRoot().getPath()).toPath(), false);
        }
    }

    public int getRequestCount() {
        return getRequestCount(apiServer());
    }

    public static int getRequestCount(WireMockServer server) {
        return server.countRequestsMatching(RequestPatternBuilder.allRequests().build()).getCount();
    }

    private void formatTestResources(Path path, boolean isRawServer) {
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
            Map<String, String> idToIndex = new HashMap<>();

            // Match all the ids to request indexes
            Files.walk(path).forEach(filePath -> {
                try {
                    if (filePath.toString().endsWith(".json") && filePath.toString().contains("/mappings/")) {
                        String fileText = new String(Files.readAllBytes(filePath));
                        Object parsedObject = g.fromJson(fileText, Object.class);
                        addMappingId((Map<String, Object>) parsedObject, idToIndex);
                    }
                } catch (Exception e) {
                    throw new RuntimeException("Files could not be read: " + filePath.toString(), e);
                }
            });

            // Update all
            Files.walk(path).forEach(filePath -> {
                try {
                    Map.Entry<String, String> entry = getId(filePath, idToIndex);
                    if (entry != null) {
                        filePath = renameFileToIndex(filePath, entry);
                    }
                    // For raw server, only fix up mapping files
                    if (isRawServer && !filePath.toString().contains("mappings")) {
                        return;
                    }
                    if (filePath.toString().endsWith(".json")) {
                        String fileText = new String(Files.readAllBytes(filePath));
                        // while recording responses we replaced all github calls localhost
                        // now we reverse that for storage.
                        fileText = fileText.replace(this.apiServer().baseUrl(), "https://api.github.com");

                        if (this.rawServer() != null) {
                            fileText = fileText.replace(this.rawServer().baseUrl(),
                                    "https://raw.githubusercontent.com");
                        }

                        if (this.uploadsServer() != null) {
                            fileText = fileText.replace(this.uploadsServer().baseUrl(), "https://uploads.github.com");
                        }

                        // point bodyFile in the mapping to the renamed body file
                        if (entry != null && filePath.toString().contains("mappings")) {
                            fileText = fileText.replace("-" + entry.getKey(), "-" + entry.getValue());
                        }

                        // Can be Array or Map
                        Object parsedObject = g.fromJson(fileText, Object.class);
                        fileText = g.toJson(parsedObject);
                        Files.write(filePath, fileText.getBytes());
                    }
                } catch (Exception e) {
                    throw new RuntimeException("Files could not be written: " + filePath.toString(), e);
                }
            });
        } catch (IOException e) {
            throw new RuntimeException("Files could not be written");
        }
    }

    private void addMappingId(Map<String, Object> parsedObject, Map<String, String> idToIndex) {
        String id = (String) parsedObject.getOrDefault("id", null);
        long insertionIndex = ((Double) parsedObject.getOrDefault("insertionIndex", 0.0)).longValue();
        if (id != null && insertionIndex > 0) {
            idToIndex.put(id, Long.toString(insertionIndex));
        }
    }

    private Map.Entry<String, String> getId(Path filePath, Map<String, String> idToIndex) throws IOException {
        Path targetPath = filePath;
        String filePathString = filePath.toString();
        for (Map.Entry<String, String> item : idToIndex.entrySet()) {
            if (filePathString.contains(item.getKey())) {
                return item;
            }
        }
        return null;
    }

    private Path renameFileToIndex(Path filePath, Map.Entry<String, String> idToIndex) throws IOException {
        String filePathString = filePath.toString();
        Path targetPath = new File(filePathString.replace(idToIndex.getKey(), idToIndex.getValue())).toPath();
        Files.move(filePath, targetPath);

        return targetPath;
    }

    @Nonnull
    public String mapToMockGitHub(String body) {
        body = body.replace("https://api.github.com", this.apiServer().baseUrl());

        if (this.rawServer() != null) {
            body = body.replace("https://raw.githubusercontent.com", this.rawServer().baseUrl());
        } else {
            body = body.replace("https://raw.githubusercontent.com", this.apiServer().baseUrl() + "/raw");
        }

        if (this.uploadsServer() != null) {
            body = body.replace("https://uploads.github.com", this.uploadsServer().baseUrl());
        } else {
            body = body.replace("https://uploads.github.com", this.apiServer().baseUrl() + "/uploads");
        }
        return body;
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
                body = rule.mapToMockGitHub(body);

                builder.body(body);

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
