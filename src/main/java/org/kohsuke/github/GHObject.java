package org.kohsuke.github;

import com.infradna.tool.bridge_method_injector.WithBridgeMethods;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

import java.io.IOException;
import java.net.URL;
import java.util.Date;

/**
 * Most (all?) domain objects in GitHub seems to have these 4 properties.
 */
@SuppressFBWarnings(value = {"UWF_UNWRITTEN_PUBLIC_OR_PROTECTED_FIELD", "UWF_UNWRITTEN_FIELD", 
    "NP_UNWRITTEN_FIELD"}, justification = "JSON API")
public abstract class GHObject {
    protected String url;
    protected int id;
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

    @SuppressFBWarnings(value = "UPM_UNCALLED_PRIVATE_METHOD", justification = "Bridge method of getCreatedAt")
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
    @WithBridgeMethods(value=String.class, adapterMethod="intToString")
    public int getId() {
        return id;
    }

    @SuppressFBWarnings(value = "UPM_UNCALLED_PRIVATE_METHOD", justification = "Bridge method of getId")
    private Object intToString(int id, Class type) {
        return String.valueOf(id);
    }

    @SuppressFBWarnings(value = "UPM_UNCALLED_PRIVATE_METHOD", justification = "Bridge method of getHtmlUrl")
    private Object urlToString(URL url, Class type) {
        return url==null ? null : url.toString();
    }
}
