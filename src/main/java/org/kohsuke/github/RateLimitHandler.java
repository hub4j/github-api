/*
 * GitHub API for Java
 * Copyright (C) 2009-2016 SonarSource SA
 * mailto:contact AT sonarsource DOT com
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */
package org.kohsuke.github;

import java.io.IOException;
import java.io.InterruptedIOException;
import java.net.HttpURLConnection;

/**
 * Pluggable strategy to determine what to do when the API rate limit is reached.
 *
 * @author Kohsuke Kawaguchi
 * @see GitHubBuilder#withRateLimitHandler(RateLimitHandler)
 * @see AbuseLimitHandler
 */
public abstract class RateLimitHandler {
    /**
     * Called when the library encounters HTTP error indicating that the API rate limit is reached.
     *
     * <p>
     * Any exception thrown from this method will cause the request to fail, and the caller of github-api
     * will receive an exception. If this method returns normally, another request will be attempted.
     * For that to make sense, the implementation needs to wait for some time.
     *
     * @see <a href="https://developer.github.com/v3/#rate-limiting">API documentation from GitHub</a>
     * @param e
     *      Exception from Java I/O layer. If you decide to fail the processing, you can throw
     *      this exception (or wrap this exception into another exception and throw it.)
     * @param uc
     *      Connection that resulted in an error. Useful for accessing other response headers.
     */
    public abstract void onError(IOException e, HttpURLConnection uc) throws IOException;

    /**
     * Block until the API rate limit is reset. Useful for long-running batch processing.
     */
    public static final RateLimitHandler WAIT = new RateLimitHandler() {
        @Override
        public void onError(IOException e, HttpURLConnection uc) throws IOException {
            try {
                Thread.sleep(parseWaitTime(uc));
            } catch (InterruptedException _) {
                throw (InterruptedIOException)new InterruptedIOException().initCause(e);
            }
        }

        private long parseWaitTime(HttpURLConnection uc) {
            String v = uc.getHeaderField("X-RateLimit-Reset");
            if (v==null)    return 10000;   // can't tell

            return Math.max(10000, Long.parseLong(v)*1000 - System.currentTimeMillis());
        }
    };

    /**
     * Fail immediately.
     */
    public static final RateLimitHandler FAIL = new RateLimitHandler() {
        @Override
        public void onError(IOException e, HttpURLConnection uc) throws IOException {
            throw (IOException)new IOException("API rate limit reached").initCause(e);
        }
    };
}
