package org.kohsuke.github.authorization;

/**
 * Functional interface for managing user input during the GitHub device flow authorization process. Implementations of
 * this interface define how to handle the verification URI and user code provided by GitHub for user authentication.
 * Usually you would be expected to redirect the user to the github code verification page (open the page, dump a
 * message to the console, etc) and show the verification code to the user. See
 * {@link LoggerDeviceFlowGithubAppInputManager} for an example.
 */
@FunctionalInterface
public interface DeviceFlowGithubAppInputManager {

    /**
     * Handles the user input flow for the device verification process.
     *
     * @param verificationUri
     *            The URI where the user needs to navigate to verify the device.
     * @param userCode
     *            The user code that the user needs to enter at the verification URI.
     */
    void handleVerificationCodeFlow(String verificationUri, String userCode);
}
