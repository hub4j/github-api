package org.kohsuke.github;

import com.fasterxml.jackson.databind.JsonMappingException;
import org.apache.commons.io.IOUtils;
import org.junit.Assert;
import org.junit.Test;
import org.kohsuke.github.GHCheckRun.Conclusion;
import org.kohsuke.github.GHOrganization.RepositoryRole;
import org.kohsuke.github.GHRepository.Visibility;

import java.io.ByteArrayInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static org.hamcrest.Matchers.*;
import static org.hamcrest.core.IsInstanceOf.instanceOf;
import static org.junit.Assert.assertThrows;
import static org.kohsuke.github.GHVerification.Reason.*;

// TODO: Auto-generated Javadoc
/**
 * The Class GHRepositoryTest.
 *
 * @author Liam Newman
 */
public class GHRepositoryTest extends AbstractGitHubWireMockTest {

    /**
     * Gets the repository.
     *
     * @return the repository
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     */
    protected GHRepository getRepository() throws IOException {
        return getRepository(gitHub);
    }

    private GHRepository getRepository(GitHub gitHub) throws IOException {
        return gitHub.getOrganization("hub4j-test-org").getRepository("github-api");
    }

    /**
     * Test zipball.
     *
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     */
    @Test
    public void testZipball() throws IOException {
        getTempRepository().readZip((InputStream inputstream) -> {
            return new ByteArrayInputStream(IOUtils.toByteArray(inputstream));
        }, null);
    }

    /**
     * Test tarball.
     *
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     */
    @Test
    public void testTarball() throws IOException {
        getTempRepository().readTar((InputStream inputstream) -> {
            return new ByteArrayInputStream(IOUtils.toByteArray(inputstream));
        }, null);
    }

    /**
     * Test getters.
     *
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     */
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

    /**
     * Archive.
     *
     * @throws Exception
     *             the exception
     */
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

    /**
     * Checks if is disabled.
     *
     * @throws Exception
     *             the exception
     */
    @Test
    public void isDisabled() throws Exception {
        GHRepository r = getRepository();

        assertThat(r.isDisabled(), is(false));
    }

    /**
     * Checks if is disabled true.
     *
     * @throws Exception
     *             the exception
     */
    @Test
    public void isDisabledTrue() throws Exception {
        GHRepository r = getRepository();

        assertThat(r.isDisabled(), is(true));
    }

    /**
     * Gets the branch URL encoded.
     *
     * @throws Exception
     *             the exception
     */
    @Test
    public void getBranch_URLEncoded() throws Exception {
        GHRepository repo = getRepository();
        GHBranch branch = repo.getBranch("test/#UrlEncode");
        assertThat(branch.getName(), is("test/#UrlEncode"));
    }

    /**
     * Creates the signed commit verify error.
     *
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     */
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

    /**
     * Creates the signed commit unknown signature type.
     *
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     */
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

    /**
     * List stargazers.
     *
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     */
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

    /**
     * Gets the branch non existent but 200 status.
     *
     * @throws Exception
     *             the exception
     */
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

    /**
     * Subscription.
     *
     * @throws Exception
     *             the exception
     */
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

    /**
     * Test set public.
     *
     * @throws Exception
     *             the exception
     */
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

    /**
     * Test update repository.
     *
     * @throws Exception
     *             the exception
     */
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

    /**
     * Test get repository with visibility.
     *
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     */
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

    /**
     * List contributors.
     *
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     */
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

    /**
     * Gets the permission.
     *
     * @throws Exception
     *             the exception
     */
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

    /**
     * Checks for permission.
     *
     * @throws Exception
     *             the exception
     */
    @Test
    public void hasPermission() throws Exception {
        kohsuke();
        GHRepository publicRepository = gitHub.getRepository("hub4j-test-org/test-permission");
        assertThat(publicRepository.hasPermission("kohsuke", GHPermissionType.ADMIN), equalTo(true));
        assertThat(publicRepository.hasPermission("kohsuke", GHPermissionType.WRITE), equalTo(true));
        assertThat(publicRepository.hasPermission("kohsuke", GHPermissionType.READ), equalTo(true));
        assertThat(publicRepository.hasPermission("kohsuke", GHPermissionType.NONE), equalTo(false));

        assertThat(publicRepository.hasPermission("dude", GHPermissionType.ADMIN), equalTo(false));
        assertThat(publicRepository.hasPermission("dude", GHPermissionType.WRITE), equalTo(false));
        assertThat(publicRepository.hasPermission("dude", GHPermissionType.READ), equalTo(true));
        assertThat(publicRepository.hasPermission("dude", GHPermissionType.NONE), equalTo(false));

        // also check the GHUser method
        GHUser kohsuke = gitHub.getUser("kohsuke");
        assertThat(publicRepository.hasPermission(kohsuke, GHPermissionType.ADMIN), equalTo(true));
        assertThat(publicRepository.hasPermission(kohsuke, GHPermissionType.WRITE), equalTo(true));
        assertThat(publicRepository.hasPermission(kohsuke, GHPermissionType.READ), equalTo(true));
        assertThat(publicRepository.hasPermission(kohsuke, GHPermissionType.NONE), equalTo(false));

        // check NONE on a private project
        GHRepository privateRepository = gitHub.getRepository("hub4j-test-org/test-permission-private");
        assertThat(privateRepository.hasPermission("dude", GHPermissionType.ADMIN), equalTo(false));
        assertThat(privateRepository.hasPermission("dude", GHPermissionType.WRITE), equalTo(false));
        assertThat(privateRepository.hasPermission("dude", GHPermissionType.READ), equalTo(false));
        assertThat(privateRepository.hasPermission("dude", GHPermissionType.NONE), equalTo(true));
    }

