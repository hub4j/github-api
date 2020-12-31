package org.kohsuke.github;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

/**
 * Represents an annotated tag in a {@link GHRepository}
 *
 * @see GHRepository#getTagObject(String) GHRepository#getTagObject(String)
 */
@SuppressFBWarnings(value = { "UWF_UNWRITTEN_PUBLIC_OR_PROTECTED_FIELD", "UWF_UNWRITTEN_FIELD", "NP_UNWRITTEN_FIELD" },
        justification = "JSON API")
public class GHTagObject extends GitHubInteractiveObject {
    private GHRepository owner;

    private String tag;
    private String sha;
    private String url;
    private String message;
    private GitUser tagger;
    private GHRef.GHObject object;
    private GHVerification verification;

    GHTagObject wrap(GHRepository owner) {
        this.owner = owner;
        this.root = owner.root;
        return this;
    }

    /**
     * Gets owner.
     *
     * @return the owner
     */
    public GHRepository getOwner() {
        return owner;
    }

    /**
     * Gets root.
     *
     * @return the root
     */
    public GitHub getRoot() {
        return root;
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
     * Gets sha.
     *
     * @return the sha
     */
    public String getSha() {
        return sha;
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
     * Gets message.
     *
     * @return the message
     */
    public String getMessage() {
        return message;
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
     * Gets object.
     *
     * @return the object
     */
    public GHRef.GHObject getObject() {
        return object;
    }

    /**
     * Gets Verification Status.
     *
     * @return the Verification status
     */
    public GHVerification getVerification() {
        return verification;
    }
}
