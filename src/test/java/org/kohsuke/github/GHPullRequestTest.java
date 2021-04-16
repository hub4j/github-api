package org.kohsuke.github;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasProperty;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;

/**
 * @author Kohsuke Kawaguchi
 */
public class GHPullRequestTest extends AbstractGitHubWireMockTest {

    @Before
    @After
    public void cleanUp() throws Exception {
        // Cleanup is only needed when proxying
        if (!mockGitHub.isUseProxy()) {
            return;
        }

        for (GHPullRequest pr : getRepository(this.getGitHubBeforeAfter()).getPullRequests(GHIssueState.OPEN)) {
            pr.close();
        }
    }

    @Test
    public void createPullRequest() throws Exception {
        String name = "createPullRequest";
        GHRepository repo = getRepository();
        GHPullRequest p = repo.createPullRequest(name, "test/stable", "master", "## test");
        assertEquals(name, p.getTitle());
        assertThat(p.canMaintainerModify(), is(false));
        assertThat(p.isDraft(), is(false));
    }

    @Test
    public void createDraftPullRequest() throws Exception {
        String name = "createDraftPullRequest";
        GHRepository repo = getRepository();
        GHPullRequest p = repo.createPullRequest(name, "test/stable", "master", "## test", false, true);
        assertEquals(name, p.getTitle());
        assertThat(p.canMaintainerModify(), is(false));
        assertThat(p.isDraft(), is(true));

        // There are multiple paths to get PRs and each needs to read draft correctly
        p.draft = false;
        p.refresh();
        assertThat(p.isDraft(), is(true));

        GHPullRequest p2 = repo.getPullRequest(p.getNumber());
        assertThat(p2.getNumber(), is(p.getNumber()));
        assertThat(p2.isDraft(), is(true));

        p = repo.queryPullRequests().state(GHIssueState.OPEN).head("test/stable").list().toList().get(0);
        assertThat(p2.getNumber(), is(p.getNumber()));
        assertThat(p.isDraft(), is(true));

    }

    @Test
    public void createPullRequestComment() throws Exception {
        String name = "createPullRequestComment";
        GHPullRequest p = getRepository().createPullRequest(name, "test/stable", "master", "## test");
        p.comment("Some comment");
    }

    @Test
    public void closePullRequest() throws Exception {
        String name = "closePullRequest";
        GHPullRequest p = getRepository().createPullRequest(name, "test/stable", "master", "## test");
        // System.out.println(p.getUrl());
        assertEquals(name, p.getTitle());
        assertEquals(GHIssueState.OPEN, getRepository().getPullRequest(p.getNumber()).getState());
        p.close();
        assertEquals(GHIssueState.CLOSED, getRepository().getPullRequest(p.getNumber()).getState());
    }

    @Test
    public void pullRequestReviews() throws Exception {
        String name = "testPullRequestReviews";
        GHPullRequest p = getRepository().createPullRequest(name, "test/stable", "master", "## test");
        GHPullRequestReview draftReview = p.createReview()
                .body("Some draft review")
                .comment("Some niggle", "README.md", 1)
                .create();
        assertThat(draftReview.getState(), is(GHPullRequestReviewState.PENDING));
        assertThat(draftReview.getBody(), is("Some draft review"));
        assertThat(draftReview.getCommitId(), notNullValue());
        List<GHPullRequestReview> reviews = p.listReviews().toList();
        assertThat(reviews.size(), is(1));
        GHPullRequestReview review = reviews.get(0);
        assertThat(review.getState(), is(GHPullRequestReviewState.PENDING));
        assertThat(review.getBody(), is("Some draft review"));
        assertThat(review.getCommitId(), notNullValue());
        draftReview.submit("Some review comment", GHPullRequestReviewEvent.COMMENT);
        List<GHPullRequestReviewComment> comments = review.listReviewComments().toList();
        assertEquals(1, comments.size());
        GHPullRequestReviewComment comment = comments.get(0);
        assertEquals("Some niggle", comment.getBody());
        draftReview = p.createReview().body("Some new review").comment("Some niggle", "README.md", 1).create();
        draftReview.delete();
    }

