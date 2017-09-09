package org.kohsuke.github;

import java.util.Date;
import java.util.List;

public abstract class GHRepositoryTrafficInfo implements TrafficInfo {
    private int count;
    private int uniques;

    /*package*/ GHRepositoryTrafficInfo() {
    }

    /*package*/ GHRepositoryTrafficInfo(int count, int uniques) {
        this.count = count;
        this.uniques = uniques;
    }

    public int getCount() {
        return count;
    }

    public int getUniques() {
        return uniques;
    }

    public abstract List<? extends DayInfo> getDailyInfo();

    public static abstract class DayInfo implements TrafficInfo {
        private Date timestamp;
        private int count;
        private int uniques;

        public Date getTimestamp() {
            return timestamp;
        }

        public int getCount() {
            return count;
        }

        public int getUniques() {
            return uniques;
        }

        /*package*/ DayInfo() {
        }

        /*package*/ DayInfo(String timestamp, Integer count, Integer uniques) {
            this.timestamp = GitHub.parseDate(timestamp);
            this.count = count;
            this.uniques = uniques;
        }

        /*package*/ DayInfo(Date timestamp, Integer count, Integer uniques) {
            this.timestamp = timestamp;
            this.count = count;
            this.uniques = uniques;
        }
    }
}
