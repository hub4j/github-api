package org.kohsuke.github;

import org.apache.commons.lang3.SystemUtils;
import org.junit.Assume;
import org.junit.Test;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;

import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.core.Is.is;

public class LifecycleTest extends AbstractGitHubWireMockTest {
    @Test
    public void testCreateRepository() throws IOException {
        Assume.assumeFalse(SystemUtils.IS_OS_WINDOWS);

        GHMyself myself = gitHub.getMyself();
        // GHOrganization org = gitHub.getOrganization(GITHUB_API_TEST_ORG);

        GHRepository repository = getTempRepository();
        assertTrue(repository.getReleases().isEmpty());

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
        assertEquals("test label", release.getAssets().get(0).getLabel());
    }

    private void deleteAsset(GHRelease release, GHAsset asset) throws IOException {
        asset.delete();
        assertEquals(0, release.getAssets().size());
    }

    private GHAsset uploadAsset(GHRelease release) throws IOException {
        GHAsset asset = release.uploadAsset(new File("LICENSE.txt"), "application/text");
        assertNotNull(asset);
        List<GHAsset> cachedAssets = release.assets();
        assertEquals(0, cachedAssets.size());
        List<GHAsset> assets = release.getAssets();
        assertEquals(1, assets.size());
        assertEquals("LICENSE.txt", assets.get(0).getName());

        return asset;
    }

    private GHRelease createRelease(GHRepository repository) throws IOException {
        GHRelease builder = repository.createRelease("release_tag")
                .name("Test Release")
                .body("How exciting!  To be able to programmatically create releases is a dream come true!")
                .create();
        List<GHRelease> releases = repository.getReleases();
        assertEquals(1, releases.size());
        GHRelease release = releases.get(0);
        assertEquals("Test Release", release.getName());
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
