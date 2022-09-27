package org.kohsuke.github;

import java.util.Collections;
import java.util.List;

// TODO: Auto-generated Javadoc
/**
 * Repository clone statistics.
 *
 * @see GHRepository#getCloneTraffic() GHRepository#getCloneTraffic()
 */
public class GHRepositoryCloneTraffic extends GHRepositoryTraffic {
    private List<DailyInfo> clones;

    /**
     * Instantiates a new GH repository clone traffic.
     */
    GHRepositoryCloneTraffic() {
    }

    /**
     * Instantiates a new GH repository clone traffic.
     *
     * @param count the count
     * @param uniques the uniques
     * @param clones the clones
     */
    GHRepositoryCloneTraffic(Integer count, Integer uniques, List<DailyInfo> clones) {
        super(count, uniques);
        this.clones = clones;
    }

    /**
     * Gets clones.
     *
     * @return the clones
     */
    public List<DailyInfo> getClones() {
        return Collections.unmodifiableList(clones);
    }

    /**
     * Gets the daily info.
     *
     * @return the daily info
     */
    public List<DailyInfo> getDailyInfo() {
        return getClones();
    }

    /**
     * The type DailyInfo.
     */
    public static class DailyInfo extends GHRepositoryTraffic.DailyInfo {
        
        /**
         * Instantiates a new daily info.
         */
        DailyInfo() {
        }

        /**
         * Instantiates a new daily info.
         *
         * @param timestamp the timestamp
         * @param count the count
         * @param uniques the uniques
         */
        DailyInfo(String timestamp, int count, int uniques) {
            super(timestamp, count, uniques);
        }
    }
}
