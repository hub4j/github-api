package org.kohsuke.github;

import com.github.tomakehurst.wiremock.http.Fault;
import com.github.tomakehurst.wiremock.stubbing.Scenario;
import org.junit.Before;
import org.junit.Test;
import org.kohsuke.github.extras.ImpatientHttpConnector;
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
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;
import java.util.logging.StreamHandler;

import javax.net.ssl.SSLHandshakeException;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.hamcrest.Matchers.*;

/**
 * @author Victor Martinez
 */
public class RequesterRetryTest extends AbstractGitHubWireMockTest {

    private static Logger log = Logger.getLogger(GitHubClient.class.getName()); // matches the logger in the affected
                                                                                // class
    private static OutputStream logCapturingStream;
    private static StreamHandler customLogHandler;
    HttpURLConnection connection;
    int baseRequestCount;

    public RequesterRetryTest() {
        useDefaultGitHub = false;
    }

    protected GHRepository getRepository() throws IOException {
        return getRepository(gitHub);
    }

    private GHRepository getRepository(GitHub gitHub) throws IOException {
        return gitHub.getOrganization("github-api-test-org").getRepository("github-api");
    }

    @Before
    public void attachLogCapturer() {
        logCapturingStream = new ByteArrayOutputStream();
        customLogHandler = new StreamHandler(logCapturingStream, new SimpleFormatter());
        log.addHandler(customLogHandler);
    }

    public String getTestCapturedLog() throws IOException {
        customLogHandler.flush();
        return logCapturingStream.toString();
    }

    public void resetTestCapturedLog() throws IOException {
        log.removeHandler(customLogHandler);
        customLogHandler.close();
        attachLogCapturer();
    }

    // Issue #539
    @Test
    public void testSocketConnectionAndRetry() throws Exception {
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
        assertTrue(capturedLog.contains("will try 2 more time"));
        assertTrue(capturedLog.contains("will try 1 more time"));

        assertThat(this.mockGitHub.getRequestCount(), equalTo(baseRequestCount + 6));
    }

    // Issue #539
    @Test
    public void testSocketConnectionAndRetry_StatusCode() throws Exception {
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
                    .withUrlPath("/repos/github-api-test-org/github-api/branches/test/timeout")
                    .fetchHttpStatusCode();
            fail();
        } catch (Exception e) {
            assertThat(e, instanceOf(HttpException.class));
        }

        String capturedLog = getTestCapturedLog();
        assertTrue(capturedLog.contains("will try 2 more time"));
        assertTrue(capturedLog.contains("will try 1 more time"));

