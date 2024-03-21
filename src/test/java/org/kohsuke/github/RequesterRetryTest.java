package org.kohsuke.github;

import com.github.tomakehurst.wiremock.http.Fault;
import com.github.tomakehurst.wiremock.stubbing.Scenario;
import okhttp3.ConnectionPool;
import okhttp3.OkHttpClient;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.junit.Assume;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.kohsuke.github.connector.GitHubConnector;
import org.kohsuke.github.connector.GitHubConnectorRequest;
import org.kohsuke.github.connector.GitHubConnectorResponse;
import org.kohsuke.github.extras.HttpClientGitHubConnector;
import org.kohsuke.github.extras.okhttp3.OkHttpGitHubConnector;
import org.kohsuke.github.internal.DefaultGitHubConnector;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;
import java.util.logging.StreamHandler;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;
import javax.net.ssl.SSLHandshakeException;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.hamcrest.Matchers.*;

// TODO: Auto-generated Javadoc
/**
 * The Class RequesterRetryTest.
 *
 * @author Victor Martinez
 */
public class RequesterRetryTest extends AbstractGitHubWireMockTest {

    private static Logger log = Logger.getLogger(GitHubClient.class.getName()); // matches the logger in the affected
                                                                                // class
    private static OutputStream logCapturingStream;
    private static StreamHandler customLogHandler;

    /** The connection. */
    HttpURLConnection connection;

    /** The base request count. */
    int baseRequestCount;

    /**
     * Instantiates a new requester retry test.
     */
    public RequesterRetryTest() {
        useDefaultGitHub = false;
    }

    /**
     * Gets the repository.
     *
     * @return the repository
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     */
    protected GHRepository getRepository() throws IOException {
        return getRepository(gitHub);
    }

    private GHRepository getRepository(GitHub gitHub) throws IOException {
        return gitHub.getOrganization("hub4j-test-org").getRepository("github-api");
    }

    /**
     * Attach log capturer.
     */
    @Before
    public void attachLogCapturer() {
        logCapturingStream = new ByteArrayOutputStream();
        customLogHandler = new StreamHandler(logCapturingStream, new SimpleFormatter());
        Logger.getLogger(GitHubClient.class.getName()).addHandler(customLogHandler);
        Logger.getLogger(OkHttpClient.class.getName()).addHandler(customLogHandler);
    }

    /**
     * Gets the test captured log.
     *
     * @return the test captured log
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     */
    public String getTestCapturedLog() throws IOException {
        customLogHandler.flush();
        return logCapturingStream.toString();
    }

    /**
     * Reset test captured log.
     *
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     */
    public void resetTestCapturedLog() throws IOException {
        Logger.getLogger(GitHubClient.class.getName()).removeHandler(customLogHandler);
        Logger.getLogger(OkHttpClient.class.getName()).removeHandler(customLogHandler);
        customLogHandler.close();
        attachLogCapturer();
    }

    /**
     * Test git hub is api url valid.
     *
     * @throws Exception
     *             the exception
     */
    @Ignore("Used okhttp3 and this to verify connection closing. Too flaky for CI system.")
    @Test
    public void testGitHubIsApiUrlValid() throws Exception {

        OkHttpClient client = new OkHttpClient().newBuilder()
                .connectionPool(new ConnectionPool(2, 100, TimeUnit.MILLISECONDS))
                .build();

        OkHttpGitHubConnector connector = new OkHttpGitHubConnector(client);

        for (int x = 0; x < 100; x++) {

            this.gitHub = getGitHubBuilder().withEndpoint(mockGitHub.apiServer().baseUrl())
                    .withConnector(connector)
                    .build();

            try {
                gitHub.checkApiUrlValidity();
            } catch (IOException ioe) {
                assertThat(ioe.getMessage(), containsString("private mode enabled"));
            }
            Thread.sleep(100);
        }

        String capturedLog = getTestCapturedLog();
        assertThat(capturedLog, not(containsString("leaked")));
    }

