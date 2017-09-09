package org.kohsuke.github;

import java.util.Date;
import java.util.List;

/**
 * Repository view statistics.
 *
 * @see GHRepository#getViewTraffic()
 */
public class GHRepositoryViewTraffic extends GHRepositoryTrafficInfo {
    private List<Daily> views;

    /*package*/ GHRepositoryViewTraffic() {
    }

    /*package*/ GHRepositoryViewTraffic(int count, int uniques, List<Daily> views) {
        super(count, uniques);
        this.views = views;
    }

    public List<Daily> getViews() {
        return views;
    }

    public List<Daily> getDailyInfo() {
        return getViews();
    }

    public static class Daily extends GHRepositoryTrafficInfo.DayInfo {
        /*package*/ Daily() {
        }

        /*package*/ Daily(String timestamp, int count, int uniques) {
            super(timestamp, count, uniques);
        }

        /*package*/ Daily(Date timestamp, int count, int uniques) {
            super(timestamp, count, uniques);
        }
    }
}
