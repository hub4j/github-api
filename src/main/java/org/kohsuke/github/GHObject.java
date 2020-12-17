package org.kohsuke.github;

import com.fasterxml.jackson.annotation.JacksonInject;
import com.infradna.tool.bridge_method_injector.WithBridgeMethods;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import org.apache.commons.lang3.builder.ReflectionToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import java.io.IOException;
import java.lang.reflect.Field;
import java.net.URL;
import java.util.Date;
import java.util.List;
import java.util.Map;

import javax.annotation.CheckForNull;

/**
 * Most (all?) domain objects in GitHub seems to have these 4 properties.
 */
@SuppressFBWarnings(value = { "UWF_UNWRITTEN_PUBLIC_OR_PROTECTED_FIELD", "UWF_UNWRITTEN_FIELD", "NP_UNWRITTEN_FIELD" },
        justification = "JSON API")
public abstract class GHObject extends GitHubInteractiveObject {
    /**
     * Capture response HTTP headers on the state object.
     */
    protected transient Map<String, List<String>> responseHeaderFields;

    private String url;

    private long id;
    private String nodeId;
    private String createdAt;
    private String updatedAt;

    GHObject() {
    }

    /**
     * Called by Jackson
     * 
     * @param responseInfo
     *            the {@link GitHubResponse.ResponseInfo} to get headers from.
     */
    @JacksonInject
    protected void setResponseHeaderFields(@CheckForNull GitHubResponse.ResponseInfo responseInfo) {
        if (responseInfo != null) {
            responseHeaderFields = responseInfo.headers();
        }
    }

    /**
     * Returns the HTTP response headers given along with the state of this object.
     *
     * <p>
     * Some of the HTTP headers have nothing to do with the object, for example "Cache-Control" and others are different
     * depending on how this object was retrieved.
     * <p>
     * This method was added as a kind of hack to allow the caller to retrieve OAuth scopes and such. Use with caution.
     * The method might be removed in the future.
     *
     * @return a map of header names to value lists
     */
    @CheckForNull
    @Deprecated
    public Map<String, List<String>> getResponseHeaderFields() {
        return responseHeaderFields;
    }

    /**
     * When was this resource created?
     *
     * @return date created
     * @throws IOException
     *             on error
     */
    @WithBridgeMethods(value = String.class, adapterMethod = "createdAtStr")
    public Date getCreatedAt() throws IOException {
        return GitHubClient.parseDate(createdAt);
    }

    @SuppressFBWarnings(value = "UPM_UNCALLED_PRIVATE_METHOD", justification = "Bridge method of getCreatedAt")
    private Object createdAtStr(Date id, Class type) {
        return createdAt;
    }

    /**
     * Gets url.
     *
     * @return API URL of this object.
     */
    @WithBridgeMethods(value = String.class, adapterMethod = "urlToString")
    public URL getUrl() {
        return GitHubClient.parseURL(url);
    }

    /**
     * Gets html url.
     *
     * @return URL of this object for humans, which renders some HTML.
     * @throws IOException
     *             on error
     */
    @WithBridgeMethods(value = String.class, adapterMethod = "urlToString")
    public abstract URL getHtmlUrl() throws IOException;

    /**
     * When was this resource last updated?
     *
     * @return updated date
     * @throws IOException
     *             on error
     */
    public Date getUpdatedAt() throws IOException {
        return GitHubClient.parseDate(updatedAt);
    }

    /**
     * Get Global node_id from Github object.
     *
     * @see <a href="https://developer.github.com/v4/guides/using-global-node-ids/">Using Global Node IDs</a>
     *
     * @return Global Node ID.
     */
    public String getNodeId() {
        return nodeId;
    }

    /**
     * Gets id.
     *
     * @return Unique ID number of this resource.
     */
    @WithBridgeMethods(value = { String.class, int.class }, adapterMethod = "longToStringOrInt")
    public long getId() {
        return id;
    }

    @SuppressFBWarnings(value = "UPM_UNCALLED_PRIVATE_METHOD", justification = "Bridge method of getId")
    private Object longToStringOrInt(long id, Class type) {
        if (type == String.class)
            return String.valueOf(id);
        if (type == int.class)
            return (int) id;
        throw new AssertionError("Unexpected type: " + type);
    }

    @SuppressFBWarnings(value = "UPM_UNCALLED_PRIVATE_METHOD", justification = "Bridge method of getHtmlUrl")
    private Object urlToString(URL url, Class type) {
        return url == null ? null : url.toString();
    }

    /**
     * String representation to assist debugging and inspection. The output format of this string is not a committed
     * part of the API and is subject to change.
     */
    @Override
    public String toString() {
        return new ReflectionToStringBuilder(this, TOSTRING_STYLE, null, null, false, false) {
            @Override
            protected boolean accept(Field field) {
                return super.accept(field) && !field.isAnnotationPresent(SkipFromToString.class);
            }
        }.toString();
    }

    private static final ToStringStyle TOSTRING_STYLE = new ToStringStyle() {
        {
            this.setUseShortClassName(true);
        }

        @Override
        public void append(StringBuffer buffer, String fieldName, Object value, Boolean fullDetail) {
            // skip unimportant properties. '_' is a heuristics as important properties tend to have short names
            if (fieldName.contains("_"))
                return;
            // avoid recursing other GHObject
            if (value instanceof GHObject)
                return;
            // likewise no point in showing root
            if (value instanceof GitHub)
                return;

            super.append(buffer, fieldName, value, fullDetail);
        }
    };
}
