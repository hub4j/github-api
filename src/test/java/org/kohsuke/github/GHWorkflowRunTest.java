package org.kohsuke.github;

import org.awaitility.Awaitility;
import org.junit.Before;
import org.junit.Test;
import org.kohsuke.github.GHPullRequestQueryBuilder.Sort;
import org.kohsuke.github.GHWorkflowJob.Step;
import org.kohsuke.github.GHWorkflowRun.Conclusion;
import org.kohsuke.github.GHWorkflowRun.Status;
import org.kohsuke.github.function.InputStreamFunction;

import java.io.IOException;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Scanner;
import java.util.function.Function;
import java.util.stream.Collectors;
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

    private static final String MULTI_JOBS_WORKFLOW_PATH = "multi-jobs-workflow.yml";
    private static final String MULTI_JOBS_WORKFLOW_NAME = "Multi jobs workflow";
    private static final String RUN_A_ONE_LINE_SCRIPT_STEP_NAME = "Run a one-line script";
    private static final String UBUNTU_LABEL = "ubuntu-latest";

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

        assertThat(workflowRun.getWorkflowId(), equalTo(workflow.getId()));
        assertThat(workflowRun.getId(), notNullValue());
        assertThat(workflowRun.getNodeId(), notNullValue());
        assertThat(workflowRun.getRepository().getFullName(), equalTo(REPO_NAME));
        assertThat(workflowRun.getUrl().getPath(), containsString("/actions/runs/"));
        assertThat(workflowRun.getHtmlUrl().getPath(), containsString("/actions/runs/"));
        assertThat(workflowRun.getJobsUrl().getPath(), endsWith("/jobs"));
        assertThat(workflowRun.getLogsUrl().getPath(), endsWith("/logs"));
        assertThat(workflowRun.getCheckSuiteUrl().getPath(), containsString("/check-suites/"));
        assertThat(workflowRun.getArtifactsUrl().getPath(), endsWith("/artifacts"));
        assertThat(workflowRun.getCancelUrl().getPath(), endsWith("/cancel"));
        assertThat(workflowRun.getRerunUrl().getPath(), endsWith("/rerun"));
        assertThat(workflowRun.getWorkflowUrl().getPath(), containsString("/actions/workflows/"));
        assertThat(workflowRun.getHeadBranch(), equalTo(MAIN_BRANCH));
        assertThat(workflowRun.getHeadCommit().getId(), notNullValue());
        assertThat(workflowRun.getHeadCommit().getTreeId(), notNullValue());
        assertThat(workflowRun.getHeadCommit().getMessage(), notNullValue());
        assertThat(workflowRun.getHeadCommit().getTimestamp(), notNullValue());
        assertThat(workflowRun.getHeadCommit().getAuthor().getEmail(), notNullValue());
        assertThat(workflowRun.getHeadCommit().getCommitter().getEmail(), notNullValue());
        assertThat(workflowRun.getEvent(), equalTo(GHEvent.WORKFLOW_DISPATCH));
        assertThat(workflowRun.getStatus(), equalTo(Status.COMPLETED));
        assertThat(workflowRun.getConclusion(), equalTo(Conclusion.SUCCESS));
        assertThat(workflowRun.getHeadSha(), notNullValue());
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

        assertThat(workflowRun.getId(), notNullValue());

        workflowRun.cancel();
        long cancelledWorkflowRunId = workflowRun.getId();

        // let's wait until it's completed
        await((nonRecordingRepo) -> getWorkflowRunStatus(nonRecordingRepo, cancelledWorkflowRunId) == Status.COMPLETED);

        // let's check that it has been properly cancelled
        workflowRun = repo.getWorkflowRun(cancelledWorkflowRunId);
        assertThat(workflowRun.getConclusion(), equalTo(Conclusion.CANCELLED));

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

        assertThat(workflowRunToDelete.getId(), notNullValue());

        workflowRunToDelete.delete();

        try {
            repo.getWorkflowRun(workflowRunToDelete.getId());
            fail("The workflow " + workflowRunToDelete.getId() + " should have been deleted.");
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

        assertThat(workflowRun.getWorkflowId(), equalTo(workflow.getId()));
        assertThat(workflowRun.getHeadBranch(), equalTo(SECOND_BRANCH));
        assertThat(workflowRun.getEvent(), equalTo(GHEvent.WORKFLOW_DISPATCH));
        assertThat(workflowRun.getStatus(), equalTo(Status.COMPLETED));
        assertThat(workflowRun.getConclusion(), equalTo(Conclusion.SUCCESS));
    }

    @Test
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
        String fullLogContent = workflowRun
                .downloadLogs(getLogArchiveInputStreamFunction("1_build.txt", logsArchiveEntries));

        assertThat(logsArchiveEntries, hasItems("1_build.txt", "build/9_Complete job.txt"));
        assertThat(fullLogContent, containsString("Hello, world!"));

        workflowRun.deleteLogs();

        try {
            workflowRun.downloadLogs((is) -> "");
            fail("Downloading logs should not be possible as they were deleted");
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
            fail("Getting the artifact should fail as it was deleted");
        } catch (GHFileNotFoundException e) {
            assertThat(e.getMessage(), containsString("Not Found"));
        }
    }

    @Test
    public void testJobs() throws IOException {
        GHWorkflow workflow = repo.getWorkflow(MULTI_JOBS_WORKFLOW_PATH);

        long latestPreexistingWorkflowRunId = getLatestPreexistingWorkflowRunId();

        workflow.dispatch(MAIN_BRANCH);

        await((nonRecordingRepo) -> getWorkflowRun(nonRecordingRepo,
                MULTI_JOBS_WORKFLOW_NAME,
                MAIN_BRANCH,
                Status.COMPLETED,
                latestPreexistingWorkflowRunId).isPresent());

        GHWorkflowRun workflowRun = getWorkflowRun(MULTI_JOBS_WORKFLOW_NAME,
                MAIN_BRANCH,
                Status.COMPLETED,
                latestPreexistingWorkflowRunId).orElseThrow(
                        () -> new IllegalStateException("We must have a valid workflow run starting from here"));

        List<GHWorkflowJob> jobs = workflowRun.listJobs()
                .toList()
                .stream()
                .sorted((j1, j2) -> j1.getName().compareTo(j2.getName()))
                .collect(Collectors.toList());

        assertThat(jobs.size(), is(2));

        GHWorkflowJob job1 = jobs.get(0);
        checkJobProperties(workflowRun.getId(), job1, "job1");
        String fullLogContent = job1.downloadLogs(getLogTextInputStreamFunction());
        assertThat(fullLogContent, containsString("Hello from job1!"));

        GHWorkflowJob job2 = jobs.get(1);
        checkJobProperties(workflowRun.getId(), job2, "job2");
        fullLogContent = job2.downloadLogs(getLogTextInputStreamFunction());
        assertThat(fullLogContent, containsString("Hello from job2!"));

        // while we have a job around, test GHRepository#getWorkflowJob(id)
        GHWorkflowJob job1ById = repo.getWorkflowJob(job1.getId());
        checkJobProperties(workflowRun.getId(), job1ById, "job1");

        // Also test listAllJobs() works correctly
        List<GHWorkflowJob> allJobs = workflowRun.listAllJobs().withPageSize(10).iterator().nextPage();
        assertThat(allJobs.size(), greaterThanOrEqualTo(2));
    }

    @Test
    public void testApproval() throws IOException {
        List<GHPullRequest> pullRequests = repo.queryPullRequests()
                .base(MAIN_BRANCH)
                .sort(Sort.CREATED)
                .direction(GHDirection.DESC)
                .state(GHIssueState.OPEN)
                .list()
                .toList();

        assertThat(pullRequests.size(), greaterThanOrEqualTo(1));
        GHPullRequest pullRequest = pullRequests.get(0);

        await("Waiting for workflow run to be pending",
                (nonRecordingRepo) -> getWorkflowRun(nonRecordingRepo,
                        FAST_WORKFLOW_NAME,
                        MAIN_BRANCH,
                        Conclusion.ACTION_REQUIRED).isPresent());

        GHWorkflowRun workflowRun = getWorkflowRun(FAST_WORKFLOW_NAME, MAIN_BRANCH, Conclusion.ACTION_REQUIRED)
                .orElseThrow(() -> new IllegalStateException("We must have a valid workflow run starting from here"));

        workflowRun.approve();

        await("Waiting for workflow run to be approved",
                (nonRecordingRepo) -> getWorkflowRun(nonRecordingRepo,
                        FAST_WORKFLOW_NAME,
                        pullRequest.getHead().getRef(),
                        Conclusion.SUCCESS).isPresent());

        workflowRun = repo.getWorkflowRun(workflowRun.getId());

        assertThat(workflowRun.getConclusion(), is(Conclusion.SUCCESS));
    }

    private void await(String alias, Function<GHRepository, Boolean> condition) throws IOException {
        if (!mockGitHub.isUseProxy()) {
            return;
        }

        GHRepository nonRecordingRepo = getNonRecordingGitHub().getRepository(REPO_NAME);

        Awaitility.await(alias).pollInterval(Duration.ofSeconds(5)).atMost(Duration.ofSeconds(60)).until(() -> {
            return condition.apply(nonRecordingRepo);
        });
    }

    private void await(Function<GHRepository, Boolean> condition) throws IOException {
        await(null, condition);
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

    private static Optional<GHWorkflowRun> getWorkflowRun(GHRepository repository,
            String workflowName,
            String branch,
            Conclusion conclusion) {
        List<GHWorkflowRun> workflowRuns = repository.queryWorkflowRuns()
                .branch(branch)
                .conclusion(conclusion)
                .event(GHEvent.PULL_REQUEST)
                .list()
                .withPageSize(20)
                .iterator()
                .nextPage();

        for (GHWorkflowRun workflowRun : workflowRuns) {
            if (workflowRun.getName().equals(workflowName)) {
                return Optional.of(workflowRun);
            }
        }
        return Optional.empty();
    }

    private Optional<GHWorkflowRun> getWorkflowRun(String workflowName, String branch, Conclusion conclusion) {
        return getWorkflowRun(this.repo, workflowName, branch, conclusion);
    }

    private static Status getWorkflowRunStatus(GHRepository repository, long workflowRunId) {
        try {
            return repository.getWorkflowRun(workflowRunId).getStatus();
        } catch (IOException e) {
            throw new IllegalStateException("Unable to get workflow run status", e);
        }
    }

    @SuppressWarnings("resource")
    private static InputStreamFunction<String> getLogArchiveInputStreamFunction(String mainLogFileName,
            List<String> logsArchiveEntries) {
        return (is) -> {
            try (ZipInputStream zis = new ZipInputStream(is)) {
                StringBuilder sb = new StringBuilder();

                ZipEntry ze;
                while ((ze = zis.getNextEntry()) != null) {
                    logsArchiveEntries.add(ze.getName());
                    if (mainLogFileName.equals(ze.getName())) {
                        // the scanner has to be kept open to avoid closing zis
                        Scanner scanner = new Scanner(zis);
                        while (scanner.hasNextLine()) {
                            sb.append(scanner.nextLine()).append("\n");
                        }
                    }
                }

                return sb.toString();
            }
        };
    }

    @SuppressWarnings("resource")
    private static InputStreamFunction<String> getLogTextInputStreamFunction() {
        return (is) -> {
            StringBuilder sb = new StringBuilder();
            Scanner scanner = new Scanner(is);
            while (scanner.hasNextLine()) {
                sb.append(scanner.nextLine()).append("\n");
            }
            return sb.toString();
        };
    }

    private static void checkArtifactProperties(GHArtifact artifact, String artifactName) throws IOException {
        assertThat(artifact.getId(), notNullValue());
        assertThat(artifact.getNodeId(), notNullValue());
        assertThat(artifact.getRepository().getFullName(), equalTo(REPO_NAME));
        assertThat(artifact.getName(), is(artifactName));
        assertThat(artifact.getArchiveDownloadUrl().getPath(), containsString("actions/artifacts"));
        assertThat(artifact.getCreatedAt(), notNullValue());
        assertThat(artifact.getUpdatedAt(), notNullValue());
        assertThat(artifact.getExpiresAt(), notNullValue());
        assertThat(artifact.getSizeInBytes(), greaterThan(0L));
        assertThat(artifact.isExpired(), is(false));
    }

    private static void checkJobProperties(long workflowRunId, GHWorkflowJob job, String jobName) throws IOException {
        assertThat(job.getId(), notNullValue());
        assertThat(job.getNodeId(), notNullValue());
        assertThat(job.getRepository().getFullName(), equalTo(REPO_NAME));
        assertThat(job.getName(), is(jobName));
        assertThat(job.getStartedAt(), notNullValue());
        assertThat(job.getCompletedAt(), notNullValue());
        assertThat(job.getHeadSha(), notNullValue());
        assertThat(job.getStatus(), is(Status.COMPLETED));
        assertThat(job.getConclusion(), is(Conclusion.SUCCESS));
        assertThat(job.getRunId(), is(workflowRunId));
        assertThat(job.getUrl().getPath(), containsString("/actions/jobs/"));
        assertThat(job.getHtmlUrl().getPath(), containsString("/runs/" + job.getId()));
        assertThat(job.getCheckRunUrl().getPath(), containsString("/check-runs/"));
        assertThat(job.getRunnerId(), is(1));
        assertThat(job.getRunnerName(), containsString("my runner"));
        assertThat(job.getRunnerGroupId(), is(2));
        assertThat(job.getRunnerGroupName(), containsString("my runner group"));

        // we only test the step we have control over, the others are added by GitHub
        Optional<Step> step = job.getSteps()
                .stream()
                .filter(s -> RUN_A_ONE_LINE_SCRIPT_STEP_NAME.equals(s.getName()))
                .findFirst();
        if (!step.isPresent()) {
            fail("Unable to find " + RUN_A_ONE_LINE_SCRIPT_STEP_NAME + " step");
        }

        Optional<String> labelOptional = job.getLabels().stream().filter(s -> s.equals(UBUNTU_LABEL)).findFirst();
        if (!labelOptional.isPresent()) {
            fail("Unable to find " + UBUNTU_LABEL + " label");
        }

        checkStepProperties(step.get(), RUN_A_ONE_LINE_SCRIPT_STEP_NAME, 2);
    }

    private static void checkStepProperties(Step step, String name, int number) {
        assertThat(step.getName(), is(name));
        assertThat(step.getNumber(), is(number));
        assertThat(step.getStatus(), is(Status.COMPLETED));
        assertThat(step.getConclusion(), is(Conclusion.SUCCESS));
        assertThat(step.getStartedAt(), notNullValue());
        assertThat(step.getCompletedAt(), notNullValue());
    }
}
