package org.kohsuke.github;

import org.junit.After;
import org.junit.Test;

import java.io.IOException;
import java.util.Collection;

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

    @Test // Requires push access to the test repo to pass
    public void setLabels() throws Exception {
        GHPullRequest p = getRepository().createPullRequest(rnd.next(), "stable", "master", "## test");
        String label = rnd.next();
        p.setLabels(label);

        Collection<GHIssue.Label> labels = getRepository().getPullRequest(p.getNumber()).getLabels();
        assertEquals(1, labels.size());
        assertEquals(label, labels.iterator().next().getName());
    }

    @Test // Requires push access to the test repo to pass
    public void setAssignee() throws Exception {
        GHPullRequest p = getRepository().createPullRequest(rnd.next(), "stable", "master", "## test");
        GHMyself user = gitHub.getMyself();
        p.assignTo(user);

        assertEquals(user, getRepository().getPullRequest(p.getNumber()).getAssignee());
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
