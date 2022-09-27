package org.kohsuke.github;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Set;

import static org.hamcrest.Matchers.*;

// TODO: Auto-generated Javadoc
/**
 * The Class GHDiscussionTest.
 *
 * @author Charles Moulliard
 */
public class GHDiscussionTest extends AbstractGitHubWireMockTest {
    private final String TEAM_SLUG = "dummy-team";
    private GHTeam team;

    /**
     * Sets the up.
     *
     * @throws Exception the exception
     */
    @Before
    public void setUp() throws Exception {
        team = gitHub.getOrganization(GITHUB_API_TEST_ORG).getTeamBySlug(TEAM_SLUG);
    }

    /**
     * Cleanup discussions.
     *
     * @throws Exception the exception
     */
    @After
    public void cleanupDiscussions() throws Exception {
        // only need to clean up if we're pointing to the live site
        if (mockGitHub.isUseProxy()) {
            for (GHDiscussion discussion : getNonRecordingGitHub().getOrganization(GITHUB_API_TEST_ORG)
                    .getTeamBySlug(TEAM_SLUG)
                    .listDiscussions()) {
                discussion.delete();
            }
        }
    }

    /**
     * Test created discussion.
     *
     * @throws IOException Signals that an I/O exception has occurred.
     */
    @Test
    public void testCreatedDiscussion() throws IOException {
        GHDiscussion discussion = team.createDiscussion("Some Discussion").body("This is a public discussion").done();
        assertThat(discussion, notNullValue());
        assertThat(discussion.getTeam(), equalTo(team));
        assertThat(discussion.getTitle(), equalTo("Some Discussion"));
        assertThat(discussion.getBody(), equalTo("This is a public discussion"));
        assertThat(discussion.isPrivate(), is(false));

        discussion = team.createDiscussion("Some Discussion")
                .body("This is another public discussion")
                .private_(false)
                .done();
        assertThat(discussion, notNullValue());
        assertThat(discussion.getTeam(), equalTo(team));
        assertThat(discussion.getTitle(), equalTo("Some Discussion"));
        assertThat(discussion.getBody(), equalTo("This is another public discussion"));
        assertThat(discussion.isPrivate(), is(false));

        discussion = team.createDiscussion("Some Discussion")
                .body("This is a private (secret) discussion")
                .private_(true)
                .done();
        assertThat(discussion, notNullValue());
        assertThat(discussion.getTeam(), equalTo(team));
        assertThat(discussion.getTitle(), equalTo("Some Discussion"));
        assertThat(discussion.getBody(), equalTo("This is a private (secret) discussion"));
        assertThat(discussion.isPrivate(), is(true));

        try {
            team.createDiscussion("Some Discussion").done();
            fail("Body is required.");
        } catch (HttpException e) {
            assertThat(e, instanceOf(HttpException.class));
            assertThat(e.getMessage(),
                    containsString("https://developer.github.com/v3/teams/discussions/#create-a-discussion"));
        }
    }

    /**
     * Test get and edit discussion.
     *
     * @throws IOException Signals that an I/O exception has occurred.
     */
    @Test
    public void testGetAndEditDiscussion() throws IOException {
        GHDiscussion created = team.createDiscussion("Some Discussion").body("This is a test discussion").done();

        GHDiscussion discussion = team.getDiscussion(created.getNumber());

        // Test convenience getId() override
        assertThat(discussion.getNumber(), equalTo(created.getId()));
        assertThat(discussion.getTeam(), equalTo(team));
        assertThat(discussion.getTitle(), equalTo("Some Discussion"));
        assertThat(discussion.getBody(), equalTo("This is a test discussion"));
        assertThat(discussion.isPrivate(), is(false));

        // Test equality
        assertThat(discussion, equalTo(created));

        discussion = discussion.set().body("This is a test discussion changed");
        assertThat(discussion.getTeam(), notNullValue());

        assertThat(discussion.getTitle(), equalTo("Some Discussion"));
        assertThat(discussion.getBody(), equalTo("This is a test discussion changed"));

        discussion = discussion.set().title("Title changed");

        assertThat(discussion.getTitle(), equalTo("Title changed"));
        assertThat(discussion.getBody(), equalTo("This is a test discussion changed"));

        GHDiscussion discussion2 = gitHub.getOrganization(GITHUB_API_TEST_ORG)
                .getTeamBySlug(TEAM_SLUG)
                .getDiscussion(discussion.getNumber());

        assertThat(discussion2, equalTo(discussion));
        assertThat(discussion2.getTitle(), equalTo("Title changed"));
        assertThat(discussion2.getBody(), equalTo("This is a test discussion changed"));

        discussion = discussion.update().body("This is a test discussion updated").title("Title updated").done();

        assertThat(discussion.getTeam(), notNullValue());
        assertThat(discussion.getTitle(), equalTo("Title updated"));
        assertThat(discussion.getBody(), equalTo("This is a test discussion updated"));

    }

    /**
     * Test list discussion.
     *
     * @throws IOException Signals that an I/O exception has occurred.
     */
    @Test
    public void testListDiscussion() throws IOException {
        team.createDiscussion("Some Discussion A").body("This is a test discussion").done();
        team.createDiscussion("Some Discussion B").body("This is a test discussion").done();
        team.createDiscussion("Some Discussion C").body("This is a test discussion").done();

        Set<GHDiscussion> all = team.listDiscussions().toSet();
        assertThat(all.size(), equalTo(3));
    }

    /**
     * Test to delete discussion.
     *
     * @throws IOException Signals that an I/O exception has occurred.
     */
    @Test
    public void testToDeleteDiscussion() throws IOException {
        GHDiscussion discussion = team.createDiscussion("Some Discussion").body("This is a test discussion").done();

        assertThat(discussion.getTitle(), equalTo("Some Discussion"));

        discussion.delete();
        try {
            gitHub.getOrganization(GITHUB_API_TEST_ORG).getTeamBySlug(TEAM_SLUG).getDiscussion(discussion.getNumber());
            fail();
        } catch (FileNotFoundException e) {
            assertThat(e.getMessage(),
                    containsString("https://developer.github.com/v3/teams/discussions/#get-a-single-discussion"));
        }
    }

}
