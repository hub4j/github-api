package org.kohsuke.github;

import org.junit.Test;

import static org.junit.Assert.*;

public class GHObjectTest extends org.kohsuke.github.AbstractGitHubWireMockTest {

    @Test
    public void test_toString() throws Exception {
        GHOrganization org = gitHub.getOrganization(GITHUB_API_TEST_ORG);

    }
}