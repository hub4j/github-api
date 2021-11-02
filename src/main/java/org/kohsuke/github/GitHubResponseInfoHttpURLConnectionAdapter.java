package org.kohsuke.github;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.ProtocolException;
import java.net.URL;
import java.security.Permission;
import java.util.*;

class GitHubResponseInfoHttpURLConnectionAdapter extends HttpURLConnection {

    private static final Comparator<String> nullableCaseInsensitiveComparator = Comparator
            .nullsFirst(String.CASE_INSENSITIVE_ORDER);

    private final GitHubResponse.ResponseInfo responseInfo;

    GitHubResponseInfoHttpURLConnectionAdapter(GitHubResponse.ResponseInfo responseInfo) {
        super(responseInfo.url());
        this.responseInfo = responseInfo;
    }

    @Override
    public String getHeaderFieldKey(int n) {
        return responseInfo.headerFieldKey(n);
    }

    @Override
    public void setFixedLengthStreamingMode(int contentLength) {
        throw new UnsupportedOperationException(
                "Setting streaming mode is not supported by " + this.getClass());
    }

    @Override
    public void setFixedLengthStreamingMode(long contentLength) {
        throw new UnsupportedOperationException(
                "Setting streaming mode is not supported by " + this.getClass());
    }

    @Override
    public void setChunkedStreamingMode(int chunklen) {
        throw new UnsupportedOperationException(
                "Setting streaming mode is not supported by " + this.getClass());
    }

    @Override
    public String getHeaderField(int n) {
        return responseInfo.headerField(responseInfo.headerFieldKey(n));
    }

    @Override
    public void setInstanceFollowRedirects(boolean followRedirects) {
        super.setInstanceFollowRedirects(followRedirects);
    }

    @Override
    public boolean getInstanceFollowRedirects() {
        return super.getInstanceFollowRedirects();
    }

    @Override
    public void setRequestMethod(String method) throws ProtocolException {
        super.setRequestMethod(method);
    }

    @Override
    public String getRequestMethod() {
        return responseInfo.request().method();
    }

    @Override
    public int getResponseCode() throws IOException {
        return responseInfo.statusCode();
    }

    @Override
    public String getResponseMessage() throws IOException {
        return responseInfo.headerField("Status");
    }

    @Override
    public long getHeaderFieldDate(String name, long defaultValue) {
        String dateString = getHeaderField(name);
        try {
            return GitHubClient.parseDate(dateString).getTime();
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
        return new ByteArrayInputStream(responseInfo.errorMessage().getBytes());
    }

    @Override
    public void setConnectTimeout(int timeout) {
        throw new UnsupportedOperationException();
    }

    @Override
    public int getConnectTimeout() {
        return super.getConnectTimeout();
    }

    @Override
    public void setReadTimeout(int timeout) {
        throw new UnsupportedOperationException();
    }

    @Override
    public int getReadTimeout() {
        return super.getReadTimeout();
    }

    @Override
    public URL getURL() {
        return responseInfo.url();
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
        return responseInfo.headerField("content-type");
    }

    @Override
    public String getContentEncoding() {
        return responseInfo.headerField("content-encoding");
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
        return responseInfo.headerField(name);
    }

    @Override
    public Map<String, List<String>> getHeaderFields() {
        return responseInfo.headers();
    }

    @Override
    public int getHeaderFieldInt(String name, int defaultValue) {
        String value = getHeaderField(name);
        try {
            return Integer.parseInt(value);
        } catch (Exception e) { }
        return defaultValue;
    }

    @Override
    public long getHeaderFieldLong(String name, long defaultValue) {
        String value = getHeaderField(name);
        try {
            return Long.parseLong(value);
        } catch (Exception e) { }
        return defaultValue;
    }

    @Override
    public Object getContent() throws IOException {
        return super.getContent();
    }

//    @Override
//    public Object getContent(Class<?>[] classes) throws IOException {
//        return super.getContent(classes);
//    }
//
//    @Override
//    public InputStream getInputStream() throws IOException {
//        return responseInfo.bodyStream();
//    }

    @Override
    public OutputStream getOutputStream() throws IOException {
        throw new UnsupportedOperationException();
    }

    @Override
    public String toString() {
        return responseInfo.toString();
    }

    @Override
    public void setDoInput(boolean doinput) {
        throw new IllegalStateException();
    }

    @Override
    public boolean getDoInput() {
        return super.getDoInput();
    }

    @Override
    public void setDoOutput(boolean dooutput) {
        throw new IllegalStateException();
    }

    @Override
    public boolean getDoOutput() {
        return super.getDoOutput();
    }

    @Override
    public void setAllowUserInteraction(boolean allowuserinteraction) {
        super.setAllowUserInteraction(allowuserinteraction);
    }

    @Override
    public void setUseCaches(boolean usecaches) {
        throw new IllegalStateException();
    }

    @Override
    public boolean getUseCaches() {
        return super.getUseCaches();
    }

    @Override
    public void setIfModifiedSince(long ifmodifiedsince) {
        throw new IllegalStateException();
    }

    @Override
    public long getIfModifiedSince() {
        String modifiedSince = responseInfo.request().headers().get("If-Modified-Since");
        if(modifiedSince != null) {
            return GitHubClient.parseDate(modifiedSince).getTime();
        } else {
            return 0;
        }
    }

    @Override
    public void setDefaultUseCaches(boolean defaultusecaches) {
        throw new IllegalStateException();
    }

    @Override
    public void setRequestProperty(String key, String value) {
        throw new IllegalStateException();
    }

    @Override
    public void addRequestProperty(String key, String value) {
        throw new IllegalStateException();
    }

    @Override
    public String getRequestProperty(String key) {
        return responseInfo.request().headers().get(key);
    }

    @Override
    public Map<String, List<String>> getRequestProperties() {
        TreeMap<String, List<String>> caseInsensitiveMap = new TreeMap<>(nullableCaseInsensitiveComparator);
        responseInfo.request().headers().forEach(
                (key, value) -> caseInsensitiveMap.put(key, Collections.singletonList(value)) );

        return Collections.unmodifiableMap(caseInsensitiveMap);
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
        return false;
    }

    @Override
    public void connect() throws IOException {
        throw new UnsupportedOperationException();
    }
}
