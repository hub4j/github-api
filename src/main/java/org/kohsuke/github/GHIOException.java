package org.kohsuke.github;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.util.List;
import java.util.Map;

import javax.annotation.CheckForNull;

/**
 * Request/responce contains useful metadata. Custom exception allows store info for next diagnostics.
 *
 * @author Kanstantsin Shautsou
 */
public class GHIOException extends IOException {
    protected Map<String, List<String>> responseHeaderFields;

    /**
     * Instantiates a new Ghio exception.
     */
    public GHIOException() {
    }

    /**
     * Instantiates a new Ghio exception.
     *
     * @param message
     *            the message
     */
    public GHIOException(String message) {
        super(message);
    }

    /**
     * Gets response header fields.
     *
     * @return the response header fields
     */
    @CheckForNull
    public Map<String, List<String>> getResponseHeaderFields() {
        return responseHeaderFields;
    }

    GHIOException withResponseHeaderFields(HttpURLConnection urlConnection) {
        this.responseHeaderFields = urlConnection.getHeaderFields();
        return this;
    }
}
