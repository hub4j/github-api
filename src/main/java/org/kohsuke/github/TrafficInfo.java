package org.kohsuke.github;

/**
 * @author Kohsuke Kawaguchi
 */
public interface TrafficInfo {
    /**
     * Total count of hits.
     */
    int getCount();

    /**
     * Unique visitors.
     */
    int getUniques();
}
