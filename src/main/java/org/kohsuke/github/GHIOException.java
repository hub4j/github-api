package org.kohsuke.github;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;

// TODO: Auto-generated Javadoc
/**
 * Request/responce contains useful metadata. Custom exception allows store info for next diagnostics.
 *
 * @author Kanstantsin Shautsou
 */
public class GHIOException extends IOException {
    
    /** The response header fields. */
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
     * Constructs a {@code GHIOException} with the specified detail message and cause.
     *
     * @param message
     *            The detail message (which is saved for later retrieval by the {@link #getMessage()} method)
     *
     * @param cause
     *            The cause (which is saved for later retrieval by the {@link #getCause()} method). (A null value is
     *            permitted, and indicates that the cause is nonexistent or unknown.)
     */
    public GHIOException(String message, Throwable cause) {
        super(message, cause);
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
     * @param headerFields the header fields
     * @return the GHIO exception
     */
    GHIOException withResponseHeaderFields(@Nonnull Map<String, List<String>> headerFields) {
        this.responseHeaderFields = headerFields;
        return this;
    }
}
