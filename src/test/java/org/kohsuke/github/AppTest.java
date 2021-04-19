package org.kohsuke.github;

import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.SystemUtils;
import org.junit.Assume;
import org.junit.Ignore;
import org.junit.Test;
import org.kohsuke.github.GHCommit.File;
import org.kohsuke.github.GHOrganization.Permission;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.Map.Entry;
import java.util.regex.Pattern;

import static org.hamcrest.Matchers.*;

/**
 * Unit test for simple App.
 */
public class AppTest extends AbstractGitHubWireMockTest {
    static final String GITHUB_API_TEST_REPO = "github-api-test";

    @Test
    public void testRepoCRUD() throws Exception {
        String targetName = "github-api-test-rename2";

        cleanupUserRepository("github-api-test-rename");
        cleanupUserRepository(targetName);

        GHRepository r = gitHub.createRepository("github-api-test-rename")
                .description("a test repository")
                .homepage("http://github-api.kohsuke.org/")
                .private_(false)
                .create();

        assertThat(r.hasIssues(), is(true));
        assertThat(r.hasWiki(), is(true));
        assertThat(r.hasDownloads(), is(true));
        assertThat(r.hasProjects(), is(true));

        r.enableIssueTracker(false);
        r.enableDownloads(false);
        r.enableWiki(false);
        r.enableProjects(false);

        r.renameTo(targetName);

        // local instance remains unchanged
        assertThat(r.getName(), equalTo("github-api-test-rename"));
        assertThat(r.hasIssues(), is(true));
        assertThat(r.hasWiki(), is(true));
        assertThat(r.hasDownloads(), is(true));
        assertThat(r.hasProjects(), is(true));

        r = gitHub.getMyself().getRepository(targetName);

        // values are updated
        assertThat(r.hasIssues(), is(false));
        assertThat(r.hasWiki(), is(false));
        assertThat(r.hasDownloads(), is(false));
        assertThat(r.getName(), equalTo(targetName));

        assertThat(r.hasProjects(), is(false));

        r.delete();
    }

    @Test
    public void testRepositoryWithAutoInitializationCRUD() throws Exception {
        String name = "github-api-test-autoinit";
        cleanupUserRepository(name);
        GHRepository r = gitHub.createRepository(name)
                .description("a test repository for auto init")
                .homepage("http://github-api.kohsuke.org/")
                .autoInit(true)
                .create();
        if (mockGitHub.isUseProxy()) {
            Thread.sleep(3000);
        }
        assertNotNull(r.getReadme());

        r.delete();
    }

    private void cleanupUserRepository(final String name) throws IOException {
        if (mockGitHub.isUseProxy()) {
            cleanupRepository(getUser(getNonRecordingGitHub()).getLogin() + "/" + name);
        }
    }

    @Test
    public void testCredentialValid() throws IOException {
        assertTrue(gitHub.isCredentialValid());
        assertThat(gitHub.lastRateLimit().getCore(), not(instanceOf(GHRateLimit.UnknownLimitRecord.class)));
        assertThat(gitHub.lastRateLimit().getCore().getLimit(), equalTo(5000));

        gitHub = getGitHubBuilder().withOAuthToken("bogus", "user")
                .withEndpoint(mockGitHub.apiServer().baseUrl())
                .build();
        assertThat(gitHub.lastRateLimit(), sameInstance(GHRateLimit.DEFAULT));
        assertFalse(gitHub.isCredentialValid());
        // For invalid credentials, we get a 401 but it includes anonymous rate limit headers
        assertThat(gitHub.lastRateLimit().getCore(), not(instanceOf(GHRateLimit.UnknownLimitRecord.class)));
        assertThat(gitHub.lastRateLimit().getCore().getLimit(), equalTo(60));
    }

    @Test
    public void testCredentialValidEnterprise() throws IOException {
        // Simulated GHE: getRateLimit returns 404
        assertThat(gitHub.lastRateLimit(), sameInstance(GHRateLimit.DEFAULT));
        assertThat(gitHub.lastRateLimit().getCore().isExpired(), is(true));
        assertTrue(gitHub.isCredentialValid());

        // lastRateLimitUpdates because 404 still includes header rate limit info
        assertThat(gitHub.lastRateLimit(), notNullValue());
        assertThat(gitHub.lastRateLimit(), not(equalTo(GHRateLimit.DEFAULT)));
        assertThat(gitHub.lastRateLimit().getCore().isExpired(), is(false));

        gitHub = getGitHubBuilder().withOAuthToken("bogus", "user")
                .withEndpoint(mockGitHub.apiServer().baseUrl())
                .build();
        assertThat(gitHub.lastRateLimit(), sameInstance(GHRateLimit.DEFAULT));
        assertFalse(gitHub.isCredentialValid());
        // Simulated GHE: For invalid credentials, we get a 401 that does not include ratelimit info
        assertThat(gitHub.lastRateLimit(), sameInstance(GHRateLimit.DEFAULT));
    }

    @Test
    public void testIssueWithNoComment() throws IOException {
        GHRepository repository = gitHub.getRepository("kohsuke/test");
        GHIssue i = repository.getIssue(4);
        List<GHIssueComment> v = i.getComments();
        // System.out.println(v);
        assertTrue(v.isEmpty());

        i = repository.getIssue(3);
        v = i.getComments();
        // System.out.println(v);
        assertThat(v.size(), equalTo(3));
        assertThat(v.get(0).getHtmlUrl().toString(),
                equalTo("https://github.com/kohsuke/test/issues/3#issuecomment-8547249"));
        assertThat(v.get(0).getUrl().toString(), endsWith("/repos/kohsuke/test/issues/comments/8547249"));
        assertThat(v.get(0).getNodeId(), equalTo("MDEyOklzc3VlQ29tbWVudDg1NDcyNDk="));
        assertThat(v.get(0).getParent().getNumber(), equalTo(3));
        assertThat(v.get(0).getParent().getId(), equalTo(6863845L));
        assertThat(v.get(0).getUser().getLogin(), equalTo("kohsuke"));
        assertThat(v.get(0).listReactions().toList().size(), equalTo(0));

        assertThat(v.get(1).getHtmlUrl().toString(),
                equalTo("https://github.com/kohsuke/test/issues/3#issuecomment-8547251"));
        assertThat(v.get(1).getUrl().toString(), endsWith("/repos/kohsuke/test/issues/comments/8547251"));
        assertThat(v.get(1).getNodeId(), equalTo("MDEyOklzc3VlQ29tbWVudDg1NDcyNTE="));
        assertThat(v.get(1).getParent().getNumber(), equalTo(3));
        assertThat(v.get(1).getUser().getLogin(), equalTo("kohsuke"));
        List<GHReaction> reactions = v.get(1).listReactions().toList();
        assertThat(reactions.size(), equalTo(3));

        // TODO: Add comment CRUD test
        // TODO: Add reactions CRUD test

    }

