package org.kohsuke.github;

import com.infradna.tool.bridge_method_injector.WithBridgeMethods;

import java.io.IOException;
import java.net.URL;
import java.util.Date;

/**
 * Most (all?) domain objects in GitHub seems to have these 4 properties.
 */
public abstract class GHObject {
    protected String url;
    protected long id;
    protected String created_at;
    protected String updated_at;

    /*package*/ GHObject() {
    }

    /**
     * When was this resource created?
     */
    @WithBridgeMethods(value=String.class, adapterMethod="createdAtStr")
    public Date getCreatedAt() throws IOException {
        return GitHub.parseDate(created_at);
    }

    private Object createdAtStr(Date id, Class type) {
        return created_at;
    }

    /**
     * API URL of this object.
     */
    @WithBridgeMethods(value=String.class, adapterMethod="urlToString")
    public URL getUrl() {
        return GitHub.parseURL(url);
    }

    /**
     * URL of this object for humans, which renders some HTML.
     */
    @WithBridgeMethods(value=String.class, adapterMethod="urlToString")
    public abstract URL getHtmlUrl();

    /**
     * When was this resource last updated?
     */
    public Date getUpdatedAt() throws IOException {
        return GitHub.parseDate(updated_at);
    }

    /**
     * Unique ID number of this resource.
     */
    @WithBridgeMethods(value=String.class, adapterMethod="longToString")
    public long getId() {
        return id;
    }

    private Object longToString(long id, Class type) {
        return String.valueOf(id);
    }

    private Object urlToString(URL url, Class type) {
        return url==null ? null : url.toString();
    }
}
