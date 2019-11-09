package org.kohsuke.github;

import java.io.IOException;
import java.net.URL;
import java.util.Locale;

/**
 * Represents a membership of a user in an organization.
 *
 * @author Kohsuke Kawaguchi
 * @see GHMyself#listOrgMemberships()
 */
public class GHMembership extends GHObjectBase {
    /* effectively extends GHObject --- but it doesn't have id, created_at, etc. */
    String url;
    String state;
    String role;
    GHUser user;
    GHOrganization organization;

    public URL getUrl() {
        return GitHub.parseURL(url);
    }

    public State getState() {
        return Enum.valueOf(State.class, state.toUpperCase(Locale.ENGLISH));
    }

    public Role getRole() {
        return Enum.valueOf(Role.class, role.toUpperCase(Locale.ENGLISH));
    }

    public GHUser getUser() {
        return user;
    }

    public GHOrganization getOrganization() {
        return organization;
    }

    /**
     * Accepts a pending invitation to an organization.
     *
     * @see GHMyself#getMembership(GHOrganization)
     */
    public void activate() throws IOException {
        getRoot().retrieve().method("PATCH").with("state",State.ACTIVE).to(url,this);
    }

    /*package*/ GHMembership wrap(GitHub root) {
        if (user!=null)     user = root.getUser(user.wrapUp(root));
        if (organization!=null) organization.wrapUp(root);
        return this;
    }

    /*package*/ static void wrap(GHMembership[] page, GitHub root) {
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
        ACTIVE,
        PENDING;
    }
}