    @Test
    public void testCreateIssue() throws IOException {
        GHUser u = getUser();
        GHRepository repository = getTestRepository();
        GHMilestone milestone = repository.createMilestone("Test Milestone Title3", "Test Milestone");
        GHIssue o = repository.createIssue("testing")
                .body("this is body")
                .assignee(u)
                .label("bug")
                .label("question")
                .milestone(milestone)
                .create();
        assertNotNull(o);
        o.close();
    }

    @Test
    public void testCreateAndListDeployments() throws IOException {
        GHRepository repository = getTestRepository();
        GHDeployment deployment = repository.createDeployment("main")
                .payload("{\"user\":\"atmos\",\"room_id\":123456}")
                .description("question")
                .environment("unittest")
                .create();
        try {
            assertNotNull(deployment.getCreator());
            assertNotNull(deployment.getId());
            List<GHDeployment> deployments = repository.listDeployments(null, "main", null, "unittest").toList();
            assertNotNull(deployments);
            assertFalse(Iterables.isEmpty(deployments));
            GHDeployment unitTestDeployment = deployments.get(0);
            assertEquals("unittest", unitTestDeployment.getEnvironment());
            assertEquals("unittest", unitTestDeployment.getOriginalEnvironment());
            assertEquals(false, unitTestDeployment.isProductionEnvironment());
            assertEquals(false, unitTestDeployment.isTransientEnvironment());
            assertEquals("main", unitTestDeployment.getRef());
        } finally {
            // deployment.delete();
            assert true;
        }
    }

    @Test
    public void testGetDeploymentStatuses() throws IOException {
        GHRepository repository = getTestRepository();
        GHDeployment deployment = repository.createDeployment("main")
                .description("question")
                .payload("{\"user\":\"atmos\",\"room_id\":123456}")
                .create();
        try {
            GHDeploymentStatus ghDeploymentStatus = deployment.createStatus(GHDeploymentState.QUEUED)
                    .description("success")
                    .targetUrl("http://www.github.com")
                    .logUrl("http://www.github.com/logurl")
                    .environmentUrl("http://www.github.com/envurl")
                    .environment("new-ci-env")
                    .create();
            Iterable<GHDeploymentStatus> deploymentStatuses = deployment.listStatuses();
            assertNotNull(deploymentStatuses);
            assertEquals(1, Iterables.size(deploymentStatuses));
            GHDeploymentStatus actualStatus = Iterables.get(deploymentStatuses, 0);
            assertEquals(ghDeploymentStatus.getId(), actualStatus.getId());
            assertEquals(ghDeploymentStatus.getState(), actualStatus.getState());
            assertEquals(ghDeploymentStatus.getLogUrl(), actualStatus.getLogUrl());
            // Target url was deprecated and replaced with log url. The gh api will
            // prefer the log url value and return it in place of target url.
            assertEquals(ghDeploymentStatus.getTargetUrl(), actualStatus.getLogUrl());
            assertThat(ghDeploymentStatus.getDeploymentUrl(), equalTo(deployment.getUrl()));
            assertThat(ghDeploymentStatus.getRepositoryUrl(), equalTo(repository.getUrl()));
        } finally {
            // deployment.delete();
            assert true;
        }
    }

    @Test
    public void testGetIssues() throws Exception {
        List<GHIssue> closedIssues = gitHub.getOrganization("hub4j")
                .getRepository("github-api")
                .getIssues(GHIssueState.CLOSED);
        // prior to using PagedIterable GHRepository.getIssues(GHIssueState) would only retrieve 30 issues
        assertTrue(closedIssues.size() > 150);
        String readRepoString = GitHub.getMappingObjectWriter().writeValueAsString(closedIssues.get(0));
    }

    private GHRepository getTestRepository() throws IOException {
        return getTempRepository(GITHUB_API_TEST_REPO);
    }

    @Test
    public void testListIssues() throws IOException {
        Iterable<GHIssue> closedIssues = gitHub.getOrganization("hub4j")
                .getRepository("github-api")
                .listIssues(GHIssueState.CLOSED);

        int x = 0;
        for (GHIssue issue : closedIssues) {
            assertNotNull(issue);
            x++;
        }

        assertTrue(x > 150);
    }

    @Test
    public void testRateLimit() throws IOException {
        assertThat(gitHub.getRateLimit(), notNullValue());
    }

    @Test
    public void testMyOrganizations() throws IOException {
        Map<String, GHOrganization> org = gitHub.getMyOrganizations();
        assertFalse(org.keySet().contains(null));
        // System.out.println(org);
    }

    @Test
    public void testMyOrganizationsContainMyTeams() throws IOException {
        Map<String, Set<GHTeam>> teams = gitHub.getMyTeams();
        Map<String, GHOrganization> myOrganizations = gitHub.getMyOrganizations();
        // GitHub no longer has default 'owners' team, so there may be organization memberships without a team
        // https://help.github.com/articles/about-improved-organization-permissions/
        assertTrue(myOrganizations.keySet().containsAll(teams.keySet()));
    }

    @Test
    public void testMyTeamsShouldIncludeMyself() throws IOException {
        Map<String, Set<GHTeam>> teams = gitHub.getMyTeams();
        for (Entry<String, Set<GHTeam>> teamsPerOrg : teams.entrySet()) {
            String organizationName = teamsPerOrg.getKey();
            for (GHTeam team : teamsPerOrg.getValue()) {
                String teamName = team.getName();
                assertTrue("Team " + teamName + " in organization " + organizationName + " does not contain myself",
                        shouldBelongToTeam(organizationName, teamName));
            }
        }
    }

