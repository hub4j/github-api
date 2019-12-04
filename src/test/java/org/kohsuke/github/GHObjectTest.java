package org.kohsuke.github;

import org.junit.Test;

import static org.hamcrest.Matchers.*;

public class GHObjectTest extends org.kohsuke.github.AbstractGitHubWireMockTest {

    @Test
    public void test_toString() throws Exception {
        GHOrganization org = gitHub.getOrganization(GITHUB_API_TEST_ORG);
        assertThat(org.toString(),
                containsString(
                        "login=github-api-test-org,location=<null>,blog=<null>,email=<null>,name=<null>,company=<null>,followers=0,following=0"));
    }
}