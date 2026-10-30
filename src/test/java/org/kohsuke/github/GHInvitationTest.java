package org.kohsuke.github;

import org.junit.Test;

import java.io.IOException;
import java.util.List;

import static org.hamcrest.Matchers.*;

/**
 * Tests for {@link GHInvitation}.
 *
 * @author Yoann Rodiere
 */
public class GHInvitationTest extends AbstractGitHubWireMockTest {

    /**
     * Create default GHInvitationTest instance
     */
    public GHInvitationTest() {
    }

    /**
     * Test that listing repository invitations returns correct data for all fields.
     *
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     */
    @Test
    public void testListInvitations() throws IOException {
        GHRepository repo = getRepository();
        List<GHInvitation> invitations = repo.listInvitations().toList();

        assertThat(invitations, hasSize(1));

        GHInvitation invitation = invitations.get(0);

        assertThat(invitation.getId(), equalTo(1L));
        assertThat(invitation.getNodeId(), equalTo("MDEwOlJlcG9zaXRvcnkxMjk2MjY5"));
        assertThat(invitation.getPermission(), equalTo("write"));
        assertThat(invitation.isExpired(), equalTo(false));
        assertThat(invitation.getHtmlUrl().toString(),
                equalTo("https://github.com/hub4j-test-org/github-api/invitations"));
        assertThat(invitation.getCreatedAt().toString(), equalTo("2016-06-13T14:52:50Z"));

        GHUser invitee = invitation.getInvitee();
        assertThat(invitee, notNullValue());
        assertThat(invitee.getLogin(), equalTo("octocat-invitee"));

        GHUser inviter = invitation.getInviter();
        assertThat(inviter, notNullValue());
        assertThat(inviter.getLogin(), equalTo("octocat"));

        GHRepository invitationRepo = invitation.getRepository();
        assertThat(invitationRepo, notNullValue());
        assertThat(invitationRepo.getName(), equalTo("github-api"));
    }

    private GHRepository getRepository(GitHub gitHub) throws IOException {
        return gitHub.getOrganization("hub4j-test-org").getRepository("github-api");
    }

    /**
     * Gets the repository.
     *
     * @return the repository
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     */
    protected GHRepository getRepository() throws IOException {
        return getRepository(gitHub);
    }
}
