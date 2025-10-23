package org.kohsuke.github_api.v2;

import java.io.IOException;

import org.kohsuke.github_api.v2.connector.GitHubConnectorResponse;

/**
 * Special {@link IOException} case for http exceptions, when {@link HttpException} is thrown due to GitHub service
 * being down.
 *
 * Inherits from {@link HttpException} to maintain compatibility with existing clients.
 *
 * @author <a href="mailto:rbudinsk@redhat.com">Rastislav Budinsky</a>
 */
public class ServiceDownException extends HttpException {

    /**
     * Instantiates a new service down exception.
     *
     * @param connectorResponse
     *            the connector response to base this on
     */
    public ServiceDownException(GitHubConnectorResponse connectorResponse) {
        super(connectorResponse);
    }
}
