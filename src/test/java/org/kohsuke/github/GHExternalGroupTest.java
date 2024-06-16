package org.kohsuke.github;

import org.junit.Test;

import java.io.IOException;
import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.kohsuke.github.ExternalGroupsTestingSupport.*;
import static org.kohsuke.github.ExternalGroupsTestingSupport.Matchers.*;

// TODO: Auto-generated Javadoc

/**
 * The Class GHExternalGroupTest.
 */
public class GHExternalGroupTest extends AbstractGitHubWireMockTest {

    /**
     * Test refresh bound external group.
     *
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     */
    @Test
    public void testRefreshBoundExternalGroup() throws IOException {
        GHOrganization org = gitHub.getOrganization(GITHUB_API_TEST_ORG);

        List<GHExternalGroup> groups = org.listExternalGroups().toList();
        final GHExternalGroup sut = findExternalGroup(groups, hasName("acme-developers"));

        assertThat(sut, isExternalGroupSummary());

        sut.refresh();

        assertThat(sut.getId(), equalTo(467431L));
        assertThat(sut.getName(), equalTo("acme-developers"));
        assertThat(sut.getUpdatedAt(), notNullValue());

        assertThat(sut.getMembers(), notNullValue());
        assertThat(membersSummary(sut),
                hasItems("158311279:john-doe_acme:John Doe:john.doe@acme.corp",
                        "166731041:jane-doe_acme:Jane Doe:jane.doe@acme.corp"));

        assertThat(sut.getTeams(), notNullValue());
        assertThat(teamSummary(sut), hasItems("9891173:ACME-DEVELOPERS"));
    }

    /**
     * Test get organization.
     *
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     */
    @Test
    public void testGetOrganization() throws IOException {
        GHOrganization org = gitHub.getOrganization(GITHUB_API_TEST_ORG);

        List<GHExternalGroup> groups = org.listExternalGroups().toList();
        final GHExternalGroup sut = findExternalGroup(groups, hasName("acme-developers"));

        assertThat(sut, isExternalGroupSummary());

        final GHOrganization other = sut.getOrganization();

        assertThat(other, is(org));
    }

}
