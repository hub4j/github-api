package org.kohsuke.github;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

import java.net.URL;

/**
 * The type GHPublicKey.
 *
 * @author João Almeida
 */
@SuppressFBWarnings(value = { "UWF_UNWRITTEN_PUBLIC_OR_PROTECTED_FIELD", "UWF_UNWRITTEN_FIELD", "NP_UNWRITTEN_FIELD" },
        justification = "JSON API")
public abstract class GHPublicKey extends GHObject {
    private String keyId;
    private String key;

    /**
     * @deprecated This object has no HTML URL.
     */
    @Override
    public URL getHtmlUrl() {
        return null;
    }

    public String getKeyId() {
        return keyId;
    }

    public String getKey() {
        return key;
    }

    abstract GitHub root();

    abstract String getApiRoute();
}
