package org.kohsuke.github;

import org.kohsuke.github.authorization.AuthorizationProvider;
import org.kohsuke.github.authorization.DeviceFlowGithubAppCredentialListener;
import org.kohsuke.github.authorization.DeviceFlowGithubAppCredentials;
import org.kohsuke.github.authorization.DeviceFlowGithubAppInputManager;

import java.io.IOException;
import java.io.InterruptedIOException;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.logging.Logger;

/**
 * Provides authorization for GitHub applications using the device flow. See <a href=
 * "https://docs.github.com/en/apps/creating-github-apps/authenticating-with-a-github-app/generating-a-user-access-token-for-a-github-app#using-the-device-flow-to-generate-a-user-access-token">...</a>
 * This class handles the device flow process, including requesting device codes, polling for access tokens, refreshing
 * tokens, and managing credential states.
 */
public class DeviceFlowGithubAppAuthorizationProvider extends GitHubInteractiveObject implements AuthorizationProvider {

    /**
     * Represents the response from GitHub's device flow access token endpoint. Contains access token, refresh token,
     * expiration information, scope, and token type. We transform it to a {@link DeviceFlowGithubAppCredentials} object
     * to expose it to the outside.
     */
    private static class DeviceFlowAccessTokenResponse {
        static DeviceFlowGithubAppCredentials toCredentials(DeviceFlowAccessTokenResponse response) {
            var credentials = new DeviceFlowGithubAppCredentials();
            credentials.setAccessToken(response.getAccessToken());
            credentials.setExpiresIn(
                    response.getExpiresIn() > 0 ? Instant.now().plusSeconds(response.getExpiresIn()) : Instant.MIN);
            credentials.setRefreshToken(response.getRefreshToken());
            credentials.setRefreshTokenExpiresIn(response.getRefreshTokenExpiresIn() > 0
                    ? Instant.now().plusSeconds(response.getRefreshTokenExpiresIn())
                    : Instant.MIN);
            credentials.setScope(response.getScope());
            credentials.setTokenType(response.getTokenType());
            return credentials;
        }
        private String accessToken;
        private int expiresIn;
        private String refreshToken;
        private int refreshTokenExpiresIn;
        // should be empty
        private String scope;

        // should be Bearer
        private String tokenType;

        public String getAccessToken() {
            return accessToken;
        }

        public int getExpiresIn() {
            return expiresIn;
        }

        public String getRefreshToken() {
            return refreshToken;
        }

        public int getRefreshTokenExpiresIn() {
            return refreshTokenExpiresIn;
        }

        public String getScope() {
            return scope;
        }

        public String getTokenType() {
            return tokenType;
        }

        public void setAccessToken(String accessToken) {
            this.accessToken = accessToken;
        }

        public void setExpiresIn(int expiresIn) {
            this.expiresIn = expiresIn;
        }

        public void setRefreshToken(String refreshToken) {
            this.refreshToken = refreshToken;
        }

        public void setRefreshTokenExpiresIn(int refreshTokenExpiresIn) {
            this.refreshTokenExpiresIn = refreshTokenExpiresIn;
        }

        public void setScope(String scope) {
            this.scope = scope;
        }

        public void setTokenType(String tokenType) {
            this.tokenType = tokenType;
        }
    }

    /**
     * Represents the response from GitHub's device flow code endpoint. Contains device code, user code, verification
     * URI, expiration, and polling interval.
     */
    private static class DeviceFlowCodeResponse {
        private String deviceCode;
        private int expiresIn;
        private int interval;
        private String userCode;
        private String verificationUri;

        public String getDeviceCode() {
            return deviceCode;
        }

        public int getExpiresIn() {
            return expiresIn;
        }

        public int getInterval() {
            return interval;
        }

        public String getUserCode() {
            return userCode;
        }

        public String getVerificationUri() {
            return verificationUri;
        }

        public void setDeviceCode(String deviceCode) {
            this.deviceCode = deviceCode;
        }

        public void setExpiresIn(int expiresIn) {
            this.expiresIn = expiresIn;
        }

        public void setInterval(int interval) {
            this.interval = interval;
        }

