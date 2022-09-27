package org.kohsuke.github;

import org.junit.Test;

import static org.hamcrest.Matchers.*;

// TODO: Auto-generated Javadoc
/**
 * The Class GHBranchTest.
 */
public class GHBranchTest extends AbstractGitHubWireMockTest {
    private static final String BRANCH_1 = "testBranch1";
    private static final String BRANCH_2 = "testBranch2";

    private GHRepository repository;

    /**
     * Test merge branch.
     *
     * @throws Exception the exception
     */
    @Test
    public void testMergeBranch() throws Exception {
        repository = getTempRepository();

        String mainHead = repository.getRef("heads/main").getObject().getSha();
        String branchName1 = "refs/heads/" + BRANCH_1;
        repository.createRef(branchName1, mainHead);
        repository.createContent()
                .content(branchName1)
                .message(branchName1)
                .path(branchName1)
                .branch(branchName1)
                .commit();

        String branchName2 = "refs/heads/" + BRANCH_2;
        repository.createRef(branchName2, mainHead);
        GHBranch otherBranch = repository.getBranch(BRANCH_2);
        assertThat(otherBranch.getSHA1(), equalTo(mainHead));
        repository.createContent()
                .content(branchName2)
                .message(branchName2)
                .path(branchName2)
                .branch(branchName2)
                .commit();

        otherBranch = repository.getBranch(BRANCH_2);
        assertThat(otherBranch.getSHA1(), not(equalTo(mainHead)));
        assertThat(otherBranch.getOwner(), notNullValue());
        assertThat(otherBranch.getOwner().getFullName(), equalTo(repository.getFullName()));

        String commitMessage = "merging " + BRANCH_2;
        GHCommit mergeCommit = repository.getBranch(BRANCH_1).merge(otherBranch, commitMessage);
        assertThat(mergeCommit, notNullValue());
        assertThat(mergeCommit.getCommitShortInfo().getMessage(), equalTo(commitMessage));

        // Merging commit sha should work
        commitMessage = "merging from " + mergeCommit.getSHA1();
        GHBranch main = repository.getBranch("main");
        mergeCommit = main.merge(mergeCommit.getSHA1(), commitMessage);

        assertThat(mergeCommit, notNullValue());
        assertThat(mergeCommit.getCommitShortInfo().getMessage(), equalTo(commitMessage));

        mergeCommit = main.merge(mergeCommit.getSHA1(), commitMessage);
        // Should be null since all changes already merged
        assertThat(mergeCommit, nullValue());
    }
}
