package org.kohsuke.github;

import org.junit.After;
import org.junit.Assert;
import org.junit.Test;

import java.io.FileNotFoundException;
import java.io.IOException;

/**
 * @author Gunnar Skjold
 */
public class GHProjectCardTest extends AbstractGitHubApiTestBase {
	private GHOrganization org;
	private GHProject project;
	private GHProjectColumn column;
	private GHProjectCard card;

	@Override public void setUp() throws Exception {
		super.setUp();
		org = gitHub.getOrganization("github-api-test-org");
		project = org.createProject("test-project", "This is a test project");
		column = project.createColumn("column-one");
		card = column.createCard("This is a card");
	}

	@Test
	public void testCreatedCard() {
		Assert.assertEquals("This is a card", card.getNote());
		Assert.assertFalse(card.isArchived());
	}

	@Test
	public void testEditCardNote() throws IOException {
		card.setNote("New note");
		card = gitHub.getProjectCard(card.getId());
		Assert.assertEquals("New note", card.getNote());
		Assert.assertFalse(card.isArchived());
	}

	@Test
	public void testArchiveCard() throws IOException {
		card.setArchived(true);
		card = gitHub.getProjectCard(card.getId());
		Assert.assertEquals("This is a card", card.getNote());
		Assert.assertTrue(card.isArchived());
	}

	@Test
	public void testCreateCardFromIssue() throws IOException {
		GHRepository repo = org.createRepository("repo-for-project-card").create();
		try {
			GHIssue issue = repo.createIssue("new-issue").body("With body").create();
			GHProjectCard card = column.createCard(issue);
			Assert.assertEquals(issue.getUrl(), card.getContentUrl());
		} finally {
			repo.delete();
		}
	}

	@Test
	public void testDeleteCard() throws IOException {
		card.delete();
		try {
			card = gitHub.getProjectCard(card.getId());
			Assert.assertNull(card);
		} catch (FileNotFoundException e) {
			card = null;
		}
	}

	@After
	public void after() throws IOException {
		if(card != null) {
			try {
				card.delete();
				card = null;
			} catch (FileNotFoundException e) {
				card = null;
			}
		}
		if(column != null) {
			try {
				column.delete();
				column = null;
			} catch (FileNotFoundException e) {
				column = null;
			}
		}
		if(project != null) {
			try {
				project.delete();
				project = null;
			} catch (FileNotFoundException e) {
				project = null;
			}
		}
	}
}
