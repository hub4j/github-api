package org.kohsuke.github;

import org.apache.commons.io.IOUtils;
import org.junit.Test;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import static org.hamcrest.Matchers.*;
import static org.hamcrest.core.IsInstanceOf.instanceOf;

/**
 * @author Liam Newman
 */
public class GHRepositoryTest extends AbstractGitHubWireMockTest {

    protected GHRepository getRepository() throws IOException {
        return getRepository(gitHub);
    }

    private GHRepository getRepository(GitHub gitHub) throws IOException {
        return gitHub.getOrganization("github-api-test-org").getRepository("github-api");
    }

    @Test
    public void archive() throws Exception {
        snapshotNotAllowed();

        // Archive is a one-way action in the API.
        // We do thi this one
        GHRepository repo = getRepository();

        assertThat(repo.isArchived(), is(false));

        repo.archive();

        assertThat(repo.isArchived(), is(true));
        assertThat(getRepository().isArchived(), is(true));
    }

    @Test
    public void getBranch_URLEncoded() throws Exception {
        GHRepository repo = getRepository();
        GHBranch branch = repo.getBranch("test/#UrlEncode");
        assertThat(branch.getName(), is("test/#UrlEncode"));
    }

    // Issue #607
    @Test
    public void getBranchNonExistentBut200Status() throws Exception {
        // Manually changed the returned status to 200 so dont take a new snapshot
        this.snapshotNotAllowed();

        // This should *never* happen but with mocking it was discovered
        GHRepository repo = getRepository();
        try {
            GHBranch branch = repo.getBranch("test/NonExistent");
            fail();
        } catch (Exception e) {
            // I dont really love this but I wanted to get to the root wrapped cause
            assertThat(e, instanceOf(IOException.class));
            assertThat(e.getMessage(),
                    equalTo("Server returned HTTP response code: 200, message: 'OK' for URL: "
                            + mockGitHub.apiServer().baseUrl()
                            + "/repos/github-api-test-org/github-api/branches/test/NonExistent"));
        }
    }

    @Test
    public void subscription() throws Exception {
        GHRepository r = getRepository();
        assertNull(r.getSubscription());

        GHSubscription s = r.subscribe(true, false);
        assertEquals(s.getRepository(), r);

        s.delete();

        assertNull(r.getSubscription());
    }

    @Test
    public void testSetPublic() throws Exception {
        kohsuke();
        GHUser myself = gitHub.getMyself();
        String repoName = "test-repo-public";
        GHRepository repo = gitHub.createRepository(repoName).private_(false).create();
        try {
            assertFalse(repo.isPrivate());
            repo.setPrivate(true);
            assertTrue(myself.getRepository(repoName).isPrivate());
            repo.setPrivate(false);
            assertFalse(myself.getRepository(repoName).isPrivate());
        } finally {
            repo.delete();
        }
    }

    @Test
    public void listContributors() throws IOException {
        GHRepository r = gitHub.getOrganization("github-api").getRepository("github-api");
        int i = 0;
        boolean kohsuke = false;

        for (GHRepository.Contributor c : r.listContributors()) {
            if (c.getLogin().equals("kohsuke")) {
                assertTrue(c.getContributions() > 0);
                kohsuke = true;
            }
            if (i++ > 5) {
                break;
            }
        }

        assertTrue(kohsuke);
    }

    @Test
    public void getPermission() throws Exception {
        kohsuke();
        GHRepository r = gitHub.getRepository("github-api-test-org/test-permission");
        assertEquals(GHPermissionType.ADMIN, r.getPermission("kohsuke"));
        assertEquals(GHPermissionType.READ, r.getPermission("dude"));
        r = gitHub.getOrganization("apache").getRepository("groovy");
        try {
            r.getPermission("jglick");
            fail();
        } catch (HttpException x) {
            // x.printStackTrace(); // good
            assertEquals(403, x.getResponseCode());
        }

        if (false) {
            // can't easily test this; there's no private repository visible to the test user
            r = gitHub.getOrganization("cloudbees").getRepository("private-repo-not-writable-by-me");
            try {
                r.getPermission("jglick");
                fail();
            } catch (FileNotFoundException x) {
                x.printStackTrace(); // good
            }
        }
    }

    @Test
    public void LatestRepositoryExist() {
        try {
            // add the repository that have latest release
            GHRelease release = gitHub.getRepository("kamontat/CheckIDNumber").getLatestRelease();
            assertEquals("v3.0", release.getTagName());
        } catch (IOException e) {
            e.printStackTrace();
            fail();
        }
    }

    @Test
    public void LatestRepositoryNotExist() {
        try {
            // add the repository that `NOT` have latest release
            GHRelease release = gitHub.getRepository("kamontat/Java8Example").getLatestRelease();
            assertNull(release);
        } catch (IOException e) {
            e.printStackTrace();
            fail();
        }
    }

    @Test
    public void listReleases() throws IOException {
        PagedIterable<GHRelease> releases = gitHub.getOrganization("github").getRepository("hub").listReleases();
        assertTrue(releases.iterator().hasNext());
    }

