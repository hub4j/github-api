package org.kohsuke.github;

import org.jetbrains.annotations.NotNull;
import org.springframework.aot.hint.MemberCategory;
import org.springframework.aot.hint.RuntimeHints;
import org.springframework.aot.hint.RuntimeHintsRegistrar;
import org.springframework.aot.hint.TypeReference;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Gets all classes in the org/kohsuke/github package (and subpackages) and register them for the AOT reflection /
 * serialization.
 */
public class AotTestRuntimeHints implements RuntimeHintsRegistrar {

    private static final String CLASSPATH_IDENTIFIER = "/target/classes";

    private static final String LOCATION_PATTERN_OF_ORG_KOHSUKE_GITHUB_CLASSES = "classpath*:org/kohsuke/github/**/*.class";

    private static final Logger LOGGER = Logger.getLogger(AotTestRuntimeHints.class.getName());

    /**
     * Default constructor.
     */
    public AotTestRuntimeHints() {
    }

    @Override
    public void registerHints(@NotNull RuntimeHints hints, ClassLoader classLoader) {
        try {
            PathMatchingResourcePatternResolver pathMatchingResourcePatternResolver = new PathMatchingResourcePatternResolver();
            List<Resource> list = Arrays.asList(
                    pathMatchingResourcePatternResolver.getResources(LOCATION_PATTERN_OF_ORG_KOHSUKE_GITHUB_CLASSES));
            list.forEach(resource -> {
                try {
                    String resourceUriString = resource.getURI().toASCIIString();
                    // filter out only classes in the classpath to avoid classes in the test classpath
                    if (!resourceUriString.contains(CLASSPATH_IDENTIFIER)) {
                        return;
                    }
                    String substring = resourceUriString.substring(
                            resourceUriString.indexOf(CLASSPATH_IDENTIFIER) + CLASSPATH_IDENTIFIER.length() + 1);
                    String githubApiClassName = substring.replace('/', '.');
                    String githubApiClassNameWithoutClass = githubApiClassName.replace(".class", "");
                    hints.reflection()
                            .registerType(TypeReference.of(githubApiClassNameWithoutClass), MemberCategory.values());
                    hints.serialization().registerType(TypeReference.of(githubApiClassNameWithoutClass));
                    LOGGER.log(Level.INFO,
                            "Registered class " + githubApiClassNameWithoutClass
                                    + " for reflections and serialization for test purpose.");
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            });
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
