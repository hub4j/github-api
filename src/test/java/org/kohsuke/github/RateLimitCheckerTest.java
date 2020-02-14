package org.kohsuke.github;

import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import com.github.tomakehurst.wiremock.extension.responsetemplating.ResponseTemplateTransformer;
import com.github.tomakehurst.wiremock.extension.responsetemplating.helpers.HandlebarsCurrentDateHelper;
import org.junit.Test;
import wiremock.com.github.jknack.handlebars.Helper;
import wiremock.com.github.jknack.handlebars.Options;

import java.io.IOException;
import java.util.Date;

import static org.hamcrest.CoreMatchers.*;

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
public class RateLimitCheckerTest extends AbstractGitHubWireMockTest {

    GHRateLimit rateLimit = null;
    GHRateLimit previousLimit = null;
    Date testStartDate = new Date();

    public RateLimitCheckerTest() {
        useDefaultGitHub = false;
    }

    @Override
    protected WireMockConfiguration getWireMockOptions() {

        return super.getWireMockOptions().extensions(ResponseTemplateTransformer.builder()
                .global(true)
                .maxCacheEntries(0L)
                .helper("testStartDate", new Helper<Object>() {
                    private HandlebarsCurrentDateHelper helper = new HandlebarsCurrentDateHelper();
                    @Override
                    public Object apply(final Object context, final Options options) throws IOException {
                        return this.helper.apply(RateLimitCheckerTest.this.testStartDate, options);
                    }
                })
                .build());
    }

    @Test
    public void testGitHubRateLimit() throws Exception {
        // Customized response that templates the date to keep things working
        // snapshotNotAllowed();

        assertThat(mockGitHub.getRequestCount(), equalTo(0));

        // // 4897 is just the what the limit was when the snapshot was taken
        // previousLimit = GHRateLimit
        // .fromHeaderRecord(new GHRateLimit.Record(5000, 4897, System.currentTimeMillis() / 1000L));

        // Give this a moment
        Thread.sleep(1000);

        testStartDate = new Date();
        // -------------------------------------------------------------
        // /user gets response with rate limit information
        gitHub = getGitHubBuilder().withRateLimitChecker(new RateLimitChecker.LiteralValue(4500))
                .withEndpoint(mockGitHub.apiServer().baseUrl())
                .build();

        assertThat(gitHub.lastRateLimit(), nullValue());

        // Checks the rate limit before getting myself
        gitHub.getMyself();
        updateTestRateLimit();
        assertThat(mockGitHub.getRequestCount(), equalTo(2));
        assertThat(rateLimit.getCore().getRemaining(), equalTo(4501));

        // Should succeed without querying rate limit
        // Also due to earlier reset date, new value is ignored.
        GHOrganization org = gitHub.getOrganization("github-api-test-org");
        updateTestRateLimit();
        assertThat(mockGitHub.getRequestCount(), equalTo(3));
        assertThat(rateLimit.getCore().getRemaining(), equalTo(4501));
        assertThat(rateLimit, sameInstance(previousLimit));

        // uses the existing header rate limit
        // This request's header sets the limit at quota
        org.getRepository("github-api");
        updateTestRateLimit();
        assertThat(mockGitHub.getRequestCount(), equalTo(4));
        assertThat(rateLimit.getCore().getRemaining(), equalTo(4500));

        // Due to previous request header, this request has to wait for reset
        // results in an additional request
        org.getRepository("github-api");
        updateTestRateLimit();
        assertThat(mockGitHub.getRequestCount(), equalTo(6));
        assertThat(rateLimit.getCore().getRemaining(), equalTo(4400));

        // Due to previous request header, this request has to wait for reset
        // results in two additional requests because even after first reset we're still outside quota
        org.getRepository("github-api");
        updateTestRateLimit();
        assertThat(mockGitHub.getRequestCount(), equalTo(9));
        assertThat(rateLimit.getCore().getRemaining(), equalTo(4601));
    }

    protected void updateTestRateLimit() {
        previousLimit = rateLimit;
        rateLimit = gitHub.lastRateLimit();
    }

    private static GHRepository getRepository(GitHub gitHub) throws IOException {
        return gitHub.getOrganization("github-api-test-org").getRepository("github-api");
    }

}
