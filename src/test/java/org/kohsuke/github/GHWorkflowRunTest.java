package org.kohsuke.github;

import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import org.awaitility.Awaitility;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.kohsuke.github.GHWorkflowRun.Conclusion;
import org.kohsuke.github.GHWorkflowRun.Status;

import java.io.IOException;
import java.time.Duration;
import java.util.List;

public class GHWorkflowRunTest extends AbstractGitHubWireMockTest {

    private static final String REPO_NAME = "hub4j-test-org/GHWorkflowRunTest";
    private static final String MAIN_BRANCH = "main";
    private static final String SECOND_BRANCH = "second-branch";

    private static final String FAST_WORKFLOW_PATH = "fast-workflow.yml";
    private static final String FAST_WORKFLOW_NAME = "Fast workflow";

    private static final String SLOW_WORKFLOW_PATH = "slow-workflow.yml";
    private static final String SLOW_WORKFLOW_NAME = "Slow workflow";

    private GHRepository repo;

    private Duration atLeast;
    private Duration pollInterval;
    private Duration atMost;

    private long cancelledWorkflowRunId;
    private long workflowRunIdToDelete;

    @Override
    protected WireMockConfiguration getWireMockOptions() {
        return super.getWireMockOptions().extensions(templating.newResponseTransformer());
    }

    @Before
    public void setUp() throws Exception {
        repo = gitHub.getRepository(REPO_NAME);

        if (mockGitHub.isUseProxy()) {
            atLeast = Duration.ofSeconds(5);
            pollInterval = Duration.ofSeconds(5);
            atMost = Duration.ofSeconds(60);
        } else {
            atLeast = Duration.ofMillis(20);
            pollInterval = Duration.ofMillis(20);
            atMost = Duration.ofMillis(240);
        }
    }

    @Test
    public void testManualRunAndBasicInformation() throws IOException {
        GHWorkflow workflow = repo.getWorkflow(FAST_WORKFLOW_PATH);

        long latestPreexistingWorkflowRunId = getLatestPreexistingWorkflowRunId();

        workflow.dispatch(MAIN_BRANCH);

        // now that we have triggered a workflow run, we can try to get the latest info from the run
        Awaitility.await().atLeast(atLeast).pollInterval(pollInterval).atMost(atMost).until(() -> {
            List<GHWorkflowRun> workflowRuns = getLatestWorkflowRuns(MAIN_BRANCH, Status.COMPLETED);
            for (GHWorkflowRun workflowRun : workflowRuns) {
                if (workflowRun.getName().equals(FAST_WORKFLOW_NAME)
                        && workflowRun.getId() > latestPreexistingWorkflowRunId) {
                    assertEquals(workflow.getId(), workflowRun.getWorkflowId());
                    assertTrue(workflowRun.getUrl().getPath().contains("/actions/runs/"));
                    assertTrue(workflowRun.getHtmlUrl().getPath().contains("/actions/runs/"));
                    assertTrue(workflowRun.getJobsUrl().getPath().endsWith("/jobs"));
                    assertTrue(workflowRun.getLogsUrl().getPath().endsWith("/logs"));
                    assertTrue(workflowRun.getCheckSuiteUrl().getPath().contains("/check-suites/"));
                    assertTrue(workflowRun.getArtifactsUrl().getPath().endsWith("/artifacts"));
                    assertTrue(workflowRun.getCancelUrl().getPath().endsWith("/cancel"));
                    assertTrue(workflowRun.getRerunUrl().getPath().endsWith("/rerun"));
                    assertTrue(workflowRun.getWorkflowUrl().getPath().contains("/actions/workflows/"));
                    assertEquals(MAIN_BRANCH, workflowRun.getHeadBranch());
                    assertNotNull(workflowRun.getHeadCommit().getId());
                    assertNotNull(workflowRun.getHeadCommit().getTreeId());
                    assertNotNull(workflowRun.getHeadCommit().getMessage());
                    assertNotNull(workflowRun.getHeadCommit().getTimestamp());
                    assertNotNull(workflowRun.getHeadCommit().getAuthor().getEmail());
                    assertNotNull(workflowRun.getHeadCommit().getCommitter().getEmail());
                    assertEquals(GHEvent.WORKFLOW_DISPATCH, workflowRun.getEvent());
                    assertEquals(Status.COMPLETED, workflowRun.getStatus());
                    assertEquals(Conclusion.SUCCESS, workflowRun.getConclusion());
                    assertNotNull(workflowRun.getHeadSha());

                    return true;
                }
            }
            return false;
        });
    }

