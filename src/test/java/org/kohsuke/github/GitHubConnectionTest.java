package org.kohsuke.github;

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

        props.put("endpoint", "bogus endpoint url");
        props.put("oauth", "bogus oauth token string");
        setupEnvironment(props);
        GitHubBuilder builder = GitHubBuilder.fromEnvironment();

        assertThat(builder.endpoint, equalTo("bogus endpoint url"));

        assertThat(builder.credentialProvider, instanceOf(ImmutableCredentialProvider.UserCredentialProvider.class));
        assertThat(builder.credentialProvider.getEncodedAuthorization(), equalTo("token bogus oauth token string"));
        assertThat(((ImmutableCredentialProvider.UserCredentialProvider) builder.credentialProvider).getLogin(),
                nullValue());

        props.put("login", "bogus login");
        setupEnvironment(props);
        builder = GitHubBuilder.fromEnvironment();

        assertThat(builder.credentialProvider, instanceOf(ImmutableCredentialProvider.UserCredentialProvider.class));
        assertThat(builder.credentialProvider.getEncodedAuthorization(), equalTo("token bogus oauth token string"));
        assertThat(((ImmutableCredentialProvider.UserCredentialProvider) builder.credentialProvider).getLogin(),
                equalTo("bogus login"));

        props.put("jwt", "bogus jwt token string");
        setupEnvironment(props);
        builder = GitHubBuilder.fromEnvironment();

        assertThat(builder.credentialProvider,
                not(instanceOf(ImmutableCredentialProvider.UserCredentialProvider.class)));
        assertThat(builder.credentialProvider.getEncodedAuthorization(), equalTo("Bearer bogus jwt token string"));

        props.put("password", "bogus weak password");
        setupEnvironment(props);
        builder = GitHubBuilder.fromEnvironment();

        assertThat(builder.credentialProvider, instanceOf(ImmutableCredentialProvider.UserCredentialProvider.class));
        assertThat(builder.credentialProvider.getEncodedAuthorization(),
                equalTo("Basic Ym9ndXMgbG9naW46Ym9ndXMgd2VhayBwYXNzd29yZA=="));
        assertThat(((ImmutableCredentialProvider.UserCredentialProvider) builder.credentialProvider).getLogin(),
                equalTo("bogus login"));

    }

    @Test
    public void testGitHubBuilderFromCustomEnvironment() throws IOException {
        Map<String, String> props = new HashMap<String, String>();

        props.put("customEndpoint", "bogus endpoint url");
        props.put("customOauth", "bogus oauth token string");
        setupEnvironment(props);
        GitHubBuilder builder = GitHubBuilder
                .fromEnvironment("customLogin", "customPassword", "customOauth", "customEndpoint");

        assertThat(builder.endpoint, equalTo("bogus endpoint url"));

        assertThat(builder.credentialProvider, instanceOf(ImmutableCredentialProvider.UserCredentialProvider.class));
        assertThat(builder.credentialProvider.getEncodedAuthorization(), equalTo("token bogus oauth token string"));
        assertThat(((ImmutableCredentialProvider.UserCredentialProvider) builder.credentialProvider).getLogin(),
                nullValue());

        props.put("customLogin", "bogus login");
        setupEnvironment(props);
        builder = GitHubBuilder.fromEnvironment("customLogin", "customPassword", "customOauth", "customEndpoint");

        assertThat(builder.credentialProvider, instanceOf(ImmutableCredentialProvider.UserCredentialProvider.class));
        assertThat(builder.credentialProvider.getEncodedAuthorization(), equalTo("token bogus oauth token string"));
        assertThat(((ImmutableCredentialProvider.UserCredentialProvider) builder.credentialProvider).getLogin(),
                equalTo("bogus login"));

        props.put("customPassword", "bogus weak password");
        setupEnvironment(props);
        builder = GitHubBuilder.fromEnvironment("customLogin", "customPassword", "customOauth", "customEndpoint");

        assertThat(builder.credentialProvider, instanceOf(ImmutableCredentialProvider.UserCredentialProvider.class));
        assertThat(builder.credentialProvider.getEncodedAuthorization(),
                equalTo("Basic Ym9ndXMgbG9naW46Ym9ndXMgd2VhayBwYXNzd29yZA=="));
        assertThat(((ImmutableCredentialProvider.UserCredentialProvider) builder.credentialProvider).getLogin(),
                equalTo("bogus login"));
    }

    @Test
    public void testGithubBuilderWithAppInstallationToken() throws Exception {

        GitHubBuilder builder = new GitHubBuilder().withAppInstallationToken("bogus app token");
        assertThat(builder.credentialProvider, instanceOf(ImmutableCredentialProvider.UserCredentialProvider.class));
        assertThat(builder.credentialProvider.getEncodedAuthorization(), equalTo("token bogus app token"));
        assertThat(((ImmutableCredentialProvider.UserCredentialProvider) builder.credentialProvider).getLogin(),
                equalTo(""));

        // test authorization header is set as in the RFC6749
        GitHub github = builder.build();
        // change this to get a request
        assertEquals("token bogus app token", github.getClient().getEncodedAuthorization());
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
