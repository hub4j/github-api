package org.kohsuke.github;

import java.io.IOException;
import java.net.URL;
import java.util.Locale;

/**
 * Represents a membership of a user in an organization.
 *
 * @author Kohsuke Kawaguchi
 * @see GHMyself#listOrgMemberships() GHMyself#listOrgMemberships()
 */
public class GHMembership /* extends GHObject --- but it doesn't have id, created_at, etc. */ {
    GitHub root;

    String url;
    String state;
    String role;
    GHUser user;
    GHOrganization organization;

    /**
     * Gets url.
     *
     * @return the url
     */
    public URL getUrl() {
        return GitHubClient.parseURL(url);
    }

    /**
     * Gets state.
     *
     * @return the state
     */
    public State getState() {
        return Enum.valueOf(State.class, state.toUpperCase(Locale.ENGLISH));
    }

    /**
     * Gets role.
     *
     * @return the role
     */
    public Role getRole() {
        return Enum.valueOf(Role.class, role.toUpperCase(Locale.ENGLISH));
    }

    /**
     * Gets user.
     *
     * @return the user
     */
    public GHUser getUser() {
        return user;
    }

    /**
     * Gets organization.
     *
     * @return the organization
     */
    public GHOrganization getOrganization() {
        return organization;
    }

    /**
     * Accepts a pending invitation to an organization.
     *
     * @throws IOException
     *             the io exception
     * @see GHMyself#getMembership(GHOrganization) GHMyself#getMembership(GHOrganization)
     */
    public void activate() throws IOException {
        root.createRequest().method("PATCH").with("state", State.ACTIVE).withUrlPath(url).fetchInto(this);
    }

    GHMembership wrap(GitHub root) {
        this.root = root;
        if (user != null)
            user = root.getUser(user.wrapUp(root));
        if (organization != null)
            organization.wrapUp(root);
        return this;
    }

    static void wrap(GHMembership[] page, GitHub root) {
        for (GHMembership m : page)
            m.wrap(root);
    }

    /**
     * Role of a user in an organization.
     */
    public enum Role {
        /**
         * Organization owner.
         */
        ADMIN,
        /**
         * Non-owner organization member.
         */
        MEMBER;
    }

    /**
     * Whether a role is currently active or waiting for acceptance (pending)
     */
    public enum State {
        ACTIVE, PENDING;
    }
}