    @Test
    public void testUserPublicOrganizationsWhenThereAreSome() throws IOException {
        // kohsuke had some public org memberships at the time Wiremock recorded the GitHub API responses
        GHUser user = new GHUser();
        user.login = "kohsuke";

        Map<String, GHOrganization> orgs = gitHub.getUserPublicOrganizations(user);
        assertFalse(orgs.isEmpty());
    }

    @Test
    public void testUserPublicOrganizationsWhenThereAreNone() throws IOException {
        // bitwiseman had no public org memberships at the time Wiremock recorded the GitHub API responses
        GHUser user = new GHUser();
        user.login = "bitwiseman";

        Map<String, GHOrganization> orgs = gitHub.getUserPublicOrganizations(user);
        assertTrue(orgs.isEmpty());
    }

    private boolean shouldBelongToTeam(String organizationName, String teamName) throws IOException {
        GHOrganization org = gitHub.getOrganization(organizationName);
        assertNotNull(org);
        GHTeam team = org.getTeamByName(teamName);
        assertNotNull(team);
        return team.hasMember(gitHub.getMyself());
    }

    @Test
    public void testShouldFetchTeam() throws Exception {
        GHOrganization organization = gitHub.getOrganization(GITHUB_API_TEST_ORG);
        GHTeam teamByName = organization.getTeams().get("Core Developers");

        GHTeam teamById = gitHub.getTeam((int) teamByName.getId());
        assertNotNull(teamById);

        assertEquals(teamByName.getId(), teamById.getId());
        assertEquals(teamByName.getDescription(), teamById.getDescription());
    }

    @Test
    public void testShouldFetchTeamFromOrganization() throws Exception {
        GHOrganization organization = gitHub.getOrganization(GITHUB_API_TEST_ORG);
        GHTeam teamByName = organization.getTeams().get("Core Developers");

        GHTeam teamById = organization.getTeam(teamByName.getId());
        assertNotNull(teamById);

        assertEquals(teamByName.getId(), teamById.getId());
        assertEquals(teamByName.getDescription(), teamById.getDescription());

        GHTeam teamById2 = organization.getTeam(teamByName.getId());
        assertNotNull(teamById2);

        assertEquals(teamByName.getId(), teamById2.getId());
        assertEquals(teamByName.getDescription(), teamById2.getDescription());

    }

    @Ignore("Needs mocking check")
    @Test
    public void testFetchPullRequest() throws Exception {
        GHRepository r = gitHub.getOrganization("jenkinsci").getRepository("jenkins");
        assertEquals("main", r.getMasterBranch());
        assertEquals("main", r.getDefaultBranch());
        r.getPullRequest(1);
        r.getPullRequests(GHIssueState.OPEN);
    }

    @Ignore("Needs mocking check")
    @Test
    public void testFetchPullRequestAsList() throws Exception {
        GHRepository r = gitHub.getRepository("hub4j/github-api");
        assertEquals("main", r.getMasterBranch());
        PagedIterable<GHPullRequest> i = r.listPullRequests(GHIssueState.CLOSED);
        List<GHPullRequest> prs = i.toList();
        assertNotNull(prs);
        assertTrue(prs.size() > 0);
    }

    @Ignore("Needs mocking check")
    @Test
    public void testRepoPermissions() throws Exception {
        kohsuke();

        GHRepository r = gitHub.getOrganization(GITHUB_API_TEST_ORG).getRepository("github-api");
        assertTrue(r.hasPullAccess());

        r = gitHub.getOrganization("github").getRepository("hub");
        assertFalse(r.hasAdminAccess());
    }

    @Test
    public void testGetMyself() throws Exception {
        GHMyself me = gitHub.getMyself();
        assertNotNull(me);
        assertNotNull(gitHub.getUser("bitwiseman"));
        PagedIterable<GHRepository> ghRepositories = me.listRepositories();
        assertTrue(ghRepositories.iterator().hasNext());
    }

    @Ignore("Needs mocking check")
    @Test
    public void testPublicKeys() throws Exception {
        List<GHKey> keys = gitHub.getMyself().getPublicKeys();
        assertFalse(keys.isEmpty());
    }

    @Test
    public void testOrgFork() throws Exception {
        cleanupRepository(GITHUB_API_TEST_ORG + "/rubywm");
        gitHub.getRepository("kohsuke/rubywm").forkTo(gitHub.getOrganization(GITHUB_API_TEST_ORG));
    }

    @Test
    public void testGetTeamsForRepo() throws Exception {
        kohsuke();
        // 'Core Developers' and 'Owners'
        assertEquals(2,
                gitHub.getOrganization(GITHUB_API_TEST_ORG).getRepository("testGetTeamsForRepo").getTeams().size());
    }

    @Test
    public void testMembership() throws Exception {
        Set<String> members = gitHub.getOrganization(GITHUB_API_TEST_ORG)
                .getRepository("jenkins")
                .getCollaboratorNames();
        // System.out.println(members.contains("kohsuke"));
    }

    @Test
    public void testMemberOrgs() throws Exception {
        HashSet<GHOrganization> o = gitHub.getUser("kohsuke").getOrganizations();
        assertThat(o, hasItem(hasProperty("name", equalTo("CloudBees"))));
    }

    @Test
    public void testOrgTeams() throws Exception {
        kohsuke();
        int sz = 0;
        for (GHTeam t : gitHub.getOrganization(GITHUB_API_TEST_ORG).listTeams()) {
            assertNotNull(t.getName());
            sz++;
        }
        assertTrue(sz < 100);
    }

    @Test
    public void testOrgTeamByName() throws Exception {
        kohsuke();
        GHTeam e = gitHub.getOrganization(GITHUB_API_TEST_ORG).getTeamByName("Core Developers");
        assertNotNull(e);
    }

    @Test
    public void testOrgTeamBySlug() throws Exception {
        kohsuke();
        GHTeam e = gitHub.getOrganization(GITHUB_API_TEST_ORG).getTeamBySlug("core-developers");
        assertNotNull(e);
    }

