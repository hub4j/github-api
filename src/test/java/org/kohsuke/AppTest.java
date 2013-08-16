package org.kohsuke;

import junit.framework.TestCase;
import org.kohsuke.github.GHCommit;
import org.kohsuke.github.GHCommit.File;
import org.kohsuke.github.GHCommitComment;
import org.kohsuke.github.GHCommitStatus;
import org.kohsuke.github.GHEvent;
import org.kohsuke.github.GHEventInfo;
import org.kohsuke.github.GHEventPayload;
import org.kohsuke.github.GHHook;
import org.kohsuke.github.GHBranch;
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

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Map;
import java.util.Set;
import java.util.List;

/**
 * Unit test for simple App.
 */
public class AppTest extends TestCase {

    private GitHub gitHub;

    @Override
    public void setUp() throws Exception {
        super.setUp();
        gitHub = GitHub.connect();
    }

    public void testRepoCRUD() throws Exception {
        GHRepository r = gitHub.createRepository("github-api-test", "a test repository", "http://github-api.kohsuke.org/", true);
        r.enableIssueTracker(false);
        r.enableDownloads(false);
        r.enableWiki(false);
        r.renameTo("github-api-test2");
        gitHub.getMyself().getRepository("github-api-test2").delete();
    }

    public void testCredentialValid() throws IOException {
        assertTrue(gitHub.isCredentialValid());
        assertFalse(GitHub.connect("totally", "bogus").isCredentialValid());
    }

    public void testIssueWithNoComment() throws IOException {
        GHRepository repository = gitHub.getRepository("kohsuke/test");
        List<GHIssueComment> v = repository.getIssue(4).getComments();
        System.out.println(v);
        assertTrue(v.isEmpty());

        v = repository.getIssue(3).getComments();
        System.out.println(v);
        assertTrue(v.size() == 3);
    }

    public void testCreateIssue() throws IOException {
        GHUser u = gitHub.getUser("kohsuke");
        GHRepository r = u.getRepository("test");
        GHMilestone someMilestone = r.listMilestones(GHIssueState.CLOSED).iterator().next();
        GHIssue o = r.createIssue("testing").body("this is body").assignee(u).label("bug").label("question").milestone(someMilestone).create();
        System.out.println(o.getUrl());
        o.close();
    }

    public void testRateLimit() throws IOException {
        System.out.println(gitHub.getRateLimit());
    }

    public void testMyOrganizations() throws IOException {
        Map<String, GHOrganization> org = gitHub.getMyOrganizations();
        assertFalse(org.keySet().contains(null));
        System.out.println(org);
    }

    public void testFetchPullRequest() throws Exception {
        GHRepository r = gitHub.getOrganization("jenkinsci").getRepository("jenkins");
        assertEquals("master",r.getMasterBranch());
        r.getPullRequest(1);
        r.getPullRequests(GHIssueState.OPEN);
    }

    public void testFetchPullRequestAsList() throws Exception {
        GHRepository r = gitHub.getOrganization("symfony").getRepository("symfony-docs");
        assertEquals("master", r.getMasterBranch());
        PagedIterable<GHPullRequest> i = r.listPullRequests(GHIssueState.CLOSED);
        List<GHPullRequest> prs = i.asList();
        assertNotNull(prs);
        assertTrue(prs.size() > 0);
    }

    public void testRepoPermissions() throws Exception {
        GHRepository r = gitHub.getOrganization("jenkinsci").getRepository("jenkins");
        assertTrue(r.hasPullAccess());

        r = gitHub.getOrganization("github").getRepository("tire");
        assertFalse(r.hasAdminAccess());
    }
    
    public void testGetMyself() throws Exception {
        GHMyself me = gitHub.getMyself();
        System.out.println(me);
        GHUser u = gitHub.getUser("kohsuke2");
        System.out.println(u);
        for (List<GHRepository> lst : me.iterateRepositories(100)) {
            for (GHRepository r : lst) {
                System.out.println(r.getPushedAt());
            }
        }
    }

    public void testPublicKeys() throws Exception {
        List<GHKey> keys = gitHub.getMyself().getPublicKeys();
        System.out.println(keys);
    }

    public void tryOrgFork() throws Exception {
        gitHub.getUser("kohsuke").getRepository("rubywm").forkTo(gitHub.getOrganization("jenkinsci"));
    }

    public void tryGetTeamsForRepo() throws Exception {
        Set<GHTeam> o = gitHub.getOrganization("jenkinsci").getRepository("rubywm").getTeams();
        System.out.println(o);
    }

