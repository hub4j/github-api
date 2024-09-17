package org.kohsuke.github;

import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;

/**
 * AOT test application so that Spring Boot is able to generate the native image resource JSON files. This class is only
 * required for test purpose.
 */
@SpringBootApplication
class AotTestApplication {

    /**
     * Runs a spring boot application to generate AOT hints
     *
     * @param args
     *            the command line arguments
     */
    public static void main(String[] args) {
        new SpringApplicationBuilder(AotTestApplication.class).run(args);
    }
}
