package org.kohsuke.github;

import java.io.IOException;
import java.io.InterruptedIOException;
import java.util.Objects;
import java.util.logging.Logger;

import javax.annotation.Nonnull;

/**
 * A GitHub API Rate Limit Checker called before each request.
 *
 * <p>
 * GitHub allots a certain number of requests to each user or application per period of time. The number of requests
 * remaining and the time when the number will be reset is returned in the response header and can also be requested
 * using {@link GitHub#getRateLimit()}. The "requests per interval" is referred to as the "rate limit".
 * </p>
 * <p>
 * Different parts of the GitHub API have separate rate limits, but most of REST API uses {@link RateLimitTarget#CORE}.
 * Checking your rate limit using {@link GitHub#getRateLimit()} does not effect your rate limit. GitHub prefers that
 * clients stop before exceeding their rate limit rather than stopping after they exceed it.
 * </p>
 * <p>
 * This class provides the infrastructure for calling the appropriate {@link RateLimitChecker} before each request and
 * retrying than call many times as needed. Each {@link RateLimitChecker} decides whether to wait and for how long. This
 * allows for a wide range of {@link RateLimitChecker} implementations, including complex throttling strategies and
 * polling.
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
     * Constructs a new {@link GitHubRateLimitChecker} with a new checker for a particular target.
     *
     * Only one {@link RateLimitChecker} is allowed per target.
     *
     * @param checker
     *            the {@link RateLimitChecker} to apply.
     * @param rateLimitTarget
     *            the {@link RateLimitTarget} for this checker. If {@link RateLimitTarget#NONE}, checker will be ignored
     *            and no change will be made.
     * @return a new {@link GitHubRateLimitChecker}
     */
    GitHubRateLimitChecker with(@Nonnull RateLimitChecker checker, @Nonnull RateLimitTarget rateLimitTarget) {
        return new GitHubRateLimitChecker(rateLimitTarget == RateLimitTarget.CORE ? checker : core,
                rateLimitTarget == RateLimitTarget.SEARCH ? checker : search,
                rateLimitTarget == RateLimitTarget.GRAPHQL ? checker : graphql,
                rateLimitTarget == RateLimitTarget.INTEGRATION_MANIFEST ? checker : integrationManifest);
    }

    /**
     * Checks whether there is sufficient requests remaining within this client's rate limit quota to make the current
     * request.
     * <p>
     * This method does not do the actual check. Instead it selects the appropriate {@link RateLimitChecker} and
     * {@link GHRateLimit.Record} for the current request's {@link RateLimitTarget}. It then calls
     * {@link RateLimitChecker#checkRateLimit(GHRateLimit.Record, long)}.
     * </p>
     * <p>
     * It is up to {@link RateLimitChecker#checkRateLimit(GHRateLimit.Record, long)} to which decide if the rate limit
     * has been exceeded. If it has, {@link RateLimitChecker#checkRateLimit(GHRateLimit.Record, long)} will sleep for as
     * long is it chooses and then return {@code true}. If not, that method will return {@code false}.
     * </p>
     * <p>
     * As long as {@link RateLimitChecker#checkRateLimit(GHRateLimit.Record, long)} returns {@code true}, this method
     * will request updated rate limit information and call
     * {@link RateLimitChecker#checkRateLimit(GHRateLimit.Record, long)} again. This looping allows different
     * {@link RateLimitChecker} implementations to apply any number of strategies to controlling the speed at which
     * requests are made.
     * </p>
     * <p>
     * When the {@link RateLimitChecker} returns {@code false} this method will return and the request processing will
     * continue.
     * </p>
     * <p>
     * If the {@link RateLimitChecker} for this the current request's urlPath is {@link RateLimitChecker#NONE} the rate
     * limit is not checked.
     * </p>
     *
     * @param client
     *            the {@link GitHubClient} to check
     * @param rateLimitTarget
     *            the {@link RateLimitTarget} to check against
     * @throws IOException
     *             if there is an I/O error
     */
    void checkRateLimit(GitHubClient client, @Nonnull RateLimitTarget rateLimitTarget) throws IOException {
        RateLimitChecker guard = selectChecker(rateLimitTarget);
        if (guard == RateLimitChecker.NONE) {
            return;
        }

        // For the first rate limit, accept the current limit if a valid one is already present.
        GHRateLimit rateLimit = client.rateLimit(rateLimitTarget);
        GHRateLimit.Record rateLimitRecord = rateLimit.getRecord(rateLimitTarget);
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
                rateLimit = client.getRateLimit(rateLimitTarget);
                rateLimitRecord = rateLimit.getRecord(rateLimitTarget);
            }
        } catch (InterruptedException e) {
            throw (IOException) new InterruptedIOException(e.getMessage()).initCause(e);
        }
    }

    /**
     * Gets the appropriate {@link RateLimitChecker} for a particular target.
     *
     * Analogous with {@link GHRateLimit#getRecord(RateLimitTarget)}.
     *
     * @param rateLimitTarget
     *            the rate limit to check
     * @return the {@link RateLimitChecker} for a particular target
     */
    @Nonnull
    private RateLimitChecker selectChecker(@Nonnull RateLimitTarget rateLimitTarget) {
        if (rateLimitTarget == RateLimitTarget.NONE) {
            return RateLimitChecker.NONE;
        } else if (rateLimitTarget == RateLimitTarget.CORE) {
            return core;
        } else if (rateLimitTarget == RateLimitTarget.SEARCH) {
            return search;
        } else if (rateLimitTarget == RateLimitTarget.GRAPHQL) {
            return graphql;
        } else if (rateLimitTarget == RateLimitTarget.INTEGRATION_MANIFEST) {
            return integrationManifest;
        } else {
            throw new IllegalArgumentException("Unknown rate limit target: " + rateLimitTarget.toString());
        }
    }
}