    public void testMembership() throws Exception {
        Set<String> members = gitHub.getOrganization("jenkinsci").getRepository("violations-plugin").getCollaboratorNames();
        System.out.println(members.contains("kohsuke"));
    }
    
    public void testMemberOrgs() throws Exception {
        Set<GHOrganization> o = gitHub.getUser("kohsuke").getOrganizations();
        System.out.println(o);
    }

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

    public void testListCommits() throws Exception {
        List<String> sha1 = new ArrayList<String>();
        for (GHCommit c : gitHub.getUser("kohsuke").getRepository("empty-commit").listCommits()) {
            System.out.println(c.getSHA1());
            sha1.add(c.getSHA1());
        }
        assertEquals("fdfad6be4db6f96faea1f153fb447b479a7a9cb7",sha1.get(0));
        assertEquals(1,sha1.size());
    }

    public void testBranches() throws Exception {
        Map<String,GHBranch> b =
                gitHub.getUser("jenkinsci").getRepository("jenkins").getBranches();
        System.out.println(b);
    }

    public void testCommitComment() throws Exception {
        GHRepository r = gitHub.getUser("jenkinsci").getRepository("jenkins");
        PagedIterable<GHCommitComment> comments = r.listCommitComments();
        List<GHCommitComment> batch = comments.iterator().nextPage();
        for (GHCommitComment comment : batch) {
            System.out.println(comment.getBody());
            assertSame(comment.getOwner(), r);
        }
    }

    public void testCreateCommitComment() throws Exception {
        GHCommit commit = gitHub.getUser("kohsuke").getRepository("sandbox-ant").getCommit("8ae38db0ea5837313ab5f39d43a6f73de3bd9000");
        GHCommitComment c = commit.createComment("[testing](http://kohsuse.org/)");
        System.out.println(c);
        c.update("updated text");
        System.out.println(c);
        c.delete();
    }

    public void tryHook() throws Exception {
        GHRepository r = gitHub.getMyself().getRepository("test2");
        GHHook hook = r.createWebHook(new URL("http://www.google.com/"));
        System.out.println(hook);

        for (GHHook h : r.getHooks())
            h.delete();
    }
    
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

    public void testOrgRepositories() throws IOException {
        GHOrganization j = gitHub.getOrganization("jenkinsci");
        long start = System.currentTimeMillis();
        Map<String, GHRepository> repos = j.getRepositories();
        long end = System.currentTimeMillis();
        System.out.printf("%d repositories in %dms\n",repos.size(),end-start);
    }
    
    public void testOrganization() throws IOException {
        GHOrganization j = gitHub.getOrganization("jenkinsci");
        GHTeam t = j.getTeams().get("Core Developers");

        assertNotNull(j.getRepository("jenkins"));

//        t.add(labs.getRepository("xyz"));
    }

    public void testCommitStatus() throws Exception {
        GHRepository r = gitHub.getUser("kohsuke").getRepository("test");
        GHCommitStatus state;
//        state = r.createCommitStatus("edacdd76b06c5f3f0697a22ca75803169f25f296", GHCommitState.FAILURE, "http://jenkins-ci.org/", "oops!");

        List<GHCommitStatus> lst = r.listCommitStatuses("edacdd76b06c5f3f0697a22ca75803169f25f296").asList();
        state = lst.get(0);
        System.out.println(state);
        assertEquals("oops!",state.getDescription());
        assertEquals("http://jenkins-ci.org/",state.getTargetUrl());
    }
    
    public void testCommitShortInfo() throws Exception {
        GHCommit commit = gitHub.getUser("kohsuke").getRepository("test").getCommit("c77360d6f2ff2c2e6dd11828ad5dccf72419fa1b");
        assertEquals(commit.getCommitShortInfo().getAuthor().getName(), "Kohsuke Kawaguchi");
        assertEquals(commit.getCommitShortInfo().getMessage(), "Added a file");
    }

    public void testPullRequestPopulate() throws Exception {
        GHRepository r = gitHub.getUser("kohsuke").getRepository("github-api");
        GHPullRequest p = r.getPullRequest(17);
        GHUser u = p.getUser();
        assertNotNull(u.getName());
    }

    public void testCheckMembership() throws Exception {
        GHOrganization j = gitHub.getOrganization("jenkinsci");
        GHUser kohsuke = gitHub.getUser("kohsuke");
        GHUser a = gitHub.getUser("a");

        assertTrue(j.hasMember(kohsuke));
        assertFalse(j.hasMember(a));

        assertTrue(j.hasPublicMember(kohsuke));
        assertFalse(j.hasPublicMember(a));
    }
}
