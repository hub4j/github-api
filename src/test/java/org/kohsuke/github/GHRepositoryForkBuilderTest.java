package org.kohsuke.github;

import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.util.Map;

import static org.hamcrest.Matchers.*;

// TODO: Auto-generated Javadoc

/**
 * The Class GHRepositoryForkBuilderTest.
 */
public class GHRepositoryForkBuilderTest extends AbstractGitHubWireMockTest {
    private GHRepository repo;
    private static final String TARGET_ORG = "nts-api-test-org";

    /**
     * Instantiates a new Gh repository fork builder test.
     */
    public GHRepositoryForkBuilderTest() {
    }

    /**
     * Sets up.
     *
     * @throws Exception
     *             the exception
     */
    @Before
    public void setUp() throws Exception {
        repo = getTempRepository();

        String defaultBranch = repo.getDefaultBranch();
        GHRef mainRef = repo.getRef("heads/" + defaultBranch);
        String mainSha = mainRef.getObject().getSha();

        String[] branchNames = { "test-branch1", "test-branch2", "test-branch3" };
        for (String branchName : branchNames) {
            repo.createRef("refs/heads/" + branchName, mainSha);
        }
    }

    private void verifyBasicForkProperties(GHRepository original, GHRepository forked, String expectedName)
            throws IOException, InterruptedException {
        assertThat(forked, notNullValue());
        assertThat(forked.getName(), equalTo(expectedName));
        assertThat(forked.isFork(), is(true));
        assertThat(forked.getParent().getFullName(), equalTo(original.getFullName()));

        GHRepository updatedFork = forked;
        for (int i = 0; i < 10; i++) {
            Thread.sleep(3000);
            updatedFork = gitHub.getRepository(forked.getFullName());
            if (updatedFork.isFork()) {
                break;
            }
        }
    }

    private void verifyBranches(GHRepository forked, boolean defaultBranchOnly) throws IOException {
        Map<String, GHBranch> branches = forked.getBranches();
        if (defaultBranchOnly) {
            assertThat(branches.size(), equalTo(1));
        } else {
            assertThat(branches.size(), greaterThan(1));
        }
        assertThat(branches.containsKey(forked.getDefaultBranch()), is(true));
    }

    /**
     * Test fork.
     *
     * @throws Exception
     *             the exception
     */
    @Test
    public void testFork() throws Exception {
        // GHRepository forkedRepo = repo.createFork().create();
        GHRepository forkedRepo = repo.fork();

        Thread.sleep(3000);
        verifyBasicForkProperties(repo, forkedRepo, repo.getName());
        verifyBranches(forkedRepo, false);

        forkedRepo.delete();
    }

    /**
     * Test fork to org.
     *
     * @throws Exception
     *             the exception
     */
    @Test
    public void testForkToOrg() throws Exception {
        GHOrganization targetOrg = gitHub.getOrganization(TARGET_ORG);
        // GHRepository forkedRepo = repo.createFork().organization(targetOrg).create();
        GHRepository forkedRepo = repo.forkTo(targetOrg);

        Thread.sleep(3000);
        verifyBasicForkProperties(repo, forkedRepo, repo.getName());
        verifyBranches(forkedRepo, false);

        forkedRepo.delete();
    }

    /**
     * Test fork default branch only.
     *
     * @throws Exception
     *             the exception
     */
    @Test
    public void testForkDefaultBranchOnly() throws Exception {
        GHRepository forkedRepo = repo.createFork().defaultBranchOnly(true).create();

        Thread.sleep(3000);
        verifyBasicForkProperties(repo, forkedRepo, repo.getName());
        verifyBranches(forkedRepo, true);

        forkedRepo.delete();
    }

    /**
     * Test fork changed name.
     *
     * @throws Exception
     *             the exception
     */
    @Test
    public void testForkChangedName() throws Exception {
        String newRepoName = "test-fork-with-new-name";
        GHRepository forkedRepo = repo.createFork().name(newRepoName).create();

        Thread.sleep(3000);
        assertThat(forkedRepo.getName(), equalTo(newRepoName));
        verifyBasicForkProperties(repo, forkedRepo, newRepoName);
        verifyBranches(forkedRepo, false);

        forkedRepo.delete();
    }

}
