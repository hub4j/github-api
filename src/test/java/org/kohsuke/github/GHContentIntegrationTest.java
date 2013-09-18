package org.kohsuke.github;

import junit.framework.TestCase;

import java.util.List;

/**
 * Unit test for {@link GHContent}.
 */
public class GHContentIntegrationTest extends TestCase {

    private GitHub gitHub;
    private GHRepository repo;

    @Override
    public void setUp() throws Exception {
        super.setUp();
        // we just read at the moment
        gitHub = GitHub.connectAnonymously();
        repo = gitHub.getUser("acollign").getRepository("github-api-test");
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
}
