package org.kohsuke.github;

import java.util.Date;
import java.util.List;

/**
 * Repository view statistics.
 *
 * @see GHRepository#getViews()
 */
public class GHRepositoryViews extends GHRepositoryTrafficInfo {
    private List<DayViews> views;

    /*package*/ GHRepositoryViews() {
    }

    /*package*/ GHRepositoryViews(int count, int uniques, List<DayViews> views) {
        super(count, uniques);
        this.views = views;
    }

    public List<DayViews> getViews() {
        return views;
    }

    public List<DayViews> getDailyInfo() {
        return getViews();
    }

    public static class DayViews extends GHRepositoryTrafficInfo.DayInfo {
        /*package*/ DayViews() {
        }

        /*package*/ DayViews(String timestamp, int count, int uniques) {
            super(timestamp, count, uniques);
        }

        /*package*/ DayViews(Date timestamp, int count, int uniques) {
            super(timestamp, count, uniques);
        }
    }
}