    @Test
    public void testCommit() throws Exception {
        GHCommit commit = gitHub.getUser("jenkinsci")
                .getRepository("jenkins")
                .getCommit("08c1c9970af4d609ae754fbe803e06186e3206f7");
        assertEquals(1, commit.getParents().size());
        assertEquals(1, commit.getFiles().size());
        assertEquals("https://github.com/jenkinsci/jenkins/commit/08c1c9970af4d609ae754fbe803e06186e3206f7",
                commit.getHtmlUrl().toString());

        File f = commit.getFiles().get(0);
        assertEquals(48, f.getLinesChanged());
        assertThat(f.getLinesAdded(), equalTo(40));
        assertThat(f.getLinesDeleted(), equalTo(8));
        assertThat(f.getPreviousFilename(), nullValue());
        assertThat(f.getPatch(), startsWith("@@ -54,6 +54,14 @@\n"));
        assertThat(f.getSha(), equalTo("04d3e54017542ad0ff46355eababacd4850ccba5"));
        assertThat(f.getBlobUrl().toString(),
                equalTo("https://github.com/jenkinsci/jenkins/blob/08c1c9970af4d609ae754fbe803e06186e3206f7/changelog.html"));
        assertThat(f.getRawUrl().toString(),
                equalTo("https://github.com/jenkinsci/jenkins/raw/08c1c9970af4d609ae754fbe803e06186e3206f7/changelog.html"));

        assertEquals("modified", f.getStatus());
        assertEquals("changelog.html", f.getFileName());

        // walk the tree
        GHTree t = commit.getTree();
        assertThat(IOUtils.toString(t.getEntry("todo.txt").readAsBlob()), containsString("executor rendering"));
        assertNotNull(t.getEntry("war").asTree());
    }

    @Test
    public void testListCommits() throws Exception {
        List<String> sha1 = new ArrayList<String>();
        for (GHCommit c : gitHub.getUser("kohsuke").getRepository("empty-commit").listCommits()) {
            sha1.add(c.getSHA1());
        }
        assertEquals("fdfad6be4db6f96faea1f153fb447b479a7a9cb7", sha1.get(0));
        assertEquals(1, sha1.size());
    }

    @Ignore("Needs mocking check")
    @Test
    public void testBranches() throws Exception {
        Map<String, GHBranch> b = gitHub.getUser("jenkinsci").getRepository("jenkins").getBranches();
        // System.out.println(b);
    }

    @Test
    public void testCommitComment() throws Exception {
        GHRepository r = gitHub.getUser("jenkinsci").getRepository("jenkins");
        PagedIterable<GHCommitComment> comments = r.listCommitComments();
        List<GHCommitComment> batch = comments.iterator().nextPage();
        for (GHCommitComment comment : batch) {
            // System.out.println(comment.getBody());
            assertSame(comment.getOwner(), r);
        }
    }

    @Test
    public void testCreateCommitComment() throws Exception {
        GHCommit commit = gitHub.getUser("kohsuke")
                .getRepository("sandbox-ant")
                .getCommit("8ae38db0ea5837313ab5f39d43a6f73de3bd9000");
        GHCommitComment c = commit.createComment("[testing](http://kohsuse.org/)");
        try {
            assertThat(c.getPath(), nullValue());
            assertThat(c.getLine(), equalTo(-1));
            assertThat(c.getHtmlUrl().toString(),
                    containsString(
                            "kohsuke/sandbox-ant/commit/8ae38db0ea5837313ab5f39d43a6f73de3bd9000#commitcomment-"));
            assertThat(c.listReactions().toList(), is(empty()));

            c.update("updated text");
            assertThat(c.getBody(), equalTo("updated text"));
        } finally {
            c.delete();
        }
    }

    @Test
    public void tryHook() throws Exception {
        GHOrganization o = gitHub.getOrganization(GITHUB_API_TEST_ORG);
        GHRepository r = o.getRepository("github-api");
        try {
            GHHook hook = r.createWebHook(new URL("http://www.google.com/"));
            assertThat(hook.getName(), equalTo("web"));
            assertThat(hook.getEvents().size(), equalTo(1));
            assertThat(hook.getEvents(), contains(GHEvent.PUSH));
            assertThat(hook.getConfig().size(), equalTo(3));
            assertThat(hook.isActive(), equalTo(true));

            GHHook hook2 = r.getHook((int) hook.getId());
            assertThat(hook2.getName(), equalTo("web"));
            assertThat(hook2.getEvents().size(), equalTo(1));
            assertThat(hook2.getEvents(), contains(GHEvent.PUSH));
            assertThat(hook2.getConfig().size(), equalTo(3));
            assertThat(hook2.isActive(), equalTo(true));
            hook2.ping();
            hook2.delete();

            hook = o.createWebHook(new URL("http://www.google.com/"));
            assertThat(hook.getName(), equalTo("web"));
            assertThat(hook.getEvents().size(), equalTo(1));
            assertThat(hook.getEvents(), contains(GHEvent.PUSH));
            assertThat(hook.getConfig().size(), equalTo(3));
            assertThat(hook.isActive(), equalTo(true));

            hook2 = o.getHook((int) hook.getId());
            assertThat(hook2.getName(), equalTo("web"));
            assertThat(hook2.getEvents().size(), equalTo(1));
            assertThat(hook2.getEvents(), contains(GHEvent.PUSH));
            assertThat(hook2.getConfig().size(), equalTo(3));
            assertThat(hook2.isActive(), equalTo(true));
            hook2.ping();
            hook2.delete();

            // System.out.println(hook);
        } finally {
            if (mockGitHub.isUseProxy()) {
                r = getNonRecordingGitHub().getOrganization(GITHUB_API_TEST_ORG).getRepository("github-api");
                for (GHHook h : r.getHooks()) {
                    h.delete();
                }
            }
        }
    }

    @Test
    public void testEventApi() throws Exception {
        for (GHEventInfo ev : gitHub.getEvents()) {
            if (ev.getType() == GHEvent.PULL_REQUEST) {
                if (ev.getId() == 10680625394L) {
                    assertThat(ev.getActorLogin(), equalTo("pull[bot]"));
                    assertThat(ev.getOrganization(), nullValue());
                    assertThat(ev.getRepository().getFullName(), equalTo("daddyfatstacksBIG/lerna"));
                    assertThat(ev.getCreatedAt(), equalTo(GitHubClient.parseDate("2019-10-21T21:54:52Z")));
                    assertThat(ev.getType(), equalTo(GHEvent.PULL_REQUEST));
                }

                GHEventPayload.PullRequest pr = ev.getPayload(GHEventPayload.PullRequest.class);
                assertThat(pr.getNumber(), is(pr.getPullRequest().getNumber()));
            }
        }
    }

