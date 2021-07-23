package org.kohsuke.github;

import org.junit.Test;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.Matchers.*;

/**
 * @author Martin van Zijl
 */
public class GHIssueEventTest extends AbstractGitHubWireMockTest {

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

    @Test
    public void testIssueReviewRequestedEvent() throws Exception {
        // Create the PR.
        final GHPullRequest pullRequest = getRepository()
                .createPullRequest("Test PR", "test/stable", "main", "## test");

        final ArrayList<GHUser> reviewers = new ArrayList<>();
        reviewers.add(gitHub.getUser("t0m4uk1991"));
        // Generate review_requested event.
        pullRequest.requestReviewers(reviewers);

        // Test that the event is present.
        final List<GHIssueEvent> list = pullRequest.listEvents().toList();
        assertThat(list.size(), equalTo(1));
        final GHIssueEvent event = list.get(0);
        assertThat(event.getEvent(), equalTo("review_requested"));
        assertThat(event.getReviewRequester(), notNullValue());
        assertThat(event.getReviewRequester().getLogin(), equalTo("t0m4uk1991"));

        // Close the PR.
        pullRequest.close();
    }

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

    @Test
    public void testRepositoryEvents() throws Exception {
        GHRepository repo = getRepository();
        List<GHIssueEvent> list = repo.listIssueEvents().toList();
        assertThat(list, is(not(empty())));

        int i = 0;
        for (GHIssueEvent event : list) {
            assertThat(event.getIssue(), notNullValue());
            if (i++ > 10)
                break;
        }
    }

    protected GHRepository getRepository() throws IOException {
        return getRepository(gitHub);
    }

    private GHRepository getRepository(GitHub gitHub) throws IOException {
        return gitHub.getOrganization("hub4j-test-org").getRepository("github-api");
    }
}
