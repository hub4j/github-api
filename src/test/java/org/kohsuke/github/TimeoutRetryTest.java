package org.kohsuke.github;

import com.github.tomakehurst.wiremock.http.Fault;
import com.github.tomakehurst.wiremock.stubbing.Scenario;
import org.junit.Before;
import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.logging.Handler;
import java.util.logging.Logger;
import java.util.logging.StreamHandler;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.hamcrest.Matchers.*;

/**
 * @author Victor Martinez
 */
public class TimeoutRetryTest extends AbstractGitHubWireMockTest {

    private static Logger log = Logger.getLogger(Requester.class.getName()); // matches the logger in the affected class
    private static OutputStream logCapturingStream;
    private static StreamHandler customLogHandler;

    protected GHRepository getRepository() throws IOException {
        return getRepository(gitHub);
    }

    private GHRepository getRepository(GitHub gitHub) throws IOException {
        return gitHub.getOrganization("github-api-test-org").getRepository("github-api");
    }

    @Before
    public void attachLogCapturer() {
        logCapturingStream = new ByteArrayOutputStream();
        Handler[] handlers = log.getParent().getHandlers();
        customLogHandler = new StreamHandler(logCapturingStream, handlers[0].getFormatter());
        log.addHandler(customLogHandler);
    }

    public String getTestCapturedLog() throws IOException {
        customLogHandler.flush();
        return logCapturingStream.toString();
    }

    // Issue #539
    @Test
    public void testSocketConnectionAndRetry() throws Exception {
        // CONNECTION_RESET_BY_PEER errors result in two requests each
        // to get this failure for "3" tries we have to do 6 queries.
        this.mockGitHub.apiServer()
                .stubFor(get(urlMatching(".+/branches/test/timeout"))
                        .willReturn(aResponse().withFault(Fault.CONNECTION_RESET_BY_PEER)));

        GHRepository repo = getRepository();
        int baseRequestCount = this.mockGitHub.getRequestCount();
        try {
            repo.getBranch("test/timeout");
            fail();
        } catch (Exception e) {
            assertThat(e, instanceOf(HttpException.class));
        }

        String capturedLog = getTestCapturedLog();
        assertTrue(capturedLog.contains("will try 2 more time"));
        assertTrue(capturedLog.contains("will try 1 more time"));

        assertThat(this.mockGitHub.getRequestCount(), equalTo(baseRequestCount + 6));
    }

    // Issue #539
    @Test
    public void testSocketConnectionAndRetry_StatusCode() throws Exception {
        // CONNECTION_RESET_BY_PEER errors result in two requests each
        // to get this failure for "3" tries we have to do 6 queries.
        this.mockGitHub.apiServer()
                .stubFor(get(urlMatching(".+/branches/test/timeout"))
                        .willReturn(aResponse().withFault(Fault.CONNECTION_RESET_BY_PEER)));

        int baseRequestCount = this.mockGitHub.getRequestCount();
        try {
            // status code is a different code path that should also be covered by this.
            gitHub.createRequest()
                    .withUrlPath("/repos/github-api-test-org/github-api/branches/test/timeout")
                    .fetchHttpStatusCode();
            fail();
        } catch (Exception e) {
            assertThat(e, instanceOf(HttpException.class));
        }

        String capturedLog = getTestCapturedLog();
        assertTrue(capturedLog.contains("will try 2 more time"));
        assertTrue(capturedLog.contains("will try 1 more time"));

        assertThat(this.mockGitHub.getRequestCount(), equalTo(baseRequestCount + 6));
    }

    @Test
    public void testSocketConnectionAndRetry_Success() throws Exception {
        // CONNECTION_RESET_BY_PEER errors result in two requests each
        // to get this failure for "3" tries we have to do 6 queries.
        // If there are only 5 errors we succeed.
        this.mockGitHub.apiServer()
                .stubFor(get(urlMatching(".+/branches/test/timeout")).atPriority(0)
                        .inScenario("Retry")
                        .whenScenarioStateIs(Scenario.STARTED)
                        .willReturn(aResponse().withFault(Fault.CONNECTION_RESET_BY_PEER)))
                .setNewScenarioState("Retry-1");
        this.mockGitHub.apiServer()
                .stubFor(get(urlMatching(".+/branches/test/timeout")).atPriority(0)
                        .inScenario("Retry")
                        .whenScenarioStateIs("Retry-1")
                        .willReturn(aResponse().withFault(Fault.CONNECTION_RESET_BY_PEER)))
                .setNewScenarioState("Retry-2");
        this.mockGitHub.apiServer()
                .stubFor(get(urlMatching(".+/branches/test/timeout")).atPriority(0)
                        .inScenario("Retry")
                        .whenScenarioStateIs("Retry-2")
                        .willReturn(aResponse().withFault(Fault.CONNECTION_RESET_BY_PEER)))
                .setNewScenarioState("Retry-3");
        this.mockGitHub.apiServer()
                .stubFor(get(urlMatching(".+/branches/test/timeout")).atPriority(0)
                        .inScenario("Retry")
                        .whenScenarioStateIs("Retry-3")
                        .willReturn(aResponse().withFault(Fault.CONNECTION_RESET_BY_PEER)))
                .setNewScenarioState("Retry-4");
        this.mockGitHub.apiServer()
                .stubFor(get(urlMatching(".+/branches/test/timeout")).atPriority(0)
                        .atPriority(0)
                        .inScenario("Retry")
                        .whenScenarioStateIs("Retry-4")
                        .willReturn(aResponse().withFault(Fault.CONNECTION_RESET_BY_PEER)))
                .setNewScenarioState("Retry-5");

        GHRepository repo = getRepository();
        int baseRequestCount = this.mockGitHub.getRequestCount();
        GHBranch branch = repo.getBranch("test/timeout");
        assertThat(branch, notNullValue());
        String capturedLog = getTestCapturedLog();
        assertTrue(capturedLog.contains("will try 2 more time"));
        assertTrue(capturedLog.contains("will try 1 more time"));

        assertThat(this.mockGitHub.getRequestCount(), equalTo(baseRequestCount + 6));

    }
}
