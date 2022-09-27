package org.kohsuke.github;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

import java.net.URL;

/**
 * A secret in a repository.
 *
 * @author João Almeida
 */
@SuppressFBWarnings(value = { "UWF_UNWRITTEN_PUBLIC_OR_PROTECTED_FIELD", "UWF_UNWRITTEN_FIELD", "NP_UNWRITTEN_FIELD" },
        justification = "JSON API")
public class GHRepoSecret extends GHObject {

    /**
     * Repository that the secret belongs to.
     */
    transient GHRepository repository;

    String name;

    /**
     * Gets the name of the secret.
     *
     * @return the name of the secret
     */
    public String getName() {
        return name;
    }

    /**
     * @deprecated This object has no HTML URL.
     */
    @Override
    public URL getHtmlUrl() {
        return null;
    }

    /**
     * Wrap up the secret.
     *
     * @param owner
     *            the owner of the secret
     * @return the secret
     */
    GHRepoSecret wrapUp(GHRepository owner) {
        this.repository = owner;
        return this;
    }
}
