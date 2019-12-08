package org.kohsuke.github;

import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import com.github.tomakehurst.wiremock.extension.responsetemplating.ResponseTemplateTransformer;
import com.github.tomakehurst.wiremock.matching.RequestPatternBuilder;
import okhttp3.Cache;
import okhttp3.OkHttpClient;
import org.apache.commons.io.FileUtils;
import org.hamcrest.CoreMatchers;
import org.junit.Before;
import org.junit.Test;
import org.kohsuke.github.extras.okhttp3.OkHttpConnector;

import java.io.File;
import java.io.IOException;
import java.util.Date;

import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;

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
public class GHRateLimitTest extends AbstractGitHubWireMockTest {

    public GHRateLimitTest() {
        useDefaultGitHub = false;
    }

    @Override
    protected WireMockConfiguration getWireMockOptions() {
        return super.getWireMockOptions()
                .extensions(ResponseTemplateTransformer.builder().global(true).maxCacheEntries(0L).build());
    }

    @Test
    public void testGitHubRateLimit() throws Exception {
        // Customized response that templates the date to keep things working
        snapshotNotAllowed();

        assertThat(mockGitHub.getRequestCount(), equalTo(0));
        GHRateLimit rateLimit = null;

        Date lastReset = new Date(System.currentTimeMillis() / 1000L);
        int lastRemaining = 5000;

        // Give this a moment
        Thread.sleep(1000);

        // -------------------------------------------------------------
        // /user gets response with rate limit information
        gitHub = getGitHubBuilder().withEndpoint(mockGitHub.apiServer().baseUrl()).build();
        gitHub.getMyself();

        assertThat(mockGitHub.getRequestCount(), equalTo(1));

        // Since we already had rate limit info these don't request again
        rateLimit = gitHub.lastRateLimit();
        assertThat(rateLimit, notNullValue());
        assertThat(rateLimit.limit, equalTo(5000));
        assertThat(rateLimit.getLimit(), equalTo(5000));
        lastRemaining = rateLimit.getRemaining();
        assertThat(rateLimit.getResetDate().compareTo(lastReset), greaterThanOrEqualTo(0));
        lastReset = rateLimit.getResetDate();

        GHRateLimit headerRateLimit = rateLimit;

        // Give this a moment
        Thread.sleep(1000);

        // ratelimit() uses headerRateLimit if available and headerRateLimit is not expired
        assertThat(gitHub.rateLimit(), equalTo(headerRateLimit));

        assertThat(mockGitHub.getRequestCount(), equalTo(1));

        // Give this a moment
        Thread.sleep(1000);

        // Always requests new info
        rateLimit = gitHub.getRateLimit();
        assertThat(mockGitHub.getRequestCount(), equalTo(2));

        assertThat(rateLimit, notNullValue());
        assertThat(rateLimit.limit, equalTo(5000));
        assertThat(rateLimit.getLimit(), equalTo(5000));
        // rate limit request is free
        assertThat(rateLimit.remaining, equalTo(lastRemaining));
        assertThat(rateLimit.getRemaining(), equalTo(lastRemaining));
        assertThat(rateLimit.getResetDate().compareTo(lastReset), greaterThanOrEqualTo(0));

        // Give this a moment
        Thread.sleep(1000);

        // Always requests new info
        rateLimit = gitHub.getRateLimit();
        assertThat(mockGitHub.getRequestCount(), equalTo(3));

        assertThat(rateLimit, notNullValue());
        assertThat(rateLimit.limit, equalTo(5000));
        assertThat(rateLimit.getLimit(), equalTo(5000));
        // rate limit request is free
        assertThat(rateLimit.remaining, equalTo(lastRemaining));
        assertThat(rateLimit.getRemaining(), equalTo(lastRemaining));
        assertThat(rateLimit.getResetDate().compareTo(lastReset), greaterThanOrEqualTo(0));

        gitHub.getOrganization(GITHUB_API_TEST_ORG);
        assertThat(mockGitHub.getRequestCount(), equalTo(4));

        assertThat(gitHub.lastRateLimit(), not(equalTo(headerRateLimit)));
        rateLimit = gitHub.lastRateLimit();
        assertThat(rateLimit, notNullValue());
        assertThat(rateLimit.limit, equalTo(5000));
        assertThat(rateLimit.getLimit(), equalTo(5000));
        // Org costs limit to query
        assertThat(rateLimit.remaining, equalTo(lastRemaining - 1));
        assertThat(rateLimit.getRemaining(), equalTo(lastRemaining - 1));
        assertThat(rateLimit.getResetDate().compareTo(lastReset), greaterThanOrEqualTo(0));
        lastReset = rateLimit.getResetDate();
        headerRateLimit = rateLimit;

        // ratelimit() should prefer headerRateLimit when it is most recent and not expired
        assertThat(gitHub.rateLimit(), equalTo(headerRateLimit));

        assertThat(mockGitHub.getRequestCount(), equalTo(4));

        // Give this a moment
        Thread.sleep(2000);

        // Always requests new info
        rateLimit = gitHub.getRateLimit();
        assertThat(mockGitHub.getRequestCount(), equalTo(5));

        assertThat(rateLimit, notNullValue());
        assertThat(rateLimit.limit, equalTo(5000));
        assertThat(rateLimit.getLimit(), equalTo(5000));
        // Org costs limit to query
        assertThat(rateLimit.remaining, equalTo(lastRemaining - 1));
        assertThat(rateLimit.getRemaining(), equalTo(lastRemaining - 1));
        assertThat(rateLimit.getResetDate().compareTo(lastReset), greaterThanOrEqualTo(0));

        // When getRateLimit() succeeds, headerRateLimit updates as usual as well (if needed)
        // These are separate instances, but should be equal
        assertThat(gitHub.rateLimit(), not(sameInstance(rateLimit)));

        // Verify different record instances can be compared
        assertThat(gitHub.rateLimit().getCore(), equalTo(rateLimit.getCore()));

        // Verify different instances can be compared
        // TODO: This is not work currently because the header rate limit has unknowns for records other than core.
        // assertThat(gitHub.rateLimit().getCore(), equalTo(rateLimit.getCore()));

        assertThat(gitHub.rateLimit(), not(sameInstance(headerRateLimit)));
        assertThat(gitHub.rateLimit(), sameInstance(gitHub.lastRateLimit()));

        assertThat(mockGitHub.getRequestCount(), equalTo(5));
    }

