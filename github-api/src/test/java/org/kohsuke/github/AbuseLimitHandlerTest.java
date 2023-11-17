package org.kohsuke.github;

import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import org.apache.commons.io.IOUtils;
import org.hamcrest.Matchers;
import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.ProtocolException;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.Map;

import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.core.IsInstanceOf.instanceOf;

// TODO: Auto-generated Javadoc
/**
 * Test showing the behavior of OkHttpConnector with and without cache.
 * <p>
 * Key take aways:
 *
 * <ul>
 * <li>These tests are artificial and intended to highlight the differences in behavior between scenarios. However, the
 * differences they indicate are stark.</li>
 * <li>Caching reduces rate limit consumption by at least a factor of two in even the simplest case.</li>
 * <li>The OkHttp cache is pretty smart and will often connect read and write requests made on the same client and
 * invalidate caches.</li>
 * <li>Changes made outside the current client cause the OkHttp cache to return stale data. This is expected and correct
 * behavior.</li>
 * <li>"max-age=0" addresses the problem of external changes by revalidating caches for each request. This produces the
 * same number of requests as OkHttp without caching, but those requests only count towards the GitHub rate limit if
 * data has changes.</li>
 * </ul>
 *
 * @author Liam Newman
 */
public class AbuseLimitHandlerTest extends AbstractGitHubWireMockTest {

    /**
     * Instantiates a new abuse limit handler test.
     */
    public AbuseLimitHandlerTest() {
        useDefaultGitHub = false;
    }

    /**
     * Gets the wire mock options.
     *
     * @return the wire mock options
     */
    @Override
    protected WireMockConfiguration getWireMockOptions() {
        return super.getWireMockOptions().extensions(templating.newResponseTransformer());
    }

