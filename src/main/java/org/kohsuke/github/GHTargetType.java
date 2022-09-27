package org.kohsuke.github;

import org.apache.commons.lang3.StringUtils;

import java.util.Locale;

// TODO: Auto-generated Javadoc
/**
 * App installation target type.
 *
 * @author Paulo Miguel Almeida
 * @see GHAppInstallation
 */
public enum GHTargetType {

    /** The organization. */
    ORGANIZATION,
    /** The user. */
    USER;

    /**
     * Returns GitHub's internal representation of this event.
     *
     * @return the string
     */
    String symbol() {
        return StringUtils.capitalize(name().toLowerCase(Locale.ENGLISH));
    }
}
