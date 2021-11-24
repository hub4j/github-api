package org.kohsuke.github.connector;

import org.kohsuke.github.HttpException;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.security.Permission;
import java.util.*;

/**
 * Adapter class for {@link org.kohsuke.github.connector.GitHubConnectorResponse} to be usable as a
 * {@link HttpURLConnection}.
 *
 * Behavior is equivalent to a {@link HttpURLConnection} after {@link HttpURLConnection#connect()} has been called.
 * Methods that make no sense throw {@link UnsupportedOperationException}.
 *
 * @author Liam Newman
 */
@Deprecated
class GitHubConnectorResponseHttpUrlConnectionAdapter extends HttpURLConnection {

    private final GitHubConnectorResponse connectorResponse;

    public GitHubConnectorResponseHttpUrlConnectionAdapter(GitHubConnectorResponse connectorResponse) {
        super(connectorResponse.request().url());
        this.connected = true;
        this.connectorResponse = connectorResponse;
    }

    @Override
    public String getHeaderFieldKey(int n) {
        List<String> keys = new ArrayList<>(connectorResponse.allHeaders().keySet());
        return keys.get(n);
    }

    @Override
    public String getHeaderField(int n) {
        return connectorResponse.header(getHeaderFieldKey(n));
    }

    @Override
    public void setInstanceFollowRedirects(boolean followRedirects) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean getInstanceFollowRedirects() {
        throw new UnsupportedOperationException();
    }

    @Override
    public String getRequestMethod() {
        return connectorResponse.request().method();
    }

    @Override
    public int getResponseCode() throws IOException {
        return connectorResponse.statusCode();
    }

    @Override
    public String getResponseMessage() throws IOException {
        return connectorResponse.header("Status");
    }

    @Override
    public long getHeaderFieldDate(String name, long defaultValue) {
        String dateString = getHeaderField(name);
        try {
            return Date.parse(dateString);
        } catch (Exception e) {
        }
        return defaultValue;
    }

    @Override
    public Permission getPermission() throws IOException {
        throw new UnsupportedOperationException();
    }

    @Override
    public InputStream getErrorStream() {
        try {
            if (connectorResponse.statusCode() >= HTTP_BAD_REQUEST) {
                return connectorResponse.bodyStream();
            }
        } catch (IOException e) {
        }
        return null;
    }

    @Override
    public void setConnectTimeout(int timeout) {
        throw new UnsupportedOperationException();
    }

    @Override
    public int getConnectTimeout() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setReadTimeout(int timeout) {
        throw new UnsupportedOperationException();
    }

    @Override
    public int getReadTimeout() {
        throw new UnsupportedOperationException();
    }

    @Override
    public int getContentLength() {
        long l = getContentLengthLong();
        if (l > Integer.MAX_VALUE)
            return -1;
        return (int) l;
    }

    @Override
    public long getContentLengthLong() {
        return getHeaderFieldLong("content-length", -1);
    }

    @Override
    public String getContentType() {
        return connectorResponse.header("content-type");
    }

    @Override
    public String getContentEncoding() {
        return connectorResponse.header("content-encoding");
    }

    @Override
    public long getExpiration() {
        return getHeaderFieldDate("expires", 0);
    }

    @Override
    public long getDate() {
        return getHeaderFieldDate("date", 0);
    }

    @Override
    public long getLastModified() {
        return getHeaderFieldDate("last-modified", 0);
    }

    @Override
    public String getHeaderField(String name) {
        return connectorResponse.header(name);
    }

    @Override
    public Map<String, List<String>> getHeaderFields() {
        return connectorResponse.allHeaders();
    }

    @Override
    public int getHeaderFieldInt(String name, int defaultValue) {
        String value = getHeaderField(name);
        try {
            return Integer.parseInt(value);
        } catch (Exception e) {
        }
        return defaultValue;
    }

    @Override
    public long getHeaderFieldLong(String name, long defaultValue) {
        String value = getHeaderField(name);
        try {
            return Long.parseLong(value);
        } catch (Exception e) {
        }
        return defaultValue;
    }

    @Override
    public Object getContent() throws IOException {
        throw new UnsupportedOperationException();
    }

    @Override
    public Object getContent(Class[] classes) throws IOException {
        throw new UnsupportedOperationException();
    }

    @Override
    public InputStream getInputStream() throws IOException {
        // This should only be possible in abuse or rate limit scenario
        if (connectorResponse.statusCode() >= HTTP_BAD_REQUEST) {
            throw new HttpException(connectorResponse);
        }
        return connectorResponse.bodyStream();
    }

    @Override
    public OutputStream getOutputStream() throws IOException {
        throw new UnsupportedOperationException();
    }

    @Override
    public String toString() {
        return this.getClass().getName() + ": " + connectorResponse.toString();
    }

    @Override
    public boolean getDoInput() {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean getDoOutput() {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean getUseCaches() {
        throw new UnsupportedOperationException();
    }

    @Override
    public long getIfModifiedSince() {
        return getHeaderFieldDate("If-Modified-Since", 0);
    }

    @Override
    public void setDefaultUseCaches(boolean defaultusecaches) {
        throw new UnsupportedOperationException();
    }

    @Override
    public String getRequestProperty(String key) {
        return connectorResponse.request().header(key);
    }

    @Override
    public boolean getAllowUserInteraction() {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean getDefaultUseCaches() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void disconnect() {
        // ignored
    }

    @Override
    public boolean usingProxy() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void connect() throws IOException {
        // no op
    }
}
