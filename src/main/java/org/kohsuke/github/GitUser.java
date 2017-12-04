/*
 * GitHub API for Java
 * Copyright (C) 2009-2016 SonarSource SA
 * mailto:contact AT sonarsource DOT com
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
 * Represents a user in Git who authors/commits a commit.
 *
 * In contrast, {@link GHUser} is an user of GitHub. Because Git allows a person to
 * use multiple e-mail addresses and names when creating a commit, there's generally
 * no meaningful mapping between {@link GHUser} and {@link GitUser}.
 *
 * @author Kohsuke Kawaguchi
 */
@SuppressFBWarnings(value = {"UWF_UNWRITTEN_PUBLIC_OR_PROTECTED_FIELD", "UWF_UNWRITTEN_FIELD", 
    "NP_UNWRITTEN_FIELD"}, justification = "JSON API")
public class GitUser {
    private String name, email, date;

    /**
     * Human readable name of the user, such as "Kohsuke Kawaguchi"
     */
    public String getName() {
        return name;
    }

    /**
     * E-mail address, such as "foo@example.com"
     */
    public String getEmail() {
        return email;
    }

    /**
     * This field doesn't appear to be consistently available in all the situations where this class
     * is used.
     */
    public Date getDate() {
        return GitHub.parseDate(date);
    }
}
