package org.kohsuke.github;

import javax.annotation.CheckForNull;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.util.List;
import java.util.Map;

/**
 * Request/responce contains useful metadata.
 * Custom exception allows store info for next diagnostics.
 *
 * @author Kanstantsin Shautsou
 */
public class GHIOException extends IOException {
    protected Map<String, List<String>> responseHeaderFields;

    public GHIOException() {
    }

    public GHIOException(String message) {
        super(message);
    }

    @CheckForNull
    public Map<String, List<String>> getResponseHeaderFields() {
        return responseHeaderFields;
    }

    GHIOException withResponseHeaderFields(HttpURLConnection urlConnection) {
        this.responseHeaderFields = urlConnection.getHeaderFields();
        return this;
    }
}
