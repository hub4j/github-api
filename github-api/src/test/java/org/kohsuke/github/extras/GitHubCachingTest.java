package org.kohsuke.github.extras;

import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import com.squareup.okhttp.Cache;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.OkUrlFactory;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.SystemUtils;
import org.junit.Assume;
import org.junit.Before;
import org.junit.Test;
import org.kohsuke.github.AbstractGitHubWireMockTest;
import org.kohsuke.github.GHFileNotFoundException;
import org.kohsuke.github.GHIssueState;
import org.kohsuke.github.GHPullRequest;
import org.kohsuke.github.GHRef;
import org.kohsuke.github.GHRepository;
import org.kohsuke.github.GitHub;

import java.io.File;
import java.io.IOException;

import static org.junit.Assert.fail;

// TODO: Auto-generated Javadoc
/**
 * Test showing the behavior of OkHttpGitHubConnector cache with GitHub 404 responses.
 *
 * @author Liam Newman
 */
public class GitHubCachingTest extends AbstractGitHubWireMockTest {

    /**
     * Instantiates a new git hub caching test.
     */
    public GitHubCachingTest() {
        useDefaultGitHub = false;
    }

    /** The test ref name. */
    String testRefName = "heads/test/content_ref_cache";

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
     * Setup repo.
     *
     * @throws Exception
     *             the exception
     */
    @Before
    public void setupRepo() throws Exception {
        if (mockGitHub.isUseProxy()) {
            for (GHPullRequest pr : getRepository(this.getNonRecordingGitHub()).getPullRequests(GHIssueState.OPEN)) {
                pr.close();
            }
            try {
                GHRef ref = getRepository(this.getNonRecordingGitHub()).getRef(testRefName);
                ref.delete();
            } catch (IOException e) {
            }
        }
    }

    /**
     * Test cached 404.
     *
     * @throws Exception
     *             the exception
     */
    @Test
    public void testCached404() throws Exception {
        Assume.assumeFalse(SystemUtils.IS_OS_WINDOWS);

        // ISSUE #669
        snapshotNotAllowed();

        OkHttpClient client = createClient(true);
        OkHttpConnector connector = new OkHttpConnector(new OkUrlFactory(client));

        this.gitHub = getGitHubBuilder().withEndpoint(mockGitHub.apiServer().baseUrl())
                .withConnector(connector)
                .build();

        // Alternate client also doing caching but staying in a good state
        // We use this to do sanity checks and other information gathering
        GitHub gitHub2 = getGitHubBuilder().withEndpoint(mockGitHub.apiServer().baseUrl())
                .withConnector(new OkHttpConnector(new OkUrlFactory(createClient(true))))
                .build();

        // Create a branch from a known conflicting branch
        GHRepository repo = getRepository(gitHub);

        String baseSha = repo.getRef("heads/test/unmergeable").getObject().getSha();

        GHRef ref;
        ref = repo.createRef("refs/" + testRefName, baseSha);

        // Verify we can query the created ref
        ref = repo.getRef(testRefName);

        // Verify we can query the created ref from cache
        ref = repo.getRef(testRefName);

        // Delete the ref
        ref.delete();

        // This is just to show this isn't a race condition
        Thread.sleep(2000);

        // Try to get the non-existant ref (GHFileNotFound)
        try {
            repo.getRef(testRefName);
            fail();
        } catch (GHFileNotFoundException e) {
            // expected

            // FYI: Querying again when the item is actually not present does not produce a 304
            // It produces another 404,
            // Try to get the non-existant ref (GHFileNotFound)
            try {
                repo.getRef(testRefName);
                fail();
            } catch (GHFileNotFoundException ex) {
                // expected
            }

        }

        // This is just to show this isn't a race condition
        Thread.sleep(2000);

        ref = repo.createRef("refs/" + testRefName, baseSha);

        // Verify ref exists and can be queried from uncached connection
        // Expected: success
        // Actual: still GHFileNotFound due to caching: GitHub incorrectly returns 304
        // even though contents of the ref have changed.
        //
        // There source of this issue seems to be that 404's do not return an ETAG,
        // so the cache falls back to using "If-Modified-Since" which is erroneously returns a 304.
        //
        // NOTE: This is even worse than you might think: 404 responses don't return an ETAG, but 304 responses do.
        //
        // Due erroneous 304 returned from "If-Modified-Since", the ETAG returned by the first 304
        // is actually the ETAG for the NEW state of the ref query (the one where the ref exists).
        // This can be verified by comparing the ETAG from gitHub2 client to the ETAG in error.
        //
        // This means that server thinks it telling the client that the new state is stable
        // while the cache thinks it confirming the old state hasn't changed.
        //
        // So, after the first 304, the failure is locked in via ETAG and won't until the ref is modified again
        // or until the cache ages out entry without the URL being requeried (which is why users report that refreshing
        // is now help).

        try {
            repo.getRef(testRefName);
        } catch (GHFileNotFoundException e) {
            // Sanity check: ref exists and can be queried from other client
            getRepository(gitHub2).getRef(testRefName);

            // We're going to fail, query again to see the incorrect ETAG cached from first query being used
            // It is the same ETAG as the one returned to the second client.
            // Now we're in trouble.
            repo.getRef(testRefName);

            // We should never fail the first query and pass the second,
            // the test has still failed if it get here.
            fail();
        }

        // OMG, the workaround succeeded!
        // This correct response should be generated from a 304.
        repo.getRef(testRefName);
    }

    private static int clientCount = 0;

    private OkHttpClient createClient(boolean useCache) throws IOException {
        OkHttpClient client = new OkHttpClient();

        if (useCache) {
            File cacheDir = new File(
                    "target/cache/" + baseFilesClassPath + "/" + mockGitHub.getMethodName() + clientCount++);
            cacheDir.mkdirs();
            FileUtils.cleanDirectory(cacheDir);
            Cache cache = new Cache(cacheDir, 100 * 1024L * 1024L);

            client.setCache(cache);
        }

        return client;
    }

    private static GHRepository getRepository(GitHub gitHub) throws IOException {
        return gitHub.getOrganization("hub4j-test-org").getRepository("github-api");
    }

}