    /**
     * Latest repository exist.
     */
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

    /**
     * Adds the collaborators.
     *
     * @throws Exception
     *             the exception
     */
    @Test
    public void addCollaborators() throws Exception {
        GHRepository repo = getRepository();
        GHUser user = getUser();
        List<GHUser> users = new ArrayList<>();

        users.add(user);
        users.add(gitHub.getUser("jimmysombrero2"));
        repo.addCollaborators(users, GHOrganization.Permission.PUSH);

        GHPersonSet<GHUser> collabs = repo.getCollaborators();
        GHUser colabUser = collabs.byLogin("jimmysombrero");

        assertThat(user.getName(), equalTo(colabUser.getName()));
    }

    /**
     * Adds the collaborators repo perm.
     *
     * @throws Exception
     *             the exception
     */
    @Test
    public void addCollaboratorsRepoPerm() throws Exception {
        GHRepository repo = getRepository();
        GHUser user = getUser();

        RepositoryRole role = RepositoryRole.from(GHOrganization.Permission.PULL);
        repo.addCollaborators(role, user);

        GHPersonSet<GHUser> collabs = repo.getCollaborators();
        GHUser colabUser = collabs.byLogin("jgangemi");

        assertThat(user.getName(), equalTo(colabUser.getName()));
    }

    /**
     * Latest repository not exist.
     */
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

    /**
     * List releases.
     *
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     */
    @Test
    public void listReleases() throws IOException {
        PagedIterable<GHRelease> releases = gitHub.getOrganization("github").getRepository("hub").listReleases();
        assertThat(releases, is(not(emptyIterable())));
    }

    /**
     * Gets the release exists.
     *
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     */
    @Test
    public void getReleaseExists() throws IOException {
        GHRelease release = gitHub.getOrganization("github").getRepository("hub").getRelease(6839710);
        assertThat(release.getTagName(), equalTo("v2.3.0-pre10"));
    }

    /**
     * Gets the release does not exist.
     *
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     */
    @Test
    public void getReleaseDoesNotExist() throws IOException {
        GHRelease release = gitHub.getOrganization("github").getRepository("hub").getRelease(Long.MAX_VALUE);
        assertThat(release, nullValue());
    }

    /**
     * Gets the release by tag name exists.
     *
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     */
    @Test
    public void getReleaseByTagNameExists() throws IOException {
        GHRelease release = gitHub.getOrganization("github").getRepository("hub").getReleaseByTagName("v2.3.0-pre10");
        assertThat(release, notNullValue());
        assertThat(release.getTagName(), equalTo("v2.3.0-pre10"));
    }

    /**
     * Gets the release by tag name does not exist.
     *
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     */
    @Test
    public void getReleaseByTagNameDoesNotExist() throws IOException {
        GHRelease release = getRepository().getReleaseByTagName("foo-bar-baz");
        assertThat(release, nullValue());
    }

    /**
     * List languages.
     *
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     */
    @Test
    public void listLanguages() throws IOException {
        GHRepository r = gitHub.getRepository("hub4j/github-api");
        String mainLanguage = r.getLanguage();
        assertThat(mainLanguage, equalTo("Java"));
        Map<String, Long> languages = r.listLanguages();
        assertThat(languages.containsKey(mainLanguage), is(true));
        assertThat(languages.get("Java"), greaterThan(100000L));
    }

    /**
     * List commit comments no comments.
     *
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     */
    @Test
    public void listCommitCommentsNoComments() throws IOException {
        List<GHCommitComment> commitComments = getRepository()
                .listCommitComments("c413fc1e3057332b93850ea48202627d29a37de5")
                .toList();

        assertThat("Commit has no comments", commitComments.isEmpty());

        commitComments = getRepository().getCommit("c413fc1e3057332b93850ea48202627d29a37de5").listComments().toList();

        assertThat("Commit has no comments", commitComments.isEmpty());
    }

