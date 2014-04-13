package org.kohsuke.github.extras;

import com.squareup.okhttp.OkHttpClient;
import org.kohsuke.github.HttpConnector;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * {@link HttpConnector} for {@link OkHttpClient}.
 *
 * Unlike {@link #DEFAULT}, OkHttp does response caching.
 * Making a conditional request against GitHubAPI and receiving a 304
 * response does not count against the rate limit.
 * See http://developer.github.com/v3/#conditional-requests
 *
 * @author Roberto Tyley
 * @author Kohsuke Kawaguchi
 */
public class OkHttpConnector implements HttpConnector {
    private final OkHttpClient client;

    public OkHttpConnector(OkHttpClient client) {
        this.client = client;
    }

    public HttpURLConnection connect(URL url) throws IOException {
        return client.open(url);
    }
}
