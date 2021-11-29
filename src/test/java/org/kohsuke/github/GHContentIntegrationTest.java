package org.kohsuke.github;

import org.apache.commons.io.IOUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.Ignore;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;
import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.hamcrest.Matchers.equalTo;

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
            repo = getNonRecordingGitHub().getRepository("hub4j-test-org/GHContentIntegrationTest");
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
    @Ignore
    public void testGetRepository() throws Exception {
        GHRepository testRepo = gitHub.getRepositoryById(repo.getId());
        assertThat(testRepo.getName(), equalTo(repo.getName()));
        testRepo = gitHub.getRepositoryById(Long.toString(repo.getId()));
        assertThat(testRepo.getName(), equalTo(repo.getName()));
    }

    @Test
    @Ignore
    public void testGetFileContent() throws Exception {
        repo = gitHub.getRepository("hub4j-test-org/GHContentIntegrationTest");
        GHContent content = repo.getFileContent("ghcontent-ro/a-file-with-content");

        assertThat(content.isFile(), is(true));
        assertThat(content.getContent(), equalTo("thanks for reading me\n"));
    }

    @Test
    @Ignore
    public void testGetEmptyFileContent() throws Exception {
        GHContent content = repo.getFileContent("ghcontent-ro/an-empty-file");

        assertThat(content.isFile(), is(true));
        assertThat(content.getContent(), is(emptyString()));
    }

    @Test
    @Ignore
    public void testGetDirectoryContent() throws Exception {
        List<GHContent> entries = repo.getDirectoryContent("ghcontent-ro/a-dir-with-3-entries");

        assertThat(entries.size(), equalTo(3));
    }

    @Test
    @Ignore
    public void testGetDirectoryContentTrailingSlash() throws Exception {
        // Used to truncate the ?ref=main, see gh-224 https://github.com/kohsuke/github-api/pull/224
        List<GHContent> entries = repo.getDirectoryContent("ghcontent-ro/a-dir-with-3-entries/", "main");

        assertThat(entries.get(0).getUrl(), endsWith("?ref=main"));
    }

    @Test
    public void testCRUDContent() throws Exception {
        GHContentUpdateResponse created = repo.createContent("this is an awesome file I created\n",
                "Creating a file for integration tests.",
                createdFilename);
        int expectedRequestCount = mockGitHub.getRequestCount();
        GHContent createdContent = created.getContent();

        assertThat(mockGitHub.getRequestCount(), equalTo(expectedRequestCount));
        assertThat(created.getCommit(), notNullValue());
        assertThat(created.getContent(), notNullValue());

        assertThat(createdContent.getPath(), equalTo(createdFilename));
        assertThat(mockGitHub.getRequestCount(), equalTo(expectedRequestCount));

        assertThat(createdContent.getContent(), notNullValue());
        assertThat(createdContent.getContent(), equalTo("this is an awesome file I created\n"));

        ;
        assertThat(mockGitHub.getRequestCount(), equalTo(expectedRequestCount += 1));

        assertThat(created.getCommit().getSHA1(), notNullValue());
        assertThat(mockGitHub.getRequestCount(), equalTo(expectedRequestCount));
        assertThat(created.getCommit().getUrl().toString(),
                endsWith(
                        "/repos/hub4j-test-org/GHContentIntegrationTest/git/commits/" + created.getCommit().getSHA1()));

        assertThat(mockGitHub.getRequestCount(), equalTo(expectedRequestCount));
        
        // initialize to make compiler happy
        Method bridgeMethod = created.getClass().getMethod("getContent", null);
        Method[] methods = created.getClass().getMethods();
        for (Method method : methods) {
            System.out.println(method.getName() + " " + method.getReturnType());
            if (method.getName() == "getCommit" && method.getReturnType() == GHCommit.class) {
                bridgeMethod = method;                
            }
        }
        GHCommit ghcommit = (GHCommit) bridgeMethod.invoke(created);
        System.out.println(ghcommit.toString());

        // assertThat(ghcommit, notNullValue());
        // assertThat(ghcommit.getSHA1(), notNullValue());
        // assertThat(ghcommit.getUrl().toString(),
                // endsWith(
                //         "/repos/hub4j-test-org/GHContentIntegrationTest/git/commits/" + created.getCommit().getSHA1()));

        // assertThat(mockGitHub.getRequestCount(), equalTo(expectedRequestCount));
        
        

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

        expectedRequestCount = mockGitHub.getRequestCount();

        GHContent updatedContent = updatedContentResponse.getContent();

        assertThat(mockGitHub.getRequestCount(), equalTo(expectedRequestCount));

        assertThat(updatedContentResponse.getCommit(), notNullValue());



        assertThat(updatedContentResponse.getContent(), notNullValue());

        assertThat(mockGitHub.getRequestCount(), equalTo(expectedRequestCount));

        // due to what appears to be a cache propagation delay, this test is too flaky
        assertThat(new BufferedReader(new InputStreamReader(updatedContent.read())).readLine(),
                equalTo("this is some new content"));
        assertThat(updatedContent.getContent(), equalTo("this is some new content\n"));

        assertThat(mockGitHub.getRequestCount(), equalTo(expectedRequestCount += 1));

        assertThat(updatedContentResponse.getCommit().getSHA1(), notNullValue());
        assertThat(updatedContentResponse.getCommit().getUrl().toString(),
                endsWith("/repos/hub4j-test-org/GHContentIntegrationTest/git/commits/"
                        + updatedContentResponse.getCommit().getSHA1()));

        assertThat(mockGitHub.getRequestCount(), equalTo(expectedRequestCount));

        assertThat(updatedContentResponse.getCommit().getMessage(),
                equalTo("Updated file for integration tests."));

        // assertThat(mockGitHub.getRequestCount(), equalTo(expectedRequestCount += 1));

        assertThat(updatedContentResponse.getCommit().getAuthor().getName(), equalTo("Liam Newman"));
        assertThat(updatedContentResponse.getCommit().getAuthor().getEmail(), equalTo("bitwiseman@gmail.com"));
        assertThat(updatedContentResponse.getCommit().getCommitter().getName(), equalTo("Liam Newman"));
        assertThat(updatedContentResponse.getCommit().getCommitter().getEmail(), equalTo("bitwiseman@gmail.com"));

        assertThat("Resolving GHUser - was already resolved",
                mockGitHub.getRequestCount(),
                equalTo(expectedRequestCount));

        assertThat(updatedContentResponse.getCommit().getTree().getSha(), notNullValue());

        // assertThat("Resolving GHTree", mockGitHub.getRequestCount(), equalTo(expectedRequestCount += 1));

        assertThat(updatedContentResponse.getCommit().getTree().getUrl().toString(),
                endsWith("/repos/hub4j-test-org/GHContentIntegrationTest/git/trees/"
                        + updatedContentResponse.getCommit().getTree().getSha()));

        // assertThat("Resolving GHTree is not cached", mockGitHub.getRequestCount(), equalTo(expectedRequestCount + 2));

        GHContentUpdateResponse deleteResponse = updatedContent.delete("Enough of this foolishness!");

        assertThat(deleteResponse.getCommit(), notNullValue());
        assertThat(deleteResponse.getContent(), nullValue());

        try {
            repo.getFileContent(createdFilename);
            fail("Delete didn't work!");
        } catch (GHFileNotFoundException e) {
            assertThat(e.getMessage(),
                    endsWith(
                            "/repos/hub4j-test-org/GHContentIntegrationTest/contents/test+directory%20%2350/test%20file-to+create-%231.txt {\"message\":\"Not Found\",\"documentation_url\":\"https://docs.github.com/rest/reference/repos#get-repository-content\"}"));
        }
    }

    @Test
    @Ignore
    public void testMIMESmall() throws IOException {
        GHRepository ghRepository = getTempRepository();
        GHContentBuilder ghContentBuilder = ghRepository.createContent();
        ghContentBuilder.message("Some commit msg");
        ghContentBuilder.path("MIME-Small.md");
        ghContentBuilder.content("123456789012345678901234567890123456789012345678901234567");
        ghContentBuilder.commit();
    }

    @Test
    @Ignore
    public void testMIMELong() throws IOException {
        GHRepository ghRepository = getTempRepository();
        GHContentBuilder ghContentBuilder = ghRepository.createContent();
        ghContentBuilder.message("Some commit msg");
        ghContentBuilder.path("MIME-Long.md");
        ghContentBuilder.content("1234567890123456789012345678901234567890123456789012345678");
        ghContentBuilder.commit();
    }
    @Test
    @Ignore
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
    @Ignore
    public void testGetFileContentWithNonAsciiPath() throws Exception {
        final GHRepository repo = gitHub.getRepository("hub4j-test-org/GHContentIntegrationTest");
        final GHContent fileContent = repo.getFileContent("ghcontent-ro/a-file-with-\u00F6");
        assertThat(IOUtils.readLines(fileContent.read(), StandardCharsets.UTF_8), hasItems("test"));

        final GHContent fileContent2 = repo.getFileContent(fileContent.getPath());
        assertThat(IOUtils.readLines(fileContent2.read(), StandardCharsets.UTF_8), hasItems("test"));
    }

    @Test
    @Ignore
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
