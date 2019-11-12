package org.kohsuke.github;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import org.junit.Test;
public class Github2faTest  extends AbstractGitHubWireMockTest {
	public String twoFactorAuthCodePrompt() {
		System.out.print("Github 2 factor temp key: ");
		// create a scanner so we can read the command-line input
		BufferedReader buf = new BufferedReader(new InputStreamReader(System.in));
		// TODO Auto-generated method stub
		try {
			return buf.readLine().trim();
		} catch (IOException e) {
			return null;
		}
	}
	@Test
	public void test2faToken() throws IOException {
		assertFalse("Test only valid when not proxying", mockGitHub.isUseProxy());

		List<String> asList = Arrays.asList("repo", "gist", "write:packages", "read:packages", "delete:packages",
				"user", "delete_repo");
		String nameOfToken = "Test2faTokenCreate";//+timestamp;// use time stamp to ensure the token creations do not collide with older tokens

		GHAuthorization token=gitHub.createToken(
		        asList, 
		        nameOfToken,
		        "this is a test token created by a unit test", () -> {
		            // can be anything from automated processes to user interaction.
		        	String data = "535493";
		            return data; 
		        });
		assert token!=null;
		
		String p = token.getToken();
		
		assert p!=null;
	}
}
