package org.kohsuke.github;

import com.google.common.collect.Iterables;
import org.junit.Test;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import static org.hamcrest.Matchers.*;

// TODO: Auto-generated Javadoc
/**
 * The Class CommitTest.
 *
 * @author Kohsuke Kawaguchi
 */
public class CommitTest extends AbstractGitHubWireMockTest {

    /**
     * Last status.
     *
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     */
    @Test // issue 152
    public void lastStatus() throws IOException {
        GHTag t = gitHub.getRepository("stapler/stapler").listTags().iterator().next();
        assertThat(t.getCommit().getLastStatus(), notNullValue());
    }

    /**
     * List files.
     *
     * @throws Exception
     *             the exception
     */
    @Test // issue 230
    public void listFiles() throws Exception {
        GHRepository repo = gitHub.getRepository("stapler/stapler");
        PagedIterable<GHCommit> commits = repo.queryCommits().path("pom.xml").list();
        for (GHCommit commit : Iterables.limit(commits, 10)) {
            GHCommit expected = repo.getCommit(commit.getSHA1());
            assertThat(commit.getFiles().size(), equalTo(expected.getFiles().size()));
        }
    }

    /**
     * Test query commits.
     *
     * @throws Exception
     *             the exception
     */
    @Test
    public void testQueryCommits() throws Exception {
        List<String> sha1 = new ArrayList<String>();
        List<GHCommit> commits = gitHub.getUser("jenkinsci")
                .getRepository("jenkins")
                .queryCommits()
                .since(1199174400000L)
                .until(1201852800000L)
                .path("pom.xml")
                .pageSize(100)
                .list()
                .toList();

        assertThat(commits.size(), equalTo(29));

        GHCommit commit = commits.get(0);
        assertThat(commit.getSHA1(), equalTo("1cccddb22e305397151b2b7b87b4b47d74ca337b"));

        commits = gitHub.getUser("jenkinsci")
                .getRepository("jenkins")
                .queryCommits()
                .since(new Date(1199174400000L))
                .until(new Date(1201852800000L))
                .path("pom.xml")
                .pageSize(100)
                .list()
                .toList();

        assertThat(commits.get(0).getSHA1(), equalTo("1cccddb22e305397151b2b7b87b4b47d74ca337b"));
        assertThat(commits.get(15).getSHA1(), equalTo("a5259970acaec9813e2a12a91f37dfc7871a5ef5"));
        assertThat(commits.size(), equalTo(29));

        commits = gitHub.getUser("jenkinsci")
                .getRepository("jenkins")
                .queryCommits()
                .since(new Date(1199174400000L))
                .until(new Date(1201852800000L))
                .path("pom.xml")
                .from("a5259970acaec9813e2a12a91f37dfc7871a5ef5")
                .list()
                .toList();

        assertThat(commits.get(0).getSHA1(), equalTo("a5259970acaec9813e2a12a91f37dfc7871a5ef5"));
        assertThat(commits.size(), equalTo(14));

        commits = gitHub.getUser("jenkinsci")
                .getRepository("jenkins")
                .queryCommits()
                .until(new Date(1201852800000L))
                .path("pom.xml")
                .author("kohsuke")
                .list()
                .toList();

        assertThat(commits, is(empty()));

        commits = gitHub.getUser("jenkinsci")
                .getRepository("jenkins")
                .queryCommits()
                .until(new Date(1201852800000L))
                .path("pom.xml")
                .pageSize(100)
                .author("kohsuke@71c3de6d-444a-0410-be80-ed276b4c234a")
                .list()
                .toList();

        assertThat(commits.size(), equalTo(266));

        commits = gitHub.getUser("jenkinsci")
                .getRepository("jenkins")
                .queryCommits()
                .path("pom.xml")
                .pageSize(100)
                .author("kohsuke@71c3de6d-444a-0410-be80-ed276b4c234a")
                .list()
                .toList();

        assertThat(commits.size(), equalTo(648));

    }

    /**
     * List pull requests of not included commit.
     *
     * @throws Exception
     *             the exception
     */
    @Test
    public void listPullRequestsOfNotIncludedCommit() throws Exception {
        GHRepository repo = gitHub.getOrganization("hub4j-test-org").getRepository("listPrsListHeads");

        GHCommit commit = repo.getCommit("f66f7ca691ace6f4a9230292efb932b49214d72c");

        assertThat("The commit is supposed to be not part of any pull request",
                commit.listPullRequests().toList().isEmpty());
    }

    /**
     * List pull requests.
     *
     * @throws Exception
     *             the exception
     */
    @Test
    public void listPullRequests() throws Exception {
        GHRepository repo = gitHub.getOrganization("hub4j-test-org").getRepository("listPrsListHeads");
        Integer prNumber = 2;

        GHCommit commit = repo.getCommit("6b9956fe8c3d030dbc49c9d4c4166b0ceb4198fc");

        List<GHPullRequest> listedPrs = commit.listPullRequests().toList();

        assertThat(1, equalTo(listedPrs.size()));

        assertThat("Pull request " + prNumber + " not found by searching from commit.",
                listedPrs.stream().findFirst().filter(it -> it.getNumber() == prNumber).isPresent());
    }

