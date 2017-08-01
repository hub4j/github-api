package org.kohsuke.github;

import java.util.Date;

public abstract class GHRepositoryTrafficInfo {
    private Integer count;
    private Integer uniques;

    public GHRepositoryTrafficInfo() {
    }

    public GHRepositoryTrafficInfo(Integer count, Integer uniques) {
        this.count = count;
        this.uniques = uniques;
    }

    public Integer getCount() {
        return count;
    }

    public Integer getUniques() {
        return uniques;
    }

    public static abstract class DayInfo {
        private Date timestamp;
        private Integer count;
        private Integer uniques;

        public Date getTimestamp() {
            return timestamp;
        }

        public Integer getCount() {
            return count;
        }

        public Integer getUniques() {
            return uniques;
        }

        public DayInfo() {
        }

        public DayInfo(String timestamp, Integer count, Integer uniques) {
            this.timestamp = GitHub.parseDate(timestamp);
            this.count = count;
            this.uniques = uniques;
        }

        public DayInfo(Date timestamp, Integer count, Integer uniques) {
            this.timestamp = timestamp;
            this.count = count;
            this.uniques = uniques;
        }
    }
}
