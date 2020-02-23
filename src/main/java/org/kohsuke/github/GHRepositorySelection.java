package org.kohsuke.github;

import java.util.Locale;

/**
 * App installation repository selection.
 *
 * @see GHAppInstallation
 */
public enum GHRepositorySelection {
    SELECTED, ALL;

    /**
     * Returns GitHub's internal representation of this event.
     */
    String symbol() {
        return name().toLowerCase(Locale.ENGLISH);
    }
}
