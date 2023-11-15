package org.kohsuke.github;

import org.apache.commons.lang3.SystemUtils;
import org.junit.Assume;
import org.junit.Test;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;

import static org.hamcrest.Matchers.*;

// TODO: Auto-generated Javadoc
/**
 * The Class LifecycleTest.
 */
public class LifecycleTest extends AbstractGitHubWireMockTest {

    /**
     * Test create repository.
     *
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     */
    @Test
    public void testCreateRepository() throws IOException {
        Assume.assumeFalse(SystemUtils.IS_OS_WINDOWS);

        GHMyself myself = gitHub.getMyself();
        // GHOrganization org = gitHub.getOrganization(GITHUB_API_TEST_ORG);

        GHRepository repository = getTempRepository();
        assertThat(repository.getReleases(), is(empty()));

        GHMilestone milestone = repository.createMilestone("Initial Release", "first one");
        GHIssue issue = repository.createIssue("Test Issue")
                .body("issue body just for grins")
                .milestone(milestone)
                .assignee(myself)
                .label("bug")
                .create();

        assertThat(issue, is(notNullValue()));

        GHRelease release = createRelease(repository);

        GHAsset asset = uploadAsset(release);

        updateAsset(release, asset);

        deleteAsset(release, asset);
    }

    private void updateAsset(GHRelease release, GHAsset asset) throws IOException {
        asset.setLabel("test label");
        assertThat(release.getAssets().get(0).getLabel(), equalTo("test label"));
    }

    private void deleteAsset(GHRelease release, GHAsset asset) throws IOException {
        asset.delete();
        assertThat(release.getAssets(), is(empty()));
    }

    private GHAsset uploadAsset(GHRelease release) throws IOException {
        GHAsset asset = release.uploadAsset(new File("../LICENSE.txt"), "application/text");
        assertThat(asset, notNullValue());
        List<GHAsset> cachedAssets = release.assets();
        assertThat(cachedAssets, is(empty()));
        List<GHAsset> assets = release.getAssets();
        assertThat(assets.size(), equalTo(1));
        assertThat(assets.get(0).getName(), equalTo("LICENSE.txt"));
        assertThat(assets.get(0).getSize(), equalTo(1104L));
        assertThat(assets.get(0).getContentType(), equalTo("application/text"));
        assertThat(assets.get(0).getState(), equalTo("uploaded"));
        assertThat(assets.get(0).getDownloadCount(), equalTo(0L));
        assertThat(assets.get(0).getOwner(), sameInstance(release.getOwner()));
        assertThat(assets.get(0).getBrowserDownloadUrl(),
                containsString("/temp-testCreateRepository/releases/download/release_tag/LICENSE.txt"));

        return asset;
    }

    private GHRelease createRelease(GHRepository repository) throws IOException {
        GHRelease builder = repository.createRelease("release_tag")
                .name("Test Release")
                .body("How exciting!  To be able to programmatically create releases is a dream come true!")
                .create();
        List<GHRelease> releases = repository.getReleases();
        assertThat(releases.size(), equalTo(1));
        GHRelease release = releases.get(0);
        assertThat(release.getName(), equalTo("Test Release"));
        assertThat(release.getBody(), startsWith("How exciting!"));
        assertThat(release.getOwner(), sameInstance(repository));
        assertThat(release.getZipballUrl(),
                endsWith("/repos/hub4j-test-org/temp-testCreateRepository/zipball/release_tag"));
        assertThat(release.getTarballUrl(),
                endsWith("/repos/hub4j-test-org/temp-testCreateRepository/tarball/release_tag"));
        assertThat(release.getTargetCommitish(), equalTo("main"));
        assertThat(release.getHtmlUrl().toString(),
                endsWith("/hub4j-test-org/temp-testCreateRepository/releases/tag/release_tag"));

        return release;
    }

    private void delete(File toDelete) {
        if (toDelete.isDirectory()) {
            for (File file : toDelete.listFiles()) {
                delete(file);
            }
        }
        toDelete.delete();
    }

    private File createDummyFile(File repoDir) throws IOException {
        File file = new File(repoDir, "testFile-" + System.currentTimeMillis());
        PrintWriter writer = new PrintWriter(new FileWriter(file));
        try {
            writer.println("test file");
        } finally {
            writer.close();
        }
        return file;
    }
}
