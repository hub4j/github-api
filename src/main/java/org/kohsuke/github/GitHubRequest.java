package org.kohsuke.github;

import org.apache.commons.lang3.StringUtils;

import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;
import javax.annotation.WillClose;

import static java.util.Arrays.asList;

class GitHubRequest {

    private static final List<String> METHODS_WITHOUT_BODY = asList("GET", "DELETE");
    private final List<Entry> args;
    private final Map<String, String> headers;
    private final String urlPath;
    private final String method;
    private final InputStream body;
    private final boolean forceBody;

    private final URL url;

    private GitHubRequest(@Nonnull List<Entry> args,
            @Nonnull Map<String, String> headers,
            @Nonnull String urlPath,
            @Nonnull String method,
            @CheckForNull InputStream body,
            boolean forceBody,
            @Nonnull GitHub root,
            @CheckForNull URL url) throws MalformedURLException {
        this.args = args;
        this.headers = headers;
        this.urlPath = urlPath;
        this.method = method;
        this.body = body;
        this.forceBody = forceBody;
        if (url == null) {
            String tailApiUrl = buildTailApiUrl(urlPath);
            url = root.getApiURL(tailApiUrl);
        }
        this.url = url;
    }

    /**
     * Transform Java Enum into Github constants given its conventions
     *
     * @param en
     *            Enum to be transformed
     * @return a String containing the value of a Github constant
     */
    static String transformEnum(Enum<?> en) {
        // by convention Java constant names are upper cases, but github uses
        // lower-case constants. GitHub also uses '-', which in Java we always
        // replace with '_'
        return en.toString().toLowerCase(Locale.ENGLISH).replace('_', '-');
    }

    @Nonnull
    public String method() {
        return method;
    }

    @Nonnull
    public List<Entry> args() {
        return args;
    }

    @Nonnull
    public Map<String, String> headers() {
        return headers;
    }

    @Nonnull
    public String urlPath() {
        return urlPath;
    }

    @Nonnull
    public String contentType() {
        return headers.get("Content-type");
    }

    @CheckForNull
    public InputStream body() {
        return body;
    }

    @Nonnull
    public URL url() {
        return url;
    }

    public boolean inBody() {
        return forceBody || !METHODS_WITHOUT_BODY.contains(method);
    }

    public Builder builder() {
        return new Builder(args, headers, urlPath, method, body, forceBody);
    }
    private String buildTailApiUrl(String tailApiUrl) {
        if (!inBody() && !args.isEmpty()) {
            try {
                boolean questionMarkFound = tailApiUrl.indexOf('?') != -1;
                StringBuilder argString = new StringBuilder();
                argString.append(questionMarkFound ? '&' : '?');

                for (Iterator<Entry> it = args.listIterator(); it.hasNext();) {
                    Entry arg = it.next();
                    argString.append(URLEncoder.encode(arg.key, StandardCharsets.UTF_8.name()));
                    argString.append('=');
                    argString.append(URLEncoder.encode(arg.value.toString(), StandardCharsets.UTF_8.name()));
                    if (it.hasNext()) {
                        argString.append('&');
                    }
                }
                tailApiUrl += argString;
            } catch (UnsupportedEncodingException e) {
                throw new AssertionError(e); // UTF-8 is mandatory
            }
        }
        return tailApiUrl;
    }

    static class Builder<T extends Builder<T>> {

        private final List<Entry> args;
        private final Map<String, String> headers;
        @Nonnull
        private String urlPath;
        /**
         * Request method.
         */
        @Nonnull
        private String method;
        private InputStream body;
        private boolean forceBody;

        protected Builder() {
            this(new ArrayList<>(), new LinkedHashMap<>(), "/", "GET", null, false);
        }

        private Builder(@Nonnull List<Entry> args,
                @Nonnull Map<String, String> headers,
                @Nonnull String urlPath,
                @Nonnull String method,
                @CheckForNull InputStream body,
                boolean forceBody) {
            this.args = args;
            this.headers = headers;
            this.urlPath = urlPath;
            this.method = method;
            this.body = body;
            this.forceBody = forceBody;
        }

        GitHubRequest build(GitHub root) throws MalformedURLException {
            return build(root, null);
        }

        GitHubRequest build(GitHub root, URL url) throws MalformedURLException {
            return new GitHubRequest(args, headers, urlPath, method, body, forceBody, root, url);
        }

        /**
         * Sets the request HTTP header.
         * <p>
         * If a header of the same name is already set, this method overrides it.
         *
         * @param name
         *            the name
         * @param value
         *            the value
         */
        public void setHeader(String name, String value) {
            headers.put(name, value);
        }

        /**
         * With header requester.
         *
         * @param name
         *            the name
         * @param value
         *            the value
         * @return the requester
         */
        public T withHeader(String name, String value) {
            setHeader(name, value);
            return (T) this;
        }

        public T withPreview(String name) {
            return withHeader("Accept", name);
        }

        /**
         * With requester.
         *
         * @param key
         *            the key
         * @param value
         *            the value
         * @return the requester
         */
        public T with(String key, int value) {
            return with(key, (Object) value);
        }

