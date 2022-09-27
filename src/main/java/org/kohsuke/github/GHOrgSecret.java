package org.kohsuke.github;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

import java.net.URL;

/**
 * A secret in an organization.
 *
 * @author João Almeida
 */
@SuppressFBWarnings(value = { "UWF_UNWRITTEN_PUBLIC_OR_PROTECTED_FIELD", "UWF_UNWRITTEN_FIELD", "NP_UNWRITTEN_FIELD" },
        justification = "JSON API")
public class GHOrgSecret extends GHObject {

    /**
     * Organization that the secret belongs to.
     */
    transient GHOrganization organization;

    String name;
    String visibility;
    String selectedRepositoriesUrl;

    /**
     * Gets the name of the secret.
     *
     * @return the name of the secret
     */
    public String getName() {
        return name;
    }

    /**
     * Gets the visibility of the secret.
     *
     * @return the visibility of the secret
     */
    public String getVisibility() {
        return visibility;
    }

    /**
     * Gets the repositories url that the secret can be accessed from.
     *
     * @return the repositories url that the secret can be accessed from
     */
    public String getRepositoriesUrl() {
        return selectedRepositoriesUrl;
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
    GHOrgSecret wrapUp(GHOrganization owner) {
        this.organization = owner;
        return this;
    }
}