    /**
     * Test handler fail.
     *
     * @throws Exception
     *             the exception
     */
    @Test
    public void testHandler_Fail() throws Exception {
        // Customized response that templates the date to keep things working
        snapshotNotAllowed();
        final HttpURLConnection[] savedConnection = new HttpURLConnection[1];

        gitHub = getGitHubBuilder().withEndpoint(mockGitHub.apiServer().baseUrl())
                .withAbuseLimitHandler(new AbuseLimitHandler() {
                    @Override
                    public void onError(IOException e, HttpURLConnection uc) throws IOException {
                        savedConnection[0] = uc;
                        // Verify
                        assertThat(uc.getDate(), Matchers.greaterThanOrEqualTo(new Date().getTime() - 10000));
                        assertThat(uc.getExpiration(), equalTo(0L));
                        assertThat(uc.getIfModifiedSince(), equalTo(0L));
                        assertThat(uc.getLastModified(), equalTo(1581014017000L));
                        assertThat(uc.getRequestMethod(), equalTo("GET"));
                        assertThat(uc.getResponseCode(), equalTo(403));
                        assertThat(uc.getResponseMessage(), containsString("Forbidden"));
                        assertThat(uc.getURL().toString(), endsWith("/repos/hub4j-test-org/temp-testHandler_Fail"));
                        assertThat(uc.getHeaderFieldInt("X-RateLimit-Limit", 10), equalTo(5000));
                        assertThat(uc.getHeaderFieldInt("X-RateLimit-Remaining", 10), equalTo(4000));
                        assertThat(uc.getHeaderFieldInt("X-Foo", 20), equalTo(20));
                        assertThat(uc.getHeaderFieldLong("X-RateLimit-Limit", 15L), equalTo(5000L));
                        assertThat(uc.getHeaderFieldLong("X-RateLimit-Remaining", 15L), equalTo(4000L));
                        assertThat(uc.getHeaderFieldLong("X-Foo", 20L), equalTo(20L));

                        assertThat(uc.getContentEncoding(), nullValue());
                        assertThat(uc.getContentType(), equalTo("application/json; charset=utf-8"));
                        assertThat(uc.getContentLength(), equalTo(-1));

                        // getting an input stream in an error case should throw
                        IOException ioEx = Assert.assertThrows(IOException.class, () -> uc.getInputStream());

                        try (InputStream errorStream = uc.getErrorStream()) {
                            assertThat(errorStream, notNullValue());
                            String errorString = IOUtils.toString(errorStream, StandardCharsets.UTF_8);
                            assertThat(errorString, containsString("Must have push access to repository"));
                        }

                        // calling again should still error
                        ioEx = Assert.assertThrows(IOException.class, () -> uc.getInputStream());

                        // calling again on a GitHubConnectorResponse should yield the same value
                        if (uc.toString().contains("GitHubConnectorResponseHttpUrlConnectionAdapter")) {
                            try (InputStream errorStream = uc.getErrorStream()) {
                                assertThat(errorStream, notNullValue());
                                String errorString = IOUtils.toString(errorStream, StandardCharsets.UTF_8);
                                assertThat(errorString, containsString("Must have push access to repository"));
                            }
                        } else {
                            try (InputStream errorStream = uc.getErrorStream()) {
                                assertThat(errorStream, notNullValue());
                                String errorString = IOUtils.toString(errorStream, StandardCharsets.UTF_8);
                                fail();
                            } catch (IOException ex) {
                                assertThat(ex, notNullValue());
                                assertThat(ex.getMessage(), containsString("stream is closed"));
                            }
                        }

                        assertThat(uc.getHeaderFields(), instanceOf(Map.class));
                        assertThat(uc.getHeaderFields().size(), Matchers.greaterThan(25));
                        assertThat(uc.getHeaderField("Status"), equalTo("403 Forbidden"));

                        String key = uc.getHeaderFieldKey(1);
                        assertThat(key, notNullValue());
                        assertThat(uc.getHeaderField(1), notNullValue());
                        assertThat(uc.getHeaderField(1), equalTo(uc.getHeaderField(key)));

                        assertThat(uc.getRequestProperty("Accept"), equalTo("application/vnd.github.v3+json"));

                        Assert.assertThrows(IllegalStateException.class, () -> uc.getRequestProperties());

                        // Actions that are not allowed because connection already opened.
                        Assert.assertThrows(IllegalStateException.class, () -> uc.addRequestProperty("bogus", "item"));

                        Assert.assertThrows(IllegalStateException.class, () -> uc.setAllowUserInteraction(true));
                        Assert.assertThrows(IllegalStateException.class, () -> uc.setChunkedStreamingMode(1));
                        Assert.assertThrows(IllegalStateException.class, () -> uc.setDoInput(true));
                        Assert.assertThrows(IllegalStateException.class, () -> uc.setDoOutput(true));
                        Assert.assertThrows(IllegalStateException.class, () -> uc.setFixedLengthStreamingMode(1));
                        Assert.assertThrows(IllegalStateException.class, () -> uc.setFixedLengthStreamingMode(1L));
                        Assert.assertThrows(IllegalStateException.class, () -> uc.setIfModifiedSince(1L));
                        Assert.assertThrows(IllegalStateException.class, () -> uc.setRequestProperty("bogus", "thing"));
                        Assert.assertThrows(IllegalStateException.class, () -> uc.setUseCaches(true));

                        if (uc.toString().contains("GitHubConnectorResponseHttpUrlConnectionAdapter")) {

                            Assert.assertThrows(UnsupportedOperationException.class,
                                    () -> uc.getAllowUserInteraction());
                            Assert.assertThrows(UnsupportedOperationException.class, () -> uc.getConnectTimeout());
                            Assert.assertThrows(UnsupportedOperationException.class, () -> uc.getContent());
                            Assert.assertThrows(UnsupportedOperationException.class, () -> uc.getContent(null));
                            Assert.assertThrows(UnsupportedOperationException.class, () -> uc.getDefaultUseCaches());
                            Assert.assertThrows(UnsupportedOperationException.class, () -> uc.getDoInput());
                            Assert.assertThrows(UnsupportedOperationException.class, () -> uc.getDoOutput());
                            Assert.assertThrows(UnsupportedOperationException.class,
                                    () -> uc.getInstanceFollowRedirects());
                            Assert.assertThrows(UnsupportedOperationException.class, () -> uc.getOutputStream());
                            Assert.assertThrows(UnsupportedOperationException.class, () -> uc.getPermission());
                            Assert.assertThrows(UnsupportedOperationException.class, () -> uc.getReadTimeout());
                            Assert.assertThrows(UnsupportedOperationException.class, () -> uc.getUseCaches());
                            Assert.assertThrows(UnsupportedOperationException.class, () -> uc.usingProxy());

                            Assert.assertThrows(UnsupportedOperationException.class, () -> uc.setConnectTimeout(10));
                            Assert.assertThrows(UnsupportedOperationException.class,
                                    () -> uc.setDefaultUseCaches(true));

                            Assert.assertThrows(UnsupportedOperationException.class,
                                    () -> uc.setInstanceFollowRedirects(true));
                            Assert.assertThrows(UnsupportedOperationException.class, () -> uc.setReadTimeout(10));
                            Assert.assertThrows(ProtocolException.class, () -> uc.setRequestMethod("GET"));
                        } else {
                            uc.getDefaultUseCaches();
                            assertThat(uc.getDoInput(), is(true));

                            // Depending on the underlying implementation, this may throw or not
                            // Assert.assertThrows(IllegalStateException.class, () -> uc.setRequestMethod("GET"));
                        }

                        // ignored
                        uc.connect();

                        // disconnect does nothing, never throws
                        uc.disconnect();
                        uc.disconnect();

                        // ignored
                        uc.connect();

                        AbuseLimitHandler.FAIL.onError(e, uc);
                    }
                })
                .build();

        gitHub.getMyself();
        assertThat(mockGitHub.getRequestCount(), equalTo(1));

        try {
            getTempRepository();
            fail();
        } catch (Exception e) {
            assertThat(e, instanceOf(HttpException.class));
            assertThat(e.getMessage(), equalTo("Abuse limit reached"));
        }

        if (savedConnection[0].toString().contains("GitHubConnectorResponseHttpUrlConnectionAdapter")) {
            // error stream is non-null above. null here because response has been closed.
            assertThat(savedConnection[0].getErrorStream(), nullValue());
        }

        assertThat(mockGitHub.getRequestCount(), equalTo(2));

    }

