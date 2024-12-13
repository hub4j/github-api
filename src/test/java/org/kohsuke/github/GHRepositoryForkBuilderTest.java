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
        repo = gitHub.getRepository("TestDitto/test-repo");
        if (repo == null) {
            throw new IllegalStateException("Failed to initialize repository");
        }
    }

    private void verifyBasicForkProperties(GHRepository original,
            GHRepository forked,
            String expectedOwner,
            String expectedName) throws IOException {
        assertThat(forked, notNullValue());
        assertThat(forked.getName(), equalTo(expectedName));
        assertThat(forked.getOwner().getLogin(), equalTo(expectedOwner));
        assertThat(forked.isFork(), is(true));
        assertThat(forked.getParent().getFullName(), equalTo(original.getFullName()));
    }

    private void verifyBranches(GHRepository original, GHRepository forked, boolean defaultBranchOnly)
            throws IOException {
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

        Thread.sleep(30000);
        verifyBasicForkProperties(repo, forkedRepo, gitHub.getMyself().getLogin(), repo.getName());
        verifyBranches(repo, forkedRepo, false);

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

        Thread.sleep(30000);
        verifyBasicForkProperties(repo, forkedRepo, targetOrg.getLogin(), repo.getName());
        verifyBranches(repo, forkedRepo, false);

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

        Thread.sleep(30000);
        verifyBasicForkProperties(repo, forkedRepo, gitHub.getMyself().getLogin(), repo.getName());
        verifyBranches(repo, forkedRepo, true);

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
        String newRepoName = "new-fork";
        GHRepository forkedRepo = repo.createFork().name(newRepoName).create();

        Thread.sleep(30000);
        assertThat(forkedRepo.getName(), equalTo(newRepoName));
        verifyBasicForkProperties(repo, forkedRepo, gitHub.getMyself().getLogin(), newRepoName);
        verifyBranches(repo, forkedRepo, false);

        forkedRepo.delete();
    }

}
