package org.kohsuke.github;

import com.infradna.tool.bridge_method_injector.WithBridgeMethods;

import java.time.Instant;
import java.util.Date;
import java.util.List;

// TODO: Auto-generated Javadoc
/**
 * The type GHRepositoryTraffic.
 */
public abstract class GHRepositoryTraffic extends GitHubBridgeAdapterObject implements TrafficInfo {
    /**
     * The type DailyInfo.
     */
    public static abstract class DailyInfo implements TrafficInfo {
        private int count;
        private String timestamp;
        private int uniques;

        /**
         * Instantiates a new daily info.
         */
        DailyInfo() {
        }

        /**
         * Instantiates a new daily info.
         *
         * @param timestamp
         *            the timestamp
         * @param count
         *            the count
         * @param uniques
         *            the uniques
         */
        DailyInfo(String timestamp, Integer count, Integer uniques) {
            this.timestamp = timestamp;
            this.count = count;
            this.uniques = uniques;
        }

        /**
         * Gets the count.
         *
         * @return the count
         */
        public int getCount() {
            return count;
        }

        /**
         * Gets timestamp.
         *
         * @return the timestamp
         */
        @WithBridgeMethods(value = Date.class, adapterMethod = "instantToDate")
        public Instant getTimestamp() {
            return GitHubClient.parseInstant(timestamp);
        }

        /**
         * Gets the uniques.
         *
         * @return the uniques
         */
        public int getUniques() {
            return uniques;
        }
    }
    private int count;

    private int uniques;

    /**
     * Instantiates a new GH repository traffic.
     */
    GHRepositoryTraffic() {
    }

    /**
     * Instantiates a new GH repository traffic.
     *
     * @param count
     *            the count
     * @param uniques
     *            the uniques
     */
    GHRepositoryTraffic(int count, int uniques) {
        this.count = count;
        this.uniques = uniques;
    }

    /**
     * Gets the count.
     *
     * @return the count
     */
    public int getCount() {
        return count;
    }

    /**
     * Gets daily info.
     *
     * @return the daily info
     */
    public abstract List<? extends DailyInfo> getDailyInfo();

    /**
     * Gets the uniques.
     *
     * @return the uniques
     */
    public int getUniques() {
        return uniques;
    }
}
