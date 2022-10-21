package org.kohsuke.github;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.time.temporal.ChronoUnit;
import java.util.Collection;
import java.util.Date;
import java.util.List;

import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasProperty;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.assertNull;

// TODO: Auto-generated Javadoc
/**
 * The Class GHIssueTest.
 *
 * @author Kohsuke Kawaguchi
 * @author Yoann Rodiere
 */
public class GHIssueTest extends AbstractGitHubWireMockTest {

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

        for (GHIssue issue : getRepository(this.getNonRecordingGitHub()).getIssues(GHIssueState.OPEN)) {
            issue.close();
        }
    }

    /**
     * Creates the issue.
     *
     * @throws Exception
     *             the exception
     */
    @Test
    public void createIssue() throws Exception {
        String name = "createIssue";
        GHRepository repo = getRepository();
        GHIssue issue = repo.createIssue(name).body("## test").create();
        assertThat(issue.getTitle(), equalTo(name));
    }

    /**
     * Issue comment.
     *
     * @throws Exception
     *             the exception
     */
    @Test
    public void issueComment() throws Exception {
        String name = "createIssueComment";
        GHIssue issue = getRepository().createIssue(name).body("## test").create();

        List<GHIssueComment> comments;
        comments = issue.listComments().toList();
        assertThat(comments, hasSize(0));
        comments = issue.queryComments().list().toList();
        assertThat(comments, hasSize(0));

        GHIssueComment firstComment = issue.comment("First comment");
        Date firstCommentCreatedAt = firstComment.getCreatedAt();
        Date firstCommentCreatedAtPlus1Second = Date
                .from(firstComment.getCreatedAt().toInstant().plus(1, ChronoUnit.SECONDS));

        comments = issue.listComments().toList();
        assertThat(comments, hasSize(1));
        assertThat(comments, contains(hasProperty("body", equalTo("First comment"))));

        comments = issue.queryComments().list().toList();
        assertThat(comments, hasSize(1));
        assertThat(comments, contains(hasProperty("body", equalTo("First comment"))));

        // Test "since"
        comments = issue.queryComments().since(firstCommentCreatedAt).list().toList();
        assertThat(comments, hasSize(1));
        assertThat(comments, contains(hasProperty("body", equalTo("First comment"))));
        comments = issue.queryComments().since(firstCommentCreatedAtPlus1Second).list().toList();
        assertThat(comments, hasSize(0));

        // "since" is only precise up to the second,
        // so if we want to differentiate comments, we need to be completely sure they're created
        // at least 1 second from each other.
        // Waiting 2 seconds to avoid edge cases.
        Thread.sleep(2000);

        GHIssueComment secondComment = issue.comment("Second comment");
        Date secondCommentCreatedAt = secondComment.getCreatedAt();
        Date secondCommentCreatedAtPlus1Second = Date
                .from(secondComment.getCreatedAt().toInstant().plus(1, ChronoUnit.SECONDS));
        assertThat(
                "There's an error in the setup of this test; please fix it."
                        + " The second comment should be created at least one second after the first one.",
                firstCommentCreatedAtPlus1Second.getTime() <= secondCommentCreatedAt.getTime());

        comments = issue.listComments().toList();
        assertThat(comments, hasSize(2));
        assertThat(comments,
                contains(hasProperty("body", equalTo("First comment")),
                        hasProperty("body", equalTo("Second comment"))));
        comments = issue.queryComments().list().toList();
        assertThat(comments, hasSize(2));
        assertThat(comments,
                contains(hasProperty("body", equalTo("First comment")),
                        hasProperty("body", equalTo("Second comment"))));

        // Test "since"
        comments = issue.queryComments().since(firstCommentCreatedAt).list().toList();
        assertThat(comments, hasSize(2));
        assertThat(comments,
                contains(hasProperty("body", equalTo("First comment")),
                        hasProperty("body", equalTo("Second comment"))));
        comments = issue.queryComments().since(firstCommentCreatedAtPlus1Second).list().toList();
        assertThat(comments, hasSize(1));
        assertThat(comments, contains(hasProperty("body", equalTo("Second comment"))));
        comments = issue.queryComments().since(secondCommentCreatedAt).list().toList();
        assertThat(comments, hasSize(1));
        assertThat(comments, contains(hasProperty("body", equalTo("Second comment"))));
        comments = issue.queryComments().since(secondCommentCreatedAtPlus1Second).list().toList();
        assertThat(comments, hasSize(0));

        // Test "since" with timestamp instead of Date
        comments = issue.queryComments().since(secondCommentCreatedAt.getTime()).list().toList();
        assertThat(comments, hasSize(1));
        assertThat(comments, contains(hasProperty("body", equalTo("Second comment"))));
    }

    /**
     * Close issue.
     *
     * @throws Exception
     *             the exception
     */
    @Test
    public void closeIssue() throws Exception {
        String name = "closeIssue";
        GHIssue issue = getRepository().createIssue(name).body("## test").create();
        assertThat(issue.getTitle(), equalTo(name));
        assertThat(getRepository().getIssue(issue.getNumber()).getState(), equalTo(GHIssueState.OPEN));
        assertNull(getRepository().getIssue(issue.getNumber()).getStateReason());
        issue.close();
        assertThat(getRepository().getIssue(issue.getNumber()).getState(), equalTo(GHIssueState.CLOSED));
        assertThat(getRepository().getIssue(issue.getNumber()).getStateReason(), equalTo(GHIssueStateReason.COMPLETED));
    }

    /**
     * Close issue as unplanned.
     *
     * @throws Exception
     *             the exception
     */
    @Test
    public void closeIssueNotPlanned() throws Exception {
        String name = "closeIssueNotPlanned";
        GHIssue issue = getRepository().createIssue(name).body("## test").create();
        assertThat(issue.getTitle(), equalTo(name));
        assertThat(getRepository().getIssue(issue.getNumber()).getState(), equalTo(GHIssueState.OPEN));
        assertNull(getRepository().getIssue(issue.getNumber()).getStateReason());
        issue.closeNotPlanned();
        assertThat(getRepository().getIssue(issue.getNumber()).getState(), equalTo(GHIssueState.CLOSED));
        assertThat(getRepository().getIssue(issue.getNumber()).getStateReason(), equalTo(GHIssueStateReason.NOT_PLANNED));
    }

    /**
     * Sets the labels.
     *
     * @throws Exception
     *             the exception
     */
    @Test
    // Requires push access to the test repo to pass
    public void setLabels() throws Exception {
        GHIssue issue = getRepository().createIssue("setLabels").body("## test").create();
        String label = "setLabels_label_name";
        issue.setLabels(label);

        Collection<GHLabel> labels = getRepository().getIssue(issue.getNumber()).getLabels();
        assertThat(labels.size(), equalTo(1));
        GHLabel savedLabel = labels.iterator().next();
        assertThat(savedLabel.getName(), equalTo(label));
        assertThat(savedLabel.getId(), notNullValue());
        assertThat(savedLabel.getNodeId(), notNullValue());
        assertThat(savedLabel.isDefault(), is(false));
    }

    /**
     * Adds the labels.
     *
     * @throws Exception
     *             the exception
     */
    @Test
    // Requires push access to the test repo to pass
    public void addLabels() throws Exception {
        GHIssue issue = getRepository().createIssue("addLabels").body("## test").create();
        String addedLabel1 = "addLabels_label_name_1";
        String addedLabel2 = "addLabels_label_name_2";
        String addedLabel3 = "addLabels_label_name_3";

        List<GHLabel> resultingLabels = issue.addLabels(addedLabel1);
        assertThat(resultingLabels.size(), equalTo(1));
        GHLabel ghLabel = resultingLabels.get(0);
        assertThat(ghLabel.getName(), equalTo(addedLabel1));

        int requestCount = mockGitHub.getRequestCount();
        resultingLabels = issue.addLabels(addedLabel2, addedLabel3);
        // multiple labels can be added with one api call
        assertThat(mockGitHub.getRequestCount(), equalTo(requestCount + 1));

        assertThat(resultingLabels.size(), equalTo(3));
        assertThat(resultingLabels,
                containsInAnyOrder(hasProperty("name", equalTo(addedLabel1)),
                        hasProperty("name", equalTo(addedLabel2)),
                        hasProperty("name", equalTo(addedLabel3))));

        // Adding a label which is already present does not throw an error
        resultingLabels = issue.addLabels(ghLabel);
        assertThat(resultingLabels.size(), equalTo(3));
    }

    /**
     * Adds the labels concurrency issue.
     *
     * @throws Exception
     *             the exception
     */
    @Test
    // Requires push access to the test repo to pass
    public void addLabelsConcurrencyIssue() throws Exception {
        String addedLabel1 = "addLabelsConcurrencyIssue_label_name_1";
        String addedLabel2 = "addLabelsConcurrencyIssue_label_name_2";

        GHIssue issue1 = getRepository().createIssue("addLabelsConcurrencyIssue").body("## test").create();
        issue1.getLabels();

        GHIssue issue2 = getRepository().getIssue(issue1.getNumber());
        issue2.addLabels(addedLabel2);

        Collection<GHLabel> labels = issue1.addLabels(addedLabel1);

        assertThat(labels.size(), equalTo(2));
        assertThat(labels,
                containsInAnyOrder(hasProperty("name", equalTo(addedLabel1)),
                        hasProperty("name", equalTo(addedLabel2))));
    }

    /**
     * Removes the labels.
     *
     * @throws Exception
     *             the exception
     */
    @Test
    // Requires push access to the test repo to pass
    public void removeLabels() throws Exception {
        GHIssue issue = getRepository().createIssue("removeLabels").body("## test").create();
        String label1 = "removeLabels_label_name_1";
        String label2 = "removeLabels_label_name_2";
        String label3 = "removeLabels_label_name_3";
        issue.setLabels(label1, label2, label3);

        Collection<GHLabel> labels = getRepository().getIssue(issue.getNumber()).getLabels();
        assertThat(labels.size(), equalTo(3));
        GHLabel ghLabel3 = labels.stream().filter(label -> label3.equals(label.getName())).findFirst().get();

        int requestCount = mockGitHub.getRequestCount();
        List<GHLabel> resultingLabels = issue.removeLabels(label2, label3);
        // each label deleted is a separate api call
        assertThat(mockGitHub.getRequestCount(), equalTo(requestCount + 2));

        assertThat(resultingLabels.size(), equalTo(1));
        assertThat(resultingLabels.get(0).getName(), equalTo(label1));

        // Removing some labels that are not present does not throw
        // This is consistent with earlier behavior and with addLabels()
        issue.removeLabels(ghLabel3);

        // Calling removeLabel() on label that is not present will throw
        try {
            issue.removeLabel(label3);
            fail("Expected GHFileNotFoundException");
        } catch (GHFileNotFoundException e) {
            assertThat(e.getMessage(), containsString("Label does not exist"));
        }
    }

    /**
     * Sets the assignee.
     *
     * @throws Exception
     *             the exception
     */
    @Test
    // Requires push access to the test repo to pass
    public void setAssignee() throws Exception {
        GHIssue issue = getRepository().createIssue("setAssignee").body("## test").create();
        GHMyself user = gitHub.getMyself();
        issue.assignTo(user);

        assertThat(getRepository().getIssue(issue.getNumber()).getAssignee(), equalTo(user));
    }

    /**
     * Gets the user test.
     *
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     */
    @Test
    public void getUserTest() throws IOException {
        GHIssue issue = getRepository().createIssue("getUserTest").create();
        GHIssue issueSingle = getRepository().getIssue(issue.getNumber());
        assertThat(issueSingle.getUser().root(), notNullValue());

        PagedIterable<GHIssue> ghIssues = getRepository().listIssues(GHIssueState.OPEN);
        for (GHIssue otherIssue : ghIssues) {
            assertThat(otherIssue.getUser().root(), notNullValue());
        }
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

    private GHRepository getRepository(GitHub gitHub) throws IOException {
        return gitHub.getOrganization(GITHUB_API_TEST_ORG).getRepository("GHIssueTest");
    }

}
