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

import com.infradna.tool.bridge_method_injector.WithBridgeMethods;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

/**
 * Represents an user of GitHub.
 *
 * @author Kohsuke Kawaguchi
 */
public class GHUser extends GHPerson {

    /**
     * Follow this user.
     */
    public void follow() throws IOException {
        new Requester(root).method("PUT").to("/user/following/" + login);
    }

    /**
     * Unfollow this user.
     */
    public void unfollow() throws IOException {
        new Requester(root).method("DELETE").to("/user/following/" + login);
    }

    /**
     * Lists the users that this user is following
     */
    @WithBridgeMethods(Set.class)
    public GHPersonSet<GHUser> getFollows() throws IOException {
        return new GHPersonSet<GHUser>(listFollows().asList());
    }

    /**
     * Lists the users that this user is following
     */
    public PagedIterable<GHUser> listFollows() {
        return listUser("following");
    }

    /**
     * Lists the users who are following this user.
     */
    @WithBridgeMethods(Set.class)
    public GHPersonSet<GHUser> getFollowers() throws IOException {
        return new GHPersonSet<GHUser>(listFollowers().asList());
    }

    /**
     * Lists the users who are following this user.
     */
    public PagedIterable<GHUser> listFollowers() {
        return listUser("followers");
    }

    private PagedIterable<GHUser> listUser(final String suffix) {
        return new PagedIterable<GHUser>() {
            public PagedIterator<GHUser> _iterator(int pageSize) {
                return new PagedIterator<GHUser>(root.retrieve().asIterator(getApiTailUrl(suffix), GHUser[].class, pageSize)) {
                    protected void wrapUp(GHUser[] page) {
                        GHUser.wrap(page,root);
                    }
                };
            }
        };
    }

    /**
     * Lists all the subscribed (aka watched) repositories.
     *
     * https://developer.github.com/v3/activity/watching/
     */
    public PagedIterable<GHRepository> listSubscriptions() {
        return listRepositories("subscriptions");
    }

    /**
     * Lists all the repositories that this user has starred.
     */
    public PagedIterable<GHRepository> listStarredRepositories() {
        return listRepositories("starred");
    }

    private PagedIterable<GHRepository> listRepositories(final String suffix) {
        return new PagedIterable<GHRepository>() {
            public PagedIterator<GHRepository> _iterator(int pageSize) {
                return new PagedIterator<GHRepository>(root.retrieve().asIterator(getApiTailUrl(suffix), GHRepository[].class, pageSize)) {
                    protected void wrapUp(GHRepository[] page) {
                        for (GHRepository c : page)
                            c.wrap(root);
                    }
                };
            }
        };
    }

    /**
     * Returns true if this user belongs to the specified organization.
     */
    public boolean isMemberOf(GHOrganization org) {
        return org.hasMember(this);
    }

    /**
     * Returns true if this user belongs to the specified team.
     */
    public boolean isMemberOf(GHTeam team) {
        return team.hasMember(this);
    }

    /**
     * Returns true if this user belongs to the specified organization as a public member.
     */
    public boolean isPublicMemberOf(GHOrganization org) {
        return org.hasPublicMember(this);
    }

    /*package*/ static GHUser[] wrap(GHUser[] users, GitHub root) {
        for (GHUser f : users)
            f.root = root;
        return users;
    }

    /**
     * Gets the organization that this user belongs to publicly.
     */
    @WithBridgeMethods(Set.class)
    public GHPersonSet<GHOrganization> getOrganizations() throws IOException {
        GHPersonSet<GHOrganization> orgs = new GHPersonSet<GHOrganization>();
        Set<String> names = new HashSet<String>();
        for (GHOrganization o : root.retrieve().to("/users/" + login + "/orgs", GHOrganization[].class)) {
            if (names.add(o.getLogin()))    // I've seen some duplicates in the data
                orgs.add(root.getOrganization(o.getLogin()));
        }
        return orgs;
    }

    /**
     * Lists events performed by a user (this includes private events if the caller is authenticated.
     */
    public PagedIterable<GHEventInfo> listEvents() throws IOException {
        return new PagedIterable<GHEventInfo>() {
            public PagedIterator<GHEventInfo> _iterator(int pageSize) {
                return new PagedIterator<GHEventInfo>(root.retrieve().asIterator(String.format("/users/%s/events", login), GHEventInfo[].class, pageSize)) {
                    @Override
                    protected void wrapUp(GHEventInfo[] page) {
                        for (GHEventInfo c : page)
                            c.wrapUp(root);
                    }
                };
            }
        };
    }

    /**
     * Lists Gists created by this user.
     */
    public PagedIterable<GHGist> listGists() throws IOException {
        return new PagedIterable<GHGist>() {
            public PagedIterator<GHGist> _iterator(int pageSize) {
                return new PagedIterator<GHGist>(root.retrieve().asIterator(String.format("/users/%s/gists", login), GHGist[].class, pageSize)) {
                    @Override
                    protected void wrapUp(GHGist[] page) {
                        for (GHGist c : page)
                            c.wrapUp(GHUser.this);
                    }
                };
            }
        };
    }

    @Override
    public int hashCode() {
        return login.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof GHUser) {
            GHUser that = (GHUser) obj;
            return this.login.equals(that.login);
        }
        return false;
    }

    String getApiTailUrl(String tail) {
        if (tail.length()>0 && !tail.startsWith("/"))    tail='/'+tail;
        return "/users/" + login + tail;
    }

    /*package*/ GHUser wrapUp(GitHub root) {
        super.wrapUp(root);
        return this;
    }
}
