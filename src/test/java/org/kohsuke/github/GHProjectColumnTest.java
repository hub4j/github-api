package org.kohsuke.github;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.FileNotFoundException;
import java.io.IOException;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.nullValue;

// TODO: Auto-generated Javadoc
/**
 * The Class GHProjectColumnTest.
 *
 * @author Gunnar Skjold
 */
public class GHProjectColumnTest extends AbstractGitHubWireMockTest {
    private GHProject project;
    private GHProjectColumn column;

    /**
     * Sets the up.
     *
     * @throws Exception the exception
     */
    @Before
    public void setUp() throws Exception {
        project = gitHub.getOrganization(GITHUB_API_TEST_ORG).createProject("test-project", "This is a test project");
        column = project.createColumn("column-one");
    }

    /**
     * Test created column.
     */
    @Test
    public void testCreatedColumn() {
        assertThat(column.getName(), equalTo("column-one"));
    }

    /**
     * Test edit column name.
     *
     * @throws IOException Signals that an I/O exception has occurred.
     */
    @Test
    public void testEditColumnName() throws IOException {
        column.setName("new-name");
        column = gitHub.getProjectColumn(column.getId());
        assertThat(column.getName(), equalTo("new-name"));
    }

    /**
     * Test delete column.
     *
     * @throws IOException Signals that an I/O exception has occurred.
     */
    @Test
    public void testDeleteColumn() throws IOException {
        column.delete();
        try {
            column = gitHub.getProjectColumn(column.getId());
            assertThat(column, nullValue());
        } catch (FileNotFoundException e) {
            column = null;
        }
    }

    /**
     * After.
     *
     * @throws IOException Signals that an I/O exception has occurred.
     */
    @After
    public void after() throws IOException {
        if (mockGitHub.isUseProxy()) {
            if (column != null) {
                column = getNonRecordingGitHub().getProjectColumn(column.getId());
                try {
                    column.delete();
                    column = null;
                } catch (FileNotFoundException e) {
                    column = null;
                }
            }
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
