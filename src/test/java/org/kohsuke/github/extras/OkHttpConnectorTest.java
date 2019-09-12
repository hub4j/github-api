package org.kohsuke.github.extras;

import com.github.tomakehurst.wiremock.matching.RequestPatternBuilder;
import com.squareup.okhttp.OkUrlFactory;
import com.squareup.okhttp.Cache;
import com.squareup.okhttp.OkHttpClient;
import org.apache.commons.io.FileUtils;
import org.junit.Before;
import org.junit.Test;
import org.kohsuke.github.*;

import javax.xml.datatype.Duration;
import java.io.File;
import java.io.IOException;
import java.util.Objects;

import static org.hamcrest.Matchers.nullValue;
import static org.hamcrest.core.Is.is;
import static org.junit.Assume.assumeFalse;
import static org.junit.Assume.assumeTrue;

/**
 * Test showing the behavior of OkHttpConnector with and without cache.
 *
 * Key take aways:
 *
 * <ul>
 *   <li>These tests are artificial and intended to highlight the differences
 *   in behavior between scenarios. However, the differences they indicate are stark.</li>
 *   <li>Caching reduces rate limit consumption by at least a factor of two in even the simplest case.</li>
 *   <li>The OkHttp cache is pretty smart and will often connect read and write requests made
 *   on the same client and invalidate caches.</li>
 *   <li>Changes made outside the current client cause the OkHttp cache to return stale data.
 *   This is expected and correct behavior.</li>
 *   <li>"max-age=0" addresses the problem of external changes by revalidating caches for each request.
 *   This produces the same number of requests as OkHttp without caching, but those requests only
 *   count towards the GitHub rate limit if data has changes.</li>
 * </ul>
 *
 * @author Liam Newman
 */
public class OkHttpConnectorTest extends AbstractGitHubApiWireMockTest {

    private static int defaultRateLimitUsed = 21;
    private static int okhttpRateLimitUsed = 17;
    private static int maxAgeZeroRateLimitUsed = 7;
    private static int maxAgeThreeRateLimitUsed = 7;
    private static int maxAgeNoneRateLimitUsed = 4;

    private static int defaultNetworkRequestCount = 16;
    private static int okhttpNetworkRequestCount = 17;
    private static int maxAgeZeroNetworkRequestCount = 17;
    private static int maxAgeThreeNetworkRequestCount = 9;
    private static int maxAgeNoneNetworkRequestCount = 6;

    private static int maxAgeZeroHitCount = 10;
    private static int maxAgeThreeHitCount = 10;
    private static int maxAgeNoneHitCount = 11;

    @Before
    public void setupRepo() throws Exception {
        assumeFalse("Test only valid when not taking a snapshot", githubApi.isTakeSnapshot());
        assumeTrue("Test only valid when proxying (-Dtest.github.useProxy to enable)", githubApi.isUseProxy());

        // TODO: (bitiwseman) These tests work locally when proxying but run in to some kind of issue
        // when running via snapshot.  I think part of it is cache aging but there is also some
        // other issue which I do not have the bandwidth to track down right now.
        // For the moment, I'm committing this code as documentation of testing.

        if (githubApi.isUseProxy()) {
            GHRepository repo = getRepository(gitHubBeforeAfter);
            repo.setDescription("Resetting");

            // Let things settle a bit between tests when working against the live site
            Thread.sleep(5000);
        }
    }

    @Test
    public void DefaultConnector() throws Exception {

        GHRateLimit rateLimitBefore = gitHub.rateLimit();
        doTestActions();

        // Testing behavior after change
        // Uncached connection gets updated correctly but at cost of rate limit
        assertThat(getRepository(gitHub).getDescription(), is("Tricky"));

        GHRateLimit rateLimitAfter = gitHub.rateLimit();

        assertThat("Request Count",
            getRequestCount(),
            is(defaultNetworkRequestCount));

        assertThat("Rate Limit Change",
            rateLimitBefore.remaining - rateLimitAfter.remaining,
            is(defaultRateLimitUsed));
    }

    @Test
    public void OkHttpConnector_NoCache() throws Exception {

        OkHttpClient client = createClient(false);
        OkHttpConnector connector = new OkHttpConnector(new OkUrlFactory(client));

        this.gitHub = getGitHubBuilder()
            .withEndpoint(githubApi.baseUrl())
            .withConnector(connector)
            .build();

        GHRateLimit rateLimitBefore = gitHub.rateLimit();
        doTestActions();

        // Testing behavior after change
        // Uncached okhttp connection gets updated correctly but at cost of rate limit
        assertThat(getRepository(gitHub).getDescription(), is("Tricky"));

        GHRateLimit rateLimitAfter = gitHub.rateLimit();

        assertThat("Request Count",
            getRequestCount(),
            is(okhttpNetworkRequestCount));

        assertThat("Rate Limit Change",
            rateLimitBefore.remaining - rateLimitAfter.remaining,
            is(okhttpRateLimitUsed));

        Cache cache = client.getCache();
        assertThat("Cache", cache, is(nullValue()));
    }

