package org.kohsuke.github;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

// TODO: Auto-generated Javadoc
/**
 * Represents an annotated tag in a {@link GHRepository}.
 *
 * @see GHRepository#getTagObject(String) GHRepository#getTagObject(String)
 */
@SuppressFBWarnings(value = { "UWF_UNWRITTEN_PUBLIC_OR_PROTECTED_FIELD", "UWF_UNWRITTEN_FIELD", "NP_UNWRITTEN_FIELD" },
        justification = "JSON API")
public class GHTagObject extends GitHubInteractiveObject {

    private String message;

    private GHRef.GHObject object;

    private GHRepository owner;
    private String sha;
    private String tag;
    private GitUser tagger;
    private String url;
    private GHVerification verification;
    /**
     * Create default GHTagObject instance
     */
    public GHTagObject() {
    }

    /**
     * Gets message.
     *
     * @return the message
     */
    public String getMessage() {
        return message;
    }

    /**
     * Gets object.
     *
     * @return the object
     */
    public GHRef.GHObject getObject() {
        return object;
    }

    /**
     * Gets owner.
     *
     * @return the owner
     */
    @SuppressFBWarnings(value = { "EI_EXPOSE_REP" }, justification = "Expected behavior")
    public GHRepository getOwner() {
        return owner;
    }

    /**
     * Gets sha.
     *
     * @return the sha
     */
    public String getSha() {
        return sha;
    }

    /**
     * Gets tag.
     *
     * @return the tag
     */
    public String getTag() {
        return tag;
    }

    /**
     * Gets tagger.
     *
     * @return the tagger
     */
    public GitUser getTagger() {
        return tagger;
    }

    /**
     * Gets url.
     *
     * @return the url
     */
    public String getUrl() {
        return url;
    }

    /**
     * Gets Verification Status.
     *
     * @return the Verification status
     */
    public GHVerification getVerification() {
        return verification;
    }

    /**
     * Wrap.
     *
     * @param owner
     *            the owner
     * @return the GH tag object
     */
    GHTagObject wrap(GHRepository owner) {
        this.owner = owner;
        return this;
    }
}
