package org.kohsuke.github;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import javax.annotation.Nonnull;
import java.util.Objects;

/**
 * @author Kohsuke Kawaguchi
 */
class JsonRateLimit {

    @Nonnull
    final GHRateLimit resources;

    @JsonCreator
    private JsonRateLimit(@Nonnull @JsonProperty(value = "resources", required = true) GHRateLimit resources) {
        Objects.requireNonNull(resources);
        this.resources = resources;
    }
}
