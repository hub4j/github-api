package org.kohsuke.github;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

import java.net.URL;

@SuppressFBWarnings(value = { "UWF_UNWRITTEN_FIELD", "NP_UNWRITTEN_FIELD", "URF_UNREAD_FIELD" },
        justification = "JSON API")
public class GHRequestedAction extends GHObject {
    private GHRepository owner;
    private GitHub root;
    private String identifier;
    private String label;
    private String description;

    GHRequestedAction wrap(GHRepository owner) {
        this.owner = owner;
        wrap(owner.root);
        return this;
    }
    GHRequestedAction wrap(GitHub root) {
        this.root = root;
        if (owner != null) {
            owner.wrap(root);
        }
        return this;
    }

    String getIdentifier() {
        return identifier;
    }

    String getLabel() {
        return label;
    }

    String getDescription() {
        return description;
    }

    /**
     * @deprecated This object has no HTML URL.
     */
    @Override
    public URL getHtmlUrl() {
        return null;
    }

}
