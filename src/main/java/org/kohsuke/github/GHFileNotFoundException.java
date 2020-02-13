package org.kohsuke.github;

import java.io.FileNotFoundException;
import java.net.HttpURLConnection;
import java.util.List;
import java.util.Map;

import javax.annotation.CheckForNull;

/**
 * Request/responce contains useful metadata. Custom exception allows store info for next diagnostics.
 *
 * @author Kanstantsin Shautsou
 */
public class GHFileNotFoundException extends FileNotFoundException {
    protected Map<String, List<String>> responseHeaderFields;

    /**
     * Instantiates a new Gh file not found exception.
     */
    public GHFileNotFoundException() {
    }

    /**
     * Instantiates a new Gh file not found exception.
     *
     * @param message
     *            the message
     */
    public GHFileNotFoundException(String message) {
        super(message);
    }

    /**
     * Instantiates a new Gh file not found exception.
     *
     * @param message
     *            the message
     * @param cause
     *            the cause
     */
    public GHFileNotFoundException(String message, Throwable cause) {
        super(message);
        this.initCause(cause);
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

    GHFileNotFoundException withResponseHeaderFields(HttpURLConnection urlConnection) {
        this.responseHeaderFields = urlConnection.getHeaderFields();
        return this;
    }
}
