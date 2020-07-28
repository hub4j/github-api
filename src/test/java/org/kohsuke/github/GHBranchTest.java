package org.kohsuke.github;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;

public class GHBranchTest extends AbstractGitHubWireMockTest {
    private static final String BRANCH_1 = "testBranch1";
    private static final String BRANCH_2 = "testBranch2";

    private GHRepository repository;

    @Before
    public void before() throws Exception {
        repository = gitHub.getUser("jkalash").getRepository("github-api");
        cleanupBranches();
    }

    @After
    public void after() throws Exception {
        // cleanupBranches();
    }

    @Test
    public void testMergeBranch() throws Exception {
        String masterHead = repository.getRef("heads/master").getObject().getSha();
        createRefAndPostContent(BRANCH_1, masterHead);
        createRefAndPostContent(BRANCH_2, masterHead);

        Thread.sleep(1000);

        GHBranch otherBranch = repository.getBranch(BRANCH_2);
        String commitMessage = "merging " + BRANCH_2;
        GHCommit mergeCommit = repository.getBranch(BRANCH_2).merge(otherBranch, commitMessage);
        assertEquals(mergeCommit.getCommitShortInfo().getMessage(), mergeCommit);
    }

    private void createRefAndPostContent(String branchName, String sha) throws IOException {
        String refName = "refs/heads/" + branchName;
        if (repository.listRefs().toList().stream().map(GHRef::getRef).noneMatch(ref -> ref.equals(refName))) {
            repository.createRef(refName, sha);
        }
        repository.createContent().content(branchName).message(branchName).path(branchName).branch(branchName).commit();
    }

    private void cleanupBranches() throws Exception {
        if (!mockGitHub.isUseProxy()) {
            return;
        }

        String ref1 = "refs/heads/" + BRANCH_1;
        String ref2 = "refs/heads/" + BRANCH_2;
        for (GHRef ref : repository.listRefs().toList()) {
            if (ref.getRef().equals(ref1) || ref.getRef().equals(ref2)) {
                ref.delete();
            }
        }

        Thread.sleep(1000);
    }
}
