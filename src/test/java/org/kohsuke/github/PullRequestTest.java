package org.kohsuke.github;

import org.junit.After;
import org.junit.Test;

import java.io.IOException;
import java.util.Collection;
import java.util.List;

/**
 * @author Kohsuke Kawaguchi
 */
public class PullRequestTest extends AbstractGitHubApiTestBase {
    @Test
    public void createPullRequest() throws Exception {
        String name = rnd.next();
        GHPullRequest p = getRepository().createPullRequest(name, "stable", "master", "## test");
        System.out.println(p.getUrl());
        assertEquals(name, p.getTitle());
    }

    @Test
    public void createPullRequestComment() throws Exception {
        String name = rnd.next();
        GHPullRequest p = getRepository().createPullRequest(name, "stable", "master", "## test");
        p.comment("Some comment");
    }

    @Test
    public void testPullRequestReviewComments() throws Exception {
        String name = rnd.next();
        GHPullRequest p = getRepository().createPullRequest(name, "stable", "master", "## test");
        System.out.println(p.getUrl());
        assertTrue(p.listReviewComments().asList().isEmpty());
        p.createReviewComment("Sample review comment", p.getHead().getSha(), "cli/pom.xml", 5);
        List<GHPullRequestReviewComment> comments = p.listReviewComments().asList();
        assertEquals(1, comments.size());
        GHPullRequestReviewComment comment = comments.get(0);
        assertEquals("Sample review comment", comment.getBody());

        comment.update("Updated review comment");
        comments = p.listReviewComments().asList();
        assertEquals(1, comments.size());
        comment = comments.get(0);
        assertEquals("Updated review comment", comment.getBody());

        comment.delete();
        comments = p.listReviewComments().asList();
        assertTrue(comments.isEmpty());
    }

    @Test
    // Requires push access to the test repo to pass
    public void setLabels() throws Exception {
        GHPullRequest p = getRepository().createPullRequest(rnd.next(), "stable", "master", "## test");
        String label = rnd.next();
        p.setLabels(label);

        Collection<GHLabel> labels = getRepository().getPullRequest(p.getNumber()).getLabels();
        assertEquals(1, labels.size());
        assertEquals(label, labels.iterator().next().getName());
    }

    @Test
    // Requires push access to the test repo to pass
    public void setAssignee() throws Exception {
        GHPullRequest p = getRepository().createPullRequest(rnd.next(), "stable", "master", "## test");
        GHMyself user = gitHub.getMyself();
        p.assignTo(user);

        assertEquals(user, getRepository().getPullRequest(p.getNumber()).getAssignee());
    }

    @Test
    public void testGetUser() throws IOException {
        GHPullRequest p = getRepository().createPullRequest(rnd.next(), "stable", "master", "## test");
        GHPullRequest prSingle = getRepository().getPullRequest(p.getNumber());
        assertNotNull(prSingle.getUser().root);
        prSingle.getMergeable();
        assertNotNull(prSingle.getUser().root);

        PagedIterable<GHPullRequest> ghPullRequests = getRepository().listPullRequests(GHIssueState.OPEN);
        for (GHPullRequest pr : ghPullRequests) {
            assertNotNull(pr.getUser().root);
            pr.getMergeable();
            assertNotNull(pr.getUser().root);
        }
    }

    @After
    public void cleanUp() throws Exception {
        for (GHPullRequest pr : getRepository().getPullRequests(GHIssueState.OPEN)) {
            pr.close();
        }
    }

    private GHRepository getRepository() throws IOException {
        return gitHub.getOrganization("github-api-test-org").getRepository("jenkins");
    }
}
