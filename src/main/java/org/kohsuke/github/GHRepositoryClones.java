package org.kohsuke.github;

import java.util.Date;
import java.util.List;

public class GHRepositoryClones extends GHRepositoryTrafficInfo {
    private List<DayClones> clones;

    public GHRepositoryClones() {
    }

    public GHRepositoryClones(Integer count, Integer uniques, List<DayClones> clones) {
        super(count, uniques);
        this.clones = clones;
    }

    public List<DayClones> getClones() {
        return clones;
    }

    public static class DayClones extends GHRepositoryTrafficInfo.DayInfo {
        public DayClones() {
        }

        public DayClones(String timestamp, Integer count, Integer uniques) {
            super(timestamp, count, uniques);
        }

        public DayClones(Date timestamp, Integer count, Integer uniques) {
            super(timestamp, count, uniques);
        }
    }
}