        public void setUserCode(String userCode) {
            this.userCode = userCode;
        }

        public void setVerificationUri(String verificationUri) {
            this.verificationUri = verificationUri;
        }
    }

    /**
     * Represents the possible states of the credentials.
     */
    private enum State {
        EXPIRED_ACCESS_TOKEN, EXPIRED_REFRESH_TOKEN, NO_ACCESS_TOKEN, NO_REFRESH_TOKEN, VALID_ACCESS_TOKEN
    }
    private static final Logger LOGGER = Logger.getLogger(DeviceFlowGithubAppAuthorizationProvider.class.getName());
    private static final int TOKEN_EXPIRATION_MARGIN_MINUTES = 5;
    private static final int USER_VERIFICATION_CODE_ATTEMPTS = 20;

    private final DeviceFlowGithubAppCredentialListener accessTokenListener;

    private DeviceFlowGithubAppCredentials appCredentials;

    private final String clientId;

    private final DeviceFlowGithubAppInputManager inputManager;

    /**
     * Constructs a new DeviceFlowGithubAppAuthorizationProvider.
     *
     * @param clientId
     *            The client ID of the GitHub app.
     * @param appCredentials
     *            The initial credentials for the app.
     * @param accessTokenListener
     *            The listener to notify when new credentials are received (either the first time or through a refresh).
     * @param inputManager
     *            The input manager for handling user input during the device flow (see
     *            {@link DeviceFlowGithubAppInputManager} for details).
     * @throws IOException
     *             If an I/O error occurs.
     */
    public DeviceFlowGithubAppAuthorizationProvider(String clientId,
            DeviceFlowGithubAppCredentials appCredentials,
            DeviceFlowGithubAppCredentialListener accessTokenListener,
            DeviceFlowGithubAppInputManager inputManager) throws IOException {
        this(clientId, appCredentials, accessTokenListener, inputManager, GitHub.connectAnonymously());
    }

