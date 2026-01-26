package org.kohsuke.github.internal;

import org.kohsuke.github.connector.GitHubConnectorResponse;

import java.io.IOException;
import java.util.Map;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;

/**
 * Interface for JSON serialization/deserialization operations.
 *
 * <p>
 * This interface abstracts Jackson-specific operations to allow supporting multiple Jackson versions (2.x and 3.x)
 * simultaneously. Implementations handle the version-specific details while providing a consistent API.
 * </p>
 *
 * <h2>Available Implementations</h2>
 * <ul>
 * <li>{@link GitHubJackson2} - Jackson 2.x implementation (default)</li>
 * <li>{@link GitHubJackson3} - Jackson 3.x implementation (requires additional dependencies)</li>
 * </ul>
 *
 * <h2>Configuration</h2>
 * <p>
 * Use {@link org.kohsuke.github.GitHubBuilder#useJackson3()} to configure the client to use Jackson 3.x:
 * </p>
 *
 * <pre>
 * // Use Jackson 3.x
 * GitHub github = new GitHubBuilder().useJackson3().build();
 * </pre>
 *
 * @author Pierre Villard
 * @see DefaultGitHubJackson
 * @see GitHubJackson2
 * @see GitHubJackson3
 */
public interface GitHubJackson {

    /**
     * Creates injectable values map with standard GitHub API values pre-populated.
     *
     * @param connectorResponse
     *            the connector response (may be null)
     * @return a map suitable for passing to read methods
     */
    @Nonnull
    Map<String, Object> createInjectableValues(@CheckForNull GitHubConnectorResponse connectorResponse);

    /**
     * Gets the name/version of this Jackson implementation for logging purposes.
     *
     * @return a string identifying this implementation (e.g., "Jackson 2.21.0" or "Jackson 3.0.3")
     */
    @Nonnull
    String getImplementationName();

    /**
     * Reads a JSON string into an object of the specified type.
     *
     * @param <T>
     *            the type to deserialize to
     * @param json
     *            the JSON string to parse
     * @param type
     *            the target class
     * @param injectedValues
     *            values to inject during deserialization (may be null)
     * @return the deserialized object
     * @throws IOException
     *             if there is an I/O error or parsing error
     */
    @CheckForNull
    <T> T readValue(@Nonnull String json, @Nonnull Class<T> type, @CheckForNull Map<String, Object> injectedValues)
            throws IOException;

    /**
     * Reads a JsonNode into an object of the specified type.
     *
     * <p>
     * This method handles the version-specific way of reading from a tree node. In Jackson 2.x, this uses
     * {@code traverse()}, while in Jackson 3.x it uses a different approach.
     * </p>
     *
     * @param <T>
     *            the type to deserialize to
     * @param node
     *            the JSON node (JsonNode from either Jackson version)
     * @param type
     *            the target class
     * @param injectedValues
     *            values to inject during deserialization (may be null)
     * @return the deserialized object
     * @throws IOException
     *             if there is an I/O error or parsing error
     */
    @CheckForNull
    <T> T readValueFromNode(@Nonnull Object node,
            @Nonnull Class<T> type,
            @CheckForNull Map<String, Object> injectedValues) throws IOException;

    /**
     * Reads a JSON string and updates an existing object instance.
     *
     * @param <T>
     *            the type of the object
     * @param json
     *            the JSON string to parse
     * @param instance
     *            the object to update with parsed data
     * @param injectedValues
     *            values to inject during deserialization (may be null)
     * @return the updated object instance
     * @throws IOException
     *             if there is an I/O error or parsing error
     */
    @CheckForNull
    <T> T readValueToUpdate(@Nonnull String json, @Nonnull T instance, @CheckForNull Map<String, Object> injectedValues)
            throws IOException;

    /**
     * Writes an object to a JSON byte array.
     *
     * @param value
     *            the object to serialize
     * @return the JSON as a byte array
     * @throws IOException
     *             if there is an I/O error or serialization error
     */
    @Nonnull
    byte[] writeValueAsBytes(@Nonnull Object value) throws IOException;
}
