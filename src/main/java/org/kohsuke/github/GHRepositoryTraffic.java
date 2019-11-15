package org.kohsuke.github;

import java.util.Date;
import java.util.List;

/**
 * The type GHRepositoryTraffic.
 */
public abstract class GHRepositoryTraffic implements TrafficInfo {
    private int count;
    private int uniques;

    GHRepositoryTraffic() {
    }

    GHRepositoryTraffic(int count, int uniques) {
        this.count = count;
        this.uniques = uniques;
    }

    public int getCount() {
        return count;
    }

    public int getUniques() {
        return uniques;
    }

    /**
     * Gets daily info.
     *
     * @return the daily info
     */
    public abstract List<? extends DailyInfo> getDailyInfo();

    /**
     * The type DailyInfo.
     */
    public static abstract class DailyInfo implements TrafficInfo {
        private String timestamp;
        private int count;
        private int uniques;

        /**
         * Gets timestamp.
         *
         * @return the timestamp
         */
        public Date getTimestamp() {
            return GitHub.parseDate(timestamp);
        }

        public int getCount() {
            return count;
        }

        public int getUniques() {
            return uniques;
        }

        DailyInfo() {
        }

        DailyInfo(String timestamp, Integer count, Integer uniques) {
            this.timestamp = timestamp;
            this.count = count;
            this.uniques = uniques;
        }
    }
}
