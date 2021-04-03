package org.kohsuke.github;

import org.awaitility.Awaitility;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.kohsuke.github.GHWorkflowRun.Conclusion;
import org.kohsuke.github.GHWorkflowRun.Status;

import java.io.IOException;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Scanner;
import java.util.function.Function;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import static org.hamcrest.Matchers.*;

public class GHWorkflowRunTest extends AbstractGitHubWireMockTest {

    private static final String REPO_NAME = "hub4j-test-org/GHWorkflowRunTest";
    private static final String MAIN_BRANCH = "main";
    private static final String SECOND_BRANCH = "second-branch";

    private static final String FAST_WORKFLOW_PATH = "fast-workflow.yml";
    private static final String FAST_WORKFLOW_NAME = "Fast workflow";

    private static final String SLOW_WORKFLOW_PATH = "slow-workflow.yml";
    private static final String SLOW_WORKFLOW_NAME = "Slow workflow";

    private static final String ARTIFACTS_WORKFLOW_PATH = "artifacts-workflow.yml";
    private static final String ARTIFACTS_WORKFLOW_NAME = "Artifacts workflow";

    private GHRepository repo;

    @Before
    public void setUp() throws Exception {
        repo = gitHub.getRepository(REPO_NAME);
    }

    @Test
    public void testManualRunAndBasicInformation() throws IOException {
        GHWorkflow workflow = repo.getWorkflow(FAST_WORKFLOW_PATH);

        long latestPreexistingWorkflowRunId = getLatestPreexistingWorkflowRunId();

        workflow.dispatch(MAIN_BRANCH);

        await((nonRecordingRepo) -> getWorkflowRun(nonRecordingRepo,
                FAST_WORKFLOW_NAME,
                MAIN_BRANCH,
                Status.COMPLETED,
                latestPreexistingWorkflowRunId).isPresent());

        GHWorkflowRun workflowRun = getWorkflowRun(FAST_WORKFLOW_NAME,
                MAIN_BRANCH,
                Status.COMPLETED,
                latestPreexistingWorkflowRunId).orElseThrow(
                        () -> new IllegalStateException("We must have a valid workflow run starting from here"));

        assertEquals(workflow.getId(), workflowRun.getWorkflowId());
        assertNotNull(workflowRun.getId());
        assertNotNull(workflowRun.getNodeId());
        assertEquals(REPO_NAME, workflowRun.getRepository().getFullName());
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
    }

    @Test
    public void testCancelAndRerun() throws IOException {
        GHWorkflow workflow = repo.getWorkflow(SLOW_WORKFLOW_PATH);

        long latestPreexistingWorkflowRunId = getLatestPreexistingWorkflowRunId();

        workflow.dispatch(MAIN_BRANCH);

        // now that we have triggered the workflow run, we will wait until it's in progress and then cancel it
        await((nonRecordingRepo) -> getWorkflowRun(nonRecordingRepo,
                SLOW_WORKFLOW_NAME,
                MAIN_BRANCH,
                Status.IN_PROGRESS,
                latestPreexistingWorkflowRunId).isPresent());

        GHWorkflowRun workflowRun = getWorkflowRun(SLOW_WORKFLOW_NAME,
                MAIN_BRANCH,
                Status.IN_PROGRESS,
                latestPreexistingWorkflowRunId).orElseThrow(
                        () -> new IllegalStateException("We must have a valid workflow run starting from here"));

        assertNotNull(workflowRun.getId());

        workflowRun.cancel();
        long cancelledWorkflowRunId = workflowRun.getId();

        // let's wait until it's completed
        await((nonRecordingRepo) -> getWorkflowRunStatus(nonRecordingRepo, cancelledWorkflowRunId) == Status.COMPLETED);

        // let's check that it has been properly cancelled
        workflowRun = repo.getWorkflowRun(cancelledWorkflowRunId);
        assertEquals(Conclusion.CANCELLED, workflowRun.getConclusion());

        // now let's rerun it
        workflowRun.rerun();

        // let's check that it has been rerun
        await((nonRecordingRepo) -> getWorkflowRunStatus(nonRecordingRepo,
                cancelledWorkflowRunId) == Status.IN_PROGRESS);

        // cancel it again
        workflowRun.cancel();
    }

    @Test
    public void testDelete() throws IOException {
        GHWorkflow workflow = repo.getWorkflow(FAST_WORKFLOW_PATH);

        long latestPreexistingWorkflowRunId = getLatestPreexistingWorkflowRunId();

        workflow.dispatch(MAIN_BRANCH);

        await((nonRecordingRepo) -> getWorkflowRun(nonRecordingRepo,
                FAST_WORKFLOW_NAME,
                MAIN_BRANCH,
                Status.COMPLETED,
                latestPreexistingWorkflowRunId).isPresent());

        GHWorkflowRun workflowRunToDelete = getWorkflowRun(FAST_WORKFLOW_NAME,
                MAIN_BRANCH,
                Status.COMPLETED,
                latestPreexistingWorkflowRunId).orElseThrow(
                        () -> new IllegalStateException("We must have a valid workflow run starting from here"));

        assertNotNull(workflowRunToDelete.getId());

        workflowRunToDelete.delete();

        try {
            repo.getWorkflowRun(workflowRunToDelete.getId());
            Assert.fail("The workflow " + workflowRunToDelete.getId() + " should have been deleted.");
        } catch (GHFileNotFoundException e) {
            // success
        }
    }

