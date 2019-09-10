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
import org.kohsuke.github.junit.WireMockRule;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

import static com.github.tomakehurst.wiremock.client.WireMock.*;

/**
 * @author Kohsuke Kawaguchi
 */
public abstract class AbstractGitHubApiWireMockTest extends Assert {

    // By default the wiremock tests will run without proxy or taking a snapshot.
    // The tests will use only the stubbed data and will fail if requests are made for missing data.
    // You can use the proxy without taking a snapshot while writing and debugging tests.
    // You cannot take a snapshot without proxying.
    protected final static boolean takeSnapshot = System.getProperty("test.github.takeSnapshot", "false") != "false";
    protected final static boolean useProxy = takeSnapshot || System.getProperty("test.github.useProxy", "false") != "false";
    private final GitHubBuilder githubBuilder = createGitHubBuilder();

    public final static String STUBBED_USER_LOGIN = "placeholder-user";
    public final static String STUBBED_USER_PASSWORD = "placeholder-password";

    protected GitHub gitHub;
    private final String baseFilesClassPath = this.getClass().getName().replace('.', '/');
    protected final String baseRecordPath = "src/test/resources/" + baseFilesClassPath + "/wiremock";



    @Rule
    public WireMockRule githubApi = new WireMockRule(WireMockConfiguration.options()
        .dynamicPort()
        .usingFilesUnderDirectory(baseRecordPath)
        .extensions(
            new ResponseTransformer() {
                @Override
                public Response transform(Request request, Response response, FileSource files,
                                          Parameters parameters) {
                    if ("application/json"
                            .equals(response.getHeaders().getContentTypeHeader().mimeTypePart())
                        && !response.getHeaders().getHeader("Content-Encoding").containsValue("gzip")) {
                        return Response.Builder.like(response)
                            .but()
                            .body(response.getBodyAsString()
                                .replace("https://api.github.com/",
                                    "http://localhost:" + githubApi.port() + "/")
                            )
                            .build();
                    }
                    return response;
                }

                @Override
                public String getName() {
                    return "github-api-url-rewrite";
                }
            })
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

        if (!useProxy) {
            // This sets the user and password to a placeholder for wiremock testing
            // This makes the tests believe they are running with permissions
            // The recorded stubs will behave like they running with permissions
            builder.withPassword(STUBBED_USER_LOGIN, STUBBED_USER_PASSWORD);
        }

        return builder.withRateLimitHandler(RateLimitHandler.FAIL);
    }

    protected GitHubBuilder getGitHubBuilder() {
        return githubBuilder;
    }

    @Before
    public void wireMockSetup() throws Exception {
        if(useProxy) {
            githubApi.stubFor(
                proxyAllTo("https://api.github.com/")
                    .atPriority(100)
            );
        } else {
            // Just to be super clear
            githubApi.stubFor(
                any(urlPathMatching(".*"))
                    .willReturn(status(500).withBody("Stubbed data not found. Set test.github.use-proxy to have WireMock proxy to github"))
                    .atPriority(100));
        }


        gitHub = getGitHubBuilder()
            .withEndpoint("http://localhost:" + githubApi.port())
            .build();
    }

    @After
    public void snapshotRequests() {
        if (takeSnapshot) {
            githubApi.snapshotRecord(recordSpec()
                .forTarget("https://api.github.com")
                .extractTextBodiesOver(255));
        }
    }
}
