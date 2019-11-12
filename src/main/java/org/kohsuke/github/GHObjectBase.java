package org.kohsuke.github;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

/**
 * Most (all?) domain objects in GitHub seems to have these 4 properties.
 */
@SuppressFBWarnings(value = {"UWF_UNWRITTEN_PUBLIC_OR_PROTECTED_FIELD", "UWF_UNWRITTEN_FIELD", 
    "NP_UNWRITTEN_FIELD"}, justification = "JSON API")
public abstract class GHObjectBase {

    private GitHub root = null;

    public GitHub getRoot() {
        return root;
    }

    void setRoot(GitHub root) {
        this.root = root;
    }

    Requester createRequester() {
        return getRoot().createRequester();
    }
}
