package org.kohsuke.github.function;

import java.io.IOException;
import java.io.InputStream;

/**
 * A functional interface, equivalent to {@link java.util.function.Consumer} but that takes an {@link InputStream} and
 * can throw an {@link IOException}
 */
@FunctionalInterface
public interface InputStreamConsumer extends ConsumerThrows<InputStream, IOException> {
}
