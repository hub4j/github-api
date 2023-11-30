package org.kohsuke.github;

import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import org.junit.Test;
import org.kohsuke.github.connector.GitHubConnectorResponse;

import java.io.IOException;

import javax.annotation.Nonnull;

import static org.hamcrest.CoreMatchers.*;
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
public class RateLimitHandlerTest extends AbstractGitHubWireMockTest {

    /**
     * Instantiates a new rate limit handler test.
     */
    public RateLimitHandlerTest() {
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

        gitHub = getGitHubBuilder().withEndpoint(mockGitHub.apiServer().baseUrl())
                .withRateLimitHandler(GitHubRateLimitHandler.FAIL)
                .build();

        gitHub.getMyself();
        assertThat(mockGitHub.getRequestCount(), equalTo(1));

        try {
            getTempRepository();
            fail();
        } catch (Exception e) {
            assertThat(e, instanceOf(HttpException.class));
            assertThat(e.getMessage(), equalTo("API rate limit reached"));
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
                .withRateLimitHandler(GitHubRateLimitHandler.FAIL)
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
            assertThat(e.getMessage(), equalTo("API rate limit reached"));
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
                .withRateLimitHandler(GitHubRateLimitHandler.WAIT)
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
                .withRateLimitHandler(new GitHubRateLimitHandler() {
                    @Override
                    public void onError(@Nonnull GitHubConnectorResponse connectorResponse) throws IOException {
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
