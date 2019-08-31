package org.kohsuke.github;

import com.github.tomakehurst.wiremock.common.FileSource;
import com.github.tomakehurst.wiremock.common.SingleRootFileSource;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import com.github.tomakehurst.wiremock.extension.Parameters;
import com.github.tomakehurst.wiremock.extension.ResponseTransformer;
import com.github.tomakehurst.wiremock.http.Request;
import com.github.tomakehurst.wiremock.http.Response;
import com.github.tomakehurst.wiremock.junit.WireMockRule;
import com.github.tomakehurst.wiremock.stubbing.Scenario;
import junit.framework.TestCase;
import org.apache.commons.io.IOUtils;
import org.junit.Assert;
import org.junit.Assume;
import org.junit.Before;
import org.junit.Rule;
import org.kohsuke.randname.RandomNameGenerator;

import java.io.Console;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

import static com.github.tomakehurst.wiremock.client.WireMock.*;

/**
 * @author Kohsuke Kawaguchi
 */
public abstract class AbstractGitHubApiWireMockBase extends Assert {

    protected GitHub gitHub;

    public static WireMockRuleFactory factory = new WireMockRuleFactory();

    @Rule
    public WireMockRule githubRaw = factory.getRule(WireMockConfiguration.options()
        .dynamicPort()
        .usingFilesUnderClasspath("raw")
    );
    @Rule
    public WireMockRule githubApi = factory.getRule(WireMockConfiguration.options()
        .dynamicPort()
        .usingFilesUnderClasspath("api")
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
        new File("src/test/resources/api/mappings").mkdirs();
        new File("src/test/resources/api/__files").mkdirs();
        new File("src/test/resources/raw/mappings").mkdirs();
        new File("src/test/resources/raw/__files").mkdirs();

        githubApi.stubFor(
            get(urlMatching(".*")).atPriority(10).willReturn(aResponse().proxiedFrom("https://api.github.com/")));
        githubRaw.stubFor(get(urlMatching(".*")).atPriority(10)
            .willReturn(aResponse().proxiedFrom("https://raw.githubusercontent.com/")));

        githubApi.enableRecordMappings(new SingleRootFileSource("src/test/resources/api/mappings"),
            new SingleRootFileSource("src/test/resources/api/__files"));
        githubRaw.enableRecordMappings(new SingleRootFileSource("src/test/resources/raw/mappings"),
            new SingleRootFileSource("src/test/resources/raw/__files"));

//        githubApi.stubFor(
//            get(urlEqualTo("/repos/cloudbeers/yolo/pulls/2"))
//                .inScenario("Pull Request Merge Hash")
//                .whenScenarioStateIs(Scenario.STARTED)
//                .willReturn(
//                    aResponse()
//                        .withHeader("Content-Type", "application/json; charset=utf-8")
//                        .withBodyFile("body-yolo-pulls-2-mergeable-null.json"))
//                .willSetStateTo("Pull Request Merge Hash - retry 1"));
//
//        githubApi.stubFor(
//            get(urlEqualTo("/repos/cloudbeers/yolo/pulls/2"))
//                .inScenario("Pull Request Merge Hash")
//                .whenScenarioStateIs("Pull Request Merge Hash - retry 1")
//                .willReturn(
//                    aResponse()
//                        .withHeader("Content-Type", "application/json; charset=utf-8")
//                        .withBodyFile("body-yolo-pulls-2-mergeable-null.json"))
//                .willSetStateTo("Pull Request Merge Hash - retry 2"));
//
//        githubApi.stubFor(
//            get(urlEqualTo("/repos/cloudbeers/yolo/pulls/2"))
//                .inScenario("Pull Request Merge Hash")
//                .whenScenarioStateIs("Pull Request Merge Hash - retry 2")
//                .willReturn(
//                    aResponse()
//                        .withHeader("Content-Type", "application/json; charset=utf-8")
//                        .withBodyFile("body-yolo-pulls-2-mergeable-true.json"))
//                .willSetStateTo("Pull Request Merge Hash - retry 2"));
//
//
        gitHub = GitHubBuilder.fromEnvironment()
            .withEndpoint("http://localhost:" + githubApi.port())
            .withRateLimitHandler(RateLimitHandler.FAIL)
            .build();

    }
}
