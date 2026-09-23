package org.kohsuke.github.authorization;

import java.io.IOException;

/**
 * Functional interface for handling events when a new access token is received. Implementations of this interface
 * define the behavior to execute upon receiving new credentials during the GitHub device flow authorization process.
 * Usually the caller is expected to securely persist the credentials for future use.
 */
@FunctionalInterface
public interface DeviceFlowGithubAppCredentialListener {

    /**
     * Called when a new access token is received. It is the responsibility of the caller to securely store the object.
     *
     * @param appCredentials
     *            The new credentials containing the access token and related information.
     * @throws IOException
     *             If an I/O error occurs while processing the credentials.
     */
    void onAccessTokenReceived(DeviceFlowGithubAppCredentials appCredentials) throws IOException;
}
