package org.kohsuke.github;

import org.kohsuke.github.authorization.AuthorizationProvider;

import java.io.IOException;

/**
 * Pluggability for customizing request behaviors without specifying any implementation details.
 *
 * @author Ned Twigg
 */
@FunctionalInterface
public interface ResponseConnector {
    /**
     * Opens a respsone to the given request.
     *
     * @param request
     *            the request
     * @param authorizationProvider
     *            the authorization provider
     * @return the response
     * @throws IOException
     *             the io exception
     */
    ResponseInfo getResponseInfo(GitHubRequest request, AuthorizationProvider authorizationProvider) throws IOException;
}
