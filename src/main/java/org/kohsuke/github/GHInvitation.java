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
 */
@SuppressFBWarnings(
        value = { "UWF_UNWRITTEN_PUBLIC_OR_PROTECTED_FIELD", "UWF_UNWRITTEN_FIELD", "NP_UNWRITTEN_FIELD",
                "UUF_UNUSED_FIELD" },
        justification = "JSON API")
public class GHInvitation extends GHObject {

    private int id;
    private GHRepository repository;
    private GHUser invitee, inviter;
    private String permissions;
    private String html_url;

    /**
     * Accept a repository invitation.
     *
     * @throws IOException
     *             the io exception
     */
    public void accept() throws IOException {
        root().createRequest().method("PATCH").withUrlPath("/user/repository_invitations/" + id).send();
    }

    /**
     * Decline a repository invitation.
     *
     * @throws IOException
     *             the io exception
     */
    public void decline() throws IOException {
        root().createRequest().method("DELETE").withUrlPath("/user/repository_invitations/" + id).send();
    }

    /**
     * Gets the html url.
     *
     * @return the html url
     */
    public URL getHtmlUrl() {
        return GitHubClient.parseURL(html_url);
    }
}
