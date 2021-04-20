package org.kohsuke.github;

import org.junit.Test;

import java.io.IOException;
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
    public void testRepositoryEvents() throws Exception {
        GHRepository repo = getRepository();
        List<GHIssueEvent> list = repo.listIssueEvents().toList();
        assertThat(list.size() > 0, is(true));

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
