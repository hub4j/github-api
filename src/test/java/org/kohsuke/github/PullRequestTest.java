package org.kohsuke.github;

import org.junit.After;
import org.junit.Test;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

/**
 * @author Kohsuke Kawaguchi
 */
public class PullRequestTest extends AbstractGitHubApiTestBase {
    @Test
    public void createPullRequest() throws Exception {
        String name = rnd.next();
        GHPullRequest p = getRepository().createPullRequest(name, "stable", "master", "## test");
        System.out.println(p.getUrl());
        assertEquals(name, p.getTitle());
    }

    @Test
    public void createPullRequestComment() throws Exception {
        String name = rnd.next();
        GHPullRequest p = getRepository().createPullRequest(name, "stable", "master", "## test");
        p.comment("Some comment");
    }

    @Test
    public void testPullRequestReviewComments() throws Exception {
        String name = rnd.next();
        GHPullRequest p = getRepository().createPullRequest(name, "stable", "master", "## test");
        System.out.println(p.getUrl());
        assertTrue(p.listReviewComments().asList().isEmpty());
        p.createReviewComment("Sample review comment", p.getHead().getSha(), "cli/pom.xml", 5);
        List<GHPullRequestReviewComment> comments = p.listReviewComments().asList();
        assertEquals(1, comments.size());
        GHPullRequestReviewComment comment = comments.get(0);
        assertEquals("Sample review comment", comment.getBody());

        comment.update("Updated review comment");
        comments = p.listReviewComments().asList();
        assertEquals(1, comments.size());
        comment = comments.get(0);
        assertEquals("Updated review comment", comment.getBody());

        comment.delete();
        comments = p.listReviewComments().asList();
        assertTrue(comments.isEmpty());
    }

    @Test
    public void testPullRequestReviews() throws Exception{
        String name = rnd.next();
        // Create PR
        GHPullRequest p = getRepository().createPullRequest(name, "stable", "master", "## test");
        System.out.println(p.getUrl());
        // At this present moment this should be empty
        assertTrue(p.listReviews().asList().isEmpty());

        // Create a review without any review comment
        p.createReview(p.getHead().getSha(),"Sample review 1", GHPullRequestReviewEventType.COMMENT);
        List<GHPullRequestReview> reviewList = p.listReviews().asList();
        // At this moment, this should have 1 item
        assertEquals(1, reviewList.size());

        // Create a review with a review comment
        GHPullRequestReviewComment reviewComment = new GHPullRequestReviewComment();
        reviewComment.setBody("Sample review comment");
        reviewComment.setPath("cli/pom.xml");
        reviewComment.setPosition(5);

        GHPullRequestReviewComment reviewComment2 = new GHPullRequestReviewComment();
        reviewComment2.setBody("Sample review comment");
        reviewComment2.setPath("cli/pom.xml");
        reviewComment2.setPosition(6);

        p.createReview(p.getHead().getSha(),"Sample review 2", GHPullRequestReviewEventType.COMMENT, Arrays.asList(reviewComment,reviewComment2));
        // At this moment, this should have 2 items
        List<GHPullRequestReview> reviewList2 = p.listReviews().asList();
        assertEquals(2, reviewList2.size());


        // Create a review with multiple comments
        int numberOfComments = 4;
        List<GHPullRequestReviewComment> reviewComments = new ArrayList<GHPullRequestReviewComment>();
        for (int i=0; i < numberOfComments; i++){
            GHPullRequestReviewComment rc = new GHPullRequestReviewComment();
            rc.setBody("Sample review comment " + i);
            rc.setPath("cli/pom.xml");
            rc.setPosition(5 + i);
            reviewComments.add(rc);
        }
        GHPullRequestReview review3 = p.createReview(p.getHead().getSha(), "Sample review 3", GHPullRequestReviewEventType.COMMENT, reviewComments);
        // Test the listComments method form the GHPullRequestReview class
        List<GHPullRequestReviewComment> requestReviewComments = review3.listComments().asList();
        assertEquals(numberOfComments, requestReviewComments.size());
        // At this moment, this should have 3 items
        List<GHPullRequestReview> reviewList3 = p.listReviews().asList();
        assertEquals(3, reviewList3.size());

        /**
         * Pull request authors can't request changes/approve on their own pull requests, hence they can't delete their
         * reviews since it hasn't been set to pending state. To test this feature properly, you need to have multiple
         * (at least 2) authenticated users with the right permissions to the repository where you're opening the PR.
         *
         * This is out of scope of this PR, however, I intent to submitting a new PR to refactor the test suite to
         * allow a more comprehensive way to cover the majority of scenarios. For the time being, I will comment
         * out some flows that can be used to test this feature locally.
         *
         */
        /*
            // Delete a review
            review3.delete();
            // At this moment, this should have 2 items
            List<GHPullRequestReview> reviewList4 = p.listReviews().asList();
            assertEquals(2, reviewList4.size());
        */

        /**
         * The submit request can't be properly tested unless you have multiple github users with the right access to the
         * github-api-test-org/jenkins repository as one can't approve his own pull request review.
         *
         * This is the error that you might receive if you try to run it using a single user for both creating the PR and approving it.
         * "errors":["Could not approve pull request review."]
         *
         * This is out of scope of this PR, however, I intent to submitting a new PR to refactor the test suite to
         * allow a more comprehensive way to cover the majority of scenarios. For the time being, I will comment
         * out some flows that can be used to test this feature locally.
         */
        /*
            // Approve a review
            reviewList2.get(0).submit("Approving review",GHPullRequestReviewEventType.APPROVE);
        */

    }

    @Test
    public void testMergeCommitSHA() throws Exception {
        String name = rnd.next();
        GHPullRequest p = getRepository().createPullRequest(name, "mergeable-branch", "master", "## test");
        for (int i=0; i<100; i++) {
            GHPullRequest updated = getRepository().getPullRequest(p.getNumber());
            if (updated.getMergeCommitSha()!=null) {
                // make sure commit exists
                GHCommit commit = getRepository().getCommit(updated.getMergeCommitSha());
                assertNotNull(commit);
                return;
            }

            // mergeability computation takes time. give it more chance
            Thread.sleep(100);
        }
        // hmm?
        fail();
    }

    @Test
    // Requires push access to the test repo to pass
    public void setLabels() throws Exception {
        GHPullRequest p = getRepository().createPullRequest(rnd.next(), "stable", "master", "## test");
        String label = rnd.next();
        p.setLabels(label);

        Collection<GHLabel> labels = getRepository().getPullRequest(p.getNumber()).getLabels();
        assertEquals(1, labels.size());
        assertEquals(label, labels.iterator().next().getName());
    }

    @Test
    // Requires push access to the test repo to pass
    public void setAssignee() throws Exception {
        GHPullRequest p = getRepository().createPullRequest(rnd.next(), "stable", "master", "## test");
        GHMyself user = gitHub.getMyself();
        p.assignTo(user);

        assertEquals(user, getRepository().getPullRequest(p.getNumber()).getAssignee());
    }

    @Test
    public void testGetUser() throws IOException {
        GHPullRequest p = getRepository().createPullRequest(rnd.next(), "stable", "master", "## test");
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

    @After
    public void cleanUp() throws Exception {
        for (GHPullRequest pr : getRepository().getPullRequests(GHIssueState.OPEN)) {
            pr.close();
        }
    }

    private GHRepository getRepository() throws IOException {
        return gitHub.getOrganization("github-api-test-org").getRepository("jenkins");
    }
}
