package org.kohsuke.github;

import java.io.IOException;

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

    public GHException(IOException cause)
    {
        super(cause);
    }
}
