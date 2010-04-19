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
        GitHub hub = new GitHub("kohsuke","9138245daf860415dfc66419177d95da");
//        hub.createRepository("test","test repository",null,true);
//        hub.getUser("kohsuke").getRepository("test").delete();

        System.out.println(hub.getUser("kohsuke").getRepository("hudson").getCollaborators());
    }
}
