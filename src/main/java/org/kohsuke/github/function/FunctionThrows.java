package org.kohsuke.github.function;

/**
 * A functional interface, equivalent to {@link java.util.function.Function} but that allows throwing {@link Throwable}
 *
 * @param <T>
 *            the type of input
 * @param <R>
 *            the type of output
 * @param <E>
 *            the type of error
 */
@FunctionalInterface
public interface FunctionThrows<T, R, E extends Throwable> {
    /**
     * Apply r.
     *
     * @param input
     *            the input
     * @return the r
     * @throws E
     *             the e
     */
    R apply(T input) throws E;
}
