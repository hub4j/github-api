# Contributing

## Using WireMock and Snapshots

This project has started converting to using WireMock to stub out http responses instead of use live data.
This change will allow tests to run in a CI environment without needing to touch github.com.
The tests will instead serve previously recorded responses from local data files.

### Running WireMock tests

Example:

`mvn install -Dtest=WireMockStatusReporterTest`

This the default behavior.


### Setting up credential

1. Create an OAuth token on github.com
2. Set the GITHUB_OAUTH environment variable to the value of that token
3. Set the system property `test.github.useProxy` (usually like "-Dtest.github.useProxy" as a Java VM option)

    `mvn install -Dtest.github.useProxy -Dtest=WireMockStatusReporterTest`

4. The above should report no test failures and include the following console output:

    `WireMockStatusReporterTest: GitHub proxying and user auth correctly configured for user login: <your login>`

Whenever you run tests with `-Dtest.github.useProxy`, they will try to get data from local files but will fallback to proxying to github if not found.


### Writing a new test

Once you have credentials setup, you add new test classes and test methods as you would normally.
Keep `useProxy` enabled and iterate on your tests as needed. Remember, while proxying your tests are interacting with GitHub - you will need to clean up your state between runs.

When you are ready to create a snapshot of your test data,
run your test with `test.github.takeSnapshot` ("-Dtest.github.takeSnapshot" as a Java VM option).  For example:

    `mvn install -Dtest.github.takeSnapshot -Dtest=YourTestClassName`

The above command would create snapshot WireMock data files under the path `src/test/resources/org/kohsuhke/github/YourTestClassName/wiremock`.
Each method would get a separate director that would hold the data files for that test method.

Add all files including the generated data to your commit and submit a PR.

### Modifying existing tests

When modifying existing tests, you can change the stubbed WireMock data files by hand or you can try generating a new snapshot.

#### Manual editing of data (minor changes only)

If you know what data will change, it is sometimes simplest to make any required changes to the data files manually.
This can be easier if the changes are minor or when you development environment is not setup to to take updated snapshots.

#### Generating a new snapshot

For more most changes, it is recommended to take a new snapshot when updating tests.
Delete the wiremock data files for the test method you will be modifying.
For more significant changes, you can even delete the WireMock files for an entire test class.
Then follow the same as when writing a new test: run with proxy enabled to debug, take a new snapshot when done, commit everything, and submit the PR.
