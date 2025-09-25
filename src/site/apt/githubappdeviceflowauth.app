Authenticating as a user

  In order to authenticate to GitHub as an user using the device flow of your GitHub App, you need to use the <<<DeviceFlowGithubAppAuthorizationProvider>>>
  authentication provider that will take care of retrieving a user access token and refresh it when needed for you.
  You need to handle two things by yourself:
  1. You need to provide a <<<DeviceFlowGithubAppCredentialListener>>> that will be called when a new user access token is retrieved (either on initial creation or on refresh).
  It is up to you to store the credential object securely the library does not take care of that.
  2. You need to provide a <<<DeviceFlowGithubAppInputManager>>> that will be called when a user interaction is needed to complete the device flow.
  The library provides a basic implementation <<<LoggerDeviceFlowGithubAppInputManager>>> that will log the instructions to the console but you could imagine a more complex
  implementation that would for example open the user browser automatically (or call some automation that will input the information automatically for instance).

  Here is a complete example to get started:

+-----+
        var clientId = "<clientId>";
        var objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());

        // for demo purpose only, this is not proper secret management!!!
        var appCredentialsFile = Path.of("/tmp/github-app-credentials.json");
        DeviceFlowGithubAppCredentials appCredentials;
        if (Files.exists(appCredentialsFile)) {
            appCredentials = objectMapper.readValue(appCredentialsFile.toFile(), DeviceFlowGithubAppCredentials.class);
        } else {
            appCredentials = EMPTY_CREDENTIALS;
        }

        var gh = new GitHubBuilder().withAuthorizationProvider(
                new DeviceFlowGithubAppAuthorizationProvider(clientId, appCredentials, ac -> {
                  // in this basic example, we serialize the credentials as json to a file
                  // this is not proper secret management and you should probably use something more secure
                    try {
                        objectMapper.writeValue(appCredentialsFile.toFile(), ac);
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                }, new LoggerDeviceFlowGithubAppInputManager())).build();
+-----+