    @Ignore("Needs mocking check")
    @Test
    public void testApp() throws IOException {
        // System.out.println(gitHub.getMyself().getEmails());

        // GHRepository r = gitHub.getOrganization("jenkinsci").createRepository("kktest4", "Kohsuke's test",
        // "http://kohsuke.org/", "Everyone", true);
        // r.fork();

        // tryDisablingIssueTrackers(gitHub);

        // tryDisablingWiki(gitHub);

        // GHPullRequest i = gitHub.getOrganization("jenkinsci").getRepository("sandbox").getPullRequest(1);
        // for (GHIssueComment c : i.getComments())
        // // System.out.println(c);
        // // System.out.println(i);

        // gitHub.getMyself().getRepository("perforce-plugin").setEmailServiceHook("kk@kohsuke.org");

        // tryRenaming(gitHub);
        // tryOrgFork(gitHub);

        // testOrganization(gitHub);
        // testPostCommitHook(gitHub);

        // tryTeamCreation(gitHub);

        // t.add(gitHub.getMyself());
        // // System.out.println(t.getMembers());
        // t.remove(gitHub.getMyself());
        // // System.out.println(t.getMembers());

        // GHRepository r = gitHub.getOrganization("HudsonLabs").createRepository("auto-test", "some description",
        // "http://kohsuke.org/", "Plugin Developers", true);

        // r.
        // GitHub hub = GitHub.connectAnonymously();
        //// hub.createRepository("test","test repository",null,true);
        //// hub.getUserTest("kohsuke").getRepository("test").delete();
        //
        // // System.out.println(hub.getUserTest("kohsuke").getRepository("hudson").getCollaborators());
    }

    private void tryDisablingIssueTrackers(GitHub gitHub) throws IOException {
        for (GHRepository r : gitHub.getOrganization("jenkinsci").getRepositories().values()) {
            if (r.hasIssues()) {
                if (r.getOpenIssueCount() == 0) {
                    // System.out.println("DISABLED " + r.getName());
                    r.enableIssueTracker(false);
                } else {
                    // System.out.println("UNTOUCHED " + r.getName());
                }
            }
        }
    }

    private void tryDisablingWiki(GitHub gitHub) throws IOException {
        for (GHRepository r : gitHub.getOrganization("jenkinsci").getRepositories().values()) {
            if (r.hasWiki()) {
                // System.out.println("DISABLED " + r.getName());
                r.enableWiki(false);
            }
        }
    }

    private void tryUpdatingIssueTracker(GitHub gitHub) throws IOException {
        GHRepository r = gitHub.getOrganization("jenkinsci").getRepository("lib-task-reactor");
        // System.out.println(r.hasIssues());
        // System.out.println(r.getOpenIssueCount());
        r.enableIssueTracker(false);
    }

    private void tryRenaming(GitHub gitHub) throws IOException {
        gitHub.getUser("kohsuke").getRepository("test").renameTo("test2");
    }

    private void tryTeamCreation(GitHub gitHub) throws IOException {
        GHOrganization o = gitHub.getOrganization("HudsonLabs");
        GHTeam t = o.createTeam("auto team", Permission.PUSH);
        t.add(o.getRepository("auto-test"));
    }

    private void testPostCommitHook(GitHub gitHub) throws IOException {
        GHRepository r = gitHub.getMyself().getRepository("foo");
        Set<URL> hooks = r.getPostCommitHooks();
        hooks.add(new URL("http://kohsuke.org/test"));
        // System.out.println(hooks);
        hooks.remove(new URL("http://kohsuke.org/test"));
        // System.out.println(hooks);
    }

    @Test
    public void testOrgRepositories() throws IOException {
        kohsuke();
        GHOrganization j = gitHub.getOrganization("jenkinsci");
        long start = System.currentTimeMillis();
        Map<String, GHRepository> repos = j.getRepositories();
        long end = System.currentTimeMillis();
        // System.out.printf("%d repositories in %dms\n", repos.size(), end - start);
    }

    @Test
    public void testOrganization() throws IOException {
        kohsuke();
        GHOrganization j = gitHub.getOrganization(GITHUB_API_TEST_ORG);
        GHTeam t = j.getTeams().get("Core Developers");

        assertNotNull(j.getRepository("jenkins"));

        // t.add(labs.getRepository("xyz"));
    }

    @Test
    public void testCommitStatus() throws Exception {
        GHRepository r = gitHub.getRepository("hub4j/github-api");

        GHCommitStatus state;

        // state = r.createCommitStatus("ecbfdd7315ef2cf04b2be7f11a072ce0bd00c396", GHCommitState.FAILURE,
        // "http://kohsuke.org/", "testing!");

        List<GHCommitStatus> lst = r.listCommitStatuses("ecbfdd7315ef2cf04b2be7f11a072ce0bd00c396").toList();
        state = lst.get(0);
        // System.out.println(state);
        assertEquals("testing!", state.getDescription());
        assertEquals("http://kohsuke.org/", state.getTargetUrl());
    }

    @Test
    public void testCommitShortInfo() throws Exception {
        GHRepository r = gitHub.getRepository("hub4j/github-api");
        GHCommit commit = r.getCommit("86a2e245aa6d71d54923655066049d9e21a15f23");
        assertEquals(commit.getCommitShortInfo().getAuthor().getName(), "Kohsuke Kawaguchi");
        assertEquals(commit.getCommitShortInfo().getMessage(), "doc");
        assertFalse(commit.getCommitShortInfo().getVerification().isVerified());
        assertEquals(commit.getCommitShortInfo().getVerification().getReason(), GHVerification.Reason.UNSIGNED);
        assertThat(commit.getCommitShortInfo().getAuthor().getDate().toInstant().getEpochSecond(),
                equalTo(1271650361L));
        assertThat(commit.getCommitShortInfo().getCommitter().getDate().toInstant().getEpochSecond(),
                equalTo(1271650361L));
    }

