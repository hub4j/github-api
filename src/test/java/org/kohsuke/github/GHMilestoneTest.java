package org.kohsuke.github;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.util.Date;

/**
 * @author Martin van Zijl
 */
public class GHMilestoneTest extends AbstractGitHubWireMockTest {

    @Before
    @After
    public void cleanUp() throws Exception {
        // Cleanup is only needed when proxying
        if (!mockGitHub.isUseProxy()) {
            return;
        }

        for (GHMilestone milestone : getRepository(gitHubBeforeAfter).listMilestones(GHIssueState.ALL)) {
            if ("Original Title".equals(milestone.getTitle()) ||
                "Updated Title".equals(milestone.getTitle())) {
                milestone.delete();
            }
        }
    }

    @Test
    public void testUpdateMilestone() throws Exception {
        GHRepository repo = getRepository();
        GHMilestone milestone = repo.createMilestone("Original Title",
                "To test the update methods");

        String NEW_TITLE = "Updated Title";
        String NEW_DESCRIPTION = "Updated Description";
        Date NEW_DUE_DATE = GitHub.parseDate("2020-10-05T13:00:00Z");
        Date OUTPUT_DUE_DATE = GitHub.parseDate("2020-10-05T07:00:00Z");

        milestone.setTitle(NEW_TITLE);
        milestone.setDescription(NEW_DESCRIPTION);
        milestone.setDueOn(NEW_DUE_DATE);

        // Force reload.
        milestone = repo.getMilestone(milestone.getNumber());

        assertEquals(NEW_TITLE, milestone.getTitle());
        assertEquals(NEW_DESCRIPTION, milestone.getDescription());

        // The time is truncated when sent to the server, but still part of the returned value
        // 07:00 midnight PDT
        assertEquals(OUTPUT_DUE_DATE, milestone.getDueOn());
    }

    protected GHRepository getRepository() throws IOException {
        return getRepository(gitHub);
    }

    private GHRepository getRepository(GitHub gitHub) throws IOException {
        return gitHub.getOrganization("github-api-test-org").getRepository("github-api");
    }
}
