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

        String reflectConfigPath = "./target/classes/META-INF/native-image/org.kohsuke/" + artifactId
                + "/reflect-config.json";
        String noReflectPath = "./target/test-classes/no-reflect-and-serialization-list";
        String serializationConfigPath = "./target/classes/META-INF/native-image/org.kohsuke/" + artifactId
                + "/serialization-config.json";
        String generatedReflectConfigPath = "./target/spring-aot/test/resources/META-INF/native-image/org.kohsuke/"
                + artifactId + "/reflect-config.json";
        String generatedSerializationConfigPath = "./target/spring-aot/test/resources/META-INF/native-image/org.kohsuke/"
                + artifactId + "/serialization-config.json";

        Stream<String> reflectConfigNames = readAotConfigToStreamOfClassNames(reflectConfigPath);
        Stream<String> noReflectNames = Files.lines(Path.of(noReflectPath));
        Stream<String> serializationNames = readAotConfigToStreamOfClassNames(serializationConfigPath);
        List<String> allConfigClassNames = Stream
                .concat(serializationNames, Stream.concat(reflectConfigNames, noReflectNames))
                .distinct()
                .sorted()
                .collect(Collectors.toList());

        Stream<String> generatedReflectConfigNames = readAotConfigToStreamOfClassNames(generatedReflectConfigPath);
        Stream<String> generatedSerializationNames = readAotConfigToStreamOfClassNames(
                generatedSerializationConfigPath);
        List<String> allGeneratedConfigClassNames = Stream
                .concat(generatedReflectConfigNames, generatedSerializationNames)
                .distinct()
                .sorted()
                .collect(Collectors.toList());

        StringBuilder failures = new StringBuilder();

        allGeneratedConfigClassNames.forEach(generatedReflectionConfigClassName -> {
            try {
                if (!allConfigClassNames.contains(generatedReflectionConfigClassName)) {
                    failures.append(String.format(
                            Files.readString(
                                    Path.of("./target/test-classes/reflection-and-serialization-test-error-message")),
                            generatedReflectionConfigClassName)).append('\n');
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });

        // Future cleanup
        // allConfigClassNames.forEach(reflectionConfigClassName -> {
        // if (!allGeneratedConfigClassNames.contains(reflectionConfigClassName)) {
        // failures.append(
        // String.format("Extra class name found in config files: %1$s\n", reflectionConfigClassName));
        // }
        // });

        // Report all failures at once rather than one at a time
        String failureString = failures.toString();
        if (!failureString.isEmpty()) {
            fail("\n" + failureString);
        }
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
