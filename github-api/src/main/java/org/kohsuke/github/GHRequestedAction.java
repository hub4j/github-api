package org.kohsuke.github;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

import java.net.URL;

// TODO: Auto-generated Javadoc
/**
 * The Class GHRequestedAction.
 */
@SuppressFBWarnings(value = { "UWF_UNWRITTEN_FIELD", "NP_UNWRITTEN_FIELD", "URF_UNREAD_FIELD" },
        justification = "JSON API")
public class GHRequestedAction extends GHObject {
    private GHRepository owner;
    private String identifier;
    private String label;
    private String description;

    /**
     * Wrap.
     *
     * @param owner
     *            the owner
     * @return the GH requested action
     */
    GHRequestedAction wrap(GHRepository owner) {
        this.owner = owner;
        return this;
    }

    /**
     * Gets the identifier.
     *
     * @return the identifier
     */
    String getIdentifier() {
        return identifier;
    }

    /**
     * Gets the label.
     *
     * @return the label
     */
    String getLabel() {
        return label;
    }

    /**
     * Gets the description.
     *
     * @return the description
     */
    String getDescription() {
        return description;
    }

    /**
     * Gets the html url.
     *
     * @return the html url
     * @deprecated This object has no HTML URL.
     */
    @Override
    public URL getHtmlUrl() {
        return null;
    }

}
