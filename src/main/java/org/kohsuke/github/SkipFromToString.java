package org.kohsuke.github;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Ignores this field for {@link GHObject#toString()}.
 *
 * @author Kohsuke Kawaguchi
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
@interface SkipFromToString {
}
