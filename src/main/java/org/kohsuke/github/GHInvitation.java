package org.kohsuke.github;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

import java.io.IOException;
import java.net.URL;

/**
 * @see GitHub#getMyInvitations()
 * @see GHRepository#listInvitations()
 */
@SuppressFBWarnings(value = {"UWF_UNWRITTEN_PUBLIC_OR_PROTECTED_FIELD", "UWF_UNWRITTEN_FIELD",
    "NP_UNWRITTEN_FIELD", "UUF_UNUSED_FIELD"}, justification = "JSON API")
public class GHInvitation extends GHObject {

    private int id;
    private GHRepository repository;
    private GHUser invitee, inviter;
    private String permissions;
    private String html_url;

    /*package*/ GHInvitation wrapUp(GitHub root) {
        this.root = root;
        return this;
    }

    /**
     * Accept a repository invitation.
     */
    public void accept() throws IOException {
        root.retrieve().method("PATCH").to("/user/repository_invitations/" + id);
    }

    /**
     * Decline a repository invitation.
     */
    public void decline() throws IOException {
        root.retrieve().method("DELETE").to("/user/repository_invitations/" + id);
    }

    @Override
    public URL getHtmlUrl() {
        return GitHub.parseURL(html_url);
    }
}
