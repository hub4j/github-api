package org.kohsuke;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import org.kohsuke.github.GitHub;

import java.io.IOException;

/**
 * Unit test for simple App.
 */
public class AppTest extends TestCase {
    public void testApp() throws IOException {
        GitHub hub = GitHub.connectAnonymously();
//        hub.createRepository("test","test repository",null,true);
//        hub.getUser("kohsuke").getRepository("test").delete();

        System.out.println(hub.getUser("kohsuke").getRepository("hudson").getCollaborators());
    }
}
