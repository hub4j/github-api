package org.kohsuke.github;

import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;

import org.apache.commons.io.IOUtils;
import org.hamcrest.CoreMatchers;
import org.junit.Assume;
import org.junit.Test;
import org.kohsuke.github.GHCommit.File;
import org.kohsuke.github.GHOrganization.Permission;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.*;
import java.util.Map.Entry;
import java.util.concurrent.ExecutionException;
import java.util.regex.Pattern;

import static org.hamcrest.CoreMatchers.*;

/**
 * Unit test for simple App.
 */
public class AppTest extends AbstractGitHubApiTestBase {
    private String getTestRepositoryName() throws IOException {
        return getUser().getLogin() + "/github-api-test";
    }

    @Test
    public void testRepoCRUD() throws Exception {
        String targetName = "github-api-test-rename2";

        deleteRepository("github-api-test-rename");
        deleteRepository(targetName);
        GHRepository r = gitHub.createRepository("github-api-test-rename", "a test repository", "http://github-api.kohsuke.org/", true);
        r.enableIssueTracker(false);
        r.enableDownloads(false);
        r.enableWiki(false);
        r.renameTo(targetName);
        getUser().getRepository(targetName).delete();
    }

    @Test
    public void testRepositoryWithAutoInitializationCRUD() throws Exception {
        String name = "github-api-test-autoinit";
        deleteRepository(name);
        GHRepository r = gitHub.createRepository(name)
                .description("a test repository for auto init")
                .homepage("http://github-api.kohsuke.org/")
                .autoInit(true).create();
        r.enableIssueTracker(false);
        r.enableDownloads(false);
        r.enableWiki(false);
        Thread.sleep(3000);
        assertNotNull(r.getReadme());
        getUser().getRepository(name).delete();
    }

