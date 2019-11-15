package org.kohsuke.github;

import org.apache.commons.io.IOUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;

/**
 * Integration test for {@link GHContent}.
 */
public class GHContentIntegrationTest extends AbstractGitHubWireMockTest {

    private GHRepository repo;
    private String createdFilename = "test-file-to-create.txt";

    @Before
    @After
    public void cleanup() throws Exception {
        if (mockGitHub.isUseProxy()) {
            repo = gitHubBeforeAfter.getRepository("github-api-test-org/GHContentIntegrationTest");
            try {
                GHContent content = repo.getFileContent(createdFilename);
                if (content != null) {
                    content.delete("Cleanup");
                }
            } catch (IOException e) {
            }
        }
    }

    @Before
    public void setUp() throws Exception {
        repo = gitHub.getRepository("github-api-test-org/GHContentIntegrationTest");
    }

    @Test
    public void testGetFileContent() throws Exception {
        repo = gitHub.getRepository("github-api-test-org/GHContentIntegrationTest");
        GHContent content = repo.getFileContent("ghcontent-ro/a-file-with-content");

        assertTrue(content.isFile());
        assertEquals("thanks for reading me\n", content.getContent());
    }

    @Test
    public void testGetEmptyFileContent() throws Exception {
        GHContent content = repo.getFileContent("ghcontent-ro/an-empty-file");

        assertTrue(content.isFile());
        assertEquals("", content.getContent());
    }

    @Test
    public void testGetDirectoryContent() throws Exception {
        List<GHContent> entries = repo.getDirectoryContent("ghcontent-ro/a-dir-with-3-entries");

        assertTrue(entries.size() == 3);
    }

    @Test
    public void testGetDirectoryContentTrailingSlash() throws Exception {
        // Used to truncate the ?ref=master, see gh-224 https://github.com/kohsuke/github-api/pull/224
        List<GHContent> entries = repo.getDirectoryContent("ghcontent-ro/a-dir-with-3-entries/", "master");

        assertTrue(entries.get(0).getUrl().endsWith("?ref=master"));
    }

    @Test
    public void testCRUDContent() throws Exception {
        GHContentUpdateResponse created = repo.createContent("this is an awesome file I created\n",
                "Creating a file for integration tests.", createdFilename);
        GHContent createdContent = created.getContent();

        assertNotNull(created.getCommit());
        assertNotNull(created.getContent());
        assertNotNull(createdContent.getContent());
        assertEquals("this is an awesome file I created\n", createdContent.getContent());

        GHContentUpdateResponse updatedContentResponse = createdContent.update("this is some new content\n",
                "Updated file for integration tests.");
        GHContent updatedContent = updatedContentResponse.getContent();

        assertNotNull(updatedContentResponse.getCommit());
        assertNotNull(updatedContentResponse.getContent());
        // due to what appears to be a cache propagation delay, this test is too flaky
        assertEquals("this is some new content",
                new BufferedReader(new InputStreamReader(updatedContent.read())).readLine());
        assertEquals("this is some new content\n", updatedContent.getContent());

        GHContentUpdateResponse deleteResponse = updatedContent.delete("Enough of this foolishness!");

        assertNotNull(deleteResponse.getCommit());
        assertNull(deleteResponse.getContent());
    }
}