    @Test
    public void pullRequestReviewComments() throws Exception {
        String name = "pullRequestReviewComments";
        GHPullRequest p = getRepository().createPullRequest(name, "test/stable", "master", "## test");
        try {
            // System.out.println(p.getUrl());
            assertTrue(p.listReviewComments().toList().isEmpty());
            p.createReviewComment("Sample review comment", p.getHead().getSha(), "README.md", 1);
            List<GHPullRequestReviewComment> comments = p.listReviewComments().toList();
            assertEquals(1, comments.size());
            GHPullRequestReviewComment comment = comments.get(0);
            assertEquals("Sample review comment", comment.getBody());
            assertThat(comment.getInReplyToId(), equalTo(-1L));
            assertThat(comment.getPath(), equalTo("README.md"));
            assertThat(comment.getPosition(), equalTo(1));
            assertThat(comment.getUser(), notNullValue());
            // Assert htmlUrl is not null
            assertThat(comment.getHtmlUrl(), notNullValue());
            assertThat(comment.getHtmlUrl().toString(),
                    containsString("hub4j-test-org/github-api/pull/" + p.getNumber()));

            List<GHReaction> reactions = comment.listReactions().toList();
            assertThat(reactions.size(), equalTo(0));

            GHReaction reaction = comment.createReaction(ReactionContent.CONFUSED);
            assertThat(reaction.getContent(), equalTo(ReactionContent.CONFUSED));

            reactions = comment.listReactions().toList();
            assertThat(reactions.size(), equalTo(1));

            GHPullRequestReviewComment reply = comment.reply("This is a reply.");
            assertThat(reply.getInReplyToId(), equalTo(comment.getId()));
            comments = p.listReviewComments().toList();

            assertEquals(2, comments.size());

            comment.update("Updated review comment");
            comments = p.listReviewComments().toList();
            comment = comments.get(0);
            assertEquals("Updated review comment", comment.getBody());

            comment.delete();
            comments = p.listReviewComments().toList();
            // Reply is still present after delete of original comment, but no longer has replyToId
            assertThat(comments.size(), equalTo(1));
            assertThat(comments.get(0).getId(), equalTo(reply.getId()));
            assertThat(comments.get(0).getInReplyToId(), equalTo(-1L));
        } finally {
            p.close();
        }
    }

    @Test
    public void testPullRequestReviewRequests() throws Exception {
        String name = "testPullRequestReviewRequests";
        GHPullRequest p = getRepository().createPullRequest(name, "test/stable", "master", "## test");
        // System.out.println(p.getUrl());
        assertTrue(p.getRequestedReviewers().isEmpty());

        GHUser kohsuke2 = gitHub.getUser("kohsuke2");
        p.requestReviewers(Collections.singletonList(kohsuke2));
        p.refresh();
        assertFalse(p.getRequestedReviewers().isEmpty());
    }

    @Test
    public void testPullRequestTeamReviewRequests() throws Exception {
        String name = "testPullRequestTeamReviewRequests";
        GHPullRequest p = getRepository().createPullRequest(name, "test/stable", "master", "## test");
        // System.out.println(p.getUrl());
        assertTrue(p.getRequestedReviewers().isEmpty());

        GHOrganization testOrg = gitHub.getOrganization("hub4j-test-org");
        GHTeam testTeam = testOrg.getTeamBySlug("dummy-team");

        p.requestTeamReviewers(Collections.singletonList(testTeam));

        int baseRequestCount = mockGitHub.getRequestCount();
        p.refresh();
        assertThat("We should not eagerly load organizations for teams",
                mockGitHub.getRequestCount() - baseRequestCount,
                equalTo(1));
        assertThat(p.getRequestedTeams().size(), equalTo(1));
        assertThat("We should not eagerly load organizations for teams",
                mockGitHub.getRequestCount() - baseRequestCount,
                equalTo(1));
        assertThat("Org should be queried for automatically if asked for",
                p.getRequestedTeams().get(0).getOrganization(),
                notNullValue());
        assertThat("Request count should show lazy load occurred",
                mockGitHub.getRequestCount() - baseRequestCount,
                equalTo(2));
    }

