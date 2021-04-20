package org.kohsuke.github;

import com.fasterxml.jackson.databind.JsonMappingException;
import org.apache.commons.io.IOUtils;
import org.junit.Test;
import org.kohsuke.github.GHCheckRun.Conclusion;
import org.kohsuke.github.GHRepository.Visibility;

import java.io.ByteArrayInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static org.hamcrest.Matchers.*;
import static org.hamcrest.core.IsInstanceOf.instanceOf;
import static org.kohsuke.github.GHVerification.Reason.*;

/**
 * @author Liam Newman
 */
public class GHRepositoryTest extends AbstractGitHubWireMockTest {

    protected GHRepository getRepository() throws IOException {
        return getRepository(gitHub);
    }

    private GHRepository getRepository(GitHub gitHub) throws IOException {
        return gitHub.getOrganization("hub4j-test-org").getRepository("github-api");
    }

    @Test
    public void testZipball() throws IOException {
        getTempRepository().readZip((InputStream inputstream) -> {
            return new ByteArrayInputStream(IOUtils.toByteArray(inputstream));
        }, null);
    }

    @Test
    public void testTarball() throws IOException {
        getTempRepository().readTar((InputStream inputstream) -> {
            return new ByteArrayInputStream(IOUtils.toByteArray(inputstream));
        }, null);
    }

    @Test
    public void testGetters() throws IOException {
        GHRepository r = getTempRepository();

        assertThat(r.hasAdminAccess(), is(true));
        assertThat(r.hasDownloads(), is(true));
        assertThat(r.hasIssues(), is(true));
        assertThat(r.hasPages(), is(false));
        assertThat(r.hasProjects(), is(true));
        assertThat(r.hasPullAccess(), is(true));
        assertThat(r.hasPushAccess(), is(true));
        assertThat(r.hasWiki(), is(true));

        assertThat(r.isAllowMergeCommit(), is(true));
        assertThat(r.isAllowRebaseMerge(), is(true));
        assertThat(r.isAllowSquashMerge(), is(true));

        String httpTransport = "https://github.com/hub4j-test-org/temp-testGetters.git";
        assertThat(r.getHttpTransportUrl(), equalTo(httpTransport));
        assertThat(r.gitHttpTransportUrl(), equalTo(httpTransport));

        assertThat(r.getName(), equalTo("temp-testGetters"));
        assertThat(r.getFullName(), equalTo("hub4j-test-org/temp-testGetters"));
    }

