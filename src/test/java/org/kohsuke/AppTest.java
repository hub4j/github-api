package org.kohsuke;

import junit.framework.TestCase;
import org.kohsuke.github.GHEvent;
import org.kohsuke.github.GHEventInfo;
import org.kohsuke.github.GHEventPayload;
import org.kohsuke.github.GHHook;
import org.kohsuke.github.GHBranch;
import org.kohsuke.github.GHIssueState;
import org.kohsuke.github.GHOrganization;
import org.kohsuke.github.GHOrganization.Permission;
import org.kohsuke.github.GHRepository;
import org.kohsuke.github.GHTeam;
import org.kohsuke.github.GitHub;

import java.io.IOException;
import java.net.URL;
import java.util.Set;
import java.util.List;

/**
 * Unit test for simple App.
 */
public class AppTest extends TestCase {
    public void testCredentialValid() throws IOException {
        assertTrue(GitHub.connect().isCredentialValid());
        assertFalse(GitHub.connect("totally","bogus").isCredentialValid());
    }

    public void testFetchPullRequest() throws Exception {
        GitHub gh = GitHub.connect();
        GHRepository r = gh.getOrganization("jenkinsci").getRepository("jenkins");
        r.getPullRequest(1);
        r.getPullRequests(GHIssueState.OPEN);
    }

    public void tryOrgFork() throws Exception {
        GitHub gh = GitHub.connect();
        gh.getUser("kohsuke").getRepository("rubywm").forkTo(gh.getOrganization("jenkinsci"));
    }

    public void tryGetTeamsForRepo() throws Exception {
        GitHub gh = GitHub.connect();
        Set<GHTeam> o = gh.getOrganization("jenkinsci").getRepository("rubywm").getTeams();
        System.out.println(o);
    }

    public void testMembership() throws Exception {
        GitHub gitHub = GitHub.connect();
        Set<String> members = gitHub.getOrganization("jenkinsci").getRepository("violations-plugin").getCollaboratorNames();
        System.out.println(members.contains("kohsuke"));
    }
    
    public void testMemberOrgs() throws Exception {
        GitHub gitHub = GitHub.connect();
        Set<GHOrganization> o = gitHub.getUser("kohsuke").getOrganizations();
        System.out.println(o);
    }

    public void testBranches() throws Exception {
        GitHub gitHub = GitHub.connect();
        List<GHBranch> b = 
                gitHub.getUser("jenkinsci").getRepository("jenkins").getBranches();
        System.out.println(b);
    }
    
    public void tryHook() throws Exception {
        GitHub gitHub = GitHub.connect();
        GHRepository r = gitHub.getMyself().getRepository("test2");
        GHHook hook = r.createWebHook(new URL("http://www.google.com/"));
        System.out.println(hook);

        for (GHHook h : r.getHooks())
            h.delete();
    }
    
    public void testEventApi() throws Exception {
        GitHub gitHub = GitHub.connect();
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
        GitHub gitHub = GitHub.connect();
        System.out.println(gitHub.getMyself().getEmails());

//        GHRepository r = gitHub.connect().getOrganization("jenkinsci").createRepository("kktest4", "Kohsuke's test", "http://kohsuke.org/", "Everyone", true);
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

//        GHRepository r = GitHub.connect().getOrganization("HudsonLabs").createRepository("auto-test", "some description", "http://kohsuke.org/", "Plugin Developers", true);

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

    private void tryOrgFork(GitHub gitHub) throws IOException {
        GHOrganization o = gitHub.getOrganization("HudsonLabs");
        System.out.println(gitHub.getUser("rtyler").getRepository("memcache-ada").forkTo(o).getUrl());
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

    public void testOrganization() throws IOException {
        GitHub gitHub = GitHub.connect();
        GHOrganization j = gitHub.getOrganization("jenkinsci");
        GHTeam t = j.getTeams().get("Core Developers");

        assertNotNull(j.getRepository("jenkins"));

//        t.add(labs.getRepository("xyz"));
    }
}