    @Test
    public void mergeCommitSHA() throws Exception {
        String name = "mergeCommitSHA";
        GHRepository repo = getRepository();
        GHPullRequest p = repo.createPullRequest(name, "test/mergeable_branch", "master", "## test");
        int baseRequestCount = mockGitHub.getRequestCount();
        assertThat(p.getMergeableNoRefresh(), nullValue());
        assertThat("Used existing value", mockGitHub.getRequestCount() - baseRequestCount, equalTo(0));

        // mergeability computation takes time, this should still be null immediately after creation
        assertThat(p.getMergeable(), nullValue());
        assertThat("Asked for PR information", mockGitHub.getRequestCount() - baseRequestCount, equalTo(1));

        for (int i = 2; i <= 10; i++) {
            if (Boolean.TRUE.equals(p.getMergeable()) && p.getMergeCommitSha() != null) {
                assertThat("Asked for PR information", mockGitHub.getRequestCount() - baseRequestCount, equalTo(i));

                // make sure commit exists
                GHCommit commit = repo.getCommit(p.getMergeCommitSha());
                assertNotNull(commit);

                assertThat("Asked for PR information", mockGitHub.getRequestCount() - baseRequestCount, equalTo(i + 1));

                return;
            }

            // mergeability computation takes time. give it more chance
            Thread.sleep(1000);
        }
        // hmm?
        fail();
    }

    @Test
    public void setBaseBranch() throws Exception {
        String prName = "testSetBaseBranch";
        String originalBaseBranch = "master";
        String newBaseBranch = "gh-pages";

        GHPullRequest pullRequest = getRepository().createPullRequest(prName, "test/stable", "master", "## test");

        assertEquals("Pull request base branch is supposed to be " + originalBaseBranch,
                originalBaseBranch,
                pullRequest.getBase().getRef());

        GHPullRequest responsePullRequest = pullRequest.setBaseBranch(newBaseBranch);

        assertEquals("Pull request base branch is supposed to be " + newBaseBranch,
                newBaseBranch,
                responsePullRequest.getBase().getRef());
    }

    @Test
    public void setBaseBranchNonExisting() throws Exception {
        String prName = "testSetBaseBranchNonExisting";
        String originalBaseBranch = "master";
        String newBaseBranch = "non-existing";

        GHPullRequest pullRequest = getRepository().createPullRequest(prName, "test/stable", "master", "## test");

        assertEquals("Pull request base branch is supposed to be " + originalBaseBranch,
                originalBaseBranch,
                pullRequest.getBase().getRef());

        try {
            pullRequest.setBaseBranch(newBaseBranch);
        } catch (HttpException e) {
            assertThat(e, instanceOf(HttpException.class));
            assertThat(e.toString(), containsString("Proposed base branch 'non-existing' was not found"));
        }

        pullRequest.close();
    }

    @Test
    public void updateOutdatedBranchesUnexpectedHead() throws Exception {
        String prName = "testUpdateOutdatedBranches";
        String outdatedRefName = "refs/heads/outdated";
        GHRepository repository = gitHub.getOrganization("hub4j-test-org").getRepository("updateOutdatedBranches");

        GHRef outdatedRef = repository.getRef(outdatedRefName);
        outdatedRef.updateTo("6440189369f9f33b2366556a94dbc26f2cfdd969", true);

        GHPullRequest outdatedPullRequest = repository.createPullRequest(prName, "outdated", "master", "## test");

        do {
            Thread.sleep(5000);
            outdatedPullRequest.refresh();
        } while (outdatedPullRequest.getMergeableState().equalsIgnoreCase("unknown"));

        assertEquals("Pull request is supposed to be not up to date",
                "behind",
                outdatedPullRequest.getMergeableState());

        outdatedRef.updateTo("f567328eb81270487864963b7d7446953353f2b5", true);

        try {
            outdatedPullRequest.updateBranch();
        } catch (HttpException e) {
            assertThat(e, instanceOf(HttpException.class));
            assertThat(e.toString(), containsString("expected head sha didn’t match current head ref."));
        }

        outdatedPullRequest.close();
    }

    @Test
    public void updateOutdatedBranches() throws Exception {
        String prName = "testUpdateOutdatedBranches";
        String outdatedRefName = "refs/heads/outdated";
        GHRepository repository = gitHub.getOrganization("hub4j-test-org").getRepository("updateOutdatedBranches");

        repository.getRef(outdatedRefName).updateTo("6440189369f9f33b2366556a94dbc26f2cfdd969", true);

        GHPullRequest outdatedPullRequest = repository.createPullRequest(prName, "outdated", "master", "## test");

        do {
            Thread.sleep(5000);
            outdatedPullRequest.refresh();
        } while (outdatedPullRequest.getMergeableState().equalsIgnoreCase("unknown"));

        assertEquals("Pull request is supposed to be not up to date",
                "behind",
                outdatedPullRequest.getMergeableState());

        outdatedPullRequest.updateBranch();
        outdatedPullRequest.refresh();

        assertNotEquals("Pull request is supposed to be up to date", "behind", outdatedPullRequest.getMergeableState());

        outdatedPullRequest.close();
    }