    /**
     * Test handler http status fail.
     *
     * @throws Exception
     *             the exception
     */
    @Test
    public void testHandler_HttpStatus_Fail() throws Exception {
        // Customized response that templates the date to keep things working
        snapshotNotAllowed();

        gitHub = getGitHubBuilder().withEndpoint(mockGitHub.apiServer().baseUrl())
                .withAbuseLimitHandler(AbuseLimitHandler.FAIL)
                .build();

        gitHub.getMyself();
        assertThat(mockGitHub.getRequestCount(), equalTo(1));

        try {
            gitHub.createRequest()
                    .withUrlPath("/repos/" + GITHUB_API_TEST_ORG + "/temp-testHandler_Fail")
                    .fetchHttpStatusCode();

            fail();
        } catch (Exception e) {
            assertThat(e, instanceOf(HttpException.class));
            assertThat(e.getMessage(), equalTo("Abuse limit reached"));
        }

        assertThat(mockGitHub.getRequestCount(), equalTo(2));

    }

    /**
     * Test handler wait.
     *
     * @throws Exception
     *             the exception
     */
    @Test
    public void testHandler_Wait() throws Exception {
        // Customized response that templates the date to keep things working
        snapshotNotAllowed();

        gitHub = getGitHubBuilder().withEndpoint(mockGitHub.apiServer().baseUrl())
                .withAbuseLimitHandler(AbuseLimitHandler.WAIT)
                .build();

        gitHub.getMyself();
        assertThat(mockGitHub.getRequestCount(), equalTo(1));

        getTempRepository();
        assertThat(mockGitHub.getRequestCount(), equalTo(3));
    }

    /**
     * Test handler wait stuck.
     *
     * @throws Exception
     *             the exception
     */
    @Test
    public void testHandler_WaitStuck() throws Exception {
        // Customized response that templates the date to keep things working
        snapshotNotAllowed();

        gitHub = getGitHubBuilder().withEndpoint(mockGitHub.apiServer().baseUrl())
                .withAbuseLimitHandler(new AbuseLimitHandler() {
                    @Override
                    public void onError(IOException e, HttpURLConnection uc) throws IOException {
                    }
                })
                .build();

        gitHub.getMyself();
        assertThat(mockGitHub.getRequestCount(), equalTo(1));

        try {
            getTempRepository();
            fail();
        } catch (Exception e) {
            assertThat(e, instanceOf(GHIOException.class));
        }

        assertThat(mockGitHub.getRequestCount(), equalTo(4));
    }

}
