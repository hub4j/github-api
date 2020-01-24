package org.kohsuke.github;

import org.junit.Test;
import org.kohsuke.github.extras.ImpatientHttpConnector;
import org.mockito.Mockito;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.ProtocolException;
import java.net.URL;
import java.security.Permission;
import java.util.List;
import java.util.Map;

import static org.hamcrest.Matchers.*;

/**
 * Unit test for {@link GitHub}.
 */
public class RequesterTest extends AbstractGitHubWireMockTest {

    public RequesterTest() {
        useDefaultGitHub = false;
    }

    HttpURLConnection connection;

    @Test
    public void test404RetryDoesNotThrow() throws Exception {
        HttpConnector connector = new ImpatientHttpConnector(url -> {
            connection = Mockito.spy(new HttpURLConnectionWrapper(url) {
                int count = 0;

                @Override
                public int getResponseCode() throws IOException {
                    // For this connector, the connection throws the first time it is called
                    // the 404 retry method will should swallow this first call
                    // While this is not the way this would go in the real world, it is a fine test
                    // to show that the 404 retry method swallows exceptions as expected.
                    count++;
                    if (count == 1) {
                        throw new IOException();
                    } else {
                        return super.getResponseCode();
                    }
                }
            });

            return connection;
        });

        // now wire in the connector
        this.gitHub = getGitHubBuilder().withEndpoint(mockGitHub.apiServer().baseUrl())
                .withConnector(connector)
                .build();

        assertThat(this.gitHub.getRateLimit(), is(notNullValue()));
        Mockito.verify(connection, Mockito.times(2)).getResponseCode();

        this.gitHub.createRequest().withUrlPath("/rate_limit").send();
        Mockito.verify(connection, Mockito.times(2)).getResponseCode();

        assertThat(this.gitHub.createRequest().withUrlPath("/rate_limit").fetchHttpStatusCode(), equalTo(200));
        Mockito.verify(connection, Mockito.times(2)).getResponseCode();
    }

    /**
     * This is not great but it get the job done. Tried to do a spy of HttpURLConnection but it wouldn't work right.
     * Trying to stub methods caused the spy to try to say it was already connected.
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