    @Test
    public void getReleaseExists() throws IOException {
        GHRelease release = gitHub.getOrganization("github").getRepository("hub").getRelease(6839710);
        assertEquals("v2.3.0-pre10", release.getTagName());
    }

    @Test
    public void getReleaseDoesNotExist() throws IOException {
        GHRelease release = gitHub.getOrganization("github").getRepository("hub").getRelease(Long.MAX_VALUE);
        assertNull(release);
    }

    @Test
    public void getReleaseByTagNameExists() throws IOException {
        GHRelease release = gitHub.getOrganization("github").getRepository("hub").getReleaseByTagName("v2.3.0-pre10");
        assertNotNull(release);
        assertEquals("v2.3.0-pre10", release.getTagName());
    }

    @Test
    public void getReleaseByTagNameDoesNotExist() throws IOException {
        GHRelease release = getRepository().getReleaseByTagName("foo-bar-baz");
        assertNull(release);
    }

    @Test
    public void listLanguages() throws IOException {
        GHRepository r = gitHub.getRepository("github-api/github-api");
        String mainLanguage = r.getLanguage();
        assertTrue(r.listLanguages().containsKey(mainLanguage));
    }

    @Test // Issue #261
    public void listEmptyContributors() throws IOException {
        for (GHRepository.Contributor c : gitHub.getRepository(GITHUB_API_TEST_ORG + "/empty").listContributors()) {
            // System.out.println(c);
            fail("This list should be empty, but should return a valid empty iterable.");
        }
    }

    @Test
    public void searchRepositories() throws Exception {
        PagedSearchIterable<GHRepository> r = gitHub.searchRepositories()
                .q("tetris")
                .language("assembly")
                .sort(GHRepositorySearchBuilder.Sort.STARS)
                .list();
        GHRepository u = r.iterator().next();
        // System.out.println(u.getName());
        assertNotNull(u.getId());
        assertEquals("Assembly", u.getLanguage());
        assertTrue(r.getTotalCount() > 0);
    }

    @Test // issue #162
    public void testIssue162() throws Exception {
        GHRepository r = gitHub.getRepository("github-api/github-api");
        List<GHContent> contents = r.getDirectoryContent("", "gh-pages");
        for (GHContent content : contents) {
            if (content.isFile()) {
                String content1 = content.getContent();
                String content2 = r.getFileContent(content.getPath(), "gh-pages").getContent();
                // System.out.println(content.getPath());
                assertEquals(content1, content2);
            }
        }
    }

    @Test
    public void markDown() throws Exception {
        assertEquals("<p><strong>Test日本語</strong></p>", IOUtils.toString(gitHub.renderMarkdown("**Test日本語**")).trim());

        String actual = IOUtils.toString(gitHub.getRepository("github-api/github-api")
                .renderMarkdown("@kohsuke to fix issue #1", MarkdownMode.GFM));
        // System.out.println(actual);
        assertTrue(actual.contains("href=\"https://github.com/kohsuke\""));
        assertTrue(actual.contains("href=\"https://github.com/github-api/github-api/pull/1\""));
        assertTrue(actual.contains("class=\"user-mention\""));
        assertTrue(actual.contains("class=\"issue-link "));
        assertTrue(actual.contains("to fix issue"));
    }

    @Test
    public void getMergeOptions() throws IOException {
        GHRepository r = getTempRepository();
        assertNotNull(r.isAllowMergeCommit());
        assertNotNull(r.isAllowRebaseMerge());
        assertNotNull(r.isAllowSquashMerge());
    }

    @Test
    public void setMergeOptions() throws IOException {
        // String repoName = "github-api-test-org/test-mergeoptions";
        GHRepository r = getTempRepository();

        // at least one merge option must be selected
        // flip all the values at least once
        r.allowSquashMerge(true);

        r.allowMergeCommit(false);
        r.allowRebaseMerge(false);

        r = gitHub.getRepository(r.getFullName());
        assertFalse(r.isAllowMergeCommit());
        assertFalse(r.isAllowRebaseMerge());
        assertTrue(r.isAllowSquashMerge());

        // flip the last value
        r.allowMergeCommit(true);
        r.allowRebaseMerge(true);
        r.allowSquashMerge(false);

        r = gitHub.getRepository(r.getFullName());
        assertTrue(r.isAllowMergeCommit());
        assertTrue(r.isAllowRebaseMerge());
        assertFalse(r.isAllowSquashMerge());
    }

    @Test
    public void getDeleteBranchOnMerge() throws IOException {
        GHRepository r = getTempRepository();
        assertNotNull(r.isDeleteBranchOnMerge());
    }

    @Test
    public void setDeleteBranchOnMerge() throws IOException {
        GHRepository r = getTempRepository();

        // enable auto delete
        r.deleteBranchOnMerge(true);

        r = gitHub.getRepository(r.getFullName());
        assertTrue(r.isDeleteBranchOnMerge());

        // flip the last value
        r.deleteBranchOnMerge(false);

        r = gitHub.getRepository(r.getFullName());
        assertFalse(r.isDeleteBranchOnMerge());
    }

