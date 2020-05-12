package org.kohsuke.github;

import com.fasterxml.jackson.databind.exc.MismatchedInputException;
import com.fasterxml.jackson.databind.exc.ValueInstantiationException;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import org.hamcrest.CoreMatchers;
import org.junit.Test;

import java.io.IOException;
import java.time.Duration;
import java.util.Date;

import static org.hamcrest.Matchers.*;
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
 *
 * @author Liam Newman
 */
public class GHRateLimitTest extends AbstractGitHubWireMockTest {

    GHRateLimit rateLimit = null;
    GHRateLimit previousLimit = null;

    public GHRateLimitTest() {
        useDefaultGitHub = false;
    }

    @Override
    protected WireMockConfiguration getWireMockOptions() {
        return super.getWireMockOptions().extensions(templating.newResponseTransformer());
    }

    @Test
    public void testGitHubRateLimit() throws Exception {
        // Customized response that templates the date to keep things working
        snapshotNotAllowed();

        assertThat(mockGitHub.getRequestCount(), equalTo(0));

        // 4897 is just the what the limit was when the snapshot was taken
        previousLimit = GHRateLimit.fromHeaderRecord(new GHRateLimit.Record(5000,
                4897,
                (templating.testStartDate.getTime() + Duration.ofHours(1).toMillis()) / 1000L));

        // -------------------------------------------------------------
        // /user gets response with rate limit information
        gitHub = getGitHubBuilder().withEndpoint(mockGitHub.apiServer().baseUrl()).build();
        gitHub.getMyself();

        assertThat(mockGitHub.getRequestCount(), equalTo(1));

        // Since we already had rate limit info these don't request again
        rateLimit = gitHub.lastRateLimit();
        verifyRateLimitValues(previousLimit, previousLimit.getRemaining());
        previousLimit = rateLimit;

        GHRateLimit headerRateLimit = rateLimit;

        // Give this a moment
        Thread.sleep(1500);

        // ratelimit() uses headerRateLimit if available and headerRateLimit is not expired
        assertThat(gitHub.rateLimit(), equalTo(headerRateLimit));

        assertThat(mockGitHub.getRequestCount(), equalTo(1));

        // Give this a moment
        Thread.sleep(1500);

        // Always requests new info
        rateLimit = gitHub.getRateLimit();
        assertThat(mockGitHub.getRequestCount(), equalTo(2));

        // Because remaining and reset date are unchanged, the header should be unchanged as well
        assertThat(gitHub.lastRateLimit(), sameInstance(headerRateLimit));

        // rate limit request is free, remaining is unchanged
        verifyRateLimitValues(previousLimit, previousLimit.getRemaining());
        previousLimit = rateLimit;

        // Give this a moment
        Thread.sleep(1500);

        // Always requests new info
        rateLimit = gitHub.getRateLimit();
        assertThat(mockGitHub.getRequestCount(), equalTo(3));

        // Because remaining and reset date are unchanged, the header should be unchanged as well
        assertThat(gitHub.lastRateLimit(), sameInstance(headerRateLimit));

        // rate limit request is free, remaining is unchanged
        verifyRateLimitValues(previousLimit, previousLimit.getRemaining());
        previousLimit = rateLimit;

        gitHub.getOrganization(GITHUB_API_TEST_ORG);
        assertThat(mockGitHub.getRequestCount(), equalTo(4));

        // Because remaining has changed the header should be different
        assertThat(gitHub.lastRateLimit(), not(sameInstance(headerRateLimit)));
        assertThat(gitHub.lastRateLimit(), not(equalTo(headerRateLimit)));
        rateLimit = gitHub.lastRateLimit();

        // Org costs limit to query
        verifyRateLimitValues(previousLimit, previousLimit.getRemaining() - 1);

        previousLimit = rateLimit;
        headerRateLimit = rateLimit;

        // ratelimit() should prefer headerRateLimit when it is most recent and not expired
        assertThat(gitHub.rateLimit(), sameInstance(headerRateLimit));

        assertThat(mockGitHub.getRequestCount(), equalTo(4));

        // AT THIS POINT WE SIMULATE A RATE LIMIT RESET

        // Give this a moment
        Thread.sleep(2000);

        // Always requests new info
        rateLimit = gitHub.getRateLimit();
        assertThat(mockGitHub.getRequestCount(), equalTo(5));

        // rate limit request is free, remaining is unchanged date is later
        verifyRateLimitValues(previousLimit, previousLimit.getRemaining(), true);
        previousLimit = rateLimit;

        // When getRateLimit() succeeds, headerRateLimit updates as usual as well (if needed)
        // These are separate instances, but should be equal
        assertThat(gitHub.rateLimit(), not(sameInstance(rateLimit)));

        // Verify different record instances can be compared
        assertThat(gitHub.rateLimit().getCore(), equalTo(rateLimit.getCore()));

        // Verify different instances can be compared
        // TODO: This is not work currently because the header rate limit has unknowns for records other than core.
        // assertThat(gitHub.rateLimit(), equalTo(rateLimit));

        assertThat(gitHub.rateLimit(), not(sameInstance(headerRateLimit)));
        assertThat(gitHub.rateLimit(), sameInstance(gitHub.lastRateLimit()));

        assertThat(mockGitHub.getRequestCount(), equalTo(5));
    }