    /**
     * List pull requests of commit with 2 pull requests.
     *
     * @throws Exception
     *             the exception
     */
    @Test
    public void listPullRequestsOfCommitWith2PullRequests() throws Exception {
        GHRepository repo = gitHub.getOrganization("hub4j-test-org").getRepository("listPrsListHeads");
        Integer[] expectedPrs = new Integer[]{ 1, 2 };

        GHCommit commit = repo.getCommit("442aa213f924a5984856f16e52a18153aaf41ad3");

        List<GHPullRequest> listedPrs = commit.listPullRequests().toList();

        assertThat(2, equalTo(listedPrs.size()));

        listedPrs.stream()
                .forEach(pr -> assertThat("PR#" + pr.getNumber() + " not expected to be matched.",
                        Arrays.stream(expectedPrs).anyMatch(prNumber -> prNumber.equals(pr.getNumber()))));
    }

    /**
     * List branches where head.
     *
     * @throws Exception
     *             the exception
     */
    @Test
    public void listBranchesWhereHead() throws Exception {
        GHRepository repo = gitHub.getOrganization("hub4j-test-org").getRepository("listPrsListHeads");

        GHCommit commit = repo.getCommit("ab92e13c0fc844fd51a379a48a3ad0b18231215c");

        assertThat("Commit which was supposed to be HEAD in the \"main\" branch was not found.",
                commit.listBranchesWhereHead()
                        .toList()
                        .stream()
                        .findFirst()
                        .filter(it -> it.getName().equals("main"))
                        .isPresent());
    }

    /**
     * List branches where head 2 heads.
     *
     * @throws Exception
     *             the exception
     */
    @Test
    public void listBranchesWhereHead2Heads() throws Exception {
        GHRepository repo = gitHub.getOrganization("hub4j-test-org").getRepository("listPrsListHeads");

        GHCommit commit = repo.getCommit("ab92e13c0fc844fd51a379a48a3ad0b18231215c");

        assertThat("Commit which was supposed to be HEAD in 2 branches was not found as such.",
                commit.listBranchesWhereHead().toList().size(),
                equalTo(2));
    }

    /**
     * List branches where head of commit with head nowhere.
     *
     * @throws Exception
     *             the exception
     */
    @Test
    public void listBranchesWhereHeadOfCommitWithHeadNowhere() throws Exception {
        GHRepository repo = gitHub.getOrganization("hub4j-test-org").getRepository("listPrsListHeads");

        GHCommit commit = repo.getCommit("7460916bfb8e9966d6b9d3e8ae378c82c6b8e43e");

        assertThat("Commit which was not supposed to be HEAD in any branch was found as HEAD.",
                commit.listBranchesWhereHead().toList().isEmpty());
    }

    /**
     * Commit signature verification.
     *
     * @throws Exception
     *             the exception
     */
    @Test // issue 737
    public void commitSignatureVerification() throws Exception {
        GHRepository repo = gitHub.getRepository("stapler/stapler");
        PagedIterable<GHCommit> commits = repo.queryCommits().path("pom.xml").list();
        for (GHCommit commit : Iterables.limit(commits, 10)) {
            GHCommit expected = repo.getCommit(commit.getSHA1());
            assertThat(commit.getCommitShortInfo().getVerification().isVerified(),
                    equalTo(expected.getCommitShortInfo().getVerification().isVerified()));
            assertThat(commit.getCommitShortInfo().getVerification().getReason(),
                    equalTo(expected.getCommitShortInfo().getVerification().getReason()));
            assertThat(commit.getCommitShortInfo().getVerification().getSignature(),
                    equalTo(expected.getCommitShortInfo().getVerification().getSignature()));
            assertThat(commit.getCommitShortInfo().getVerification().getPayload(),
                    equalTo(expected.getCommitShortInfo().getVerification().getPayload()));
        }
    }

    /**
     * Commit date not null.
     *
     * @throws Exception
     *             the exception
     */
    @Test // issue 883
    public void commitDateNotNull() throws Exception {
        GHRepository repo = gitHub.getRepository("hub4j/github-api");
        GHCommit commit = repo.getCommit("865a49d2e86c24c5777985f0f103e975c4b765b9");

        assertThat(commit.getCommitShortInfo().getAuthoredDate().toInstant().getEpochSecond(), equalTo(1609207093L));
        assertThat(commit.getCommitShortInfo().getAuthoredDate(),
                equalTo(commit.getCommitShortInfo().getAuthor().getDate()));
        assertThat(commit.getCommitShortInfo().getCommitDate().toInstant().getEpochSecond(), equalTo(1609207652L));
        assertThat(commit.getCommitShortInfo().getCommitDate(),
                equalTo(commit.getCommitShortInfo().getCommitter().getDate()));
    }
}