    @Test
    public void testSetTopics() throws Exception {
        GHRepository repo = getRepository(gitHub);

        List<String> topics = new ArrayList<>();

        topics.add("java");
        topics.add("api-test-dummy");
        repo.setTopics(topics);
        assertThat("Topics retain input order (are not sort when stored)",
                repo.listTopics(),
                contains("java", "api-test-dummy"));

        topics = new ArrayList<>();
        topics.add("ordered-state");
        topics.add("api-test-dummy");
        topics.add("java");
        repo.setTopics(topics);
        assertThat("Topics behave as a set and retain order from previous calls",
                repo.listTopics(),
                contains("java", "api-test-dummy", "ordered-state"));

        topics = new ArrayList<>();
        topics.add("ordered-state");
        topics.add("api-test-dummy");
        repo.setTopics(topics);
        assertThat("Topics retain order even when some are removed",
                repo.listTopics(),
                contains("api-test-dummy", "ordered-state"));

        topics = new ArrayList<>();
        repo.setTopics(topics);
        assertTrue("Topics can be set to empty", repo.listTopics().isEmpty());
    }

    @Test
    public void getCollaborators() throws Exception {
        GHRepository repo = getRepository(gitHub);
        GHPersonSet<GHUser> collaborators = repo.getCollaborators();
        assertThat(collaborators.size(), greaterThan(0));
    }

    @Test
    public void getPostCommitHooks() throws Exception {
        GHRepository repo = getRepository(gitHub);
        Set<URL> postcommitHooks = repo.getPostCommitHooks();
        assertThat(postcommitHooks.size(), equalTo(0));
    }

    @Test
    public void getRefs() throws Exception {
        GHRepository repo = getTempRepository();
        GHRef[] refs = repo.getRefs();
        assertThat(refs, notNullValue());
        assertThat(refs.length, equalTo(1));
        assertThat(refs[0].getRef(), equalTo("refs/heads/master"));
    }

    @Test
    public void getRefsHeads() throws Exception {
        GHRepository repo = getTempRepository();
        GHRef[] refs = repo.getRefs("heads");
        assertThat(refs, notNullValue());
        assertThat(refs.length, equalTo(1));
        assertThat(refs[0].getRef(), equalTo("refs/heads/master"));
    }

    @Test
    public void getRefsEmptyTags() throws Exception {
        GHRepository repo = getTempRepository();
        try {
            repo.getRefs("tags");
            fail();
        } catch (Exception e) {
            assertThat(e, instanceOf(GHFileNotFoundException.class));
            assertThat(e.getMessage(),
                    containsString(
                            "{\"message\":\"Not Found\",\"documentation_url\":\"https://developer.github.com/v3/git/refs/#get-a-reference\"}"));
            assertThat(e.getCause(), instanceOf(FileNotFoundException.class));
        }
    }

    @Test
    public void listRefs() throws Exception {
        GHRepository repo = getTempRepository();
        List<GHRef> refs = repo.listRefs().asList();
        assertThat(refs, notNullValue());
        assertThat(refs.size(), equalTo(1));
        assertThat(refs.get(0).getRef(), equalTo("refs/heads/master"));
    }

    @Test
    public void listRefsHeads() throws Exception {
        GHRepository repo = getTempRepository();
        List<GHRef> refs = repo.listRefs("heads").asList();
        assertThat(refs, notNullValue());
        assertThat(refs.size(), equalTo(1));
        assertThat(refs.get(0).getRef(), equalTo("refs/heads/master"));
    }

    @Test
    public void listRefsEmptyTags() throws Exception {
        try {
            GHRepository repo = getTempRepository();
            repo.listRefs("tags").asList();
            fail();
        } catch (Exception e) {
            assertThat(e, instanceOf(GHException.class));
            assertThat(e.getMessage(), containsString("Failed to retrieve "));
            assertThat(e.getMessage(),
                    containsString("/repos/github-api-test-org/temp-listRefsEmptyTags/git/refs/tags"));
            assertThat(e.getCause(), instanceOf(GHFileNotFoundException.class));
        }
    }

    @Test
    public void listTagsEmpty() throws Exception {
        GHRepository repo = getTempRepository();
        List<GHTag> refs = repo.listTags().asList();
        assertThat(refs, notNullValue());
        assertThat(refs.size(), equalTo(0));
    }

    @Test
    public void listTags() throws Exception {
        GHRepository repo = getRepository();
        List<GHTag> refs = repo.listTags().withPageSize(33).asList();
        assertThat(refs, notNullValue());
        assertThat(refs.size(), greaterThan(90));
    }

    @Test
    public void checkWatchersCount() throws Exception {
        snapshotNotAllowed();
        GHRepository repo = getTempRepository();
        int watchersCount = repo.getWatchers();
        assertEquals(10, watchersCount);
    }

    @Test
    public void checkStargazersCount() throws Exception {
        snapshotNotAllowed();
        GHRepository repo = getTempRepository();
        int stargazersCount = repo.getStargazersCount();
        assertEquals(10, stargazersCount);
    }
}
