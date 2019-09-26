package org.kohsuke.github;

import org.junit.Test;

/**
 * @author Martin van Zijl
 */
public class IssueEventsTest extends AbstractGitHubApiTestBase {
    public static String REPOSITORY = "martinvanzijl/sandbox";

    @Test
    public void testEventsForSingleIssue() throws Exception {
        GitHub github = GitHub.connect();
        GHRepository repo = github.getRepository(REPOSITORY);
        GHIssue issue = repo.getIssue(1);

        for(GHIssue.IssueEventInfo info: issue.listEvents()) {
            // TODO: Create toString() method from this.
            String line = String.format("Issue was %s by %s on %s",
                    info.getEvent(),
                    info.getActor().getLogin(),
                    info.getCreatedAt().toString());
            //System.out.println(line);
        }

        // TODO: Use the following...
        //GHIssueBuilder builder = repo.createIssue("test from the api");
        //GHIssue issue = builder.create();
    }

    @Test
    public void testRepositoryEvents() throws Exception {
        GitHub github = GitHub.connect();
        GHRepository repo = github.getRepository(REPOSITORY);
        PagedIterable<GHRepository.IssueEventInfo> list = repo.listIssueEvents();

        for(GHRepository.IssueEventInfo info: list) {
            String line = String.format("Issue %d was %s by %s on %s",
                    info.getIssue().getNumber(),
                    info.getEvent(),
                    info.getActor().getLogin(),
                    info.getCreatedAt().toString());
            //System.out.println(line);
        }
    }

    @Test
    public void testRepositorySingleEvent() throws Exception {
        GitHub github = GitHub.connect();
        GHRepository repo = github.getRepository(REPOSITORY);
        GHRepository.IssueEventInfo info = repo.getIssueEvent(1776389306);

        String line = String.format("Issue %d was %s by %s on %s",
                info.getIssue().getNumber(),
                info.getEvent(),
                info.getActor().getLogin(),
                info.getCreatedAt().toString());
        System.out.println(line);
    }
}
