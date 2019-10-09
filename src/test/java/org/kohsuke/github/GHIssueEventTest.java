package org.kohsuke.github;

import java.io.IOException;
import org.junit.Test;

/**
 * @author Martin van Zijl
 */
public class GHIssueEventTest extends AbstractGitHubApiTestBase {

    @Test
    public void testEventsForSingleIssue() throws Exception {
        GHRepository repo = getRepository();
        GHIssue issue = repo.getIssue(1);

        System.out.println("Single issue:");
        for (GHIssueEvent event : issue.listEvents()) {
            System.out.println(event);
        }

        //TODO: Use the following...
        //GHIssueBuilder builder = repo.createIssue("test from the api");
        //GHIssue issue = builder.create();
    }

    @Test
    public void testRepositoryEvents() throws Exception {
        GHRepository repo = getRepository();
        PagedIterable<GHIssueEvent> list = repo.listIssueEvents();

        System.out.println("Repository (all):");
        int i = 0;
        for (GHIssueEvent event : list.asList()) {
            System.out.println(event);
            if (i++ > 10) break;
        }
    }

    @Test
    public void testRepositorySingleEvent() throws Exception {
        GHRepository repo = getRepository();
        GHIssueEvent event = repo.getIssueEvent(2615868520L);

        System.out.println("Repository (single event):");
        System.out.println(event);
    }

    protected GHRepository getRepository() throws IOException {
        return getRepository(gitHub);
    }

    private GHRepository getRepository(GitHub gitHub) throws IOException {
        return gitHub.getOrganization("github-api-test-org").getRepository("github-api");
    }
}