        assertThat(this.mockGitHub.getRequestCount(), equalTo(baseRequestCount + 6));
    }

    @Test
    public void testSocketConnectionAndRetry_Success() throws Exception {
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
        assertTrue(capturedLog.contains("will try 2 more time"));
        assertTrue(capturedLog.contains("will try 1 more time"));

        assertThat(this.mockGitHub.getRequestCount(), equalTo(baseRequestCount + 6));
    }

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
            assertFalse(capturedLog.contains("will try 2 more time"));
            assertFalse(capturedLog.contains("will try 1 more time"));
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
            assertFalse(capturedLog.contains("will try 2 more time"));
            assertFalse(capturedLog.contains("will try 1 more time"));
            assertThat(this.mockGitHub.getRequestCount(), equalTo(baseRequestCount));
        }
    }

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
            assertFalse(capturedLog.contains("will try 2 more time"));
            assertFalse(capturedLog.contains("will try 1 more time"));
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
            assertThat(e.getCause().getMessage(), containsString("github-api-test-org-missing"));
            String capturedLog = getTestCapturedLog();
            assertFalse(capturedLog.contains("will try 2 more time"));
            assertFalse(capturedLog.contains("will try 1 more time"));
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
        assertFalse(capturedLog.contains("will try 2 more time"));
        assertFalse(capturedLog.contains("will try 1 more time"));
        assertThat(this.mockGitHub.getRequestCount(), equalTo(baseRequestCount + 1));
    }

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
        assertTrue(capturedLog.contains("will try 2 more time"));
        assertTrue(capturedLog.contains("will try 1 more time"));
        assertThat(this.mockGitHub.getRequestCount(), equalTo(baseRequestCount + expectedRequestCount));

        resetTestCapturedLog();
        baseRequestCount = this.mockGitHub.getRequestCount();
        this.gitHub.createRequest().withUrlPath("/orgs/" + GITHUB_API_TEST_ORG).send();
        capturedLog = getTestCapturedLog();
        assertTrue(capturedLog.contains("will try 2 more time"));
        assertTrue(capturedLog.contains("will try 1 more time"));
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
            assertTrue(capturedLog.contains("will try 2 more time"));
            assertTrue(capturedLog.contains("will try 1 more time"));
            assertThat(this.mockGitHub.getRequestCount(), equalTo(baseRequestCount + expectedRequestCount));
        } else {
            // Success without retries
            assertFalse(capturedLog.contains("will try 2 more time"));
            assertFalse(capturedLog.contains("will try 1 more time"));
            assertThat(this.mockGitHub.getRequestCount(), equalTo(baseRequestCount + 1));
        }
    }

    class ResponseCodeThrowingHttpConnector<E extends IOException> extends ImpatientHttpConnector {

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

    class InputStreamThrowingHttpConnector<E extends IOException> extends ImpatientHttpConnector {

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

    @FunctionalInterface
    public interface Thrower<E extends Throwable> {
        void throwError() throws E;
    }

    /**
     * This is not great but it get the job done. Tried to do a spy of HttpURLConnection but it wouldn't work right.
     * Trying to stub methods caused the spy to say it was already connected.
     */
    static class HttpURLConnectionWrapper extends HttpURLConnection {

        protected final HttpURLConnection httpURLConnection;

        HttpURLConnectionWrapper(URL url) throws IOException {
            super(new URL("http://nonexistant"));
            httpURLConnection = (HttpURLConnection) url.openConnection();
        }

        public void connect() throws IOException {
            httpURLConnection.connect();
        }

        public void setConnectTimeout(int timeout) {
            httpURLConnection.setConnectTimeout(timeout);
        }

        public int getConnectTimeout() {
            return httpURLConnection.getConnectTimeout();
        }

        public void setReadTimeout(int timeout) {
            httpURLConnection.setReadTimeout(timeout);
        }

        public int getReadTimeout() {
            return httpURLConnection.getReadTimeout();
        }

        public URL getURL() {
            return httpURLConnection.getURL();
        }

        public int getContentLength() {
            return httpURLConnection.getContentLength();
        }

        public long getContentLengthLong() {
            return httpURLConnection.getContentLengthLong();
        }

        public String getContentType() {
            return httpURLConnection.getContentType();
        }

        public String getContentEncoding() {
            return httpURLConnection.getContentEncoding();
        }

        public long getExpiration() {
            return httpURLConnection.getExpiration();
        }

        public long getDate() {
            return httpURLConnection.getDate();
        }

        public long getLastModified() {
            return httpURLConnection.getLastModified();
        }

        public String getHeaderField(String name) {
            return httpURLConnection.getHeaderField(name);
        }

        public Map<String, List<String>> getHeaderFields() {
            return httpURLConnection.getHeaderFields();
        }

        public int getHeaderFieldInt(String name, int Default) {
            return httpURLConnection.getHeaderFieldInt(name, Default);
        }

        public long getHeaderFieldLong(String name, long Default) {
            return httpURLConnection.getHeaderFieldLong(name, Default);
        }

        public Object getContent() throws IOException {
            return httpURLConnection.getContent();
        }

        @Override
        public Object getContent(Class[] classes) throws IOException {
            return httpURLConnection.getContent(classes);
        }

        public InputStream getInputStream() throws IOException {
            return httpURLConnection.getInputStream();
        }

        public OutputStream getOutputStream() throws IOException {
            return httpURLConnection.getOutputStream();
        }

        public String toString() {
            return httpURLConnection.toString();
        }

        public void setDoInput(boolean doinput) {
            httpURLConnection.setDoInput(doinput);
        }

        public boolean getDoInput() {
            return httpURLConnection.getDoInput();
        }

        public void setDoOutput(boolean dooutput) {
            httpURLConnection.setDoOutput(dooutput);
        }

        public boolean getDoOutput() {
            return httpURLConnection.getDoOutput();
        }

        public void setAllowUserInteraction(boolean allowuserinteraction) {
            httpURLConnection.setAllowUserInteraction(allowuserinteraction);
        }

        public boolean getAllowUserInteraction() {
            return httpURLConnection.getAllowUserInteraction();
        }

        public void setUseCaches(boolean usecaches) {
            httpURLConnection.setUseCaches(usecaches);
        }

        public boolean getUseCaches() {
            return httpURLConnection.getUseCaches();
        }

        public void setIfModifiedSince(long ifmodifiedsince) {
            httpURLConnection.setIfModifiedSince(ifmodifiedsince);
        }

        public long getIfModifiedSince() {
            return httpURLConnection.getIfModifiedSince();
        }

        public boolean getDefaultUseCaches() {
            return httpURLConnection.getDefaultUseCaches();
        }

        public void setDefaultUseCaches(boolean defaultusecaches) {
            httpURLConnection.setDefaultUseCaches(defaultusecaches);
        }

        public void setRequestProperty(String key, String value) {
            httpURLConnection.setRequestProperty(key, value);
        }

        public void addRequestProperty(String key, String value) {
            httpURLConnection.addRequestProperty(key, value);
        }

        public String getRequestProperty(String key) {
            return httpURLConnection.getRequestProperty(key);
        }

        public Map<String, List<String>> getRequestProperties() {
            return httpURLConnection.getRequestProperties();
        }

        public String getHeaderFieldKey(int n) {
            return httpURLConnection.getHeaderFieldKey(n);
        }

        public void setFixedLengthStreamingMode(int contentLength) {
            httpURLConnection.setFixedLengthStreamingMode(contentLength);
        }

        public void setFixedLengthStreamingMode(long contentLength) {
            httpURLConnection.setFixedLengthStreamingMode(contentLength);
        }

        public void setChunkedStreamingMode(int chunklen) {
            httpURLConnection.setChunkedStreamingMode(chunklen);
        }

        public String getHeaderField(int n) {
            return httpURLConnection.getHeaderField(n);
        }

        public void setInstanceFollowRedirects(boolean followRedirects) {
            httpURLConnection.setInstanceFollowRedirects(followRedirects);
        }

        public boolean getInstanceFollowRedirects() {
            return httpURLConnection.getInstanceFollowRedirects();
        }

        public void setRequestMethod(String method) throws ProtocolException {
            httpURLConnection.setRequestMethod(method);
        }

        public String getRequestMethod() {
            return httpURLConnection.getRequestMethod();
        }

        public int getResponseCode() throws IOException {
            return httpURLConnection.getResponseCode();
        }

        public String getResponseMessage() throws IOException {
            return httpURLConnection.getResponseMessage();
        }

        public long getHeaderFieldDate(String name, long Default) {
            return httpURLConnection.getHeaderFieldDate(name, Default);
        }

        public void disconnect() {
            httpURLConnection.disconnect();
        }

        public boolean usingProxy() {
            return httpURLConnection.usingProxy();
        }

        public Permission getPermission() throws IOException {
            return httpURLConnection.getPermission();
        }

        public InputStream getErrorStream() {
            return httpURLConnection.getErrorStream();
        }
    }

}
