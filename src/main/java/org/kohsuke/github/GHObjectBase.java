package org.kohsuke.github;

import com.fasterxml.jackson.annotation.JacksonInject;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

import javax.annotation.Nonnull;

/**
 *
 */
@SuppressFBWarnings(value = {"UWF_UNWRITTEN_PUBLIC_OR_PROTECTED_FIELD", "UWF_UNWRITTEN_FIELD", 
    "NP_UNWRITTEN_FIELD"}, justification = "JSON API")
public abstract class GHObjectBase {

    private final static GitHub DEFAULT_OFFLINE_ROOT = GitHub.offline();
    /**
     * Effectively final.
     * TODO: Make this actually final.
     */
    @Nonnull
    @JacksonInject(value = "org.kohsuke.github.GitHub")
    private GitHub root;

    GHObjectBase() {
        this(DEFAULT_OFFLINE_ROOT);
    }

    GHObjectBase(@Nonnull GitHub root) {
        this.root = root;
    }


    GitHub getRoot() {
        return root;
    }

    /**
     * Adding this setter to allow objects to override and hook in.
     *
     * @param root the ro
     * @deprecated This will be removed soon. Root should be final.
     */
    void setRoot(GitHub root) {
        if (root != null) {
            this.root = root;
        }
    }

    @Nonnull
    Requester createRequest() {
        return getRoot().createRequest();
    }


}
