package org.kohsuke.github;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.kohsuke.github.authorization.LoggerDeviceFlowGithubAppInputManager;

import java.io.IOException;

import static org.hamcrest.CoreMatchers.notNullValue;
import static org.kohsuke.github.authorization.DeviceFlowGithubAppCredentials.EMPTY_CREDENTIALS;

public class DeviceFlowGithubAppAuthorizationProviderTest extends AbstractGitHubWireMockTest {

    @Rule
    public TemporaryFolder tempFolder = new TemporaryFolder();

    @Ignore
    @Test
    public void performDeviceFlow() throws Exception {
        var clientId = "TODO";
        var objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());

        var appCredentialsFile = tempFolder.newFile().toPath();
        var appCredentials = EMPTY_CREDENTIALS;

        DeviceFlowGithubAppAuthorizationProvider provider = new DeviceFlowGithubAppAuthorizationProvider(clientId,
                appCredentials,
                ac -> {
                    try {
                        objectMapper.writeValue(appCredentialsFile.toFile(), ac);
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                },
                new LoggerDeviceFlowGithubAppInputManager(),
                getGitHubBuilder().withEndpoint(mockGitHub.apiServer().baseUrl()).build());
        gitHub = getGitHubBuilder().withAuthorizationProvider(provider)
                .withEndpoint(mockGitHub.apiServer().baseUrl())
                .build();

        // verify a protected resource can be accessed
        var myself = gitHub.getMyself();
        assertThat(myself, notNullValue());
    }
}