    @Test
    public void squashMerge() throws Exception {
        String name = "squashMerge";
        String branchName = "test/" + name;
        GHRef masterRef = getRepository().getRef("heads/master");
        GHRef branchRef = getRepository().createRef("refs/heads/" + branchName, masterRef.getObject().getSha());

        getRepository().createContent(name, name, name, branchName);
        Thread.sleep(1000);
        GHPullRequest p = getRepository().createPullRequest(name, branchName, "master", "## test squash");
        Thread.sleep(1000);
        p.merge("squash merge", null, GHPullRequest.MergeMethod.SQUASH);
    }

    @Test
    public void updateContentSquashMerge() throws Exception {
        String name = "updateContentSquashMerge";
        String branchName = "test/" + name;

        GHRef masterRef = getRepository().getRef("heads/master");
        GHRef branchRef = getRepository().createRef("refs/heads/" + branchName, masterRef.getObject().getSha());

        GHContentUpdateResponse response = getRepository().createContent(name, name, name, branchName);
        Thread.sleep(1000);

        getRepository().createContent()
                .content(name + name)
                .path(name)
                .branch(branchName)
                .message(name)
                .sha(response.getContent().getSha())
                .commit();
        GHPullRequest p = getRepository().createPullRequest(name, branchName, "master", "## test squash");
        Thread.sleep(1000);
        p.merge("squash merge", null, GHPullRequest.MergeMethod.SQUASH);
    }

    @Test
    public void queryPullRequestsQualifiedHead() throws Exception {
        GHRepository repo = getRepository();
        // Create PRs from two different branches to master
        repo.createPullRequest("queryPullRequestsQualifiedHead_stable", "test/stable", "master", null);
        repo.createPullRequest("queryPullRequestsQualifiedHead_rc", "test/rc", "master", null);

        // Query by one of the heads and make sure we only get that branch's PR back.
        List<GHPullRequest> prs = repo.queryPullRequests()
                .state(GHIssueState.OPEN)
                .head("hub4j-test-org:test/stable")
                .base("master")
                .list()
                .toList();
        assertNotNull(prs);
        assertEquals(1, prs.size());
        assertEquals("test/stable", prs.get(0).getHead().getRef());
    }

    @Test
    public void queryPullRequestsUnqualifiedHead() throws Exception {
        GHRepository repo = getRepository();
        // Create PRs from two different branches to master
        repo.createPullRequest("queryPullRequestsUnqualifiedHead_stable", "test/stable", "master", null);
        repo.createPullRequest("queryPullRequestsUnqualifiedHead_rc", "test/rc", "master", null);

        // Query by one of the heads and make sure we only get that branch's PR back.
        List<GHPullRequest> prs = repo.queryPullRequests()
                .state(GHIssueState.OPEN)
                .head("test/stable")
                .base("master")
                .list()
                .toList();
        assertNotNull(prs);
        assertEquals(1, prs.size());
        assertEquals("test/stable", prs.get(0).getHead().getRef());
    }

    @Test
    // Requires push access to the test repo to pass
    public void setLabels() throws Exception {
        GHPullRequest p = getRepository().createPullRequest("setLabels", "test/stable", "master", "## test");
        String label = "setLabels_label_name";
        p.setLabels(label);

        Collection<GHLabel> labels = getRepository().getPullRequest(p.getNumber()).getLabels();
        assertEquals(1, labels.size());
        GHLabel savedLabel = labels.iterator().next();
        assertEquals(label, savedLabel.getName());
        assertNotNull(savedLabel.getId());
        assertNotNull(savedLabel.getNodeId());
        assertFalse(savedLabel.isDefault());
    }

