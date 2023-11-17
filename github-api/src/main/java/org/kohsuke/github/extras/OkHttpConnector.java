package org.kohsuke.github.extras;

import com.squareup.okhttp.CacheControl;
import com.squareup.okhttp.ConnectionSpec;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.OkUrlFactory;
import org.kohsuke.github.HttpConnector;
import org.kohsuke.github.extras.okhttp3.OkHttpGitHubConnector;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;

/**
 * {@link HttpConnector} for {@link OkHttpClient}.
 * <p>
 * Unlike {@link #DEFAULT}, OkHttp does response caching. Making a conditional request against GitHubAPI and receiving a
 * 304 response does not count against the rate limit. See http://developer.github.com/v3/#conditional-requests
 *
 * @author Roberto Tyley
 * @author Kohsuke Kawaguchi
 * @deprecated This class depends on an unsupported version of OkHttp. Switch to {@link OkHttpGitHubConnector}.
 * @see OkHttpGitHubConnector
 */
@Deprecated
public class OkHttpConnector implements HttpConnector {
    private static final String HEADER_NAME = "Cache-Control";
    private final OkUrlFactory urlFactory;

    private final String maxAgeHeaderValue;

    /**
     * Instantiates a new Ok http connector.
     *
     * @param urlFactory
     *            the url factory
     */
    public OkHttpConnector(OkUrlFactory urlFactory) {
        this(urlFactory, 0);
    }

    /**
     * package private for tests to be able to change max-age for cache.
     *
     * @param urlFactory
     * @param cacheMaxAge
     */
    OkHttpConnector(OkUrlFactory urlFactory, int cacheMaxAge) {
        urlFactory.client().setSslSocketFactory(TlsSocketFactory());
        urlFactory.client().setConnectionSpecs(TlsConnectionSpecs());
        this.urlFactory = urlFactory;

        if (cacheMaxAge >= 0 && urlFactory.client() != null && urlFactory.client().getCache() != null) {
            maxAgeHeaderValue = new CacheControl.Builder().maxAge(cacheMaxAge, TimeUnit.SECONDS).build().toString();
        } else {
            maxAgeHeaderValue = null;
        }
    }

    public HttpURLConnection connect(URL url) throws IOException {
        HttpURLConnection urlConnection = urlFactory.open(url);
        if (maxAgeHeaderValue != null) {
            // By default OkHttp honors max-age, meaning it will use local cache
            // without checking the network within that time frame.
            // However, that can result in stale data being returned during that time so
            // we force network-based checking no matter how often the query is made.
            // OkHttp still automatically does ETag checking and returns cached data when
            // GitHub reports 304, but those do not count against rate limit.
            urlConnection.setRequestProperty(HEADER_NAME, maxAgeHeaderValue);
        }

        return urlConnection;
    }

    /** Returns TLSv1.2 only SSL Socket Factory. */
    private SSLSocketFactory TlsSocketFactory() {
        SSLContext sc;
        try {
            sc = SSLContext.getInstance("TLSv1.2");
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e.getMessage(), e);
        }
        try {
            sc.init(null, null, null);
            return sc.getSocketFactory();
        } catch (KeyManagementException e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    /** Returns connection spec with TLS v1.2 in it */
    private List<ConnectionSpec> TlsConnectionSpecs() {
        return Arrays.asList(ConnectionSpec.MODERN_TLS, ConnectionSpec.CLEARTEXT);
    }
}