    private void verifyRateLimitValues(GHRateLimit previousLimit, int remaining) {
        verifyRateLimitValues(previousLimit, remaining, false);
    }

    private void verifyRateLimitValues(GHRateLimit previousLimit, int remaining, boolean changedResetDate) {
        // Basic checks of values
        assertThat(rateLimit, notNullValue());
        assertThat(rateLimit.getLimit(), equalTo(previousLimit.getLimit()));
        assertThat(rateLimit.getRemaining(), equalTo(remaining));

        // Check that the reset date of the current limit is not older than the previous one
        long diffMillis = rateLimit.getResetDate().getTime() - previousLimit.getResetDate().getTime();

        assertThat(diffMillis, greaterThanOrEqualTo(0L));
        if (changedResetDate) {
            assertThat(diffMillis, greaterThan(1000L));
        } else {
            assertThat(diffMillis, lessThanOrEqualTo(1000L));
        }

        // Additional checks for record values
        assertThat(rateLimit.getCore().getLimit(), equalTo(rateLimit.getLimit()));
        assertThat(rateLimit.getCore().getRemaining(), equalTo(rateLimit.getRemaining()));
        assertThat(rateLimit.getCore().getResetEpochSeconds(), equalTo(rateLimit.getResetEpochSeconds()));
        assertThat(rateLimit.getCore().getResetDate(), equalTo(rateLimit.getResetDate()));

        // Additional checks for deprecated values
        assertThat(rateLimit.limit, equalTo(rateLimit.getLimit()));
        assertThat(rateLimit.remaining, equalTo(rateLimit.getRemaining()));
        assertThat(rateLimit.reset.getTime(), equalTo(rateLimit.getResetEpochSeconds()));
    }

