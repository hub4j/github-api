package org.kohsuke.github;

import com.github.tomakehurst.wiremock.http.Fault;
import com.github.tomakehurst.wiremock.stubbing.Scenario;
import okhttp3.ConnectionPool;
import okhttp3.OkHttpClient;
import org.junit.Assume;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.kohsuke.github.extras.HttpClientGitHubConnector;
import org.kohsuke.github.extras.ImpatientHttpConnector;
import org.kohsuke.github.extras.okhttp3.OkHttpGitHubConnector;
import org.kohsuke.github.internal.DefaultGitHubConnector;
import org.mockito.Mockito;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.ProtocolException;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.security.Permission;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;
import java.util.logging.StreamHandler;

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
     * @throws IOException Signals that an I/O exception has occurred.
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
     * @throws IOException Signals that an I/O exception has occurred.
     */
    public String getTestCapturedLog() throws IOException {
        customLogHandler.flush();
        return logCapturingStream.toString();
    }

    /**
     * Reset test captured log.
     *
     * @throws IOException Signals that an I/O exception has occurred.
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
     * @throws Exception the exception
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
     * @throws Exception the exception
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
        assertThat(capturedLog.contains("will try 2 more time"), is(true));
        assertThat(capturedLog.contains("will try 1 more time"), is(true));

        assertThat(this.mockGitHub.getRequestCount(), equalTo(baseRequestCount + 6));
    }

    /**
     * Test socket connection and retry status code.
     *
     * @throws Exception the exception
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
        assertThat(capturedLog.contains("will try 2 more time"), is(true));
        assertThat(capturedLog.contains("will try 1 more time"), is(true));

        assertThat(this.mockGitHub.getRequestCount(), equalTo(baseRequestCount + 6));
    }

    /**
     * Test socket connection and retry success.
     *
     * @throws Exception the exception
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
        assertThat(capturedLog.contains("will try 2 more time"), is(true));
        assertThat(capturedLog.contains("will try 1 more time"), is(true));

        assertThat(this.mockGitHub.getRequestCount(), equalTo(baseRequestCount + 6));
    }

    /**
     * Test response code failure exceptions.
     *
     * @throws Exception the exception
     */
    @Test
    public void testResponseCodeFailureExceptions() throws Exception {
        // No retry for these Exceptions
        HttpConnector connector = new ResponseCodeThrowingHttpConnector<>(() -> {
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
            assertThat(capturedLog.contains("will try 2 more time"), is(false));
            assertThat(capturedLog.contains("will try 1 more time"), is(false));
            assertThat(this.mockGitHub.getRequestCount(), equalTo(baseRequestCount));
        }

        connector = new ResponseCodeThrowingHttpConnector<>(() -> {
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
            assertThat(capturedLog.contains("will try 2 more time"), is(false));
            assertThat(capturedLog.contains("will try 1 more time"), is(false));
            assertThat(this.mockGitHub.getRequestCount(), equalTo(baseRequestCount));
        }
    }

    /**
     * Test input stream failure exceptions.
     *
     * @throws Exception the exception
     */
    @Test
    public void testInputStreamFailureExceptions() throws Exception {
        // No retry for these Exceptions
        HttpConnector connector = new InputStreamThrowingHttpConnector<>(() -> {
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
            assertThat(capturedLog.contains("will try 2 more time"), is(false));
            assertThat(capturedLog.contains("will try 1 more time"), is(false));
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
            assertThat(capturedLog.contains("will try 2 more time"), is(false));
            assertThat(capturedLog.contains("will try 1 more time"), is(false));
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
        assertThat(capturedLog.contains("will try 2 more time"), is(false));
        assertThat(capturedLog.contains("will try 1 more time"), is(false));
        assertThat(this.mockGitHub.getRequestCount(), equalTo(baseRequestCount + 1));
    }

    /**
     * Test response code connection exceptions.
     *
     * @throws Exception the exception
     */
    @Test
    public void testResponseCodeConnectionExceptions() throws Exception {
        // Because the test throws at the very start of getResponseCode, there is only one connection for 3 retries
        HttpConnector connector = new ResponseCodeThrowingHttpConnector<>(() -> {
            throw new SocketException();
        });
        runConnectionExceptionTest(connector, 1);
        runConnectionExceptionStatusCodeTest(connector, 1);

        connector = new ResponseCodeThrowingHttpConnector<>(() -> {
            throw new SocketTimeoutException();
        });
        runConnectionExceptionTest(connector, 1);
        runConnectionExceptionStatusCodeTest(connector, 1);

        connector = new ResponseCodeThrowingHttpConnector<>(() -> {
            throw new SSLHandshakeException("TestFailure");
        });
        runConnectionExceptionTest(connector, 1);
        runConnectionExceptionStatusCodeTest(connector, 1);
    }

    /**
     * Test input stream connection exceptions.
     *
     * @throws Exception the exception
     */
    @Test
    public void testInputStreamConnectionExceptions() throws Exception {
        // InputStream is where most exceptions get thrown whether connection or simple FNF
        // Because the test throws after getResponseCode, there is one connection for each retry
        // However, getStatusCode never calls that and so it does succeed
        HttpConnector connector = new InputStreamThrowingHttpConnector<>(() -> {
            throw new SocketException();
        });
        runConnectionExceptionTest(connector, 3);
        runConnectionExceptionStatusCodeTest(connector, 0);

        connector = new InputStreamThrowingHttpConnector<>(() -> {
            throw new SocketTimeoutException();
        });
        runConnectionExceptionTest(connector, 3);
        runConnectionExceptionStatusCodeTest(connector, 0);

        connector = new InputStreamThrowingHttpConnector<>(() -> {
            throw new SSLHandshakeException("TestFailure");
        });
        runConnectionExceptionTest(connector, 3);
        runConnectionExceptionStatusCodeTest(connector, 0);
    }

    private void runConnectionExceptionTest(HttpConnector connector, int expectedRequestCount) throws IOException {
        this.gitHub = getGitHubBuilder().withEndpoint(mockGitHub.apiServer().baseUrl())
                .withConnector(connector)
                .build();

        resetTestCapturedLog();
        baseRequestCount = this.mockGitHub.getRequestCount();
        assertThat(this.gitHub.getOrganization(GITHUB_API_TEST_ORG), is(notNullValue()));
        String capturedLog = getTestCapturedLog();
        assertThat(capturedLog, containsString("will try 2 more time"));
        assertThat(capturedLog, containsString("will try 1 more time"));
        assertThat(this.mockGitHub.getRequestCount(), equalTo(baseRequestCount + expectedRequestCount));

        resetTestCapturedLog();
        baseRequestCount = this.mockGitHub.getRequestCount();
        this.gitHub.createRequest().withUrlPath("/orgs/" + GITHUB_API_TEST_ORG).send();
        capturedLog = getTestCapturedLog();
        assertThat(capturedLog, containsString("will try 2 more time"));
        assertThat(capturedLog, containsString("will try 1 more time"));
        assertThat(this.mockGitHub.getRequestCount(), equalTo(baseRequestCount + expectedRequestCount));
    }

    private void runConnectionExceptionStatusCodeTest(HttpConnector connector, int expectedRequestCount)
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
            assertThat(capturedLog, containsString("will try 2 more time"));
            assertThat(capturedLog, containsString("will try 1 more time"));
            assertThat(this.mockGitHub.getRequestCount(), equalTo(baseRequestCount + expectedRequestCount));
        } else {
            // Success without retries
            assertThat(capturedLog, not(containsString("will try 2 more time")));
            assertThat(capturedLog, not(containsString("will try 1 more time")));
            assertThat(this.mockGitHub.getRequestCount(), equalTo(baseRequestCount + 1));
        }
    }

    /**
     * The Class ResponseCodeThrowingHttpConnector.
     *
     * @param <E> the element type
     */
    class ResponseCodeThrowingHttpConnector<E extends IOException> extends ImpatientHttpConnector {

        /**
         * Instantiates a new response code throwing http connector.
         *
         * @param thrower the thrower
         */
        ResponseCodeThrowingHttpConnector(final Thrower<E> thrower) {
            super(new HttpConnector() {
                final int[] count = { 0 };

                @Override
                public HttpURLConnection connect(URL url) throws IOException {
                    if (url.toString().contains(GITHUB_API_TEST_ORG)) {
                        count[0]++;
                    }
                    connection = Mockito.spy(new HttpURLConnectionWrapper(url) {
                        @Override
                        public int getResponseCode() throws IOException {
                            // While this is not the way this would go in the real world, it is a fine test
                            // to show that exception handling and retries are working as expected
                            if (getURL().toString().contains(GITHUB_API_TEST_ORG)) {
                                if (count[0] % 3 != 0) {
                                    thrower.throwError();
                                }
                            }
                            return super.getResponseCode();
                        }
                    });

                    return connection;
                }
            });
        }

    }

    /**
     * The Class InputStreamThrowingHttpConnector.
     *
     * @param <E> the element type
     */
    class InputStreamThrowingHttpConnector<E extends IOException> extends ImpatientHttpConnector {

        /**
         * Instantiates a new input stream throwing http connector.
         *
         * @param thrower the thrower
         */
        InputStreamThrowingHttpConnector(final Thrower<E> thrower) {
            super(new HttpConnector() {
                final int[] count = { 0 };

                @Override
                public HttpURLConnection connect(URL url) throws IOException {
                    if (url.toString().contains(GITHUB_API_TEST_ORG)) {
                        count[0]++;
                    }
                    connection = Mockito.spy(new HttpURLConnectionWrapper(url) {
                        @Override
                        public InputStream getInputStream() throws IOException {
                            // getResponseMessage throwing even though getResponseCode doesn't.
                            // While this is not the way this would go in the real world, it is a fine test
                            // to show that exception handling and retries are working as expected
                            if (getURL().toString().contains(GITHUB_API_TEST_ORG)) {
                                if (count[0] % 3 != 0) {
                                    thrower.throwError();
                                }
                            }
                            return super.getInputStream();
                        }
                    });

                    return connection;
                }
            });
        }
    }

    /**
     * The Interface Thrower.
     *
     * @param <E> the element type
     */
    @FunctionalInterface
    public interface Thrower<E extends Throwable> {
        
        /**
         * Throw error.
         *
         * @throws E the e
         */
        void throwError() throws E;
    }

    /**
     * This is not great but it get the job done. Tried to do a spy of HttpURLConnection but it wouldn't work right.
     * Trying to stub methods caused the spy to say it was already connected.
     */
    static class HttpURLConnectionWrapper extends HttpURLConnection {

        /** The http URL connection. */
        protected final HttpURLConnection httpURLConnection;

        /**
         * Instantiates a new http URL connection wrapper.
         *
         * @param url the url
         * @throws IOException Signals that an I/O exception has occurred.
         */
        HttpURLConnectionWrapper(URL url) throws IOException {
            super(new URL("http://nonexistant"));
            httpURLConnection = (HttpURLConnection) url.openConnection();
        }

        /**
         * Connect.
         *
         * @throws IOException Signals that an I/O exception has occurred.
         */
        public void connect() throws IOException {
            httpURLConnection.connect();
        }

        /**
         * Sets the connect timeout.
         *
         * @param timeout the new connect timeout
         */
        public void setConnectTimeout(int timeout) {
            httpURLConnection.setConnectTimeout(timeout);
        }

        /**
         * Gets the connect timeout.
         *
         * @return the connect timeout
         */
        public int getConnectTimeout() {
            return httpURLConnection.getConnectTimeout();
        }

        /**
         * Sets the read timeout.
         *
         * @param timeout the new read timeout
         */
        public void setReadTimeout(int timeout) {
            httpURLConnection.setReadTimeout(timeout);
        }

        /**
         * Gets the read timeout.
         *
         * @return the read timeout
         */
        public int getReadTimeout() {
            return httpURLConnection.getReadTimeout();
        }

        /**
         * Gets the url.
         *
         * @return the url
         */
        public URL getURL() {
            return httpURLConnection.getURL();
        }

        /**
         * Gets the content length.
         *
         * @return the content length
         */
        public int getContentLength() {
            return httpURLConnection.getContentLength();
        }

        /**
         * Gets the content length long.
         *
         * @return the content length long
         */
        public long getContentLengthLong() {
            return httpURLConnection.getContentLengthLong();
        }

        /**
         * Gets the content type.
         *
         * @return the content type
         */
        public String getContentType() {
            return httpURLConnection.getContentType();
        }

        /**
         * Gets the content encoding.
         *
         * @return the content encoding
         */
        public String getContentEncoding() {
            return httpURLConnection.getContentEncoding();
        }

        /**
         * Gets the expiration.
         *
         * @return the expiration
         */
        public long getExpiration() {
            return httpURLConnection.getExpiration();
        }

        /**
         * Gets the date.
         *
         * @return the date
         */
        public long getDate() {
            return httpURLConnection.getDate();
        }

        /**
         * Gets the last modified.
         *
         * @return the last modified
         */
        public long getLastModified() {
            return httpURLConnection.getLastModified();
        }

        /**
         * Gets the header field.
         *
         * @param name the name
         * @return the header field
         */
        public String getHeaderField(String name) {
            return httpURLConnection.getHeaderField(name);
        }

        /**
         * Gets the header fields.
         *
         * @return the header fields
         */
        public Map<String, List<String>> getHeaderFields() {
            return httpURLConnection.getHeaderFields();
        }

        /**
         * Gets the header field int.
         *
         * @param name the name
         * @param Default the default
         * @return the header field int
         */
        public int getHeaderFieldInt(String name, int Default) {
            return httpURLConnection.getHeaderFieldInt(name, Default);
        }

        /**
         * Gets the header field long.
         *
         * @param name the name
         * @param Default the default
         * @return the header field long
         */
        public long getHeaderFieldLong(String name, long Default) {
            return httpURLConnection.getHeaderFieldLong(name, Default);
        }

        /**
         * Gets the content.
         *
         * @return the content
         * @throws IOException Signals that an I/O exception has occurred.
         */
        public Object getContent() throws IOException {
            return httpURLConnection.getContent();
        }

        /**
         * Gets the content.
         *
         * @param classes the classes
         * @return the content
         * @throws IOException Signals that an I/O exception has occurred.
         */
        @Override
        public Object getContent(Class[] classes) throws IOException {
            return httpURLConnection.getContent(classes);
        }

        /**
         * Gets the input stream.
         *
         * @return the input stream
         * @throws IOException Signals that an I/O exception has occurred.
         */
        public InputStream getInputStream() throws IOException {
            return httpURLConnection.getInputStream();
        }

        /**
         * Gets the output stream.
         *
         * @return the output stream
         * @throws IOException Signals that an I/O exception has occurred.
         */
        public OutputStream getOutputStream() throws IOException {
            return httpURLConnection.getOutputStream();
        }

        /**
         * To string.
         *
         * @return the string
         */
        public String toString() {
            return httpURLConnection.toString();
        }

        /**
         * Sets the do input.
         *
         * @param doinput the new do input
         */
        public void setDoInput(boolean doinput) {
            httpURLConnection.setDoInput(doinput);
        }

        /**
         * Gets the do input.
         *
         * @return the do input
         */
        public boolean getDoInput() {
            return httpURLConnection.getDoInput();
        }

        /**
         * Sets the do output.
         *
         * @param dooutput the new do output
         */
        public void setDoOutput(boolean dooutput) {
            httpURLConnection.setDoOutput(dooutput);
        }

        /**
         * Gets the do output.
         *
         * @return the do output
         */
        public boolean getDoOutput() {
            return httpURLConnection.getDoOutput();
        }

        /**
         * Sets the allow user interaction.
         *
         * @param allowuserinteraction the new allow user interaction
         */
        public void setAllowUserInteraction(boolean allowuserinteraction) {
            httpURLConnection.setAllowUserInteraction(allowuserinteraction);
        }

        /**
         * Gets the allow user interaction.
         *
         * @return the allow user interaction
         */
        public boolean getAllowUserInteraction() {
            return httpURLConnection.getAllowUserInteraction();
        }

        /**
         * Sets the use caches.
         *
         * @param usecaches the new use caches
         */
        public void setUseCaches(boolean usecaches) {
            httpURLConnection.setUseCaches(usecaches);
        }

        /**
         * Gets the use caches.
         *
         * @return the use caches
         */
        public boolean getUseCaches() {
            return httpURLConnection.getUseCaches();
        }

        /**
         * Sets the if modified since.
         *
         * @param ifmodifiedsince the new if modified since
         */
        public void setIfModifiedSince(long ifmodifiedsince) {
            httpURLConnection.setIfModifiedSince(ifmodifiedsince);
        }

        /**
         * Gets the if modified since.
         *
         * @return the if modified since
         */
        public long getIfModifiedSince() {
            return httpURLConnection.getIfModifiedSince();
        }

        /**
         * Gets the default use caches.
         *
         * @return the default use caches
         */
        public boolean getDefaultUseCaches() {
            return httpURLConnection.getDefaultUseCaches();
        }

        /**
         * Sets the default use caches.
         *
         * @param defaultusecaches the new default use caches
         */
        public void setDefaultUseCaches(boolean defaultusecaches) {
            httpURLConnection.setDefaultUseCaches(defaultusecaches);
        }

        /**
         * Sets the request property.
         *
         * @param key the key
         * @param value the value
         */
        public void setRequestProperty(String key, String value) {
            httpURLConnection.setRequestProperty(key, value);
        }

        /**
         * Adds the request property.
         *
         * @param key the key
         * @param value the value
         */
        public void addRequestProperty(String key, String value) {
            httpURLConnection.addRequestProperty(key, value);
        }

        /**
         * Gets the request property.
         *
         * @param key the key
         * @return the request property
         */
        public String getRequestProperty(String key) {
            return httpURLConnection.getRequestProperty(key);
        }

        /**
         * Gets the request properties.
         *
         * @return the request properties
         */
        public Map<String, List<String>> getRequestProperties() {
            return httpURLConnection.getRequestProperties();
        }

        /**
         * Gets the header field key.
         *
         * @param n the n
         * @return the header field key
         */
        public String getHeaderFieldKey(int n) {
            return httpURLConnection.getHeaderFieldKey(n);
        }

        /**
         * Sets the fixed length streaming mode.
         *
         * @param contentLength the new fixed length streaming mode
         */
        public void setFixedLengthStreamingMode(int contentLength) {
            httpURLConnection.setFixedLengthStreamingMode(contentLength);
        }

        /**
         * Sets the fixed length streaming mode.
         *
         * @param contentLength the new fixed length streaming mode
         */
        public void setFixedLengthStreamingMode(long contentLength) {
            httpURLConnection.setFixedLengthStreamingMode(contentLength);
        }

        /**
         * Sets the chunked streaming mode.
         *
         * @param chunklen the new chunked streaming mode
         */
        public void setChunkedStreamingMode(int chunklen) {
            httpURLConnection.setChunkedStreamingMode(chunklen);
        }

        /**
         * Gets the header field.
         *
         * @param n the n
         * @return the header field
         */
        public String getHeaderField(int n) {
            return httpURLConnection.getHeaderField(n);
        }

        /**
         * Sets the instance follow redirects.
         *
         * @param followRedirects the new instance follow redirects
         */
        public void setInstanceFollowRedirects(boolean followRedirects) {
            httpURLConnection.setInstanceFollowRedirects(followRedirects);
        }

        /**
         * Gets the instance follow redirects.
         *
         * @return the instance follow redirects
         */
        public boolean getInstanceFollowRedirects() {
            return httpURLConnection.getInstanceFollowRedirects();
        }

        /**
         * Sets the request method.
         *
         * @param method the new request method
         * @throws ProtocolException the protocol exception
         */
        public void setRequestMethod(String method) throws ProtocolException {
            httpURLConnection.setRequestMethod(method);
        }

        /**
         * Gets the request method.
         *
         * @return the request method
         */
        public String getRequestMethod() {
            return httpURLConnection.getRequestMethod();
        }

        /**
         * Gets the response code.
         *
         * @return the response code
         * @throws IOException Signals that an I/O exception has occurred.
         */
        public int getResponseCode() throws IOException {
            return httpURLConnection.getResponseCode();
        }

        /**
         * Gets the response message.
         *
         * @return the response message
         * @throws IOException Signals that an I/O exception has occurred.
         */
        public String getResponseMessage() throws IOException {
            return httpURLConnection.getResponseMessage();
        }

        /**
         * Gets the header field date.
         *
         * @param name the name
         * @param Default the default
         * @return the header field date
         */
        public long getHeaderFieldDate(String name, long Default) {
            return httpURLConnection.getHeaderFieldDate(name, Default);
        }

        /**
         * Disconnect.
         */
        public void disconnect() {
            httpURLConnection.disconnect();
        }

        /**
         * Using proxy.
         *
         * @return true, if successful
         */
        public boolean usingProxy() {
            return httpURLConnection.usingProxy();
        }

        /**
         * Gets the permission.
         *
         * @return the permission
         * @throws IOException Signals that an I/O exception has occurred.
         */
        public Permission getPermission() throws IOException {
            return httpURLConnection.getPermission();
        }

        /**
         * Gets the error stream.
         *
         * @return the error stream
         */
        public InputStream getErrorStream() {
            return httpURLConnection.getErrorStream();
        }
    }

}