    /**
     * Search all public and forked repos.
     *
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     */
    @Test
    public void searchAllPublicAndForkedRepos() throws IOException {
        PagedSearchIterable<GHRepository> list = gitHub.searchRepositories()
                .user("t0m4uk1991")
                .visibility(GHRepository.Visibility.PUBLIC)
                .fork(GHFork.PARENT_AND_FORKS)
                .list();
        List<GHRepository> u = list.toList();
        assertThat(u.size(), is(14));
        assertThat(u.stream().filter(item -> item.getName().equals("github-api")).count(), is(1L));
        assertThat(u.stream().filter(item -> item.getName().equals("Complete-Python-3-Bootcamp")).count(), is(1L));
    }

    /**
     * Search for public forked only repos.
     *
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     */
    @Test
    public void searchForPublicForkedOnlyRepos() throws IOException {
        PagedSearchIterable<GHRepository> list = gitHub.searchRepositories()
                .user("t0m4uk1991")
                .visibility(GHRepository.Visibility.PUBLIC)
                .fork(GHFork.FORKS_ONLY)
                .list();
        List<GHRepository> u = list.toList();
        assertThat(u.size(), is(2));
        assertThat(u.get(0).getName(), is("github-api"));
        assertThat(u.get(1).getName(), is("Complete-Python-3-Bootcamp"));
    }

    /**
     * Gh repository search builder ignores unknown visibility.
     */
    @Test
    public void ghRepositorySearchBuilderIgnoresUnknownVisibility() {
        GHRepositorySearchBuilder ghRepositorySearchBuilder;

        GHException exception = assertThrows(GHException.class,
                () -> new GHRepositorySearchBuilder(gitHub).visibility(Visibility.UNKNOWN));
        assertThat(exception.getMessage(),
                startsWith("UNKNOWN is a placeholder for unexpected values encountered when reading data."));

        ghRepositorySearchBuilder = new GHRepositorySearchBuilder(gitHub).visibility(Visibility.PUBLIC);
        assertThat(ghRepositorySearchBuilder.terms.stream().filter(item -> item.contains("is:")).count(), is(1L));

        ghRepositorySearchBuilder = new GHRepositorySearchBuilder(gitHub).visibility(Visibility.PRIVATE);
        assertThat(ghRepositorySearchBuilder.terms.stream().filter(item -> item.contains("is:")).count(), is(1L));

        ghRepositorySearchBuilder = new GHRepositorySearchBuilder(gitHub).visibility(Visibility.INTERNAL);
        assertThat(ghRepositorySearchBuilder.terms.stream().filter(item -> item.contains("is:")).count(), is(1L));
    }

    /**
     * Gh repository search builder fork default reset forks search terms.
     */
    @Test
    public void ghRepositorySearchBuilderForkDefaultResetForksSearchTerms() {
        GHRepositorySearchBuilder ghRepositorySearchBuilder = new GHRepositorySearchBuilder(gitHub);

        ghRepositorySearchBuilder = ghRepositorySearchBuilder.fork(GHFork.PARENT_AND_FORKS);
        assertThat(ghRepositorySearchBuilder.terms.stream().filter(item -> item.contains("fork:true")).count(), is(1L));
        assertThat(ghRepositorySearchBuilder.terms.stream().filter(item -> item.contains("fork:")).count(), is(1L));

        ghRepositorySearchBuilder = ghRepositorySearchBuilder.fork(GHFork.FORKS_ONLY);
        assertThat(ghRepositorySearchBuilder.terms.stream().filter(item -> item.contains("fork:only")).count(), is(1L));
        assertThat(ghRepositorySearchBuilder.terms.stream().filter(item -> item.contains("fork:")).count(), is(2L));

        ghRepositorySearchBuilder = ghRepositorySearchBuilder.fork(GHFork.PARENT_ONLY);
        assertThat(ghRepositorySearchBuilder.terms.stream().filter(item -> item.contains("fork:")).count(), is(0L));
    }

