package org.kohsuke.github;

import com.fasterxml.jackson.annotation.JsonIgnore;

import java.io.IOException;
import java.net.URL;

/**
 * A public key for the given repository
 *
 * @author Aditya Bansal
 */
public class GHRepositoryPublicKey extends GHObject {
    // Not provided by the API.
    @JsonIgnore
    private GHRepository owner;

    private String keyId;
    private String key;

    @Override
    public URL getHtmlUrl() throws IOException {
        return null;
    }

    public String getKeyId() {
        return keyId;
    }

    public String getKey() {
        return key;
    }

    GHRepositoryPublicKey wrapUp(GHRepository owner) {
        this.owner = owner;
        return this;
    }
}
