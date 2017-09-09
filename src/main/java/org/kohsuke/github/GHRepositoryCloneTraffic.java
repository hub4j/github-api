package org.kohsuke.github;

import java.util.Date;
import java.util.List;

/**
 * Repository clone statistics.
 *
 * @see GHRepository#getCloneTraffic()
 */
public class GHRepositoryCloneTraffic extends GHRepositoryTrafficInfo {
    private List<DayInfo> clones;

    /*package*/ GHRepositoryCloneTraffic() {
    }

    /*package*/ GHRepositoryCloneTraffic(Integer count, Integer uniques, List<DayInfo> clones) {
        super(count, uniques);
        this.clones = clones;
    }

    public List<DayInfo> getClones() {
        return clones;
    }

    public List<DayInfo> getDailyInfo() {
        return getClones();
    }

    public static class DayInfo extends GHRepositoryTrafficInfo.DayInfo {
        /*package*/ DayInfo() {
        }

        /*package*/ DayInfo(String timestamp, int count, int uniques) {
            super(timestamp, count, uniques);
        }

        /*package*/ DayInfo(Date timestamp, int count, int uniques) {
            super(timestamp, count, uniques);
        }
    }
}