    @Ignore("Needs mocking check")
    @Test
    public void testPullRequestPopulate() throws Exception {
        GHRepository r = gitHub.getUser("kohsuke").getRepository("github-api");
        GHPullRequest p = r.getPullRequest(17);
        GHUser u = p.getUser();
        assertNotNull(u.getName());
    }

    @Test
    public void testCheckMembership() throws Exception {
        kohsuke();
        GHOrganization j = gitHub.getOrganization("jenkinsci");
        GHUser kohsuke = gitHub.getUser("kohsuke");
        GHUser b = gitHub.getUser("b");

        assertTrue(j.hasMember(kohsuke));
        assertFalse(j.hasMember(b));

        assertTrue(j.hasPublicMember(kohsuke));
        assertFalse(j.hasPublicMember(b));
    }

    @Ignore("Needs mocking check")
    @Test
    public void testCreateRelease() throws Exception {
        kohsuke();

        GHRepository r = gitHub.getRepository("kohsuke2/testCreateRelease");

        String tagName = UUID.randomUUID().toString();
        String releaseName = "release-" + tagName;

        GHRelease rel = r.createRelease(tagName).name(releaseName).prerelease(false).create();

        Thread.sleep(3000);

        try {

            for (GHTag tag : r.listTags()) {
                if (tagName.equals(tag.getName())) {
                    String ash = tag.getCommit().getSHA1();
                    GHRef ref = r.createRef("refs/heads/" + releaseName, ash);
                    assertEquals(ref.getRef(), "refs/heads/" + releaseName);

                    for (Map.Entry<String, GHBranch> entry : r.getBranches().entrySet()) {
                        // System.out.println(entry.getKey() + "/" + entry.getValue());
                        if (releaseName.equals(entry.getValue().getName())) {
                            return;
                        }
                    }
                    fail("branch not found");
                }
            }
            fail("release creation failed! tag not found");
        } finally {
            rel.delete();
        }
    }

    @Test
    public void testRef() throws IOException {
        GHRef mainRef = gitHub.getRepository("jenkinsci/jenkins").getRef("heads/main");
        assertEquals(mockGitHub.apiServer().baseUrl() + "/repos/jenkinsci/jenkins/git/refs/heads/main",
                mainRef.getUrl().toString());
    }

    @Test
    public void directoryListing() throws IOException {
        List<GHContent> children = gitHub.getRepository("jenkinsci/jenkins").getDirectoryContent("core");
        for (GHContent c : children) {
            // System.out.println(c.getName());
            if (c.isDirectory()) {
                for (GHContent d : c.listDirectoryContent()) {
                    // System.out.println(" " + d.getName());
                }
            }
        }
    }

    @Ignore("Needs mocking check")
    @Test
    public void testAddDeployKey() throws IOException {
        GHRepository myRepository = getTestRepository();
        final GHDeployKey newDeployKey = myRepository.addDeployKey("test",
                "ssh-rsa AAAAB3NzaC1yc2EAAAADAQABAAABAQDUt0RAycC5cS42JKh6SecfFZBR1RrF+2hYMctz4mk74/arBE+wFb7fnSHGzdGKX2h5CFOWODifRCJVhB7hlVxodxe+QkQQYAEL/x1WVCJnGgTGQGOrhOMj95V3UE5pQKhsKD608C+u5tSofcWXLToP1/wZ7U4/AHjqYi08OLsWToHCax55TZkvdt2jo0hbIoYU+XI9Q8Uv4ONDN1oabiOdgeKi8+crvHAuvNleiBhWVBzFh8KdfzaH5uNdw7ihhFjEd1vzqACsjCINCjdMfzl6jD9ExuWuE92nZJnucls2cEoNC6k2aPmrZDg9hA32FXVpyseY+bDUWFU6LO2LG6PB kohsuke@atlas");
        try {
            assertNotNull(newDeployKey.getId());

            GHDeployKey k = Iterables.find(myRepository.getDeployKeys(), new Predicate<GHDeployKey>() {
                public boolean apply(GHDeployKey deployKey) {
                    return newDeployKey.getId() == deployKey.getId();
                }
            });
            assertNotNull(k);
        } finally {
            newDeployKey.delete();
        }
    }

    @Ignore("Needs mocking check")
    @Test
    public void testCommitStatusContext() throws IOException {
        GHRepository myRepository = getTestRepository();
        GHRef mainRef = myRepository.getRef("heads/main");
        GHCommitStatus commitStatus = myRepository.createCommitStatus(mainRef.getObject()
                .getSha(), GHCommitState.SUCCESS, "http://www.example.com", "test", "test/context");
        assertEquals("test/context", commitStatus.getContext());

    }

    @Ignore("Needs mocking check")
    @Test
    public void testMemberPagenation() throws IOException {
        Set<GHUser> all = new HashSet<GHUser>();
        for (GHUser u : gitHub.getOrganization(GITHUB_API_TEST_ORG).getTeamByName("Core Developers").listMembers()) {
            // System.out.println(u.getLogin());
            all.add(u);
        }
        assertFalse(all.isEmpty());
    }

    @Test
    public void testCommitSearch() throws IOException {
        PagedSearchIterable<GHCommit> r = gitHub.searchCommits()
                .org("github-api")
                .repo("github-api")
                .author("kohsuke")
                .sort(GHCommitSearchBuilder.Sort.COMMITTER_DATE)
                .list();
        assertTrue(r.getTotalCount() > 0);

        GHCommit firstCommit = r.iterator().next();
        assertTrue(firstCommit.getFiles().size() > 0);
    }

    @Test
    public void testIssueSearch() throws IOException {
        PagedSearchIterable<GHIssue> r = gitHub.searchIssues()
                .mentions("kohsuke")
                .isOpen()
                .sort(GHIssueSearchBuilder.Sort.UPDATED)
                .list();
        assertTrue(r.getTotalCount() > 0);
        for (GHIssue issue : r) {
            assertThat(issue.getTitle(), notNullValue());
            PagedIterable<GHIssueComment> comments = issue.listComments();
            for (GHIssueComment comment : comments) {
                assertThat(comment, notNullValue());
            }
        }
    }

