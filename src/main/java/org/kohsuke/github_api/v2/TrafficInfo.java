package org.kohsuke.github_api.v2;

// TODO: Auto-generated Javadoc
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
