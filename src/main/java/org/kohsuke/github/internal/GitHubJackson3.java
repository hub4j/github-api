package org.kohsuke.github.internal;

import com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility;
import org.kohsuke.github.GitHub;
import org.kohsuke.github.GitHubRequest;
import org.kohsuke.github.connector.GitHubConnectorRequest;
import org.kohsuke.github.connector.GitHubConnectorResponse;
import tools.jackson.core.JacksonException;
import tools.jackson.core.Version;
import tools.jackson.databind.DeserializationFeature;
import tools.jackson.databind.InjectableValues;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.MapperFeature;
import tools.jackson.databind.ObjectReader;
import tools.jackson.databind.ObjectWriter;
import tools.jackson.databind.PropertyNamingStrategies;
import tools.jackson.databind.json.JsonMapper;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;

/**
 * Jackson 3.x implementation of {@link GitHubJackson}.
 *
 * <p>
 * This implementation uses Jackson 3.x APIs ({@code tools.jackson.*}).
 * </p>
 *
 * <p>
 * To use Jackson 3.x, add the {@code tools.jackson.core:jackson-databind} dependency to your project.
 * </p>
 *
 * <p>
 * Then configure the GitHub client to use Jackson 3:
 * </p>
 *
 * <pre>
 * GitHub github = new GitHubBuilder().withJackson(DefaultGitHubJackson.createJackson3()).build();
 * </pre>
 *
 * @author Pierre Villard
 */
public class GitHubJackson3 implements GitHubJackson {

    private static final String JACKSON3_MARKER_CLASS = "tools.jackson.databind.json.JsonMapper";

    private static final JsonMapper MAPPER = JsonMapper.builder()
            // Java 8 date/time support is built-in to Jackson 3.x (no module needed)
            .changeDefaultVisibility(vc -> vc.withFieldVisibility(Visibility.ANY)
                    .withGetterVisibility(Visibility.NONE)
                    .withSetterVisibility(Visibility.NONE)
                    .withCreatorVisibility(Visibility.NONE)
                    .withIsGetterVisibility(Visibility.NONE))
            .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
            .enable(MapperFeature.ACCEPT_CASE_INSENSITIVE_ENUMS)
            .propertyNamingStrategy(PropertyNamingStrategies.SNAKE_CASE)
            .build();

    /**
     * Checks if Jackson 3.x is available on the classpath.
     *
     * @return true if Jackson 3.x classes can be loaded
     */
    public static boolean isAvailable() {
        try {
            Class.forName(JACKSON3_MARKER_CLASS);
            return true;
        } catch (ClassNotFoundException e) {
            return false;
        }
    }

    private final String version;

    /**
     * Creates a new GitHubJackson3 instance.
     */
    public GitHubJackson3() {
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
        } catch (JacksonException e) {
            throw new GitHubJacksonException("Failed to deserialize JSON", e);
        }
    }

    @Override
    @CheckForNull
    public <T> T readValueFromNode(@Nonnull Object node,
            @Nonnull Class<T> type,
            @CheckForNull Map<String, Object> injectedValues) throws IOException {
        if (!(node instanceof JsonNode)) {
            throw new IllegalArgumentException("Node must be a Jackson 3.x JsonNode");
        }
        try {
            ObjectReader reader = createReader(injectedValues);
            return reader.forType(type).readValue((JsonNode) node);
        } catch (JacksonException e) {
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
        } catch (JacksonException e) {
            throw new GitHubJacksonException("Failed to deserialize JSON", e);
        }
    }

    @Override
    @Nonnull
    public byte[] writeValueAsBytes(@Nonnull Object value) throws IOException {
        try {
            ObjectWriter writer = MAPPER.writer();
            return writer.writeValueAsBytes(value);
        } catch (JacksonException e) {
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
