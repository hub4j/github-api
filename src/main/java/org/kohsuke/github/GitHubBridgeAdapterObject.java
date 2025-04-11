package org.kohsuke.github;

import java.time.Instant;
import java.util.Date;

/**
 * Defines a base class that holds bridge adapter methods.
 *
 * @author Liam Newman
 */
abstract class GitHubBridgeAdapterObject {
    /**
     * Instantiates a new git hub bridge adapter object.
     */
    GitHubBridgeAdapterObject() {
    }

    // Used by bridge method to convert Instant to Date
    Object instantToDate(Instant value, Class<?> type) {
        if (value == null)
            return null;

        return Date.from(value);
    }
}
