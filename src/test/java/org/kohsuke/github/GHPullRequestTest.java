package org.kohsuke.github;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import static org.hamcrest.CoreMatchers.*;

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
        // System.out.println(p.getUrl());
        assertTrue(p.listReviewComments().toList().isEmpty());
        p.createReviewComment("Sample review comment", p.getHead().getSha(), "README.md", 1);
        List<GHPullRequestReviewComment> comments = p.listReviewComments().toList();
        assertEquals(1, comments.size());
        GHPullRequestReviewComment comment = comments.get(0);
        assertEquals("Sample review comment", comment.getBody());

        comment.update("Updated review comment");
        comments = p.listReviewComments().toList();
        assertEquals(1, comments.size());
        comment = comments.get(0);
        assertEquals("Updated review comment", comment.getBody());

        comment.delete();
        comments = p.listReviewComments().toList();
        assertTrue(comments.isEmpty());
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

        GHOrganization testOrg = gitHub.getOrganization("github-api-test-org");
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
                .head("github-api-test-org:test/stable")
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
        assertEquals(label, labels.iterator().next().getName());
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
        assertNotNull(prSingle.getUser().root);
        prSingle.getMergeable();
        assertNotNull(prSingle.getUser().root);

        PagedIterable<GHPullRequest> ghPullRequests = getRepository().listPullRequests(GHIssueState.OPEN);
        for (GHPullRequest pr : ghPullRequests) {
            assertNotNull(pr.getUser().root);
            pr.getMergeable();
            assertNotNull(pr.getUser().root);
        }
    }

    protected GHRepository getRepository() throws IOException {
        return getRepository(gitHub);
    }

    private GHRepository getRepository(GitHub gitHub) throws IOException {
        return gitHub.getOrganization("github-api-test-org").getRepository("github-api");
    }
}
