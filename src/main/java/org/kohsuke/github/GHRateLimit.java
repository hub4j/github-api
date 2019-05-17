package org.kohsuke.github;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

import java.util.Date;

/**
 * Rate limit.
 * @author Kohsuke Kawaguchi
 */
public class GHRateLimit {
    /**
     * Remaining calls that can be made.
     */
    public int remaining;
    /**
     * Allotted API call per hour.
     */
    public int limit;

    /**
     * The time at which the current rate limit window resets in UTC epoch seconds.
     */
    public Date reset;

    /**
     * Non-epoch date
     */
    @SuppressFBWarnings(value = "UWF_FIELD_NOT_INITIALIZED_IN_CONSTRUCTOR", 
            justification = "The value comes from JSON deserialization")
    public Date getResetDate() {
        return new Date(reset.getTime() * 1000);
    }

    @Override
    public String toString() {
        return "GHRateLimit{" +
                "remaining=" + remaining +
                ", limit=" + limit +
                ", resetDate=" + getResetDate() +
                '}';
    }
}
