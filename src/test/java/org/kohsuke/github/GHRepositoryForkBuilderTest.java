package org.kohsuke.github;

import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.util.Map;

import static org.hamcrest.Matchers.*;
import static org.mockito.Mockito.*;

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
        GHRepository updatedFork = forked;
        for (int i = 0; i < 10; i++) {
            Thread.sleep(3000);
            updatedFork = gitHub.getRepository(forked.getFullName());
            if (updatedFork.isFork()) {
                break;
            }
        }
        assertThat(updatedFork, notNullValue());
        assertThat(updatedFork.getName(), equalTo(expectedName));
        assertThat(updatedFork.isFork(), is(true));
        assertThat(updatedFork.getParent().getFullName(), equalTo(original.getFullName()));
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

        assertThat(forkedRepo.getName(), equalTo(newRepoName));
        verifyBasicForkProperties(repo, forkedRepo, newRepoName);
        verifyBranches(forkedRepo, false);

        forkedRepo.delete();
    }

    /**
     * Test create timeout message.
     */
    @Test
    public void testCreateTimeoutMessage() {
        GitHub mockGitHub = mock(GitHub.class);
        Requester mockRequester = mock(Requester.class);
        GHRepository mockRepo = mock(GHRepository.class);

        when(mockRepo.getFullName()).thenReturn("test/mock-repo");
        when(mockRepo.root()).thenReturn(mockGitHub);
        when(mockGitHub.createRequest()).thenReturn(mockRequester);

        GHRepositoryForkBuilder builder = new GHRepositoryForkBuilder(mockRepo);

        assertThat(builder.createTimeoutMessage(),
                equalTo("test/mock-repo was forked but can't find the new repository"));

        GHOrganization org = mock(GHOrganization.class);
        when(org.getLogin()).thenReturn(TARGET_ORG);
        builder.organization(org);

        assertThat(builder.createTimeoutMessage(),
                equalTo("test/mock-repo was forked into " + TARGET_ORG + " but can't find the new repository"));

        String customName = "custom-fork-name";
        builder.name(customName);

        assertThat(builder.createTimeoutMessage(),
                equalTo("test/mock-repo was forked into " + TARGET_ORG + " with name " + customName
                        + " but can't find the new repository"));
    }

    /**
     * Test timeout.
     */
    @Test
    public void testTimeout() {
        class TrackingSleepBuilder extends GHRepositoryForkBuilder {
            int sleepCount = 0;
            int lastSleepMillis = 0;

            TrackingSleepBuilder(GHRepository repo) {
                super(repo);
            }

            @Override
            protected void sleep(int millis) throws IOException {
                sleepCount++;
                lastSleepMillis = millis;

            }
        }

        int originalInterval = GHRepositoryForkBuilder.FORK_RETRY_INTERVAL;
        try {
            GHRepositoryForkBuilder.FORK_RETRY_INTERVAL = 100;

            TrackingSleepBuilder builder = new TrackingSleepBuilder(repo);
            try {
                builder.create();
                fail("Expected IOException for timeout");
            } catch (IOException e) {
                System.out.println("Exception message: " + e.getMessage());
                assertThat(builder.sleepCount, equalTo(10));
                assertThat(builder.lastSleepMillis, equalTo(100));
                assertThat(e.getMessage(), containsString("but can't find the new repository"));
            }
        } finally {
            GHRepositoryForkBuilder.FORK_RETRY_INTERVAL = originalInterval;
        }
    }

}
