package org.kohsuke.github;

import org.kohsuke.github.connector.GitHubConnectorResponse;

import java.io.IOException;

/**
 * Special {@link IOException} case for http exceptions, when {@link HttpException} is thrown due to GitHub service
 * being down.
 *
 * Inherits from {@link HttpException} to maintain compatibility with existing clients.
 *
 * @author <a href="mailto:rbudinsk@redhat.com">Rastislav Budinsky</a>
 */
public class ServiceDownException extends HttpException {
    public ServiceDownException(GitHubConnectorResponse connectorResponse) {
        super(connectorResponse);
    }
}
