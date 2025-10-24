package org.kohsuke.github_api.v2;

/**
 * Failure related to Enterprise Managed Users operations.
 *
 * @author Miguel Esteban Gutiérrez
 */
public class GHEnterpriseManagedUsersException extends GHIOException {

    /**
     * The serial version UID of the exception.
     */
    private static final long serialVersionUID = 1980051901L;

    /**
     * The error that caused the exception.
     */
    private final GHError error;

    /**
     * Instantiates a new exception.
     *
     * @param message
     *            the message
     * @param error
     *            the error that caused the exception
     * @param cause
     *            the cause
     */
    public GHEnterpriseManagedUsersException(final String message, final GHError error, final Throwable cause) {
        super(message, cause);
        this.error = error;
    }

    /**
     * Get the error that caused the exception.
     *
     * @return the error
     */
    public GHError getError() {
        return error;
    }

}
