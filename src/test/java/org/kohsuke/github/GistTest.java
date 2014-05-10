package org.kohsuke.github;

import org.junit.Test;

/**
 * @author Kohsuke Kawaguchi
 */
public class GistTest extends AbstractGitHubApiTestBase {
    /**
     * CRUD operation.
     */
    @Test
    public void lifecycleTest() throws Exception {
        GHGist gist = gitHub.createGist()
                .public_(false)
                .description("Test Gist")
                .file("abc.txt","abc")
                .file("def.txt","def")
                .create();

        assertNotNull(gist.getCreatedAt());
        assertNotNull(gist.getUpdatedAt());

        assertNotNull(gist.getCommentsUrl());
        assertNotNull(gist.getCommitsUrl());
        assertNotNull(gist.getGitPullUrl());
        assertNotNull(gist.getGitPushUrl());
        assertNotNull(gist.getHtmlUrl());

        gist.delete();
    }

    @Test
    public void starTest() throws Exception {
        GHGist gist = gitHub.getGist("9903708");
        assertEquals("rtyler",gist.getOwner().getLogin());

        gist.star();
        assertTrue(gist.isStarred());
        gist.unstar();
        assertFalse(gist.isStarred());

        GHGist newGist = gist.fork();

        try {
            for (GHGist g : gist.listForks()) {
                if (g.equals(newGist)) {
                    // expected to find it in the clone list
                    return;
                }
            }

            fail("Expected to find a newly cloned gist");
        } finally {
            newGist.delete();
        }
    }
}
