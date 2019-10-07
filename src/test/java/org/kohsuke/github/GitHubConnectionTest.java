package org.kohsuke.github;

import com.google.common.collect.Iterables;
import org.junit.Test;

import java.io.IOException;
import java.lang.reflect.Field;
import java.util.*;

import static org.hamcrest.CoreMatchers.*;

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
        assertEquals("https://api.github.invalid/test", hub.getApiURL("/test").toString());
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
        assertEquals("http://enterprise.kohsuke.org/api/v3/test", hub.getApiURL("/test").toString());
    }

    @Test
    public void testGitHubServerWithHttps() throws Exception {
        GitHub hub = GitHub.connectToEnterprise("https://enterprise.kohsuke.org/api/v3", "bogus", "bogus");
        assertEquals("https://enterprise.kohsuke.org/api/v3/test", hub.getApiURL("/test").toString());
    }
    @Test
    public void testGitHubServerWithoutServer() throws Exception {
        GitHub hub = GitHub.connectUsingPassword("kohsuke", "bogus");
        assertEquals("https://api.github.com/test", hub.getApiURL("/test").toString());
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

        assertEquals("bogus", builder.user);
        assertEquals("bogus", builder.oauthToken);
        assertEquals("bogus", builder.password);
        assertEquals("bogus", builder.jwtToken);

    }

    @Test
    public void testGitHubBuilderFromCustomEnvironment() throws IOException {
        Map<String, String> props = new HashMap<String, String>();

        props.put("customLogin", "bogusLogin");
        props.put("customOauth", "bogusOauth");
        props.put("customPassword", "bogusPassword");
        props.put("customEndpoint", "bogusEndpoint");

        setupEnvironment(props);

        GitHubBuilder builder = GitHubBuilder.fromEnvironment("customLogin", "customPassword", "customOauth", "customEndpoint");

        assertEquals("bogusLogin", builder.user);
        assertEquals("bogusOauth", builder.oauthToken);
        assertEquals("bogusPassword", builder.password);
        assertEquals("bogusEndpoint", builder.endpoint);
    }

    @Test
    public void testGitHubRateLimit() throws Exception {
        assertThat(mockGitHub.getRequestCount(), equalTo(0));
        GHRateLimit rateLimit = null;
        GitHub hub = null;
        Date lastReset = new Date(System.currentTimeMillis() / 1000L);
        int lastRemaining = 5000;

        // Give this a moment
        Thread.sleep(1000);

        // -------------------------------------------------------------
        // /user gets response with rate limit information
        hub = getGitHubBuilder()
            .withEndpoint(mockGitHub.apiServer().baseUrl()).build();
        hub.getMyself();

        assertThat(mockGitHub.getRequestCount(), equalTo(1));

        // Since we already had rate limit info these don't request again
        rateLimit = hub.lastRateLimit();
        assertThat(rateLimit, notNullValue());
        assertThat(rateLimit.limit, equalTo(5000));
        lastRemaining = rateLimit.remaining;
        // Because we're gettting this from old mocked info, it will be an older date
        //assertThat(rateLimit.getResetDate().compareTo(lastReset), equalTo(-1));
        lastReset = rateLimit.getResetDate();

        GHRateLimit headerRateLimit = rateLimit;

        // Give this a moment
        Thread.sleep(1000);

        // ratelimit() uses headerRateLimit if available
        assertThat(hub.rateLimit(), equalTo(headerRateLimit));

        assertThat(mockGitHub.getRequestCount(), equalTo(1));

        // Give this a moment
        Thread.sleep(1000);

        // Always requests new info
        rateLimit = hub.getRateLimit();
        assertThat(mockGitHub.getRequestCount(), equalTo(2));

        assertThat(rateLimit, notNullValue());
        assertThat(rateLimit.limit, equalTo(5000));
        // rate limit request is free
        assertThat(rateLimit.remaining, equalTo(lastRemaining));
        assertThat(rateLimit.getResetDate().compareTo(lastReset), equalTo(0));

        // Give this a moment
        Thread.sleep(1000);

        // Always requests new info
        rateLimit = hub.getRateLimit();
        assertThat(mockGitHub.getRequestCount(), equalTo(3));

        assertThat(rateLimit, notNullValue());
        assertThat(rateLimit.limit, equalTo(5000));
        // rate limit request is free
        assertThat(rateLimit.remaining, equalTo(lastRemaining));
        assertThat(rateLimit.getResetDate().compareTo(lastReset), equalTo(0));


        hub.getOrganization(GITHUB_API_TEST_ORG);
        assertThat(mockGitHub.getRequestCount(), equalTo(4));


        assertThat(hub.lastRateLimit(), not(equalTo(headerRateLimit)));
        rateLimit = hub.lastRateLimit();
        assertThat(rateLimit, notNullValue());
        assertThat(rateLimit.limit, equalTo(5000));
        // Org costs limit to query
        assertThat(rateLimit.remaining, equalTo(lastRemaining - 1));
        assertThat(rateLimit.getResetDate().compareTo(lastReset), equalTo(0));
        lastReset = rateLimit.getResetDate();
        headerRateLimit = rateLimit;

        // ratelimit() should prefer headerRateLimit when it is most recent
        assertThat(hub.rateLimit(), equalTo(headerRateLimit));

        assertThat(mockGitHub.getRequestCount(), equalTo(4));

        // Always requests new info
        rateLimit = hub.getRateLimit();
        assertThat(mockGitHub.getRequestCount(), equalTo(5));

        assertThat(rateLimit, notNullValue());
        assertThat(rateLimit.limit, equalTo(5000));
        // Org costs limit to query
        assertThat(rateLimit.remaining, equalTo(lastRemaining - 1));
        assertThat(rateLimit.getResetDate().compareTo(lastReset), equalTo(0));

        // ratelimit() should prefer headerRateLimit when getRateLimit() fails
        // BUG: When getRateLimit() succeeds, it should reset the ratelimit() to the new value
//        assertThat(hub.rateLimit(), equalTo(rateLimit));
//        assertThat(hub.rateLimit(), not(equalTo(headerRateLimit)));
        assertThat(hub.rateLimit(), equalTo(headerRateLimit));

        assertThat(mockGitHub.getRequestCount(), equalTo(5));
    }

    @Test
    public void testGitHubEnterpriseDoesNotHaveRateLimit() throws Exception {
        // Customized response that results in file not found the same as GitHub Enterprise
        snapshotNotAllowed();
        assertThat(mockGitHub.getRequestCount(), equalTo(0));
        GHRateLimit rateLimit = null;
        GitHub hub = null;


        Date lastReset = new Date(System.currentTimeMillis() / 1000L);

        // Give this a moment
        Thread.sleep(1000);

        // -------------------------------------------------------------
        // Before any queries, rate limit starts as null but may be requested
        hub = GitHub.connectToEnterprise(mockGitHub.apiServer().baseUrl(), "bogus", "bogus");
        assertThat(mockGitHub.getRequestCount(), equalTo(0));

        assertThat(hub.lastRateLimit(), nullValue());

        rateLimit = hub.rateLimit();
        assertThat(rateLimit, notNullValue());
        assertThat(rateLimit.limit, equalTo(1000000));
        assertThat(rateLimit.remaining, equalTo(1000000));
        assertThat(rateLimit.getResetDate().compareTo(lastReset), equalTo(1));
        lastReset = rateLimit.getResetDate();

        assertThat(mockGitHub.getRequestCount(), equalTo(1));

        // last is still null, because it actually means lastHeaderRateLimit
        assertThat(hub.lastRateLimit(), nullValue());

        assertThat(mockGitHub.getRequestCount(), equalTo(1));

        // Give this a moment
        Thread.sleep(1000);

        // -------------------------------------------------------------
        // First call to /user gets response without rate limit information
        hub = GitHub.connectToEnterprise(mockGitHub.apiServer().baseUrl(), "bogus", "bogus");
        hub.getMyself();
        assertThat(mockGitHub.getRequestCount(), equalTo(2));

        assertThat(hub.lastRateLimit(), nullValue());

        rateLimit = hub.rateLimit();
        assertThat(rateLimit, notNullValue());
        assertThat(rateLimit.limit, equalTo(1000000));
        assertThat(rateLimit.remaining, equalTo(1000000));
        assertThat(rateLimit.getResetDate().compareTo(lastReset), equalTo(1));
        lastReset = rateLimit.getResetDate();

        assertThat(mockGitHub.getRequestCount(), equalTo(3));

        // Give this a moment
        Thread.sleep(1000);

        // Always requests new info
        rateLimit = hub.getRateLimit();
        assertThat(mockGitHub.getRequestCount(), equalTo(4));

        assertThat(rateLimit, notNullValue());
        assertThat(rateLimit.limit, equalTo(1000000));
        assertThat(rateLimit.remaining, equalTo(1000000));
        assertThat(rateLimit.getResetDate().compareTo(lastReset), equalTo(1));

        // Give this a moment
        Thread.sleep(1000);


        // last is still null, because it actually means lastHeaderRateLimit
        assertThat(hub.lastRateLimit(), nullValue());

        // ratelimit() tries not to make additional requests, uses queried rate limit since header not available
        Thread.sleep(1000);
        assertThat(hub.rateLimit(), equalTo(rateLimit));

        // -------------------------------------------------------------
        // Second call to /user gets response with rate limit information
        hub = GitHub.connectToEnterprise(mockGitHub.apiServer().baseUrl(), "bogus", "bogus");
        hub.getMyself();
        assertThat(mockGitHub.getRequestCount(), equalTo(5));

        // Since we already had rate limit info these don't request again
        rateLimit = hub.lastRateLimit();
        assertThat(rateLimit, notNullValue());
        assertThat(rateLimit.limit, equalTo(5000));
        assertThat(rateLimit.remaining, equalTo(4978));
        // Because we're gettting this from old mocked info, it will be an older date
        assertThat(rateLimit.getResetDate().compareTo(lastReset), equalTo(-1));
        lastReset = rateLimit.getResetDate();

        GHRateLimit headerRateLimit = rateLimit;

        // Give this a moment
        Thread.sleep(1000);

        // ratelimit() uses headerRateLimit if available
        assertThat(hub.rateLimit(), equalTo(headerRateLimit));

        assertThat(mockGitHub.getRequestCount(), equalTo(5));

        // Give this a moment
        Thread.sleep(1000);

        // Always requests new info
        rateLimit = hub.getRateLimit();
        assertThat(mockGitHub.getRequestCount(), equalTo(6));

        assertThat(rateLimit, notNullValue());
        assertThat(rateLimit.limit, equalTo(1000000));
        assertThat(rateLimit.remaining, equalTo(1000000));
        assertThat(rateLimit.getResetDate().compareTo(lastReset), equalTo(1));

        // Give this a moment
        Thread.sleep(1000);

        // ratelimit() should prefer headerRateLimit when getRateLimit fails
        assertThat(hub.rateLimit(), equalTo(headerRateLimit));

        assertThat(mockGitHub.getRequestCount(), equalTo(6));
    }

    @Test
    public void testGitHubIsApiUrlValid() throws IOException {
        GitHub hub = GitHub.connectAnonymously();
        //GitHub github = GitHub.connectToEnterpriseAnonymously("https://github.mycompany.com/api/v3/");
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
     * Its used to wire in values for the github credentials to test that the GitHubBuilder works properly to resolve them.
     */
    private void setupEnvironment(Map<String, String> newenv) {
        try {
            Class<?> processEnvironmentClass = Class.forName("java.lang.ProcessEnvironment");
            Field theEnvironmentField = processEnvironmentClass.getDeclaredField("theEnvironment");
            theEnvironmentField.setAccessible(true);
            Map<String, String> env = (Map<String, String>) theEnvironmentField.get(null);
            env.putAll(newenv);
            Field theCaseInsensitiveEnvironmentField = processEnvironmentClass.getDeclaredField("theCaseInsensitiveEnvironment");
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
