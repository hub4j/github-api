package org.kohsuke.github;

import org.apache.commons.io.IOUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.List;

import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.Matchers.hasProperty;

/**
 * Integration test for {@link GHContent}.
 */
public class GHContentIntegrationTest extends AbstractGitHubWireMockTest {

    private GHRepository repo;

    // file name with spaces and other chars
    private final String createdDirectory = "test+directory #50";
    private final String createdFilename = createdDirectory + "/test file-to+create-#1.txt";

    @Before
    @After
    public void cleanup() throws Exception {
        if (mockGitHub.isUseProxy()) {
            repo = getGitHubBeforeAfter().getRepository("hub4j-test-org/GHContentIntegrationTest");
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
        repo = gitHub.getRepository("hub4j-test-org/GHContentIntegrationTest");
    }

    @Test
    public void testGetFileContent() throws Exception {
        repo = gitHub.getRepository("hub4j-test-org/GHContentIntegrationTest");
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
                "Creating a file for integration tests.",
                createdFilename);
        GHContent createdContent = created.getContent();

        assertNotNull(created.getCommit());
        assertNotNull(created.getContent());
        assertNotNull(createdContent.getContent());
        assertThat(createdContent.getPath(), equalTo(createdFilename));
        assertEquals("this is an awesome file I created\n", createdContent.getContent());

        GHContent content = repo.getFileContent(createdFilename);
        assertThat(content, is(notNullValue()));
        assertThat(content.getSha(), equalTo(createdContent.getSha()));
        assertThat(content.getContent(), equalTo(createdContent.getContent()));
        assertThat(content.getPath(), equalTo(createdContent.getPath()));

        List<GHContent> directoryContents = repo.getDirectoryContent(createdDirectory);
        assertThat(directoryContents, is(notNullValue()));
        assertThat(directoryContents.size(), equalTo(1));
        content = directoryContents.get(0);
        assertThat(content.getSha(), is(created.getContent().getSha()));
        assertThat(content.getContent(), is(created.getContent().getContent()));
        assertThat(content.getPath(), equalTo(createdFilename));

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

        try {
            repo.getFileContent(createdFilename);
            fail("Delete didn't work!");
        } catch (GHFileNotFoundException e) {
            assertThat(e.getMessage(),
                    endsWith(
                            "/repos/hub4j-test-org/GHContentIntegrationTest/contents/test+directory%20%2350/test%20file-to+create-%231.txt {\"message\":\"Not Found\",\"documentation_url\":\"https://developer.github.com/v3/repos/contents/#get-contents\"}"));
        }
    }

    @Test
    public void testMIMESmall() throws IOException {
        GHRepository ghRepository = getTempRepository();
        GHContentBuilder ghContentBuilder = ghRepository.createContent();
        ghContentBuilder.message("Some commit msg");
        ghContentBuilder.path("MIME-Small.md");
        ghContentBuilder.content("123456789012345678901234567890123456789012345678901234567");
        ghContentBuilder.commit();
    }

    @Test
    public void testMIMELong() throws IOException {
        GHRepository ghRepository = getTempRepository();
        GHContentBuilder ghContentBuilder = ghRepository.createContent();
        ghContentBuilder.message("Some commit msg");
        ghContentBuilder.path("MIME-Long.md");
        ghContentBuilder.content("1234567890123456789012345678901234567890123456789012345678");
        ghContentBuilder.commit();
    }
    @Test
    public void testMIMELonger() throws IOException {
        GHRepository ghRepository = getTempRepository();
        GHContentBuilder ghContentBuilder = ghRepository.createContent();
        ghContentBuilder.message("Some commit msg");
        ghContentBuilder.path("MIME-Long.md");
        ghContentBuilder.content("123456789012345678901234567890123456789012345678901234567890"
                + "123456789012345678901234567890123456789012345678901234567890"
                + "123456789012345678901234567890123456789012345678901234567890"
                + "123456789012345678901234567890123456789012345678901234567890");
        ghContentBuilder.commit();
    }

    @Test
    public void testGetFileContentWithNonAsciiPath() throws Exception {
        final GHRepository repo = gitHub.getRepository("hub4j-test-org/GHContentIntegrationTest");
        final GHContent fileContent = repo.getFileContent("ghcontent-ro/a-file-with-\u00F6");
        assertThat(IOUtils.readLines(fileContent.read(), StandardCharsets.UTF_8), hasItems("test"));

        final GHContent fileContent2 = repo.getFileContent(fileContent.getPath());
        assertThat(IOUtils.readLines(fileContent2.read(), StandardCharsets.UTF_8), hasItems("test"));
    }

    @Test
    public void testGetFileContentWithSymlink() throws Exception {
        final GHRepository repo = gitHub.getRepository("hub4j-test-org/GHContentIntegrationTest");

        final GHContent fileContent = repo.getFileContent("ghcontent-ro/a-symlink-to-a-file");
        // for whatever reason GH says this is a file :-o
        assertThat(IOUtils.toString(fileContent.read(), StandardCharsets.UTF_8), is("thanks for reading me\n"));

        final GHContent dirContent = repo.getFileContent("ghcontent-ro/a-symlink-to-a-dir");
        // but symlinks to directories are symlinks!
        assertThat(dirContent,
                allOf(hasProperty("target", is("a-dir-with-3-entries")), hasProperty("type", is("symlink"))));

        // future somehow...

        // final GHContent fileContent2 = repo.getFileContent("ghcontent-ro/a-symlink-to-a-dir/entry-one");
        // this needs special handling and will 404 from GitHub
        // assertThat(IOUtils.toString(fileContent.read(), StandardCharsets.UTF_8), is(""));
    }
}
