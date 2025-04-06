package org.kohsuke.github;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import org.junit.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import static org.junit.Assert.fail;

/**
 * AOT test to check if the required classes are registered for reflections / serialization. WARNING: This test needs to
 * be run with maven as a plugin is required to generate the AOT information first.
 */
@SpringBootTest
public class AotIntegrationTest {

    /**
     * Create default AotIntegrationTest instance
     */
    public AotIntegrationTest() {
    }

    /**
     * Test to check if all required classes are registered for AOT.
     *
     * @throws IOException
     *             if the files to test can not be read
     */
    @Test
    public void testIfAllRequiredClassesAreRegisteredForAot() throws IOException {
        String artifactId = System.getProperty("test.projectArtifactId", "default");

        Stream<String> providedReflectionConfigStreamOfNames = readAotConfigToStreamOfClassNames(
                "./target/classes/META-INF/native-image/org.kohsuke/" + artifactId + "/reflect-config.json");
        Stream<String> providedNoReflectStreamOfNames = Files
                .lines(Path.of("./target/test-classes/no-reflect-and-serialization-list"));
        Stream<String> providedSerializationStreamOfNames = readAotConfigToStreamOfClassNames(
                "./target/classes/META-INF/native-image/org.kohsuke/" + artifactId + "/serialization-config.json");
        Stream<String> providedAotConfigClassNamesPart = Stream
                .concat(providedSerializationStreamOfNames,
                        Stream.concat(providedReflectionConfigStreamOfNames, providedNoReflectStreamOfNames))
                .distinct();
        List<String> providedReflectionAndNoReflectionConfigNames = providedAotConfigClassNamesPart
                .collect(Collectors.toList());

        Stream<String> generatedReflectConfigStreamOfClassNames = readAotConfigToStreamOfClassNames(
                "./target/spring-aot/test/resources/META-INF/native-image/org.kohsuke/" + artifactId
                        + "/reflect-config.json");
        Stream<String> generatedSerializationStreamOfNames = readAotConfigToStreamOfClassNames(
                "./target/spring-aot/test/resources/META-INF/native-image/org.kohsuke/" + artifactId
                        + "/serialization-config.json");
        Stream<String> generatedAotConfigClassNames = Stream.concat(generatedReflectConfigStreamOfClassNames,
                generatedSerializationStreamOfNames);

        generatedAotConfigClassNames.forEach(generatedReflectionConfigClassName -> {
            try {
                if (!providedReflectionAndNoReflectionConfigNames.contains(generatedReflectionConfigClassName)) {
                    fail(String.format(
                            Files.readString(
                                    Path.of("./target/test-classes/reflection-and-serialization-test-error-message")),
                            generatedReflectionConfigClassName));
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });

    }

    private Stream<String> readAotConfigToStreamOfClassNames(String reflectionConfig) throws IOException {
        byte[] reflectionConfigFileAsBytes = Files.readAllBytes(Path.of(reflectionConfig));
        ArrayNode reflectConfigJsonArray = (ArrayNode) new ObjectMapper().readTree(reflectionConfigFileAsBytes);
        return StreamSupport
                .stream(Spliterators.spliteratorUnknownSize(reflectConfigJsonArray.iterator(), Spliterator.ORDERED),
                        false)
                .map(jsonNode -> jsonNode.get("name"))
                .map(JsonNode::toString)
                .map(reflectConfigEntryClassName -> reflectConfigEntryClassName.replace("\"", ""))
                .filter(x -> x.contains("org.kohsuke.github"))
                .filter(x -> !x.contains("org.kohsuke.github.AotTest"));
    }
}
