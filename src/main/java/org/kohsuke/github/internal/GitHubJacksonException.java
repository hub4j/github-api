package org.kohsuke.github.internal;

import java.io.IOException;

/**
 * Wrapper exception for Jackson-specific exceptions.
 *
 * <p>
 * This exception wraps Jackson-specific exceptions (from either Jackson 2.x or 3.x) to provide a consistent exception
 * type that doesn't expose Jackson version-specific classes to callers.
 * </p>
 *
 * @author Pierre Villard
 */
public class GitHubJacksonException extends IOException {

    private static final long serialVersionUID = 1L;

    /**
     * Constructs a new GitHubJacksonException with the specified detail message.
     *
     * @param message
     *            the detail message
     */
    public GitHubJacksonException(String message) {
        super(message);
    }

    /**
     * Constructs a new GitHubJacksonException with the specified detail message and cause.
     *
     * @param message
     *            the detail message
     * @param cause
     *            the cause (a Jackson-specific exception)
     */
    public GitHubJacksonException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * Constructs a new GitHubJacksonException with the specified cause.
     *
     * @param cause
     *            the cause (a Jackson-specific exception)
     */
    public GitHubJacksonException(Throwable cause) {
        super(cause);
    }
}
