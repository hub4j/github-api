package org.kohsuke.github;

import org.junit.Test;
import org.kohsuke.github.GHReleaseBuilder.MakeLatest;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThrows;

// TODO: Auto-generated Javadoc
/**
 * The Class GHReleaseTest.
 */
public class GHReleaseTest extends AbstractGitHubWireMockTest {

    /**
     * Test create simple release.
     *
     * @throws Exception
     *             the exception
     */
    @Test
    public void testCreateSimpleRelease() throws Exception {
        GHRepository repo = gitHub.getRepository("hub4j-test-org/testCreateRelease");

        String tagName = mockGitHub.getMethodName();
        GHRelease release = repo.createRelease(tagName).categoryName("announcements").prerelease(false).create();
        try {
            GHRelease releaseCheck = repo.getRelease(release.getId());

            assertThat(releaseCheck, notNullValue());
            assertThat(releaseCheck.getTagName(), is(tagName));
            assertThat(releaseCheck.isPrerelease(), is(false));
            assertThat(releaseCheck.getDiscussionUrl(), notNullValue());
        } finally {
            release.delete();
            assertThat(repo.getRelease(release.getId()), nullValue());
        }
    }

    /**
     * Test create simple release without discussion.
     *
     * @throws Exception
     *             the exception
     */
    @Test
    public void testCreateSimpleReleaseWithoutDiscussion() throws Exception {
        GHRepository repo = gitHub.getRepository("hub4j-test-org/testCreateRelease");

        String tagName = mockGitHub.getMethodName();
        GHRelease release = repo.createRelease(tagName).create();

        try {
            GHRelease releaseCheck = repo.getRelease(release.getId());

            assertThat(releaseCheck, notNullValue());
            assertThat(releaseCheck.getTagName(), is(tagName));
            assertThat(releaseCheck.getDiscussionUrl(), nullValue());
        } finally {
            release.delete();
            assertThat(repo.getRelease(release.getId()), nullValue());
        }
    }

    /**
     * Test create double release fails.
     *
     * @throws Exception
     *             the exception
     */
    @Test
    public void testCreateDoubleReleaseFails() throws Exception {
        GHRepository repo = gitHub.getRepository("hub4j-test-org/testCreateRelease");

        String tagName = mockGitHub.getMethodName();

        GHRelease release = repo.createRelease(tagName).create();

        try {
            GHRelease releaseCheck = repo.getRelease(release.getId());
            assertThat(releaseCheck, notNullValue());

            HttpException httpException = assertThrows(HttpException.class, () -> {
                repo.createRelease(tagName).create();
            });

            assertThat(httpException.getResponseCode(), is(422));
        } finally {
            release.delete();
            assertThat(repo.getRelease(release.getId()), nullValue());
        }
    }

    /**
     * Test create release with unknown category fails.
     *
     * @throws Exception
     *             the exception
     */
    @Test
    public void testCreateReleaseWithUnknownCategoryFails() throws Exception {
        GHRepository repo = gitHub.getRepository("hub4j-test-org/testCreateRelease");

        String tagName = mockGitHub.getMethodName();
        String releaseName = "release-" + tagName;

        assertThrows(GHFileNotFoundException.class, () -> {
            repo.createRelease(tagName)
                    .name(releaseName)
                    .categoryName("an invalid cateogry")
                    .prerelease(false)
                    .create();
        });
    }

    /**
     * Test update release.
     *
     * @throws Exception
     *             the exception
     */
    @Test
    public void testUpdateRelease() throws Exception {
        GHRepository repo = gitHub.getRepository("hub4j-test-org/testCreateRelease");

        String tagName = mockGitHub.getMethodName();
        GHRelease release = repo.createRelease(tagName).prerelease(true).create();
        try {
            GHRelease releaseCheck = repo.getRelease(release.getId());
            GHRelease updateCheck = releaseCheck.update().categoryName("announcements").prerelease(false).update();

            assertThat(releaseCheck, notNullValue());
            assertThat(releaseCheck.getTagName(), is(tagName));
            assertThat(releaseCheck.isPrerelease(), is(true));
            assertThat(releaseCheck.getDiscussionUrl(), nullValue());

            assertThat(updateCheck, notNullValue());
            assertThat(updateCheck.getTagName(), is(tagName));
            assertThat(updateCheck.isPrerelease(), is(false));
            assertThat(updateCheck.getDiscussionUrl(), notNullValue());

        } finally {
            release.delete();
            assertThat(repo.getRelease(release.getId()), nullValue());
        }
    }

    /**
     * Test delete release.
     *
     * @throws Exception
     *             the exception
     */
    @Test
    public void testDeleteRelease() throws Exception {
        GHRepository repo = gitHub.getRepository("hub4j-test-org/testCreateRelease");

        String tagName = mockGitHub.getMethodName();
        GHRelease release = repo.createRelease(tagName).categoryName("announcements").prerelease(true).create();

        assertThat(repo.getRelease(release.getId()), notNullValue());
        release.delete();
        assertThat(repo.getRelease(release.getId()), nullValue());

    }

    /**
     * Test making a release the latest
     *
     * @throws Exception
     *             the exception
     */
    @Test
    public void testMakeLatestRelease() throws Exception {
        GHRepository repo = getTempRepository();

        GHRelease release1 = repo.createRelease("tag1").create();
        GHRelease release2 = null;

        try {
            // Newly created release should also be the latest.
            GHRelease latestRelease = repo.getLatestRelease();
            assertThat(release1.getNodeId(), is(latestRelease.getNodeId()));

            // Create a second release, explicitly set it not to be latest, confirm it isn't
            release2 = repo.createRelease("tag2").makeLatest(MakeLatest.FALSE).create();
            latestRelease = repo.getLatestRelease();
            assertThat(release1.getNodeId(), is(latestRelease.getNodeId()));

            // Update the second release to be latest, confirm it is.
            release2.update().makeLatest(MakeLatest.TRUE).update();
            latestRelease = repo.getLatestRelease();
            assertThat(release2.getNodeId(), is(latestRelease.getNodeId()));
        } finally {
            release1.delete();
            if (release2 != null) {
                release2.delete();
            }
        }
    }

    /**
     * Tests creation of the release with `generate_release_notes` parameter on.
     *
     * @throws Exception
     *             if any failure has happened.
     */
    @Test
    public void testCreateReleaseWithNotes() throws Exception {
        GHRepository repo = gitHub.getRepository("hub4j-test-org/testCreateRelease");

        String tagName = mockGitHub.getMethodName();
        GHRelease release = new GHReleaseBuilder(repo, tagName).generateReleaseNotes(true).create();
        try {
            GHRelease releaseCheck = repo.getRelease(release.getId());

            assertThat(releaseCheck, notNullValue());
            assertThat(releaseCheck.getTagName(), is(tagName));
            assertThat(releaseCheck.isPrerelease(), is(false));
            assertThat(releaseCheck.getDiscussionUrl(), notNullValue());
        } finally {
            release.delete();
            assertThat(repo.getRelease(release.getId()), nullValue());
        }
    }
}
