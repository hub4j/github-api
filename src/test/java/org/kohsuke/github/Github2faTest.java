package org.kohsuke.github;

import org.junit.Test;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import static org.hamcrest.Matchers.*;

// TODO: Auto-generated Javadoc
/**
 * The Class Github2faTest.
 *
 * @author Kevin Harrington mad.hephaestus@gmail.com
 */
public class Github2faTest extends AbstractGitHubWireMockTest {

    /**
     * Test 2 fa token.
     *
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     */
    @Test
    public void test2faToken() throws IOException {
        assertThat("Test only valid when not proxying", mockGitHub.isUseProxy(), is(false));

        List<String> asList = Arrays
                .asList("repo", "gist", "write:packages", "read:packages", "delete:packages", "user", "delete_repo");
        String nameOfToken = "Test2faTokenCreate";// +timestamp;// use time stamp to ensure the token creations do not
                                                  // collide with older tokens

        GHAuthorization token = gitHub
                .createToken(asList, nameOfToken, "https://localhost/this/is/a/test/token", () -> {
                    String data = "111878";
                    // TO UPDATE run this in debugger mode, put a breakpoint here, and enter the OTP you get into the
                    // value of Data
                    return data;
                });

        assertThat(token, notNullValue());
        assertThat(token.getScopes(), contains(asList.toArray()));

        assertThat(token.getToken(), equalTo("63042a99d88bf138e6d6cf5788e0dc4e7a5d7309"));
        assertThat(token.getTokenLastEight(), equalTo("7a5d7309"));
        assertThat(token.getHashedToken(), equalTo("12b727a23cad7c5a5caabb806d88e722794dede98464aed7f77cbc00dbf031a2"));
        assertThat(token.getNote(), equalTo("Test2faTokenCreate"));
        assertThat(token.getNoteUrl().toString(), equalTo("https://localhost/this/is/a/test/token"));
        assertThat(token.getAppUrl().toString(), equalTo("https://localhost/this/is/a/test/app/token"));
        assertThat(token.getFingerprint(), nullValue());

    }
}
