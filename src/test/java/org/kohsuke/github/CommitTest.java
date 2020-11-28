package org.kohsuke.github;

import com.google.common.collect.Iterables;
import org.junit.Test;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

/**
 * @author Kohsuke Kawaguchi
 */
public class CommitTest extends AbstractGitHubWireMockTest {
    @Test // issue 152
    public void lastStatus() throws IOException {
        GHTag t = gitHub.getRepository("stapler/stapler").listTags().iterator().next();
        assertNotNull(t.getCommit().getLastStatus());
    }

    @Test // issue 230
    public void listFiles() throws Exception {
        GHRepository repo = gitHub.getRepository("stapler/stapler");
        PagedIterable<GHCommit> commits = repo.queryCommits().path("pom.xml").list();
        for (GHCommit commit : Iterables.limit(commits, 10)) {
            GHCommit expected = repo.getCommit(commit.getSHA1());
            assertEquals(expected.getFiles().size(), commit.getFiles().size());
        }
    }

    @Test
    public void listPullRequestsOfNotIncludedCommit() throws Exception {
        GHRepository repo = gitHub.getOrganization("hub4j-test-org").getRepository("listPrsListHeads");

        GHCommit commit = repo.getCommit("f66f7ca691ace6f4a9230292efb932b49214d72c");

        assertThat("The commit is supposed to be not part of any pull request",
                commit.listPullRequests().toList().isEmpty());
    }

    @Test
    public void listPullRequests() throws Exception {
        GHRepository repo = gitHub.getOrganization("hub4j-test-org").getRepository("listPrsListHeads");
        Integer prNumber = 2;

        GHCommit commit = repo.getCommit("6b9956fe8c3d030dbc49c9d4c4166b0ceb4198fc");

        List<GHPullRequest> listedPrs = commit.listPullRequests().toList();

        assertEquals(listedPrs.size(), 1);

        assertThat("Pull request " + prNumber + " not found by searching from commit.",
                listedPrs.stream().findFirst().filter(it -> it.getNumber() == prNumber).isPresent());
    }

    @Test
    public void listPullRequestsOfCommitWith2PullRequests() throws Exception {
        GHRepository repo = gitHub.getOrganization("hub4j-test-org").getRepository("listPrsListHeads");
        Integer[] expectedPrs = new Integer[]{ 1, 2 };

        GHCommit commit = repo.getCommit("442aa213f924a5984856f16e52a18153aaf41ad3");

        List<GHPullRequest> listedPrs = commit.listPullRequests().toList();

        assertEquals(listedPrs.size(), 2);

        listedPrs.stream()
                .forEach(pr -> assertThat("PR#" + pr.getNumber() + " not expected to be matched.",
                        Arrays.stream(expectedPrs).anyMatch(prNumber -> prNumber.equals(pr.getNumber()))));
    }

    @Test
    public void listBranchesWhereHead() throws Exception {
        GHRepository repo = gitHub.getOrganization("hub4j-test-org").getRepository("listPrsListHeads");

        GHCommit commit = repo.getCommit("ab92e13c0fc844fd51a379a48a3ad0b18231215c");

        assertThat("Commit which was supposed to be HEAD in the \"master\" branch was not found.",
                commit.listBranchesWhereHead()
                        .toList()
                        .stream()
                        .findFirst()
                        .filter(it -> it.getName().equals("master"))
                        .isPresent());
    }

    @Test
    public void listBranchesWhereHead2Heads() throws Exception {
        GHRepository repo = gitHub.getOrganization("hub4j-test-org").getRepository("listPrsListHeads");

        GHCommit commit = repo.getCommit("ab92e13c0fc844fd51a379a48a3ad0b18231215c");

        assertEquals("Commit which was supposed to be HEAD in 2 branches was not found as such.",
                2,
                commit.listBranchesWhereHead().toList().size());
    }

    @Test
    public void listBranchesWhereHeadOfCommitWithHeadNowhere() throws Exception {
        GHRepository repo = gitHub.getOrganization("hub4j-test-org").getRepository("listPrsListHeads");

        GHCommit commit = repo.getCommit("7460916bfb8e9966d6b9d3e8ae378c82c6b8e43e");

        assertThat("Commit which was not supposed to be HEAD in any branch was found as HEAD.",
                commit.listBranchesWhereHead().toList().isEmpty());
    }

    @Test // issue 737
    public void commitSignatureVerification() throws Exception {
        GHRepository repo = gitHub.getRepository("stapler/stapler");
        PagedIterable<GHCommit> commits = repo.queryCommits().path("pom.xml").list();
        for (GHCommit commit : Iterables.limit(commits, 10)) {
            GHCommit expected = repo.getCommit(commit.getSHA1());
            assertEquals(expected.getCommitShortInfo().getVerification().isVerified(),
                    commit.getCommitShortInfo().getVerification().isVerified());
            assertEquals(expected.getCommitShortInfo().getVerification().getReason(),
                    commit.getCommitShortInfo().getVerification().getReason());
            assertEquals(expected.getCommitShortInfo().getVerification().getSignature(),
                    commit.getCommitShortInfo().getVerification().getSignature());
            assertEquals(expected.getCommitShortInfo().getVerification().getPayload(),
                    commit.getCommitShortInfo().getVerification().getPayload());
        }
    }

    @Test // issue 883
    public void commitDateNotNull() throws Exception {
        GHRepository repo = gitHub.getRepository("hub4j/github-api");
        GHCommit commit = repo.getCommit("ed4f9c8176866977677c99ac9668a8ce10231bc8");

        assertNotNull(commit.getCommitShortInfo().getAuthoredDate());
        assertNotNull(commit.getCommitShortInfo().getAuthor().getDate());
    }
}
