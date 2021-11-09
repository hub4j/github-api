package org.kohsuke.github.extras.okhttp3;

import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import com.github.tomakehurst.wiremock.matching.RequestPatternBuilder;
import okhttp3.Cache;
import okhttp3.OkHttpClient;
import org.apache.commons.io.FileUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.kohsuke.github.AbstractGitHubWireMockTest;
import org.kohsuke.github.GHRateLimit;
import org.kohsuke.github.GHRepository;
import org.kohsuke.github.GitHub;

import java.io.File;
import java.io.IOException;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.lessThanOrEqualTo;
import static org.hamcrest.Matchers.nullValue;
import static org.hamcrest.core.Is.is;
import static org.junit.Assume.assumeFalse;
import static org.junit.Assume.assumeTrue;

/**
 * Test showing the behavior of OkHttpGitHubConnector with and without cache.
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
public class OkHttpConnectorTest extends AbstractGitHubWireMockTest {

    public OkHttpConnectorTest() {
        useDefaultGitHub = false;
    }

    private static int defaultRateLimitUsed = 17;
    private static int okhttpRateLimitUsed = 17;
    private static int maxAgeZeroRateLimitUsed = 7;
    private static int maxAgeThreeRateLimitUsed = 7;
    private static int maxAgeNoneRateLimitUsed = 4;

    private static int userRequestCount = 0;

    private static int defaultNetworkRequestCount = 16;
    private static int okhttpNetworkRequestCount = 16;
    private static int maxAgeZeroNetworkRequestCount = 16;
    private static int maxAgeThreeNetworkRequestCount = 9;
    private static int maxAgeNoneNetworkRequestCount = 5;

    private static int maxAgeZeroHitCount = 10;
    private static int maxAgeThreeHitCount = 10;
    private static int maxAgeNoneHitCount = 11;

    private GHRateLimit rateLimitBefore;
    private Cache cache = null;

    @Override
    protected WireMockConfiguration getWireMockOptions() {
        return super.getWireMockOptions()
                // Use the same data files as the 2.x test
                .usingFilesUnderDirectory(baseRecordPath.replace("/okhttp3/", "/"))
                .extensions(templating.newResponseTransformer());
    }

    @Before
    public void setupRepo() throws Exception {
        if (mockGitHub.isUseProxy()) {
            GHRepository repo = getRepository(getNonRecordingGitHub());
            repo.setDescription("Resetting");

            // Let things settle a bit between tests when working against the live site
            Thread.sleep(5000);
            userRequestCount = 1;
        }
    }

    @After
    public void deleteCache() throws IOException {
        if (cache != null) {
            cache.delete();
        }
    }

    @Test
    public void DefaultConnector() throws Exception {

        this.gitHub = getGitHubBuilder().withEndpoint(mockGitHub.apiServer().baseUrl()).build();

        doTestActions();

        // Testing behavior after change
        // Uncached connection gets updated correctly but at cost of rate limit
        assertThat(getRepository(gitHub).getDescription(), is("Tricky"));

        checkRequestAndLimit(defaultNetworkRequestCount, defaultRateLimitUsed);
    }

    @Test
    public void OkHttpConnector_NoCache() throws Exception {

        OkHttpClient client = createClient(false);
        OkHttpGitHubConnector connector = new OkHttpGitHubConnector(client);

        this.gitHub = getGitHubBuilder().withEndpoint(mockGitHub.apiServer().baseUrl())
                .withConnector(connector)
                .build();

        doTestActions();

        // Testing behavior after change
        // Uncached okhttp connection gets updated correctly but at cost of rate limit
        assertThat(getRepository(gitHub).getDescription(), is("Tricky"));

        checkRequestAndLimit(okhttpNetworkRequestCount, okhttpRateLimitUsed);

        assertThat("Cache", cache, is(nullValue()));
    }

    @Test
    public void OkHttpConnector_Cache_MaxAgeNone() throws Exception {
        // The responses were recorded from github, but the Date headers
        // have been templated to make caching behavior work as expected.
        // This is reasonable as long as the number of network requests matches up.
        snapshotNotAllowed();

        OkHttpClient client = createClient(true);
        OkHttpGitHubConnector connector = new OkHttpGitHubConnector(client, -1);

        this.gitHub = getGitHubBuilder().withEndpoint(mockGitHub.apiServer().baseUrl())
                .withConnector(connector)
                .build();

        doTestActions();

        // Testing behavior after change
        // NOTE: this is wrong! The live data changed!
        // Due to max-age (default 60 from response) the cache returns the old data.
        assertThat(getRepository(gitHub).getDescription(), is(mockGitHub.getMethodName()));

        checkRequestAndLimit(maxAgeNoneNetworkRequestCount, maxAgeNoneRateLimitUsed);

        // NOTE: this is actually bad.
        // This elevated hit count is the stale requests returning bad data took longer to detect a change.
        assertThat("getHitCount", cache.hitCount(), is(maxAgeNoneHitCount));
    }

    @Test
    public void OkHttpConnector_Cache_MaxAge_Three() throws Exception {

        // NOTE: This test is very timing sensitive.
        // It can be run locally to verify behavior but snapshot data is to touchy
        assumeFalse("Test only valid when not taking a snapshot", mockGitHub.isTakeSnapshot());
        assumeTrue("Test only valid when proxying (-Dtest.github.useProxy to enable)", mockGitHub.isUseProxy());

        OkHttpClient client = createClient(true);
        OkHttpGitHubConnector connector = new OkHttpGitHubConnector(client, 3);

        this.gitHub = getGitHubBuilder().withEndpoint(mockGitHub.apiServer().baseUrl())
                .withConnector(connector)
                .build();

        doTestActions();

        // Due to max-age=3 this eventually checks the site and gets updated information. Yay?
        assertThat(getRepository(gitHub).getDescription(), is("Tricky"));

        checkRequestAndLimit(maxAgeThreeNetworkRequestCount, maxAgeThreeRateLimitUsed);

        assertThat("getHitCount", cache.hitCount(), is(maxAgeThreeHitCount));
    }

    @Test
    public void OkHttpConnector_Cache_MaxAgeDefault_Zero() throws Exception {
        // The responses were recorded from github, but the Date headers
        // have been templated to make caching behavior work as expected.
        // This is reasonable as long as the number of network requests matches up.
        snapshotNotAllowed();

        OkHttpClient client = createClient(true);
        OkHttpGitHubConnector connector = new OkHttpGitHubConnector(client);

        this.gitHub = getGitHubBuilder().withEndpoint(mockGitHub.apiServer().baseUrl())
                .withConnector(connector)
                .build();

        doTestActions();

        // Testing behavior after change
        // NOTE: max-age=0 produces the same result at uncached without added rate-limit use.
        assertThat(getRepository(gitHub).getDescription(), is("Tricky"));

        checkRequestAndLimit(maxAgeZeroNetworkRequestCount, maxAgeZeroRateLimitUsed);

        assertThat("getHitCount", cache.hitCount(), is(maxAgeZeroHitCount));
    }

    private void checkRequestAndLimit(int networkRequestCount, int rateLimitUsed) throws IOException {
        GHRateLimit rateLimitAfter = gitHub.rateLimit();
        assertThat("Request Count", getRequestCount(), is(networkRequestCount + userRequestCount));

        // Rate limit must be under this value, but if it wiggles we don't care
        assertThat("Rate Limit Change",
                rateLimitBefore.remaining - rateLimitAfter.remaining,
                is(lessThanOrEqualTo(rateLimitUsed + userRequestCount)));

    }

    private int getRequestCount() {
        return mockGitHub.apiServer().countRequestsMatching(RequestPatternBuilder.allRequests().build()).getCount();
    }

    private OkHttpClient createClient(boolean useCache) throws IOException {
        OkHttpClient.Builder builder = new OkHttpClient().newBuilder();

        if (useCache) {
            File cacheDir = new File("target/cache/" + baseFilesClassPath + "/" + mockGitHub.getMethodName());
            cacheDir.mkdirs();
            FileUtils.cleanDirectory(cacheDir);
            cache = new Cache(cacheDir, 100 * 1024L * 1024L);

            builder.cache(cache);
        }

        return builder.build();
    }

    /**
     * This is a standard set of actions to be performed with each connector
     *
     * @throws Exception
     */
    private void doTestActions() throws Exception {
        rateLimitBefore = gitHub.getRateLimit();

        String name = mockGitHub.getMethodName();

        GHRepository repo = getRepository(gitHub);

        // Testing behavior when nothing has changed.
        pollForChange("Resetting");
        assertThat(getRepository(gitHub).getDescription(), is("Resetting"));

        repo.setDescription(name);

        pollForChange(name);

        // Test behavior after change
        assertThat(getRepository(gitHub).getDescription(), is(name));

        // Get Tricky - make a change via a different client
        if (mockGitHub.isUseProxy()) {
            GHRepository altRepo = getRepository(getNonRecordingGitHub());
            altRepo.setDescription("Tricky");
        }

        // Testing behavior after change
        pollForChange("Tricky");
    }

    private void pollForChange(String name) throws IOException, InterruptedException {
        getRepository(gitHub).getDescription();
        Thread.sleep(500);
        getRepository(gitHub).getDescription();
        // This is only interesting when running the max-age=3 test which currently only runs with proxy
        // Disabled to speed up the tests
        if (mockGitHub.isUseProxy()) {
            Thread.sleep(1000);
        }
        getRepository(gitHub).getDescription();
        // This is only interesting when running the max-age=3 test which currently only runs with proxy
        // Disabled to speed up the tests
        if (mockGitHub.isUseProxy()) {
            Thread.sleep(4000);
        }
    }

    private static GHRepository getRepository(GitHub gitHub) throws IOException {
        return gitHub.getOrganization("hub4j-test-org").getRepository("github-api");
    }

}
