package org.kohsuke.github;

/**
 * The interface TrafficInfo.
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
