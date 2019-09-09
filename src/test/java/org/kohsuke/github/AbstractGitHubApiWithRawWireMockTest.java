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

import static com.github.tomakehurst.wiremock.client.WireMock.proxyAllTo;

/**
 * @author Kohsuke Kawaguchi
 */
public abstract class AbstractGitHubApiWithRawWireMockTest extends AbstractGitHubApiWireMockTest {

    @Rule
    public WireMockRule githubRaw = factory.getRule(WireMockConfiguration.options()
        .dynamicPort()
        .usingFilesUnderClasspath(baseFilesClassPath + "/raw")
    );

    @Before
    public void prepareMockRawGitHub() throws Exception {
        new File(baseRecordPath + "/raw/mappings").mkdirs();
        new File(baseRecordPath + "/raw/__files").mkdirs();

        githubRaw.stubFor(proxyAllTo("https://raw.githubusercontent.com/").atPriority(10));

        githubRaw.enableRecordMappings(new SingleRootFileSource(baseRecordPath + "/raw/mappings"),
            new SingleRootFileSource(baseRecordPath + "/raw/__files"));
    }
}
