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
public class GHProjectColumnTest extends AbstractGitHubApiWireMockTest {
	private GHProject project;
	private GHProjectColumn column;

	@Before
	public void setUp() throws Exception {
		project = gitHub
				.getOrganization(GITHUB_API_TEST_ORG)
				.createProject("test-project", "This is a test project");
		column = project.createColumn("column-one");
	}

	@Test
	public void testCreatedColumn() {
		Assert.assertEquals("column-one", column.getName());
	}

	@Test
	public void testEditColumnName() throws IOException {
		column.setName("new-name");
		column = gitHub.getProjectColumn(column.getId());
		Assert.assertEquals("new-name", column.getName());
	}

	@Test
	public void testDeleteColumn() throws IOException {
		column.delete();
		try {
			column = gitHub.getProjectColumn(column.getId());
			Assert.assertNull(column);
		} catch (FileNotFoundException e) {
			column = null;
		}
	}

	@After
	public void after() throws IOException {
		if(githubApi.isUseProxy()) {
			if (column != null) {
				column = gitHubBeforeAfter
					.getProjectColumn(column.getId());
				try {
					column.delete();
					column = null;
				} catch (FileNotFoundException e) {
					column = null;
				}
			}
			if (project != null) {
				project = gitHubBeforeAfter
					.getProject(project.getId());
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
