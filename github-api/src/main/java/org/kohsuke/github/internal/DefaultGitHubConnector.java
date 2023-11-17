package org.kohsuke.github.internal;

import okhttp3.OkHttpClient;
import org.kohsuke.github.HttpConnector;
import org.kohsuke.github.connector.GitHubConnector;
import org.kohsuke.github.extras.HttpClientGitHubConnector;
import org.kohsuke.github.extras.okhttp3.OkHttpConnector;
import org.kohsuke.github.extras.okhttp3.OkHttpGitHubConnector;

/**
 * Internal class that selects what kind of {@link GitHubConnector} will be the default.
 *
 * Allows behavior to be changed for different versions of Java, such as supporting Java 11 HttpClient.
 *
 * @author Liam Newman
 */
public final class DefaultGitHubConnector {

    private DefaultGitHubConnector() {
    }

    /**
     * Creates a {@link GitHubConnector} that will be used as the default connector.
     *
     * This method currently defaults to returning an instance of {@link GitHubConnectorHttpConnectorAdapter}. This
     * preserves backward compatibility with {@link HttpConnector}.
     *
     * <p>
     * For testing purposes, the system property {@code test.github.connector} can be set to change the default.
     * Possible values: {@code default}, {@code okhttp}, {@code httpconnector}.
     *
     * <p>
     * Should only be called to set {@link GitHubConnector#DEFAULT}.
     *
     * @return a GitHubConnector
     */
    public static GitHubConnector create() {
        String defaultConnectorProperty = System.getProperty("test.github.connector", "default");
        return create(defaultConnectorProperty);
    }

    static GitHubConnector create(String defaultConnectorProperty) {

        if (defaultConnectorProperty.equalsIgnoreCase("okhttp")) {
            return new OkHttpGitHubConnector(new OkHttpClient.Builder().build());
        } else if (defaultConnectorProperty.equalsIgnoreCase("okhttpconnector")) {
            return new GitHubConnectorHttpConnectorAdapter(new OkHttpConnector(new OkHttpClient.Builder().build()));
        } else if (defaultConnectorProperty.equalsIgnoreCase("urlconnection")) {
            return new GitHubConnectorHttpConnectorAdapter(HttpConnector.DEFAULT);
        } else if (defaultConnectorProperty.equalsIgnoreCase("httpclient")) {
            return new HttpClientGitHubConnector();
        } else if (defaultConnectorProperty.equalsIgnoreCase("default")) {
            try {
                return new HttpClientGitHubConnector();
            } catch (UnsupportedOperationException | LinkageError e) {
                return new GitHubConnectorHttpConnectorAdapter(HttpConnector.DEFAULT);
            }
        } else {
            throw new IllegalStateException(
                    "Property 'test.github.connector' must reference a valid built-in connector - okhttp, okhttpconnector, urlconnection, or default.");
        }
    }
}
