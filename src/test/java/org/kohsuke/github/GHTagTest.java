package org.kohsuke.github;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

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
            GHRef ref = getRepository(this.getGitHubBeforeAfter()).getRef("tags/create_tag_test");
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
        assertEquals(tagName, tag.getTag());
        assertEquals(tagMessage, tag.getMessage());
        assertEquals(commitSha, tag.getObject().getSha());
        assertFalse(tag.getVerification().getVerified());
        assertEquals(tag.getVerification().getReason(), GHReason.unsigned);

        // Make a reference to the newly created tag.
        GHRef ref = repo.createRef("refs/tags/" + tagName, tag.getSha());
        assertNotNull(ref);
    }

    protected GHRepository getRepository() throws IOException {
        return getRepository(gitHub);
    }

    private GHRepository getRepository(GitHub gitHub) throws IOException {
        return gitHub.getOrganization("github-api-test-org").getRepository("github-api");
    }
}
