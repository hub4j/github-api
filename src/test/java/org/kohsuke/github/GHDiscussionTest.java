package org.kohsuke.github;

import org.junit.Assert;
import org.junit.Before;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

/**
 * @author Charles Moulliard
 */
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class GHDiscussionTest extends AbstractGitHubWireMockTest {
    private final String TEAM_SLUG = "dummy-team";
    private GHDiscussion discussion;

    @Before
    public void setUp() throws Exception {
        discussion = gitHub.getOrganization(GITHUB_API_TEST_ORG)
                .getTeamBySlug(TEAM_SLUG)
                .createDiscussion("Dummy")
                .body("This is a dummy discussion")
                .create();
    }

    @Test
    public void testCreatedDiscussion() throws IOException {
        Assert.assertNotNull(discussion);
        Assert.assertEquals("Dummy", discussion.getTitle());
        Assert.assertEquals("This is a dummy discussion", discussion.getBody());
    }

    @Test
    public void testGetAndEditDiscussion() throws IOException {
        discussion.setBody("This is a dummy discussion changed");
        discussion.setTitle("Dummy title changed");
        discussion = gitHub.getOrganization(GITHUB_API_TEST_ORG)
                .getTeamBySlug(TEAM_SLUG)
                .getDiscussion(discussion.getNumber());
        Assert.assertEquals("Dummy title changed", discussion.getTitle());
        Assert.assertEquals("This is a dummy discussion changed", discussion.getBody());
    }

    @Test
    public void testListDiscussion() throws IOException {
        Set<GHDiscussion> all = new HashSet<GHDiscussion>();
        for (GHDiscussion d : gitHub.getOrganization(GITHUB_API_TEST_ORG).getTeamBySlug(TEAM_SLUG).listDiscussions()) {
            all.add(d);
        } ;
        assertFalse(all.isEmpty());
    }

    @Test
    public void testToDeleteDiscussion() throws IOException {
        discussion.delete(discussion.getNumber());
        try {
            discussion = gitHub.getOrganization(GITHUB_API_TEST_ORG)
                    .getTeamBySlug(TEAM_SLUG)
                    .getDiscussion(discussion.getNumber());
            Assert.assertNull(discussion);
        } catch (FileNotFoundException e) {
            discussion = null;
        }
    }

}