    /**
     * Gh repository search builder fork deprecated enum.
     */
    @Test
    public void ghRepositorySearchBuilderForkDeprecatedEnum() {
        GHRepositorySearchBuilder ghRepositorySearchBuilder = new GHRepositorySearchBuilder(gitHub);
        ghRepositorySearchBuilder = ghRepositorySearchBuilder.fork(GHRepositorySearchBuilder.Fork.PARENT_AND_FORKS);
        assertThat(ghRepositorySearchBuilder.terms.stream().filter(item -> item.contains("fork:true")).count(), is(1L));
        assertThat(ghRepositorySearchBuilder.terms.stream().filter(item -> item.contains("fork:")).count(), is(1L));

        ghRepositorySearchBuilder = ghRepositorySearchBuilder.fork(GHRepositorySearchBuilder.Fork.FORKS_ONLY);
        assertThat(ghRepositorySearchBuilder.terms.stream().filter(item -> item.contains("fork:only")).count(), is(1L));
        assertThat(ghRepositorySearchBuilder.terms.stream().filter(item -> item.contains("fork:")).count(), is(2L));

        ghRepositorySearchBuilder = ghRepositorySearchBuilder.fork(GHRepositorySearchBuilder.Fork.PARENT_ONLY);
        assertThat(ghRepositorySearchBuilder.terms.stream().filter(item -> item.contains("fork:")).count(), is(0L));
    }

    /**
     * Gh repository search builder fork deprecated string.
     */
    @Test
    public void ghRepositorySearchBuilderForkDeprecatedString() {
        GHRepositorySearchBuilder ghRepositorySearchBuilder = new GHRepositorySearchBuilder(gitHub);
        ghRepositorySearchBuilder = ghRepositorySearchBuilder.forks(GHFork.PARENT_AND_FORKS.toString());
        assertThat(ghRepositorySearchBuilder.terms.stream().filter(item -> item.contains("fork:true")).count(), is(1L));
        assertThat(ghRepositorySearchBuilder.terms.stream().filter(item -> item.contains("fork:")).count(), is(1L));

        ghRepositorySearchBuilder = ghRepositorySearchBuilder.forks(GHFork.FORKS_ONLY.toString());
        assertThat(ghRepositorySearchBuilder.terms.stream().filter(item -> item.contains("fork:only")).count(), is(1L));
        assertThat(ghRepositorySearchBuilder.terms.stream().filter(item -> item.contains("fork:")).count(), is(2L));

        ghRepositorySearchBuilder = ghRepositorySearchBuilder.forks(null);
        assertThat(ghRepositorySearchBuilder.terms.stream().filter(item -> item.contains("fork:")).count(), is(0L));
    }

    /**
     * List commit comments some comments.
     *
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     */
    @Test
    public void listCommitCommentsSomeComments() throws IOException {
        List<GHCommitComment> commitComments = getRepository()
                .listCommitComments("499d91f9f846b0087b2a20cf3648b49dc9c2eeef")
                .toList();

        assertThat("Two comments present", commitComments.size(), equalTo(2));
        assertThat("Comment text found",
                commitComments.stream().map(GHCommitComment::getBody).collect(Collectors.toList()),
                containsInAnyOrder("comment 1", "comment 2"));

        commitComments = getRepository().getCommit("499d91f9f846b0087b2a20cf3648b49dc9c2eeef").listComments().toList();

        assertThat("Two comments present", commitComments.size(), equalTo(2));
        assertThat("Comment text found",
                commitComments.stream().map(GHCommitComment::getBody).collect(Collectors.toList()),
                containsInAnyOrder("comment 1", "comment 2"));
    }

    /**
     * List empty contributors.
     *
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     */
    @Test // Issue #261
    public void listEmptyContributors() throws IOException {
        assertThat("This list should be empty, but should return a valid empty iterable.",
                gitHub.getRepository(GITHUB_API_TEST_ORG + "/empty").listContributors(),
                is(emptyIterable()));
    }

    /**
     * Search repositories.
     *
     * @throws Exception
     *             the exception
     */
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

    /**
     * Search org for repositories.
     *
     * @throws Exception
     *             the exception
     */
    @Test
    public void searchOrgForRepositories() throws Exception {
        PagedSearchIterable<GHRepository> r = gitHub.searchRepositories().org("hub4j-test-org").list();
        GHRepository u = r.iterator().next();
        assertThat(u.getOwnerName(), equalTo("hub4j-test-org"));
        assertThat(r.getTotalCount(), greaterThan(0));
    }

    /**
     * Test issue 162.
     *
     * @throws Exception
     *             the exception
     */
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

    /**
     * Mark down.
     *
     * @throws Exception
     *             the exception
     */
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

    /**
     * Sets the merge options.
     *
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     */
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

    /**
     * Gets the delete branch on merge.
     *
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     */
    @Test
    public void getDeleteBranchOnMerge() throws IOException {
        GHRepository r = getRepository();
        assertThat(r.isDeleteBranchOnMerge(), notNullValue());
    }

    /**
     * Sets the delete branch on merge.
     *
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     */
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

    /**
     * Test set topics.
     *
     * @throws Exception
     *             the exception
     */
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

