package org.kohsuke.github;

import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import com.github.tomakehurst.wiremock.extension.responsetemplating.ResponseTemplateTransformer;
import org.junit.Test;

import java.io.IOException;
import java.net.HttpURLConnection;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.core.IsInstanceOf.instanceOf;

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
 */
public class AbuseLimitHandlerTest extends AbstractGitHubWireMockTest {

    public AbuseLimitHandlerTest() {
        useDefaultGitHub = false;
    }

    @Override
    protected WireMockConfiguration getWireMockOptions() {
        return super.getWireMockOptions()
                .extensions(ResponseTemplateTransformer.builder().global(true).maxCacheEntries(0L).build());
    }

    @Test
    public void testHandler_Fail() throws Exception {
        // Customized response that templates the date to keep things working
        snapshotNotAllowed();

        gitHub = getGitHubBuilder().withEndpoint(mockGitHub.apiServer().baseUrl())
                .withAbuseLimitHandler(AbuseLimitHandler.FAIL)
                .build();

        gitHub.getMyself();
        assertThat(mockGitHub.getRequestCount(), equalTo(1));

        try {
            getTempRepository();
            fail();
        } catch (Exception e) {
            assertThat(e, instanceOf(IOException.class));
            assertThat(e.getCause(), instanceOf(HttpException.class));
        }

        assertThat(mockGitHub.getRequestCount(), equalTo(2));

    }

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
            assertThat(e, instanceOf(IOException.class));
            assertThat(e.getCause(), instanceOf(HttpException.class));
        }

        assertThat(mockGitHub.getRequestCount(), equalTo(2));

    }

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

    @Test
    public void testHandler_WaitStuck() throws Exception {
        // Customized response that templates the date to keep things working
        snapshotNotAllowed();

        gitHub = getGitHubBuilder().withEndpoint(mockGitHub.apiServer().baseUrl())
                .withRateLimitHandler(new RateLimitHandler() {
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
