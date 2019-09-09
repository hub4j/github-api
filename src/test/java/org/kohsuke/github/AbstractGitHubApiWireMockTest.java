package org.kohsuke.github;

import com.github.tomakehurst.wiremock.common.FileSource;
import com.github.tomakehurst.wiremock.common.SingleRootFileSource;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import com.github.tomakehurst.wiremock.extension.Parameters;
import com.github.tomakehurst.wiremock.extension.ResponseTransformer;
import com.github.tomakehurst.wiremock.http.Request;
import com.github.tomakehurst.wiremock.http.Response;
import com.github.tomakehurst.wiremock.junit.WireMockRule;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;

import java.io.File;

import static com.github.tomakehurst.wiremock.client.WireMock.*;

/**
 * @author Kohsuke Kawaguchi
 */
public abstract class AbstractGitHubApiWireMockTest extends Assert {

    protected GitHub gitHub;
    protected final String baseFilesClassPath = this.getClass().getName().replace('.', '/');;
    protected final String baseRecordPath = "src/test/resources/" + baseFilesClassPath;

    public static WireMockRuleFactory factory = new WireMockRuleFactory();

    @Rule
    public WireMockRule githubApi = factory.getRule(WireMockConfiguration.options()
        .dynamicPort()
        .usingFilesUnderClasspath(baseFilesClassPath + "/api")
        .extensions(
            new ResponseTransformer() {
                @Override
                public Response transform(Request request, Response response, FileSource files,
                                          Parameters parameters) {
                    try {
                      if ("application/json"
                          .equals(response.getHeaders().getContentTypeHeader().mimeTypePart())) {
                        // Something strange happending here... turning off for now
//                        return Response.Builder.like(response)
//                            .but()
//                            .body(response.getBodyAsString()
//                                .replace("https://api.github.com/",
//                                    "http://localhost:" + githubApi.port() + "/")
//                                .replace("https://raw.githubusercontent.com/",
//                                    "http://localhost:" + githubRaw.port() + "/")
//                            )
//                            .build();
                      }
                    } catch (Exception e) {
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
    public void prepareMockGitHub() throws Exception {
        new File(baseRecordPath + "/api/mappings").mkdirs();
        new File(baseRecordPath + "/api/__files").mkdirs();

        githubApi.stubFor(proxyAllTo("https://api.github.com/").atPriority(10));

        githubApi.enableRecordMappings(new SingleRootFileSource(baseRecordPath + "/api/mappings"),
            new SingleRootFileSource(baseRecordPath + "/api/__files"));

        gitHub = GitHubBuilder.fromEnvironment()
            .withEndpoint("http://localhost:" + githubApi.port())
            .withRateLimitHandler(RateLimitHandler.FAIL)
            .build();

    }
}
