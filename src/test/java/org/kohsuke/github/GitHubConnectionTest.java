package org.kohsuke.github;

import org.apache.commons.lang3.SystemUtils;
import org.junit.Assume;
import org.junit.Test;
import org.kohsuke.github.authorization.AuthorizationProvider;
import org.kohsuke.github.authorization.ImmutableAuthorizationProvider;
import org.kohsuke.github.authorization.UserAuthorizationProvider;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import static org.hamcrest.Matchers.*;

// TODO: Auto-generated Javadoc
/**
 * Unit test for {@link GitHub}.
 */
public class GitHubConnectionTest extends AbstractGitHubWireMockTest {

    /**
     * Instantiates a new git hub connection test.
     */
    public GitHubConnectionTest() {
        useDefaultGitHub = false;
    }

    /**
     * Test offline.
     *
     * @throws Exception
     *             the exception
     */
    @Test
    public void testOffline() throws Exception {
        GitHub hub = GitHub.offline();
        assertThat(GitHubRequest.getApiURL(hub.getClient().getApiUrl(), "/test").toString(),
                equalTo("https://api.github.invalid/test"));
        assertThat(hub.isAnonymous(), is(true));
        try {
            hub.getRateLimit();
            fail("Offline instance should always fail");
        } catch (IOException e) {
            assertThat(e.getMessage(), equalTo("Offline"));
        }
    }

    /**
     * Test git hub server with http.
     *
     * @throws Exception
     *             the exception
     */
    @Test
    public void testGitHubServerWithHttp() throws Exception {
        GitHub hub = GitHub.connectToEnterprise("http://enterprise.kohsuke.org/api/v3", "bogus", "bogus");
        assertThat(GitHubRequest.getApiURL(hub.getClient().getApiUrl(), "/test").toString(),
                equalTo("http://enterprise.kohsuke.org/api/v3/test"));
    }

    /**
     * Test git hub server with https.
     *
     * @throws Exception
     *             the exception
     */
    @Test
    public void testGitHubServerWithHttps() throws Exception {
        GitHub hub = GitHub.connectToEnterprise("https://enterprise.kohsuke.org/api/v3", "bogus", "bogus");
        assertThat(GitHubRequest.getApiURL(hub.getClient().getApiUrl(), "/test").toString(),
                equalTo("https://enterprise.kohsuke.org/api/v3/test"));
    }

    /**
     * Test git hub server without server.
     *
     * @throws Exception
     *             the exception
     */
    @Test
    public void testGitHubServerWithoutServer() throws Exception {
        GitHub hub = GitHub.connectUsingPassword("kohsuke", "bogus");
        assertThat(GitHubRequest.getApiURL(hub.getClient().getApiUrl(), "/test").toString(),
                equalTo("https://api.github.com/test"));
    }

    /**
     * Test git hub builder from environment.
     *
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     */
    @Test
    public void testGitHubBuilderFromEnvironment() throws IOException {
        // we disable this test for JDK 16+ as the current hacks in setupEnvironment() don't work with JDK 16+
        Assume.assumeThat(Double.valueOf(System.getProperty("java.specification.version")), lessThan(16.0));

        Map<String, String> props = new HashMap<String, String>();

        props.put("endpoint", "bogus endpoint url");
        props.put("oauth", "bogus oauth token string");
        setupEnvironment(props);
        GitHubBuilder builder = GitHubBuilder.fromEnvironment();

        assertThat(builder.endpoint, equalTo("bogus endpoint url"));

        assertThat(builder.authorizationProvider, instanceOf(UserAuthorizationProvider.class));
        assertThat(builder.authorizationProvider.getEncodedAuthorization(), equalTo("token bogus oauth token string"));
        assertThat(((UserAuthorizationProvider) builder.authorizationProvider).getLogin(), nullValue());

        props.put("login", "bogus login");
        setupEnvironment(props);
        builder = GitHubBuilder.fromEnvironment();

        assertThat(builder.authorizationProvider, instanceOf(UserAuthorizationProvider.class));
        assertThat(builder.authorizationProvider.getEncodedAuthorization(), equalTo("token bogus oauth token string"));
        assertThat(((UserAuthorizationProvider) builder.authorizationProvider).getLogin(), equalTo("bogus login"));

        props.put("jwt", "bogus jwt token string");
        setupEnvironment(props);
        builder = GitHubBuilder.fromEnvironment();

        assertThat(builder.authorizationProvider, not(instanceOf(UserAuthorizationProvider.class)));
        assertThat(builder.authorizationProvider.getEncodedAuthorization(), equalTo("Bearer bogus jwt token string"));

        props.put("password", "bogus weak password");
        setupEnvironment(props);
        builder = GitHubBuilder.fromEnvironment();

        assertThat(builder.authorizationProvider, instanceOf(UserAuthorizationProvider.class));
        assertThat(builder.authorizationProvider.getEncodedAuthorization(),
                equalTo("Basic Ym9ndXMgbG9naW46Ym9ndXMgd2VhayBwYXNzd29yZA=="));
        assertThat(((UserAuthorizationProvider) builder.authorizationProvider).getLogin(), equalTo("bogus login"));

    }

