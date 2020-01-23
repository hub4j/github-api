package org.kohsuke.github;

import org.junit.Before;
import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.logging.Handler;
import java.util.logging.Logger;
import java.util.logging.StreamHandler;

import static org.hamcrest.core.IsInstanceOf.instanceOf;

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
        GHRepository repo = getRepository();
        try {
            repo.getBranch("test/timeout");
            fail();
        } catch (Exception e) {
            assertThat(e, instanceOf(HttpException.class));
            String capturedLog = getTestCapturedLog();
            assertTrue(capturedLog.contains("will try 2 more time"));
            assertTrue(capturedLog.contains("will try 1 more time"));
        }
    }
}
