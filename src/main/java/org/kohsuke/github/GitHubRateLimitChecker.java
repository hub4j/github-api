package org.kohsuke.github;

import java.io.IOException;
import java.io.InterruptedIOException;
import java.util.Objects;
import java.util.logging.Logger;

import javax.annotation.Nonnull;

/**
 * A GitHub API Rate Limit Checker called before each request. This class provides the basic infrastructure for calling
 * the appropriate {@link RateLimitChecker} for a request and retrying as many times as needed. This class supports more
 * complex throttling strategies and polling, but leaves the specifics to the {@link RateLimitChecker} implementations.
 * <p>
 * GitHub allots a certain number of requests to each user or application per period of time (usually per hour). The
 * number of requests remaining is returned in the response header and can also be requested using
 * {@link GitHub#getRateLimit()}. This requests per interval is referred to as the "rate limit".
 * </p>
 * <p>
 * GitHub prefers that clients stop before exceeding their rate limit rather than stopping after they exceed it. The
 * {@link RateLimitChecker} is called before each request to check the rate limit and wait if the checker criteria are
 * met.
 * </p>
 * <p>
 * Checking your rate limit using {@link GitHub#getRateLimit()} does not effect your rate limit, but each {@link GitHub}
 * instance will attempt to cache and reuse the last see rate limit rather than making a new request.
 * </p>
 */
class GitHubRateLimitChecker {

    @Nonnull
    private final RateLimitChecker core;

    @Nonnull
    private final RateLimitChecker search;

    @Nonnull
    private final RateLimitChecker graphql;

    @Nonnull
    private final RateLimitChecker integrationManifest;

    private static final Logger LOGGER = Logger.getLogger(GitHubRateLimitChecker.class.getName());

    GitHubRateLimitChecker() {
        this(RateLimitChecker.NONE, RateLimitChecker.NONE, RateLimitChecker.NONE, RateLimitChecker.NONE);
    }

    GitHubRateLimitChecker(@Nonnull RateLimitChecker core,
            @Nonnull RateLimitChecker search,
            @Nonnull RateLimitChecker graphql,
            @Nonnull RateLimitChecker integrationManifest) {
        this.core = Objects.requireNonNull(core);
        this.search = Objects.requireNonNull(search);
        this.graphql = Objects.requireNonNull(graphql);
        this.integrationManifest = Objects.requireNonNull(integrationManifest);
    }

    /**
     * Checks whether there is sufficient requests remaining within this client's rate limit quota to make the current
     * request.
     * <p>
     * This method does not do the actual check. Instead it select the appropriate {@link RateLimitChecker} and
     * {@link GHRateLimit.Record} for the current request's urlPath. If the {@link RateLimitChecker} for this the
     * current request's urlPath is {@link RateLimitChecker#NONE} the rate limit is not checked. If not, it calls
     * {@link RateLimitChecker#checkRateLimit(GHRateLimit.Record, long)}. which decides if the rate limit has been
     * exceeded and then sleeps for as long is it choose.
     * </p>
     * <p>
     * It is up to the {@link RateLimitChecker#checkRateLimit(GHRateLimit.Record, long)} which decide if the rate limit
     * has been exceeded. If it has, that method will sleep for as long is it chooses and then return {@code true}. If
     * not, that method will return {@code false}.
     * </p>
     * <p>
     * As long as {@link RateLimitChecker#checkRateLimit(GHRateLimit.Record, long)} returns {@code true}, this method
     * will request updated rate limit information and call
     * {@link RateLimitChecker#checkRateLimit(GHRateLimit.Record, long)} again. This looping allows implementers of
     * {@link RateLimitChecker#checkRateLimit(GHRateLimit.Record, long)} to apply any number of strategies to
     * controlling the speed at which requests are made. When it returns {@code false} this method will return and the
     * request will be sent.
     * </p>
     *
     * @param client
     *            the {@link GitHubClient} to check
     * @param request
     *            the {@link GitHubRequest} to check against
     * @throws IOException
     *             if there is an I/O error
     */
    void checkRateLimit(GitHubClient client, GitHubRequest request) throws IOException {
        RateLimitChecker guard = selectChecker(request.rateLimitSpecifier());
        if (guard == RateLimitChecker.NONE) {
            return;
        }

        // For the first rate limit, accept the current limit if a valid one is already present.
        GHRateLimit rateLimit = client.rateLimit(request.rateLimitSpecifier());
        GHRateLimit.Record rateLimitRecord = rateLimit.getRecordForUrlPath(request.rateLimitSpecifier());
        long waitCount = 0;
        try {
            while (guard.checkRateLimit(rateLimitRecord, waitCount)) {
                waitCount++;

                // When rate limit is exceeded, sleep for one additional second beyond when the
                // called {@link RateLimitChecker} sleeps.
                // Reset time is only accurate to the second, so adding a one second buffer for safety is a good idea.
                // This also keeps polling clients from querying too often.
                Thread.sleep(1000);

                // After the first wait, always request a new rate limit from the server.
                rateLimit = client.getRateLimit(request.rateLimitSpecifier());
                rateLimitRecord = rateLimit.getRecordForUrlPath(request.rateLimitSpecifier());
            }
        } catch (InterruptedException e) {
            throw (IOException) new InterruptedIOException(e.getMessage()).initCause(e);
        }
    }

    /**
     * Gets the appropriate {@link RateLimitChecker} for a particular url path. Similar to
     * {@link GHRateLimit#getRecordForUrlPath(GitHubRateLimitSpecifier)}.
     *
     * @param rateLimitSpecifier
     *            the rate limit endpoint specifier
     * @return the {@link RateLimitChecker} for a particular specifier
     */
    @Nonnull
    private RateLimitChecker selectChecker(@Nonnull GitHubRateLimitSpecifier rateLimitSpecifier) {
        if (rateLimitSpecifier == GitHubRateLimitSpecifier.NONE) {
            return RateLimitChecker.NONE;
        } else if (rateLimitSpecifier == GitHubRateLimitSpecifier.CORE) {
            return core;
        } else if (rateLimitSpecifier == GitHubRateLimitSpecifier.SEARCH) {
            return search;
        } else if (rateLimitSpecifier == GitHubRateLimitSpecifier.GRAPHQL) {
            return graphql;
        } else if (rateLimitSpecifier == GitHubRateLimitSpecifier.INTEGRATION_MANIFEST) {
            return integrationManifest;
        } else {
            throw new IllegalArgumentException("Unknown rate limit specifier: " + rateLimitSpecifier.toString());
        }
    }
}
