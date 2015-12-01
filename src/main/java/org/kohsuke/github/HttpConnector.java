package org.kohsuke.github;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.TimeUnit;

/**
 * Pluggability for customizing HTTP request behaviors or using altogether different library.
 *
 * <p>
 * For example, you can implement this to st custom timeouts.
 *
 * @author Kohsuke Kawaguchi
 */
public interface HttpConnector {
    /**
     * Opens a connection to the given URL.
     */
    HttpURLConnection connect(URL url) throws IOException;

    /**
     * Default implementation that uses {@link URL#openConnection()}.
     */
    HttpConnector DEFAULT = new HttpConnector() {
        public HttpURLConnection connect(URL url) throws IOException {
            HttpURLConnection con = (HttpURLConnection) url.openConnection();
            con.setConnectTimeout(HTTP_CONNECT_TIMEOUT);
            con.setReadTimeout(HTTP_READ_TIMEOUT);
            return con;
        }
    };

    int HTTP_CONNECT_TIMEOUT = (int) TimeUnit.SECONDS.toMillis(10);
    int HTTP_READ_TIMEOUT = (int) TimeUnit.SECONDS.toMillis(10);
}
