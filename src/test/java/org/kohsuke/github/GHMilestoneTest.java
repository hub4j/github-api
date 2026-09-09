package org.kohsuke.github;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.time.Instant;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import static org.hamcrest.Matchers.*;

// TODO: Auto-generated Javadoc
/**
 * The Class GHMilestoneTest.
 *
 * @author Martin van Zijl
 */
public class GHMilestoneTest extends AbstractGitHubWireMockTest {

    private static List<String> filterTestMilestones(List<GHMilestone> milestones) {
        return milestones.stream()
                .map(GHMilestone::getTitle)
                .filter(t -> t.startsWith("Milestone Sort "))
                .collect(Collectors.toList());
    }

    /**
     * Create default GHMilestoneTest instance
     */
    public GHMilestoneTest() {
    }

    /**
     * Clean up.
     *
     * @throws Exception
     *             the exception
     */
    @Before
    @After
    public void cleanUp() throws Exception {
        // Cleanup is only needed when proxying
        if (!mockGitHub.isUseProxy()) {
            return;
        }

        GHRepository repo = getRepository(getNonRecordingGitHub());

        for (GHIssue issue : repo.queryIssues().state(GHIssueState.ALL).list()) {
            if (issue.getTitle().endsWith("for sort test") || issue.getTitle().equals("Issue for testUnsetMilestone")) {
                issue.close();
            }
        }

        for (GHMilestone milestone : repo.listMilestones(GHIssueState.ALL)) {
            if ("Original Title".equals(milestone.getTitle()) || "Updated Title".equals(milestone.getTitle())
                    || "Unset Test Milestone".equals(milestone.getTitle())
                    || "Milestone Sort A".equals(milestone.getTitle())
                    || "Milestone Sort B".equals(milestone.getTitle())) {
                milestone.delete();
            }
        }
    }

    /**
     * Test list milestones with sort and direction.
     *
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     */
    @Test
    public void testListMilestonesWithSort() throws IOException {
        GHRepository repo = getRepository();
        GHMilestone milestoneA = repo.createMilestone("Milestone Sort A", "First milestone");
        milestoneA.setDueOn(GitHubClient.parseInstant("2025-06-01T00:00:00Z"));
        GHMilestone milestoneB = repo.createMilestone("Milestone Sort B", "Second milestone");
        milestoneB.setDueOn(GitHubClient.parseInstant("2025-12-01T00:00:00Z"));

        // List with due_on sort ascending (default)
        List<String> ascending = filterTestMilestones(repo.queryMilestones()
                .state(GHIssueState.OPEN)
                .sort(GHMilestoneQueryBuilder.Sort.DUE_ON)
                .direction(GHDirection.ASC)
                .list()
                .toList());
        assertThat(ascending, contains("Milestone Sort A", "Milestone Sort B"));

        // List with due_on sort descending
        List<String> descending = filterTestMilestones(repo.queryMilestones()
                .state(GHIssueState.OPEN)
                .sort(GHMilestoneQueryBuilder.Sort.DUE_ON)
                .direction(GHDirection.DESC)
                .list()
                .toList());
        assertThat(descending, contains("Milestone Sort B", "Milestone Sort A"));

        // Create issues to test completeness sort
        // Milestone A: 1 open, 1 closed = 50% complete
        GHIssue issueA1 = repo.createIssue("Issue A1 for sort test").milestone(milestoneA).create();
        GHIssue issueA2 = repo.createIssue("Issue A2 for sort test").milestone(milestoneA).create();
        issueA2.close();

        // Milestone B: 1 open, 0 closed = 0% complete
        GHIssue issueB1 = repo.createIssue("Issue B1 for sort test").milestone(milestoneB).create();

        // List with completeness sort ascending (least complete first)
        List<String> byCompleteness = filterTestMilestones(repo.queryMilestones()
                .state(GHIssueState.OPEN)
                .sort(GHMilestoneQueryBuilder.Sort.COMPLETENESS)
                .direction(GHDirection.ASC)
                .list()
                .toList());
        assertThat(byCompleteness, contains("Milestone Sort B", "Milestone Sort A"));

        // List with completeness sort descending (most complete first)
        List<String> byCompletenessDesc = filterTestMilestones(repo.queryMilestones()
                .state(GHIssueState.OPEN)
                .sort(GHMilestoneQueryBuilder.Sort.COMPLETENESS)
                .direction(GHDirection.DESC)
                .list()
                .toList());
        assertThat(byCompletenessDesc, contains("Milestone Sort A", "Milestone Sort B"));
    }

    /**
     * Test unset milestone.
     *
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     */
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

    /**
     * Test unset milestone from pull request.
     *
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     */
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

    /**
     * Test update milestone.
     *
     * @throws Exception
     *             the exception
     */
    @Test
    public void testUpdateMilestone() throws Exception {
        GHRepository repo = getRepository();
        GHMilestone milestone = repo.createMilestone("Original Title", "To test the update methods");

        String NEW_TITLE = "Updated Title";
        String NEW_DESCRIPTION = "Updated Description";
        Date NEW_DUE_DATE = Date.from(GitHubClient.parseInstant("2020-10-05T13:00:00Z"));
        Instant OUTPUT_DUE_DATE = GitHubClient.parseInstant("2020-10-05T07:00:00Z");

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
        assertThat(milestone.getClosedAt(), nullValue());
        assertThat(milestone.getHtmlUrl().toString(), containsString("/hub4j-test-org/github-api/milestone/"));
        assertThat(milestone.getUrl().toString(), containsString("/repos/hub4j-test-org/github-api/milestones/"));
        assertThat(milestone.getClosedIssues(), equalTo(0));
        assertThat(milestone.getOpenIssues(), equalTo(0));
    }

    private GHRepository getRepository(GitHub gitHub) throws IOException {
        return gitHub.getOrganization("hub4j-test-org").getRepository("github-api");
    }

    /**
     * Gets the repository.
     *
     * @return the repository
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     */
    protected GHRepository getRepository() throws IOException {
        return getRepository(gitHub);
    }
}
