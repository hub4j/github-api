package org.kohsuke.github;

import java.io.IOException;
import java.io.InterruptedIOException;
import java.util.Objects;

import javax.annotation.Nonnull;

class GitHubRateLimitChecker {

    @Nonnull
    private final RateLimitChecker core;

    @Nonnull
    private final RateLimitChecker search;

    @Nonnull
    private final RateLimitChecker graphql;

    @Nonnull
    private final RateLimitChecker integrationManifest;

    GitHubRateLimitChecker() {
        this(RateLimitChecker.NONE, RateLimitChecker.NONE, RateLimitChecker.NONE, RateLimitChecker.NONE);
    }

    GitHubRateLimitChecker(@Nonnull RateLimitChecker core,
            @Nonnull RateLimitChecker search,
            @Nonnull RateLimitChecker graphql,
            @Nonnull RateLimitChecker integrationManifest) {
        this.core = Objects.requireNonNull(core);

        // for now only support rate limiting on core
        // remove these asserts when that changes
        assert search == RateLimitChecker.NONE;
        assert graphql == RateLimitChecker.NONE;
        assert integrationManifest == RateLimitChecker.NONE;

        this.search = Objects.requireNonNull(search);
        this.graphql = Objects.requireNonNull(graphql);
        this.integrationManifest = Objects.requireNonNull(integrationManifest);
    }

    void checkRateLimit(GitHubClient client, GitHubRequest request) throws IOException {
        RateLimitChecker guard = selectChecker(request);
        if (guard == RateLimitChecker.NONE) {
            return;
        }

        // For the first rate limit, accept the current limit if a valid one is already present.
        GHRateLimit rateLimit = client.rateLimit();
        GHRateLimit.Record rateLimitRecord = rateLimit.selectRateLimitRecord(request.urlPath());
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
                rateLimit = client.getRateLimit();
                rateLimitRecord = rateLimit.selectRateLimitRecord(request.urlPath());
            }
        } catch (InterruptedException e) {
            throw (IOException) new InterruptedIOException(e.getMessage()).initCause(e);
        }
    }

    private RateLimitChecker selectChecker(GitHubRequest request) {
        if (request.urlPath().equals("/rate_limit")) {
            return RateLimitChecker.NONE;
        } else if (request.urlPath().startsWith("/search")) {
            return search;
        } else if (request.urlPath().startsWith("/graphql")) {
            return graphql;
        } else if (request.urlPath().startsWith("/app-manifests")) {
            return integrationManifest;
        } else {
            return core;
        }
    }
}
