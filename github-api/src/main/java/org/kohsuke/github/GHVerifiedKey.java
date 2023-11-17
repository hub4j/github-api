package org.kohsuke.github;

// TODO: Auto-generated Javadoc
/**
 * The type GHVerifiedKey.
 */
public class GHVerifiedKey extends GHKey {

    /**
     * Instantiates a new Gh verified key.
     */
    public GHVerifiedKey() {
        this.verified = true;
    }

    /**
     * Gets the title.
     *
     * @return the title
     */
    @Override
    public String getTitle() {
        return (title == null ? "key-" + id : title);
    }
}
