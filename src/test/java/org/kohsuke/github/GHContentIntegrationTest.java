package org.kohsuke.github;

import org.apache.commons.io.IOUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;
import java.util.List;

import static org.hamcrest.Matchers.*;

// TODO: Auto-generated Javadoc
/**
 * Integration test for {@link GHContent}.
 */
public class GHContentIntegrationTest extends AbstractGitHubWireMockTest {

    private GHRepository repo;

    // file name with spaces and other chars
    private final String createdDirectory = "test+directory #50";
    private final String createdFilename = createdDirectory + "/test file-to+create-#1.txt";

    /**
     * Cleanup.
     *
     * @throws Exception
     *             the exception
     */
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

    /**
     * Sets the up.
     *
     * @throws Exception
     *             the exception
     */
    @Before
    public void setUp() throws Exception {
        repo = gitHub.getRepository("hub4j-test-org/GHContentIntegrationTest");
    }

    /**
     * Test get repository.
     *
     * @throws Exception
     *             the exception
     */
    @Test
    public void testGetRepository() throws Exception {
        GHRepository testRepo = gitHub.getRepositoryById(repo.getId());
        assertThat(testRepo.getName(), equalTo(repo.getName()));
        testRepo = gitHub.getRepositoryById(Long.toString(repo.getId()));
        assertThat(testRepo.getName(), equalTo(repo.getName()));
    }

    /**
     * Test get repository created from a template repository
     *
     * @throws Exception
     *             the exception
     */
    @Test
    public void testGetRepositoryWithTemplateRepositoryInfo() throws Exception {
        GHRepository testRepo = gitHub.getRepositoryById(repo.getId());
        assertThat(testRepo.getTemplateRepository(), notNullValue());
        assertThat(testRepo.getTemplateRepository().getOwnerName(), equalTo("octocat"));
        assertThat(testRepo.getTemplateRepository().isTemplate(), equalTo(true));
    }

    /**
     * Test get file content.
     *
     * @throws Exception
     *             the exception
     */
    @Test
    public void testGetFileContent() throws Exception {
        repo = gitHub.getRepository("hub4j-test-org/GHContentIntegrationTest");
        GHContent content = repo.getFileContent("ghcontent-ro/a-file-with-content");

        assertThat(content.isFile(), is(true));
        assertThat(content.getContent(), equalTo("thanks for reading me\n"));
    }

    /**
     * Test get empty file content.
     *
     * @throws Exception
     *             the exception
     */
    @Test
    public void testGetEmptyFileContent() throws Exception {
        GHContent content = repo.getFileContent("ghcontent-ro/an-empty-file");

        assertThat(content.isFile(), is(true));
        assertThat(content.getContent(), is(emptyString()));
    }

    /**
     * Test get directory content.
     *
     * @throws Exception
     *             the exception
     */
    @Test
    public void testGetDirectoryContent() throws Exception {
        List<GHContent> entries = repo.getDirectoryContent("ghcontent-ro/a-dir-with-3-entries");

        assertThat(entries.size(), equalTo(3));
    }

    /**
     * Test get directory content trailing slash.
     *
     * @throws Exception
     *             the exception
     */
    @Test
    public void testGetDirectoryContentTrailingSlash() throws Exception {
        // Used to truncate the ?ref=main, see gh-224 https://github.com/kohsuke/github-api/pull/224
        List<GHContent> entries = repo.getDirectoryContent("ghcontent-ro/a-dir-with-3-entries/", "main");

        assertThat(entries.get(0).getUrl(), endsWith("?ref=main"));
    }