    @Test
    public void testGitHubEnterpriseDoesNotHaveRateLimit() throws Exception {
        // Customized response that results in file not found the same as GitHub Enterprise
        snapshotNotAllowed();
        assertThat(mockGitHub.getRequestCount(), equalTo(0));
        GHRateLimit rateLimit = null;

        Date lastReset = new Date(System.currentTimeMillis() / 1000L);

        // Give this a moment
        Thread.sleep(1500);

        // -------------------------------------------------------------
        // Before any queries, rate limit starts as null but may be requested
        gitHub = GitHub.connectToEnterprise(mockGitHub.apiServer().baseUrl(), "bogus", "bogus");
        assertThat(mockGitHub.getRequestCount(), equalTo(0));

        assertThat(gitHub.lastRateLimit(), CoreMatchers.nullValue());

        rateLimit = gitHub.rateLimit();
        assertThat(rateLimit.getCore(), instanceOf(GHRateLimit.UnknownLimitRecord.class));
        assertThat(rateLimit.getLimit(), equalTo(GHRateLimit.UnknownLimitRecord.unknownLimit));
        assertThat(rateLimit.getRemaining(), equalTo(GHRateLimit.UnknownLimitRecord.unknownRemaining));
        assertThat(rateLimit.getResetDate().compareTo(lastReset), equalTo(1));
        lastReset = rateLimit.getResetDate();

        assertThat(mockGitHub.getRequestCount(), equalTo(1));

        // last is still null, because it actually means lastHeaderRateLimit
        assertThat(gitHub.lastRateLimit(), CoreMatchers.nullValue());

        assertThat(mockGitHub.getRequestCount(), equalTo(1));

        // Give this a moment
        Thread.sleep(1500);

        // -------------------------------------------------------------
        // First call to /user gets response without rate limit information
        gitHub = GitHub.connectToEnterprise(mockGitHub.apiServer().baseUrl(), "bogus", "bogus");
        gitHub.getMyself();
        assertThat(mockGitHub.getRequestCount(), equalTo(2));

        assertThat(gitHub.lastRateLimit(), CoreMatchers.nullValue());

        rateLimit = gitHub.rateLimit();
        assertThat(rateLimit.getCore(), instanceOf(GHRateLimit.UnknownLimitRecord.class));
        assertThat(rateLimit.getLimit(), equalTo(GHRateLimit.UnknownLimitRecord.unknownLimit));
        assertThat(rateLimit.getRemaining(), equalTo(GHRateLimit.UnknownLimitRecord.unknownRemaining));
        assertThat(rateLimit.getResetDate().compareTo(lastReset), equalTo(1));
        lastReset = rateLimit.getResetDate();

        assertThat(mockGitHub.getRequestCount(), equalTo(3));

        // Give this a moment
        Thread.sleep(1500);

        // Always requests new info
        rateLimit = gitHub.getRateLimit();
        assertThat(mockGitHub.getRequestCount(), equalTo(4));

        assertThat(rateLimit.getCore(), instanceOf(GHRateLimit.UnknownLimitRecord.class));
        assertThat(rateLimit.getLimit(), equalTo(GHRateLimit.UnknownLimitRecord.unknownLimit));
        assertThat(rateLimit.getRemaining(), equalTo(GHRateLimit.UnknownLimitRecord.unknownRemaining));
        assertThat(rateLimit.getResetDate().compareTo(lastReset), equalTo(1));

        // Give this a moment
        Thread.sleep(1500);

        // last is still null, because it actually means lastHeaderRateLimit
        assertThat(gitHub.lastRateLimit(), CoreMatchers.nullValue());

        // ratelimit() tries not to make additional requests, uses queried rate limit since header not available
        Thread.sleep(1500);
        assertThat(gitHub.rateLimit(), sameInstance(rateLimit));

        // -------------------------------------------------------------
        // Second call to /user gets response with rate limit information
        gitHub = GitHub.connectToEnterprise(mockGitHub.apiServer().baseUrl(), "bogus", "bogus");
        gitHub.getMyself();
        assertThat(mockGitHub.getRequestCount(), equalTo(5));

        // Since we already had rate limit info these don't request again
        rateLimit = gitHub.lastRateLimit();
        assertThat(rateLimit, notNullValue());
        assertThat(rateLimit.getLimit(), equalTo(5000));
        assertThat(rateLimit.getRemaining(), equalTo(4978));
        // The previous record was an "Unknown", so even though this records resets sooner we take it
        assertThat(rateLimit.getResetDate().compareTo(lastReset), equalTo(-1));
        lastReset = rateLimit.getResetDate();

        GHRateLimit headerRateLimit = rateLimit;

        // Give this a moment
        Thread.sleep(1500);

        // ratelimit() uses headerRateLimit if available and headerRateLimit is not expired
        assertThat(gitHub.rateLimit(), sameInstance(headerRateLimit));

        assertThat(mockGitHub.getRequestCount(), equalTo(5));

        // Give this a moment
        Thread.sleep(1500);

        // Always requests new info
        rateLimit = gitHub.getRateLimit();
        assertThat(mockGitHub.getRequestCount(), equalTo(6));

        assertThat(rateLimit.getCore(), instanceOf(GHRateLimit.UnknownLimitRecord.class));
        assertThat(rateLimit.getLimit(), equalTo(GHRateLimit.UnknownLimitRecord.unknownLimit));
        assertThat(rateLimit.getRemaining(), equalTo(GHRateLimit.UnknownLimitRecord.unknownRemaining));
        assertThat(rateLimit.getResetDate().compareTo(lastReset), equalTo(1));

        // ratelimit() should prefer headerRateLimit when getRateLimit fails and headerRateLimit is not expired
        assertThat(gitHub.rateLimit(), equalTo(headerRateLimit));

        assertThat(mockGitHub.getRequestCount(), equalTo(6));

        // Wait for the header
        Thread.sleep(1500);
    }

