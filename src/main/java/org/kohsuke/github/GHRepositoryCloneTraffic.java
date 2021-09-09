package org.kohsuke.github;

import java.util.Collections;
import java.util.List;

/**
 * Repository clone statistics.
 *
 * @see GHRepository#getCloneTraffic() GHRepository#getCloneTraffic()
 */
public class GHRepositoryCloneTraffic extends GHRepositoryTraffic {
    private List<DailyInfo> clones;

    GHRepositoryCloneTraffic() {
    }

    GHRepositoryCloneTraffic(Integer count, Integer uniques, List<DailyInfo> clones) {
        super(count, uniques);
        this.clones = clones;
    }

    /**
     * Gets clones.
     *
     * @return the clones
     */
    public List<DailyInfo> getClones() {
        return Collections.unmodifiableList(clones);
    }

    public List<DailyInfo> getDailyInfo() {
        return getClones();
    }

    /**
     * The type DailyInfo.
     */
    public static class DailyInfo extends GHRepositoryTraffic.DailyInfo {
        DailyInfo() {
        }

        DailyInfo(String timestamp, int count, int uniques) {
            super(timestamp, count, uniques);
        }
    }
}
