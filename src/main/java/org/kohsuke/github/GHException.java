package org.kohsuke.github;

// TODO: Auto-generated Javadoc
/**
 * The type GHException.
 *
 * @author Kohsuke Kawaguchi
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
