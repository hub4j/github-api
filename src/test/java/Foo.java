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
import org.kohsuke.github.GHRepository.Contributor;
import org.kohsuke.github.GHUser;
import org.kohsuke.github.GitHub;

/**
 * @author Kohsuke Kawaguchi
 */
public class Foo {
    public static void main(String[] args) throws Exception {
        GitHub gh = GitHub.connect();
        for (Contributor c : gh.getRepository("kohsuke/yo").listContributors()) {
            System.out.println(c);
        }
    }

    private static void testRateLimit() throws Exception {
        GitHub g = GitHub.connectAnonymously();
        for (GHUser u : g.getOrganization("jenkinsci").listMembers()) {
            u.getFollowersCount();
        }
    }
}
