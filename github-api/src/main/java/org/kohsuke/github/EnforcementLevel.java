package org.kohsuke.github;

import java.util.Locale;

// TODO: Auto-generated Javadoc
/**
 * This was added during preview API period but it has changed since then.
 *
 * @author Kohsuke Kawaguchi
 */
@Deprecated
public enum EnforcementLevel {

    /** The off. */
    OFF,
    /** The non admins. */
    NON_ADMINS,
    /** The everyone. */
    EVERYONE;

    /**
     * To string.
     *
     * @return the string
     */
    public String toString() {
        return name().toLowerCase(Locale.ENGLISH);
    }
}