    /**
     * Gets the collaborators.
     *
     * @throws Exception
     *             the exception
     */
    @Test
    public void getCollaborators() throws Exception {
        GHRepository repo = getRepository(gitHub);
        GHPersonSet<GHUser> collaborators = repo.getCollaborators();
        assertThat(collaborators.size(), greaterThan(0));
    }

    /**
     * Gets the post commit hooks.
     *
     * @throws Exception
     *             the exception
     */
    @Test
    public void getPostCommitHooks() throws Exception {
        GHRepository repo = getRepository(gitHub);
        Set<URL> postcommitHooks = repo.getPostCommitHooks();
        assertThat(postcommitHooks, is(empty()));
    }

    /**
     * Gets the refs.
     *
     * @throws Exception
     *             the exception
     */
    @Test
    public void getRefs() throws Exception {
        GHRepository repo = getTempRepository();
        GHRef[] refs = repo.getRefs();
        assertThat(refs, notNullValue());
        assertThat(refs.length, equalTo(1));
        assertThat(refs[0].getRef(), equalTo("refs/heads/main"));
    }

    /**
     * Gets the public key.
     *
     * @throws Exception
     *             the exception
     */
    @Test
    public void getPublicKey() throws Exception {
        GHRepository repo = getTempRepository();
        GHRepositoryPublicKey publicKey = repo.getPublicKey();
        assertThat(publicKey, notNullValue());
        assertThat(publicKey.getKey(), equalTo("test-key"));
        assertThat(publicKey.getKeyId(), equalTo("key-id"));
    }

    /**
     * Gets the refs heads.
     *
     * @throws Exception
     *             the exception
     */
    @Test
    public void getRefsHeads() throws Exception {
        GHRepository repo = getTempRepository();
        GHRef[] refs = repo.getRefs("heads");
        assertThat(refs, notNullValue());
        assertThat(refs.length, equalTo(1));
        assertThat(refs[0].getRef(), equalTo("refs/heads/main"));
    }

    /**
     * Gets the refs empty tags.
     *
     * @throws Exception
     *             the exception
     */
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

    /**
     * List refs.
     *
     * @throws Exception
     *             the exception
     */
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

    /**
     * Gets the ref.
     *
     * @throws Exception
     *             the exception
     */
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
        assertThat(ghRefWithPrefix.getObject().getType(), equalTo("commit"));
        assertThat(ghRefWithPrefix.getObject().getUrl().toString(),
                containsString("/repos/hub4j-test-org/github-api/git/commits/"));

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

    /**
     * List refs heads.
     *
     * @throws Exception
     *             the exception
     */
    @Test
    public void listRefsHeads() throws Exception {
        GHRepository repo = getTempRepository();
        List<GHRef> refs = repo.listRefs("heads").toList();
        assertThat(refs, notNullValue());
        assertThat(refs.size(), equalTo(1));
        assertThat(refs.get(0).getRef(), equalTo("refs/heads/main"));
    }

    /**
     * List refs empty tags.
     *
     * @throws Exception
     *             the exception
     */
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

    /**
     * List tags empty.
     *
     * @throws Exception
     *             the exception
     */
    @Test
    public void listTagsEmpty() throws Exception {
        GHRepository repo = getTempRepository();
        List<GHTag> refs = repo.listTags().toList();
        assertThat(refs, notNullValue());
        assertThat(refs, is(empty()));
    }

    /**
     * List tags.
     *
     * @throws Exception
     *             the exception
     */
    @Test
    public void listTags() throws Exception {
        GHRepository repo = getRepository();
        List<GHTag> refs = repo.listTags().withPageSize(33).toList();
        assertThat(refs, notNullValue());
        assertThat(refs.size(), greaterThan(90));
    }

    /**
     * Check watchers count.
     *
     * @throws Exception
     *             the exception
     */
    @Test
    public void checkWatchersCount() throws Exception {
        snapshotNotAllowed();
        GHRepository repo = getTempRepository();
        int watchersCount = repo.getWatchersCount();
        assertThat(watchersCount, equalTo(10));
    }

    /**
     * Check stargazers count.
     *
     * @throws Exception
     *             the exception
     */
    @Test
    public void checkStargazersCount() throws Exception {
        snapshotNotAllowed();
        GHRepository repo = getTempRepository();
        int stargazersCount = repo.getStargazersCount();
        assertThat(stargazersCount, equalTo(10));
    }

    /**
     * List collaborators.
     *
     * @throws Exception
     *             the exception
     */
    @Test
    public void listCollaborators() throws Exception {
        GHRepository repo = getRepository();
        List<GHUser> collaborators = repo.listCollaborators().toList();
        assertThat(collaborators.size(), greaterThan(10));
    }

