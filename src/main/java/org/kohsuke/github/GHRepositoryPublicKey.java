package org.kohsuke.github;

import com.fasterxml.jackson.annotation.JsonIgnore;

// TODO: Auto-generated Javadoc
/**
 * A public key for the given repository.
 *
 * @author Aditya Bansal
 */
public class GHRepositoryPublicKey extends GHObject {
    // Not provided by the API.
    @JsonIgnore
    private GHRepository owner;

    private String keyId;
    private String key;

    /**
     * Gets the key id.
     *
     * @return the key id
     */
    public String getKeyId() {
        return keyId;
    }

    /**
     * Gets the key.
     *
     * @return the key
     */
    public String getKey() {
        return key;
    }

    /**
     * Wrap up.
     *
     * @param owner
     *            the owner
     * @return the GH repository public key
     */
    GHRepositoryPublicKey wrapUp(GHRepository owner) {
        this.owner = owner;
        return this;
    }
}
