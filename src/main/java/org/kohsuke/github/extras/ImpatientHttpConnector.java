package org.kohsuke.github.extras;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import org.kohsuke.github.HttpConnector;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.TimeUnit;

/**
 * {@link HttpConnector} wrapper that sets timeout
 *
 * @author Kohsuke Kawaguchi
 */
public class ImpatientHttpConnector implements HttpConnector {
    private final HttpConnector base;
    private final int readTimeout, connectTimeout;

    /**
     * Instantiates a new Impatient http connector.
     *
     * @param base
     *            the base
     * @param connectTimeout
     *            HTTP connection timeout in milliseconds
     * @param readTimeout
     *            HTTP read timeout in milliseconds
     */
    public ImpatientHttpConnector(HttpConnector base, int connectTimeout, int readTimeout) {
        this.base = base;
        this.connectTimeout = connectTimeout;
        this.readTimeout = readTimeout;
    }

    /**
     * Instantiates a new Impatient http connector.
     *
     * @param base
     *            the base
     * @param timeout
     *            the timeout
     */
    public ImpatientHttpConnector(HttpConnector base, int timeout) {
        this(base, timeout, timeout);
    }

    /**
     * Instantiates a new Impatient http connector.
     *
     * @param base
     *            the base
     */
    public ImpatientHttpConnector(HttpConnector base) {
        this(base, CONNECT_TIMEOUT, READ_TIMEOUT);
    }

    public HttpURLConnection connect(URL url) throws IOException {
        HttpURLConnection con = base.connect(url);
        con.setConnectTimeout(connectTimeout);
        con.setReadTimeout(readTimeout);
        return con;
    }

    /**
     * Default connection timeout in milliseconds
     */
    @SuppressFBWarnings("MS_SHOULD_BE_FINAL")
    public static int CONNECT_TIMEOUT = (int) TimeUnit.SECONDS.toMillis(10);

    /**
     * Default read timeout in milliseconds
     */
    @SuppressFBWarnings("MS_SHOULD_BE_FINAL")
    public static int READ_TIMEOUT = (int) TimeUnit.SECONDS.toMillis(10);
}
