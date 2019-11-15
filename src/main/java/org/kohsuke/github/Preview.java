package org.kohsuke.github;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Indicates that the method/class/etc marked maps to GitHub API in the preview period.
 * <p>
 * These APIs are subject to change and not a part of the backward compatibility commitment. Always used in conjunction
 * with 'deprecated' to raise awareness to clients.
 *
 * @author Kohsuke Kawaguchi
 */
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Preview {
}
