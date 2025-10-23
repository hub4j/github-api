package org.kohsuke.github;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Indicates that the method/class/etc marked is a beta implementation of an SDK feature.
 * <p>
 * These APIs are subject to change and not a part of the backward compatibility commitment.
 */
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface BetaApi {
}
