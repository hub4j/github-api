package org.kohsuke.github;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * @see GitHub#getMyInvitations()
 * @see GHRepository#listInvitations()
 */
public class GHInvitation extends GHObject {
    /*package almost final*/ GitHub root;

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
