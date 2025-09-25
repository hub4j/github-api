package org.kohsuke.github.authorization;

import java.util.logging.Logger;

/**
 * A simple implementation of {@link DeviceFlowGithubAppInputManager} that logs the verification URI and user code.
 */
public class LoggerDeviceFlowGithubAppInputManager implements DeviceFlowGithubAppInputManager {
    private static final Logger LOGGER = Logger.getLogger(LoggerDeviceFlowGithubAppInputManager.class.getName());

    @Override
    public void handleVerificationCodeFlow(String verificationUri, String userCode) {
        LOGGER.info("Please go to " + verificationUri + " and enter the code: " + userCode);
    }
}
