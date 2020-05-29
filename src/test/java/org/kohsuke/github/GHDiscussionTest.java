package org.kohsuke.github;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class GHDiscussionTest extends AbstractGitHubWireMockTest {

    private GHDiscussion discussion;

    @Before
    public void setUp() throws Exception {
        discussion = gitHub.getOrganization(GITHUB_API_TEST_ORG)
                .getTeamBySlug("dummy-team")
                .createDiscussion("Dummy")
                .body("This is a dummy discussion")
                .create();
    }

    @Test
    public void testCreatedDiscussion() {
        Assert.assertNotNull(discussion);
        Assert.assertEquals("Dummy", discussion.getTitle());
        Assert.assertEquals("This is a dummy discussion", discussion.getBody());
    }

}
