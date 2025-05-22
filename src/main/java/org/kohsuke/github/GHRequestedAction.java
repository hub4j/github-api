package org.kohsuke.github;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

// TODO: Auto-generated Javadoc
/**
 * The Class GHRequestedAction.
 */
@SuppressFBWarnings(value = { "UWF_UNWRITTEN_FIELD", "NP_UNWRITTEN_FIELD", "URF_UNREAD_FIELD" },
        justification = "JSON API")
public class GHRequestedAction extends GHObject {

    /**
     * Create default GHRequestedAction instance
     */
    public GHRequestedAction() {
    }

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
    public String getIdentifier() {
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

}