    @Test
    public void archive() throws Exception {
        // Archive is a one-way action in the API.
        // After taking snapshot, manual state reset is required.
        snapshotNotAllowed();

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

    @Test
    public void createSignedCommitVerifyError() throws IOException {
        GHRepository repository = getRepository();

        GHTree ghTree = new GHTreeBuilder(repository).textEntry("a", "", false).create();

        GHVerification verification = repository.createCommit()
                .message("test signing")
                .withSignature("-----BEGIN PGP SIGNATURE-----\ninvalid\n-----END PGP SIGNATURE-----")
                .tree(ghTree.getSha())
                .create()
                .getCommitShortInfo()
                .getVerification();

        assertThat(verification.getReason(), equalTo(GPGVERIFY_ERROR));
    }

    @Test
    public void createSignedCommitUnknownSignatureType() throws IOException {
        GHRepository repository = getRepository();

        GHTree ghTree = new GHTreeBuilder(repository).textEntry("a", "", false).create();

        GHVerification verification = repository.createCommit()
                .message("test signing")
                .withSignature("unknown")
                .tree(ghTree.getSha())
                .create()
                .getCommitShortInfo()
                .getVerification();

        assertThat(verification.getReason(), equalTo(UNKNOWN_SIGNATURE_TYPE));
    }

    @Test
    public void listStargazers() throws IOException {
        GHRepository repository = getRepository();
        assertThat(repository.listStargazers2().toList(), is(empty()));

        repository = gitHub.getOrganization("hub4j").getRepository("github-api");
        Iterable<GHStargazer> stargazers = repository.listStargazers2();
        GHStargazer stargazer = stargazers.iterator().next();
        assertThat(stargazer.getStarredAt(), equalTo(new Date(1271650383000L)));
        assertThat(stargazer.getUser().getLogin(), equalTo("nielswind"));
        assertThat(stargazer.getRepository(), sameInstance(repository));
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
                    equalTo("Server returned HTTP response code: 200, message: '404 Not Found' for URL: "
                            + mockGitHub.apiServer().baseUrl()
                            + "/repos/hub4j-test-org/github-api/branches/test/NonExistent"));
        }
    }

    @Test
    public void subscription() throws Exception {
        GHRepository r = getRepository();
        assertThat(r.getSubscription(), nullValue());
        GHSubscription s = r.subscribe(true, false);
        try {

            assertThat(r, equalTo(s.getRepository()));
            assertThat(s.isIgnored(), equalTo(false));
            assertThat(s.isSubscribed(), equalTo(true));
            assertThat(s.getRepositoryUrl().toString(), containsString("/repos/hub4j-test-org/github-api"));
            assertThat(s.getUrl().toString(), containsString("/repos/hub4j-test-org/github-api/subscription"));

            assertThat(s.getReason(), nullValue());
            assertThat(s.getCreatedAt(), equalTo(new Date(1611377286000L)));
        } finally {
            s.delete();
        }

        assertThat(r.getSubscription(), nullValue());
    }

    @Test
    public void testSetPublic() throws Exception {
        kohsuke();
        GHUser myself = gitHub.getMyself();
        String repoName = "test-repo-public";
        GHRepository repo = gitHub.createRepository(repoName).private_(false).create();
        try {
            assertThat(repo.isPrivate(), is(false));
            repo.setPrivate(true);
            assertThat(myself.getRepository(repoName).isPrivate(), is(true));
            repo.setPrivate(false);
            assertThat(myself.getRepository(repoName).isPrivate(), is(false));
        } finally {
            repo.delete();
        }
    }

    @Test
    public void testUpdateRepository() throws Exception {
        String homepage = "https://github-api.kohsuke.org/apidocs/index.html";
        String description = "A test repository for update testing via the github-api project";

        GHRepository repo = getTempRepository();
        GHRepository.Updater builder = repo.update();

        // one merge option is always required
        GHRepository updated = builder.allowRebaseMerge(false)
                .allowSquashMerge(false)
                .deleteBranchOnMerge(true)
                .description(description)
                .downloads(false)
                .downloads(false)
                .homepage(homepage)
                .issues(false)
                .private_(true)
                .projects(false)
                .wiki(false)
                .done();

        assertThat(updated.isAllowMergeCommit(), is(true));
        assertThat(updated.isAllowRebaseMerge(), is(false));
        assertThat(updated.isAllowSquashMerge(), is(false));
        assertThat(updated.isDeleteBranchOnMerge(), is(true));
        assertThat(updated.isPrivate(), is(true));
        assertThat(updated.hasDownloads(), is(false));
        assertThat(updated.hasIssues(), is(false));
        assertThat(updated.hasProjects(), is(false));
        assertThat(updated.hasWiki(), is(false));

        assertThat(updated.getHomepage(), equalTo(homepage));
        assertThat(updated.getDescription(), equalTo(description));

        // test the other merge option and making the repo public again
        GHRepository redux = updated.update().allowMergeCommit(false).allowRebaseMerge(true).private_(false).done();

        assertThat(redux.isAllowMergeCommit(), is(false));
        assertThat(redux.isAllowRebaseMerge(), is(true));
        assertThat(redux.isPrivate(), is(false));

        String updatedDescription = "updated using set()";
        redux = redux.set().description(updatedDescription);

        assertThat(redux.getDescription(), equalTo(updatedDescription));
    }

    @Test
    public void testGetRepositoryWithVisibility() throws IOException {
        snapshotNotAllowed();
        final String repoName = "test-repo-visibility";
        final GHRepository repo = getTempRepository(repoName);
        assertThat(repo.getVisibility(), equalTo(Visibility.PUBLIC));

        repo.setVisibility(Visibility.INTERNAL);
        assertThat(gitHub.getRepository(repo.getOwnerName() + "/" + repo.getName()).getVisibility(),
                equalTo(Visibility.INTERNAL));

        repo.setVisibility(Visibility.PRIVATE);
        assertThat(gitHub.getRepository(repo.getOwnerName() + "/" + repo.getName()).getVisibility(),
                equalTo(Visibility.PRIVATE));

        repo.setVisibility(Visibility.PUBLIC);
        assertThat(gitHub.getRepository(repo.getOwnerName() + "/" + repo.getName()).getVisibility(),
                equalTo(Visibility.PUBLIC));

        // deliberately bogus response in snapshot
        assertThat(gitHub.getRepository(repo.getOwnerName() + "/" + repo.getName()).getVisibility(),
                equalTo(Visibility.UNKNOWN));
    }

    @Test
    public void listContributors() throws IOException {
        GHRepository r = gitHub.getOrganization("hub4j").getRepository("github-api");
        int i = 0;
        boolean kohsuke = false;

        for (GHRepository.Contributor c : r.listContributors()) {
            if (c.getLogin().equals("kohsuke")) {
                assertThat(c.getContributions(), greaterThan(0));
                kohsuke = true;
            }
            if (i++ > 5) {
                break;
            }
        }

        assertThat(kohsuke, is(true));
    }

    @Test
    public void getPermission() throws Exception {
        kohsuke();
        GHRepository r = gitHub.getRepository("hub4j-test-org/test-permission");
        assertThat(r.getPermission("kohsuke"), equalTo(GHPermissionType.ADMIN));
        assertThat(r.getPermission("dude"), equalTo(GHPermissionType.READ));
        r = gitHub.getOrganization("apache").getRepository("groovy");
        try {
            r.getPermission("jglick");
            fail();
        } catch (HttpException x) {
            // x.printStackTrace(); // good
            assertThat(x.getResponseCode(), equalTo(403));
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
            assertThat(release.getTagName(), equalTo("v3.0"));
        } catch (IOException e) {
            e.printStackTrace();
            fail();
        }
    }

    @Test
    public void addCollaborators() throws Exception {
        GHRepository repo = getRepository();
        GHUser user = getUser();
        List<GHUser> users = new ArrayList<GHUser>();

        users.add(user);
        users.add(gitHub.getUser("jimmysombrero2"));
        repo.addCollaborators(users, GHOrganization.Permission.PUSH);

        GHPersonSet<GHUser> collabs = repo.getCollaborators();

        GHUser colabUser = collabs.byLogin("jimmysombrero");

        assertThat(user.getName(), equalTo(colabUser.getName()));
    }

    @Test
    public void LatestRepositoryNotExist() {
        try {
            // add the repository that `NOT` have latest release
            GHRelease release = gitHub.getRepository("kamontat/Java8Example").getLatestRelease();
            assertThat(release, nullValue());
        } catch (IOException e) {
            e.printStackTrace();
            fail();
        }
    }

    @Test
    public void listReleases() throws IOException {
        PagedIterable<GHRelease> releases = gitHub.getOrganization("github").getRepository("hub").listReleases();
        assertThat(releases, is(not(emptyIterable())));
    }

    @Test
    public void getReleaseExists() throws IOException {
        GHRelease release = gitHub.getOrganization("github").getRepository("hub").getRelease(6839710);
        assertThat(release.getTagName(), equalTo("v2.3.0-pre10"));
    }

    @Test
    public void getReleaseDoesNotExist() throws IOException {
        GHRelease release = gitHub.getOrganization("github").getRepository("hub").getRelease(Long.MAX_VALUE);
        assertThat(release, nullValue());
    }

    @Test
    public void getReleaseByTagNameExists() throws IOException {
        GHRelease release = gitHub.getOrganization("github").getRepository("hub").getReleaseByTagName("v2.3.0-pre10");
        assertThat(release, notNullValue());
        assertThat(release.getTagName(), equalTo("v2.3.0-pre10"));
    }

    @Test
    public void getReleaseByTagNameDoesNotExist() throws IOException {
        GHRelease release = getRepository().getReleaseByTagName("foo-bar-baz");
        assertThat(release, nullValue());
    }

    @Test
    public void listLanguages() throws IOException {
        GHRepository r = gitHub.getRepository("hub4j/github-api");
        String mainLanguage = r.getLanguage();
        assertThat(mainLanguage, equalTo("Java"));
        Map<String, Long> languages = r.listLanguages();
        assertThat(languages.containsKey(mainLanguage), is(true));
        assertThat(languages.get("Java"), greaterThan(100000L));
    }

    @Test
    public void listCommitCommentsNoComments() throws IOException {
        List<GHCommitComment> commitComments = getRepository()
                .listCommitComments("c413fc1e3057332b93850ea48202627d29a37de5")
                .toList();

        assertThat("Commit has no comments", commitComments.isEmpty());
    }

    @Test
    public void listCommitCommentsSomeComments() throws IOException {
        List<GHCommitComment> commitComments = getRepository()
                .listCommitComments("499d91f9f846b0087b2a20cf3648b49dc9c2eeef")
                .toList();

        assertThat("Two comments present", commitComments.size(), equalTo(2));
        assertThat("Comment text found",
                commitComments.stream().map(GHCommitComment::getBody).collect(Collectors.toList()),
                containsInAnyOrder("comment 1", "comment 2"));
    }

    @Test // Issue #261
    public void listEmptyContributors() throws IOException {
        assertThat("This list should be empty, but should return a valid empty iterable.",
                gitHub.getRepository(GITHUB_API_TEST_ORG + "/empty").listContributors(),
                is(emptyIterable()));
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
        assertThat(u.getId(), notNullValue());
        assertThat(u.getLanguage(), equalTo("Assembly"));
        assertThat(r.getTotalCount(), greaterThan(0));
    }

    @Test // issue #162
    public void testIssue162() throws Exception {
        GHRepository r = gitHub.getRepository("hub4j/github-api");
        List<GHContent> contents = r.getDirectoryContent("", "gh-pages");
        for (GHContent content : contents) {
            if (content.isFile()) {
                String content1 = content.getContent();
                String content2 = r.getFileContent(content.getPath(), "gh-pages").getContent();
                // System.out.println(content.getPath());
                assertThat(content2, equalTo(content1));
            }
        }
    }

    @Test
    public void markDown() throws Exception {
        assertThat(IOUtils.toString(gitHub.renderMarkdown("**Test日本語**")).trim(),
                equalTo("<p><strong>Test日本語</strong></p>"));

        String actual = IOUtils.toString(
                gitHub.getRepository("hub4j/github-api").renderMarkdown("@kohsuke to fix issue #1", MarkdownMode.GFM));
        // System.out.println(actual);
        assertThat(actual, containsString("href=\"https://github.com/kohsuke\""));
        assertThat(actual, containsString("href=\"https://github.com/hub4j/github-api/pull/1\""));
        assertThat(actual, containsString("class=\"user-mention\""));
        assertThat(actual, containsString("class=\"issue-link "));
        assertThat(actual, containsString("to fix issue"));
    }

    @Test
    public void setMergeOptions() throws IOException {
        // String repoName = "hub4j-test-org/test-mergeoptions";
        GHRepository r = getTempRepository();

        // at least one merge option must be selected
        // flip all the values at least once
        r.allowSquashMerge(true);

        r.allowMergeCommit(false);
        r.allowRebaseMerge(false);

        r = gitHub.getRepository(r.getFullName());
        assertThat(r.isAllowMergeCommit(), is(false));
        assertThat(r.isAllowRebaseMerge(), is(false));
        assertThat(r.isAllowSquashMerge(), is(true));

        // flip the last value
        r.allowMergeCommit(true);
        r.allowRebaseMerge(true);
        r.allowSquashMerge(false);

        r = gitHub.getRepository(r.getFullName());
        assertThat(r.isAllowMergeCommit(), is(true));
        assertThat(r.isAllowRebaseMerge(), is(true));
        assertThat(r.isAllowSquashMerge(), is(false));
    }

    @Test
    public void getDeleteBranchOnMerge() throws IOException {
        GHRepository r = getRepository();
        assertThat(r.isDeleteBranchOnMerge(), notNullValue());
    }

    @Test
    public void setDeleteBranchOnMerge() throws IOException {
        GHRepository r = getRepository();

        // enable auto delete
        r.deleteBranchOnMerge(true);

        r = gitHub.getRepository(r.getFullName());
        assertThat(r.isDeleteBranchOnMerge(), is(true));

        // flip the last value
        r.deleteBranchOnMerge(false);

        r = gitHub.getRepository(r.getFullName());
        assertThat(r.isDeleteBranchOnMerge(), is(false));
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
        assertThat("Topics can be set to empty", repo.listTopics(), is(empty()));
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
        assertThat(postcommitHooks, is(empty()));
    }

    @Test
    public void getRefs() throws Exception {
        GHRepository repo = getTempRepository();
        GHRef[] refs = repo.getRefs();
        assertThat(refs, notNullValue());
        assertThat(refs.length, equalTo(1));
        assertThat(refs[0].getRef(), equalTo("refs/heads/main"));
    }

    @Test
    public void getRefsHeads() throws Exception {
        GHRepository repo = getTempRepository();
        GHRef[] refs = repo.getRefs("heads");
        assertThat(refs, notNullValue());
        assertThat(refs.length, equalTo(1));
        assertThat(refs[0].getRef(), equalTo("refs/heads/main"));
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
        }
    }

    @Test
    public void listRefs() throws Exception {
        GHRepository repo = getRepository();

        List<GHRef> ghRefs;

        // handle refs/*
        ghRefs = repo.listRefs("heads").toList();
        List<GHRef> ghRefsWithPrefix = repo.listRefs("refs/heads").toList();

        assertThat(ghRefs, notNullValue());
        assertThat(ghRefs.size(), greaterThan(3));
        assertThat(ghRefs.get(0).getRef(), equalTo("refs/heads/changes"));
        assertThat(ghRefsWithPrefix.size(), equalTo(ghRefs.size()));
        assertThat(ghRefsWithPrefix.get(0).getRef(), equalTo(ghRefs.get(0).getRef()));

        // git/refs/heads/gh-pages
        // passing a specific ref to listRefs will fail to parse due to returning a single item not an array
        try {
            ghRefs = repo.listRefs("heads/gh-pages").toList();
            fail();
        } catch (Exception e) {
            assertThat(e, instanceOf(HttpException.class));
            assertThat(e.getCause(), instanceOf(JsonMappingException.class));
        }

        // git/refs/heads/gh
        ghRefs = repo.listRefs("heads/gh").toList();
        assertThat(ghRefs, notNullValue());
        assertThat(ghRefs.size(), equalTo(1));
        assertThat(ghRefs.get(0).getRef(), equalTo("refs/heads/gh-pages"));

        // git/refs/headz
        try {
            ghRefs = repo.listRefs("headz").toList();
            fail();
        } catch (Exception e) {
            assertThat(e, instanceOf(GHFileNotFoundException.class));
            assertThat(e.getMessage(),
                    containsString(
                            "{\"message\":\"Not Found\",\"documentation_url\":\"https://developer.github.com/v3/git/refs/#get-a-reference\"}"));
        }
    }

    @Test
    public void getRef() throws Exception {
        GHRepository repo = getRepository();

        GHRef ghRef;

        // handle refs/*
        ghRef = repo.getRef("heads/gh-pages");
        GHRef ghRefWithPrefix = repo.getRef("refs/heads/gh-pages");

        assertThat(ghRef, notNullValue());
        assertThat(ghRef.getRef(), equalTo("refs/heads/gh-pages"));
        assertThat(ghRefWithPrefix.getRef(), equalTo(ghRef.getRef()));

        // git/refs/heads/gh-pages
        ghRef = repo.getRef("heads/gh-pages");
        assertThat(ghRef, notNullValue());
        assertThat(ghRef.getRef(), equalTo("refs/heads/gh-pages"));

        // git/refs/heads/gh
        try {
            ghRef = repo.getRef("heads/gh");
            fail();
        } catch (Exception e) {
            assertThat(e, instanceOf(GHFileNotFoundException.class));
            assertThat(e.getMessage(),
                    containsString(
                            "{\"message\":\"Not Found\",\"documentation_url\":\"https://developer.github.com/v3/git/refs/#get-a-reference\"}"));
        }

        // git/refs/headz
        try {
            ghRef = repo.getRef("headz");
            fail();
        } catch (Exception e) {
            assertThat(e, instanceOf(GHFileNotFoundException.class));
            assertThat(e.getMessage(),
                    containsString(
                            "{\"message\":\"Not Found\",\"documentation_url\":\"https://developer.github.com/v3/git/refs/#get-a-reference\"}"));
        }
    }

    @Test
    public void listRefsHeads() throws Exception {
        GHRepository repo = getTempRepository();
        List<GHRef> refs = repo.listRefs("heads").toList();
        assertThat(refs, notNullValue());
        assertThat(refs.size(), equalTo(1));
        assertThat(refs.get(0).getRef(), equalTo("refs/heads/main"));
    }

    @Test
    public void listRefsEmptyTags() throws Exception {
        try {
            GHRepository repo = getTempRepository();
            repo.listRefs("tags").toList();
            fail();
        } catch (Exception e) {
            assertThat(e, instanceOf(GHFileNotFoundException.class));
            assertThat(e.getMessage(), containsString("/repos/hub4j-test-org/temp-listRefsEmptyTags/git/refs/tags"));
        }
    }

    @Test
    public void listTagsEmpty() throws Exception {
        GHRepository repo = getTempRepository();
        List<GHTag> refs = repo.listTags().toList();
        assertThat(refs, notNullValue());
        assertThat(refs, is(empty()));
    }

    @Test
    public void listTags() throws Exception {
        GHRepository repo = getRepository();
        List<GHTag> refs = repo.listTags().withPageSize(33).toList();
        assertThat(refs, notNullValue());
        assertThat(refs.size(), greaterThan(90));
    }

    @Test
    public void checkWatchersCount() throws Exception {
        snapshotNotAllowed();
        GHRepository repo = getTempRepository();
        int watchersCount = repo.getWatchersCount();
        assertThat(watchersCount, equalTo(10));
    }

    @Test
    public void checkStargazersCount() throws Exception {
        snapshotNotAllowed();
        GHRepository repo = getTempRepository();
        int stargazersCount = repo.getStargazersCount();
        assertThat(stargazersCount, equalTo(10));
    }

    @Test
    public void listCollaborators() throws Exception {
        GHRepository repo = getRepository();
        List<GHUser> collaborators = repo.listCollaborators().toList();
        assertThat(collaborators.size(), greaterThan(10));
    }

    @Test
    public void listCollaboratorsFiltered() throws Exception {
        GHRepository repo = getRepository();
        List<GHUser> allCollaborators = repo.listCollaborators().toList();
        List<GHUser> filteredCollaborators = repo.listCollaborators(GHRepository.CollaboratorAffiliation.OUTSIDE)
                .toList();
        assertThat(filteredCollaborators.size(), lessThan(allCollaborators.size()));
    }

    @Test
    public void getCheckRuns() throws Exception {
        final int expectedCount = 8;
        // Use github-api repository as it has checks set up
        PagedIterable<GHCheckRun> checkRuns = gitHub.getOrganization("hub4j")
                .getRepository("github-api")
                .getCheckRuns("78b9ff49d47daaa158eb373c4e2e040f739df8b9");
        // Check if the paging works correctly
        assertThat(checkRuns.withPageSize(2).iterator().nextPage(), hasSize(2));

        // Check if the checkruns are all succeeded and if we got all of them
        int checkRunsCount = 0;
        for (GHCheckRun checkRun : checkRuns) {
            assertThat(checkRun.getConclusion(), equalTo(Conclusion.SUCCESS));
            checkRunsCount++;
        }
        assertThat(checkRunsCount, equalTo(expectedCount));

        // Check that we can call update on the results
        for (GHCheckRun checkRun : checkRuns) {
            checkRun.update();
        }
    }

    @Test
    public void getLastCommitStatus() throws Exception {
        GHCommitStatus status = getRepository().getLastCommitStatus("8051615eff597f4e49f4f47625e6fc2b49f26bfc");
        assertThat(status.getId(), equalTo(9027542286L));
        assertThat(status.getState(), equalTo(GHCommitState.SUCCESS));
        assertThat(status.getContext(), equalTo("ci/circleci: build"));
    }
}
