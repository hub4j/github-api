package org.kohsuke.github;

import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import com.github.tomakehurst.wiremock.extension.responsetemplating.ResponseTemplateTransformer;
import com.github.tomakehurst.wiremock.extension.responsetemplating.helpers.HandlebarsCurrentDateHelper;
import org.apache.commons.io.IOUtils;
import org.hamcrest.Matcher;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
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

import static org.hamcrest.Matchers.*;
import static org.junit.Assume.assumeFalse;
import static org.junit.Assume.assumeTrue;

// TODO: Auto-generated Javadoc
/**
 * The Class AbstractGitHubWireMockTest.
 *
 * @author Liam Newman
 */
public abstract class AbstractGitHubWireMockTest {

    private final GitHubBuilder githubBuilder = createGitHubBuilder();

    /** The Constant GITHUB_API_TEST_ORG. */
    final static String GITHUB_API_TEST_ORG = "hub4j-test-org";

    /** The Constant STUBBED_USER_LOGIN. */
    final static String STUBBED_USER_LOGIN = "placeholder-user";

    /** The Constant STUBBED_USER_PASSWORD. */
    final static String STUBBED_USER_PASSWORD = "placeholder-password";

    /** The use default git hub. */
    protected boolean useDefaultGitHub = true;

    /** The temp git hub repositories. */
    protected final Set<String> tempGitHubRepositories = new HashSet<>();

    /**
     * {@link GitHub} instance for use during test. Traffic will be part of snapshot when taken.
     */
    protected GitHub gitHub;

    private GitHub nonRecordingGitHub;

    /** The base files class path. */
    protected final String baseFilesClassPath = this.getClass().getName().replace('.', '/');

    /** The base record path. */
    protected final String baseRecordPath = "src/test/resources/" + baseFilesClassPath + "/wiremock";

    /** The mock git hub. */
    @Rule
    public final GitHubWireMockRule mockGitHub;

    /** The templating. */
    protected final TemplatingHelper templating = new TemplatingHelper();

    /**
     * Instantiates a new abstract git hub wire mock test.
     */
    public AbstractGitHubWireMockTest() {
        mockGitHub = new GitHubWireMockRule(this.getWireMockOptions());
    }

    /**
     * Gets the wire mock options.
     *
     * @return the wire mock options
     */
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

    /**
     * Gets the git hub builder.
     *
     * @return the git hub builder
     */
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

    /**
     * Wire mock setup.
     *
     * @throws Exception
     *             the exception
     */
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

    /**
     * Snapshot not allowed.
     */
    protected void snapshotNotAllowed() {
        assumeFalse("Test contains hand written mappings. Only valid when not taking a snapshot.",
                mockGitHub.isTakeSnapshot());
    }

    /**
     * Require proxy.
     *
     * @param reason
     *            the reason
     */
    protected void requireProxy(String reason) {
        assumeTrue("Test only valid when proxying (-Dtest.github.useProxy to enable): " + reason,
                mockGitHub.isUseProxy());
    }

    /**
     * Verify authenticated.
     *
     * @param instance
     *            the instance
     */
    protected void verifyAuthenticated(GitHub instance) {
        assertThat(
                "GitHub connection believes it is anonymous.  Make sure you set GITHUB_OAUTH or both GITHUB_LOGIN and GITHUB_PASSWORD environment variables",
                instance.isAnonymous(),
                Matchers.is(false));
    }

    /**
     * Gets the user.
     *
     * @return the user
     */
    protected GHUser getUser() {
        return getUser(gitHub);
    }

    /**
     * Gets the user.
     *
     * @param gitHub
     *            the git hub
     * @return the user
     */
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
     *            string name of the repository
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

    /**
     * Cleanup temp repositories.
     *
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     */
    @Before
    @After
    public void cleanupTempRepositories() throws IOException {
        if (mockGitHub.isUseProxy()) {
            for (String fullName : tempGitHubRepositories) {
                cleanupRepository(fullName);
            }
        }
    }

    /**
     * Cleanup repository.
     *
     * @param fullName
     *            the full name
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     */
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

    /**
     * Kohsuke.
     */
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

    /**
     * Fail.
     */
    public static void fail() {
        Assert.fail();
    }

    /**
     * Fail.
     *
     * @param reason
     *            the reason
     */
    public static void fail(String reason) {
        Assert.fail(reason);
    }

    /**
     * Assert that.
     *
     * @param <T>
     *            the generic type
     * @param actual
     *            the actual
     * @param matcher
     *            the matcher
     */
    public static <T> void assertThat(T actual, Matcher<? super T> matcher) {
        MatcherAssert.assertThat("", actual, matcher);
    }

    /**
     * Assert that.
     *
     * @param <T>
     *            the generic type
     * @param reason
     *            the reason
     * @param actual
     *            the actual
     * @param matcher
     *            the matcher
     */
    public static <T> void assertThat(String reason, T actual, Matcher<? super T> matcher) {
        MatcherAssert.assertThat(reason, actual, matcher);
    }

    /**
     * Assert that.
     *
     * @param reason
     *            the reason
     * @param assertion
     *            the assertion
     */
    public static void assertThat(String reason, boolean assertion) {
        MatcherAssert.assertThat(reason, assertion);
    }

    /**
     * The Class TemplatingHelper.
     */
    protected static class TemplatingHelper {

        /** The test start date. */
        public Date testStartDate = new Date();

        /**
         * New response transformer.
         *
         * @return the response template transformer
         */
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
