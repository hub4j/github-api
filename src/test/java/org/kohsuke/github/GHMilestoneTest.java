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

        for (GHMilestone milestone : getRepository(getNonRecordingGitHub()).listMilestones(GHIssueState.ALL)) {
            if ("Original Title".equals(milestone.getTitle()) || "Updated Title".equals(milestone.getTitle())
                    || "Unset Test Milestone".equals(milestone.getTitle())) {
                milestone.delete();
            }
        }
    }

    @Test
    public void testUpdateMilestone() throws Exception {
        GHRepository repo = getRepository();
        GHMilestone milestone = repo.createMilestone("Original Title", "To test the update methods");

        String NEW_TITLE = "Updated Title";
        String NEW_DESCRIPTION = "Updated Description";
        Date NEW_DUE_DATE = GitHubClient.parseDate("2020-10-05T13:00:00Z");
        Date OUTPUT_DUE_DATE = GitHubClient.parseDate("2020-10-05T07:00:00Z");

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

    @Test
    public void testUnsetMilestone() throws IOException {
        GHRepository repo = getRepository();
        GHMilestone milestone = repo.createMilestone("Unset Test Milestone", "For testUnsetMilestone");
        GHIssue issue = repo.createIssue("Issue for testUnsetMilestone").create();

        // set the milestone
        issue.setMilestone(milestone);
        issue = repo.getIssue(issue.getNumber()); // force reload
        assertEquals(milestone.getNumber(), issue.getMilestone().getNumber());

        // remove the milestone
        issue.setMilestone(null);
        issue = repo.getIssue(issue.getNumber()); // force reload
        assertEquals(null, issue.getMilestone());
    }

    @Test
    public void testUnsetMilestoneFromPullRequest() throws IOException {
        GHRepository repo = getRepository();
        GHMilestone milestone = repo.createMilestone("Unset Test Milestone", "For testUnsetMilestone");
        GHPullRequest p = repo
                .createPullRequest("testUnsetMilestoneFromPullRequest", "test/stable", "main", "## test pull request");

        // set the milestone
        p.setMilestone(milestone);
        p = repo.getPullRequest(p.getNumber()); // force reload
        assertEquals(milestone.getNumber(), p.getMilestone().getNumber());

        // remove the milestone
        p.setMilestone(null);
        p = repo.getPullRequest(p.getNumber()); // force reload
        assertNull(p.getMilestone());
    }

    protected GHRepository getRepository() throws IOException {
        return getRepository(gitHub);
    }

    private GHRepository getRepository(GitHub gitHub) throws IOException {
        return gitHub.getOrganization("hub4j-test-org").getRepository("github-api");
    }
}
