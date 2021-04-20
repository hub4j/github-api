package org.kohsuke.github;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.FileNotFoundException;
import java.io.IOException;

import static org.hamcrest.Matchers.*;

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
        assertThat(project, notNullValue());
        assertThat(project.getName(), equalTo("test-project"));
        assertThat(project.getBody(), equalTo("This is a test project"));
        assertThat(project.getState(), equalTo(GHProject.ProjectState.OPEN));
    }

    @Test
    public void testEditProjectName() throws IOException {
        project.setName("new-name");
        project = gitHub.getProject(project.getId());
        assertThat(project.getName(), equalTo("new-name"));
        assertThat(project.getBody(), equalTo("This is a test project"));
        assertThat(project.getState(), equalTo(GHProject.ProjectState.OPEN));
    }

    @Test
    public void testEditProjectBody() throws IOException {
        project.setBody("New body");
        project = gitHub.getProject(project.getId());
        assertThat(project.getName(), equalTo("test-project"));
        assertThat(project.getBody(), equalTo("New body"));
        assertThat(project.getState(), equalTo(GHProject.ProjectState.OPEN));
    }

    @Test
    public void testEditProjectState() throws IOException {
        project.setState(GHProject.ProjectState.CLOSED);
        project = gitHub.getProject(project.getId());
        assertThat(project.getName(), equalTo("test-project"));
        assertThat(project.getBody(), equalTo("This is a test project"));
        assertThat(project.getState(), equalTo(GHProject.ProjectState.CLOSED));
    }

    @Test
    public void testDeleteProject() throws IOException {
        project.delete();
        try {
            project = gitHub.getProject(project.getId());
            assertThat(project, nullValue());
        } catch (FileNotFoundException e) {
            project = null;
        }
    }

    @After
    public void after() throws IOException {
        if (mockGitHub.isUseProxy()) {
            if (project != null) {
                project = getNonRecordingGitHub().getProject(project.getId());
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
