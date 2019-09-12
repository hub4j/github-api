package org.kohsuke.github;

import com.github.tomakehurst.wiremock.common.FileSource;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import com.github.tomakehurst.wiremock.extension.Parameters;
import com.github.tomakehurst.wiremock.extension.ResponseTransformer;
import com.github.tomakehurst.wiremock.http.Request;
import com.github.tomakehurst.wiremock.http.Response;
import org.apache.commons.io.IOUtils;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.rules.TestWatcher;
import org.kohsuke.github.junit.GitHubApiWireMockRule;
import org.kohsuke.github.junit.WireMockRule;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

import static com.github.tomakehurst.wiremock.client.WireMock.*;

/**
 * @author Liam Newman
 */
public abstract class AbstractGitHubApiWireMockTest extends Assert {

    private final GitHubBuilder githubBuilder = createGitHubBuilder();

    public final static String STUBBED_USER_LOGIN = "placeholder-user";
    public final static String STUBBED_USER_PASSWORD = "placeholder-password";

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
    public GitHubApiWireMockRule githubApi = new GitHubApiWireMockRule(
        WireMockConfiguration.options()
            .dynamicPort()
            .usingFilesUnderDirectory(baseRecordPath)
    );

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
                builder = GitHubBuilder.fromCredentials();
            }
        } catch (IOException e) {
        }

        return builder.withRateLimitHandler(RateLimitHandler.FAIL);
    }

    protected GitHubBuilder getGitHubBuilder() {
        GitHubBuilder builder = githubBuilder.clone();

        if (!githubApi.isUseProxy()) {
            // This sets the user and password to a placeholder for wiremock testing
            // This makes the tests believe they are running with permissions
            // The recorded stubs will behave like they running with permissions
            builder.withPassword(STUBBED_USER_LOGIN, STUBBED_USER_PASSWORD);
        }

        return builder;
    }

    @Before
    public void wireMockSetup() throws Exception {
        GitHubBuilder builder = getGitHubBuilder()
            .withEndpoint(githubApi.baseUrl());

        gitHub = builder
            .build();

        if (githubApi.isUseProxy()) {
            gitHubBeforeAfter = getGitHubBuilder()
                .withEndpoint("https://api.github.com/")
                .build();
        } else {
            gitHubBeforeAfter = null;
        }
    }
}