    /**
     * Test socket connection and retry.
     *
     * @throws Exception
     *             the exception
     */
    // Issue #539
    @Test
    public void testSocketConnectionAndRetry() throws Exception {
        // Only implemented for HttpURLConnection connectors
        Assume.assumeThat(DefaultGitHubConnector.create(), not(instanceOf(HttpClientGitHubConnector.class)));

        // CONNECTION_RESET_BY_PEER errors result in two requests each
        // to get this failure for "3" tries we have to do 6 queries.
        this.mockGitHub.apiServer()
                .stubFor(get(urlMatching(".+/branches/test/timeout"))
                        .willReturn(aResponse().withFault(Fault.CONNECTION_RESET_BY_PEER)));

        this.gitHub = getGitHubBuilder().withEndpoint(mockGitHub.apiServer().baseUrl()).build();

        GHRepository repo = getRepository();
        baseRequestCount = this.mockGitHub.getRequestCount();
        try {
            repo.getBranch("test/timeout");
            fail();
        } catch (Exception e) {
            assertThat(e, instanceOf(HttpException.class));
        }

        String capturedLog = getTestCapturedLog();
        assertThat(capturedLog, containsString("(2 retries remaining)"));
        assertThat(capturedLog, containsString("(1 retries remaining)"));

        assertThat(this.mockGitHub.getRequestCount(), equalTo(baseRequestCount + 6));
    }

    /**
     * Test socket connection and retry status code.
     *
     * @throws Exception
     *             the exception
     */
    // Issue #539
    @Test
    public void testSocketConnectionAndRetry_StatusCode() throws Exception {
        // Only implemented for HttpURLConnection connectors
        Assume.assumeThat(DefaultGitHubConnector.create(), not(instanceOf(HttpClientGitHubConnector.class)));

        // CONNECTION_RESET_BY_PEER errors result in two requests each
        // to get this failure for "3" tries we have to do 6 queries.
        this.mockGitHub.apiServer()
                .stubFor(get(urlMatching(".+/branches/test/timeout"))
                        .willReturn(aResponse().withFault(Fault.CONNECTION_RESET_BY_PEER)));

        this.gitHub = getGitHubBuilder().withEndpoint(mockGitHub.apiServer().baseUrl()).build();

        baseRequestCount = this.mockGitHub.getRequestCount();
        try {
            // status code is a different code path that should also be covered by this.
            gitHub.createRequest()
                    .withUrlPath("/repos/hub4j-test-org/github-api/branches/test/timeout")
                    .fetchHttpStatusCode();
            fail();
        } catch (Exception e) {
            assertThat(e, instanceOf(HttpException.class));
        }

        String capturedLog = getTestCapturedLog();
        assertThat(capturedLog, containsString("(2 retries remaining)"));
        assertThat(capturedLog, containsString("(1 retries remaining)"));

        assertThat(this.mockGitHub.getRequestCount(), equalTo(baseRequestCount + 6));
    }

    /**
     * Test socket connection and retry success.
     *
     * @throws Exception
     *             the exception
     */
    @Test
    public void testSocketConnectionAndRetry_Success() throws Exception {
        // Only implemented for HttpURLConnection connectors
        Assume.assumeThat(DefaultGitHubConnector.create(), not(instanceOf(HttpClientGitHubConnector.class)));

        // CONNECTION_RESET_BY_PEER errors result in two requests each
        // to get this failure for "3" tries we have to do 6 queries.
        // If there are only 5 errors we succeed.
        this.mockGitHub.apiServer()
                .stubFor(get(urlMatching(".+/branches/test/timeout")).atPriority(0)
                        .inScenario("Retry")
                        .whenScenarioStateIs(Scenario.STARTED)
                        .willReturn(aResponse().withFault(Fault.CONNECTION_RESET_BY_PEER)))
                .setNewScenarioState("Retry-1");
        this.mockGitHub.apiServer()
                .stubFor(get(urlMatching(".+/branches/test/timeout")).atPriority(0)
                        .inScenario("Retry")
                        .whenScenarioStateIs("Retry-1")
                        .willReturn(aResponse().withFault(Fault.CONNECTION_RESET_BY_PEER)))
                .setNewScenarioState("Retry-2");
        this.mockGitHub.apiServer()
                .stubFor(get(urlMatching(".+/branches/test/timeout")).atPriority(0)
                        .inScenario("Retry")
                        .whenScenarioStateIs("Retry-2")
                        .willReturn(aResponse().withFault(Fault.CONNECTION_RESET_BY_PEER)))
                .setNewScenarioState("Retry-3");
        this.mockGitHub.apiServer()
                .stubFor(get(urlMatching(".+/branches/test/timeout")).atPriority(0)
                        .inScenario("Retry")
                        .whenScenarioStateIs("Retry-3")
                        .willReturn(aResponse().withFault(Fault.CONNECTION_RESET_BY_PEER)))
                .setNewScenarioState("Retry-4");
        this.mockGitHub.apiServer()
                .stubFor(get(urlMatching(".+/branches/test/timeout")).atPriority(0)
                        .atPriority(0)
                        .inScenario("Retry")
                        .whenScenarioStateIs("Retry-4")
                        .willReturn(aResponse().withFault(Fault.CONNECTION_RESET_BY_PEER)))
                .setNewScenarioState("Retry-5");

        this.gitHub = getGitHubBuilder().withEndpoint(mockGitHub.apiServer().baseUrl()).build();

        GHRepository repo = getRepository();
        baseRequestCount = this.mockGitHub.getRequestCount();
        GHBranch branch = repo.getBranch("test/timeout");
        assertThat(branch, notNullValue());
        String capturedLog = getTestCapturedLog();
        assertThat(capturedLog, containsString("(2 retries remaining)"));
        assertThat(capturedLog, containsString("(1 retries remaining)"));

        assertThat(this.mockGitHub.getRequestCount(), equalTo(baseRequestCount + 6));
    }

