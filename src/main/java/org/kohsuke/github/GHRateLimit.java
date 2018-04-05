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

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

import java.util.Date;

/**
 * Rate limit.
 * @author Kohsuke Kawaguchi
 */
public class GHRateLimit {
    /**
     * Remaining calls that can be made.
     */
    public int remaining;
    /**
     * Allotted API call per hour.
     */
    public int limit;

    /**
     * The time at which the current rate limit window resets in UTC epoch seconds.
     */
    public Date reset;

    /**
     * Non-epoch date
     */
    @SuppressFBWarnings(value = "UWF_FIELD_NOT_INITIALIZED_IN_CONSTRUCTOR", 
            justification = "The value comes from JSON deserialization")
    public Date getResetDate() {
        return new Date(reset.getTime() * 1000);
    }

    @Override
    public String toString() {
        return "GHRateLimit{" +
                "remaining=" + remaining +
                ", limit=" + limit +
                ", resetDate=" + getResetDate() +
                '}';
    }
}
