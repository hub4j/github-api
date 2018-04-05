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

import java.io.IOException;
import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

/**
 * A team in GitHub organization.
 * 
 * @author Kohsuke Kawaguchi
 */
public class GHTeam {
    private String name,permission,slug;
    private int id;
    private GHOrganization organization; // populated by GET /user/teams where Teams+Orgs are returned together

    protected /*final*/ GHOrganization org;

    /** Member's role in a team */
    public enum Role {
        /**
         * A normal member of the team
         */
        MEMBER,
        /**
         * Able to add/remove other team members, promote other team members to team maintainer, and edit the team's name and description.
         */
        MAINTAINER
    }

    /*package*/ GHTeam wrapUp(GHOrganization owner) {
        this.org = owner;
        return this;
    }

    /*package*/ GHTeam wrapUp(GitHub root) { // auto-wrapUp when organization is known from GET /user/teams
      this.organization.wrapUp(root);
      return wrapUp(organization);
    }

    /*package*/ static GHTeam[] wrapUp(GHTeam[] teams, GHOrganization owner) {
        for (GHTeam t : teams) {
            t.wrapUp(owner);
        }
        return teams;
    }

    public String getName() {
        return name;
    }

    public String getPermission() {
        return permission;
    }

    public String getSlug() {
        return slug;
    }

    public int getId() {
        return id;
    }

    /**
     * Retrieves the current members.
     */
    public PagedIterable<GHUser> listMembers() throws IOException {
        return new PagedIterable<GHUser>() {
            public PagedIterator<GHUser> _iterator(int pageSize) {
                return new PagedIterator<GHUser>(org.root.retrieve().asIterator(api("/members"), GHUser[].class, pageSize)) {
                    @Override
                    protected void wrapUp(GHUser[] page) {
                        GHUser.wrap(page, org.root);
                    }
                };
            }
        };
    }

    public Set<GHUser> getMembers() throws IOException {
        return Collections.unmodifiableSet(listMembers().asSet());
    }

    /**
     * Checks if this team has the specified user as a member.
     */
    public boolean hasMember(GHUser user) {
        try {
            org.root.retrieve().to("/teams/" + id + "/members/"  + user.getLogin());
            return true;
        } catch (IOException ignore) {
            return false;
        }
    }

    public Map<String,GHRepository> getRepositories() throws IOException {
        Map<String,GHRepository> m = new TreeMap<String, GHRepository>();
        for (GHRepository r : listRepositories()) {
            m.put(r.getName(), r);
        }
        return m;
    }

    public PagedIterable<GHRepository> listRepositories() {
        return new PagedIterable<GHRepository>() {
            public PagedIterator<GHRepository> _iterator(int pageSize) {
                return new PagedIterator<GHRepository>(org.root.retrieve().asIterator(api("/repos"), GHRepository[].class, pageSize)) {
                    @Override
                    protected void wrapUp(GHRepository[] page) {
                        for (GHRepository r : page)
                            r.wrap(org.root);
                    }
                };
            }
        };
    }

    /**
     * Adds a member to the team.
     *
     * The user will be invited to the organization if required.
     *
     * @since 1.59
     */
    public void add(GHUser u) throws IOException {
        org.root.retrieve().method("PUT").to(api("/memberships/" + u.getLogin()), null);
    }

    /**
     * Adds a member to the team
     *
     * The user will be invited to the organization if required.
     *
     * @param user github user
     * @param role role for the new member
     *
     * @throws IOException
     */
    public void add(GHUser user, Role role) throws IOException {
        org.root.retrieve().method("PUT")
                .with("role", role.name())
                .to(api("/memberships/" + user.getLogin()), null);
    }

    /**
     * Removes a member to the team.
     */
    public void remove(GHUser u) throws IOException {
        org.root.retrieve().method("DELETE").to(api("/members/" + u.getLogin()), null);
    }

    public void add(GHRepository r) throws IOException {
        add(r,null);
    }

    public void add(GHRepository r, GHOrganization.Permission permission) throws IOException {
        org.root.retrieve().method("PUT")
                .with("permission", permission)
                .to(api("/repos/" + r.getOwnerName() + '/' + r.getName()), null);
    }

    public void remove(GHRepository r) throws IOException {
        org.root.retrieve().method("DELETE").to(api("/repos/" + r.getOwnerName() + '/' + r.getName()), null);
    }
    
    /**
     * Deletes this team.
     */
    public void delete() throws IOException {
        org.root.retrieve().method("DELETE").to(api(""));
    }

    private String api(String tail) {
        return "/teams/" + id + tail;
    }

    public GHOrganization getOrganization() {
      return org;
    }
}
