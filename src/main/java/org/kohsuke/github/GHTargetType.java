package org.kohsuke.github;

import org.apache.commons.lang3.StringUtils;

import java.util.Locale;

/**
 * App installation target type.
 *
 * @author Paulo Miguel Almeida
 *
 * @see GHAppInstallation
 */
public enum GHTargetType {
    ORGANIZATION, USER;

    /**
     * Returns GitHub's internal representation of this event.
     */
    String symbol() {
        return StringUtils.capitalize(name().toLowerCase(Locale.ENGLISH));
    }
}
