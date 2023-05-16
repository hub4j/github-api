# Java API for GitHub

[![Sonatype Nexus (Releases)](https://img.shields.io/nexus/r/org.kohsuke/github-api?server=https%3A%2F%2Foss.sonatype.org)](https://mvnrepository.com/artifact/org.kohsuke/github-api)
[![Join the chat at https://gitter.im/hub4j/github-api](https://badges.gitter.im/Join%20Chat.svg)](https://gitter.im/hub4j/github-api?utm_source=badge&utm_medium=badge&utm_campaign=pr-badge&utm_content=badge)
![CI](https://github.com/hub4j/github-api/workflows/CI/badge.svg?branch=main)
[![codecov](https://codecov.io/gh/hub4j/github-api/branch/main/graph/badge.svg?token=j1jQqydZLJ)](https://codecov.io/gh/hub4j/github-api)


See https://github-api.kohsuke.org/ for more details

To locally publish changes to this API, update the version in `pom.xml` to a unique identifier.
Then run `mvn install -Dmaven.test.skip -Dspotless.check.skip=true -Dgpg.skip -Dmaven.javadoc.skip=true -Djacoco.skip=true -Dspotbugs.skip`. 
To use this dependency in `brain-backend`, update the version of `cortexapps-github-api` in `build.gradle.kts (:web)` to the same identifier.