    @Test
    public void testSearchOnBranch() throws IOException {
        GHWorkflow workflow = repo.getWorkflow(FAST_WORKFLOW_PATH);

        long latestPreexistingWorkflowRunId = getLatestPreexistingWorkflowRunId();

        workflow.dispatch(SECOND_BRANCH);

        await((nonRecordingRepo) -> getWorkflowRun(nonRecordingRepo,
                FAST_WORKFLOW_NAME,
                SECOND_BRANCH,
                Status.COMPLETED,
                latestPreexistingWorkflowRunId).isPresent());

        GHWorkflowRun workflowRun = getWorkflowRun(FAST_WORKFLOW_NAME,
                SECOND_BRANCH,
                Status.COMPLETED,
                latestPreexistingWorkflowRunId).orElseThrow(
                        () -> new IllegalStateException("We must have a valid workflow run starting from here"));

        assertEquals(workflow.getId(), workflowRun.getWorkflowId());
        assertEquals(SECOND_BRANCH, workflowRun.getHeadBranch());
        assertEquals(GHEvent.WORKFLOW_DISPATCH, workflowRun.getEvent());
        assertEquals(Status.COMPLETED, workflowRun.getStatus());
        assertEquals(Conclusion.SUCCESS, workflowRun.getConclusion());
    }

    @Test
    @SuppressWarnings("resource")
    public void testLogs() throws IOException {
        GHWorkflow workflow = repo.getWorkflow(FAST_WORKFLOW_PATH);

        long latestPreexistingWorkflowRunId = getLatestPreexistingWorkflowRunId();

        workflow.dispatch(MAIN_BRANCH);

        await((nonRecordingRepo) -> getWorkflowRun(nonRecordingRepo,
                FAST_WORKFLOW_NAME,
                MAIN_BRANCH,
                Status.COMPLETED,
                latestPreexistingWorkflowRunId).isPresent());

        GHWorkflowRun workflowRun = getWorkflowRun(FAST_WORKFLOW_NAME,
                MAIN_BRANCH,
                Status.COMPLETED,
                latestPreexistingWorkflowRunId).orElseThrow(
                        () -> new IllegalStateException("We must have a valid workflow run starting from here"));

        List<String> logsArchiveEntries = new ArrayList<>();
        String fullLogContent = workflowRun.downloadLogs((is) -> {
            try (ZipInputStream zis = new ZipInputStream(is)) {
                StringBuilder sb = new StringBuilder();

                ZipEntry ze;
                while ((ze = zis.getNextEntry()) != null) {
                    logsArchiveEntries.add(ze.getName());
                    if ("1_build.txt".equals(ze.getName())) {
                        // the scanner has to be kept open to avoid closing zis
                        Scanner scanner = new Scanner(zis);
                        while (scanner.hasNextLine()) {
                            sb.append(scanner.nextLine()).append("\n");
                        }
                    }
                }

                return sb.toString();
            }
        });

        assertThat(logsArchiveEntries, hasItems("1_build.txt", "build/9_Complete job.txt"));
        assertThat(fullLogContent, containsString("Hello, world!"));

        workflowRun.deleteLogs();

        try {
            workflowRun.downloadLogs((is) -> "");
            Assert.fail("Downloading logs should not be possible as they were deleted");
        } catch (GHFileNotFoundException e) {
            assertThat(e.getMessage(), containsString("Not Found"));
        }
    }