    @Test
    public void testGitHubEnterpriseDoesNotHaveRateLimit() throws Exception {
        // Customized response that results in file not found the same as GitHub Enterprise
        snapshotNotAllowed();
        assertThat(mockGitHub.getRequestCount(), equalTo(0));
        GHRateLimit rateLimit = null;

        Date lastReset = new Date(System.currentTimeMillis() / 1000L);

        // Give this a moment
        Thread.sleep(1000);

        // -------------------------------------------------------------
        // Before any queries, rate limit starts as null but may be requested
        gitHub = GitHub.connectToEnterprise(mockGitHub.apiServer().baseUrl(), "bogus", "bogus");
        assertThat(mockGitHub.getRequestCount(), equalTo(0));

        assertThat(gitHub.lastRateLimit(), CoreMatchers.nullValue());

        rateLimit = gitHub.rateLimit();
        assertThat(rateLimit.getCore(), instanceOf(GHRateLimit.UnknownLimitRecord.class));
        assertThat(rateLimit.limit, equalTo(GHRateLimit.UnknownLimitRecord.unknownLimit));
        assertThat(rateLimit.getLimit(), equalTo(GHRateLimit.UnknownLimitRecord.unknownLimit));
        assertThat(rateLimit.remaining, equalTo(GHRateLimit.UnknownLimitRecord.unknownRemaining));
        assertThat(rateLimit.getRemaining(), equalTo(GHRateLimit.UnknownLimitRecord.unknownRemaining));
        assertThat(rateLimit.getResetDate().compareTo(lastReset), equalTo(1));
        lastReset = rateLimit.getResetDate();

        assertThat(mockGitHub.getRequestCount(), equalTo(1));

        // last is still null, because it actually means lastHeaderRateLimit
        assertThat(gitHub.lastRateLimit(), CoreMatchers.nullValue());

        assertThat(mockGitHub.getRequestCount(), equalTo(1));

        // Give this a moment
        Thread.sleep(1000);

        // -------------------------------------------------------------
        // First call to /user gets response without rate limit information
        gitHub = GitHub.connectToEnterprise(mockGitHub.apiServer().baseUrl(), "bogus", "bogus");
        gitHub.getMyself();
        assertThat(mockGitHub.getRequestCount(), equalTo(2));

        assertThat(gitHub.lastRateLimit(), CoreMatchers.nullValue());

        rateLimit = gitHub.rateLimit();
        assertThat(rateLimit.getCore(), instanceOf(GHRateLimit.UnknownLimitRecord.class));
        assertThat(rateLimit.limit, equalTo(GHRateLimit.UnknownLimitRecord.unknownLimit));
        assertThat(rateLimit.getLimit(), equalTo(GHRateLimit.UnknownLimitRecord.unknownLimit));
        assertThat(rateLimit.remaining, equalTo(GHRateLimit.UnknownLimitRecord.unknownRemaining));
        assertThat(rateLimit.getRemaining(), equalTo(GHRateLimit.UnknownLimitRecord.unknownRemaining));
        assertThat(rateLimit.getResetDate().compareTo(lastReset), equalTo(1));
        lastReset = rateLimit.getResetDate();

        assertThat(mockGitHub.getRequestCount(), equalTo(3));

        // Give this a moment
        Thread.sleep(1000);

        // Always requests new info
        rateLimit = gitHub.getRateLimit();
        assertThat(mockGitHub.getRequestCount(), equalTo(4));

        assertThat(rateLimit.getCore(), instanceOf(GHRateLimit.UnknownLimitRecord.class));
        assertThat(rateLimit.limit, equalTo(GHRateLimit.UnknownLimitRecord.unknownLimit));
        assertThat(rateLimit.getLimit(), equalTo(GHRateLimit.UnknownLimitRecord.unknownLimit));
        assertThat(rateLimit.remaining, equalTo(GHRateLimit.UnknownLimitRecord.unknownRemaining));
        assertThat(rateLimit.getRemaining(), equalTo(GHRateLimit.UnknownLimitRecord.unknownRemaining));
        assertThat(rateLimit.getResetDate().compareTo(lastReset), equalTo(1));

        // Give this a moment
        Thread.sleep(1000);

        // last is still null, because it actually means lastHeaderRateLimit
        assertThat(gitHub.lastRateLimit(), CoreMatchers.nullValue());

        // ratelimit() tries not to make additional requests, uses queried rate limit since header not available
        Thread.sleep(1000);
        assertThat(gitHub.rateLimit(), sameInstance(rateLimit));

        // -------------------------------------------------------------
        // Second call to /user gets response with rate limit information
        gitHub = GitHub.connectToEnterprise(mockGitHub.apiServer().baseUrl(), "bogus", "bogus");
        gitHub.getMyself();
        assertThat(mockGitHub.getRequestCount(), equalTo(5));

        // Since we already had rate limit info these don't request again
        rateLimit = gitHub.lastRateLimit();
        assertThat(rateLimit, notNullValue());
        assertThat(rateLimit.limit, equalTo(5000));
        assertThat(rateLimit.getLimit(), equalTo(5000));
        assertThat(rateLimit.remaining, equalTo(4978));
        assertThat(rateLimit.getRemaining(), equalTo(4978));
        assertThat(rateLimit.getResetDate().compareTo(lastReset), greaterThanOrEqualTo(0));
        lastReset = rateLimit.getResetDate();

        GHRateLimit headerRateLimit = rateLimit;

        // Give this a moment
        Thread.sleep(1000);

        // ratelimit() uses headerRateLimit if available and headerRateLimit is not expired
        assertThat(gitHub.rateLimit(), equalTo(headerRateLimit));

        assertThat(mockGitHub.getRequestCount(), equalTo(5));

        // Give this a moment
        Thread.sleep(1000);

        // Always requests new info
        rateLimit = gitHub.getRateLimit();
        assertThat(mockGitHub.getRequestCount(), equalTo(6));

        assertThat(rateLimit.getCore(), instanceOf(GHRateLimit.UnknownLimitRecord.class));
        assertThat(rateLimit.limit, equalTo(GHRateLimit.UnknownLimitRecord.unknownLimit));
        assertThat(rateLimit.getLimit(), equalTo(GHRateLimit.UnknownLimitRecord.unknownLimit));
        assertThat(rateLimit.remaining, equalTo(GHRateLimit.UnknownLimitRecord.unknownRemaining));
        assertThat(rateLimit.getRemaining(), equalTo(GHRateLimit.UnknownLimitRecord.unknownRemaining));
        assertThat(rateLimit.getResetDate().compareTo(lastReset), equalTo(1));

        // ratelimit() should prefer headerRateLimit when getRateLimit fails and headerRateLimit is not expired
        assertThat(gitHub.rateLimit(), equalTo(headerRateLimit));

        assertThat(mockGitHub.getRequestCount(), equalTo(6));

        // Wait for the header
        Thread.sleep(1000);
    }

