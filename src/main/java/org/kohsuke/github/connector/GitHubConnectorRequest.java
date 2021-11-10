package org.kohsuke.github.connector;

import java.io.InputStream;
import java.net.URL;
import java.util.List;
import java.util.Map;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;

/**
 * A request passed to {@link GitHubConnector#send(GitHubConnectorRequest)} to get a {@link GitHubConnectorResponse}.\
 *
 * Implementers of {@link GitHubConnector#send(GitHubConnectorRequest)} process the information from a
 * {@link GitHubConnectorRequest} to open an HTTP connection and retrieve a response. They then return a class that
 * extends {@link GitHubConnectorResponse} for their response data.
 *
 * Clients should not implement their own {@link GitHubConnectorRequest}. The {@link GitHubConnectorRequest} provided by
 * the caller of {@link GitHubConnector#send(GitHubConnectorRequest)} should be passed to the constructor of
 * {@link GitHubConnectorResponse}.
 */
public interface GitHubConnectorRequest {

    /**
     * The request method for this request.
     *
     * For example, {@code GET} or {@code PATCH}.
     *
     * @return the request method.
     */
    @Nonnull
    String method();

    /**
     * All request headers for this request.
     *
     * @return a map of all headers.
     */
    @Nonnull
    Map<String, List<String>> allHeaders();

    /**
     * Gets the value contained in a header field.
     *
     * @param name
     *            the name of the field.
     * @return the value contained in that field, or {@code null} if not present.
     */
    @CheckForNull
    String header(String name);

    /**
     * Get the content type for the body of this request.
     *
     * @return the content type string for the body of this request.
     */
    @CheckForNull
    String contentType();

    /**
     * Gets the request body as an InputStream.
     *
     * @return the request body as an InputStream.
     */
    @CheckForNull
    InputStream body();

    /**
     * Gets the url for this request.
     *
     * @return the url for this request.
     */
    @Nonnull
    URL url();

    /**
     * Gets whether the request has information in {@link #body()} that needs to be sent.
     *
     * @return true, if the body is not null. Otherwise, false.
     */
    boolean hasBody();
}