    private void deleteRepository(final String name) throws IOException {
        GHRepository repository = getUser().getRepository(name);
        if(repository != null) {
            repository.delete();
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
        System.out.println(v);
        assertTrue(v.isEmpty());

        v = repository.getIssue(3).getComments();
        System.out.println(v);
        assertTrue(v.size() == 3);
    }

    @Test
    public void testCreateIssue() throws IOException {
        GHUser u = getUser();
        GHRepository repository = getTestRepository();
        GHMilestone milestone = repository.createMilestone(System.currentTimeMillis() + "", "Test Milestone");
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
    public void testCreateDeployment() throws IOException {
        GHRepository repository = getTestRepository();
        GHDeployment deployment = repository.createDeployment("master")
                .payload("{\"user\":\"atmos\",\"room_id\":123456}")
                .description("question")
                .create();
        assertNotNull(deployment.getCreator());
        assertNotNull(deployment.getId());
    }

    @Test
    public void testListDeployments() throws IOException {
        GHRepository repository = getTestRepository();
        GHDeployment deployment = repository.createDeployment("master")
                .payload("{\"user\":\"atmos\",\"room_id\":123456}")
                .description("question")
                .environment("unittest")
                .create();
        assertNotNull(deployment.getCreator());
        assertNotNull(deployment.getId());
        ArrayList<GHDeployment> deployments = Lists.newArrayList(repository.listDeployments(null, "master", null, "unittest"));
        assertNotNull(deployments);
        assertFalse(Iterables.isEmpty(deployments));
        GHDeployment unitTestDeployment = deployments.get(0);
        assertEquals("unittest",unitTestDeployment.getEnvironment());
        assertEquals("master", unitTestDeployment.getRef());
    }

    @Test
    public void testGetDeploymentStatuses() throws IOException {
        GHRepository repository = getTestRepository();
        GHDeployment deployment = repository.createDeployment("master")
                .description("question")
                .payload("{\"user\":\"atmos\",\"room_id\":123456}")
                .create();
       GHDeploymentStatus ghDeploymentStatus = repository.createDeployStatus(deployment.getId(), GHDeploymentState.SUCCESS)
                                     .description("success")
                                     .targetUrl("http://www.github.com").create();
        Iterable<GHDeploymentStatus> deploymentStatuses = repository.getDeploymentStatuses(deployment.getId());
        assertNotNull(deploymentStatuses);
        assertEquals(1,Iterables.size(deploymentStatuses));
        assertEquals(ghDeploymentStatus.getId(), Iterables.get(deploymentStatuses, 0).getId());
    }

    @Test
    public void testGetIssues() throws Exception {
        List<GHIssue> closedIssues = gitHub.getUser("kohsuke").getRepository("github-api").getIssues(GHIssueState.CLOSED);
        // prior to using PagedIterable GHRepository.getIssues(GHIssueState) would only retrieve 30 issues
        assertTrue(closedIssues.size() > 30);
    }


    private GHRepository getTestRepository() throws IOException {
        GHRepository repository;
        try {
            repository = gitHub.getRepository(getTestRepositoryName());
        } catch (IOException e) {
            repository = gitHub.createRepository("github-api-test", "A test repository for testing" +
                    "the github-api project", "http://github-api.kohsuke.org/", true);
            try {
                Thread.sleep(2000);
            } catch (InterruptedException e1) {
                throw new RuntimeException(e.getMessage(), e);
            }
            repository.enableIssueTracker(true);
            repository.enableDownloads(true);
            repository.enableWiki(true);
        }
        return repository;
    }

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
        System.out.println(gitHub.getRateLimit());
    }

    @Test
    public void testMyOrganizations() throws IOException {
        Map<String, GHOrganization> org = gitHub.getMyOrganizations();
        assertFalse(org.keySet().contains(null));
        System.out.println(org);
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

    private boolean shouldBelongToTeam(String organizationName, String teamName) throws IOException {
        GHOrganization org = gitHub.getOrganization(organizationName);
        assertNotNull(org);
        GHTeam team = org.getTeamByName(teamName);
        assertNotNull(team);
        return team.hasMember(gitHub.getMyself());
    }

    @Test
    public void testFetchPullRequest() throws Exception {
        GHRepository r = gitHub.getOrganization("jenkinsci").getRepository("jenkins");
        assertEquals("master",r.getMasterBranch());
        r.getPullRequest(1);
        r.getPullRequests(GHIssueState.OPEN);
    }

    @Test
    public void testFetchPullRequestAsList() throws Exception {
        GHRepository r = gitHub.getRepository("kohsuke/github-api");
        assertEquals("master", r.getMasterBranch());
        PagedIterable<GHPullRequest> i = r.listPullRequests(GHIssueState.CLOSED);
        List<GHPullRequest> prs = i.asList();
        assertNotNull(prs);
        assertTrue(prs.size() > 0);
    }

    @Test
    public void testRepoPermissions() throws Exception {
        kohsuke();
        GHRepository r = gitHub.getOrganization("jenkinsci").getRepository("jenkins");
        assertTrue(r.hasPullAccess());

        r = gitHub.getOrganization("github").getRepository("hub");
        assertFalse(r.hasAdminAccess());
    }
    
    @Test
    public void testGetMyself() throws Exception {
        GHMyself me = gitHub.getMyself();
        assertNotNull(me);
        assertNotNull(gitHub.getUser("kohsuke2"));
        PagedIterable<GHRepository> ghRepositories = me.listRepositories();
        assertTrue(ghRepositories.iterator().hasNext());
    }

    @Test
    public void testPublicKeys() throws Exception {
        List<GHKey> keys = gitHub.getMyself().getPublicKeys();
        assertFalse(keys.isEmpty());
    }

    @Test
    public void testOrgFork() throws Exception {
        kohsuke();
        gitHub.getRepository("kohsuke/rubywm").forkTo(gitHub.getOrganization("github-api-test-org"));
    }

    @Test
    public void testGetTeamsForRepo() throws Exception {
        kohsuke();
        // 'Core Developers' and 'Owners'
        assertEquals(2, gitHub.getOrganization("github-api-test-org").getRepository("testGetTeamsForRepo").getTeams().size());
    }

    @Test
    public void testMembership() throws Exception {
        Set<String> members = gitHub.getOrganization("jenkinsci").getRepository("violations-plugin").getCollaboratorNames();
        System.out.println(members.contains("kohsuke"));
    }

    @Test
    public void testMemberOrgs() throws Exception {
        Set<GHOrganization> o = gitHub.getUser("kohsuke").getOrganizations();
        System.out.println(o);
    }

    @Test
    public void testOrgTeams() throws Exception {
        kohsuke();
        int sz=0;
        for (GHTeam t : gitHub.getOrganization("github-api-test-org").listTeams()) {
            assertNotNull(t.getName());
            sz++;
        }
        assertTrue(sz < 100);
    }

    @Test
    public void testOrgTeamByName() throws Exception {
        kohsuke();
        GHTeam e = gitHub.getOrganization("github-api-test-org").getTeamByName("Core Developers");
        assertNotNull(e);
    }

    @Test
    public void testOrgTeamBySlug() throws Exception {
        kohsuke();
        GHTeam e = gitHub.getOrganization("github-api-test-org").getTeamBySlug("core-developers");
        assertNotNull(e);
    }

    @Test
    public void testCommit() throws Exception {
        GHCommit commit = gitHub.getUser("jenkinsci").getRepository("jenkins").getCommit("08c1c9970af4d609ae754fbe803e06186e3206f7");
        System.out.println(commit);
        assertEquals(1, commit.getParents().size());
        assertEquals(1,commit.getFiles().size());
        assertEquals("https://github.com/jenkinsci/jenkins/commit/08c1c9970af4d609ae754fbe803e06186e3206f7",
                commit.getHtmlUrl().toString());

        File f = commit.getFiles().get(0);
        assertEquals(48,f.getLinesChanged());
        assertEquals("modified",f.getStatus());
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
            System.out.println(c.getSHA1());
            sha1.add(c.getSHA1());
        }
        assertEquals("fdfad6be4db6f96faea1f153fb447b479a7a9cb7", sha1.get(0));
        assertEquals(1, sha1.size());
    }

    public void testQueryCommits() throws Exception {
        List<String> sha1 = new ArrayList<String>();
        for (GHCommit c : gitHub.getUser("jenkinsci").getRepository("jenkins").queryCommits()
                .since(new Date(1199174400000L)).until(1201852800000L).path("pom.xml").list()) {
            System.out.println(c.getSHA1());
            sha1.add(c.getSHA1());
        }
        assertEquals("1cccddb22e305397151b2b7b87b4b47d74ca337b",sha1.get(0));
        assertEquals(29, sha1.size());
    }

    @Test
    public void testBranches() throws Exception {
        Map<String,GHBranch> b =
                gitHub.getUser("jenkinsci").getRepository("jenkins").getBranches();
        System.out.println(b);
    }

    @Test
    public void testCommitComment() throws Exception {
        GHRepository r = gitHub.getUser("jenkinsci").getRepository("jenkins");
        PagedIterable<GHCommitComment> comments = r.listCommitComments();
        List<GHCommitComment> batch = comments.iterator().nextPage();
        for (GHCommitComment comment : batch) {
            System.out.println(comment.getBody());
            assertSame(comment.getOwner(), r);
        }
    }

    @Test
    public void testCreateCommitComment() throws Exception {
        GHCommit commit = gitHub.getUser("kohsuke").getRepository("sandbox-ant").getCommit("8ae38db0ea5837313ab5f39d43a6f73de3bd9000");
        GHCommitComment c = commit.createComment("[testing](http://kohsuse.org/)");
        System.out.println(c);
        c.update("updated text");
        System.out.println(c);
        c.delete();
    }

    @Test
    public void tryHook() throws Exception {
        kohsuke();
        GHRepository r = gitHub.getMyself().getRepository("test2");
        GHHook hook = r.createWebHook(new URL("http://www.google.com/"));
        System.out.println(hook);

        for (GHHook h : r.getHooks())
            h.delete();
    }
    
    @Test
    public void testEventApi() throws Exception {
        for (GHEventInfo ev : gitHub.getEvents()) {
            System.out.println(ev);
            if (ev.getType()==GHEvent.PULL_REQUEST) {
                GHEventPayload.PullRequest pr = ev.getPayload(GHEventPayload.PullRequest.class);
                System.out.println(pr.getNumber());
                System.out.println(pr.getPullRequest());
            }
        }
    }

    @Test
    public void testApp() throws IOException {
        System.out.println(gitHub.getMyself().getEmails());

//        GHRepository r = gitHub.getOrganization("jenkinsci").createRepository("kktest4", "Kohsuke's test", "http://kohsuke.org/", "Everyone", true);
//        r.fork();

//        tryDisablingIssueTrackers(gitHub);

//        tryDisablingWiki(gitHub);

//        GHPullRequest i = gitHub.getOrganization("jenkinsci").getRepository("sandbox").getPullRequest(1);
//        for (GHIssueComment c : i.getComments())
//            System.out.println(c);
//        System.out.println(i);

//        gitHub.getMyself().getRepository("perforce-plugin").setEmailServiceHook("kk@kohsuke.org");

//        tryRenaming(gitHub);
//        tryOrgFork(gitHub);

//        testOrganization(gitHub);
//        testPostCommitHook(gitHub);

//        tryTeamCreation(gitHub);

//        t.add(gitHub.getMyself());
//        System.out.println(t.getMembers());
//        t.remove(gitHub.getMyself());
//        System.out.println(t.getMembers());

//        GHRepository r = gitHub.getOrganization("HudsonLabs").createRepository("auto-test", "some description", "http://kohsuke.org/", "Plugin Developers", true);

//        r.
//        GitHub hub = GitHub.connectAnonymously();
////        hub.createRepository("test","test repository",null,true);
////        hub.getUser("kohsuke").getRepository("test").delete();
//
//        System.out.println(hub.getUser("kohsuke").getRepository("hudson").getCollaborators());
    }

    private void tryDisablingIssueTrackers(GitHub gitHub) throws IOException {
        for (GHRepository r : gitHub.getOrganization("jenkinsci").getRepositories().values()) {
            if (r.hasIssues()) {
                if (r.getOpenIssueCount()==0) {
                    System.out.println("DISABLED  "+r.getName());
                    r.enableIssueTracker(false);
                } else {
                    System.out.println("UNTOUCHED "+r.getName());
                }
            }
        }
    }

    private void tryDisablingWiki(GitHub gitHub) throws IOException {
        for (GHRepository r : gitHub.getOrganization("jenkinsci").getRepositories().values()) {
            if (r.hasWiki()) {
                System.out.println("DISABLED  "+r.getName());
                r.enableWiki(false);
            }
        }
    }

    private void tryUpdatingIssueTracker(GitHub gitHub) throws IOException {
        GHRepository r = gitHub.getOrganization("jenkinsci").getRepository("lib-task-reactor");
        System.out.println(r.hasIssues());
        System.out.println(r.getOpenIssueCount());
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
        System.out.println(hooks);
        hooks.remove(new URL("http://kohsuke.org/test"));
        System.out.println(hooks);
    }

    @Test
    public void testOrgRepositories() throws IOException {
        kohsuke();
        GHOrganization j = gitHub.getOrganization("jenkinsci");
        long start = System.currentTimeMillis();
        Map<String, GHRepository> repos = j.getRepositories();
        long end = System.currentTimeMillis();
        System.out.printf("%d repositories in %dms\n", repos.size(), end - start);
    }
    
    @Test
    public void testOrganization() throws IOException {
        kohsuke();
        GHOrganization j = gitHub.getOrganization("github-api-test-org");
        GHTeam t = j.getTeams().get("Core Developers");

        assertNotNull(j.getRepository("jenkins"));

//        t.add(labs.getRepository("xyz"));
    }

    @Test
    public void testCommitStatus() throws Exception {
        GHRepository r = gitHub.getRepository("kohsuke/github-api");

        GHCommitStatus state;

//        state = r.createCommitStatus("ecbfdd7315ef2cf04b2be7f11a072ce0bd00c396", GHCommitState.FAILURE, "http://kohsuke.org/", "testing!");

        List<GHCommitStatus> lst = r.listCommitStatuses("ecbfdd7315ef2cf04b2be7f11a072ce0bd00c396").asList();
        state = lst.get(0);
        System.out.println(state);
        assertEquals("testing!",state.getDescription());
        assertEquals("http://kohsuke.org/", state.getTargetUrl());
    }
    
    @Test
    public void testCommitShortInfo() throws Exception {
        GHRepository r = gitHub.getRepository("kohsuke/github-api");
        GHCommit commit = r.getCommit("86a2e245aa6d71d54923655066049d9e21a15f23");
        assertEquals(commit.getCommitShortInfo().getAuthor().getName(), "Kohsuke Kawaguchi");
        assertEquals(commit.getCommitShortInfo().getMessage(), "doc");
    }

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
                    GHRef ref = r.createRef("refs/heads/"+releaseName, ash);
                    assertEquals(ref.getRef(),"refs/heads/"+releaseName);

                    for (Map.Entry<String, GHBranch> entry : r.getBranches().entrySet()) {
                        System.out.println(entry.getKey() + "/" + entry.getValue());
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
        assertEquals("https://api.github.com/repos/jenkinsci/jenkins/git/refs/heads/master", masterRef.getUrl().toString());
    }

    @Test
    public void directoryListing() throws IOException {
        List<GHContent> children = gitHub.getRepository("jenkinsci/jenkins").getDirectoryContent("core");
        for (GHContent c : children) {
            System.out.println(c.getName());
            if (c.isDirectory()) {
                for (GHContent d : c.listDirectoryContent()) {
                    System.out.println("  "+d.getName());
                }
            }
        }
    }
    
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
    
    @Test
    public void testCommitStatusContext() throws IOException {
        GHRepository myRepository = getTestRepository();
        GHRef masterRef = myRepository.getRef("heads/master");
        GHCommitStatus commitStatus = myRepository.createCommitStatus(masterRef.getObject().getSha(), GHCommitState.SUCCESS, "http://www.example.com", "test", "test/context");
        assertEquals("test/context", commitStatus.getContext());
         
    }

    @Test
    public void testMemberPagenation() throws IOException {
        Set<GHUser> all = new HashSet<GHUser>();
        for (GHUser u : gitHub.getOrganization("github-api-test-org").getTeamByName("Core Developers").listMembers()) {
            System.out.println(u.getLogin());
            all.add(u);
        }
        assertFalse(all.isEmpty());
    }

    @Test
    public void testCommitSearch() throws IOException {
        PagedSearchIterable<GHCommit> r = gitHub.searchCommits().author("kohsuke").list();
        assertTrue(r.getTotalCount() > 0);
    }

    @Test
    public void testIssueSearch() throws IOException {
        PagedSearchIterable<GHIssue> r = gitHub.searchIssues().mentions("kohsuke").isOpen().list();
        for (GHIssue i : r) {
            System.out.println(i.getTitle());
        }
    }

    @Test   // issue #99
    public void testReadme() throws IOException {
        GHContent readme = gitHub.getRepository("github-api-test-org/test-readme").getReadme();
        assertEquals(readme.getName(),"README.md");
        assertEquals(readme.getContent(),"This is a markdown readme.\n");
    }
    
    
    @Test
    public void testTrees() throws IOException {
        GHTree masterTree = gitHub.getRepository("kohsuke/github-api").getTree("master");
        boolean foundReadme = false;
        for(GHTreeEntry e : masterTree.getTree()){
            if("readme".equalsIgnoreCase(e.getPath().replaceAll("\\.md", ""))){
                foundReadme = true;
                break;
            }
        }
        assertTrue(foundReadme);
    }
    
    @Test
    public void testTreesRecursive() throws IOException {
        GHTree masterTree = gitHub.getRepository("kohsuke/github-api").getTreeRecursive("master", 1);
        boolean foundThisFile = false;
        for(GHTreeEntry e : masterTree.getTree()){ 
            if(e.getPath().endsWith(AppTest.class.getSimpleName() + ".java")){
                foundThisFile = true;
                break;
            }
        }
        assertTrue(foundThisFile);
    }

    @Test
    public void testRepoLabel() throws IOException {
        GHRepository r = gitHub.getRepository("github-api-test-org/test-labels");
        List<GHLabel> lst = r.listLabels().asList();
        for (GHLabel l : lst) {
            System.out.println(l.getName());
        }
        assertTrue(lst.size() > 5);
        GHLabel e = r.getLabel("enhancement");
        assertEquals("enhancement",e.getName());
        assertNotNull(e.getUrl());
        assertTrue(Pattern.matches("[0-9a-fA-F]{6}",e.getColor()));

        {// CRUD
            GHLabel t = r.createLabel("test", "123456");
            GHLabel t2 = r.getLabel("test");
            assertEquals(t.getName(), t2.getName());
            assertEquals(t.getColor(), "123456");
            assertEquals(t.getColor(), t2.getColor());
            assertEquals(t.getUrl(), t2.getUrl());
            t.delete();
        }
    }

    @Test
    public void testSubscribers() throws IOException {
        boolean kohsuke = false;
        GHRepository mr = gitHub.getRepository("kohsuke/github-api");
        for (GHUser u : mr.listSubscribers()) {
            System.out.println(u.getLogin());
            kohsuke |= u.getLogin().equals("kohsuke");
        }
        assertTrue(kohsuke);
        System.out.println("---");

        boolean githubApi = false;
        for (GHRepository r : gitHub.getUser("kohsuke").listRepositories()) {
            System.out.println(r.getName());
            githubApi |= r.equals(mr);
        }
        assertTrue(githubApi);
    }

    @Test
    public void testListAllRepositories() throws Exception {
        Iterator<GHRepository> itr = gitHub.listAllPublicRepositories().iterator();
        for (int i=0; i<30; i++) {
            assertTrue(itr.hasNext());
            GHRepository r = itr.next();
            System.out.println(r.getFullName());
            assertNotNull(r.getUrl());
            assertNotEquals(0,r.getId());
        }
    }

    @Test // issue #162
    public void testIssue162() throws Exception {
        GHRepository r = gitHub.getRepository("kohsuke/github-api");
        List<GHContent> contents = r.getDirectoryContent("", "gh-pages");
        for (GHContent content : contents) {
            if (content.isFile()) {
                String content1 = content.getContent();
                String content2 = r.getFileContent(content.getPath(), "gh-pages").getContent();
                System.out.println(content.getPath());
                assertEquals(content1, content2);
            }
        }
    }

    @Test
    public void markDown() throws Exception {
        assertEquals("<p><strong>Test日本語</strong></p>", IOUtils.toString(gitHub.renderMarkdown("**Test日本語**")).trim());

        String actual = IOUtils.toString(gitHub.getRepository("kohsuke/github-api").renderMarkdown("@kohsuke to fix issue #1", MarkdownMode.GFM));
        System.out.println(actual);
        assertTrue(actual.contains("href=\"https://github.com/kohsuke\""));
        assertTrue(actual.contains("href=\"https://github.com/kohsuke/github-api/pull/1\""));
        assertTrue(actual.contains("class=\"user-mention\""));
        assertTrue(actual.contains("class=\"issue-link "));
        assertTrue(actual.contains("to fix issue"));
    }

    @Test
    public void searchUsers() throws Exception {
        PagedSearchIterable<GHUser> r = gitHub.searchUsers().q("tom").repos(">42").followers(">1000").list();
        GHUser u = r.iterator().next();
        System.out.println(u.getName());
        assertNotNull(u.getId());
        assertTrue(r.getTotalCount() > 0);
    }

    @Test
    public void searchRepositories() throws Exception {
        PagedSearchIterable<GHRepository> r = gitHub.searchRepositories().q("tetris").language("assembly").sort(GHRepositorySearchBuilder.Sort.STARS).list();
        GHRepository u = r.iterator().next();
        System.out.println(u.getName());
        assertNotNull(u.getId());
        assertEquals("Assembly", u.getLanguage());
        assertTrue(r.getTotalCount() > 0);
    }

    @Test
    public void searchContent() throws Exception {
        PagedSearchIterable<GHContent> r = gitHub.searchContent().q("addClass").in("file").language("js").repo("jquery/jquery").list();
        GHContent c = r.iterator().next();
        System.out.println(c.getName());
        assertNotNull(c.getDownloadUrl());
        assertNotNull(c.getOwner());
        assertEquals("jquery/jquery",c.getOwner().getFullName());
        assertTrue(r.getTotalCount() > 0);
    }

    @Test
    public void notifications() throws Exception {
        boolean found=false;
        for (GHThread t : gitHub.listNotifications().nonBlocking(true).read(true)) {
            if (!found) {
                found = true;
                t.markAsRead(); // test this by calling it once on old nofication
            }
            assertNotNull(t.getTitle());
            assertNotNull(t.getReason());

            System.out.println(t.getTitle());
            System.out.println(t.getLastReadAt());
            System.out.println(t.getType());
            System.out.println();
        }
        assertTrue(found);
        gitHub.listNotifications().markAsRead();
    }

    /**
     * Just basic code coverage to make sure toString() doesn't blow up
     */
    @Test
    public void checkToString() throws Exception {
        GHUser u = gitHub.getUser("rails");
        System.out.println(u);
        GHRepository r = u.getRepository("rails");
        System.out.println(r);
        System.out.println(r.getIssue(1));
    }

    @Test
    public void reactions() throws Exception {
        GHIssue i = gitHub.getRepository("kohsuke/github-api").getIssue(311);

        // retrieval
        GHReaction r = i.listReactions().iterator().next();
        assertThat(r.getUser().getLogin(), is("kohsuke"));
        assertThat(r.getContent(),is(ReactionContent.HEART));

        // CRUD
        GHReaction a = i.createReaction(ReactionContent.HOORAY);
        assertThat(a.getUser().getLogin(),is(gitHub.getMyself().getLogin()));
        a.delete();
    }

    @Test
    public void listOrgMemberships() throws Exception {
        GHMyself me = gitHub.getMyself();
        for (GHMembership m : me.listOrgMemberships()) {
            assertThat(m.getUser(), is((GHUser)me));
            assertNotNull(m.getState());
            assertNotNull(m.getRole());

            System.out.printf("%s %s %s\n",
                    m.getOrganization().getLogin(),
                    m.getState(),
                    m.getRole());
        }
    }

    @Test
    public void blob() throws Exception {
        GHRepository r = gitHub.getRepository("kohsuke/github-api");
        String sha1 = "a12243f2fc5b8c2ba47dd677d0b0c7583539584d";

        assertBlobContent(r.readBlob(sha1));

        GHBlob blob = r.getBlob(sha1);
        assertBlobContent(blob.read());
        assertThat(blob.getSha(),is("a12243f2fc5b8c2ba47dd677d0b0c7583539584d"));
        assertThat(blob.getSize(),is(1104L));
    }

    private void assertBlobContent(InputStream is) throws Exception {
        String content = new String(IOUtils.toByteArray(is),"UTF-8");
        assertThat(content,containsString("Copyright (c) 2011- Kohsuke Kawaguchi and other contributors"));
        assertThat(content,containsString("FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR"));
        assertThat(content.length(),is(1104));
    }
}
