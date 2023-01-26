package org.kohsuke.github;

import com.fasterxml.jackson.annotation.JacksonInject;
import edu.umd.cs.findbugs.annotations.CheckForNull;
import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

import java.util.Objects;

// TODO: Auto-generated Javadoc
/**
 * Defines a base class that all classes in this library that interact with GitHub inherit from.
 *
 * Ensures that all data references to GitHub connection are transient.
 *
 * Classes that do not need to interact with GitHub after they are instantiated do not need to inherit from this class.
 *
 * @author Liam Newman
 */
abstract class GitHubInteractiveObject {
    @JacksonInject
    @CheckForNull
    private transient final GitHub root;

    /**
     * Instantiates a new git hub interactive object.
     */
    GitHubInteractiveObject() {
        root = null;
    }

    /**
     * Instantiates a new git hub interactive object.
     *
     * @param root
     *            the root
     */
    GitHubInteractiveObject(GitHub root) {
        this.root = root;
    }

    /**
     * Get the root {@link GitHub} instance for this object.
     *
     * @return the root {@link GitHub} instance
     *
     * @deprecated For access to the {@link GitHub} instance, use a local copy instead of pulling it out of objects.
     */
    @Deprecated
    @SuppressFBWarnings(value = { "EI_EXPOSE_REP" }, justification = "Expected behavior")
    public GitHub getRoot() {
        return root();
    }

    /**
     * Get the root {@link GitHub} instance for this object.
     *
     * @return the root {@link GitHub} instance
     */
    @NonNull
    GitHub root() {
        return Objects.requireNonNull(root,
                "The root GitHub reference for this instance is null. Probably caused by deserializing this class without using a GitHub instance. If you must do this, use the MappingObjectReader from GitHub.getMappingObjectReader().");
    }

    /**
     * Object is offline.
     *
     * @return true if GitHub instance is null or offline.
     */
    boolean isOffline() {
        return root == null || root.isOffline();
    }
}
