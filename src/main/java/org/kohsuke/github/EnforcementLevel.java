package org.kohsuke.github;

import java.util.Locale;

/**
 * This was added during preview API period but it has changed since then.
 *
 * @author Kohsuke Kawaguchi
 */
@Deprecated
public enum EnforcementLevel {
    OFF, NON_ADMINS, EVERYONE;

    public String toString() {
        return name().toLowerCase(Locale.ENGLISH);
    }
}
