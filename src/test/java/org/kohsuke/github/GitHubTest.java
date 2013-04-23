package org.kohsuke.github;

import junit.framework.TestCase;

/**
 * Unit test for {@link GitHub}.
 */
public class GitHubTest extends TestCase {

    public void testGitHubServerWithHttp() throws Exception {
        GitHub hub = GitHub.connectToEnterprise("http://enterprise.kohsuke.org/api/v3", "bogus","bogus");
        assertEquals("http://enterprise.kohsuke.org/api/v3/test", hub.getApiURL("/test").toString());
    }

    public void testGitHubServerWithHttps() throws Exception {
        GitHub hub = GitHub.connectToEnterprise("https://enterprise.kohsuke.org/api/v3", "bogus","bogus");
        assertEquals("https://enterprise.kohsuke.org/api/v3/test", hub.getApiURL("/test").toString());
    }

    public void testGitHubServerWithoutServer() throws Exception {
        GitHub hub = GitHub.connectUsingPassword("kohsuke", "bogus");
        assertEquals("https://api.github.com/test", hub.getApiURL("/test").toString());
    }
}
