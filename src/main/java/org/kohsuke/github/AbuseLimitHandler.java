package org.kohsuke.github;

import java.io.IOException;
import java.io.InterruptedIOException;
import java.net.HttpURLConnection;

/**
 * Pluggable strategy to determine what to do when the API abuse limit is hit.
 *
 * @author Kohsuke Kawaguchi
 * @see GitHubBuilder#withAbuseLimitHandler(AbuseLimitHandler)
 * @see <a href="https://developer.github.com/v3/#abuse-rate-limits">documentation</a>
 * @see RateLimitHandler
 */
public abstract class AbuseLimitHandler {
    /**
     * Called when the library encounters HTTP error indicating that the API abuse limit is reached.
     *
     * <p>
     * Any exception thrown from this method will cause the request to fail, and the caller of github-api
     * will receive an exception. If this method returns normally, another request will be attempted.
     * For that to make sense, the implementation needs to wait for some time.
     *
     * @see <a href="https://developer.github.com/v3/#abuse-rate-limits">API documentation from GitHub</a>
     * @param e
     *      Exception from Java I/O layer. If you decide to fail the processing, you can throw
     *      this exception (or wrap this exception into another exception and throw it.)
     * @param uc
     *      Connection that resulted in an error. Useful for accessing other response headers.
     */
    public abstract void onError(IOException e, HttpURLConnection uc) throws IOException;

    /**
     * Wait until the API abuse "wait time" is passed.
     */
    public static final AbuseLimitHandler WAIT = new AbuseLimitHandler() {
        @Override
        public void onError(IOException e, HttpURLConnection uc) throws IOException {
            try {
                Thread.sleep(parseWaitTime(uc));
            } catch (InterruptedException _) {
                throw (InterruptedIOException)new InterruptedIOException().initCause(e);
            }
        }

        private long parseWaitTime(HttpURLConnection uc) {
            String v = uc.getHeaderField("Retry-After");
            if (v==null)    return 60 * 1000;   // can't tell, return 1 min

            return Math.max(1000, Long.parseLong(v)*1000);
        }
    };

    /**
     * Fail immediately.
     */
    public static final AbuseLimitHandler FAIL = new AbuseLimitHandler() {
        @Override
        public void onError(IOException e, HttpURLConnection uc) throws IOException {
            throw (IOException)new IOException("Abust limit reached").initCause(e);
        }
    };
}
