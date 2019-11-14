package org.kohsuke.github.extras;

import okhttp3.OkHttpClient;
import okhttp3.OkUrlFactory;
import org.kohsuke.github.HttpConnector;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * {@link HttpConnector} for {@link OkHttpClient}.
 * <p>
 * Unlike {@link #DEFAULT}, OkHttp does response caching. Making a conditional request against GitHubAPI and receiving a
 * 304 response does not count against the rate limit. See http://developer.github.com/v3/#conditional-requests
 *
 * @author Roberto Tyley
 * @author Kohsuke Kawaguchi
 * @see org.kohsuke.github.extras.okhttp3.OkHttpConnector
 */
@Deprecated
public class OkHttp3Connector implements HttpConnector {
    private final OkUrlFactory urlFactory;

    /**
     * Instantiates a new Ok http 3 connector.
     *
     * @param urlFactory
     *            the url factory
     */
    /*
     * @see org.kohsuke.github.extras.okhttp3.OkHttpConnector
     */
    @Deprecated
    public OkHttp3Connector(OkUrlFactory urlFactory) {
        this.urlFactory = urlFactory;
    }

    public HttpURLConnection connect(URL url) throws IOException {
        return urlFactory.open(url);
    }
}
