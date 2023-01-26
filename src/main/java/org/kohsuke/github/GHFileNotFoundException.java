package org.kohsuke.github;

import java.io.FileNotFoundException;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;

// TODO: Auto-generated Javadoc
/**
 * Request/response contains useful metadata. Custom exception allows store info for next diagnostics.
 *
 * @author Kanstantsin Shautsou
 */
public class GHFileNotFoundException extends FileNotFoundException {

    /** The response header fields. */
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
        return Collections.unmodifiableMap(responseHeaderFields);
    }

    /**
     * With response header fields.
     *
     * @param headerFields
     *            the header fields
     * @return the GH file not found exception
     */
    GHFileNotFoundException withResponseHeaderFields(@Nonnull Map<String, List<String>> headerFields) {
        this.responseHeaderFields = headerFields;
        return this;
    }
}
