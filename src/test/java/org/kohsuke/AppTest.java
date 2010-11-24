package org.kohsuke;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import org.kohsuke.github.GHRepository;
import org.kohsuke.github.GHTeam;
import org.kohsuke.github.GitHub;

import java.io.IOException;

/**
 * Unit test for simple App.
 */
public class AppTest extends TestCase {
    public void testApp() throws IOException {
        GitHub gitHub = GitHub.connect();
        GHTeam t = gitHub.getOrganization("HudsonLabs").getTeams().get("Core Developers");
        t.add(gitHub.getMyself());
        System.out.println(t.getMembers());
        t.remove(gitHub.getMyself());
        System.out.println(t.getMembers());

//        GHRepository r = GitHub.connect().getOrganization("HudsonLabs").createRepository("auto-test", "some description", "http://kohsuke.org/", "Plugin Developers", true);

//        r.
//        GitHub hub = GitHub.connectAnonymously();
////        hub.createRepository("test","test repository",null,true);
////        hub.getUser("kohsuke").getRepository("test").delete();
//
//        System.out.println(hub.getUser("kohsuke").getRepository("hudson").getCollaborators());
    }
}
