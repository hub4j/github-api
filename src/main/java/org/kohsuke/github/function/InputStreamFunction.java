package org.kohsuke.github.function;

import java.io.IOException;
import java.io.InputStream;

/**
 * A functional interface, equivalent to {@link java.util.function.Function} but that allows throwing {@link Throwable}
 *
 */
@FunctionalInterface
public interface InputStreamFunction<R> extends FunctionThrows<InputStream, R, IOException> {
}
