package org.kohsuke.github;

import java.util.Locale;

public enum GHVisibility {
    PUBLIC, PRIVATE, INTERNAL;

    public String toString() {
        return name().toLowerCase(Locale.ENGLISH);
    }
}
