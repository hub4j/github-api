package org.kohsuke.github;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

import java.io.IOException;
import java.net.URL;
import java.util.Locale;

// TODO: Auto-generated Javadoc
/**
 * Represents a membership of a user in an organization.
 *
 * @author Kohsuke Kawaguchi
 * @see GHMyself#listOrgMemberships() GHMyself#listOrgMemberships()
 */
public class GHMembership extends GitHubInteractiveObject {
    
    /** The url. */
    String url;
    
    /** The state. */
    String state;
    
    /** The role. */
    String role;
    
    /** The user. */
    GHUser user;
    
    /** The organization. */
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
    @SuppressFBWarnings(value = { "EI_EXPOSE_REP" }, justification = "Expected behavior")
    public GHUser getUser() {
        return user;
    }

    /**
     * Gets organization.
     *
     * @return the organization
     */
    @SuppressFBWarnings(value = { "EI_EXPOSE_REP" }, justification = "Expected behavior")
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
        root().createRequest().method("PATCH").with("state", State.ACTIVE).withUrlPath(url).fetchInto(this);
    }

    /**
     * Wrap.
     *
     * @param root the root
     * @return the GH membership
     */
    GHMembership wrap(GitHub root) {
        if (user != null)
            user = root.getUser(user);
        return this;
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
     * Whether a role is currently active or waiting for acceptance (pending).
     */
    public enum State {
        
        /** The active. */
        ACTIVE, 
 /** The pending. */
 PENDING;
    }
}