    /**
     * List collaborators filtered.
     *
     * @throws Exception
     *             the exception
     */
    @Test
    public void listCollaboratorsFiltered() throws Exception {
        GHRepository repo = getRepository();
        List<GHUser> allCollaborators = repo.listCollaborators().toList();
        List<GHUser> filteredCollaborators = repo.listCollaborators(GHRepository.CollaboratorAffiliation.OUTSIDE)
                .toList();
        assertThat(filteredCollaborators.size(), lessThan(allCollaborators.size()));
    }

    /**
     * User is collaborator.
     *
     * @throws Exception
     *             the exception
     */
    @Test
    public void userIsCollaborator() throws Exception {
        GHRepository repo = getRepository();
        GHUser collaborator = repo.listCollaborators().toList().get(0);
        assertThat(repo.isCollaborator(collaborator), is(true));
    }

    /**
     * Gets the check runs.
     *
     * @throws Exception
     *             the exception
     */
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

    /**
     * Filter out the checks from a reference
     *
     * @throws Exception
     *             the exception
     */
    @Test
    public void getCheckRunsWithParams() throws Exception {
        final int expectedCount = 1;
        // Use github-api repository as it has checks set up
        final Map<String, Object> params = new HashMap<>(1);
        params.put("check_name", "build-only (Java 17)");
        PagedIterable<GHCheckRun> checkRuns = gitHub.getOrganization("hub4j")
                .getRepository("github-api")
                .getCheckRuns("54d60fbb53b4efa19f3081417bfb6a1de30c55e4", params);

        // Check if the checkruns are all succeeded and if we got all of them
        int checkRunsCount = 0;
        for (GHCheckRun checkRun : checkRuns) {
            assertThat(checkRun.getConclusion(), equalTo(Conclusion.SUCCESS));
            checkRunsCount++;
        }
        assertThat(checkRunsCount, equalTo(expectedCount));
    }

    /**
     * Gets the last commit status.
     *
     * @throws Exception
     *             the exception
     */
    @Test
    public void getLastCommitStatus() throws Exception {
        GHCommitStatus status = getRepository().getLastCommitStatus("8051615eff597f4e49f4f47625e6fc2b49f26bfc");
        assertThat(status.getId(), equalTo(9027542286L));
        assertThat(status.getState(), equalTo(GHCommitState.SUCCESS));
        assertThat(status.getContext(), equalTo("ci/circleci: build"));
    }

    /**
     * List commits between.
     *
     * @throws Exception
     *             the exception
     */
    @Test
    public void listCommitsBetween() throws Exception {
        GHRepository repository = getRepository();
        int startingCount = mockGitHub.getRequestCount();
        GHCompare compare = repository.getCompare("e46a9f3f2ac55db96de3c5c4706f2813b3a96465",
                "8051615eff597f4e49f4f47625e6fc2b49f26bfc");
        int actualCount = 0;
        for (GHCompare.Commit item : compare.listCommits().withPageSize(5)) {
            assertThat(item, notNullValue());
            actualCount++;
        }
        assertThat(compare.getTotalCommits(), is(9));
        assertThat(actualCount, is(9));
        assertThat(mockGitHub.getRequestCount(), equalTo(startingCount + 1));
    }

    /**
     * List commits between paginated.
     *
     * @throws Exception
     *             the exception
     */
    @Test
    public void listCommitsBetweenPaginated() throws Exception {
        GHRepository repository = getRepository();
        int startingCount = mockGitHub.getRequestCount();
        repository.setCompareUsePaginatedCommits(true);
        GHCompare compare = repository.getCompare("e46a9f3f2ac55db96de3c5c4706f2813b3a96465",
                "8051615eff597f4e49f4f47625e6fc2b49f26bfc");
        int actualCount = 0;
        for (GHCompare.Commit item : compare.listCommits().withPageSize(5)) {
            assertThat(item, notNullValue());
            actualCount++;
        }
        assertThat(compare.getTotalCommits(), is(9));
        assertThat(actualCount, is(9));
        assertThat(mockGitHub.getRequestCount(), equalTo(startingCount + 3));
    }

