package org.kohsuke.github;

import java.util.List;

/**
 * Repository view statistics.
 *
 * @see GHRepository#getViewTraffic()
 */
public class GHRepositoryViewTraffic extends GHRepositoryTraffic {
    private List<DailyInfo> views;

    GHRepositoryViewTraffic() {
    }

    GHRepositoryViewTraffic(int count, int uniques, List<DailyInfo> views) {
        super(count, uniques);
        this.views = views;
    }

    public List<DailyInfo> getViews() {
        return views;
    }

    public List<DailyInfo> getDailyInfo() {
        return getViews();
    }

    public static class DailyInfo extends GHRepositoryTraffic.DailyInfo {
        DailyInfo() {
        }

        DailyInfo(String timestamp, int count, int uniques) {
            super(timestamp, count, uniques);
        }
    }
}
