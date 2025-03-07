package org.kohsuke.github;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.List;

import static org.hamcrest.Matchers.*;

// TODO: Auto-generated Javadoc
/**
 * The type Gh autolink test.
 */
public class GHAutolinkTest extends AbstractGitHubWireMockTest {

    private GHRepository repo;

    /**
     * Instantiates a new Gh autolink test.
     */
    public GHAutolinkTest() {
    }

    /**
     * Sets up.
     *
     * @throws Exception
     *             the exception
     */
    @Before
    public void setUp() throws Exception {
        repo = gitHub.getRepository("Alaurant/github-api-test");
        if (repo == null) {
            throw new IllegalStateException("Failed to initialize repository");
        }
    }

    /**
     * Test create autolink.
     *
     * @throws Exception
     *             the exception
     */
    @Test
    public void testCreateAutolink() throws Exception {
        String keyPrefix = "EXAMPLE-";
        String urlTemplate = "https://example.com/TICKET?q=<num>";
        boolean isAlphanumeric = true;

        GHAutolink autolink = repo.createAutolink()
                .withKeyPrefix(keyPrefix)
                .withUrlTemplate(urlTemplate)
                .withIsAlphanumeric(isAlphanumeric)
                .create();

        assertThat(autolink.getId(), notNullValue());
        assertThat(autolink.getKeyPrefix(), equalTo(keyPrefix));
        assertThat(autolink.getUrlTemplate(), equalTo(urlTemplate));
        assertThat(autolink.isAlphanumeric(), equalTo(isAlphanumeric));
        assertThat(autolink.getOwner(), equalTo(repo));

        autolink.delete();

    }

    /**
     * Test get autolink.
     *
     * @throws Exception
     *             the exception
     */
    @Test
    public void testReadAutolink() throws Exception {
        GHAutolink autolink = repo.createAutolink()
                .withKeyPrefix("JIRA-")
                .withUrlTemplate("https://example.com/test/<num>")
                .withIsAlphanumeric(false)
                .create();

        GHAutolink fetched = repo.readAutolink(autolink.getId());

        assertThat(fetched.getId(), equalTo(autolink.getId()));
        assertThat(fetched.getKeyPrefix(), equalTo(autolink.getKeyPrefix()));
        assertThat(fetched.getUrlTemplate(), equalTo(autolink.getUrlTemplate()));
        assertThat(fetched.isAlphanumeric(), equalTo(autolink.isAlphanumeric()));
        assertThat(fetched.getOwner(), equalTo(repo));

        autolink.delete();

    }

    /**
     * Test get autolinks.
     *
     * @throws Exception
     *             the exception
     */
    @Test
    public void testListAllAutolinks() throws Exception {
        assertThat("Initial autolinks list", repo.listAutolinks().toList(), is(empty()));

        GHAutolink autolink1 = repo.createAutolink()
                .withKeyPrefix("LIST-")
                .withUrlTemplate("https://example.com/list1/<num>")
                .withIsAlphanumeric(true)
                .create();

        GHAutolink autolink2 = repo.createAutolink()
                .withKeyPrefix("LISTED-")
                .withUrlTemplate("https://example.com/list2/<num>")
                .withIsAlphanumeric(false)
                .create();

        boolean found1 = false;
        boolean found2 = false;

        PagedIterable<GHAutolink> autolinks = repo.listAutolinks();

        List<GHAutolink> autolinkList = autolinks.toList();
        assertThat("Number of autolinks", autolinkList.size(), is(2));

        for (GHAutolink autolink : autolinkList) {

            if (autolink.getId() == autolink1.getId()) {
                found1 = true;
                assertThat(autolink.getKeyPrefix(), equalTo(autolink1.getKeyPrefix()));
                assertThat(autolink.getUrlTemplate(), equalTo(autolink1.getUrlTemplate()));
                assertThat(autolink.isAlphanumeric(), equalTo(autolink1.isAlphanumeric()));
            }
            if (autolink.getId() == autolink2.getId()) {
                found2 = true;
                assertThat(autolink.getKeyPrefix(), equalTo(autolink2.getKeyPrefix()));
                assertThat(autolink.getUrlTemplate(), equalTo(autolink2.getUrlTemplate()));
                assertThat(autolink.isAlphanumeric(), equalTo(autolink2.isAlphanumeric()));
            }
        }

        assertThat("First autolink", found1, is(true));
        assertThat("Second autolink", found2, is(true));

    }

    /**
     * Test delete autolink.
     *
     * @throws Exception
     *             the exception
     */
    @Test
    public void testDeleteAutolink() throws Exception {
        // Delete autolink using the instance method
        GHAutolink autolink = repo.createAutolink()
                .withKeyPrefix("DELETE-")
                .withUrlTemplate("https://example.com/delete/<num>")
                .withIsAlphanumeric(true)
                .create();

        autolink.delete();

        try {
            repo.readAutolink(autolink.getId());
            fail("Expected GHFileNotFoundException");
        } catch (GHFileNotFoundException e) {
            // Expected
        }

        // Delete autolink using repository delete method
        autolink = repo.createAutolink()
                .withKeyPrefix("DELETED-")
                .withUrlTemplate("https://example.com/delete2/<num>")
                .withIsAlphanumeric(true)
                .create();

        repo.deleteAutolink(autolink.getId());

        try {
            repo.readAutolink(autolink.getId());
            fail("Expected GHFileNotFoundException");
        } catch (GHFileNotFoundException e) {
            // Expected
        }
    }

    /**
     * Cleanup.
     */
    @After
    public void cleanup() {
        if (repo != null) {
            try {
                PagedIterable<GHAutolink> autolinks = repo.listAutolinks();
                for (GHAutolink autolink : autolinks) {
                    try {
                        autolink.delete();
                    } catch (Exception e) {
                        System.err.println("Failed to delete autolink: " + e.getMessage());
                    }
                }
            } catch (Exception e) {
                System.err.println("Cleanup failed: " + e.getMessage());
            }
        }
    }
}