    @SuppressWarnings("resource")
    @Test
    public void testArtifacts() throws IOException {
        GHWorkflow workflow = repo.getWorkflow(ARTIFACTS_WORKFLOW_PATH);

        long latestPreexistingWorkflowRunId = getLatestPreexistingWorkflowRunId();

        workflow.dispatch(MAIN_BRANCH);

        await((nonRecordingRepo) -> getWorkflowRun(nonRecordingRepo,
                ARTIFACTS_WORKFLOW_NAME,
                MAIN_BRANCH,
                Status.COMPLETED,
                latestPreexistingWorkflowRunId).isPresent());

        GHWorkflowRun workflowRun = getWorkflowRun(ARTIFACTS_WORKFLOW_NAME,
                MAIN_BRANCH,
                Status.COMPLETED,
                latestPreexistingWorkflowRunId).orElseThrow(
                        () -> new IllegalStateException("We must have a valid workflow run starting from here"));

        List<GHArtifact> artifacts = new ArrayList<>(workflowRun.listArtifacts().toList());
        artifacts.sort((a1, a2) -> a1.getName().compareTo(a2.getName()));

        assertThat(artifacts.size(), is(2));

        // Test properties
        checkArtifactProperties(artifacts.get(0), "artifact1");
        checkArtifactProperties(artifacts.get(1), "artifact2");

        // Test download
        String artifactContent = artifacts.get(0).download((is) -> {
            try (ZipInputStream zis = new ZipInputStream(is)) {
                StringBuilder sb = new StringBuilder();

                ZipEntry ze = zis.getNextEntry();
                assertThat(ze.getName(), is("artifact1.txt"));

                // the scanner has to be kept open to avoid closing zis
                Scanner scanner = new Scanner(zis);
                while (scanner.hasNextLine()) {
                    sb.append(scanner.nextLine());
                }

                return sb.toString();
            }
        });

        assertThat(artifactContent, is("artifact1"));

        // Test GHRepository#getArtifact(long) as we are sure we have artifacts around
        GHArtifact artifactById = repo.getArtifact(artifacts.get(0).getId());
        checkArtifactProperties(artifactById, "artifact1");

        artifactById = repo.getArtifact(artifacts.get(1).getId());
        checkArtifactProperties(artifactById, "artifact2");

        // Test GHRepository#listArtifacts() as we are sure we have artifacts around
        List<GHArtifact> artifactsFromRepo = new ArrayList<>(
                repo.listArtifacts().withPageSize(2).iterator().nextPage());
        artifactsFromRepo.sort((a1, a2) -> a1.getName().compareTo(a2.getName()));

        // We have at least the two artifacts we just added
        assertThat(artifactsFromRepo.size(), is(2));

        // Test properties
        checkArtifactProperties(artifactsFromRepo.get(0), "artifact1");
        checkArtifactProperties(artifactsFromRepo.get(1), "artifact2");

        // Now let's test the delete() method
        GHArtifact artifact1 = artifacts.get(0);
        artifact1.delete();

        try {
            repo.getArtifact(artifact1.getId());
            Assert.fail("Getting the artifact should fail as it was deleted");
        } catch (GHFileNotFoundException e) {
            assertThat(e.getMessage(), containsString("Not Found"));
        }
    }

    private void await(Function<GHRepository, Boolean> condition) throws IOException {
        if (!mockGitHub.isUseProxy()) {
            return;
        }

        GHRepository nonRecordingRepo = getGitHubBeforeAfter().getRepository(REPO_NAME);

        Awaitility.await().pollInterval(Duration.ofSeconds(5)).atMost(Duration.ofSeconds(60)).until(() -> {
            return condition.apply(nonRecordingRepo);
        });
    }

    private long getLatestPreexistingWorkflowRunId() {
        return repo.queryWorkflowRuns().list().withPageSize(1).iterator().next().getId();
    }

    private static Optional<GHWorkflowRun> getWorkflowRun(GHRepository repository,
            String workflowName,
            String branch,
            Status status,
            long latestPreexistingWorkflowRunId) {
        List<GHWorkflowRun> workflowRuns = repository.queryWorkflowRuns()
                .branch(branch)
                .status(status)
                .event(GHEvent.WORKFLOW_DISPATCH)
                .list()
                .withPageSize(20)
                .iterator()
                .nextPage();

        for (GHWorkflowRun workflowRun : workflowRuns) {
            if (workflowRun.getName().equals(workflowName) && workflowRun.getId() > latestPreexistingWorkflowRunId) {
                return Optional.of(workflowRun);
            }
        }
        return Optional.empty();
    }

    private Optional<GHWorkflowRun> getWorkflowRun(String workflowName,
            String branch,
            Status status,
            long latestPreexistingWorkflowRunId) {
        return getWorkflowRun(this.repo, workflowName, branch, status, latestPreexistingWorkflowRunId);
    }

    private static Status getWorkflowRunStatus(GHRepository repository, long workflowRunId) {
        try {
            return repository.getWorkflowRun(workflowRunId).getStatus();
        } catch (IOException e) {
            throw new IllegalStateException("Unable to get workflow run status", e);
        }
    }

    private static void checkArtifactProperties(GHArtifact artifact, String artifactName) throws IOException {
        assertNotNull(artifact.getId());
        assertNotNull(artifact.getNodeId());
        assertEquals(REPO_NAME, artifact.getRepository().getFullName());
        assertThat(artifact.getName(), is(artifactName));
        assertThat(artifact.getArchiveDownloadUrl().getPath(), containsString("actions/artifacts"));
        assertNotNull(artifact.getCreatedAt());
        assertNotNull(artifact.getUpdatedAt());
        assertNotNull(artifact.getExpiresAt());
        assertThat(artifact.getSizeInBytes(), greaterThan(0L));
        assertFalse(artifact.isExpired());
    }
}
