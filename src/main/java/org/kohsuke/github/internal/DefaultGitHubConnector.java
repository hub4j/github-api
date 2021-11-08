package org.kohsuke.github.internal;

import okhttp3.OkHttpClient;
import org.kohsuke.github.HttpConnector;
import org.kohsuke.github.connector.GitHubConnector;
import org.kohsuke.github.extras.okhttp3.OkHttpConnector;

public class DefaultGitHubConnector {
    public static GitHubConnector create() {
        String defaultConnectorProperty = System.getProperty("test.github.connector", "default");
        if (defaultConnectorProperty.equalsIgnoreCase("okhttp")) {
            return new OkHttpConnector(new OkHttpClient.Builder().build());
        } else if (defaultConnectorProperty.equalsIgnoreCase("httpconnector")) {
            return new GitHubConnectorHttpConnectorAdapter(HttpConnector.DEFAULT);
        } else if (defaultConnectorProperty.equalsIgnoreCase("default")) {
            return new GitHubConnectorHttpConnectorAdapter(HttpConnector.DEFAULT);
        } else {
            throw new IllegalStateException(
                    "Property 'test.github.connector' must reference a valid built-in connector - okhttp, httpconnector, or default.");
        }
    }
}
