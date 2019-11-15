package org.kohsuke.github;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import org.junit.Test;

/**
 * @author Kevin Harrington mad.hephaestus@gmail.com
 */
public class Github2faTest extends AbstractGitHubWireMockTest {

    @Test
    public void test2faToken() throws IOException {
        assertFalse("Test only valid when not proxying", mockGitHub.isUseProxy());

        List<String> asList = Arrays
                .asList("repo", "gist", "write:packages", "read:packages", "delete:packages", "user", "delete_repo");
        String nameOfToken = "Test2faTokenCreate";// +timestamp;// use time stamp to ensure the token creations do not
                                                  // collide with older tokens

        GHAuthorization token = gitHub
                .createToken(asList, nameOfToken, "this is a test token created by a unit test", () -> {
                    String data = "111878";
                    // TO UPDATE run this in debugger mode, put a breakpoint here, and enter the OTP you get into the
                    // value of Data
                    return data;
                });
        assert token != null;
        for (int i = 0; i < asList.size(); i++) {
            assertTrue(token.getScopes().get(i).contentEquals(asList.get(i)));
        }

        String p = token.getToken();

        assert p != null;
    }
}