    @Test
    // Requires push access to the test repo to pass
    public void addLabels() throws Exception {
        GHPullRequest p = getRepository().createPullRequest("addLabels", "test/stable", "master", "## test");
        String addedLabel1 = "addLabels_label_name_1";
        String addedLabel2 = "addLabels_label_name_2";
        String addedLabel3 = "addLabels_label_name_3";

        List<GHLabel> resultingLabels = p.addLabels(addedLabel1);
        assertEquals(1, resultingLabels.size());
        GHLabel ghLabel = resultingLabels.get(0);
        assertThat(ghLabel.getName(), equalTo(addedLabel1));

        int requestCount = mockGitHub.getRequestCount();
        resultingLabels = p.addLabels(addedLabel2, addedLabel3);
        // multiple labels can be added with one api call
        assertThat(mockGitHub.getRequestCount(), equalTo(requestCount + 1));

        assertEquals(3, resultingLabels.size());
        assertThat(resultingLabels,
                containsInAnyOrder(hasProperty("name", equalTo(addedLabel1)),
                        hasProperty("name", equalTo(addedLabel2)),
                        hasProperty("name", equalTo(addedLabel3))));

        // Adding a label which is already present does not throw an error
        resultingLabels = p.addLabels(ghLabel);
        assertThat(resultingLabels.size(), equalTo(3));
    }

    @Test
    // Requires push access to the test repo to pass
    public void addLabelsConcurrencyIssue() throws Exception {
        String addedLabel1 = "addLabelsConcurrencyIssue_label_name_1";
        String addedLabel2 = "addLabelsConcurrencyIssue_label_name_2";

        GHPullRequest p1 = getRepository()
                .createPullRequest("addLabelsConcurrencyIssue", "test/stable", "master", "## test");
        p1.getLabels();

        GHPullRequest p2 = getRepository().getPullRequest(p1.getNumber());
        p2.addLabels(addedLabel2);

        Collection<GHLabel> labels = p1.addLabels(addedLabel1);

        assertEquals(2, labels.size());
        assertThat(labels,
                containsInAnyOrder(hasProperty("name", equalTo(addedLabel1)),
                        hasProperty("name", equalTo(addedLabel2))));
    }

    @Test
    // Requires push access to the test repo to pass
    public void removeLabels() throws Exception {
        GHPullRequest p = getRepository().createPullRequest("removeLabels", "test/stable", "master", "## test");
        String label1 = "removeLabels_label_name_1";
        String label2 = "removeLabels_label_name_2";
        String label3 = "removeLabels_label_name_3";
        p.setLabels(label1, label2, label3);

        Collection<GHLabel> labels = getRepository().getPullRequest(p.getNumber()).getLabels();
        assertEquals(3, labels.size());
        GHLabel ghLabel3 = labels.stream().filter(label -> label3.equals(label.getName())).findFirst().get();

        int requestCount = mockGitHub.getRequestCount();
        List<GHLabel> resultingLabels = p.removeLabels(label2, label3);
        // each label deleted is a separate api call
        assertThat(mockGitHub.getRequestCount(), equalTo(requestCount + 2));

        assertEquals(1, resultingLabels.size());
        assertEquals(label1, resultingLabels.get(0).getName());

        // Removing some labels that are not present does not throw
        // This is consistent with earlier behavior and with addLabels()
        p.removeLabels(ghLabel3);

        // Calling removeLabel() on label that is not present will throw
        try {
            p.removeLabel(label3);
            fail("Expected GHFileNotFoundException");
        } catch (GHFileNotFoundException e) {
            assertThat(e.getMessage(), containsString("Label does not exist"));
        }
    }

    @Test
    // Requires push access to the test repo to pass
    public void setAssignee() throws Exception {
        GHPullRequest p = getRepository().createPullRequest("setAssignee", "test/stable", "master", "## test");
        GHMyself user = gitHub.getMyself();
        p.assignTo(user);

        assertEquals(user, getRepository().getPullRequest(p.getNumber()).getAssignee());
    }

    @Test
    public void getUserTest() throws IOException {
        GHPullRequest p = getRepository().createPullRequest("getUserTest", "test/stable", "master", "## test");
        GHPullRequest prSingle = getRepository().getPullRequest(p.getNumber());
        assertNotNull(prSingle.getUser().getRoot());
        prSingle.getMergeable();
        assertNotNull(prSingle.getUser().getRoot());

        PagedIterable<GHPullRequest> ghPullRequests = getRepository().listPullRequests(GHIssueState.OPEN);
        for (GHPullRequest pr : ghPullRequests) {
            assertNotNull(pr.getUser().getRoot());
            pr.getMergeable();
            assertNotNull(pr.getUser().getRoot());
        }
    }

    protected GHRepository getRepository() throws IOException {
        return getRepository(gitHub);
    }

    private GHRepository getRepository(GitHub gitHub) throws IOException {
        return gitHub.getOrganization("hub4j-test-org").getRepository("github-api");
    }
}