    /**
     * Test git hub builder from custom environment.
     *
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     */
    @Test
    public void testGitHubBuilderFromCustomEnvironment() throws IOException {
        // we disable this test for JDK 16+ as the current hacks in setupEnvironment() don't work with JDK 16+
        Assume.assumeThat(Double.valueOf(System.getProperty("java.specification.version")), lessThan(16.0));

        Map<String, String> props = new HashMap<String, String>();

        props.put("customEndpoint", "bogus endpoint url");
        props.put("customOauth", "bogus oauth token string");
        setupEnvironment(props);
        GitHubBuilder builder = GitHubBuilder
                .fromEnvironment("customLogin", "customPassword", "customOauth", "customEndpoint");

        assertThat(builder.endpoint, equalTo("bogus endpoint url"));

        assertThat(builder.authorizationProvider, instanceOf(UserAuthorizationProvider.class));
        assertThat(builder.authorizationProvider.getEncodedAuthorization(), equalTo("token bogus oauth token string"));
        assertThat(((UserAuthorizationProvider) builder.authorizationProvider).getLogin(), nullValue());

        props.put("customLogin", "bogus login");
        setupEnvironment(props);
        builder = GitHubBuilder.fromEnvironment("customLogin", "customPassword", "customOauth", "customEndpoint");

        assertThat(builder.authorizationProvider, instanceOf(UserAuthorizationProvider.class));
        assertThat(builder.authorizationProvider.getEncodedAuthorization(), equalTo("token bogus oauth token string"));
        assertThat(((UserAuthorizationProvider) builder.authorizationProvider).getLogin(), equalTo("bogus login"));

        props.put("customPassword", "bogus weak password");
        setupEnvironment(props);
        builder = GitHubBuilder.fromEnvironment("customLogin", "customPassword", "customOauth", "customEndpoint");

        assertThat(builder.authorizationProvider, instanceOf(UserAuthorizationProvider.class));
        assertThat(builder.authorizationProvider.getEncodedAuthorization(),
                equalTo("Basic Ym9ndXMgbG9naW46Ym9ndXMgd2VhayBwYXNzd29yZA=="));
        assertThat(((UserAuthorizationProvider) builder.authorizationProvider).getLogin(), equalTo("bogus login"));
    }