    @Test // issue #99
    public void testReadme() throws IOException {
        GHContent readme = gitHub.getRepository("hub4j-test-org/test-readme").getReadme();
        assertEquals(readme.getName(), "README.md");
        assertEquals(readme.getContent(), "This is a markdown readme.\n");
    }

    @Ignore("Needs mocking check")
    @Test
    public void testTrees() throws IOException {
        GHTree mainTree = gitHub.getRepository("hub4j/github-api").getTree("main");
        boolean foundReadme = false;
        for (GHTreeEntry e : mainTree.getTree()) {
            if ("readme".equalsIgnoreCase(e.getPath().replaceAll("\\.md", ""))) {
                foundReadme = true;
                break;
            }
        }
        assertTrue(foundReadme);
    }

    @Test
    public void testTreesRecursive() throws IOException {
        GHTree mainTree = gitHub.getRepository("hub4j/github-api").getTreeRecursive("main", 1);
        boolean foundThisFile = false;
        for (GHTreeEntry e : mainTree.getTree()) {
            if (e.getPath().endsWith(AppTest.class.getSimpleName() + ".java")) {
                foundThisFile = true;
                assertThat(e.getPath(), equalTo("src/test/java/org/kohsuke/github/AppTest.java"));
                assertThat(e.getSha(), equalTo("baad7a7c4cf409f610a0e8c7eba17664eb655c44"));
                assertThat(e.getMode(), equalTo("100755"));
                assertThat(e.getSize(), greaterThan(30000L));
                assertThat(e.getUrl().toString(),
                        containsString("/repos/hub4j/github-api/git/blobs/baad7a7c4cf409f610a0e8c7eba17664eb655c44"));
                GHBlob blob = e.asBlob();
                assertThat(e.asBlob().getUrl().toString(),
                        containsString("/repos/hub4j/github-api/git/blobs/baad7a7c4cf409f610a0e8c7eba17664eb655c44"));
                break;
            }

        }
        assertTrue(foundThisFile);
    }

    @Test
    public void testRepoLabel() throws IOException {
        cleanupLabel("test");
        cleanupLabel("test2");

        GHRepository r = gitHub.getRepository("hub4j-test-org/test-labels");
        List<GHLabel> lst = r.listLabels().toList();
        for (GHLabel l : lst) {
            assertThat(l.getUrl(), containsString(l.getName().replace(" ", "%20")));
        }
        assertTrue(lst.size() > 5);
        GHLabel e = r.getLabel("enhancement");
        assertEquals("enhancement", e.getName());
        assertNotNull(e.getUrl());
        assertEquals(177339106L, e.getId());
        assertEquals("MDU6TGFiZWwxNzczMzkxMDY=", e.getNodeId());
        assertTrue(e.isDefault());
        assertTrue(Pattern.matches("[0-9a-fA-F]{6}", e.getColor()));

        GHLabel t = null;
        GHLabel t2 = null;
        GHLabel t3 = null;
        try {// CRUD
            t = r.createLabel("test", "123456");
            t2 = r.getLabel("test");
            assertThat(t, not(sameInstance(t2)));
            assertThat(t, equalTo(t2));

            assertFalse(t2.isDefault());

            assertEquals(t.getId(), t2.getId());
            assertEquals(t.getNodeId(), t2.getNodeId());
            assertEquals(t.getName(), t2.getName());
            assertEquals(t.getColor(), "123456");
            assertEquals(t.getColor(), t2.getColor());
            assertEquals(t.getDescription(), "");
            assertEquals(t.getDescription(), t2.getDescription());
            assertEquals(t.getUrl(), t2.getUrl());
            assertEquals(t.isDefault(), t2.isDefault());

            // update works on multiple changes in one call
            t3 = t.update().color("000000").description("It is dark!").done();

            // instances behave as immutable by default. Update returns a new updated instance.
            assertThat(t, not(sameInstance(t2)));
            assertThat(t, equalTo(t2));

            assertThat(t, not(sameInstance(t3)));
            assertThat(t, not(equalTo(t3)));

            assertEquals(t.getColor(), "123456");
            assertEquals(t.getDescription(), "");
            assertEquals(t3.getColor(), "000000");
            assertEquals(t3.getDescription(), "It is dark!");

            // Test deprecated methods
            t.setDescription("Deprecated");
            t = r.getLabel("test");

            // By using the old instance t when calling setDescription it also sets color to the old value
            // this is a bad behavior, but it is expected
            assertEquals(t.getColor(), "123456");
            assertEquals(t.getDescription(), "Deprecated");

            t.setColor("000000");
            t = r.getLabel("test");
            assertEquals(t.getColor(), "000000");
            assertEquals(t.getDescription(), "Deprecated");

            // set() makes a single change
            t3 = t.set().description("this is also a test");

            // instances behave as immutable by default. Update returns a new updated instance.
            assertThat(t, not(sameInstance(t3)));
            assertThat(t, not(equalTo(t3)));

            assertEquals(t3.getColor(), "000000");
            assertEquals(t3.getDescription(), "this is also a test");

            t.delete();
            try {
                t = r.getLabel("test");
                fail("Test label should be deleted.");
            } catch (IOException ex) {
                assertThat(ex, instanceOf(FileNotFoundException.class));
            }

            t = r.createLabel("test2", "123457", "this is a different test");
            t2 = r.getLabel("test2");

            assertEquals(t.getName(), t2.getName());
            assertEquals(t.getColor(), "123457");
            assertEquals(t.getColor(), t2.getColor());
            assertEquals(t.getDescription(), "this is a different test");
            assertEquals(t.getDescription(), t2.getDescription());
            assertEquals(t.getUrl(), t2.getUrl());
            t.delete();

            // Allow null description
            t = GHLabel.create(r).name("test2").color("123458").done();
            assertThat(t.getName(), equalTo("test2"));
            assertThat(t.getDescription(), is(nullValue()));

        } finally {
            cleanupLabel("test");
            cleanupLabel("test2");
        }
    }

    void cleanupLabel(String name) {
        if (mockGitHub.isUseProxy()) {
            try {
                GHLabel t = getNonRecordingGitHub().getRepository("hub4j-test-org/test-labels").getLabel(name);
                t.delete();
            } catch (IOException e) {

            }
        }
    }

