package org.kohsuke.github;

import org.junit.Test;

import java.io.IOException;

import static org.hamcrest.Matchers.*;

public class GHBranchTest extends AbstractGitHubWireMockTest {
    private static final String BRANCH_1 = "testBranch1";
    private static final String BRANCH_2 = "testBranch2";

    private GHRepository repository;

    @Test
    public void testMergeBranch() throws Exception {
        repository = getTempRepository();

        String masterHead = repository.getRef("heads/master").getObject().getSha();
        createRefAndPostContent(BRANCH_1, masterHead);
        createRefAndPostContent(BRANCH_2, masterHead);

        GHBranch otherBranch = repository.getBranch(BRANCH_2);
        String commitMessage = "merging " + BRANCH_2;
        GHCommit mergeCommit = repository.getBranch(BRANCH_1).merge(otherBranch, commitMessage);
        assertThat(mergeCommit, notNullValue());
        assertThat(mergeCommit.getCommitShortInfo().getMessage(), equalTo(commitMessage));

        // Merging commit sha should work
        commitMessage = "merging from " + mergeCommit.getSHA1();
        GHBranch master = repository.getBranch("master");
        mergeCommit = master.merge(mergeCommit.getSHA1(), commitMessage);

        assertThat(mergeCommit, notNullValue());
        assertThat(mergeCommit.getCommitShortInfo().getMessage(), equalTo(commitMessage));

        mergeCommit = master.merge(mergeCommit.getSHA1(), commitMessage);
        // Should be null since all changes already merged
        assertThat(mergeCommit, nullValue());
    }

    private void createRefAndPostContent(String branchName, String sha) throws IOException {
        String refName = "refs/heads/" + branchName;
        repository.createRef(refName, sha);
        repository.createContent().content(branchName).message(branchName).path(branchName).branch(branchName).commit();
    }
}
