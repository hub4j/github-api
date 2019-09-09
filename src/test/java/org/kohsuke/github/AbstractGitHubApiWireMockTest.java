package org.kohsuke.github;

import com.github.tomakehurst.wiremock.common.FileSource;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import com.github.tomakehurst.wiremock.extension.Parameters;
import com.github.tomakehurst.wiremock.extension.ResponseTransformer;
import com.github.tomakehurst.wiremock.http.Request;
import com.github.tomakehurst.wiremock.http.Response;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.kohsuke.github.junit.WireMockRule;

import java.util.Properties;

import static com.github.tomakehurst.wiremock.client.WireMock.*;

/**
 * @author Kohsuke Kawaguchi
 */
public abstract class AbstractGitHubApiWireMockTest extends Assert {

    protected GitHub gitHub;
    private final String baseFilesClassPath = this.getClass().getName().replace('.', '/');
    protected final String baseRecordPath = "src/test/resources/" + baseFilesClassPath + "/wiremock";
    private boolean takeSnapshot = false;

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
                    return "url-rewrite";
                }
            })
    );

    @Before
    public void wireMockSetup() throws Exception {

        Properties props = GitHubBuilder.getPropertiesFromEnvironment();

        // By default the wiremock tests will proxy to github transparently without taking snapshots
        // This lets you debug any
        if(Boolean.parseBoolean(props.getProperty("snapshot", "false"))) {
            takeSnapshot = true;
        }

        if(!props.containsKey("oauth") && !takeSnapshot) {
            // This sets the oauth token to a placeholder wiremock tests
            // This makes the tests believe they are running with permissions
            // The recorded stubs will behave like they running with permissions
            props.setProperty("oauth", "placeholder-will-fail-when-not-mocking");

            githubApi.stubFor(
                any(urlPathMatching(".*"))
                    .willReturn(status(500))
                    .atPriority(100));
        }

        githubApi.stubFor(
            proxyAllTo("https://api.github.com/")
                .atPriority(100)
        );

        gitHub = GitHubBuilder.fromProperties(props)
            .withEndpoint("http://localhost:" + githubApi.port())
            .withRateLimitHandler(RateLimitHandler.FAIL)
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
