package org.kohsuke.github;

import junit.framework.TestCase;

/**
 * Unit test for {@link GitHub}.
 */
public class GitHubTest extends TestCase {

    public void testGitHubServerWithHttp() throws Exception {
        GitHub hub = GitHub.connectToEnterprise("http://enterprise.kohsuke.org/api/v3", "token");
        assertEquals("http://enterprise.kohsuke.org/api/v3/test", hub.getApiURL("/test").toString());
    }

    public void testGitHubServerWithHttps() throws Exception {
        GitHub hub = GitHub.connectToEnterprise("https://enterprise.kohsuke.org/api/v3", "token");
        assertEquals("https://enterprise.kohsuke.org/api/v3/test", hub.getApiURL("/test").toString());
    }

    public void testGitHubServerWithoutServer() throws Exception {
        GitHub hub = GitHub.connect("kohsuke", "token", "password");
        assertEquals("https://api.github.com/test", hub.getApiURL("/test").toString());
    }
}
