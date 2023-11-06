package org.kohsuke.github.extras.authorization;

import java.time.Instant;

class RefreshResult {
    private final Instant validUntil;
    private final String jwt;

    RefreshResult(Instant validUntil, String jwt) {
        this.validUntil = validUntil;
        this.jwt = jwt;
    }

    Instant getValidUntil() {
        return this.validUntil;
    }

    String getJwt() {
        return this.jwt;
    }
}
