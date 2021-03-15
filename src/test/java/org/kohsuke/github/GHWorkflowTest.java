package org.kohsuke.github;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.util.Collections;

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
        workflow.dispatch("main", Collections.singletonMap("parameter", "value"));

        // if we implement the logs API at some point, it might be a good idea to validate all this
    }
}
