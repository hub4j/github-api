package org.kohsuke.github;

/**
 * The enum GHPermissionType.
 *
 * @author Kohsuke Kawaguchi
 */
public enum GHPermissionType {
    ADMIN(30), WRITE(20), READ(10), NONE(0);

    private final int level;

    GHPermissionType(int level) {
        this.level = level;
    }

    boolean implies(GHPermissionType other) {
        // NONE is a special case
        if (other == NONE) {
            return this == NONE;
        }

        return this.level >= other.level;
    }
}
