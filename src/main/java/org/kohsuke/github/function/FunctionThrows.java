package org.kohsuke.github.function;

/**
 * A functional interface, equivalent to {@link java.util.function.Function} but that allows throwing {@link Throwable}
 */
@FunctionalInterface
public interface FunctionThrows<T, R, E extends Throwable> {
    R apply(T input) throws E;
}
