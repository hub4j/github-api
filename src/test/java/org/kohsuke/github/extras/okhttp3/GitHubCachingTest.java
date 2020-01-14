package org.kohsuke.github.extras.okhttp3;

import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import com.github.tomakehurst.wiremock.extension.responsetemplating.ResponseTemplateTransformer;
import okhttp3.Cache;
import okhttp3.OkHttpClient;
import org.apache.commons.io.FileUtils;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.kohsuke.github.AbstractGitHubWireMockTest;
import org.kohsuke.github.GHContent;
import org.kohsuke.github.GHException;
import org.kohsuke.github.GHFileNotFoundException;
import org.kohsuke.github.GHIssueState;
import org.kohsuke.github.GHPullRequest;
import org.kohsuke.github.GHRef;
import org.kohsuke.github.GHRepository;
import org.kohsuke.github.GitHub;

import java.io.File;
import java.io.IOException;
import java.util.List;

import static org.hamcrest.core.Is.is;

/**
 * Test showing the behavior of OkHttpConnector cache with GitHub 404 responses.
 *
 * @author Liam Newman
 */
public class GitHubCachingTest extends AbstractGitHubWireMockTest {

    public GitHubCachingTest() {
        useDefaultGitHub = false;
    }

    String testRefName = "heads/test/content_ref_cache";

    @Override
    protected WireMockConfiguration getWireMockOptions() {
        return super.getWireMockOptions()
                .extensions(ResponseTemplateTransformer.builder().global(true).maxCacheEntries(0L).build());
    }

    @Before
    public void setupRepo() throws Exception {
        if (mockGitHub.isUseProxy()) {
            for (GHPullRequest pr : getRepository(this.gitHubBeforeAfter).getPullRequests(GHIssueState.OPEN)) {
                pr.close();
            }
            try {
                GHRef ref = getRepository(this.gitHubBeforeAfter).getRef(testRefName);
                ref.delete();
            } catch (IOException e) {
            }
        }
    }

    @Test
    public void OkHttpConnector_Cache_MaxAgeDefault_Zero_GitHubRef_Error_runnable() throws Exception {

        requireProxy("This test method can be run locally for debugging and analyzing.");
        OkHttpConnector_Cache_MaxAgeDefault_Zero_GitHubRef_Error();
    }

    @Ignore("The wiremock snapshot files attached to this test method show what was sent to and from the server during a run, but they aren't re-runnable - not templated.")
    @Test
    public void OkHttpConnector_Cache_MaxAgeDefault_Zero_GitHubRef_Error() throws Exception {

        // requireProxy("For clarity. Will switch to snapshot shortly.");
        // snapshotNotAllowed();

        OkHttpClient client = createClient(true);
        OkHttpConnector connector = new OkHttpConnector(client);

        this.gitHub = getGitHubBuilder().withEndpoint(mockGitHub.apiServer().baseUrl())
                .withConnector(connector)
                .build();

        // Alternate client also doing caching but staying in a good state
        // We use this to do sanity checks and other information gathering
        GitHub gitHub2 = getGitHubBuilder().withEndpoint(mockGitHub.apiServer().baseUrl())
                .withConnector(new OkHttpConnector(createClient(true)))
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

        // Work arounds:

        try {
            repo.getRef(testRefName);
        } catch (GHFileNotFoundException e) {
            // Sanity check: ref exists and can be queried from other client
            getRepository(gitHub2).getRef(testRefName);

            // We're going to fail, query again to see the incorrect ETAG cached from first query being used
            // It is the same ETAG as the one returned to the other client.
            // Now we're in trouble.
            repo.getRef(testRefName);

        }
    }


