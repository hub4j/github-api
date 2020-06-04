package org.kohsuke.github;

import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;

public class GHDiscussionTest extends AbstractGitHubWireMockTest {

    @Test
    public void testCreatedDiscussion() throws IOException {
        GHTeam team = gitHub.getOrganization(GITHUB_API_TEST_ORG).getTeamBySlug("dummy-team");
        GHDiscussion discussion = team.createDiscussion("Dummy").body("This is a dummy discussion").create();
        Assert.assertNotNull(discussion);
        Assert.assertEquals("Dummy", discussion.getTitle());
        Assert.assertEquals("This is a dummy discussion", discussion.getBody());
    }

}