    // These tests should behave the same, showing server time adjustment working
    @Test
    public void testGitHubRateLimitExpirationServerFiveMinutesAhead() throws Exception {
        executeExpirationTest();
    }

    @Test
    public void testGitHubRateLimitExpirationServerFiveMinutesBehind() throws Exception {
        executeExpirationTest();
    }

    private void executeExpirationTest() throws Exception {
        // Customized response that templates the date to keep things working
        snapshotNotAllowed();

        assertThat(mockGitHub.getRequestCount(), equalTo(0));
        GHRateLimit rateLimit = null;
        GHRateLimit headerRateLimit = null;

        // Give this a moment
        Thread.sleep(1000);

        // -------------------------------------------------------------
        // /user gets response with rate limit information
        gitHub = getGitHubBuilder().withEndpoint(mockGitHub.apiServer().baseUrl()).build();
        gitHub.getMyself();

        assertThat(mockGitHub.getRequestCount(), equalTo(1));

        // Since we already had rate limit info these don't request again
        headerRateLimit = gitHub.lastRateLimit();
        rateLimit = gitHub.rateLimit();

        assertThat(rateLimit, notNullValue());
        assertThat("rateLimit() selects header instance when not expired, does not ask server", rateLimit,
                sameInstance(headerRateLimit));

        // Nothing changes still valid
        Thread.sleep(1000);

        assertThat("rateLimit() selects header instance when not expired, does not ask server", gitHub.rateLimit(),
                sameInstance(headerRateLimit));
        assertThat("rateLimit() selects header instance when not expired, does not ask server", gitHub.lastRateLimit(),
                sameInstance(headerRateLimit));

        assertThat(mockGitHub.getRequestCount(), equalTo(1));

        // This time, rateLimit() should find an expired record and get a new one.
        Thread.sleep(3000);

        assertThat("Header instance has expired", gitHub.lastRateLimit().isExpired(), is(true));

        assertThat("rateLimit() will ask server when header instance expires and it has not called getRateLimit() yet",
                gitHub.rateLimit(), not(sameInstance(rateLimit)));

        assertThat("lastRateLimit() (header instance) is populated as part of internal call to getRateLimit()",
                gitHub.lastRateLimit(), not(sameInstance(rateLimit)));

        assertThat("After request, rateLimit() selects header instance since it has been refreshed", gitHub.rateLimit(),
                sameInstance(gitHub.lastRateLimit()));

        headerRateLimit = gitHub.lastRateLimit();

        assertThat(mockGitHub.getRequestCount(), equalTo(2));

        // This time, rateLimit() should find an expired header record, but a valid returned record
        Thread.sleep(4000);

        rateLimit = gitHub.rateLimit();

        // Using custom data to have a header instance that expires before the queried instance
        assertThat(
                "if header instance expires but queried instance is valid, ratelimit() uses it without asking server",
                gitHub.rateLimit(), not(sameInstance(gitHub.lastRateLimit())));

        assertThat("ratelimit() should almost never return a return a GHRateLimit that is already expired",
                gitHub.rateLimit().isExpired(), is(false));

        assertThat("Header instance hasn't been reloaded", gitHub.lastRateLimit(), sameInstance(headerRateLimit));
        assertThat("Header instance has expired", gitHub.lastRateLimit().isExpired(), is(true));

        assertThat(mockGitHub.getRequestCount(), equalTo(2));

        // Finally they both expire and rateLimit() should find both expired and get a new record
        Thread.sleep(2000);

        headerRateLimit = gitHub.rateLimit();

        assertThat("rateLimit() has asked server for new information", gitHub.rateLimit(),
                not(sameInstance(rateLimit)));
        assertThat("rateLimit() has asked server for new information", gitHub.lastRateLimit(),
                not(sameInstance(rateLimit)));

        assertThat("rateLimit() selects header instance when not expired, does not ask server", gitHub.rateLimit(),
                sameInstance((gitHub.lastRateLimit())));

        assertThat(mockGitHub.getRequestCount(), equalTo(3));
    }

    private static GHRepository getRepository(GitHub gitHub) throws IOException {
        return gitHub.getOrganization("github-api-test-org").getRepository("github-api");
    }

}
