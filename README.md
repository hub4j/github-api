# Java API for GitHub

## How to make release a change
1. Make a change in the code
2. Update the version in the `pom.xml` file by incrementing the version number
3. Ensure it compiles and commit the change
4. Run the following command to create a release
	```bash
 	mvn package -DskipTests
	```
5. Upload the jar file to Nexus:
   - log in to https://public-nexus.ecwid.com
   - click Upload -> thirdparty
   - choose your jar file from `target` directory
   - fill _Group ID_, _Artifact ID_ and _Version_ from pom.xml (`org.kohsuke`:`github-api`:`<version>`)
   - enable _Generate a POM file with these coordinates_
   - click Upload

Now the new version is available to be used in main project dependencies.

---

[![Sonatype Nexus (Releases)](https://img.shields.io/nexus/r/org.kohsuke/github-api?server=https%3A%2F%2Foss.sonatype.org)](https://mvnrepository.com/artifact/org.kohsuke/github-api)
[![Join the chat at https://gitter.im/hub4j/github-api](https://badges.gitter.im/Join%20Chat.svg)](https://gitter.im/hub4j/github-api?utm_source=badge&utm_medium=badge&utm_campaign=pr-badge&utm_content=badge)
![CI](https://github.com/hub4j/github-api/workflows/CI/badge.svg?branch=main)
[![codecov](https://codecov.io/gh/hub4j/github-api/branch/main/graph/badge.svg?token=j1jQqydZLJ)](https://codecov.io/gh/hub4j/github-api)


See https://github-api.kohsuke.org/ for more details
