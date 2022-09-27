package org.kohsuke.github;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.FileNotFoundException;
import java.io.IOException;

import static org.hamcrest.Matchers.*;

// TODO: Auto-generated Javadoc
/**
 * The Class GHProjectCardTest.
 *
 * @author Gunnar Skjold
 */
public class GHProjectCardTest extends AbstractGitHubWireMockTest {
    private GHOrganization org;
    private GHProject project;
    private GHProjectColumn column;
    private GHProjectCard card;

    /**
     * Sets the up.
     *
     * @throws Exception the exception
     */
    @Before
    public void setUp() throws Exception {
        org = gitHub.getOrganization(GITHUB_API_TEST_ORG);
        project = org.createProject("test-project", "This is a test project");
        column = project.createColumn("column-one");
        card = column.createCard("This is a card");
    }

    /**
     * Test created card.
     */
    @Test
    public void testCreatedCard() {
        assertThat(card.getNote(), equalTo("This is a card"));
        assertThat(card.isArchived(), is(false));
    }

    /**
     * Test edit card note.
     *
     * @throws IOException Signals that an I/O exception has occurred.
     */
    @Test
    public void testEditCardNote() throws IOException {
        card.setNote("New note");
        card = gitHub.getProjectCard(card.getId());
        assertThat(card.getNote(), equalTo("New note"));
        assertThat(card.isArchived(), is(false));
    }

    /**
     * Test archive card.
     *
     * @throws IOException Signals that an I/O exception has occurred.
     */
    @Test
    public void testArchiveCard() throws IOException {
        card.setArchived(true);
        card = gitHub.getProjectCard(card.getId());
        assertThat(card.getNote(), equalTo("This is a card"));
        assertThat(card.isArchived(), is(true));
    }

    /**
     * Test create card from issue.
     *
     * @throws IOException Signals that an I/O exception has occurred.
     */
    @Test
    public void testCreateCardFromIssue() throws IOException {
        GHRepository repo = org.createRepository("repo-for-project-card").create();
        try {
            GHIssue issue = repo.createIssue("new-issue").body("With body").create();
            GHProjectCard card = column.createCard(issue);
            assertThat(card.getContentUrl(), equalTo(issue.getUrl()));
            assertThat(card.getContent().getUrl(), equalTo(issue.getUrl()));
            assertThat(card.getContent().getRepository().getUrl(), equalTo(repo.getUrl()));
        } finally {
            repo.delete();
        }
    }

    /**
     * Test create card from PR.
     *
     * @throws IOException Signals that an I/O exception has occurred.
     */
    @Test
    public void testCreateCardFromPR() throws IOException {
        GHRepository repo = org.createRepository("repo-for-project-card").autoInit(true).create();

        try {
            String mainHead = repo.getRef("heads/main").getObject().getSha();
            String branchName1 = "refs/heads/branch1";
            repo.createRef(branchName1, mainHead);
            repo.createContent()
                    .content(branchName1)
                    .message(branchName1)
                    .path(branchName1)
                    .branch(branchName1)
                    .commit();
            GHPullRequest pr = repo.createPullRequest("new-PR", branchName1, "refs/heads/main", "Body Text");
            GHProjectCard card = column.createCard(pr);
            // For some reason, PRs are still treated as issues in project card urls
            assertThat(card.getContentUrl().toString(), equalTo(pr.getUrl().toString().replace("/pulls/", "/issues/")));
            assertThat(card.getContent().getUrl().toString(),
                    equalTo(pr.getUrl().toString().replace("/pulls/", "/issues/")));
            assertThat(card.getContent().getRepository().getUrl(), equalTo(repo.getUrl()));
        } finally {
            repo.delete();
        }
    }

    /**
     * Test delete card.
     *
     * @throws IOException Signals that an I/O exception has occurred.
     */
    @Test
    public void testDeleteCard() throws IOException {
        card.delete();
        try {
            card = gitHub.getProjectCard(card.getId());
            assertThat(card, nullValue());
        } catch (FileNotFoundException e) {
            card = null;
        }
    }

    /**
     * After.
     *
     * @throws IOException Signals that an I/O exception has occurred.
     */
    @After
    public void after() throws IOException {
        if (mockGitHub.isUseProxy()) {
            if (card != null) {
                card = getNonRecordingGitHub().getProjectCard(card.getId());
                try {
                    card.delete();
                    card = null;
                } catch (FileNotFoundException e) {
                    card = null;
                }
            }
            if (column != null) {
                column = getNonRecordingGitHub().getProjectColumn(column.getId());
                try {
                    column.delete();
                    column = null;
                } catch (FileNotFoundException e) {
                    column = null;
                }
            }
            if (project != null) {
                project = getNonRecordingGitHub().getProject(project.getId());
                try {
                    project.delete();
                    project = null;
                } catch (FileNotFoundException e) {
                    project = null;
                }
            }
        }
    }
}
