package org.kohsuke.github.internal;

/**
 * Factory class for creating {@link GitHubJackson} implementations.
 *
 * <p>
 * This factory provides methods to create Jackson 2.x or Jackson 3.x implementations for JSON
 * serialization/deserialization.
 * </p>
 *
 * <h2>Usage</h2>
 *
 * <p>
 * By default, Jackson 3.x is used if available on the classpath, otherwise Jackson 2.x is used. To explicitly use
 * Jackson 2.x, configure the {@link org.kohsuke.github.GitHubBuilder}:
 * </p>
 *
 * <pre>
 * // Using default (Jackson 3.x if available, otherwise Jackson 2.x)
 * GitHub github = new GitHubBuilder().withOAuthToken("token").build();
 *
 * // Explicitly using Jackson 2.x
 * GitHub github = new GitHubBuilder().withOAuthToken("token").useJackson2().build();
 * </pre>
 *
 * <h2>Jackson 3.x Dependencies</h2>
 *
 * <p>
 * To use Jackson 3.x, add the {@code tools.jackson.core:jackson-databind} dependency to your project.
 * </p>
 *
 * @author Pierre Villard
 * @see GitHubJackson
 * @see GitHubJackson2
 * @see GitHubJackson3
 */
public final class DefaultGitHubJackson {

    /**
     * Creates the default {@link GitHubJackson} instance.
     *
     * <p>
     * This method returns a Jackson 3.x implementation if available on the classpath, otherwise it returns a Jackson
     * 2.x implementation.
     * </p>
     *
     * @return a GitHubJackson3 instance if Jackson 3.x is available, otherwise a GitHubJackson2 instance
     */
    public static GitHubJackson createDefault() {
        if (isJackson3Available()) {
            return new GitHubJackson3();
        } else {
            return new GitHubJackson2();
        }
    }

    /**
     * Creates a Jackson 2.x implementation.
     *
     * <p>
     * Jackson 2.x uses the {@code com.fasterxml.jackson} package and is the default implementation.
     * </p>
     *
     * @return a GitHubJackson2 instance
     */
    public static GitHubJackson2 createJackson2() {
        return new GitHubJackson2();
    }

    /**
     * Creates a Jackson 3.x implementation.
     *
     * <p>
     * Jackson 3.x uses the {@code tools.jackson} package and requires the Jackson 3.x dependencies to be present on the
     * classpath.
     * </p>
     *
     * @return a GitHubJackson3 instance
     * @throws IllegalStateException
     *             if Jackson 3.x is not available on the classpath
     */
    public static GitHubJackson3 createJackson3() {
        if (!isJackson3Available()) {
            throw new IllegalStateException("Jackson 3.x is not available on the classpath. "
                    + "Please add tools.jackson.core:jackson-databind and tools.jackson.datatype:jackson-datatype-jsr310 dependencies.");
        }
        return new GitHubJackson3();
    }

    /**
     * Checks if Jackson 3.x is available on the classpath.
     *
     * @return true if Jackson 3.x classes can be loaded
     */
    public static boolean isJackson3Available() {
        return GitHubJackson3.isAvailable();
    }

    private DefaultGitHubJackson() {
    }
}