    /**
     * Test response code failure exceptions.
     *
     * @throws Exception
     *             the exception
     */
    @Test
    public void testResponseCodeFailureExceptions() throws Exception {
        // No retry for these Exceptions
        GitHubConnector connector = new SendThrowingGitHubConnector<>(() -> {
            throw new IOException("Custom");
        });
        this.gitHub = getGitHubBuilder().withEndpoint(mockGitHub.apiServer().baseUrl())
                .withConnector(connector)
                .build();

        resetTestCapturedLog();
        baseRequestCount = this.mockGitHub.getRequestCount();
        try {
            this.gitHub.getOrganization(GITHUB_API_TEST_ORG);
            fail();
        } catch (Exception e) {
            assertThat(e, instanceOf(HttpException.class));
            assertThat(e.getCause(), instanceOf(IOException.class));
            assertThat(e.getCause().getMessage(), is("Custom"));
            String capturedLog = getTestCapturedLog();
            assertThat(capturedLog, not(containsString("retries remaining")));
            assertThat(this.mockGitHub.getRequestCount(), equalTo(baseRequestCount));
        }

        connector = new SendThrowingGitHubConnector<>(() -> {
            throw new FileNotFoundException("Custom");
        });
        this.gitHub = getGitHubBuilder().withEndpoint(mockGitHub.apiServer().baseUrl())
                .withConnector(connector)
                .build();

        resetTestCapturedLog();
        baseRequestCount = this.mockGitHub.getRequestCount();
        try {
            this.gitHub.getOrganization(GITHUB_API_TEST_ORG);
            fail();
        } catch (Exception e) {
            assertThat(e, instanceOf(FileNotFoundException.class));
            assertThat(e.getMessage(), is("Custom"));
            String capturedLog = getTestCapturedLog();
            assertThat(capturedLog, not(containsString("retries remaining")));
            assertThat(this.mockGitHub.getRequestCount(), equalTo(baseRequestCount));
        }
    }

    /**
     * Test input stream failure exceptions.
     *
     * @throws Exception
     *             the exception
     */
    @Test
    public void testInputStreamFailureExceptions() throws Exception {
        // No retry for these Exceptions
        GitHubConnector connector = new BodyStreamThrowingGitHubConnector<>(() -> {
            throw new IOException("Custom");
        });
        this.gitHub = getGitHubBuilder().withEndpoint(mockGitHub.apiServer().baseUrl())
                .withConnector(connector)
                .build();

        resetTestCapturedLog();
        baseRequestCount = this.mockGitHub.getRequestCount();
        try {
            this.gitHub.getOrganization(GITHUB_API_TEST_ORG);
            fail();
        } catch (Exception e) {
            assertThat(e, instanceOf(HttpException.class));
            assertThat(e.getCause(), instanceOf(IOException.class));
            assertThat(e.getCause().getMessage(), is("Custom"));
            String capturedLog = getTestCapturedLog();
            assertThat(capturedLog, not(containsString("retries remaining")));
            assertThat(this.mockGitHub.getRequestCount(), equalTo(baseRequestCount + 1));
        }

        // FileNotFound doesn't need a special connector
        this.gitHub = getGitHubBuilder().withEndpoint(mockGitHub.apiServer().baseUrl()).build();

        resetTestCapturedLog();
        baseRequestCount = this.mockGitHub.getRequestCount();
        try {
            this.gitHub.getOrganization(GITHUB_API_TEST_ORG + "-missing");
            fail();
        } catch (Exception e) {
            assertThat(e, instanceOf(GHFileNotFoundException.class));
            assertThat(e.getCause(), instanceOf(FileNotFoundException.class));
            assertThat(e.getCause().getMessage(), containsString("hub4j-test-org-missing"));
            String capturedLog = getTestCapturedLog();
            assertThat(capturedLog, not(containsString("retries remaining")));
            assertThat(this.mockGitHub.getRequestCount(), equalTo(baseRequestCount + 1));
        }

        // FileNotFound doesn't need a special connector
        this.gitHub = getGitHubBuilder().withEndpoint(mockGitHub.apiServer().baseUrl()).build();

        resetTestCapturedLog();
        baseRequestCount = this.mockGitHub.getRequestCount();
        assertThat(
                this.gitHub.createRequest()
                        .withUrlPath("/orgs/" + GITHUB_API_TEST_ORG + "-missing")
                        .fetchHttpStatusCode(),
                equalTo(404));
        String capturedLog = getTestCapturedLog();
        assertThat(capturedLog, not(containsString("retries remaining")));
        assertThat(this.mockGitHub.getRequestCount(), equalTo(baseRequestCount + 1));
    }

