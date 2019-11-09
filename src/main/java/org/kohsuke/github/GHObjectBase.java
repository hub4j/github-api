package org.kohsuke.github;

import com.fasterxml.jackson.annotation.JacksonInject;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

import javax.annotation.Nonnull;

/**
 * Most (all?) domain objects in GitHub seems to have these 4 properties.
 */
@SuppressFBWarnings(value = {"UWF_UNWRITTEN_PUBLIC_OR_PROTECTED_FIELD", "UWF_UNWRITTEN_FIELD", 
    "NP_UNWRITTEN_FIELD"}, justification = "JSON API")
public abstract class GHObjectBase {

    /**
     * Effectively final.
     */
    @Nonnull
    private GitHub root;

    GHObjectBase() {
        this(GitHub.offline());
    }

    GHObjectBase(@Nonnull GitHub root) {
        this.root = root;
    }

    GitHub getRoot() {
        return root;
    }

    /**
     * Adding this setter to allow objects to override and hook in.
     * @param root
     */
    @JacksonInject(value = "org.kohsuke.github.GitHub")
    void setRoot(GitHub root) {
        if (root != null) {
            this.root = root;
        }
    }

}