    /**
     * Gets the commits between over 250.
     *
     * @throws Exception
     *             the exception
     */
    @Test
    public void getCommitsBetweenOver250() throws Exception {
        GHRepository repository = getRepository();
        int startingCount = mockGitHub.getRequestCount();
        GHCompare compare = repository.getCompare("4261c42949915816a9f246eb14c3dfd21a637bc2",
                "94ff089e60064bfa43e374baeb10846f7ce82f40");
        int actualCount = 0;
        for (GHCompare.Commit item : compare.getCommits()) {
            assertThat(item, notNullValue());
            actualCount++;
        }
        assertThat(compare.getTotalCommits(), is(283));
        assertThat(actualCount, is(250));
        assertThat(mockGitHub.getRequestCount(), equalTo(startingCount + 1));

        // Additional GHCompare checks
        assertThat(compare.getAheadBy(), equalTo(283));
        assertThat(compare.getBehindBy(), equalTo(0));
        assertThat(compare.getStatus(), equalTo(GHCompare.Status.ahead));
        assertThat(compare.getDiffUrl().toString(),
                endsWith(
                        "compare/4261c42949915816a9f246eb14c3dfd21a637bc2...94ff089e60064bfa43e374baeb10846f7ce82f40.diff"));
        assertThat(compare.getHtmlUrl().toString(),
                endsWith(
                        "compare/4261c42949915816a9f246eb14c3dfd21a637bc2...94ff089e60064bfa43e374baeb10846f7ce82f40"));
        assertThat(compare.getPatchUrl().toString(),
                endsWith(
                        "compare/4261c42949915816a9f246eb14c3dfd21a637bc2...94ff089e60064bfa43e374baeb10846f7ce82f40.patch"));
        assertThat(compare.getPermalinkUrl().toString(),
                endsWith("compare/hub4j-test-org:4261c42...hub4j-test-org:94ff089"));
        assertThat(compare.getUrl().toString(),
                endsWith(
                        "compare/4261c42949915816a9f246eb14c3dfd21a637bc2...94ff089e60064bfa43e374baeb10846f7ce82f40"));

        assertThat(compare.getBaseCommit().getSHA1(), equalTo("4261c42949915816a9f246eb14c3dfd21a637bc2"));

        assertThat(compare.getMergeBaseCommit().getSHA1(), equalTo("4261c42949915816a9f246eb14c3dfd21a637bc2"));
        // it appears this field is not present in the returned JSON. Strange.
        assertThat(compare.getMergeBaseCommit().getCommit().getSha(), nullValue());
        assertThat(compare.getMergeBaseCommit().getCommit().getUrl(),
                endsWith("/commits/4261c42949915816a9f246eb14c3dfd21a637bc2"));
        assertThat(compare.getMergeBaseCommit().getCommit().getMessage(),
                endsWith("[maven-release-plugin] prepare release github-api-1.123"));
        assertThat(compare.getMergeBaseCommit().getCommit().getAuthor().getName(), equalTo("Liam Newman"));
        assertThat(compare.getMergeBaseCommit().getCommit().getCommitter().getName(), equalTo("Liam Newman"));

        assertThat(compare.getMergeBaseCommit().getCommit().getTree().getSha(),
                equalTo("5da98090976978c93aba0bdfa550e05675543f99"));
        assertThat(compare.getMergeBaseCommit().getCommit().getTree().getUrl(),
                endsWith("/git/trees/5da98090976978c93aba0bdfa550e05675543f99"));

        assertThat(compare.getFiles().length, equalTo(300));
        assertThat(compare.getFiles()[0].getFileName(), equalTo(".github/PULL_REQUEST_TEMPLATE.md"));
        assertThat(compare.getFiles()[0].getLinesAdded(), equalTo(8));
        assertThat(compare.getFiles()[0].getLinesChanged(), equalTo(15));
        assertThat(compare.getFiles()[0].getLinesDeleted(), equalTo(7));
        assertThat(compare.getFiles()[0].getFileName(), equalTo(".github/PULL_REQUEST_TEMPLATE.md"));
        assertThat(compare.getFiles()[0].getPatch(), startsWith("@@ -1,15 +1,16 @@"));
        assertThat(compare.getFiles()[0].getPreviousFilename(), nullValue());
        assertThat(compare.getFiles()[0].getStatus(), equalTo("modified"));
        assertThat(compare.getFiles()[0].getSha(), equalTo("e4234f5f6f39899282a6ef1edff343ae1269222e"));

        assertThat(compare.getFiles()[0].getBlobUrl().toString(),
                endsWith("/blob/94ff089e60064bfa43e374baeb10846f7ce82f40/.github/PULL_REQUEST_TEMPLATE.md"));
        assertThat(compare.getFiles()[0].getRawUrl().toString(),
                endsWith("/raw/94ff089e60064bfa43e374baeb10846f7ce82f40/.github/PULL_REQUEST_TEMPLATE.md"));
    }

    /**
     * Gets the commits between paged.
     *
     * @throws Exception
     *             the exception
     */
    @Test
    public void getCommitsBetweenPaged() throws Exception {
        GHRepository repository = getRepository();
        int startingCount = mockGitHub.getRequestCount();
        repository.setCompareUsePaginatedCommits(true);
        GHCompare compare = repository.getCompare("4261c42949915816a9f246eb14c3dfd21a637bc2",
                "94ff089e60064bfa43e374baeb10846f7ce82f40");
        int actualCount = 0;
        for (GHCompare.Commit item : compare.getCommits()) {
            assertThat(item, notNullValue());
            actualCount++;
        }
        assertThat(compare.getTotalCommits(), is(283));
        assertThat(actualCount, is(283));
        assertThat(mockGitHub.getRequestCount(), equalTo(startingCount + 4));
    }

