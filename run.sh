#!/bin/bash

export GITHUB_OAUTH=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
export MAVEN_OPTS="--add-exports jdk.compiler/com.sun.tools.javac.api=ALL-UNNAMED \
                     --add-exports jdk.compiler/com.sun.tools.javac.file=ALL-UNNAMED \
                     --add-exports jdk.compiler/com.sun.tools.javac.parser=ALL-UNNAMED \
                     --add-exports jdk.compiler/com.sun.tools.javac.tree=ALL-UNNAMED \
                     --add-exports jdk.compiler/com.sun.tools.javac.util=ALL-UNNAMED"

mvn install -Dtest.github.useProxy -Dtest=WireMockStatusReporterTest