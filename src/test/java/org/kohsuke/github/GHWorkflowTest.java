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

public class GHWorkflowTest extends AbstractGitHubWireMockTest {

    private static String REPO_NAME = "hub4j-test-org/GHWorkflowTest";

    private GHRepository repo;

    @Before
    @After
    public void cleanup() throws Exception {
        if (mockGitHub.isUseProxy()) {
            repo = getGitHubBeforeAfter().getRepository(REPO_NAME);

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

        assertEquals("test-workflow", workflow.getName());
        assertEquals(REPO_NAME, workflow.getRepository().getFullName());
        assertEquals(".github/workflows/test-workflow.yml", workflow.getPath());
        assertEquals("active", workflow.getState());
        assertEquals("/repos/hub4j-test-org/GHWorkflowTest/actions/workflows/6817859", workflow.getUrl().getPath());
        assertEquals("/hub4j-test-org/GHWorkflowTest/blob/main/.github/workflows/test-workflow.yml",
                workflow.getHtmlUrl().getPath());
        assertEquals("/hub4j-test-org/GHWorkflowTest/workflows/test-workflow/badge.svg",
                workflow.getBadgeUrl().getPath());

        GHWorkflow workflowById = repo.getWorkflow(workflow.getId());
        assertEquals(workflow.getNodeId(), workflowById.getNodeId());
    }

    @Test
    public void testDisableEnable() throws IOException {
        GHWorkflow workflow = repo.getWorkflow("test-workflow.yml");

        assertEquals("active", workflow.getState());

        workflow.disable();

        workflow = repo.getWorkflow("test-workflow.yml");
        assertEquals("disabled_manually", workflow.getState());

        workflow.enable();

        workflow = repo.getWorkflow("test-workflow.yml");
        assertEquals("active", workflow.getState());
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
        assertEquals(6817859L, workflow.getId());
        assertEquals("MDg6V29ya2Zsb3c2ODE3ODU5", workflow.getNodeId());
        assertEquals("test-workflow", workflow.getName());
        assertEquals(".github/workflows/test-workflow.yml", workflow.getPath());
        assertEquals("active", workflow.getState());
        assertEquals("/repos/hub4j-test-org/GHWorkflowTest/actions/workflows/6817859", workflow.getUrl().getPath());
        assertEquals("/hub4j-test-org/GHWorkflowTest/blob/main/.github/workflows/test-workflow.yml",
                workflow.getHtmlUrl().getPath());
        assertEquals("/hub4j-test-org/GHWorkflowTest/workflows/test-workflow/badge.svg",
                workflow.getBadgeUrl().getPath());
    }
}
