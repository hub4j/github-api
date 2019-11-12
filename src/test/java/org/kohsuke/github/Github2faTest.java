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
		//assertFalse("Test only valid when not proxying", mockGitHub.isUseProxy());
		
		DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss");
		String timestamp = dateFormat.format(new Date());
		List<String> asList = Arrays.asList("repo", "gist", "write:packages", "read:packages", "delete:packages",
				"user", "delete_repo");
		String string = "Test2faTokenCreate-"+timestamp;// + timestamp;// use time stamp to ensure the token creations do not collide with older tokens

		GHAuthorization token=null;
		try {
			token = gitHub.createToken(asList, string, "this is a test token created by a unit test");
		}catch (IOException ex) {
			//ex.printStackTrace();
			// under 2fa mode this exception is expected, and is necessary
			// as the exception is called, GitHub will generate and send an OTP to the users SMS
			// we will prompt at the command line for the users 2fa code 
			try {
				String twofaCode = "908966";//twoFactorAuthCodePrompt();	
				token = gitHub.createTokenOtp(asList, string, "", twofaCode);// prompt at command line for 2fa OTP code
				//return;
			} catch (Exception e) {
				e.printStackTrace();
				fail();
			}
		}
		assert token!=null;
		
		String p = token.getToken();
		
		assert p!=null;
	}
}
