package org.kohsuke.github;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import org.junit.Test;
public class Github2faTest  extends AbstractGitHubWireMockTest {

	@Test
	public void test2faToken() throws IOException {
		//assertFalse("Test only valid when not proxying", mockGitHub.isUseProxy());

		List<String> asList = Arrays.asList("repo", "gist", "write:packages", "read:packages", "delete:packages",
				"user", "delete_repo");
		String nameOfToken = "Test2faTokenCreate";//+timestamp;// use time stamp to ensure the token creations do not collide with older tokens

		GHAuthorization token=gitHub.createToken(
		        asList, 
		        nameOfToken,
		        "this is a test token created by a unit test", () -> {
		            // can be anything from automated processes to user interaction.
		        	String data = "111878";
		            return data; 
		        });
		assert token!=null;
		
		String p = token.getToken();
		
		assert p!=null;
	}
}
