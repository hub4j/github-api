package org.kohsuke.github;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

import static com.github.tomakehurst.wiremock.client.WireMock.containing;
import static com.github.tomakehurst.wiremock.client.WireMock.postRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;
import static com.github.tomakehurst.wiremock.client.WireMock.verify;
import static org.hamcrest.Matchers.equalTo;

public class GHWorkflowTest extends AbstractGitHubWireMockTest {

    private static String REPO_NAME = "hub4j-test-org/GHWorkflowTest";

    private GHRepository repo;

    @Before
    @After
    public void cleanup() throws Exception {
        if (mockGitHub.isUseProxy()) {
            repo = getNonRecordingGitHub().getRepository(REPO_NAME);

            // we need to make sure the workflow is enabled before the tests
            GHWorkflow workflow = repo.getWorkflow("test-workflow.yml");
            if (!workflow.getState().equals("active")) {
                workflow.enable();
            }
        }
    }

    @Before
    public void setUp() throws Exception {
        repo = gitHub.getRepository(REPO_NAME);
    }

    @Test
    public void testBasicInformation() throws IOException {
        GHWorkflow workflow = repo.getWorkflow("test-workflow.yml");

        assertThat(workflow.getName(), equalTo("test-workflow"));
        assertThat(workflow.getRepository().getFullName(), equalTo(REPO_NAME));
        assertThat(workflow.getPath(), equalTo(".github/workflows/test-workflow.yml"));
        assertThat(workflow.getState(), equalTo("active"));
        assertThat(workflow.getUrl().getPath(),
                equalTo("/repos/hub4j-test-org/GHWorkflowTest/actions/workflows/6817859"));
        assertThat(workflow.getHtmlUrl().getPath(),
                equalTo("/hub4j-test-org/GHWorkflowTest/blob/main/.github/workflows/test-workflow.yml"));
        assertThat(workflow.getBadgeUrl().getPath(),
                equalTo("/hub4j-test-org/GHWorkflowTest/workflows/test-workflow/badge.svg"));

        GHWorkflow workflowById = repo.getWorkflow(workflow.getId());
        assertThat(workflowById.getNodeId(), equalTo(workflow.getNodeId()));
    }

    @Test
    public void testDisableEnable() throws IOException {
        GHWorkflow workflow = repo.getWorkflow("test-workflow.yml");

        assertThat(workflow.getState(), equalTo("active"));

        workflow.disable();

        workflow = repo.getWorkflow("test-workflow.yml");
        assertThat(workflow.getState(), equalTo("disabled_manually"));

        workflow.enable();

        workflow = repo.getWorkflow("test-workflow.yml");
        assertThat(workflow.getState(), equalTo("active"));
    }

    @Test
    public void testDispatch() throws IOException {
        GHWorkflow workflow = repo.getWorkflow("test-workflow.yml");

        workflow.dispatch("main");
        verify(postRequestedFor(
                urlPathEqualTo("/repos/hub4j-test-org/GHWorkflowTest/actions/workflows/6817859/dispatches")));

        workflow.dispatch("main", Collections.singletonMap("parameter", "value"));
        verify(postRequestedFor(
                urlPathEqualTo("/repos/hub4j-test-org/GHWorkflowTest/actions/workflows/6817859/dispatches"))
                        .withRequestBody(containing("inputs"))
                        .withRequestBody(containing("parameter"))
                        .withRequestBody(containing("value")));
    }

    @Test
    public void testListWorkflows() throws IOException {
        List<GHWorkflow> workflows = repo.listWorkflows().toList();

        GHWorkflow workflow = workflows.get(0);
        assertThat(workflow.getId(), equalTo(6817859L));
        assertThat(workflow.getNodeId(), equalTo("MDg6V29ya2Zsb3c2ODE3ODU5"));
        assertThat(workflow.getName(), equalTo("test-workflow"));
        assertThat(workflow.getPath(), equalTo(".github/workflows/test-workflow.yml"));
        assertThat(workflow.getState(), equalTo("active"));
        assertThat(workflow.getUrl().getPath(),
                equalTo("/repos/hub4j-test-org/GHWorkflowTest/actions/workflows/6817859"));
        assertThat(workflow.getHtmlUrl().getPath(),
                equalTo("/hub4j-test-org/GHWorkflowTest/blob/main/.github/workflows/test-workflow.yml"));
        assertThat(workflow.getBadgeUrl().getPath(),
                equalTo("/hub4j-test-org/GHWorkflowTest/workflows/test-workflow/badge.svg"));
    }
}