    @Test
    public void testCancelAndRerun() throws IOException {
        GHWorkflow workflow = repo.getWorkflow(SLOW_WORKFLOW_PATH);

        long latestPreexistingWorkflowRunId = getLatestPreexistingWorkflowRunId();

        workflow.dispatch(MAIN_BRANCH);

        // now that we have triggered the workflow run, we will wait until it's in progress and then cancel it
        Awaitility.await().atLeast(atLeast).pollInterval(pollInterval).atMost(atMost).until(() -> {
            List<GHWorkflowRun> workflowRuns = getLatestWorkflowRuns(MAIN_BRANCH, Status.IN_PROGRESS);
            for (GHWorkflowRun workflowRun : workflowRuns) {
                if (workflowRun.getName().equals(SLOW_WORKFLOW_NAME)
                        && workflowRun.getId() > latestPreexistingWorkflowRunId) {
                    assertNotNull(workflowRun.getId());

                    workflowRun.cancel();
                    cancelledWorkflowRunId = workflowRun.getId();
                    return true;
                }
            }
            return false;
        });

        // let's check that it has been properly cancelled
        Awaitility.await().atLeast(atLeast).pollInterval(pollInterval).atMost(atMost).until(() -> {
            GHWorkflowRun workflowRun = repo.getWorkflowRun(cancelledWorkflowRunId);
            if (workflowRun.getStatus() == Status.COMPLETED && workflowRun.getConclusion() == Conclusion.CANCELLED) {
                return true;
            }

            return false;
        });

        // now let's rerun it
        GHWorkflowRun cancelledWorkflowRun = repo.getWorkflowRun(cancelledWorkflowRunId);
        cancelledWorkflowRun.rerun();

        // let's check that it has been rerun
        Awaitility.await().atLeast(atLeast).pollInterval(pollInterval).atMost(atMost).until(() -> {
            GHWorkflowRun rerunWorkflowRun = repo.getWorkflowRun(cancelledWorkflowRunId);
            return rerunWorkflowRun.getStatus() == Status.IN_PROGRESS;
        });

        // cancel it again
        cancelledWorkflowRun.cancel();
    }

    @Test
    public void testDelete() throws IOException {
        GHWorkflow workflow = repo.getWorkflow(FAST_WORKFLOW_PATH);

        long latestPreexistingWorkflowRunId = getLatestPreexistingWorkflowRunId();

        workflow.dispatch(MAIN_BRANCH);

        // now that we have triggered a workflow run, we can try to get the latest info from the run
        Awaitility.await().atLeast(atLeast).pollInterval(pollInterval).atMost(atMost).until(() -> {
            List<GHWorkflowRun> workflowRuns = getLatestWorkflowRuns(MAIN_BRANCH, Status.COMPLETED);
            for (GHWorkflowRun workflowRun : workflowRuns) {
                if (workflowRun.getName().equals(FAST_WORKFLOW_NAME)
                        && workflowRun.getId() > latestPreexistingWorkflowRunId) {
                    assertNotNull(workflowRun.getId());

                    workflowRunIdToDelete = workflowRun.getId();

                    return true;
                }
            }
            return false;
        });

        GHWorkflowRun workflowRunToDelete = repo.getWorkflowRun(workflowRunIdToDelete);
        workflowRunToDelete.delete();

        try {
            repo.getWorkflowRun(workflowRunIdToDelete);
            Assert.fail("The workflow " + workflowRunIdToDelete + " should have been deleted.");
        } catch (GHFileNotFoundException e) {
            // success
        }
    }

    @Test
    public void testSearchOnBranch() throws IOException {
        GHWorkflow workflow = repo.getWorkflow(FAST_WORKFLOW_PATH);

        long latestPreexistingWorkflowRunId = getLatestPreexistingWorkflowRunId();

        workflow.dispatch(SECOND_BRANCH);

        // now that we have triggered a workflow run, we can try to get the latest info from the run
        Awaitility.await().atLeast(atLeast).pollInterval(pollInterval).atMost(atMost).until(() -> {
            List<GHWorkflowRun> workflowRuns = getLatestWorkflowRuns(SECOND_BRANCH, Status.COMPLETED);
            for (GHWorkflowRun workflowRun : workflowRuns) {
                if (workflowRun.getName().equals(FAST_WORKFLOW_NAME)
                        && workflowRun.getId() > latestPreexistingWorkflowRunId) {
                    assertEquals(workflow.getId(), workflowRun.getWorkflowId());
                    assertEquals(SECOND_BRANCH, workflowRun.getHeadBranch());
                    assertEquals(GHEvent.WORKFLOW_DISPATCH, workflowRun.getEvent());
                    assertEquals(Status.COMPLETED, workflowRun.getStatus());
                    assertEquals(Conclusion.SUCCESS, workflowRun.getConclusion());

                    return true;
                }
            }
            return false;
        });
    }

    private long getLatestPreexistingWorkflowRunId() {
        return repo.queryWorkflowRuns().list().withPageSize(1).iterator().next().getId();
    }

    private List<GHWorkflowRun> getLatestWorkflowRuns(String branch, Status status) {
        return repo.queryWorkflowRuns()
                .branch(branch)
                .status(status)
                .event(GHEvent.WORKFLOW_DISPATCH)
                .list()
                .withPageSize(20)
                .iterator()
                .nextPage();
    }
}
