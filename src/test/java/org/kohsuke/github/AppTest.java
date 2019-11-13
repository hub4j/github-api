package org.kohsuke.github;

import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;

import org.apache.commons.io.IOUtils;
import org.junit.Ignore;
import org.junit.Test;
import org.kohsuke.github.GHCommit.File;
import org.kohsuke.github.GHOrganization.Permission;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.*;
import java.util.Map.Entry;
import java.util.regex.Pattern;

import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.hasProperty;

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

        GHRepository r = gitHub.createRepository("github-api-test-rename", "a test repository", "http://github-api.kohsuke.org/", true);
        assertThat(r.hasIssues(), is(true));

        r.enableIssueTracker(false);
        r.enableDownloads(false);
        r.enableWiki(false);
        r.renameTo(targetName);
        getUser().getRepository(targetName).delete();
    }

    @Test
    public void testRepositoryWithAutoInitializationCRUD() throws Exception {
        String name = "github-api-test-autoinit";
        cleanupUserRepository(name);
        GHRepository r = gitHub.createRepository(name)
            .description("a test repository for auto init")
            .homepage("http://github-api.kohsuke.org/")
            .autoInit(true).create();
        r.enableIssueTracker(false);
        r.enableDownloads(false);
        r.enableWiki(false);
        if (mockGitHub.isUseProxy()) {
            Thread.sleep(3000);
        }
        assertNotNull(r.getReadme());
        getUser().getRepository(name).delete();
    }

    private void cleanupUserRepository(final String name) throws IOException {
        if (mockGitHub.isUseProxy()) {
            cleanupRepository(getUser(gitHubBeforeAfter).getLogin() + "/" + name);
        }
    }

    @Test
    public void testCredentialValid() throws IOException {
        assertTrue(gitHub.isCredentialValid());
        GitHub connect = GitHub.connect("totally", "bogus");
        assertFalse(connect.isCredentialValid());
    }

    @Test
    public void testIssueWithNoComment() throws IOException {
        GHRepository repository = gitHub.getRepository("kohsuke/test");
        List<GHIssueComment> v = repository.getIssue(4).getComments();
        //System.out.println(v);
        assertTrue(v.isEmpty());

        v = repository.getIssue(3).getComments();
        //System.out.println(v);
        assertTrue(v.size() == 3);
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
        GHDeployment deployment = repository.createDeployment("master")
            .payload("{\"user\":\"atmos\",\"room_id\":123456}")
            .description("question")
            .environment("unittest")
            .create();
        assertNotNull(deployment.getCreator());
        assertNotNull(deployment.getId());
        List<GHDeployment> deployments = repository.listDeployments(null, "master", null, "unittest").asList();
        assertNotNull(deployments);
        assertFalse(Iterables.isEmpty(deployments));
        GHDeployment unitTestDeployment = deployments.get(0);
        assertEquals("unittest", unitTestDeployment.getEnvironment());
        assertEquals("master", unitTestDeployment.getRef());
    }

    @Ignore("Needs mocking check")
    @Test
    public void testGetDeploymentStatuses() throws IOException {
        GHRepository repository = getTestRepository();
        GHDeployment deployment = repository.createDeployment("master")
            .description("question")
            .payload("{\"user\":\"atmos\",\"room_id\":123456}")
            .create();
        GHDeploymentStatus ghDeploymentStatus = deployment.createStatus(GHDeploymentState.SUCCESS)
            .description("success")
            .targetUrl("http://www.github.com").create();
        Iterable<GHDeploymentStatus> deploymentStatuses = deployment.listStatuses();
        assertNotNull(deploymentStatuses);
        assertEquals(1, Iterables.size(deploymentStatuses));
        assertEquals(ghDeploymentStatus.getId(), Iterables.get(deploymentStatuses, 0).getId());
    }

    @Test
    public void testGetIssues() throws Exception {
        List<GHIssue> closedIssues = gitHub.getOrganization("github-api").getRepository("github-api").getIssues(GHIssueState.CLOSED);
        // prior to using PagedIterable GHRepository.getIssues(GHIssueState) would only retrieve 30 issues
        assertTrue(closedIssues.size() > 150);
    }

    private GHRepository getTestRepository() throws IOException {
        return getTempRepository(GITHUB_API_TEST_REPO);
    }

    @Ignore("Needs to be rewritten to not create new issues just to check that they can be found.")
    @Test
    public void testListIssues() throws IOException {
        GHUser u = getUser();
        GHRepository repository = getTestRepository();

        GHMilestone milestone = repository.createMilestone(System.currentTimeMillis() + "", "Test Milestone");
        milestone.close();
        GHIssue unhomed = null;
        GHIssue homed = null;
        try {
            unhomed = repository.createIssue("testing").body("this is body")
                .assignee(u)
                .label("bug")
                .label("question")
                .create();
            assertEquals(unhomed.getNumber(), repository.getIssues(GHIssueState.OPEN, null).get(0).getNumber());
            homed = repository.createIssue("testing").body("this is body")
                .assignee(u)
                .label("bug")
                .label("question")
                .milestone(milestone)
                .create();
            assertEquals(homed.getNumber(), repository.getIssues(GHIssueState.OPEN, milestone).get(0).getNumber());
        } finally {
            if (unhomed != null) {
                unhomed.close();
            }
            if (homed != null) {
                homed.close();
            }
        }
    }

    @Test
    public void testRateLimit() throws IOException {
        assertThat(gitHub.getRateLimit(), notNullValue());
    }

    @Test
    public void testMyOrganizations() throws IOException {
        Map<String, GHOrganization> org = gitHub.getMyOrganizations();
        assertFalse(org.keySet().contains(null));
        //System.out.println(org);
    }

    @Test
    public void testMyOrganizationsContainMyTeams() throws IOException {
        Map<String, Set<GHTeam>> teams = gitHub.getMyTeams();
        Map<String, GHOrganization> myOrganizations = gitHub.getMyOrganizations();
        //GitHub no longer has default 'owners' team, so there may be organization memberships without a team
        //https://help.github.com/articles/about-improved-organization-permissions/
        assertTrue(myOrganizations.keySet().containsAll(teams.keySet()));
    }

    @Test
    public void testMyTeamsShouldIncludeMyself() throws IOException {
        Map<String, Set<GHTeam>> teams = gitHub.getMyTeams();
        for (Entry<String, Set<GHTeam>> teamsPerOrg : teams.entrySet()) {
            String organizationName = teamsPerOrg.getKey();
            for (GHTeam team : teamsPerOrg.getValue()) {
                String teamName = team.getName();
                assertTrue("Team " + teamName + " in organization " + organizationName
                        + " does not contain myself",
                    shouldBelongToTeam(organizationName, teamName));
            }
        }
    }

    @Test
    public void testUserPublicOrganizationsWhenThereAreSome() throws IOException {
        // kohsuke had some public org memberships at the time Wiremock recorded the GitHub API responses
        GHUser user = new GHUser();
        user.login = "kohsuke";

        Map<String, GHOrganization> orgs = gitHub.getUserPublicOrganizations( user );
        assertFalse(orgs.isEmpty());
    }

    @Test
    public void testUserPublicOrganizationsWhenThereAreNone() throws IOException {
        // bitwiseman had no public org memberships at the time Wiremock recorded the GitHub API responses
        GHUser user = new GHUser();
        user.login = "bitwiseman";

        Map<String, GHOrganization> orgs = gitHub.getUserPublicOrganizations( user );
        assertTrue(orgs.isEmpty());
    }

    private boolean shouldBelongToTeam(String organizationName, String teamName) throws IOException {
        GHOrganization org = gitHub.getOrganization(organizationName);
        assertNotNull(org);
        GHTeam team = org.getTeamByName(teamName);
        assertNotNull(team);
        return team.hasMember(gitHub.getMyself());
    }

    @Ignore("Needs mocking check")
    @Test
    public void testShouldFetchTeam() throws Exception {
        GHOrganization j = gitHub.getOrganization(GITHUB_API_TEST_ORG);
        GHTeam teamByName = j.getTeams().get("Core Developers");

        GHTeam teamById = gitHub.getTeam(teamByName.getId());
        assertNotNull(teamById);

        assertEquals(teamByName, teamById);
    }

    @Ignore("Needs mocking check")
    @Test
    public void testFetchPullRequest() throws Exception {
        GHRepository r = gitHub.getOrganization("jenkinsci").getRepository("jenkins");
        assertEquals("master", r.getMasterBranch());
        r.getPullRequest(1);
        r.getPullRequests(GHIssueState.OPEN);
    }

    @Ignore("Needs mocking check")
    @Test
    public void testFetchPullRequestAsList() throws Exception {
        GHRepository r = gitHub.getRepository("github-api/github-api");
        assertEquals("master", r.getMasterBranch());
        PagedIterable<GHPullRequest> i = r.listPullRequests(GHIssueState.CLOSED);
        List<GHPullRequest> prs = i.asList();
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

    @Ignore("Needs mocking check")
    @Test
    public void testOrgFork() throws Exception {
        kohsuke();

        gitHub.getRepository("kohsuke/rubywm").forkTo(gitHub.getOrganization(GITHUB_API_TEST_ORG));
    }

    @Test
    public void testGetTeamsForRepo() throws Exception {
        kohsuke();
        // 'Core Developers' and 'Owners'
        assertEquals(2, gitHub.getOrganization(GITHUB_API_TEST_ORG).getRepository("testGetTeamsForRepo").getTeams().size());
    }

    @Test
    public void testMembership() throws Exception {
        Set<String> members = gitHub.getOrganization(GITHUB_API_TEST_ORG).getRepository("jenkins").getCollaboratorNames();
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
        GHCommit commit = gitHub.getUser("jenkinsci").getRepository("jenkins").getCommit("08c1c9970af4d609ae754fbe803e06186e3206f7");
        assertEquals(1, commit.getParents().size());
        assertEquals(1, commit.getFiles().size());
        assertEquals("https://github.com/jenkinsci/jenkins/commit/08c1c9970af4d609ae754fbe803e06186e3206f7",
            commit.getHtmlUrl().toString());

        File f = commit.getFiles().get(0);
        assertEquals(48, f.getLinesChanged());
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

    public void testQueryCommits() throws Exception {
        List<String> sha1 = new ArrayList<String>();
        for (GHCommit c : gitHub.getUser("jenkinsci").getRepository("jenkins").queryCommits()
            .since(new Date(1199174400000L)).until(1201852800000L).path("pom.xml").list()) {
            // System.out.println(c.getSHA1());
            sha1.add(c.getSHA1());
        }
        assertEquals("1cccddb22e305397151b2b7b87b4b47d74ca337b", sha1.get(0));
        assertEquals(29, sha1.size());
    }

    @Ignore("Needs mocking check")
    @Test
    public void testBranches() throws Exception {
        Map<String, GHBranch> b =
            gitHub.getUser("jenkinsci").getRepository("jenkins").getBranches();
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
        GHCommit commit = gitHub.getUser("kohsuke").getRepository("sandbox-ant").getCommit("8ae38db0ea5837313ab5f39d43a6f73de3bd9000");
        GHCommitComment c = commit.createComment("[testing](http://kohsuse.org/)");
        // System.out.println(c);
        c.update("updated text");
        // System.out.println(c);
        c.delete();
    }

    @Test
    public void tryHook() throws Exception {
        kohsuke();
        GHRepository r = gitHub.getOrganization(GITHUB_API_TEST_ORG).getRepository("github-api");
        GHHook hook = r.createWebHook(new URL("http://www.google.com/"));
        // System.out.println(hook);

        if (mockGitHub.isUseProxy()) {
            r = gitHubBeforeAfter.getOrganization(GITHUB_API_TEST_ORG).getRepository("github-api");
            for (GHHook h : r.getHooks()) {
                h.delete();
            }
        }
    }

    @Test
    public void testEventApi() throws Exception {
        for (GHEventInfo ev : gitHub.getEvents()) {
            if (ev.getType() == GHEvent.PULL_REQUEST) {
                GHEventPayload.PullRequest pr = ev.getPayload(GHEventPayload.PullRequest.class);
                assertThat(pr.getNumber(), is(pr.getPullRequest().getNumber()));
            }
        }
    }

    @Ignore("Needs mocking check")
    @Test
    public void testApp() throws IOException {
        // System.out.println(gitHub.getMyself().getEmails());

//        GHRepository r = gitHub.getOrganization("jenkinsci").createRepository("kktest4", "Kohsuke's test", "http://kohsuke.org/", "Everyone", true);
//        r.fork();

//        tryDisablingIssueTrackers(gitHub);

//        tryDisablingWiki(gitHub);

//        GHPullRequest i = gitHub.getOrganization("jenkinsci").getRepository("sandbox").getPullRequest(1);
//        for (GHIssueComment c : i.getComments())
//            // System.out.println(c);
//        // System.out.println(i);

//        gitHub.getMyself().getRepository("perforce-plugin").setEmailServiceHook("kk@kohsuke.org");

//        tryRenaming(gitHub);
//        tryOrgFork(gitHub);

//        testOrganization(gitHub);
//        testPostCommitHook(gitHub);

//        tryTeamCreation(gitHub);

//        t.add(gitHub.getMyself());
//        // System.out.println(t.getMembers());
//        t.remove(gitHub.getMyself());
//        // System.out.println(t.getMembers());

//        GHRepository r = gitHub.getOrganization("HudsonLabs").createRepository("auto-test", "some description", "http://kohsuke.org/", "Plugin Developers", true);

//        r.
//        GitHub hub = GitHub.connectAnonymously();
////        hub.createRepository("test","test repository",null,true);
////        hub.getUserTest("kohsuke").getRepository("test").delete();
//
//        // System.out.println(hub.getUserTest("kohsuke").getRepository("hudson").getCollaborators());
    }

    private void tryDisablingIssueTrackers(GitHub gitHub) throws IOException {
        for (GHRepository r : gitHub.getOrganization("jenkinsci").getRepositories().values()) {
            if (r.hasIssues()) {
                if (r.getOpenIssueCount() == 0) {
                    // System.out.println("DISABLED  " + r.getName());
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
                // System.out.println("DISABLED  " + r.getName());
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

//        t.add(labs.getRepository("xyz"));
    }

    @Test
    public void testCommitStatus() throws Exception {
        GHRepository r = gitHub.getRepository("github-api/github-api");

        GHCommitStatus state;

//        state = r.createCommitStatus("ecbfdd7315ef2cf04b2be7f11a072ce0bd00c396", GHCommitState.FAILURE, "http://kohsuke.org/", "testing!");

        List<GHCommitStatus> lst = r.listCommitStatuses("ecbfdd7315ef2cf04b2be7f11a072ce0bd00c396").asList();
        state = lst.get(0);
        // System.out.println(state);
        assertEquals("testing!", state.getDescription());
        assertEquals("http://kohsuke.org/", state.getTargetUrl());
    }

    @Test
    public void testCommitShortInfo() throws Exception {
        GHRepository r = gitHub.getRepository("github-api/github-api");
        GHCommit commit = r.getCommit("86a2e245aa6d71d54923655066049d9e21a15f23");
        assertEquals(commit.getCommitShortInfo().getAuthor().getName(), "Kohsuke Kawaguchi");
        assertEquals(commit.getCommitShortInfo().getMessage(), "doc");
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

        GHRelease rel = r.createRelease(tagName)
            .name(releaseName)
            .prerelease(false)
            .create();

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
        GHRef masterRef = gitHub.getRepository("jenkinsci/jenkins").getRef("heads/master");
        assertEquals(mockGitHub.apiServer().baseUrl() + "/repos/jenkinsci/jenkins/git/refs/heads/master", masterRef.getUrl().toString());
    }

    @Test
    public void directoryListing() throws IOException {
        List<GHContent> children = gitHub.getRepository("jenkinsci/jenkins").getDirectoryContent("core");
        for (GHContent c : children) {
            // System.out.println(c.getName());
            if (c.isDirectory()) {
                for (GHContent d : c.listDirectoryContent()) {
                    // System.out.println("  " + d.getName());
                }
            }
        }
    }

    @Ignore("Needs mocking check")
    @Test
    public void testAddDeployKey() throws IOException {
        GHRepository myRepository = getTestRepository();
        final GHDeployKey newDeployKey = myRepository.addDeployKey("test", "ssh-rsa AAAAB3NzaC1yc2EAAAADAQABAAABAQDUt0RAycC5cS42JKh6SecfFZBR1RrF+2hYMctz4mk74/arBE+wFb7fnSHGzdGKX2h5CFOWODifRCJVhB7hlVxodxe+QkQQYAEL/x1WVCJnGgTGQGOrhOMj95V3UE5pQKhsKD608C+u5tSofcWXLToP1/wZ7U4/AHjqYi08OLsWToHCax55TZkvdt2jo0hbIoYU+XI9Q8Uv4ONDN1oabiOdgeKi8+crvHAuvNleiBhWVBzFh8KdfzaH5uNdw7ihhFjEd1vzqACsjCINCjdMfzl6jD9ExuWuE92nZJnucls2cEoNC6k2aPmrZDg9hA32FXVpyseY+bDUWFU6LO2LG6PB kohsuke@atlas");
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
        GHRef masterRef = myRepository.getRef("heads/master");
        GHCommitStatus commitStatus = myRepository.createCommitStatus(masterRef.getObject().getSha(), GHCommitState.SUCCESS, "http://www.example.com", "test", "test/context");
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

    @Ignore("Needs mocking check")
    @Test
    public void testCommitSearch() throws IOException {
        PagedSearchIterable<GHCommit> r = gitHub.searchCommits().author("kohsuke").list();
        assertTrue(r.getTotalCount() > 0);

        GHCommit firstCommit = r.iterator().next();
        assertTrue(firstCommit.getFiles().size() > 0);
    }

    @Ignore("Needs mocking check")
    @Test
    public void testIssueSearch() throws IOException {
        PagedSearchIterable<GHIssue> r = gitHub.searchIssues().mentions("kohsuke").isOpen().list();
        for (GHIssue i : r) {
            // System.out.println(i.getTitle());
        }
    }

    @Ignore("Needs mocking check")
    @Test   // issue #99
    public void testReadme() throws IOException {
        GHContent readme = gitHub.getRepository("github-api-test-org/test-readme").getReadme();
        assertEquals(readme.getName(), "README.md");
        assertEquals(readme.getContent(), "This is a markdown readme.\n");
    }


    @Ignore("Needs mocking check")
    @Test
    public void testTrees() throws IOException {
        GHTree masterTree = gitHub.getRepository("github-api/github-api").getTree("master");
        boolean foundReadme = false;
        for (GHTreeEntry e : masterTree.getTree()) {
            if ("readme".equalsIgnoreCase(e.getPath().replaceAll("\\.md", ""))) {
                foundReadme = true;
                break;
            }
        }
        assertTrue(foundReadme);
    }

    @Test
    public void testTreesRecursive() throws IOException {
        GHTree masterTree = gitHub.getRepository("github-api/github-api").getTreeRecursive("master", 1);
        boolean foundThisFile = false;
        for (GHTreeEntry e : masterTree.getTree()) {
            if (e.getPath().endsWith(AppTest.class.getSimpleName() + ".java")) {
                foundThisFile = true;
                break;
            }
        }
        assertTrue(foundThisFile);
    }

    @Test
    public void testRepoLabel() throws IOException {
        cleanupLabel("test");
        cleanupLabel("test2");

        GHRepository r = gitHub.getRepository("github-api-test-org/test-labels");
        List<GHLabel> lst = r.listLabels().asList();
        for (GHLabel l : lst) {
            // System.out.println(l.getName());
        }
        assertTrue(lst.size() > 5);
        GHLabel e = r.getLabel("enhancement");
        assertEquals("enhancement", e.getName());
        assertNotNull(e.getUrl());
        assertTrue(Pattern.matches("[0-9a-fA-F]{6}", e.getColor()));

        GHLabel t = null;
        GHLabel t2 = null;
        try {// CRUD
            t = r.createLabel("test", "123456");
            t2 = r.getLabel("test");
            assertEquals(t.getName(), t2.getName());
            assertEquals(t.getColor(), "123456");
            assertEquals(t.getColor(), t2.getColor());
            assertEquals(t.getDescription(), "");
            assertEquals(t.getDescription(), t2.getDescription());
            assertEquals(t.getUrl(), t2.getUrl());

            t.setColor("000000");

            // This is annoying behavior, but it is by design at this time.
            // Verifying so we can know when it is fixed.
            assertEquals(t.getColor(), "123456");

            t = r.getLabel("test");
            t.setDescription("this is also a test");

            GHLabel t3 = r.getLabel("test");
            assertEquals(t3.getColor(), "000000");
            assertEquals(t3.getDescription(), "this is also a test");
            t.delete();

            t = r.createLabel("test2", "123457", "this is a different test");
            t2 = r.getLabel("test2");
            assertEquals(t.getName(), t2.getName());
            assertEquals(t.getColor(), "123457");
            assertEquals(t.getColor(), t2.getColor());
            assertEquals(t.getDescription(), "this is a different test");
            assertEquals(t.getDescription(), t2.getDescription());
            assertEquals(t.getUrl(), t2.getUrl());
        } finally {
            cleanupLabel("test");
            cleanupLabel("test2");
        }
    }

    void cleanupLabel(String name) {
        if (mockGitHub.isUseProxy()) {
            try {
                GHLabel t = gitHubBeforeAfter.getRepository("github-api-test-org/test-labels").getLabel("test");
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
                t.markAsRead(); // test this by calling it once on old notfication
            }
            assertNotNull(t.getTitle());
            assertNotNull(t.getReason());
        }
        assertTrue(found);
        gitHub.listNotifications().markAsRead();
    }

    /**
     * Just basic code coverage to make sure toString() doesn't blow up
     */
    @Ignore("Needs mocking check")
    @Test
    public void checkToString() throws Exception {
        GHUser u = gitHub.getUser("rails");
        // System.out.println(u);
        GHRepository r = u.getRepository("rails");
        // System.out.println(r);
        // System.out.println(r.getIssue(1));
    }

    @Test
    public void reactions() throws Exception {
        GHIssue i = gitHub.getRepository("github-api/github-api").getIssue(311);

        // retrieval
        GHReaction r = i.listReactions().iterator().next();
        assertThat(r.getUser().getLogin(), is("kohsuke"));
        assertThat(r.getContent(), is(ReactionContent.HEART));

        // CRUD
        GHReaction a = i.createReaction(ReactionContent.HOORAY);
        assertThat(a.getUser().getLogin(), is(gitHub.getMyself().getLogin()));
        a.delete();
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
        GHRepository r = gitHub.getRepository("github-api/github-api");
        String sha1 = "a12243f2fc5b8c2ba47dd677d0b0c7583539584d";

        assertBlobContent(r.readBlob(sha1));

        GHBlob blob = r.getBlob(sha1);
        assertBlobContent(blob.read());
        assertThat(blob.getSha(), is("a12243f2fc5b8c2ba47dd677d0b0c7583539584d"));
        assertThat(blob.getSize(), is(1104L));
    }

    private void assertBlobContent(InputStream is) throws Exception {
        String content = new String(IOUtils.toByteArray(is), "UTF-8");
        assertThat(content, containsString("Copyright (c) 2011- Kohsuke Kawaguchi and other contributors"));
        assertThat(content, containsString("FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR"));
        assertThat(content.length(), is(1104));
    }
}
