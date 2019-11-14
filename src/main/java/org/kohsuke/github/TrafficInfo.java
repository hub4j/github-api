package org.kohsuke.github;

/**
 * The interface TrafficInfo.
 *
 * @author Kohsuke Kawaguchi
 */
public interface TrafficInfo {
    /**
     * Total count of hits.
     *
     * @return the count
     */
    int getCount();

    /**
     * Unique visitors.
     *
     * @return the uniques
     */
    int getUniques();
}
