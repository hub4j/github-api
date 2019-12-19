package org.kohsuke.github;

import org.junit.Test;

import java.io.IOException;
import java.util.List;

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
        issue.addLabels("test-label");

        // Test that the events are present.
        List<GHIssueEvent> list = issue.listEvents().asList();
        assertEquals(1, list.size());

        GHIssueEvent event = list.get(0);
        assertEquals(issue.getNumber(), event.getIssue().getNumber());
        assertEquals("labeled", event.getEvent());

        // Test that we can get a single event directly.
        GHIssueEvent eventFromRepo = repo.getIssueEvent(event.getId());
        assertEquals(event.getId(), eventFromRepo.getId());
        assertEquals(event.getCreatedAt(), eventFromRepo.getCreatedAt());

        // Close the issue.
        issue.close();
    }

    @Test
    public void testRepositoryEvents() throws Exception {
        GHRepository repo = getRepository();
        List<GHIssueEvent> list = repo.listIssueEvents().asList();
        assertTrue(list.size() > 0);

        int i = 0;
        for (GHIssueEvent event : list) {
            assertNotNull(event.getIssue());
            if (i++ > 10)
                break;
        }
    }

    protected GHRepository getRepository() throws IOException {
        return getRepository(gitHub);
    }

    private GHRepository getRepository(GitHub gitHub) throws IOException {
        return gitHub.getOrganization("github-api-test-org").getRepository("github-api");
    }
}
