package org.kohsuke.github;

import com.fasterxml.jackson.annotation.JsonIgnore;

// TODO: Auto-generated Javadoc
/**
 * A public key for the given repository.
 *
 * @author Aditya Bansal
 */
public class GHRepositoryPublicKey extends GHObject {

    private String key;

    private String keyId;

    // Not provided by the API.
    @JsonIgnore
    private GHRepository owner;
    /**
     * Create default GHRepositoryPublicKey instance
     */
    public GHRepositoryPublicKey() {
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
     * Gets the key id.
     *
     * @return the key id
     */
    public String getKeyId() {
        return keyId;
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
