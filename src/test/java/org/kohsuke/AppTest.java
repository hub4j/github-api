package org.kohsuke;

import junit.framework.TestCase;
import org.kohsuke.github.GHOrganization;
import org.kohsuke.github.GHRepository;
import org.kohsuke.github.GHTeam;
import org.kohsuke.github.GitHub;

import java.io.IOException;
import java.net.URL;
import java.util.Set;

/**
 * Unit test for simple App.
 */
public class AppTest extends TestCase {
    public void testCredentialValid() throws IOException {
        assertTrue(GitHub.connect().isCredentialValid());
        assertFalse(GitHub.connect("totally","bogus").isCredentialValid());
    }

    public void testApp() throws IOException {
        GitHub gitHub = GitHub.connect();
//        testOrganization(gitHub);

        testPostCommitHook(gitHub);

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

    private void testPostCommitHook(GitHub gitHub) throws IOException {
        GHRepository r = gitHub.getMyself().getRepository("foo");
        Set<URL> hooks = r.getPostCommitHooks();
        hooks.add(new URL("http://kohsuke.org/test"));
        System.out.println(hooks);
        hooks.remove(new URL("http://kohsuke.org/test"));
        System.out.println(hooks);
    }

    private void testOrganization(GitHub gitHub) throws IOException {
        GHOrganization labs = gitHub.getOrganization("HudsonLabs");
        GHTeam t = labs.getTeams().get("Core Developers");

        t.add(labs.getRepository("xyz"));
    }
}
