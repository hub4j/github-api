package org.kohsuke.github;

import java.util.Locale;

// TODO: Auto-generated Javadoc
/**
 * App installation repository selection.
 *
 * @author Paulo Miguel Almeida
 * @see GHAppInstallation
 */
public enum GHRepositorySelection {

    /** The all. */
    ALL,
    /** The selected. */
    SELECTED;

    /**
     * Returns GitHub's internal representation of this event.
     *
     * @return the string
     */
    String symbol() {
        return name().toLowerCase(Locale.ENGLISH);
    }
}
