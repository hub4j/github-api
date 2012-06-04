package org.kohsuke.github;

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
     * Alotted API call per hour.
     */
    public int limit;

    @Override
    public String toString() {
        return remaining+"/"+limit;
    }
}
