package org.kohsuke.github;

import junit.framework.TestCase;

/**
 * Unit test for {@link GitHub}.
 */
public class GitHubTest extends TestCase {

    public void testGitHubServerWithHttp() throws Exception {
        GitHub hub = GitHub.connect("http://enterprise.kohsuke.org/api/v3", "kohsuke", "token", "password");
        assertEquals("http://enterprise.kohsuke.org/api/v3/test", hub.getApiURL("/test").toString());
    }

    public void testGitHubServerWithHttps() throws Exception {
        GitHub hub = GitHub.connect("https://enterprise.kohsuke.org/api/v3", "kohsuke", "token", "password");
        assertEquals("https://enterprise.kohsuke.org/api/v3/test", hub.getApiURL("/test").toString());
    }

    public void testGitHubServerWithoutProtocol() throws Exception {
        GitHub hub = GitHub.connect("enterprise.kohsuke.org", "kohsuke", "token", "password");
        assertEquals("https://enterprise.kohsuke.org/api/v3/test", hub.getApiURL("/test").toString());
    }

    public void testGitHubServerWithoutServer() throws Exception {
        GitHub hub = GitHub.connect("kohsuke", "token", "password");
        assertEquals("https://api.github.com/test", hub.getApiURL("/test").toString());
    }
}
