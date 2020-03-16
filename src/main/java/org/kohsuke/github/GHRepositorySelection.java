package org.kohsuke.github;

import java.util.Locale;

/**
 * App installation repository selection.
 *
 * @author Paulo Miguel Almeida
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