    @Test
    public void testGitHubRateLimitWithBadData() throws Exception {
        snapshotNotAllowed();
        gitHub = getGitHubBuilder().withEndpoint(mockGitHub.apiServer().baseUrl()).build();
        gitHub.getMyself();
        try {
            gitHub.getRateLimit();
            fail("Invalid rate limit missing some records should throw");
        } catch (Exception e) {
            assertThat(e, instanceOf(HttpException.class));
            assertThat(e.getCause(), instanceOf(IOException.class));
            assertThat(e.getCause().getCause(), instanceOf(ValueInstantiationException.class));
            assertThat(e.getCause().getCause().getMessage(),
                    containsString(
                            "Cannot construct instance of `org.kohsuke.github.GHRateLimit`, problem: `java.lang.NullPointerException`"));
        }

        try {
            gitHub.getRateLimit();
            fail("Invalid rate limit record missing a value should throw");
        } catch (Exception e) {
            assertThat(e, instanceOf(HttpException.class));
            assertThat(e.getCause(), instanceOf(IOException.class));
            assertThat(e.getCause().getCause(), instanceOf(MismatchedInputException.class));
            assertThat(e.getCause().getCause().getMessage(),
                    containsString("Missing required creator property 'reset' (index 2)"));
        }

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
        Thread.sleep(1500);

        // -------------------------------------------------------------
        // /user gets response with rate limit information
        gitHub = getGitHubBuilder().withEndpoint(mockGitHub.apiServer().baseUrl()).build();
        gitHub.getMyself();

        assertThat(mockGitHub.getRequestCount(), equalTo(1));

        // Since we already had rate limit info these don't request again
        headerRateLimit = gitHub.lastRateLimit();
        rateLimit = gitHub.rateLimit();

        assertThat(rateLimit, notNullValue());
        assertThat("rateLimit() selects header instance when not expired, does not ask server",
                rateLimit,
                sameInstance(headerRateLimit));

        // Nothing changes still valid
        Thread.sleep(1500);

        assertThat("rateLimit() selects header instance when not expired, does not ask server",
                gitHub.rateLimit(),
                sameInstance(headerRateLimit));
        assertThat("lastRateLimit() always selects header instance, does not ask server",
                gitHub.lastRateLimit(),
                sameInstance(headerRateLimit));

        assertThat(mockGitHub.getRequestCount(), equalTo(1));

        // This time, rateLimit() should find an expired record and get a new one.
        Thread.sleep(2500);

        assertThat("Header instance has expired", gitHub.lastRateLimit().isExpired(), is(true));

        assertThat("rateLimit() will ask server when header instance expires and it has not called getRateLimit() yet",
                gitHub.rateLimit(),
                not(sameInstance(rateLimit)));

        assertThat("lastRateLimit() (header instance) is populated as part of internal call to getRateLimit()",
                gitHub.lastRateLimit(),
                not(sameInstance(rateLimit)));

        assertThat("After request, rateLimit() selects header instance since it has been refreshed",
                gitHub.rateLimit(),
                sameInstance(gitHub.lastRateLimit()));

        headerRateLimit = gitHub.lastRateLimit();

        assertThat(mockGitHub.getRequestCount(), equalTo(2));

        // This time, rateLimit() should find an expired header record, but a valid returned record
        Thread.sleep(4000);

        rateLimit = gitHub.rateLimit();

        // Using custom data to have a header instance that expires before the queried instance
        assertThat(
                "if header instance expires but queried instance is valid, ratelimit() uses it without asking server",
                gitHub.rateLimit(),
                not(sameInstance(gitHub.lastRateLimit())));

        assertThat("ratelimit() should almost never return a return a GHRateLimit that is already expired",
                gitHub.rateLimit().isExpired(),
                is(false));

        assertThat("Header instance hasn't been reloaded", gitHub.lastRateLimit(), sameInstance(headerRateLimit));
        assertThat("Header instance has expired", gitHub.lastRateLimit().isExpired(), is(true));

        assertThat(mockGitHub.getRequestCount(), equalTo(2));

        // Finally they both expire and rateLimit() should find both expired and get a new record
        Thread.sleep(2000);

        headerRateLimit = gitHub.rateLimit();

        assertThat("rateLimit() has asked server for new information",
                gitHub.rateLimit(),
                not(sameInstance(rateLimit)));
        assertThat("rateLimit() has asked server for new information",
                gitHub.lastRateLimit(),
                not(sameInstance(rateLimit)));

        assertThat("rateLimit() selects header instance when not expired, does not ask server",
                gitHub.rateLimit(),
                sameInstance((gitHub.lastRateLimit())));

        assertThat(mockGitHub.getRequestCount(), equalTo(3));
    }

    private static GHRepository getRepository(GitHub gitHub) throws IOException {
        return gitHub.getOrganization("hub4j-test-org").getRepository("github-api");
    }

}
