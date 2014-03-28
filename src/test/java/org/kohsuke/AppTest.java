package org.kohsuke;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import org.junit.Assume;
import org.junit.Before;
import org.junit.Test;
import org.kohsuke.github.GHBranch;
import org.kohsuke.github.GHCommit;
import org.kohsuke.github.GHCommit.File;
import org.kohsuke.github.GHCommitComment;
import org.kohsuke.github.GHCommitState;
import org.kohsuke.github.GHCommitStatus;
import org.kohsuke.github.GHEvent;
import org.kohsuke.github.GHEventInfo;
import org.kohsuke.github.GHEventPayload;
import org.kohsuke.github.GHHook;
import org.kohsuke.github.GHIssue;
import org.kohsuke.github.GHIssueComment;
import org.kohsuke.github.GHIssueState;
import org.kohsuke.github.GHKey;
import org.kohsuke.github.GHMilestone;
import org.kohsuke.github.GHMyself;
import org.kohsuke.github.GHOrganization;
import org.kohsuke.github.GHOrganization.Permission;
import org.kohsuke.github.GHPullRequest;
import org.kohsuke.github.GHRepository;
import org.kohsuke.github.GHTeam;
import org.kohsuke.github.GHUser;
import org.kohsuke.github.GitHub;
import org.kohsuke.github.PagedIterable;

/**
 * Unit test for simple App.
 */
public class AppTest {

    private GitHub gitHub;

    @Before
    public void setUp() throws Exception {
        gitHub = GitHub.connect();
    }

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

    private GHUser getUser() {
        try {
            return gitHub.getMyself();
        } catch (IOException e) {
            throw new RuntimeException(e.getMessage(), e);
        }
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
        getUser().getRepository("rubywm").forkTo(gitHub.getOrganization("jenkinsci"));
    }

    @Test
    public void testGetTeamsForRepo() throws Exception {
        kohsuke();
        assertFalse(gitHub.getOrganization("jenkinsci").getRepository("rubywm").getTeams().isEmpty());
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
    public void testCommit() throws Exception {
        GHCommit commit = gitHub.getUser("jenkinsci").getRepository("jenkins").getCommit("08c1c9970af4d609ae754fbe803e06186e3206f7");
        System.out.println(commit);
        assertEquals(1, commit.getParents().size());
        assertEquals(1,commit.getFiles().size());

        File f = commit.getFiles().get(0);
        assertEquals(48,f.getLinesChanged());
        assertEquals("modified",f.getStatus());
        assertEquals("changelog.html", f.getFileName());
    }

    @Test
    public void testListCommits() throws Exception {
        List<String> sha1 = new ArrayList<String>();
        for (GHCommit c : gitHub.getUser("kohsuke").getRepository("empty-commit").listCommits()) {
            System.out.println(c.getSHA1());
            sha1.add(c.getSHA1());
        }
        assertEquals("fdfad6be4db6f96faea1f153fb447b479a7a9cb7",sha1.get(0));
        assertEquals(1,sha1.size());
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
        System.out.printf("%d repositories in %dms\n",repos.size(),end-start);
    }
    
    @Test
    public void testOrganization() throws IOException {
        kohsuke();
        GHOrganization j = gitHub.getOrganization("jenkinsci");
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
        assertEquals("http://kohsuke.org/",state.getTargetUrl());
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

    private void kohsuke() {
        Assume.assumeTrue(getUser().getLogin().equals("kohsuke"));
    }
}