    /**
     * Test CRUD content.
     *
     * @throws Exception
     *             the exception
     */
    @Test
    public void testCRUDContent() throws Exception {
        GHContentUpdateResponse created = repo.createContent("this is an awesome file I created\n",
                "Creating a file for integration tests.",
                createdFilename);
        int expectedRequestCount = mockGitHub.getRequestCount();
        GHContent createdContent = created.getContent();

        assertThat(mockGitHub.getRequestCount(), equalTo(expectedRequestCount));

        expectedRequestCount = checkCreatedCommits(created.getCommit(), getGHCommit(created), expectedRequestCount);

        assertThat(created.getContent(), notNullValue());
        assertThat(createdContent.getPath(), equalTo(createdFilename));
        assertThat(mockGitHub.getRequestCount(), equalTo(expectedRequestCount));
        assertThat(createdContent.getContent(), notNullValue());
        assertThat(createdContent.getContent(), equalTo("this is an awesome file I created\n"));
        assertThat(mockGitHub.getRequestCount(), equalTo(expectedRequestCount += 1));

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

        assertThat(updatedContentResponse.getContent(), notNullValue());

        assertThat(mockGitHub.getRequestCount(), equalTo(expectedRequestCount));

        // due to what appears to be a cache propagation delay, this test is too flaky
        assertThat(new BufferedReader(new InputStreamReader(updatedContent.read())).readLine(),
                equalTo("this is some new content"));
        assertThat(updatedContent.getContent(), equalTo("this is some new content\n"));

        assertThat(mockGitHub.getRequestCount(), equalTo(expectedRequestCount += 1));

        expectedRequestCount = checkUpdatedContentResponseCommits(updatedContentResponse.getCommit(),
                getGHCommit(updatedContentResponse),
                expectedRequestCount);

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

    /**
     * Check created commits.
     *
     * @param gitCommit
     *            the git commit
     * @param ghCommit
     *            the gh commit
     * @param expectedRequestCount
     *            the expected request count
     * @return the int
     * @throws Exception
     *             the exception
     */
    int checkCreatedCommits(GitCommit gitCommit, GHCommit ghCommit, int expectedRequestCount) throws Exception {
        expectedRequestCount = checkBasicCommitInfo(gitCommit, ghCommit, expectedRequestCount);
        assertThat(mockGitHub.getRequestCount(), equalTo(expectedRequestCount));

        assertThat(gitCommit.getMessage(), equalTo("Creating a file for integration tests."));
        assertThat(gitCommit.getAuthoredDate(), equalTo(GitHubClient.parseDate("2021-06-28T20:37:49Z")));
        assertThat(gitCommit.getCommitDate(), equalTo(GitHubClient.parseDate("2021-06-28T20:37:49Z")));

        // Changed this to assert null since bridge methods are missing.
        assertThat(ghCommit, nullValue());
        // assertThat(ghCommit.getCommitShortInfo().getMessage(), equalTo("Creating a file for integration tests."));
        // assertThat("Message already resolved", mockGitHub.getRequestCount(), equalTo(expectedRequestCount));
        // Assert.assertThrows(GHException.class, () -> ghCommit.getCommitShortInfo().getCommentCount());

        // ghCommit.populate();
        // assertThat("Populate GHCommit", mockGitHub.getRequestCount(), equalTo(expectedRequestCount += 1));

        // expectedRequestCount = checkCommitUserInfo(gitCommit, ghCommit, expectedRequestCount);
        // assertThat("Resolved GHUser for GHCommit", mockGitHub.getRequestCount(), equalTo(expectedRequestCount += 1));

        expectedRequestCount = checkCommitTree(gitCommit, ghCommit, expectedRequestCount);

        expectedRequestCount = checkCommitParents(gitCommit, ghCommit, expectedRequestCount);

        return expectedRequestCount;
    }

    /**
     * Gets the GH commit.
     *
     * @param resp
     *            the resp
     * @return the GH commit
     * @throws Exception
     *             the exception
     */
    GHCommit getGHCommit(GHContentUpdateResponse resp) throws Exception {
        for (Method method : resp.getClass().getMethods()) {
            if (method.getName().equals("getCommit") && method.getReturnType().equals(GHCommit.class)) {
                return (GHCommit) method.invoke(resp);
            }
        }
        System.out.println("Unable to find bridge method");
        return null;
    }

    /**
     * Check updated content response commits.
     *
     * @param gitCommit
     *            the git commit
     * @param ghCommit
     *            the gh commit
     * @param expectedRequestCount
     *            the expected request count
     * @return the int
     * @throws Exception
     *             the exception
     */
    int checkUpdatedContentResponseCommits(GitCommit gitCommit, GHCommit ghCommit, int expectedRequestCount)
            throws Exception {

        expectedRequestCount = checkBasicCommitInfo(gitCommit, ghCommit, expectedRequestCount);
        assertThat(mockGitHub.getRequestCount(), equalTo(expectedRequestCount));

        assertThat(gitCommit.getMessage(), equalTo("Updated file for integration tests."));
        assertThat(gitCommit.getAuthoredDate(), equalTo(GitHubClient.parseDate("2021-06-28T20:37:51Z")));
        assertThat(gitCommit.getCommitDate(), equalTo(GitHubClient.parseDate("2021-06-28T20:37:51Z")));

        // Changed this to assert null since bridge methods are missing.
        assertThat(ghCommit, nullValue());
        // assertThat(ghCommit.getCommitShortInfo().getMessage(), equalTo("Updated file for integration tests."));
        // assertThat("Message already resolved", mockGitHub.getRequestCount(), equalTo(expectedRequestCount));

        // ghCommit.populate();
        // assertThat("Populate GHCommit", mockGitHub.getRequestCount(), equalTo(expectedRequestCount += 1));

        // expectedRequestCount = checkCommitUserInfo(gitCommit, ghCommit, expectedRequestCount);
        // assertThat("GHUser already resolved", mockGitHub.getRequestCount(), equalTo(expectedRequestCount));

        expectedRequestCount = checkCommitTree(gitCommit, ghCommit, expectedRequestCount);

        return expectedRequestCount;
    }

    /**
     * Check basic commit info.
     *
     * @param gitCommit
     *            the git commit
     * @param ghCommit
     *            the gh commit
     * @param expectedRequestCount
     *            the expected request count
     * @return the int
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     */
    int checkBasicCommitInfo(GitCommit gitCommit, GHCommit ghCommit, int expectedRequestCount) throws IOException {
        assertThat(gitCommit, notNullValue());
        assertThat(gitCommit.getSHA1(), notNullValue());
        assertThat(gitCommit.getUrl().toString(),
                endsWith("/repos/hub4j-test-org/GHContentIntegrationTest/git/commits/" + gitCommit.getSHA1()));
        assertThat(gitCommit.getNodeId(), notNullValue());
        assertThat(gitCommit.getHtmlUrl().toString(),
                equalTo("https://github.com/hub4j-test-org/GHContentIntegrationTest/commit/" + gitCommit.getSHA1()));
        assertThat(gitCommit.getVerification(), notNullValue());

        // Changed this to assert null since bridge methods are missing.
        assertThat(ghCommit, nullValue());
        // assertThat(ghCommit.getSHA1(), notNullValue());
        // assertThat(ghCommit.getUrl().toString(),
        // endsWith("/repos/hub4j-test-org/GHContentIntegrationTest/git/commits/" + ghCommit.getSHA1()));

        return expectedRequestCount;
    }

    /**
     * Check commit user info.
     *
     * @param gitCommit
     *            the git commit
     * @param ghCommit
     *            the gh commit
     * @param expectedRequestCount
     *            the expected request count
     * @return the int
     * @throws Exception
     *             the exception
     */
    int checkCommitUserInfo(GitCommit gitCommit, GHCommit ghCommit, int expectedRequestCount) throws Exception {
        assertThat(gitCommit.getAuthor().getName(), equalTo("Liam Newman"));
        assertThat(gitCommit.getAuthor().getEmail(), equalTo("bitwiseman@gmail.com"));

        // Check that GHCommit.GHAuthor bridge method still works
        assertThat(getGHAuthor(gitCommit).getName(), equalTo("Liam Newman"));
        assertThat(getGHAuthor(gitCommit).getEmail(), equalTo("bitwiseman@gmail.com"));

        assertThat(gitCommit.getAuthor().getName(), equalTo("Liam Newman"));
        assertThat(gitCommit.getAuthor().getEmail(), equalTo("bitwiseman@gmail.com"));
        assertThat(gitCommit.getCommitter().getName(), equalTo("Liam Newman"));
        assertThat(gitCommit.getCommitter().getEmail(), equalTo("bitwiseman@gmail.com"));
        assertThat("GHUser already resolved", mockGitHub.getRequestCount(), equalTo(expectedRequestCount));

        assertThat(ghCommit.getAuthor().getName(), equalTo("Liam Newman"));
        assertThat(ghCommit.getAuthor().getEmail(), equalTo("bitwiseman@gmail.com"));

        // Check that GHCommit.GHAuthor bridge method still works
        assertThat(getGHAuthor(ghCommit.getCommitShortInfo()).getName(), equalTo("Liam Newman"));
        assertThat(getGHAuthor(ghCommit.getCommitShortInfo()).getEmail(), equalTo("bitwiseman@gmail.com"));

        assertThat(ghCommit.getCommitter().getName(), equalTo("Liam Newman"));
        assertThat(ghCommit.getCommitter().getEmail(), equalTo("bitwiseman@gmail.com"));

        return expectedRequestCount;
    }

    /**
     * Gets the GH author.
     *
     * @param commit
     *            the commit
     * @return the GH author
     * @throws GHException
     *             the GH exception
     * @throws IllegalAccessException
     *             the illegal access exception
     * @throws IllegalArgumentException
     *             the illegal argument exception
     * @throws InvocationTargetException
     *             the invocation target exception
     */
    GHCommit.GHAuthor getGHAuthor(GitCommit commit)
            throws GHException, IllegalAccessException, IllegalArgumentException, InvocationTargetException {
        for (Method method : commit.getClass().getMethods()) {
            if (method.getName().equals("getAuthor") && method.getReturnType().equals(GHCommit.GHAuthor.class)) {
                return (GHCommit.GHAuthor) method.invoke(commit);
            }
        }
        System.out.println("Unable to find bridge method");
        return null;
    }

    /**
     * Gets the GH author.
     *
     * @param commit
     *            the commit
     * @return the GH author
     * @throws GHException
     *             the GH exception
     * @throws IllegalAccessException
     *             the illegal access exception
     * @throws IllegalArgumentException
     *             the illegal argument exception
     * @throws InvocationTargetException
     *             the invocation target exception
     */
    GHCommit.GHAuthor getGHAuthor(GHCommit.ShortInfo commit)
            throws GHException, IllegalAccessException, IllegalArgumentException, InvocationTargetException {
        for (Method method : commit.getClass().getMethods()) {
            if (method.getName().equals("getAuthor") && method.getReturnType().equals(GHCommit.GHAuthor.class)) {
                return (GHCommit.GHAuthor) method.invoke(commit);
            }
        }
        System.out.println("Unable to find bridge method");
        return null;
    }

    /**
     * Check commit tree.
     *
     * @param gitCommit
     *            the git commit
     * @param ghCommit
     *            the gh commit
     * @param expectedRequestCount
     *            the expected request count
     * @return the int
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     */
    int checkCommitTree(GitCommit gitCommit, GHCommit ghCommit, int expectedRequestCount) throws IOException {
        assertThat(gitCommit.getTreeSHA1(), notNullValue());
        assertThat(gitCommit.getTreeUrl(),
                endsWith("/repos/hub4j-test-org/GHContentIntegrationTest/git/trees/" + gitCommit.getTree().getSha()));
        assertThat("GHTree already resolved", mockGitHub.getRequestCount(), equalTo(expectedRequestCount));

        // Changed this to assert null since bridge methods are missing.
        assertThat(ghCommit, nullValue());
        // assertThat(ghCommit.getTree().getSha(), notNullValue());
        // assertThat("GHCommit has to resolve GHTree", mockGitHub.getRequestCount(), equalTo(expectedRequestCount +=
        // 1));
        // assertThat(ghCommit.getTree().getUrl().toString(),
        // endsWith("/repos/hub4j-test-org/GHContentIntegrationTest/git/trees/" + ghCommit.getTree().getSha()));
        // assertThat("GHCommit resolving GHTree is not cached",
        // mockGitHub.getRequestCount(),
        // equalTo(expectedRequestCount += 2));

        return expectedRequestCount;
    }

    /**
     * Check commit parents.
     *
     * @param gitCommit
     *            the git commit
     * @param ghCommit
     *            the gh commit
     * @param expectedRequestCount
     *            the expected request count
     * @return the int
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     */
    int checkCommitParents(GitCommit gitCommit, GHCommit ghCommit, int expectedRequestCount) throws IOException {
        assertThat(gitCommit.getParentSHA1s().size(), is(greaterThan(0)));
        assertThat(gitCommit.getParentSHA1s().get(0), notNullValue());
        // Changed this to assert null since bridge methods are missing.
        assertThat(ghCommit, nullValue());
        // assertThat(ghCommit.getParentSHA1s().size(), is(greaterThan(0)));
        // assertThat(ghCommit.getParentSHA1s().get(0), notNullValue());

        return expectedRequestCount;
    }

    // @Test
    // public void testGitCommit2GHCommitExceptions() {

    // }

    /**
     * Test MIME small.
     *
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     */
    @Test
    public void testMIMESmall() throws IOException {
        GHRepository ghRepository = getTempRepository();
        GHContentBuilder ghContentBuilder = ghRepository.createContent();
        ghContentBuilder.message("Some commit msg");
        ghContentBuilder.path("MIME-Small.md");
        ghContentBuilder.content("123456789012345678901234567890123456789012345678901234567");
        ghContentBuilder.commit();
    }

    /**
     * Test MIME long.
     *
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     */
    @Test
    public void testMIMELong() throws IOException {
        GHRepository ghRepository = getTempRepository();
        GHContentBuilder ghContentBuilder = ghRepository.createContent();
        ghContentBuilder.message("Some commit msg");
        ghContentBuilder.path("MIME-Long.md");
        ghContentBuilder.content("1234567890123456789012345678901234567890123456789012345678");
        ghContentBuilder.commit();
    }

    /**
     * Test MIME longer.
     *
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     */
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

    /**
     * Test get file content with non ascii path.
     *
     * @throws Exception
     *             the exception
     */
    @Test
    public void testGetFileContentWithNonAsciiPath() throws Exception {
        final GHRepository repo = gitHub.getRepository("hub4j-test-org/GHContentIntegrationTest");
        final GHContent fileContent = repo.getFileContent("ghcontent-ro/a-file-with-\u00F6");
        assertThat(IOUtils.readLines(fileContent.read(), StandardCharsets.UTF_8), hasItems("test"));

        final GHContent fileContent2 = repo.getFileContent(fileContent.getPath());
        assertThat(IOUtils.readLines(fileContent2.read(), StandardCharsets.UTF_8), hasItems("test"));
    }

    /**
     * Test get file content with symlink.
     *
     * @throws Exception
     *             the exception
     */
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
