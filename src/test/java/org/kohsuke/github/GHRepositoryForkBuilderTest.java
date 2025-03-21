package org.kohsuke.github;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.io.InterruptedIOException;
import java.time.Duration;
import java.util.Map;

import static org.awaitility.Awaitility.await;
import static org.hamcrest.Matchers.*;

// TODO: Auto-generated Javadoc

/**
 * The Class GHRepositoryForkBuilderTest.
 */
public class GHRepositoryForkBuilderTest extends AbstractGitHubWireMockTest {
    private GHRepository repo;
    private static final String TARGET_ORG = "nts-api-test-org";
    private int originalInterval;

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

        originalInterval = GHRepositoryForkBuilder.FORK_RETRY_INTERVAL;
        GHRepositoryForkBuilder.FORK_RETRY_INTERVAL = 100;

        if (mockGitHub.isUseProxy()) {
            GitHub github = getNonRecordingGitHub();
            GHRepository repo = github.getRepository(this.repo.getFullName());
            String defaultBranch = repo.getDefaultBranch();
            GHRef mainRef = repo.getRef("heads/" + defaultBranch);
            String mainSha = mainRef.getObject().getSha();

            String[] branchNames = { "test-branch1", "test-branch2", "test-branch3" };
            for (String branchName : branchNames) {
                repo.createRef("refs/heads/" + branchName, mainSha);
            }
        }
    }

    /**
     * Tear down.
     */
    @After
    public void tearDown() {
        GHRepositoryForkBuilder.FORK_RETRY_INTERVAL = originalInterval;
    }

    /**
     * The type Test fork builder.
     */
    class TestForkBuilder extends GHRepositoryForkBuilder {
        /**
         * The Sleep count.
         */
        int sleepCount = 0;
        /**
         * The Last sleep millis.
         */
        int lastSleepMillis = 0;

        /**
         * Instantiates a new Test fork builder.
         *
         * @param repo
         *            the repo
         */
        TestForkBuilder(GHRepository repo) {
            super(repo);
        }

        @Override
        void sleep(int millis) throws IOException {
            sleepCount++;
            lastSleepMillis = millis;
            try {
                if (mockGitHub.isUseProxy()) {
                    Thread.sleep(millis);
                } else {
                    Thread.sleep(1);
                }
            } catch (InterruptedException e) {
                throw (IOException) new InterruptedIOException().initCause(e);
            }
        }
    }

    private TestForkBuilder createBuilder() {
        return new TestForkBuilder(repo);
    }

    private void verifyBasicForkProperties(GHRepository original, GHRepository forked, String expectedName)
            throws IOException {
        GHRepository updatedFork = forked;

        await().atMost(Duration.ofSeconds(30))
                .pollInterval(Duration.ofSeconds(3))
                .until(() -> gitHub.getRepository(forked.getFullName()).isFork());

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
        // cover the deprecated fork() method
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
        // equivalent to the deprecated forkTo() method
        TestForkBuilder builder = createBuilder();
        GHRepository forkedRepo = builder.organization(targetOrg).create();

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
        TestForkBuilder builder = createBuilder();
        GHRepository forkedRepo = builder.defaultBranchOnly(true).create();

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
        TestForkBuilder builder = createBuilder();
        GHRepository forkedRepo = builder.name(newRepoName).create();

        assertThat(forkedRepo.getName(), equalTo(newRepoName));
        verifyBasicForkProperties(repo, forkedRepo, newRepoName);
        verifyBranches(forkedRepo, false);

        forkedRepo.delete();
    }

    /**
     * Test timeout message and sleep count.
     */
    @Test
    public void testTimeoutMessage() {
        // For re-recording, use line below to create successful fork test copy, then comment it out and modify json
        // response to 404
        // repo.createFork().name("test-message").create();

        String newRepoName = "test-message";
        try {

            TestForkBuilder builder = createBuilder();
            try {
                builder.name(newRepoName).create();
                fail("Expected IOException for timeout");
            } catch (IOException e) {
                assertThat(builder.sleepCount, equalTo(10));
                assertThat(builder.lastSleepMillis, equalTo(100));
                assertThat(e.getMessage(),
                        allOf(containsString("was forked"),
                                containsString("with name " + newRepoName),
                                containsString("but can't find the new repository")));
            }
        } finally {
            GHRepositoryForkBuilder.FORK_RETRY_INTERVAL = originalInterval;
        }
    }

    /**
     * Test timeout org message.
     *
     * @throws Exception
     *             the exception
     */
    @Test
    public void testTimeoutOrgMessage() throws Exception {
        GHOrganization targetOrg = gitHub.getOrganization(TARGET_ORG);
        // For re-recording, use line below to create successful fork test copy, then comment it out and modify json
        // response to 404
        // repo.createFork().organization(targetOrg).create();
        try {
            repo.createFork().organization(targetOrg).create();
            fail("Expected IOException for timeout");
        } catch (IOException e) {
            assertThat(e.getMessage(),
                    allOf(containsString("was forked"),
                            containsString("into " + TARGET_ORG),
                            containsString("but can't find the new repository")));
        }
    }

    /**
     * Test sleep.
     *
     * @throws Exception
     *             the exception
     */
    @Test
    public void testSleep() throws Exception {
        GHRepositoryForkBuilder builder = new GHRepositoryForkBuilder(repo);
        Thread.currentThread().interrupt();

        try {
            builder.sleep(100);
            fail("Expected InterruptedIOException");
        } catch (InterruptedIOException e) {
            assertThat(e, instanceOf(InterruptedIOException.class));
            assertThat(e.getCause(), instanceOf(InterruptedException.class));
        }
    }

}