    /**
     * Test git hub builder from credentials with environment.
     *
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     */
    @Test
    public void testGitHubBuilderFromCredentialsWithEnvironment() throws IOException {
        // we disable this test for JDK 16+ as the current hacks in setupEnvironment() don't work with JDK 16+
        Assume.assumeThat(Double.valueOf(System.getProperty("java.specification.version")), lessThan(16.0));
        Assume.assumeFalse(SystemUtils.IS_OS_WINDOWS);

        Map<String, String> props = new HashMap<String, String>();

        props.put("endpoint", "bogus endpoint url");
        props.put("oauth", "bogus oauth token string");
        setupEnvironment(props);
        GitHubBuilder builder = GitHubBuilder.fromCredentials();

        assertThat(builder.endpoint, equalTo("bogus endpoint url"));

        assertThat(builder.authorizationProvider, instanceOf(UserAuthorizationProvider.class));
        assertThat(builder.authorizationProvider.getEncodedAuthorization(), equalTo("token bogus oauth token string"));
        assertThat(((UserAuthorizationProvider) builder.authorizationProvider).getLogin(), nullValue());

        props.put("login", "bogus login");
        setupEnvironment(props);
        builder = GitHubBuilder.fromCredentials();

        assertThat(builder.authorizationProvider, instanceOf(UserAuthorizationProvider.class));
        assertThat(builder.authorizationProvider.getEncodedAuthorization(), equalTo("token bogus oauth token string"));
        assertThat(((UserAuthorizationProvider) builder.authorizationProvider).getLogin(), equalTo("bogus login"));

        props.put("jwt", "bogus jwt token string");
        setupEnvironment(props);
        builder = GitHubBuilder.fromCredentials();

        assertThat(builder.authorizationProvider, not(instanceOf(UserAuthorizationProvider.class)));
        assertThat(builder.authorizationProvider.getEncodedAuthorization(), equalTo("Bearer bogus jwt token string"));

        props.put("password", "bogus weak password");
        setupEnvironment(props);
        builder = GitHubBuilder.fromCredentials();

        assertThat(builder.authorizationProvider, instanceOf(UserAuthorizationProvider.class));
        assertThat(builder.authorizationProvider.getEncodedAuthorization(),
                equalTo("Basic Ym9ndXMgbG9naW46Ym9ndXMgd2VhayBwYXNzd29yZA=="));
        assertThat(((UserAuthorizationProvider) builder.authorizationProvider).getLogin(), equalTo("bogus login"));
    }

    /**
     * Test git hub builder from credentials with property file.
     *
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     */
    @Test
    public void testGitHubBuilderFromCredentialsWithPropertyFile() throws IOException {
        // we disable this test for JDK 16+ as the current hacks in setupEnvironment() don't work with JDK 16+
        Assume.assumeThat(Double.valueOf(System.getProperty("java.specification.version")), lessThan(16.0));
        Assume.assumeFalse(SystemUtils.IS_OS_WINDOWS);

        Map<String, String> props = new HashMap<String, String>();

        // Clear the environment
        setupEnvironment(props);
        try {
            GitHubBuilder.HOME_DIRECTORY = new File(getTestDirectory());
            try {
                GitHubBuilder builder = GitHubBuilder.fromCredentials();
                fail();
            } catch (Exception e) {
                assertThat(e, instanceOf(IOException.class));
                assertThat(e.getMessage(), equalTo("Failed to resolve credentials from ~/.github or the environment."));
            }

            props = new HashMap<String, String>();

            props.put("endpoint", "bogus endpoint url");
            props.put("oauth", "bogus oauth token string");

            setupPropertyFile(props);

            GitHubBuilder builder = GitHubBuilder.fromCredentials();

            assertThat(builder.endpoint, equalTo("bogus endpoint url"));

            assertThat(builder.authorizationProvider, instanceOf(UserAuthorizationProvider.class));
            assertThat(builder.authorizationProvider.getEncodedAuthorization(),
                    equalTo("token bogus oauth token string"));
            assertThat(((UserAuthorizationProvider) builder.authorizationProvider).getLogin(), nullValue());

            props.put("login", "bogus login");
            setupPropertyFile(props);
            builder = GitHubBuilder.fromCredentials();

            assertThat(builder.authorizationProvider, instanceOf(UserAuthorizationProvider.class));
            assertThat(builder.authorizationProvider.getEncodedAuthorization(),
                    equalTo("token bogus oauth token string"));
            assertThat(((UserAuthorizationProvider) builder.authorizationProvider).getLogin(), equalTo("bogus login"));

            props.put("jwt", "bogus jwt token string");
            setupPropertyFile(props);
            builder = GitHubBuilder.fromCredentials();

            assertThat(builder.authorizationProvider, not(instanceOf(UserAuthorizationProvider.class)));
            assertThat(builder.authorizationProvider.getEncodedAuthorization(),
                    equalTo("Bearer bogus jwt token string"));

            props.put("password", "bogus weak password");
            setupPropertyFile(props);
            builder = GitHubBuilder.fromCredentials();

            assertThat(builder.authorizationProvider, instanceOf(UserAuthorizationProvider.class));
            assertThat(builder.authorizationProvider.getEncodedAuthorization(),
                    equalTo("Basic Ym9ndXMgbG9naW46Ym9ndXMgd2VhayBwYXNzd29yZA=="));
            assertThat(((UserAuthorizationProvider) builder.authorizationProvider).getLogin(), equalTo("bogus login"));
        } finally {
            GitHubBuilder.HOME_DIRECTORY = null;
            File propertyFile = new File(getTestDirectory(), ".github");
            propertyFile.delete();
        }
    }

