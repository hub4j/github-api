package org.kohsuke.github;

import java.util.List;

public class GHRepositoryViews{
    private Integer count;
    private Integer uniques;
    private List<DayViews> views;

    public GHRepositoryViews() {
    }

    public GHRepositoryViews(Integer count, Integer uniques, List<DayViews> views) {
        this.count = count;
        this.uniques = uniques;
        this.views = views;
    }

    public Integer getCount() {
        return count;
    }

    public Integer getUniques() {
        return uniques;
    }

    public List<DayViews> getViews() {
        return views;
    }

    public static class DayViews {
        private String timestamp;
        private Integer count;
        private Integer uniques;

        public String getTimestamp() {
            return timestamp;
        }

        public Integer getCount() {
            return count;
        }

        public Integer getUniques() {
            return uniques;
        }

        public DayViews() {
        }

        public DayViews(String timestamp, Integer count, Integer uniques) {
            this.timestamp = timestamp;
            this.count = count;
            this.uniques = uniques;
        }
    }
}
