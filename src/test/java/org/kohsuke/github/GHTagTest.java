package org.kohsuke.github;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;

import static org.hamcrest.Matchers.*;

/**
 *
 * @author Martin van Zijl
 */
public class GHTagTest extends AbstractGitHubWireMockTest {

    @Before
    @After
    public void cleanUpTags() throws Exception {
        // Cleanup is only needed when proxying
        if (!mockGitHub.isUseProxy()) {
            return;
        }

        try {
            GHRef ref = getRepository(this.getNonRecordingGitHub()).getRef("tags/create_tag_test");
            if (ref != null) {
                ref.delete();
            }
        } catch (IOException e) {
            // The reference probably does not exist.
            // This is safe to ignore.
        }
    }

    @Test
    public void testCreateTag() throws Exception {
        GHRepository repo = getRepository();

        String commitSha = "dfe47235cfdcaa12292dab3b1a84ca53a1ceadaf";
        String tagName = "create_tag_test";
        String tagMessage = "Test Tag";
        String tagType = "commit";

        GHTagObject tag = repo.createTag(tagName, tagMessage, commitSha, tagType);
        assertThat(tag.getTag(), equalTo(tagName));
        assertThat(tag.getMessage(), equalTo(tagMessage));
        assertThat(tag.getObject().getSha(), equalTo(commitSha));
        assertThat(tag.getVerification().isVerified(), is(false));
        assertThat(GHVerification.Reason.UNSIGNED, equalTo(tag.getVerification().getReason()));
        assertThat(tag.getUrl(),
                containsString("/repos/hub4j-test-org/github-api/git/tags/e7aa6d4afbaa48669f0bbe11ca3c4d787b2b153c"));
        assertThat(tag.getOwner().getId(), equalTo(repo.getId()));
        assertThat(tag.getTagger().getEmail(), equalTo("martin.vanzijl@gmail.com"));

        // Make a reference to the newly created tag.
        GHRef ref = repo.createRef("refs/tags/" + tagName, tag.getSha());
        assertThat(ref, notNullValue());
    }

    protected GHRepository getRepository() throws IOException {
        return getRepository(gitHub);
    }

    private GHRepository getRepository(GitHub gitHub) throws IOException {
        return gitHub.getOrganization("hub4j-test-org").getRepository("github-api");
    }
}
