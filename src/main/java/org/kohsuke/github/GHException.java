package org.kohsuke.github;

/**
 * @author Kohsuke Kawaguchi
 */
public class GHException extends RuntimeException {
    public GHException(String message) {
        super(message);
    }

    public GHException(String message, Throwable cause) {
        super(message, cause);
    }
}
