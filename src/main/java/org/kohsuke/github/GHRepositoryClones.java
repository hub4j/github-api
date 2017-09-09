package org.kohsuke.github;

import java.util.Date;
import java.util.List;

/**
 * Repository clone statistics.
 *
 * @see GHRepository#getClones()
 */
public class GHRepositoryClones extends GHRepositoryTrafficInfo {
    private List<DayClones> clones;

    /*package*/ GHRepositoryClones() {
    }

    /*package*/ GHRepositoryClones(Integer count, Integer uniques, List<DayClones> clones) {
        super(count, uniques);
        this.clones = clones;
    }

    public List<DayClones> getClones() {
        return clones;
    }

    public List<DayClones> getDailyInfo() {
        return getClones();
    }

    public static class DayClones extends GHRepositoryTrafficInfo.DayInfo {
        /*package*/ DayClones() {
        }

        /*package*/ DayClones(String timestamp, int count, int uniques) {
            super(timestamp, count, uniques);
        }

        /*package*/ DayClones(Date timestamp, int count, int uniques) {
            super(timestamp, count, uniques);
        }
    }
}
