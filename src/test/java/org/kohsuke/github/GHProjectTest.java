package org.kohsuke.github;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.FileNotFoundException;
import java.io.IOException;

/**
 * @author Gunnar Skjold
 */
public class GHProjectTest extends AbstractGitHubWireMockTest {
    private GHProject project;

    @Before
    public void setUp() throws Exception {
        project = gitHub.getOrganization(GITHUB_API_TEST_ORG).createProject("test-project", "This is a test project");
    }

    @Test
    public void testCreatedProject() {
        Assert.assertNotNull(project);
        Assert.assertEquals("test-project", project.getName());
        Assert.assertEquals("This is a test project", project.getBody());
        Assert.assertEquals(GHProject.ProjectState.OPEN, project.getState());
    }

    @Test
    public void testEditProjectName() throws IOException {
        project.setName("new-name");
        project = gitHub.getProject(project.getId());
        Assert.assertEquals("new-name", project.getName());
        Assert.assertEquals("This is a test project", project.getBody());
        Assert.assertEquals(GHProject.ProjectState.OPEN, project.getState());
    }

    @Test
    public void testEditProjectBody() throws IOException {
        project.setBody("New body");
        project = gitHub.getProject(project.getId());
        Assert.assertEquals("test-project", project.getName());
        Assert.assertEquals("New body", project.getBody());
        Assert.assertEquals(GHProject.ProjectState.OPEN, project.getState());
    }

    @Test
    public void testEditProjectState() throws IOException {
        project.setState(GHProject.ProjectState.CLOSED);
        project = gitHub.getProject(project.getId());
        Assert.assertEquals("test-project", project.getName());
        Assert.assertEquals("This is a test project", project.getBody());
        Assert.assertEquals(GHProject.ProjectState.CLOSED, project.getState());
    }

    @Test
    public void testDeleteProject() throws IOException {
        project.delete();
        try {
            project = gitHub.getProject(project.getId());
            Assert.assertNull(project);
        } catch (FileNotFoundException e) {
            project = null;
        }
    }

    @After
    public void after() throws IOException {
        if (mockGitHub.isUseProxy()) {
            if (project != null) {
                project = gitHubBeforeAfter.getProject(project.getId());
                try {
                    project.delete();
                    project = null;
                } catch (FileNotFoundException e) {
                    project = null;
                }
            }
        }
    }
}
