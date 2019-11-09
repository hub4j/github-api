package org.kohsuke.github;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

/**
 * Represents an annotated tag in a {@link GHRepository}
 *
 * @see GHRepository#getTagObject(String)
 */
@SuppressFBWarnings(value = {"UWF_UNWRITTEN_PUBLIC_OR_PROTECTED_FIELD", "UWF_UNWRITTEN_FIELD", 
    "NP_UNWRITTEN_FIELD"}, justification = "JSON API")
public class GHTagObject extends GHObjectBase{
    private GHRepository owner;

    private String tag;
    private String sha;
    private String url;
    private String message;
    private GitUser tagger;
    private GHRef.GHObject object;

    /*package*/ GHTagObject wrap(GHRepository owner) {
        this.owner = owner;
        this.root = owner.root;
        return this;
    }

    public GHRepository getOwner() {
        return owner;
    }

    public GitHub getRoot() {
        return root;
    }

    public String getTag() {
        return tag;
    }

    public String getSha() {
        return sha;
    }

    public String getUrl() {
        return url;
    }

    public String getMessage() {
        return message;
    }

    public GitUser getTagger() {
        return tagger;
    }

    public GHRef.GHObject getObject() {
        return object;
    }
}
