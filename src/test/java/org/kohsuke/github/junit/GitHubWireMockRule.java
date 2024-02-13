package org.kohsuke.github.junit;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.ResponseDefinitionBuilder;
import com.github.tomakehurst.wiremock.common.FileSource;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import com.github.tomakehurst.wiremock.extension.Parameters;
import com.github.tomakehurst.wiremock.extension.ResponseDefinitionTransformer;
import com.github.tomakehurst.wiremock.extension.ResponseTransformer;
import com.github.tomakehurst.wiremock.http.*;
import com.github.tomakehurst.wiremock.matching.RequestPatternBuilder;
import com.github.tomakehurst.wiremock.recording.RecordSpecBuilder;
import com.google.gson.*;
import edu.umd.cs.findbugs.annotations.NonNull;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Type;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.annotation.Nonnull;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static com.github.tomakehurst.wiremock.common.Gzip.unGzipToString;

// TODO: Auto-generated Javadoc
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
    private final static boolean testWithOrg = System.getProperty("test.github.org", "true") == "true";
    private final static boolean useProxy = takeSnapshot
            || System.getProperty("test.github.useProxy", "false") != "false";

    private final static Pattern ACTIONS_USER_CONTENT_PATTERN = Pattern
            .compile("https://pipelines[a-z0-9]*\\.actions\\.githubusercontent\\.com", Pattern.CASE_INSENSITIVE);
    private final static Pattern BLOB_CORE_WINDOWS_PATTERN = Pattern
            .compile("https://([a-z0-9]*\\.blob\\.core\\.windows\\.net)", Pattern.CASE_INSENSITIVE);
    private final static String ORIGINAL_HOST = "originalHost";

    /**
     * Customize record spec.
     *
     * @param customizeRecordSpec
     *            the customize record spec
     */
    public void customizeRecordSpec(Consumer<RecordSpecBuilder> customizeRecordSpec) {
        this.customizeRecordSpec = customizeRecordSpec;
    }

    private Consumer<RecordSpecBuilder> customizeRecordSpec = null;

    /**
     * Instantiates a new git hub wire mock rule.
     */
    public GitHubWireMockRule() {
        this(WireMockConfiguration.options());
    }

    /**
     * Instantiates a new git hub wire mock rule.
     *
     * @param options
     *            the options
     */
    public GitHubWireMockRule(WireMockConfiguration options) {
        this(options, true);
    }

    /**
     * Instantiates a new git hub wire mock rule.
     *
     * @param options
     *            the options
     * @param failOnUnmatchedRequests
     *            the fail on unmatched requests
     */
    public GitHubWireMockRule(WireMockConfiguration options, boolean failOnUnmatchedRequests) {
        super(options, failOnUnmatchedRequests);
    }

    /**
     * Api server.
     *
     * @return the wire mock server
     */
    public WireMockServer apiServer() {
        return servers.get("default");
    }

    /**
     * Raw server.
     *
     * @return the wire mock server
     */
    public WireMockServer rawServer() {
        return servers.get("raw");
    }

    /**
     * Uploads server.
     *
     * @return the wire mock server
     */
    public WireMockServer uploadsServer() {
        return servers.get("uploads");
    }

    /**
     * Codeload server.
     *
     * @return the wire mock server
     */
    public WireMockServer codeloadServer() {
        return servers.get("codeload");
    }

    /**
     * Actions user content server.
     *
     * @return the wire mock server
     */
    public WireMockServer actionsUserContentServer() {
        return servers.get("actions-user-content");
    }

    /**
     * Actions user content server.
     *
     * @return the wire mock server
     */
    public WireMockServer blobCoreWindowsNetServer() {
        return servers.get("blob-core-windows-net");
    }

    /**
     * Checks if is use proxy.
     *
     * @return true, if is use proxy
     */
    public boolean isUseProxy() {
        return GitHubWireMockRule.useProxy;
    }

    /**
     * Checks if is take snapshot.
     *
     * @return true, if is take snapshot
     */
    public boolean isTakeSnapshot() {
        return GitHubWireMockRule.takeSnapshot;
    }

    /**
     * Checks if is test with org.
     *
     * @return true, if is test with org
     */
    public boolean isTestWithOrg() {
        return GitHubWireMockRule.testWithOrg;
    }

    /**
     * Initialize servers.
     */
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

        if (new File(apiServer().getOptions().filesRoot().getPath() + "_codeload").exists() || isUseProxy()) {
            initializeServer("codeload");
        }

        if (new File(apiServer().getOptions().filesRoot().getPath() + "_actions-user-content").exists()
                || isUseProxy()) {
            initializeServer("actions-user-content");
        }

        if (new File(apiServer().getOptions().filesRoot().getPath() + "_blob-core-windows-net").exists()
                || isUseProxy()) {
            initializeServer("blob-core-windows-net", new ProxyToOriginalHostTransformer(this));
        }
    }

    /**
     * Before.
     */
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

        if (this.codeloadServer() != null) {
            this.codeloadServer().stubFor(proxyAllTo("https://codeload.github.com").atPriority(100));
        }

        if (this.actionsUserContentServer() != null) {
            this.actionsUserContentServer()
                    .stubFor(proxyAllTo("https://pipelines.actions.githubusercontent.com").atPriority(100));
        }

        if (this.blobCoreWindowsNetServer() != null) {
            this.blobCoreWindowsNetServer()
                    .stubFor(any(anyUrl()).willReturn(aResponse().withTransformers(ProxyToOriginalHostTransformer.NAME))
                            .atPriority(100));
        }
    }

    /**
     * After.
     */
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

        recordSnapshot(this.codeloadServer(), "https://codeload.github.com", true);

        recordSnapshot(this.actionsUserContentServer(), "https://pipelines.actions.githubusercontent.com", true);

        recordSnapshot(this.blobCoreWindowsNetServer(), "https://productionresults.blob.core.windows.net", true);
    }

    private void recordSnapshot(WireMockServer server, String target, boolean isRawServer) {
        if (server != null) {

            final RecordSpecBuilder recordSpecBuilder = recordSpec().forTarget(target)
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
                    .extractTextBodiesOver(255);

            if (customizeRecordSpec != null) {
                customizeRecordSpec.accept(recordSpecBuilder);
            }

            server.snapshotRecord(recordSpecBuilder);

            // After taking the snapshot, format the output
            formatTestResources(new File(server.getOptions().filesRoot().getPath()).toPath(), isRawServer);
        }
    }

    /**
     * Gets the request count.
     *
     * @return the request count
     */
    public int getRequestCount() {
        return getRequestCount(apiServer());
    }

    /**
     * Gets the request count.
     *
     * @param server
     *            the server
     * @return the request count
     */
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
                    // For raw server, only fix up mapping files
                    if (isRawServer && !filePath.toString().contains("mappings")) {
                        return;
                    }

                    if (filePath.toString().endsWith(".json")) {
                        Path renamedFilePath = renameFile(filePath, idToIndex);
                        Path targetFilePath = renamedFilePath == null ? filePath : renamedFilePath;

                        String fileText = new String(Files.readAllBytes(targetFilePath));
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

                        if (this.codeloadServer() != null) {
                            fileText = fileText.replace(this.codeloadServer().baseUrl(), "https://codeload.github.com");
                        }

                        if (this.actionsUserContentServer() != null) {
                            fileText = fileText.replace(this.actionsUserContentServer().baseUrl(),
                                    "https://pipelines.actions.githubusercontent.com");
                        }

                        if (this.blobCoreWindowsNetServer() != null) {
                            fileText = fileText.replace(this.blobCoreWindowsNetServer().baseUrl(),
                                    "https://productionresults.blob.core.windows.net");
                        }

                        // point bodyFile in the mapping to the renamed body file
                        if (renamedFilePath != null && filePath.toString().contains("mappings")) {
                            fileText = fileText.replace(filePath.getFileName().toString(),
                                    renamedFilePath.getFileName().toString());
                        }

                        // Can be Array or Map
                        Object parsedObject = g.fromJson(fileText, Object.class);
                        String outputFileText = g.toJson(parsedObject);
                        Files.write(targetFilePath, outputFileText.getBytes());
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

    private Map.Entry<String, String> getId(String fileName, Map<String, String> idToIndex) throws IOException {
        for (Map.Entry<String, String> item : idToIndex.entrySet()) {
            if (fileName.contains(item.getKey())) {
                return item;
            }
        }
        return null;
    }

    private Path renameFile(Path filePath, Map<String, String> idToIndex) throws IOException {
        Path targetPath = null;
        String fileName = filePath.getFileName().toString();

        // Short early segments of the file name
        // which tend to be "repos_hub4j-test-org_{repository}".
        fileName = fileName.replaceAll("^([a-zA-Z])[^_]+_([a-zA-Z])[^_]+_([a-zA-Z])[^_]+_", "$1_$2_$3_");
        fileName = fileName.replaceAll("^([a-zA-Z])[^_]+_([a-zA-Z])[^_]+_", "$1_$2_");

        Map.Entry<String, String> idToIndexEntry = getId(fileName, idToIndex);
        if (idToIndexEntry != null) {
            fileName = fileName.replace("-" + idToIndexEntry.getKey(), "");
            // put index number on the front for clarity
            fileName = idToIndexEntry.getValue() + "-" + fileName;
        }

        // Replace GUID strings in file paths with abbreviated GUID to limit file path length for windows
        fileName = fileName.replaceAll("(_[a-f0-9]{8})[a-f0-9]{32}([_.])", "$1$2");

        // If the file name is still longer than 60 characters, truncate it
        fileName = fileName.replaceAll("^([^.]{60})[^.]+\\.", "$1.");

        String renamedFilePathString = Paths.get(filePath.getParent().toString(), fileName).toString();
        if (renamedFilePathString != filePath.toString()) {
            targetPath = new File(renamedFilePathString).toPath();
            Files.move(filePath, targetPath);
        }
        return targetPath;
    }

    /**
     * Map to mock git hub.
     *
     * @param body
     *            the body
     * @return the string
     */
    @Nonnull
    public String mapToMockGitHub(String body) {
        body = body.replace("https://api.github.com", this.apiServer().baseUrl());

        body = replaceTargetServerUrl(body, this.rawServer(), "https://raw.githubusercontent.com", "/raw");

        body = replaceTargetServerUrl(body, this.uploadsServer(), "https://uploads.github.com", "/uploads");

        body = replaceTargetServerUrl(body, this.codeloadServer(), "https://codeload.github.com", "/codeload");

        body = replaceTargetServerUrl(body,
                this.actionsUserContentServer(),
                ACTIONS_USER_CONTENT_PATTERN,
                "/actions-user-content");

        body = replaceTargetServerUrl(body,
                this.blobCoreWindowsNetServer(),
                BLOB_CORE_WINDOWS_PATTERN,
                "/blob-core-windows-net");

        return body;
    }

    @NonNull
    private String replaceTargetServerUrl(String body,
            WireMockServer wireMockServer,
            String rawTarget,
            String inactiveTarget) {
        if (wireMockServer != null) {
            body = body.replace(rawTarget, wireMockServer.baseUrl());
        } else {
            body = body.replace(rawTarget, this.apiServer().baseUrl() + inactiveTarget);
        }
        return body;
    }

    @NonNull
    private String replaceTargetServerUrl(String body,
            WireMockServer wireMockServer,
            Pattern regexp,
            String inactiveTarget) {
        if (wireMockServer != null) {
            body = regexp.matcher(body).replaceAll(wireMockServer.baseUrl());
        } else {
            body = regexp.matcher(body).replaceAll(this.apiServer().baseUrl() + inactiveTarget);
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
            fixLocationHeader(response, headers);

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
            // Lists are broken up into pages. The Link header contains urls for previous and next pages.
            HttpHeader linkHeader = response.getHeaders().getHeader("Link");
            if (linkHeader.isPresent()) {
                headers.removeIf(item -> item.keyEquals("Link"));
                headers.add(HttpHeader.httpHeader("Link", rule.mapToMockGitHub(linkHeader.firstValue())));
            }
        }

        private void fixLocationHeader(Response response, Collection<HttpHeader> headers) {
            // For redirects, the Location header points to the new target.
            HttpHeader locationHeader = response.getHeaders().getHeader("Location");
            if (locationHeader.isPresent()) {
                String originalLocationHeaderValue = locationHeader.firstValue();
                String rewrittenLocationHeaderValue = rule.mapToMockGitHub(originalLocationHeaderValue);

                headers.removeIf(item -> item.keyEquals("Location"));

                // in the case of the blob.core.windows.net server, we need to keep the original host around
                // as the host name is dynamic
                // this is a hack as we pass the original host as an additional parameter which will
                // end up in the request we push to the GitHub server but that is the best we can do
                // given Wiremock's infrastructure
                Matcher matcher = BLOB_CORE_WINDOWS_PATTERN.matcher(originalLocationHeaderValue);
                if (matcher.find() && rule.isUseProxy()) {
                    rewrittenLocationHeaderValue += "&" + ORIGINAL_HOST + "=" + matcher.group(1);
                }

                headers.add(HttpHeader.httpHeader("Location", rewrittenLocationHeaderValue));
            }
        }

        @Override
        public String getName() {
            return "github-api-url-rewrite";
        }
    }

    private static class ProxyToOriginalHostTransformer extends ResponseDefinitionTransformer {

        private static final String NAME = "proxy-to-original-host";

        private final GitHubWireMockRule rule;

        private ProxyToOriginalHostTransformer(GitHubWireMockRule rule) {
            this.rule = rule;
        }

        @Override
        public String getName() {
            return NAME;
        }

        @Override
        public ResponseDefinition transform(Request request,
                ResponseDefinition responseDefinition,
                FileSource files,
                Parameters parameters) {
            if (!rule.isUseProxy() || !request.queryParameter(ORIGINAL_HOST).isPresent()) {
                return responseDefinition;
            }

            String originalHost = request.queryParameter(ORIGINAL_HOST).firstValue();

            return ResponseDefinitionBuilder.like(responseDefinition).proxiedFrom("https://" + originalHost).build();
        }
    }
}
