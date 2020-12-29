package org.kohsuke.github;

import com.fasterxml.jackson.annotation.JacksonInject;

/**
 * Defines a base class that all classes in this library that interact with GitHub inherit from.
 *
 * Ensures that all data references to GitHub connection are transient.
 *
 * Classes that do not need to interact with GitHub after they are instantiated do not need to inherit from this class.
 */
abstract class GitHubInteractiveObject {
    @JacksonInject
    /* package almost final */ transient GitHub root;

    GitHubInteractiveObject() {
        root = null;
    }

    GitHubInteractiveObject(GitHub root) {
        this.root = root;
    }
}
