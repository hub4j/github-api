package org.kohsuke.github;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Indicates that the method/class/etc marked maps to GitHub API in the preview period.
 * <p>
 * These APIs are subject to change and not a part of the backward compatibility commitment. Always used in conjunction
 * with 'deprecated' to raise awareness to clients. In addition, it's advised to update the targets documentation to
 * signify that the deprecation is required until preview feature being used is promoted to stable.
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
    public Previews[] value() default {};

}
