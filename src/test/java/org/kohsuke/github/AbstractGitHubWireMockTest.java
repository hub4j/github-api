package org.kohsuke.github;

import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import com.github.tomakehurst.wiremock.extension.responsetemplating.ResponseTemplateTransformer;
import com.github.tomakehurst.wiremock.extension.responsetemplating.helpers.HandlebarsCurrentDateHelper;
import org.apache.commons.io.IOUtils;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.Matchers;
import org.hamcrest.StringDescription;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.kohsuke.github.junit.GitHubWireMockRule;
import wiremock.com.github.jknack.handlebars.Helper;
import wiremock.com.github.jknack.handlebars.Options;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.*;

import static org.junit.Assume.assumeFalse;
import static org.junit.Assume.assumeTrue;

/**
 * @author Liam Newman
 */
public abstract class AbstractGitHubWireMockTest extends Assert {

    private final GitHubBuilder githubBuilder = createGitHubBuilder();

    final static String GITHUB_API_TEST_ORG = "hub4j-test-org";

    final static String STUBBED_USER_LOGIN = "placeholder-user";
    final static String STUBBED_USER_PASSWORD = "placeholder-password";

    protected boolean useDefaultGitHub = true;

    protected final Set<String> tempGitHubRepositories = new HashSet<>();

    /**
     * {@link GitHub} instance for use during test. Traffic will be part of snapshot when taken.
     */
    protected GitHub gitHub;

    private GitHub nonRecordingGitHub;

    protected final String baseFilesClassPath = this.getClass().getName().replace('.', '/');
    protected final String baseRecordPath = "src/test/resources/" + baseFilesClassPath + "/wiremock";

    @Rule
    public final GitHubWireMockRule mockGitHub;

    protected final TemplatingHelper templating = new TemplatingHelper();

    public AbstractGitHubWireMockTest() {
        mockGitHub = new GitHubWireMockRule(this.getWireMockOptions());
    }

    protected WireMockConfiguration getWireMockOptions() {
        return WireMockConfiguration.options().dynamicPort().usingFilesUnderDirectory(baseRecordPath);
    }

    private static GitHubBuilder createGitHubBuilder() {

        GitHubBuilder builder = new GitHubBuilder();

        try {
            File f = new File(System.getProperty("user.home"), ".github.kohsuke2");
            if (f.exists()) {
                Properties props = new Properties();
                FileInputStream in = null;
                try {
                    in = new FileInputStream(f);
                    props.load(in);
                } finally {
                    IOUtils.closeQuietly(in);
                }
                // use the non-standard credential preferentially, so that developers of this library do not have
                // to clutter their event stream.
                builder = GitHubBuilder.fromProperties(props);
            } else {

                builder = GitHubBuilder.fromEnvironment();

                builder = GitHubBuilder.fromCredentials();
            }
        } catch (IOException e) {
        }

        return builder.withRateLimitHandler(RateLimitHandler.FAIL);
    }

    protected GitHubBuilder getGitHubBuilder() {
        GitHubBuilder builder = githubBuilder.clone();

        if (!mockGitHub.isUseProxy()) {
            // This sets the user and password to a placeholder for wiremock testing
            // This makes the tests believe they are running with permissions
            // The recorded stubs will behave like they running with permissions
            builder.withPassword(STUBBED_USER_LOGIN, STUBBED_USER_PASSWORD);
        }

        return builder;
    }

    @Before
    public void wireMockSetup() throws Exception {
        GitHubBuilder builder = getGitHubBuilder().withEndpoint(mockGitHub.apiServer().baseUrl());

        if (useDefaultGitHub) {
            gitHub = builder.build();
        }

        if (mockGitHub.isUseProxy()) {
            nonRecordingGitHub = getGitHubBuilder().withEndpoint("https://api.github.com/").build();
        } else {
            nonRecordingGitHub = null;
        }
    }

    protected void snapshotNotAllowed() {
        assumeFalse("Test contains hand written mappings. Only valid when not taking a snapshot.",
                mockGitHub.isTakeSnapshot());
    }

    protected void requireProxy(String reason) {
        assumeTrue("Test only valid when proxying (-Dtest.github.useProxy to enable): " + reason,
                mockGitHub.isUseProxy());
    }

    protected void verifyAuthenticated(GitHub instance) {
        assertThat(
                "GitHub connection believes it is anonymous.  Make sure you set GITHUB_OAUTH or both GITHUB_LOGIN and GITHUB_PASSWORD environment variables",
                instance.isAnonymous(),
                Matchers.is(false));
    }

    protected GHUser getUser() {
        return getUser(gitHub);
    }

