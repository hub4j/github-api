package org.kohsuke.github.function;

/**
 * A functional interface, equivalent to {@link java.util.function.Consumer} but that allows throwing {@link Throwable}
 */
@FunctionalInterface
public interface ConsumerThrows<T, E extends Throwable> {
    void accept(T input) throws E;
}
