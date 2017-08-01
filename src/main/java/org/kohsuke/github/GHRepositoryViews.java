package org.kohsuke.github;

import java.util.Date;
import java.util.List;

public class GHRepositoryViews extends GHRepositoryTrafficInfo {
    private List<DayViews> views;

    public GHRepositoryViews() {
    }

    public GHRepositoryViews(Integer count, Integer uniques, List<DayViews> views) {
        super(count, uniques);
        this.views = views;
    }

    public List<DayViews> getViews() {
        return views;
    }

    public static class DayViews extends GHRepositoryTrafficInfo.DayInfo {
        public DayViews() {
        }

        public DayViews(String timestamp, Integer count, Integer uniques) {
            super(timestamp, count, uniques);
        }

        public DayViews(Date timestamp, Integer count, Integer uniques) {
            super(timestamp, count, uniques);
        }
    }
}