        /**
         * With requester.
         *
         * @param key
         *            the key
         * @param value
         *            the value
         * @return the requester
         */
        public T with(String key, long value) {
            return with(key, (Object) value);
        }

        /**
         * With requester.
         *
         * @param key
         *            the key
         * @param value
         *            the value
         * @return the requester
         */
        public T with(String key, boolean value) {
            return with(key, (Object) value);
        }

        /**
         * With requester.
         *
         * @param key
         *            the key
         * @param e
         *            the e
         * @return the requester
         */
        public T with(String key, Enum<?> e) {
            if (e == null)
                return with(key, (Object) null);
            return with(key, transformEnum(e));
        }

        /**
         * With requester.
         *
         * @param key
         *            the key
         * @param value
         *            the value
         * @return the requester
         */
        public T with(String key, String value) {
            return with(key, (Object) value);
        }

        /**
         * With requester.
         *
         * @param key
         *            the key
         * @param value
         *            the value
         * @return the requester
         */
        public T with(String key, Collection<?> value) {
            return with(key, (Object) value);
        }

        /**
         * With requester.
         *
         * @param key
         *            the key
         * @param value
         *            the value
         * @return the requester
         */
        public T with(String key, Map<?, ?> value) {
            return with(key, (Object) value);
        }

        /**
         * With requester.
         *
         * @param body
         *            the body
         * @return the requester
         */
        public T with(@WillClose /* later */ InputStream body) {
            this.body = body;
            return (T) this;
        }

        /**
         * With nullable requester.
         *
         * @param key
         *            the key
         * @param value
         *            the value
         * @return the requester
         */
        public T withNullable(String key, Object value) {
            args.add(new Entry(key, value));
            return (T) this;
        }

        /**
         * With requester.
         *
         * @param key
         *            the key
         * @param value
         *            the value
         * @return the requester
         */
        public T with(String key, Object value) {
            if (value != null) {
                args.add(new Entry(key, value));
            }
            return (T) this;
        }

        /**
         * Unlike {@link #with(String, String)}, overrides the existing value
         *
         * @param key
         *            the key
         * @param value
         *            the value
         * @return the requester
         */
        public T set(String key, Object value) {
            for (int index = 0; index < args.size(); index++) {
                if (args.get(index).key.equals(key)) {
                    args.set(index, new Entry(key, value));
                    return (T) this;
                }
            }
            return with(key, value);
        }

        /**
         * Method requester.
         *
         * @param method
         *            the method
         * @return the requester
         */
        public T method(@Nonnull String method) {
            this.method = method;
            return (T) this;
        }

        /**
         * Content type requester.
         *
         * @param contentType
         *            the content type
         * @return the requester
         */
        public T contentType(String contentType) {
            this.headers.put("Content-type", contentType);
            return (T) this;
        }

        /**
         * NOT FOR PUBLIC USE. Do not make this method public.
         * <p>
         * Sets the path component of api URL without URI encoding.
         * <p>
         * Should only be used when passing a literal URL field from a GHObject, such as {@link GHContent#refresh()} or
         * when needing to set query parameters on requests methods that don't usually have them, such as
         * {@link GHRelease#uploadAsset(String, InputStream, String)}.
         *
         * @param urlOrPath
         *            the content type
         * @return the requester
         */
        T setRawUrlPath(String urlOrPath) {
            Objects.requireNonNull(urlOrPath);
            this.urlPath = urlOrPath;
            return (T) this;
        }

        /**
         * Path component of api URL. Appended to api url.
         * <p>
         * If urlPath starts with a slash, it will be URI encoded as a path. If it starts with anything else, it will be
         * used as is.
         *
         * @param urlPathItems
         *            the content type
         * @return the requester
         */
        public T withUrlPath(String... urlPathItems) {
            if (!this.urlPath.startsWith("/")) {
                throw new GHException("Cannot append to url path after setting a raw path");
            }

            if (urlPathItems.length == 1 && !urlPathItems[0].startsWith("/")) {
                return setRawUrlPath(urlPathItems[0]);
            }

            String tailUrlPath = String.join("/", urlPathItems);

            if (this.urlPath.endsWith("/")) {
                tailUrlPath = StringUtils.stripStart(tailUrlPath, "/");
            } else {
                tailUrlPath = StringUtils.prependIfMissing(tailUrlPath, "/");
            }

            this.urlPath += urlPathEncode(tailUrlPath);
            return (T) this;
        }

        /**
         * Encode the path to url safe string.
         *
         * @param value
         *            string to be path encoded.
         * @return The encoded string.
         */
        private static String urlPathEncode(String value) {
            try {
                return new URI(null, null, value, null, null).toString();
            } catch (URISyntaxException ex) {
                throw new AssertionError(ex);
            }
        }

        /**
         * Small number of GitHub APIs use HTTP methods somewhat inconsistently, and use a body where it's not expected.
         * Normally whether parameters go as query parameters or a body depends on the HTTP verb in use, but this method
         * forces the parameters to be sent as a body.
         */
        public T inBody() {
            forceBody = true;
            return (T) this;
        }

    }

    protected static class Entry {
        final String key;
        final Object value;

        protected Entry(String key, Object value) {
            this.key = key;
            this.value = value;
        }
    }
}
