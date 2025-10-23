package org.kohsuke.github.function;

/**
 * A functional interface, equivalent to {@link java.util.function.Supplier} but that allows throwing {@link Throwable}
 *
 * @param <T>
 *            the type of output
 * @param <E>
 *            the type of error
 */
@FunctionalInterface
public interface SupplierThrows<T, E extends Throwable> {
    /**
     * Get a value.
     *
     * @return the
     * @throws E
     *             the exception that may be thrown
     */
    T get() throws E;
}
