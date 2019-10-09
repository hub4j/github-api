package org.kohsuke.github;

import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import org.apache.commons.io.IOUtils;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.kohsuke.github.junit.GitHubWireMockRule;

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

    final static String GITHUB_API_TEST_ORG = "github-api-test-org";

    final static String STUBBED_USER_LOGIN = "placeholder-user";
    final static String STUBBED_USER_PASSWORD = "placeholder-password";

    protected boolean useDefaultGitHub = true;

    protected Set<String> tempGitHubRepositories = new HashSet<>();

    /**
     * {@link GitHub} instance for use during test.
     * Traffic will be part of snapshot when taken.
     */
    protected GitHub gitHub;

    /**
     * {@link GitHub} instance for use before/after test.
     * Traffic will not be part of snapshot when taken.
     * Should only be used when isUseProxy() or isTakeSnapShot().
     */
    protected GitHub gitHubBeforeAfter;

    protected final String baseFilesClassPath = this.getClass().getName().replace('.', '/');
    protected final String baseRecordPath = "src/test/resources/" + baseFilesClassPath + "/wiremock";

    @Rule
    public final GitHubWireMockRule mockGitHub;

    public AbstractGitHubWireMockTest() {
        mockGitHub = new GitHubWireMockRule(
            this.getWireMockOptions()
        );
    }

    protected WireMockConfiguration getWireMockOptions() {
        return WireMockConfiguration.options()
            .dynamicPort()
            .usingFilesUnderDirectory(baseRecordPath);
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
            builder.oauthToken = null;
            builder.withPassword(STUBBED_USER_LOGIN, STUBBED_USER_PASSWORD);
        }

        return builder;
    }

    @Before
    public void wireMockSetup() throws Exception {
        GitHubBuilder builder = getGitHubBuilder()
            .withEndpoint(mockGitHub.apiServer().baseUrl());

        if (useDefaultGitHub) {
            gitHub = builder
                .build();
        }

        if (mockGitHub.isUseProxy()) {
            gitHubBeforeAfter = getGitHubBuilder()
                .withEndpoint("https://api.github.com/")
                .build();
        } else {
            gitHubBeforeAfter = null;
        }
    }

    protected void snapshotNotAllowed() {
        assumeFalse("Test contains hand written mappings. Only valid when not taking a snapshot.", mockGitHub.isTakeSnapshot());
    }

    protected void requireProxy(String reason) {
        assumeTrue("Test only valid when proxying (-Dtest.github.useProxy to enable): " + reason, mockGitHub.isUseProxy());
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
     *
     */
    protected GHRepository getTempRepository() throws IOException{
        return getTempRepository("temp-" + this.mockGitHub.getMethodName());
    }

    /**
     * Creates
     */
    protected GHRepository getTempRepository(String name) throws IOException {
        String fullName = GITHUB_API_TEST_ORG +'/' + name;
        if (mockGitHub.isUseProxy()) {
            GHRepository repository = gitHubBeforeAfter
                .getOrganization(GITHUB_API_TEST_ORG)
                .getRepository(name);
            if (repository != null) {
                repository.delete();
            }

            repository = gitHubBeforeAfter.getOrganization(GITHUB_API_TEST_ORG)
                .createRepository(name)
                .description("A test repository for testing the github-api project: " + name )
                .homepage("http://github-api.kohsuke.org/")
                .autoInit(true)
                .create();
            try {
                Thread.sleep(3000);
            } catch (InterruptedException e) {
                throw new RuntimeException(e.getMessage(), e);
            }

            configureTempRepository(repository);

            this.tempGitHubRepositories.add(fullName);
        }

        return gitHub.getRepository(fullName);
    }

    protected void configureTempRepository(GHRepository repository) throws IOException {
        repository.enableIssueTracker(true);
        repository.enableDownloads(true);
        repository.enableWiki(true);
    }

    @Before
    @After
    public void cleanupTempRepositories() throws IOException {
        if (mockGitHub.isUseProxy()) {
            for(String fullName : tempGitHubRepositories) {
                cleanupRepository(fullName);
            }
        }
    }

    protected void cleanupRepository(String fullName) throws IOException {
        if (mockGitHub.isUseProxy()) {
            tempGitHubRepositories.add(fullName);
            try {
                GHRepository repository = gitHubBeforeAfter.getRepository(fullName);
                if (repository != null) {
                    repository.delete();
                }
            } catch (GHFileNotFoundException e) {
                // Repo already deleted
            }

        }
    }

    protected void kohsuke() {
        // No-op for now
        // Generally this means the test is doing something that requires additional access rights
        // Not always clear which ones.
        // TODO: Add helpers that assert the expected rights using gitHubBeforeAfter and only when proxy is enabled
//        String login = getUserTest().getLogin();
//        assumeTrue(login.equals("kohsuke") || login.equals("kohsuke2"));
    }

}