    private void setupPropertyFile(Map<String, String> props) throws IOException {
        File propertyFile = new File(getTestDirectory(), ".github");
        Properties properties = new Properties();
        properties.putAll(props);
        properties.store(new FileOutputStream(propertyFile), "");
    }

    private String getTestDirectory() {
        return new File("target").getAbsolutePath();
    }

    /**
     * Test anonymous.
     *
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     */
    @Test
    public void testAnonymous() throws IOException {
        // we disable this test for JDK 16+ as the current hacks in setupEnvironment() don't work with JDK 16+
        Assume.assumeThat(Double.valueOf(System.getProperty("java.specification.version")), lessThan(16.0));

        Map<String, String> props = new HashMap<String, String>();

        props.put("endpoint", mockGitHub.apiServer().baseUrl());
        setupEnvironment(props);

        // No values present except endpoint
        GitHubBuilder builder = GitHubBuilder
                .fromEnvironment("customLogin", "customPassword", "customOauth", "endpoint");

        assertThat(builder.endpoint, equalTo(mockGitHub.apiServer().baseUrl()));
        assertThat(builder.authorizationProvider, sameInstance(AuthorizationProvider.ANONYMOUS));
    }

    /**
     * Test github builder with app installation token.
     *
     * @throws Exception
     *             the exception
     */
    @Test
    public void testGithubBuilderWithAppInstallationToken() throws Exception {

        GitHubBuilder builder = new GitHubBuilder().withAppInstallationToken("bogus app token");
        assertThat(builder.authorizationProvider, instanceOf(UserAuthorizationProvider.class));
        assertThat(builder.authorizationProvider.getEncodedAuthorization(), equalTo("token bogus app token"));
        assertThat(((UserAuthorizationProvider) builder.authorizationProvider).getLogin(), is(emptyString()));

        // test authorization header is set as in the RFC6749
        GitHub github = builder.build();
        // change this to get a request
        assertThat(github.getClient().getEncodedAuthorization(), equalTo("token bogus app token"));
        assertThat(github.getClient().getLogin(), is(emptyString()));
    }

    /**
     * Test git hub is api url valid.
     *
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     */
    @Test
    public void testGitHubIsApiUrlValid() throws IOException {
        // NOTE: We cannot test connectAnonymously on a general basis because it can hang if
        // rate limit is reached. We connectToEnterpriseAnonymously as the nearest equivalent.
        // GitHub hub = GitHub.connectAnonymously();

        GitHub hub = GitHub.connectToEnterpriseAnonymously(mockGitHub.apiServer().baseUrl());
        hub.checkApiUrlValidity();
        try {
            hub.checkApiUrlValidity();
            fail();
        } catch (IOException ioe) {
            assertThat(ioe.getMessage(), containsString("doesn't look like GitHub API URL"));
        }
        try {
            hub.checkApiUrlValidity();
            fail();
        } catch (IOException ioe) {
            assertThat(ioe.getMessage(), containsString("private mode enabled"));
        }
        try {
            hub.getClient().requireCredential();
            fail();
        } catch (Exception e) {
            assertThat(e.getMessage(), containsString("This operation requires a credential"));
        }
    }

    /**
     * Test git hub O auth user query.
     *
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     */
    @Test
    public void testGitHubOAuthUserQuery() throws IOException {
        snapshotNotAllowed();
        mockGitHub.customizeRecordSpec(recordSpecBuilder -> recordSpecBuilder.captureHeader("Authorization"));
        gitHub = getGitHubBuilder().withEndpoint(mockGitHub.apiServer().baseUrl())
                .withAuthorizationProvider(ImmutableAuthorizationProvider.fromOauthToken("super_secret_token"))
                .build();
        assertThat(mockGitHub.getRequestCount(), equalTo(1));
        assertThat(gitHub.getMyself(), notNullValue());
        assertThat(gitHub.getMyself().root(), notNullValue());
        assertThat(gitHub.getMyself().getLogin(), equalTo("bitwiseman"));
        assertThat(mockGitHub.getRequestCount(), equalTo(1));
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
