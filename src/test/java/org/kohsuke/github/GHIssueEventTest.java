package org.kohsuke.github;

import org.junit.Test;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.Matchers.*;

// TODO: Auto-generated Javadoc
/**
 * The Class GHIssueEventTest.
 *
 * @author Martin van Zijl
 */
public class GHIssueEventTest extends AbstractGitHubWireMockTest {

    /**
     * Create default GHIssueEventTest instance
     */
    public GHIssueEventTest() {
    }

    /**
     * Test events for issue rename.
     *
     * @throws Exception
     *             the exception
     */
    @Test
    public void testEventsForIssueRename() throws Exception {
        // Create the issue.
        GHRepository repo = getRepository();
        GHIssueBuilder builder = repo.createIssue("Some invalid issue name");
        GHIssue issue = builder.create();

        // Generate rename event.
        issue.setTitle("Fixed issue name");

        // Test that the event is present.
        List<GHIssueEvent> list = issue.listEvents().toList();
        assertThat(list.size(), equalTo(1));

        GHIssueEvent event = list.get(0);
        assertThat(event.getIssue().getNumber(), equalTo(issue.getNumber()));
        assertThat(event.getEvent(), equalTo("renamed"));
        assertThat(event.getRename(), notNullValue());
        assertThat(event.getRename().getFrom(), equalTo("Some invalid issue name"));
        assertThat(event.getRename().getTo(), equalTo("Fixed issue name"));

        // Test that we can get a single event directly.
        GHIssueEvent eventFromRepo = repo.getIssueEvent(event.getId());
        assertThat(eventFromRepo.getId(), equalTo(event.getId()));
        assertThat(eventFromRepo.getCreatedAt(), equalTo(event.getCreatedAt()));
        assertThat(eventFromRepo.getEvent(), equalTo("renamed"));
        assertThat(eventFromRepo.getRename(), notNullValue());
        assertThat(eventFromRepo.getRename().getFrom(), equalTo("Some invalid issue name"));
        assertThat(eventFromRepo.getRename().getTo(), equalTo("Fixed issue name"));

        // Close the issue.
        issue.close();
    }

    /**
     * Test events for single issue.
     *
     * @throws Exception
     *             the exception
     */
    @Test
    public void testEventsForSingleIssue() throws Exception {
        // Create the issue.
        GHRepository repo = getRepository();
        GHIssueBuilder builder = repo.createIssue("Test from the API");
        GHIssue issue = builder.create();

        // Generate some events.
        issue.setLabels("test-label");

        // Test that the events are present.
        List<GHIssueEvent> list = issue.listEvents().toList();
        assertThat(list.size(), equalTo(1));

        GHIssueEvent event = list.get(0);
        assertThat(event.getIssue().getNumber(), equalTo(issue.getNumber()));
        assertThat(event.getEvent(), equalTo("labeled"));

        // Test that we can get a single event directly.
        GHIssueEvent eventFromRepo = repo.getIssueEvent(event.getId());
        assertThat(eventFromRepo.getId(), equalTo(event.getId()));
        assertThat(eventFromRepo.getCreatedAt(), equalTo(event.getCreatedAt()));

        // Close the issue.
        issue.close();
    }

    /**
     * Test issue review requested event.
     *
     * @throws Exception
     *             the exception
     */
    @Test
    public void testIssueReviewRequestedEvent() throws Exception {
        // Create the PR.
        final GHPullRequest pullRequest = getRepository()
                .createPullRequest("ReviewRequestedEventTest", "test/stable", "main", "## test");

        final ArrayList<GHUser> reviewers = new ArrayList<>();
        reviewers.add(gitHub.getUser("bitwiseman"));
        // Generate review_requested event.
        pullRequest.requestReviewers(reviewers);

        // Test that the event is present.
        final List<GHIssueEvent> list = pullRequest.listEvents().toList();
        assertThat(list.size(), equalTo(1));
        final GHIssueEvent event = list.get(0);
        assertThat(event.getEvent(), equalTo("review_requested"));
        assertThat(event.getReviewRequester(), notNullValue());
        assertThat(event.getReviewRequester().getLogin(), equalTo("t0m4uk1991"));
        assertThat(event.getRequestedReviewer(), notNullValue());
        assertThat(event.getRequestedReviewer().getLogin(), equalTo("bitwiseman"));
        assertThat(event.getNodeId(), equalTo("MDIwOlJldmlld1JlcXVlc3RlZEV2ZW50NTA2NDM2MzE3OQ=="));
        assertThat(event.getCommitId(), nullValue());
        assertThat(event.getCommitUrl(), nullValue());
        assertThat(event.toString(), equalTo("Issue 434 was review_requested by t0m4uk1991 on 2021-07-24T20:28:28Z"));

        // Close the PR.
        pullRequest.close();
    }

    /**
     * Test repository events.
     *
     * @throws Exception
     *             the exception
     */
    @Test
    public void testRepositoryEvents() throws Exception {
        GHRepository repo = getRepository();
        List<GHIssueEvent> list = repo.listIssueEvents().toList();
        assertThat(list, is(not(empty())));

        int i = 0;
        int successfulChecks = 0;
        for (GHIssueEvent event : list) {

            if ("merged".equals(event.getEvent())
                    && "ecec449372b1e8270524a35c1a5aa8fdaf0e6676".equals(event.getCommitId())) {
                assertThat(event.getCommitUrl(), endsWith("/ecec449372b1e8270524a35c1a5aa8fdaf0e6676"));
                assertThat(event.getActor().getLogin(), equalTo("bitwiseman"));
                assertThat(event.getActor().getLogin(), equalTo("bitwiseman"));
                assertThat(event.getIssue().getPullRequest().getUrl().toString(), endsWith("/github-api/pull/267"));
                successfulChecks++;
            }
            assertThat(event.getIssue(), notNullValue());
            if (i++ > 100)
                break;
        }
        assertThat("All issue checks must be found and passed", successfulChecks, equalTo(1));
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
