package org.kohsuke.github;

/**
 * Failure when the operation cannot be carried out because the team cannot be externally managed.
 *
 * @author Kohsuke Kawaguchi
 */
public class GHTeamCannotBeExternallyManagedException extends GHEnterpriseManagedUsersException {

    /**
     * The serial version UID of the exception.
     */
    private static final long serialVersionUID = 2013101301L;

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
    public GHTeamCannotBeExternallyManagedException(final String message, final GHError error, final Throwable cause) {
        super(message, error, cause);
    }
}