    /**
     * Test response code connection exceptions.
     *
     * @throws Exception
     *             the exception
     */
    @Test
    public void testResponseCodeConnectionExceptions() throws Exception {
        // Because the test throws at the very start of send(), there is only one connection for 3 retries
        GitHubConnector connector = new SendThrowingGitHubConnector<>(() -> {
            throw new SocketException();
        });
        runConnectionExceptionTest(connector, 1);
        runConnectionExceptionStatusCodeTest(connector, 1);

        connector = new SendThrowingGitHubConnector<>(() -> {
            throw new SocketTimeoutException();
        });
        runConnectionExceptionTest(connector, 1);
        runConnectionExceptionStatusCodeTest(connector, 1);

        connector = new SendThrowingGitHubConnector<>(() -> {
            throw new SSLHandshakeException("TestFailure");
        });
        runConnectionExceptionTest(connector, 1);
        runConnectionExceptionStatusCodeTest(connector, 1);
    }

    /**
     * Test input stream connection exceptions.
     *
     * @throws Exception
     *             the exception
     */
    @Test
    public void testInputStreamConnectionExceptions() throws Exception {
        // InputStream is where most exceptions get thrown whether connection or simple FNF
        // Because the test throws after send(), there is one connection for each retry
        // However, getStatusCode never calls that and so it does succeed
        GitHubConnector connector = new BodyStreamThrowingGitHubConnector<>(() -> {
            throw new SocketException();
        });
        runConnectionExceptionTest(connector, 3);
        runConnectionExceptionStatusCodeTest(connector, 0);

        connector = new BodyStreamThrowingGitHubConnector<>(() -> {
            throw new SocketTimeoutException();
        });
        runConnectionExceptionTest(connector, 3);
        runConnectionExceptionStatusCodeTest(connector, 0);

        connector = new BodyStreamThrowingGitHubConnector<>(() -> {
            throw new SSLHandshakeException("TestFailure");
        });
        runConnectionExceptionTest(connector, 3);
        runConnectionExceptionStatusCodeTest(connector, 0);
    }

    private void runConnectionExceptionTest(GitHubConnector connector, int expectedRequestCount) throws IOException {
        this.gitHub = getGitHubBuilder().withEndpoint(mockGitHub.apiServer().baseUrl())
                .withConnector(connector)
                .build();

        resetTestCapturedLog();
        baseRequestCount = this.mockGitHub.getRequestCount();
        assertThat(this.gitHub.getOrganization(GITHUB_API_TEST_ORG), is(notNullValue()));
        String capturedLog = getTestCapturedLog();
        assertThat(capturedLog, containsString("(2 retries remaining)"));
        assertThat(capturedLog, containsString("(1 retries remaining)"));
        assertThat(this.mockGitHub.getRequestCount(), equalTo(baseRequestCount + expectedRequestCount));

        resetTestCapturedLog();
        baseRequestCount = this.mockGitHub.getRequestCount();
        this.gitHub.createRequest().withUrlPath("/orgs/" + GITHUB_API_TEST_ORG).send();
        capturedLog = getTestCapturedLog();
        assertThat(capturedLog, containsString("(2 retries remaining)"));
        assertThat(capturedLog, containsString("(1 retries remaining)"));
        assertThat(this.mockGitHub.getRequestCount(), equalTo(baseRequestCount + expectedRequestCount));
    }

    private void runConnectionExceptionStatusCodeTest(GitHubConnector connector, int expectedRequestCount)
            throws IOException {
        // now wire in the connector
        this.gitHub = getGitHubBuilder().withEndpoint(mockGitHub.apiServer().baseUrl())
                .withConnector(connector)
                .build();

        resetTestCapturedLog();
        baseRequestCount = this.mockGitHub.getRequestCount();
        assertThat(this.gitHub.createRequest().withUrlPath("/orgs/" + GITHUB_API_TEST_ORG).fetchHttpStatusCode(),
                equalTo(200));
        String capturedLog = getTestCapturedLog();
        if (expectedRequestCount > 0) {
            assertThat(capturedLog, containsString("(2 retries remaining)"));
            assertThat(capturedLog, containsString("(1 retries remaining)"));
            assertThat(this.mockGitHub.getRequestCount(), equalTo(baseRequestCount + expectedRequestCount));
        } else {
            // Success without retries
            assertThat(capturedLog, not(containsString("retries remaining")));
            assertThat(this.mockGitHub.getRequestCount(), equalTo(baseRequestCount + 1));
        }
    }

