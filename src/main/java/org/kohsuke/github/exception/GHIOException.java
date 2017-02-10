package org.kohsuke.github.exception;

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
    protected Map<String, List<String>> responceHeaderFields;

    public GHIOException() {
    }

    public GHIOException(String message) {
        super(message);
    }

    public GHIOException(String message, Throwable cause) {
        super(message, cause);
    }

    public GHIOException(Throwable cause) {
        super(cause);
    }

    @CheckForNull
    public Map<String, List<String>> getResponceHeaderFields() {
        return responceHeaderFields;
    }

    public GHIOException withResponceHeaderFields(HttpURLConnection urlConnection) {
        this.responceHeaderFields = urlConnection.getHeaderFields();
        return this;
    }
}
