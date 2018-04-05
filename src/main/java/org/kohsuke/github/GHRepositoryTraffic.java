/*
 * GitHub API for Java
 * Copyright (C) 2009-2017 SonarSource SA
 * mailto:info AT sonarsource DOT com
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */
package org.kohsuke.github;

import java.util.Date;
import java.util.List;

public abstract class GHRepositoryTraffic implements TrafficInfo {
    private int count;
    private int uniques;

    /*package*/ GHRepositoryTraffic() {
    }

    /*package*/ GHRepositoryTraffic(int count, int uniques) {
        this.count = count;
        this.uniques = uniques;
    }

    public int getCount() {
        return count;
    }

    public int getUniques() {
        return uniques;
    }

    public abstract List<? extends DailyInfo> getDailyInfo();

    public static abstract class DailyInfo implements TrafficInfo {
        private String timestamp;
        private int count;
        private int uniques;

        public Date getTimestamp() {
            return GitHub.parseDate(timestamp);
        }

        public int getCount() {
            return count;
        }

        public int getUniques() {
            return uniques;
        }

        /*package*/ DailyInfo() {
        }

        /*package*/ DailyInfo(String timestamp, Integer count, Integer uniques) {
            this.timestamp = timestamp;
            this.count = count;
            this.uniques = uniques;
        }
    }
}
