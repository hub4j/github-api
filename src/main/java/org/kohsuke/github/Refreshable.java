package org.kohsuke.github;

import org.kohsuke.github.extras.ImpatientHttpConnector;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * The interface Refreshable.
 *
 * @author Liam Newman
 */
public interface Refreshable {
    /**
     * Opens a connection to the given URL.
     *
     * @throws IOException
     *             the io exception
     */
    void refresh() throws IOException;

    /**
     * Calls refresh if the provided value is null
     *
     * @param value
     *            the value
     * @throws IOException
     *             the io exception
     */
    default void refresh(Object value) throws IOException {
        if (value == null) {
            this.refresh();
        }
    }
}
