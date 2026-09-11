package org.kohsuke.github.internal;

import com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.TreeNode;
import com.fasterxml.jackson.core.Version;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.InjectableValues;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.introspect.VisibilityChecker;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.kohsuke.github.GitHub;
import org.kohsuke.github.GitHubRequest;
import org.kohsuke.github.connector.GitHubConnectorRequest;
import org.kohsuke.github.connector.GitHubConnectorResponse;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;

/**
 * Jackson 2.x implementation of {@link GitHubJackson}.
 *
 * <p>
 * This implementation uses Jackson 2.x APIs ({@code com.fasterxml.jackson.*}).
 * </p>
 *
 * @author Pierre Villard
 */
public class GitHubJackson2 implements GitHubJackson {

    private static final ObjectMapper MAPPER = JsonMapper.builder()
            .addModule(new JavaTimeModule())
            .visibility(new VisibilityChecker.Std(Visibility.NONE,
                    Visibility.NONE,
                    Visibility.NONE,
                    Visibility.NONE,
                    Visibility.ANY))
            .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
            .enable(MapperFeature.ACCEPT_CASE_INSENSITIVE_ENUMS)
            .propertyNamingStrategy(PropertyNamingStrategies.SNAKE_CASE)
            .build();

    private final String version;

    /**
     * Creates a new GitHubJackson2 instance.
     */
    public GitHubJackson2() {
        Version jacksonVersion = MAPPER.version();
        this.version = "Jackson " + jacksonVersion.getMajorVersion() + "." + jacksonVersion.getMinorVersion() + "."
                + jacksonVersion.getPatchLevel();
    }

    /**
     * Adds a value to the injectable values map for the GitHub root object.
     *
     * @param injectedValues
     *            the map to modify
     * @param root
     *            the GitHub root object to add
     */
    public void addGitHubRoot(@Nonnull Map<String, Object> injectedValues, @Nonnull GitHub root) {
        injectedValues.put(GitHub.class.getName(), root);
    }

    @Override
    @Nonnull
    public Map<String, Object> createInjectableValues(@CheckForNull GitHubConnectorResponse connectorResponse) {
        Map<String, Object> injected = new HashMap<>();

        // Required or many things break
        injected.put(GitHubConnectorResponse.class.getName(), null);
        injected.put(GitHub.class.getName(), null);

        if (connectorResponse != null) {
            injected.put(GitHubConnectorResponse.class.getName(), connectorResponse);
            GitHubConnectorRequest request = connectorResponse.request();
            // This is cheating, but it is an acceptable cheat for now.
            // GitHubRequest has additional injectable values
            if (request instanceof GitHubRequest) {
                injected.putAll(((GitHubRequest) request).injectedMappingValues());
            }
        }
        return injected;
    }

    @Override
    @Nonnull
    public String getImplementationName() {
        return version;
    }

    /**
     * Gets an ObjectReader configured with injectable values.
     *
     * <p>
     * This method is exposed for compatibility with code that still needs direct access to ObjectReader.
     * </p>
     *
     * @param injectedValues
     *            values to inject during deserialization
     * @return a configured ObjectReader
     */
    @Nonnull
    public ObjectReader getReader(@CheckForNull Map<String, Object> injectedValues) {
        return createReader(injectedValues);
    }

    /**
     * Gets an ObjectWriter.
     *
     * <p>
     * This method is exposed for compatibility with code that still needs direct access to ObjectWriter.
     * </p>
     *
     * @return an ObjectWriter
     */
    @Nonnull
    public ObjectWriter getWriter() {
        return MAPPER.writer();
    }

    @Override
    @CheckForNull
    public <T> T readValue(@Nonnull String json,
            @Nonnull Class<T> type,
            @CheckForNull Map<String, Object> injectedValues) throws IOException {
        try {
            ObjectReader reader = createReader(injectedValues);
            return reader.forType(type).readValue(json);
        } catch (JsonProcessingException e) {
            throw new GitHubJacksonException("Failed to deserialize JSON", e);
        }
    }

    @Override
    @CheckForNull
    public <T> T readValueFromNode(@Nonnull Object node,
            @Nonnull Class<T> type,
            @CheckForNull Map<String, Object> injectedValues) throws IOException {
        if (!(node instanceof TreeNode)) {
            throw new IllegalArgumentException("Node must be a Jackson 2.x TreeNode");
        }
        try {
            ObjectReader reader = createReader(injectedValues);
            return reader.forType(type).readValue(((TreeNode) node).traverse());
        } catch (JsonProcessingException e) {
            throw new GitHubJacksonException("Failed to deserialize JSON from node", e);
        }
    }

    @Override
    @CheckForNull
    public <T> T readValueToUpdate(@Nonnull String json,
            @Nonnull T instance,
            @CheckForNull Map<String, Object> injectedValues) throws IOException {
        try {
            ObjectReader reader = createReader(injectedValues);
            return reader.withValueToUpdate(instance).readValue(json);
        } catch (JsonProcessingException e) {
            throw new GitHubJacksonException("Failed to deserialize JSON", e);
        }
    }

    @Override
    @Nonnull
    public byte[] writeValueAsBytes(@Nonnull Object value) throws IOException {
        try {
            ObjectWriter writer = MAPPER.writer();
            return writer.writeValueAsBytes(value);
        } catch (JsonProcessingException e) {
            throw new GitHubJacksonException("Failed to serialize object to JSON", e);
        }
    }

    private ObjectReader createReader(@CheckForNull Map<String, Object> injectedValues) {
        if (injectedValues == null || injectedValues.isEmpty()) {
            Map<String, Object> defaultValues = new HashMap<>();
            defaultValues.put(GitHubConnectorResponse.class.getName(), null);
            defaultValues.put(GitHub.class.getName(), null);
            return MAPPER.reader(new InjectableValues.Std(defaultValues));
        }
        return MAPPER.reader(new InjectableValues.Std(injectedValues));
    }
}