    @Ignore("Keeping for reference. Simpler repro above.")
    @Test
    public void OkHttpConnector_Cache_MaxAgeDefault_Zero_GitHubContents_Error() throws Exception {

        // requireProxy("For clarity. Will switch to snapshot shortly.");
        // snapshotNotAllowed();

        OkHttpClient client = createClient(true);
        OkHttpConnector connector = new OkHttpConnector(client);

        this.gitHub = getGitHubBuilder().withEndpoint(mockGitHub.apiServer().baseUrl())
                .withConnector(connector)
                .build();

        // Create a branch from a known conflicting branch
        GHRepository repo = getRepository(gitHub);

        // Try to get a non-existant ref (GHFileNotFound)
        try {
            repo.getRef("heads/test/content_ref_cache");
            fail();
        } catch (GHFileNotFoundException e) {
            // ignore
        } catch (GHException e) {
            // ignore
        }

        // Try to get the root directory contents for non-existant ref (GHFileNotFound)
        try {
            repo.getDirectoryContent("/", "refs/heads/test/content_ref_cache");
            fail();
        } catch (GHFileNotFoundException e) {
            // ignore
        } catch (GHException e) {
            // ignore
        }

        GHRef ref = repo.createRef("refs/heads/test/content_ref_cache",
                repo.getRef("heads/test/unmergeable").getObject().getSha());

        // Wait a little to make sure there's some time between queries
        Thread.sleep(5000);

        // Verify we can query the created ref
        repo.getRef("heads/test/content_ref_cache");

        // Sanity check: ref exists and can be queried from uncached connection
        // if (mockGitHub.isUseProxy()) {
        // getRepository(this.gitHubBeforeAfter).getDirectoryContent("/", ref.getRef());
        // }

        // Verify ref exists and can be queried from uncached connection
        // Expected: success
        // Actual: still GHFileNotFound due to caching: GitHub incorrectly returns 304
        // even though contents of the ref have changed.
        try {
            repo.getDirectoryContent("/", ref.getRef());
        } catch (GHFileNotFoundException e) {
            // Useful for breakpoint when debugging
            throw e;
        } catch (GHException e) {
            // Useful for breakpoint when debugging
            throw e;
        }

    }

    @Ignore("Keeping for reference. Simpler repro above.")
    @Test
    public void OkHttpConnector_Cache_MaxAgeDefault_Zero_PRGitHubContents_Error() throws Exception {

        OkHttpClient client = createClient(true);
        OkHttpConnector connector = new OkHttpConnector(client);

        this.gitHub = getGitHubBuilder().withEndpoint(mockGitHub.apiServer().baseUrl())
                .withConnector(connector)
                .build();

        if (mockGitHub.isUseProxy()) {
            for (GHPullRequest pr : getRepository(this.gitHubBeforeAfter).getPullRequests(GHIssueState.OPEN)) {
                pr.close();
            }
            try {
                GHRef ref = getRepository(this.gitHubBeforeAfter).getRef("heads/test/content_ref_cache");
                ref.delete();
            } catch (IOException e) {
            }
        }

        // Create a branch from a known conflicting branch
        GHRepository repo = getRepository(gitHub);

        GHRef ref = repo.createRef("refs/heads/test/content_ref_cache",
                repo.getRef("heads/test/unmergeable").getObject().getSha());

        // Ensure ref exists and can be queried
        repo.getDirectoryContent("/", ref.getRef());

        // Create a PR from the created (unmergeable) branch
        GHPullRequest pr = repo.createPullRequest("Title", ref.getRef(), "master", "");

        // Verify branch is unmergable state true
        while (pr.getMergeable() == null) {
            Thread.sleep(500);
        }
        assertThat(pr.getMergeable(), is(false));

        String mergeRefName = "pull/" + Integer.toString(pr.getNumber()) + "/merge";

        // Try to get the root directory contents for non-existant merge ref (GHFileNotFound)
        try {
            repo.getDirectoryContent("/", "refs/" + mergeRefName);
            fail();
        } catch (GHFileNotFoundException e) {
            // ignore
        } catch (GHException e) {
            // ignore
        }

        // Make PR mergeable
        ref.updateTo(repo.getRef("heads/test/mergeable_branch").getObject().getSha(), true);
        pr.refresh();
        // Verify mergable state true
        while (pr.getMergeable() == null) {
            Thread.sleep(500);
        }
        assertThat(pr.getMergeable(), is(true));

        // Verify we can get the root directory contents for merge ref
        // Expected: success
        // Actual: still GHFileNotFound due to caching - GitHub server error
        try {
            List<GHContent> files = repo.getDirectoryContent("/", "refs/" + mergeRefName);
        } catch (GHFileNotFoundException e) {
            // Useful for breakpoint when debugging
            throw e;
        } catch (GHException e) {
            // Useful for breakpoint when debugging
            throw e;
        }

    }

    private static int clientCount = 0;

    private OkHttpClient createClient(boolean useCache) throws IOException {
        OkHttpClient.Builder builder = new OkHttpClient().newBuilder();

        if (useCache) {
            File cacheDir = new File("target/cache/" + baseFilesClassPath + "/" + mockGitHub.getMethodName()
                    + Integer.toString(clientCount++));
            cacheDir.mkdirs();
            FileUtils.cleanDirectory(cacheDir);
            Cache cache = new Cache(cacheDir, 100 * 1024L * 1024L);

            builder.cache(cache);
        }

        return builder.build();
    }

    private static GHRepository getRepository(GitHub gitHub) throws IOException {
        return gitHub.getOrganization("github-api-test-org").getRepository("github-api");
    }

}
