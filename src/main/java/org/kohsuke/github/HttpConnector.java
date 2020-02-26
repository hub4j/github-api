package org.kohsuke.github;

import org.kohsuke.github.extras.ImpatientHttpConnector;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Pluggability for customizing HTTP request behaviors or using altogether different library.
 *
 * <p>
 * For example, you can implement this to st custom timeouts.
 */
@FunctionalInterface
public interface HttpConnector {
    /**
     * Opens a connection to the given URL.
     *
     * @param url
     *            the url
     * @return the http url connection
     * @throws IOException
     *             the io exception
     */
    HttpURLConnection connect(URL url) throws IOException;

    /**
     * Default implementation that uses {@link URL#openConnection()}.
     */
    HttpConnector DEFAULT = new ImpatientHttpConnector(url -> (HttpURLConnection) url.openConnection());

    /**
     * Stub implementation that is always off-line.
     */
    HttpConnector OFFLINE = url -> {
        throw new IOException("Offline");
    };
}
