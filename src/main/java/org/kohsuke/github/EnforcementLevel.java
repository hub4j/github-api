package org.kohsuke.github;

import java.util.Locale;

/**
 * @author Kohsuke Kawaguchi
 */
public enum EnforcementLevel {
    OFF, NON_ADMINS, EVERYONE;

    public String toString() {
        return name().toLowerCase(Locale.ENGLISH);
    }
}
