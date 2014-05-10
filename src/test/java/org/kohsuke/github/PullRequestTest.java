package org.kohsuke.github;

import org.junit.Test;

/**
 * @author Kohsuke Kawaguchi
 */
public class PullRequestTest extends AbstractGitHubApiTestBase {
    @Test
    public void createPullRequest() throws Exception {
        GHRepository j = gitHub.getOrganization("github-api-test-org").getRepository("jenkins");
        String name = rnd.next();
        GHPullRequest p = j.createPullRequest(name, "stable", "master", "## test");
        System.out.println(p.getUrl());
        assertEquals(name, p.getTitle());
        p.close();
    }
}