    @Test
    public void testSubscribers() throws IOException {
        boolean bitwiseman = false;
        GHRepository mr = gitHub.getRepository("bitwiseman/github-api");
        for (GHUser u : mr.listSubscribers()) {
            bitwiseman |= u.getLogin().equals("bitwiseman");
        }
        assertTrue(bitwiseman);

        boolean githubApiFound = false;
        for (GHRepository r : gitHub.getUser("bitwiseman").listRepositories()) {
            githubApiFound |= r.equals(mr);
        }
        assertTrue(githubApiFound);
    }

    @Test
    public void notifications() throws Exception {
        boolean found = false;
        for (GHThread t : gitHub.listNotifications().nonBlocking(true).read(true)) {
            if (!found) {
                found = true;
                // both read and unread are included
                assertThat(t.getTitle(), is("Create a Jenkinsfile for Librecores CI in mor1kx"));
                assertThat(t.getLastReadAt(), notNullValue());
                assertThat(t.isRead(), equalTo(true));

                t.markAsRead(); // test this by calling it once on old notfication
            }
            assertThat(t.getReason(), oneOf("subscribed", "mention", "review_requested", "comment"));
            assertThat(t.getTitle(), notNullValue());
            assertThat(t.getLastCommentUrl(), notNullValue());
            assertThat(t.getRepository(), notNullValue());
            assertThat(t.getUpdatedAt(), notNullValue());
            assertThat(t.getType(), oneOf("Issue", "PullRequest"));

            // both thread an unread are included
            // assertThat(t.getLastReadAt(), notNullValue());
            // assertThat(t.isRead(), equalTo(true));

            // Doesn't exist on threads but is part of GHObject. :(
            assertThat(t.getCreatedAt(), nullValue());

        }
        assertTrue(found);
        gitHub.listNotifications().markAsRead();
    }

    @Ignore("Needs mocking check")
    @Test
    public void checkToString() throws Exception {
        // Just basic code coverage to make sure toString() doesn't blow up
        GHUser u = gitHub.getUser("rails");
        // System.out.println(u);
        GHRepository r = u.getRepository("rails");
        // System.out.println(r);
        // System.out.println(r.getIssue(1));
    }

    @Test
    public void reactions() throws Exception {
        GHIssue i = gitHub.getRepository("hub4j/github-api").getIssue(311);

        List<GHReaction> l;
        // retrieval
        l = i.listReactions().toList();
        assertThat(l.size(), equalTo(1));

        assertThat(l.get(0).getUser().getLogin(), is("kohsuke"));
        assertThat(l.get(0).getContent(), is(ReactionContent.HEART));

        // CRUD
        GHReaction a;
        a = i.createReaction(ReactionContent.HOORAY);
        assertThat(a.getUser().getLogin(), is(gitHub.getMyself().getLogin()));
        assertThat(a.getContent(), is(ReactionContent.HOORAY));
        a.delete();

        l = i.listReactions().toList();
        assertThat(l.size(), equalTo(1));

        a = i.createReaction(ReactionContent.PLUS_ONE);
        assertThat(a.getUser().getLogin(), is(gitHub.getMyself().getLogin()));
        assertThat(a.getContent(), is(ReactionContent.PLUS_ONE));

        a = i.createReaction(ReactionContent.CONFUSED);
        assertThat(a.getUser().getLogin(), is(gitHub.getMyself().getLogin()));
        assertThat(a.getContent(), is(ReactionContent.CONFUSED));

        a = i.createReaction(ReactionContent.EYES);
        assertThat(a.getUser().getLogin(), is(gitHub.getMyself().getLogin()));
        assertThat(a.getContent(), is(ReactionContent.EYES));

        a = i.createReaction(ReactionContent.ROCKET);
        assertThat(a.getUser().getLogin(), is(gitHub.getMyself().getLogin()));
        assertThat(a.getContent(), is(ReactionContent.ROCKET));

        l = i.listReactions().toList();
        assertThat(l.size(), equalTo(5));
        assertThat(l.get(0).getUser().getLogin(), is("kohsuke"));
        assertThat(l.get(0).getContent(), is(ReactionContent.HEART));
        assertThat(l.get(1).getUser().getLogin(), is(gitHub.getMyself().getLogin()));
        assertThat(l.get(1).getContent(), is(ReactionContent.PLUS_ONE));
        assertThat(l.get(2).getUser().getLogin(), is(gitHub.getMyself().getLogin()));
        assertThat(l.get(2).getContent(), is(ReactionContent.CONFUSED));
        assertThat(l.get(3).getUser().getLogin(), is(gitHub.getMyself().getLogin()));
        assertThat(l.get(3).getContent(), is(ReactionContent.EYES));
        assertThat(l.get(4).getUser().getLogin(), is(gitHub.getMyself().getLogin()));
        assertThat(l.get(4).getContent(), is(ReactionContent.ROCKET));

        l.get(1).delete();
        l.get(2).delete();
        l.get(3).delete();
        l.get(4).delete();

        l = i.listReactions().toList();
        assertThat(l.size(), equalTo(1));
    }

    @Test
    public void listOrgMemberships() throws Exception {
        GHMyself me = gitHub.getMyself();
        for (GHMembership m : me.listOrgMemberships()) {
            assertThat(m.getUser(), is((GHUser) me));
            assertNotNull(m.getState());
            assertNotNull(m.getRole());
        }
    }

    @Test
    public void blob() throws Exception {
        Assume.assumeFalse(SystemUtils.IS_OS_WINDOWS);

        GHRepository r = gitHub.getRepository("hub4j/github-api");
        String sha1 = "a12243f2fc5b8c2ba47dd677d0b0c7583539584d";

        assertBlobContent(r.readBlob(sha1));

        GHBlob blob = r.getBlob(sha1);
        assertBlobContent(blob.read());
        assertThat(blob.getSha(), is("a12243f2fc5b8c2ba47dd677d0b0c7583539584d"));
        assertThat(blob.getSize(), is(1104L));
    }

    private void assertBlobContent(InputStream is) throws Exception {
        String content = new String(IOUtils.toByteArray(is), StandardCharsets.UTF_8);
        assertThat(content, containsString("Copyright (c) 2011- Kohsuke Kawaguchi and other contributors"));
        assertThat(content, containsString("FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR"));
        assertThat(content.length(), is(1104));
    }
}
