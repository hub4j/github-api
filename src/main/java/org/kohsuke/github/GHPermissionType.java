package org.kohsuke.github;

// TODO: Auto-generated Javadoc
/**
 * The enum GHPermissionType.
 *
 * @author Kohsuke Kawaguchi
 */
public enum GHPermissionType {

    /** The admin. */
    ADMIN(30),
    /** The write. */
    WRITE(20),
    /** The read. */
    READ(10),
    /** The none. */
    NONE(0),
    /** The unknown permission type returned when an unrecognized permission type is returned. */
    UNKNOWN(-5);

    private final int level;

    /**
     * Instantiates a new GH permission type.
     *
     * @param level
     *            the level
     */
    GHPermissionType(int level) {
        this.level = level;
    }

    /**
     * Implies.
     *
     * @param other
     *            the other
     * @return true, if successful
     */
    boolean implies(GHPermissionType other) {
        // NONE is a special case
        if (other == NONE) {
            return this == NONE;
        }

        return this.level >= other.level;
    }
}
