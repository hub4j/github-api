package org.kohsuke.github;

/**
 * The type GHException.
 */
public class GHException extends RuntimeException {
    /**
     * Instantiates a new Gh exception.
     *
     * @param message
     *            the message
     */
    public GHException(String message) {
        super(message);
    }

    /**
     * Instantiates a new Gh exception.
     *
     * @param message
     *            the message
     * @param cause
     *            the cause
     */
    public GHException(String message, Throwable cause) {
        super(message, cause);
    }
}
