package org.kohsuke.github;

public class GHVerifiedKey extends GHKey {

    public GHVerifiedKey() {
        this.verified = true;
    }

    @Override
    public String getTitle() {
        return (title == null ? "key-" + id : title);
    }
}