    /**
     * The Class ResponseCodeThrowingGitHubConnector.
     *
     * @param <E>
     *            the element type
     */
    static class SendThrowingGitHubConnector<E extends IOException> extends HttpClientGitHubConnector {

        final int[] count = { 0 };

        private final Thrower<E> thrower;

        /**
         * Instantiates a new response code throwing http connector.
         *
         * @param thrower
         *            the thrower
         */
        SendThrowingGitHubConnector(final Thrower<E> thrower) {
            super();
            this.thrower = thrower;
        }

        @Override
        public GitHubConnectorResponse send(GitHubConnectorRequest connectorRequest) throws IOException {
            if (connectorRequest.url().toString().contains(GITHUB_API_TEST_ORG)) {
                count[0]++;
                // throwing before we call super.send() simulates error
                if (count[0] % 3 != 0) {
                    thrower.throwError();
                }
            }

            GitHubConnectorResponse response = super.send(connectorRequest);
            return new GitHubConnectorResponseWrapper(response);
        }

    }

    /**
     * The Class InputStreamThrowingHttpConnector.
     *
     * @param <E>
     *            the element type
     */
    static class BodyStreamThrowingGitHubConnector<E extends IOException> extends HttpClientGitHubConnector {

        final int[] count = { 0 };

        private final Thrower<E> thrower;

        /**
         * Instantiates a new input stream throwing http connector.
         *
         * @param thrower
         *            the thrower
         */
        BodyStreamThrowingGitHubConnector(final Thrower<E> thrower) {
            super();
            this.thrower = thrower;
        }

        @Override
        public GitHubConnectorResponse send(GitHubConnectorRequest connectorRequest) throws IOException {
            if (connectorRequest.url().toString().contains(GITHUB_API_TEST_ORG)) {
                count[0]++;
            }
            GitHubConnectorResponse response = super.send(connectorRequest);
            return new GitHubConnectorResponseWrapper(response) {
                @NotNull
                @Override
                public InputStream bodyStream() throws IOException {
                    if (response.request().url().toString().contains(GITHUB_API_TEST_ORG)) {
                        if (count[0] % 3 != 0) {
                            thrower.throwError();
                        }
                    }
                    return super.bodyStream();
                }
            };
        }
    }

    private static final GitHubConnectorRequest IGNORED_EMPTY_REQUEST = new GitHubConnectorRequest() {
        @NotNull
        @Override
        public String method() {
            return null;
        }

        @NotNull
        @Override
        public Map<String, List<String>> allHeaders() {
            return null;
        }

        @Nullable
        @Override
        public String header(String name) {
            return null;
        }

        @Nullable
        @Override
        public String contentType() {
            return null;
        }

        @Nullable
        @Override
        public InputStream body() {
            return null;
        }

        @NotNull
        @Override
        public URL url() {
            return null;
        }

        @Override
        public boolean hasBody() {
            return false;
        }
    };

    private static class GitHubConnectorResponseWrapper extends GitHubConnectorResponse {

        private final GitHubConnectorResponse wrapped;

        GitHubConnectorResponseWrapper(GitHubConnectorResponse response) {
            super(IGNORED_EMPTY_REQUEST, -1, new HashMap<>());
            wrapped = response;
        }

        @CheckForNull
        @Override
        public String header(String name) {
            return wrapped.header(name);
        }

        @NotNull
        @Override
        public InputStream bodyStream() throws IOException {
            return wrapped.bodyStream();
        }

        @Nonnull
        @Override
        public GitHubConnectorRequest request() {
            return wrapped.request();
        }

        @Override
        public int statusCode() {
            return wrapped.statusCode();
        }

        @Nonnull
        @Override
        public Map<String, List<String>> allHeaders() {
            return wrapped.allHeaders();
        }

        @Override
        public void close() throws IOException {
            wrapped.close();
        }
    }

    /**
     * The Interface Thrower.
     *
     * @param <E>
     *            the element type
     */
    @FunctionalInterface
    public interface Thrower<E extends Throwable> {

        /**
         * Throw error.
         *
         * @throws E
         *             the e
         */
        void throwError() throws E;
    }
}
