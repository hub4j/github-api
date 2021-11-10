package org.kohsuke.github.function;

import org.kohsuke.github.connector.GitHubConnectorResponse;

import java.io.IOException;

/**
 * Represents a supplier of results that can throw.
 *
 * @param <T>
 *            the type of results supplied by this supplier
 */
@FunctionalInterface
public interface BodyHandler<T> extends FunctionThrows<GitHubConnectorResponse, T, IOException> {
}
