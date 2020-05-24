package org.kohsuke.github;

import org.junit.Test;

import java.io.IOException;
import java.net.URL;
import java.util.Arrays;
import java.util.List;

import javax.swing.JFrame;
import javax.swing.JOptionPane;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * @author Kevin Harrington mad.hephaestus@gmail.com
 */
public class GithubWebFlowOAuthTest extends AbstractGitHubWireMockTest {

    @Test
    public void testWebFlowToken() throws Exception {
        assertFalse("Test only valid when not proxying", mockGitHub.isUseProxy());

        List<String> asList = Arrays
                .asList("repo", "gist", "write:packages", "read:packages", "delete:packages", "user", "delete_repo");
        String secret = "";
        String publicAPI = "1edf79fae494c232d4d2";
        
        JFrame jframe = new JFrame();
		jframe.setAlwaysOnTop(true);
		secret = JOptionPane.showInputDialog(jframe, "Secret API key");
		jframe.dispose();
        GHAuthorization token = gitHub
                .createOAuthTokenWebFlow(asList,// List of scopes requested
                		true,// true allows user signups in flow
                		"madhephaestus", // username of the github user desired. this can be null to allow the user to decide
                		publicAPI,// the public key of the API
                		secret,// the secret which must never be committed in source 
                		new URL("http://commonwealthrobotics.com")// where to go on success
                		);
        assert token != null;
        for (int i = 0; i < asList.size(); i++) {
            assertTrue(token.getScopes().get(i).contentEquals(asList.get(i)));
        }

        String p = token.getToken();

        assert p != null;
    }
}