    @Test
    public void OkHttpConnector_Cache_MaxAgeNone() throws Exception {

        OkHttpClient client = createClient(true);
        OkHttpConnector connector = new OkHttpConnector(new OkUrlFactory(client), -1);

        this.gitHub = getGitHubBuilder()
            .withEndpoint(githubApi.baseUrl())
            .withConnector(connector)
            .build();

        GHRateLimit rateLimitBefore = gitHub.rateLimit();
        doTestActions();

        // Testing behavior after change
        // NOTE: this is wrong!  The live data changed!
        // Due to max-age (default 60 from response) the cache returns the old data.
        assertThat(getRepository(gitHub).getDescription(), is(githubApi.getMethodName()));

        GHRateLimit rateLimitAfter = gitHub.rateLimit();

        assertThat("Request Count",
            getRequestCount(),
            is(maxAgeNoneNetworkRequestCount));

        assertThat("Rate Limit Change",
            rateLimitBefore.remaining - rateLimitAfter.remaining,
            is(maxAgeNoneRateLimitUsed));

        Cache cache = client.getCache();

        // NOTE: this is actually bad.
        // This elevated hit count is the stale requests returning bad data took longer to detect a change.
        assertThat("getHitCount",  cache.getHitCount(), is(maxAgeNoneHitCount));
    }

    @Test
    public void OkHttpConnector_Cache_MaxAge_Three() throws Exception {

        OkHttpClient client = createClient(true);
        OkHttpConnector connector = new OkHttpConnector(new OkUrlFactory(client), 3);

        this.gitHub = getGitHubBuilder()
            .withEndpoint(githubApi.baseUrl())
            .withConnector(connector)
            .build();

        GHRateLimit rateLimitBefore = gitHub.rateLimit();
        doTestActions();

        // Due to max-age=5 this eventually checks the site and gets updated information. Yay?
        assertThat(getRepository(gitHub).getDescription(), is("Tricky"));

        GHRateLimit rateLimitAfter = gitHub.rateLimit();

        assertThat("Request Count",
            getRequestCount(),
            is(maxAgeThreeNetworkRequestCount));

        assertThat("Rate Limit Change",
            rateLimitBefore.remaining - rateLimitAfter.remaining,
            is(maxAgeThreeRateLimitUsed));

        Cache cache = client.getCache();
        assertThat("getHitCount",  cache.getHitCount(), is(maxAgeThreeHitCount));
    }

    @Test
    public void OkHttpConnector_Cache_MaxAgeDefault_Zero() throws Exception {
        OkHttpClient client = createClient(true);
        OkHttpConnector connector = new OkHttpConnector(new OkUrlFactory(client));

        this.gitHub = getGitHubBuilder()
            .withEndpoint(githubApi.baseUrl())
            .withConnector(connector)
            .build();

        GHRateLimit rateLimitBefore = gitHub.rateLimit();
        doTestActions();

        // Testing behavior after change
        // NOTE: max-age=0 produces the same result at uncached without added rate-limit use.
        assertThat(getRepository(gitHub).getDescription(), is("Tricky"));

        GHRateLimit rateLimitAfter = gitHub.rateLimit();

        assertThat("Request Count",
            getRequestCount(),
            is(maxAgeZeroNetworkRequestCount));

        assertThat("Rate Limit Change",
            rateLimitBefore.remaining - rateLimitAfter.remaining,
            is(maxAgeZeroRateLimitUsed));

        Cache cache = client.getCache();
        assertThat("getHitCount",  cache.getHitCount(), is(maxAgeZeroHitCount));
    }

    private int getRequestCount() {
        return githubApi.countRequestsMatching(RequestPatternBuilder.allRequests().build()).getCount();
    }

    private OkHttpClient createClient(boolean useCache) throws IOException {
        OkHttpClient client = new OkHttpClient();

        if (useCache) {
            File cacheDir = new File("target/cache/" + baseFilesClassPath + "/" + githubApi.getMethodName());
            cacheDir.mkdirs();
            FileUtils.cleanDirectory(cacheDir);
            Cache cache = new Cache(cacheDir, 100 * 1024L * 1024L);

            client.setCache(cache);
        }

        return client;
    }


    /**
     * This is a standard set of actions to be performed with each connector
     * @throws Exception
     */
    private void  doTestActions() throws Exception {
        String name = githubApi.getMethodName();

        GHRepository repo = getRepository(gitHub);

        // Testing behavior when nothing has changed.
        pollForChange("Resetting");
        assertThat(getRepository(gitHub).getDescription(), is("Resetting"));

        repo.setDescription(name);

        pollForChange(name);

        // Test behavior after change
        assertThat(getRepository(gitHub).getDescription(), is(name));


        // Get Tricky - make a change via a different client
        if (githubApi.isUseProxy()) {
            GHRepository altRepo = getRepository(gitHubBeforeAfter);
            altRepo.setDescription("Tricky");
        }

        // Testing behavior after change
        pollForChange("Tricky");
    }

    private void pollForChange(String name) throws IOException, InterruptedException {
        getRepository(gitHub).getDescription();
        Thread.sleep(500);
        getRepository(gitHub).getDescription();
        Thread.sleep(1000);
        getRepository(gitHub).getDescription();
        Thread.sleep(4000);
    }

    private static GHRepository getRepository(GitHub gitHub) throws IOException {
        return gitHub.getOrganization("github-api-test-org").getRepository("github-api");
    }

}
