package org.kohsuke.github;

import junit.framework.TestCase;

import java.util.List;
import java.util.UUID;

/**
 * Integration test for {@link GHContent}.
 */
public class GHContentIntegrationTest extends TestCase {

    private GitHub gitHub;
    private GHRepository repo;
    private String createdFilename;

    @Override
    public void setUp() throws Exception {
        super.setUp();

        gitHub = GitHub.connect();
        repo = gitHub.getRepository("acollign/github-api-test").fork();
        createdFilename = UUID.randomUUID().toString();
    }

    public void testGetFileContent() throws Exception {
        GHContent content = repo.getFileContent("ghcontent-ro/a-file-with-content");

        assertTrue(content.isFile());
        assertEquals("thanks for reading me\n", content.getContent());
    }

    public void testGetEmptyFileContent() throws Exception {
        GHContent content = repo.getFileContent("ghcontent-ro/an-empty-file");

        assertTrue(content.isFile());
        assertEquals("", content.getContent());
    }

    public void testGetDirectoryContent() throws Exception {
        List<GHContent> entries = repo.getDirectoryContent("ghcontent-ro/a-dir-with-3-entries");

        assertTrue(entries.size() == 3);
    }

    public void testCRUDContent() throws Exception {
        GHContentUpdateResponse created = repo.createContent("this is an awesome file I created\n", "Creating a file for integration tests.", createdFilename);
        GHContent createdContent = created.getContent();

        assertNotNull(created.getCommit());
        assertNotNull(created.getContent());
        assertNotNull(createdContent.getContent());
        assertEquals("this is an awesome file I created\n", createdContent.getContent());

        GHContentUpdateResponse updatedContentResponse = createdContent.update("this is some new content\n", "Updated file for integration tests.");
        GHContent updatedContent = updatedContentResponse.getContent();

        assertNotNull(updatedContentResponse.getCommit());
        assertNotNull(updatedContentResponse.getContent());
        assertEquals("this is some new content\n", updatedContent.getContent());

        GHContentUpdateResponse deleteResponse = updatedContent.delete("Enough of this foolishness!");

        assertNotNull(deleteResponse.getCommit());
        assertNull(deleteResponse.getContent());
    }
}
