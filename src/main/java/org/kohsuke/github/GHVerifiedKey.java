package org.kohsuke.github;

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

    @Override
    public String getTitle() {
        return (title == null ? "key-" + id : title);
    }
}
