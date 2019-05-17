package org.kohsuke.github;

import java.util.Date;
import java.util.List;

public abstract class GHRepositoryTraffic implements TrafficInfo {
    private int count;
    private int uniques;

    /*package*/ GHRepositoryTraffic() {
    }

    /*package*/ GHRepositoryTraffic(int count, int uniques) {
        this.count = count;
        this.uniques = uniques;
    }

    public int getCount() {
        return count;
    }

    public int getUniques() {
        return uniques;
    }

    public abstract List<? extends DailyInfo> getDailyInfo();

    public static abstract class DailyInfo implements TrafficInfo {
        private String timestamp;
        private int count;
        private int uniques;

        public Date getTimestamp() {
            return GitHub.parseDate(timestamp);
        }

        public int getCount() {
            return count;
        }

        public int getUniques() {
            return uniques;
        }

        /*package*/ DailyInfo() {
        }

        /*package*/ DailyInfo(String timestamp, Integer count, Integer uniques) {
            this.timestamp = timestamp;
            this.count = count;
            this.uniques = uniques;
        }
    }
}