    protected static GHUser getUser(GitHub gitHub) {
        try {
            return gitHub.getMyself();
        } catch (IOException e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    /**
     * Creates a temporary repository that will be deleted at the end of the test. Repository name is based on the
     * current test method.
     *
     * @return a temporary repository
     * @throws IOException
     *             if repository could not be created or retrieved.
     */
    protected GHRepository getTempRepository() throws IOException {
        return getTempRepository("temp-" + this.mockGitHub.getMethodName());
    }

    /**
     * Creates a temporary repository that will be deleted at the end of the test.
     *
     * @param name
     *            string name of the the repository
     *
     * @return a temporary repository
     * @throws IOException
     *             if repository could not be created or retrieved.
     */
    protected GHRepository getTempRepository(String name) throws IOException {
        String fullName = getOrganization() + '/' + name;

        if (mockGitHub.isUseProxy()) {
            cleanupRepository(fullName);

            getCreateBuilder(name).description("A test repository for testing the github-api project: " + name)
                    .homepage("http://github-api.kohsuke.org/")
                    .autoInit(true)
                    .wiki(true)
                    .downloads(true)
                    .issues(true)
                    .private_(false)
                    .create();
            try {
                Thread.sleep(3000);
            } catch (InterruptedException e) {
                throw new RuntimeException(e.getMessage(), e);
            }
        }

        return gitHub.getRepository(fullName);
    }

    @Before
    @After
    public void cleanupTempRepositories() throws IOException {
        if (mockGitHub.isUseProxy()) {
            for (String fullName : tempGitHubRepositories) {
                cleanupRepository(fullName);
            }
        }
    }

    protected void cleanupRepository(String fullName) throws IOException {
        if (mockGitHub.isUseProxy()) {
            tempGitHubRepositories.add(fullName);
            try {
                GHRepository repository = getNonRecordingGitHub().getRepository(fullName);
                if (repository != null) {
                    repository.delete();
                }
            } catch (GHFileNotFoundException e) {
                // Repo already deleted
            }

        }
    }

    /**
     * {@link GitHub} instance for use before/after test. Traffic will not be part of snapshot when taken. Should only
     * be used when isUseProxy() or isTakeSnapShot().
     *
     * @return a github instance after checking Authentication
     */
    public GitHub getNonRecordingGitHub() {
        verifyAuthenticated(nonRecordingGitHub);
        return nonRecordingGitHub;
    }

    protected void kohsuke() {
        // No-op for now
        // Generally this means the test is doing something that requires additional access rights
        // Not always clear which ones.
        // TODO: Add helpers that assert the expected rights using nonRecordingGitHub and only when proxy is enabled
        // String login = getUserTest().getLogin();
        // assumeTrue(login.equals("kohsuke") || login.equals("kohsuke2"));
    }

    private GHCreateRepositoryBuilder getCreateBuilder(String name) throws IOException {
        GitHub github = getNonRecordingGitHub();

        if (mockGitHub.isTestWithOrg()) {
            return github.getOrganization(GITHUB_API_TEST_ORG).createRepository(name);
        }

        return github.createRepository(name);
    }

    private String getOrganization() throws IOException {
        return mockGitHub.isTestWithOrg() ? GITHUB_API_TEST_ORG : gitHub.getMyself().getLogin();
    }

    public static <T> void assertThat(T actual, Matcher<? super T> matcher) {
        assertThat("", actual, matcher);
    }

    public static <T> void assertThat(String reason, T actual, Matcher<? super T> matcher) {
        if (!matcher.matches(actual)) {
            Description description = new StringDescription();
            description.appendText(reason)
                    .appendText(System.lineSeparator())
                    .appendText("Expected: ")
                    .appendDescriptionOf(matcher)
                    .appendText(System.lineSeparator())
                    .appendText("     but: ");
            matcher.describeMismatch(actual, description);
            throw new AssertionError(description.toString());
        }
    }

    public static void assertThat(String reason, boolean assertion) {
        if (!assertion) {
            throw new AssertionError(reason);
        }
    }

    public static void assertEquals(Object expected, Object actual) {
        assertThat(actual, Matchers.equalTo(expected));
    }

    public static void assertNotEquals(Object expected, Object actual) {
        assertThat(actual, Matchers.not(expected));
    }

    public static void assertNotNull(Object actual) {
        assertThat(actual, Matchers.notNullValue());
    }

    public static void assertNull(Object actual) {
        assertThat(actual, Matchers.nullValue());
    }

    public static void assertTrue(Boolean condition) {
        assertThat(condition, Matchers.is(true));
    }

    public static void assertFalse(Boolean condition) {
        assertThat(condition, Matchers.is(false));
    }

    protected static class TemplatingHelper {
        public Date testStartDate = new Date();

        public ResponseTemplateTransformer newResponseTransformer() {
            testStartDate = new Date();
            return ResponseTemplateTransformer.builder()
                    .global(true)
                    .maxCacheEntries(0L)
                    .helper("testStartDate", new Helper<Object>() {
                        private HandlebarsCurrentDateHelper helper = new HandlebarsCurrentDateHelper();
                        @Override
                        public Object apply(final Object context, final Options options) throws IOException {
                            return this.helper.apply(TemplatingHelper.this.testStartDate, options);
                        }
                    })
                    .build();
        }
    }

}
