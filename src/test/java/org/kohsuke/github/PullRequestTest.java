/*
 * GitHub API for Java
 * Copyright (C) 2009-2017 SonarSource SA
 * mailto:info AT sonarsource DOT com
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */
package org.kohsuke.github;

import org.junit.After;
import org.junit.Test;

import java.io.IOException;
import java.util.Collection;
import java.util.List;

import static org.hamcrest.CoreMatchers.*;

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
    public void testPullRequestReviews() throws Exception {
        String name = rnd.next();
        GHPullRequest p = getRepository().createPullRequest(name, "stable", "master", "## test");
        GHPullRequestReview draftReview = p.createReview()
            .body("Some draft review")
            .comment("Some niggle", "changelog.html", 1)
            .create();
        assertThat(draftReview.getState(), is(GHPullRequestReviewState.PENDING));
        assertThat(draftReview.getBody(), is("Some draft review"));
        assertThat(draftReview.getCommitId(), notNullValue());
        List<GHPullRequestReview> reviews = p.listReviews().asList();
        assertThat(reviews.size(), is(1));
        GHPullRequestReview review = reviews.get(0);
        assertThat(review.getState(), is(GHPullRequestReviewState.PENDING));
        assertThat(review.getBody(), is("Some draft review"));
        assertThat(review.getCommitId(), notNullValue());
        draftReview.submit("Some review comment", GHPullRequestReviewEvent.COMMENT);
        List<GHPullRequestReviewComment> comments = review.listReviewComments().asList();
        assertEquals(1, comments.size());
        GHPullRequestReviewComment comment = comments.get(0);
        assertEquals("Some niggle", comment.getBody());
        draftReview = p.createReview()
            .body("Some new review")
            .comment("Some niggle", "changelog.html", 1)
            .create();
        draftReview.delete();
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
    public void testSquashMerge() throws Exception {
        String name = rnd.next();
        GHRef masterRef = getRepository().getRef("heads/master");
        GHRef branchRef = getRepository().createRef("refs/heads/" + name, masterRef.getObject().getSha());
        getRepository().createContent(name, name, name, name);
        Thread.sleep(1000);
        GHPullRequest p = getRepository().createPullRequest(name, name, "master", "## test squash");
        Thread.sleep(1000);
        p.merge("squash merge", null, GHPullRequest.MergeMethod.SQUASH);
        branchRef.delete();
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