    /**
     * Creates the dispatch event without client payload.
     *
     * @throws Exception
     *             the exception
     */
    @Test
    public void createDispatchEventWithoutClientPayload() throws Exception {
        GHRepository repository = getTempRepository();
        repository.dispatch("test", null);
    }

    /**
     * Creates the dispatch event with client payload.
     *
     * @throws Exception
     *             the exception
     */
    @Test
    public void createDispatchEventWithClientPayload() throws Exception {
        GHRepository repository = getTempRepository();
        Map<String, Object> clientPayload = new HashMap<>();
        clientPayload.put("name", "joe.doe");
        clientPayload.put("list", new ArrayList<>());
        repository.dispatch("test", clientPayload);
    }

    /**
     * Creates the secret.
     *
     * @throws Exception
     *             the exception
     */
    @Test
    public void createSecret() throws Exception {
        GHRepository repo = getTempRepository();
        repo.createSecret("secret", "encrypted", "public");
    }

    /**
     * Creates the template repository
     *
     * @throws Exception
     *             the exception
     */
    @Test
    public void templateRepository() throws Exception {
        GHRepository repo = getRepository();

        assertThat(repo.getTemplateRepository().getName(), is("github-api-template"));
    }

    /**
     * Test to check star method by verifying stargarzer count.
     *
     * @throws Exception
     *             the exception
     */
    @Test
    public void starTest() throws Exception {
        String owner = "hub4j-test-org";
        GHRepository repository = getRepository();
        assertThat(repository.getOwner().getLogin(), equalTo(owner));
        assertThat(repository.getStargazersCount(), is(0));
        repository.star();
        assertThat(repository.listStargazers2().toList().size(), is(1));
        repository.unstar();
        assertThat(repository.listStargazers().toList().size(), is(0));
    }

    /**
     * Test to check getRepoVariable method.
     *
     * @throws Exception
     *             the exception
     */
    @Test
    public void testRepoActionVariable() throws Exception {
        GHRepository repository = getRepository();
        GHRepositoryVariable variable = repository.getRepoVariable("myvar");
        assertThat(variable.getValue(), is("this is my var value"));
    }

    /**
     * Test create repo action variable.
     *
     * @throws IOException
     *             the exception
     */
    @Test
    public void testCreateRepoActionVariable() throws IOException {
        GHRepository repository = getRepository();
        repository.createVariable("MYNEWVARIABLE", "mynewvalue");
        GHRepositoryVariable variable = repository.getVariable("mynewvariable");
        assertThat(variable.getName(), is("MYNEWVARIABLE"));
        assertThat(variable.getValue(), is("mynewvalue"));
    }

    /**
     * Test update repo action variable.
     *
     * @throws IOException
     *             the exception
     */
    @Test
    public void testUpdateRepoActionVariable() throws IOException {
        GHRepository repository = getRepository();
        GHRepositoryVariable variable = repository.getVariable("MYNEWVARIABLE");
        variable.set().value("myupdatevalue");
        variable = repository.getVariable("MYNEWVARIABLE");
        assertThat(variable.getValue(), is("myupdatevalue"));
    }

    /**
     * Test delete repo action variable.
     *
     * @throws IOException
     *             the exception
     */
    @Test
    public void testDeleteRepoActionVariable() throws IOException {
        GHRepository repository = getRepository();
        GHRepositoryVariable variable = repository.getVariable("mynewvariable");
        variable.delete();
        Assert.assertThrows(GHFileNotFoundException.class, () -> repository.getVariable("mynewvariable"));
    }

    /**
     * Test demoing the issue with a user having the maintain permission on a repository.
     *
     * Test checking the permission fallback mechanism in case the Github API changes. The test was recorded at a time a
     * new permission was added by mistake. If a re-recording it is needed, you'll like have to manually edit the
     * generated mocks to get a non existing permission See
     * https://github.com/hub4j/github-api/issues/1671#issuecomment-1577515662 for the details.
     *
     * @throws IOException
     *             the exception
     */
    @Test
    public void cannotRetrievePermissionMaintainUser() throws IOException {
        GHRepository r = gitHub.getRepository("hub4j-test-org/maintain-permission-issue");
        GHPermissionType permission = r.getPermission("alecharp");
        assertThat(permission.toString(), is("UNKNOWN"));
    }
}
