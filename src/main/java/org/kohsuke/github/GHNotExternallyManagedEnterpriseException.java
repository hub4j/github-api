package org.kohsuke.github;

/**
 * Failure when the operation cannot be carried out because the resource is not part of an externally managed
 * enterprise.
 *
 * @author Miguel Esteban Gutiérrez
 */
public class GHNotExternallyManagedEnterpriseException extends GHEnterpriseManagedUsersException {

    /**
     * The serial version UID of the exception.
     */
    private static final long serialVersionUID = 1978052201L;

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
    public GHNotExternallyManagedEnterpriseException(final String message, final GHError error, final Throwable cause) {
        super(message, error, cause);
    }
}
