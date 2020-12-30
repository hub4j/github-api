package org.kohsuke.github;

import org.junit.Test;

import java.io.IOException;
import java.lang.reflect.Field;
import java.util.*;

/**
 * Unit test for {@link GitHub}.
 */
public class GitHubConnectionTest extends AbstractGitHubWireMockTest {

    public GitHubConnectionTest() {
        useDefaultGitHub = false;
    }

    @Test
    public void testOffline() throws Exception {
        GitHub hub = GitHub.offline();
        assertEquals("https://api.github.invalid/test",

                GitHubRequest.getApiURL(hub.getClient().getApiUrl(), "/test").toString());
        assertTrue(hub.isAnonymous());
        try {
            hub.getRateLimit();
            fail("Offline instance should always fail");
        } catch (IOException e) {
            assertEquals("Offline", e.getMessage());
        }
    }

    @Test
    public void testGitHubServerWithHttp() throws Exception {
        GitHub hub = GitHub.connectToEnterprise("http://enterprise.kohsuke.org/api/v3", "bogus", "bogus");
        assertEquals("http://enterprise.kohsuke.org/api/v3/test",
                GitHubRequest.getApiURL(hub.getClient().getApiUrl(), "/test").toString());
    }

    @Test
    public void testGitHubServerWithHttps() throws Exception {
        GitHub hub = GitHub.connectToEnterprise("https://enterprise.kohsuke.org/api/v3", "bogus", "bogus");
        assertEquals("https://enterprise.kohsuke.org/api/v3/test",
                GitHubRequest.getApiURL(hub.getClient().getApiUrl(), "/test").toString());
    }

    @Test
    public void testGitHubServerWithoutServer() throws Exception {
        GitHub hub = GitHub.connectUsingPassword("kohsuke", "bogus");
        assertEquals("https://api.github.com/test",
                GitHubRequest.getApiURL(hub.getClient().getApiUrl(), "/test").toString());
    }

    @Test
    public void testGitHubBuilderFromEnvironment() throws IOException {

        Map<String, String> props = new HashMap<String, String>();

        props.put("login", "bogus");
        props.put("oauth", "bogus");
        props.put("password", "bogus");
        props.put("jwt", "bogus");

        setupEnvironment(props);

        GitHubBuilder builder = GitHubBuilder.fromEnvironment();

        // TODO: figure out how to test these again
        // assertEquals("bogus", builder.user);
        // assertEquals("bogus", builder.oauthToken);
        // assertEquals("bogus", builder.password);
        // assertEquals("bogus", builder.jwtToken);

    }

    @Test
    public void testGitHubBuilderFromCustomEnvironment() throws IOException {
        Map<String, String> props = new HashMap<String, String>();

        props.put("customLogin", "bogusLogin");
        props.put("customOauth", "bogusOauth");
        props.put("customPassword", "bogusPassword");
        props.put("customEndpoint", "bogusEndpoint");

        setupEnvironment(props);

        GitHubBuilder builder = GitHubBuilder
                .fromEnvironment("customLogin", "customPassword", "customOauth", "customEndpoint");

        // TODO: figure out how to test these again
        // assertEquals("bogusLogin", builder.user);
        // assertEquals("bogusOauth", builder.oauthToken);
        // assertEquals("bogusPassword", builder.password);
        assertEquals("bogusEndpoint", builder.endpoint);
    }

    @Test
    public void testGithubBuilderWithAppInstallationToken() throws Exception {
        GitHubBuilder builder = new GitHubBuilder().withAppInstallationToken("bogus");
        // assertEquals("bogus", builder.oauthToken);
        // assertEquals("", builder.user);

        // test authorization header is set as in the RFC6749
        GitHub github = builder.build();
        // change this to get a request
        assertEquals("token bogus", github.getClient().credentialProvider.getEncodedAuthorization());
        assertEquals("", github.getClient().login);
    }

    @Test
    public void testGitHubIsApiUrlValid() throws IOException {
        // NOTE: We cannot test connectAnonymously on a general basis because it can hang if
        // rate limit is reached. We connectToEnterpriseAnonymously as the nearest equivalent.
        // GitHub hub = GitHub.connectAnonymously();

        GitHub hub = GitHub.connectToEnterpriseAnonymously(mockGitHub.apiServer().baseUrl());
        try {
            hub.checkApiUrlValidity();
        } catch (IOException ioe) {
            assertTrue(ioe.getMessage().contains("private mode enabled"));
        }
    }

    /*
     * Copied from StackOverflow: http://stackoverflow.com/a/7201825/2336755
     *
     * This allows changing the in memory process environment.
     *
     * Its used to wire in values for the github credentials to test that the GitHubBuilder works properly to resolve
     * them.
     */
    private void setupEnvironment(Map<String, String> newenv) {
        try {
            Class<?> processEnvironmentClass = Class.forName("java.lang.ProcessEnvironment");
            Field theEnvironmentField = processEnvironmentClass.getDeclaredField("theEnvironment");
            theEnvironmentField.setAccessible(true);
            Map<String, String> env = (Map<String, String>) theEnvironmentField.get(null);
            env.putAll(newenv);
            Field theCaseInsensitiveEnvironmentField = processEnvironmentClass
                    .getDeclaredField("theCaseInsensitiveEnvironment");
            theCaseInsensitiveEnvironmentField.setAccessible(true);
            Map<String, String> cienv = (Map<String, String>) theCaseInsensitiveEnvironmentField.get(null);
            cienv.putAll(newenv);
        } catch (NoSuchFieldException e) {
            try {
                Class[] classes = Collections.class.getDeclaredClasses();
                Map<String, String> env = System.getenv();
                for (Class cl : classes) {
                    if ("java.util.Collections$UnmodifiableMap".equals(cl.getName())) {
                        Field field = cl.getDeclaredField("m");
                        field.setAccessible(true);
                        Object obj = field.get(env);
                        Map<String, String> map = (Map<String, String>) obj;
                        map.clear();
                        map.putAll(newenv);
                    }
                }
            } catch (Exception e2) {
                e2.printStackTrace();
            }
        } catch (Exception e1) {
            e1.printStackTrace();
        }
    }
}
