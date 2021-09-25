package org.kohsuke.github;

import org.kohsuke.github.internal.Previews;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Indicates that the method/class/etc marked maps to GitHub API in the preview period.
 * <p>
 * These APIs are subject to change and not a part of the backward compatibility commitment.
 *
 * It is advised to update the target's documentation with text indicating that a preview feature being used.
 *
 * @author Kohsuke Kawaguchi
 */
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Preview {

    /**
     * An optional field defining what API media types must be set inorder to support the usage of this annotations
     * target.
     * <p>
     * This value must be set using the existing constants defined in {@link Previews}
     *
     * @return The API preview media type.
     */
    public Previews[] value();

}