    /**
     * Constructs a new DeviceFlowGithubAppAuthorizationProvider with a specified GitHub instance. This is useful for
     * testing, outside of tests you should not have to provide a GitHub instance.
     *
     * @param clientId
     *            The client ID of the GitHub app.
     * @param appCredentials
     *            The initial credentials for the app.
     * @param accessTokenListener
     *            The listener to notify when new credentials are received (either the first time or through a refresh).
     * @param inputManager
     *            The input manager for handling user input during the device flow (see
     *            {@link DeviceFlowGithubAppInputManager} for details).
     * @param github
     *            The GitHub instance to use for API requests.
     */
    DeviceFlowGithubAppAuthorizationProvider(String clientId,
            DeviceFlowGithubAppCredentials appCredentials,
            DeviceFlowGithubAppCredentialListener accessTokenListener,
            DeviceFlowGithubAppInputManager inputManager,
            GitHub github) {
        super(github);
        this.clientId = clientId;
        this.appCredentials = appCredentials;
        this.accessTokenListener = accessTokenListener;
        this.inputManager = inputManager;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getEncodedAuthorization() throws IOException {
        // 5 possible cases
        // * very 1s call, we do not have anything, no access token, no refresh token
        // * we have a valid access token
        // * we have an expired access token
        // * we do not have a refresh token
        // * we have an expired refresh token
        // Note that technically if the user did not properly persist the information we could have other states
        // like for instance having an object with no access token but a refresh token, but let's KISS it for now
        switch (getCredentialState()) {
            case VALID_ACCESS_TOKEN :
                return appCredentials.toEncodedCredentials();
            case EXPIRED_ACCESS_TOKEN :
                return refreshToken();
            case NO_ACCESS_TOKEN :
            case NO_REFRESH_TOKEN :
            case EXPIRED_REFRESH_TOKEN :
            default :
                return performDeviceFlow();
        }
    }

    private State getCredentialState() {
        if (appCredentials == null || appCredentials.getAccessToken() == null) {
            return State.NO_ACCESS_TOKEN;
        }
        if (appCredentials.getExpiresIn()
                .minus(TOKEN_EXPIRATION_MARGIN_MINUTES, ChronoUnit.MINUTES)
                .isAfter(Instant.now())) {
            return State.VALID_ACCESS_TOKEN;
        }
        if (appCredentials.getRefreshToken() == null) {
            return State.NO_REFRESH_TOKEN;
        }
        if (appCredentials.getRefreshTokenExpiresIn()
                .minus(TOKEN_EXPIRATION_MARGIN_MINUTES, ChronoUnit.MINUTES)
                .isAfter(Instant.now())) {
            return State.EXPIRED_ACCESS_TOKEN;
        }
        return State.EXPIRED_REFRESH_TOKEN;
    }

    private String performDeviceFlow() throws IOException {
        var deviceCodeResponse = requestDeviceCode();
        inputManager.handleVerificationCodeFlow(deviceCodeResponse.getVerificationUri(),
                deviceCodeResponse.getUserCode());
        var accessTokenResponse = pollForAccessToken(deviceCodeResponse);
        return refreshCredentialsAndNotifyListener(accessTokenResponse);
    }

    private DeviceFlowAccessTokenResponse pollForAccessToken(DeviceFlowCodeResponse deviceFlowCodeResponse)
            throws IOException {
        var attempts = 0;
        while (attempts < USER_VERIFICATION_CODE_ATTEMPTS) {
            var request = GitHubRequest.newBuilder()
                    .method("POST")
                    .setRawUrlPath("https://github.com/login/oauth/access_token")
                    .setHeader("Accept", "application/json")
                    .with("client_id", clientId)
                    .with("device_code", deviceFlowCodeResponse.getDeviceCode())
                    .with("grant_type", "urn:ietf:params:oauth:grant-type:device_code")
                    .inBody()
                    .build();
            var accessTokenResponse = root().getClient()
                    .sendRequest(request, r -> GitHubResponse.parseBody(r, DeviceFlowAccessTokenResponse.class))
                    .body();
            if (accessTokenResponse != null && accessTokenResponse.getAccessToken() != null) {
                LOGGER.finest("Access token obtained: " + accessTokenResponse.getAccessToken());
                return accessTokenResponse;
            }
            var intervalSeconds = deviceFlowCodeResponse.getInterval();
            if (intervalSeconds <= 0) {
                // this is the default in the GitHub doc
                intervalSeconds = 5;
            }
            attempts++;
            LOGGER.finest(String.format("No access token, sleeping for %d seconds", intervalSeconds));
            try {
                Thread.sleep(intervalSeconds * 1000L);
            } catch (InterruptedException e) {
                throw (IOException) new InterruptedIOException().initCause(e);
            }
        }
        throw new IOException("User failed to provide the verification code in the allocated time");
    }

    private String refreshCredentialsAndNotifyListener(DeviceFlowAccessTokenResponse accessTokenResponse)
            throws IOException {
        appCredentials = DeviceFlowAccessTokenResponse.toCredentials(accessTokenResponse);
        accessTokenListener.onAccessTokenReceived(appCredentials);
        return appCredentials.toEncodedCredentials();
    }

    private String refreshToken() throws IOException {
        var request = GitHubRequest.newBuilder()
                .method("POST")
                .setRawUrlPath("https://github.com/login/oauth/access_token")
                .setHeader("Accept", "application/json")
                .with("client_id", clientId)
                .with("grant_type", "refresh_token")
                .with("refresh_token", appCredentials.getRefreshToken())
                .inBody()
                .build();
        var accessTokenResponse = root().getClient()
                .sendRequest(request, r -> GitHubResponse.parseBody(r, DeviceFlowAccessTokenResponse.class))
                .body();
        return refreshCredentialsAndNotifyListener(accessTokenResponse);
    }

    private DeviceFlowCodeResponse requestDeviceCode() throws IOException {
        var request = GitHubRequest.newBuilder()
                .method("POST")
                .setRawUrlPath("https://github.com/login/device/code")
                .setHeader("Accept", "application/json")
                .with("client_id", clientId)
                .inBody()
                .build();
        return root().getClient()
                .sendRequest(request, r -> GitHubResponse.parseBody(r, DeviceFlowCodeResponse.class))
                .body();
    }
}
