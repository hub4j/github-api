# Java API for GitHub

## How to make release a change
1. Make a change in the code
2. Update the version in the `pom.xml` file by incrementing the version number
3. Ensure it compiles and commit the change
4. Run the following command to create a release
	```	mvn package -DskipTests
	```
5. Upload the jar file to public-nexus.ecwid.com

[![Sonatype Nexus (Releases)](https://img.shields.io/nexus/r/org.kohsuke/github-api?server=https%3A%2F%2Foss.sonatype.org)](https://mvnrepository.com/artifact/org.kohsuke/github-api)
[![Join the chat at https://gitter.im/hub4j/github-api](https://badges.gitter.im/Join%20Chat.svg)](https://gitter.im/hub4j/github-api?utm_source=badge&utm_medium=badge&utm_campaign=pr-badge&utm_content=badge)
![CI](https://github.com/hub4j/github-api/workflows/CI/badge.svg?branch=main)
[![codecov](https://codecov.io/gh/hub4j/github-api/branch/main/graph/badge.svg?token=j1jQqydZLJ)](https://codecov.io/gh/hub4j/github-api)


See https://github-api.kohsuke.org/ for more details
