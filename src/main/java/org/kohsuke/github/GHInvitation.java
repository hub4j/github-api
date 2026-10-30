package org.kohsuke.github;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

import java.io.IOException;
import java.net.URL;

// TODO: Auto-generated Javadoc
/**
 * The type GHInvitation.
 *
 * @see GitHub#getMyInvitations() GitHub#getMyInvitations()
 * @see GHRepository#listInvitations() GHRepository#listInvitations()
 * @see <a href="https://docs.github.com/en/rest/collaborators/invitations">Repository invitations API</a>
 */
@SuppressFBWarnings(
        value = { "UWF_UNWRITTEN_PUBLIC_OR_PROTECTED_FIELD", "UWF_UNWRITTEN_FIELD", "NP_UNWRITTEN_FIELD",
                "UUF_UNUSED_FIELD" },
        justification = "JSON API")
public class GHInvitation extends GHObject {

    private boolean expired;
    private String htmlUrl;
    private GHUser invitee, inviter;
    private String permissions;
    private GHRepository repository;

    /**
     * Create default GHInvitation instance
     */
    public GHInvitation() {
    }

    /**
     * Accept a repository invitation.
     *
     * @throws IOException
     *             the io exception
     * @see <a href= "https://docs.github.com/en/rest/collaborators/invitations#accept-a-repository-invitation">Accept a
     *      repository invitation</a>
     */
    public void accept() throws IOException {
        root().createRequest().method("PATCH").withUrlPath("/user/repository_invitations/" + getId()).send();
    }

    /**
     * Decline a repository invitation.
     *
     * @throws IOException
     *             the io exception
     * @see <a href= "https://docs.github.com/en/rest/collaborators/invitations#decline-a-repository-invitation">Decline
     *      a repository invitation</a>
     */
    public void decline() throws IOException {
        root().createRequest().method("DELETE").withUrlPath("/user/repository_invitations/" + getId()).send();
    }

    /**
     * Gets the html url.
     *
     * @return the html url
     */
    public URL getHtmlUrl() {
        return GitHubClient.parseURL(htmlUrl);
    }

    /**
     * Gets the invitee (the user being invited).
     *
     * @return the invitee
     */
    @SuppressFBWarnings(value = { "EI_EXPOSE_REP" }, justification = "Expected behavior")
    public GHUser getInvitee() {
        return invitee;
    }

    /**
     * Gets the inviter (the user who created the invitation).
     *
     * @return the inviter
     */
    @SuppressFBWarnings(value = { "EI_EXPOSE_REP" }, justification = "Expected behavior")
    public GHUser getInviter() {
        return inviter;
    }

    /**
     * Gets the permission associated with the invitation.
     *
     * @return the permission (e.g. "read", "write", "admin", "triage", "maintain")
     */
    public String getPermission() {
        return permissions;
    }

    /**
     * Gets the repository for this invitation.
     *
     * @return the repository
     */
    @SuppressFBWarnings(value = { "EI_EXPOSE_REP" }, justification = "Expected behavior")
    public GHRepository getRepository() {
        return repository;
    }

    /**
     * Whether this invitation has expired.
     *
     * @return true if expired
     */
    public boolean isExpired() {
        return expired;
    }
}
