package org.kohsuke.github;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.util.Date;

import static org.hamcrest.Matchers.*;
import static org.hamcrest.Matchers.containsString;

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

        assertThat(milestone.getTitle(), equalTo(NEW_TITLE));
        assertThat(milestone.getDescription(), equalTo(NEW_DESCRIPTION));

        // The time is truncated when sent to the server, but still part of the returned value
        // 07:00 midnight PDT
        assertThat(milestone.getDueOn(), equalTo(OUTPUT_DUE_DATE));
        assertThat(milestone.getHtmlUrl().toString(), containsString("/hub4j-test-org/github-api/milestone/"));
        assertThat(milestone.getUrl().toString(), containsString("/repos/hub4j-test-org/github-api/milestones/"));
        assertThat(milestone.getClosedIssues(), equalTo(0));
        assertThat(milestone.getOpenIssues(), equalTo(0));
    }

    @Test
    public void testUnsetMilestone() throws IOException {
        GHRepository repo = getRepository();
        GHMilestone milestone = repo.createMilestone("Unset Test Milestone", "For testUnsetMilestone");
        GHIssue issue = repo.createIssue("Issue for testUnsetMilestone").create();

        // set the milestone
        issue.setMilestone(milestone);
        issue = repo.getIssue(issue.getNumber()); // force reload
        assertThat(issue.getMilestone().getNumber(), equalTo(milestone.getNumber()));

        // remove the milestone
        issue.setMilestone(null);
        issue = repo.getIssue(issue.getNumber()); // force reload
        assertThat(issue.getMilestone(), nullValue());
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
        assertThat(p.getMilestone().getNumber(), equalTo(milestone.getNumber()));

        // remove the milestone
        p.setMilestone(null);
        p = repo.getPullRequest(p.getNumber()); // force reload
        assertThat(p.getMilestone(), nullValue());
    }

    protected GHRepository getRepository() throws IOException {
        return getRepository(gitHub);
    }

    private GHRepository getRepository(GitHub gitHub) throws IOException {
        return gitHub.getOrganization("hub4j-test-org").getRepository("github-api");
    }
}
