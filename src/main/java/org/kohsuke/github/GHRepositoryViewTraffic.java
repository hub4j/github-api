package org.kohsuke.github;

import java.util.Collections;
import java.util.List;

// TODO: Auto-generated Javadoc
/**
 * Repository view statistics.
 *
 * @see GHRepository#getViewTraffic() GHRepository#getViewTraffic()
 */
public class GHRepositoryViewTraffic extends GHRepositoryTraffic {
    private List<DailyInfo> views;

    /**
     * Instantiates a new GH repository view traffic.
     */
    GHRepositoryViewTraffic() {
    }

    /**
     * Instantiates a new GH repository view traffic.
     *
     * @param count the count
     * @param uniques the uniques
     * @param views the views
     */
    GHRepositoryViewTraffic(int count, int uniques, List<DailyInfo> views) {
        super(count, uniques);
        this.views = views;
    }

    /**
     * Gets views.
     *
     * @return the views
     */
    public List<DailyInfo> getViews() {
        return Collections.unmodifiableList(views);
    }

    /**
     * Gets the daily info.
     *
     * @return the daily info
     */
    public List<DailyInfo> getDailyInfo() {
        return getViews();
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
