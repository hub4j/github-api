package org.kohsuke.github;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

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
        assertThat("Initial autolinks should be empty", repo.listAutolinks().toList(), is(empty()));

        try {
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

            List<GHAutolink> autolinks = repo.listAutolinks().toList();
            assertThat("Should have exactly 2 autolinks", ((List<?>) autolinks).size(), is(2));

            GHAutolink foundAutolink1 = autolinks.stream()
                    .filter(a -> a.getId().equals(autolink1.getId()))
                    .findFirst()
                    .orElseThrow(() -> new AssertionError("Autolink 1 not found"));

            GHAutolink foundAutolink2 = autolinks.stream()
                    .filter(a -> a.getId().equals(autolink2.getId()))
                    .findFirst()
                    .orElseThrow(() -> new AssertionError("Autolink 2 not found"));

            assertAutolinksEqual(autolink1, foundAutolink1);
            assertAutolinksEqual(autolink2, foundAutolink2);

        } catch (Exception e) {
            System.err.println("Failed to list autolinks: " + e.getMessage());
        }
    }

    private void assertAutolinksEqual(GHAutolink expected, GHAutolink actual) {
        assertThat(actual.getKeyPrefix(), equalTo(expected.getKeyPrefix()));
        assertThat(actual.getUrlTemplate(), equalTo(expected.getUrlTemplate()));
        assertThat(actual.isAlphanumeric(), equalTo(expected.isAlphanumeric()));
        assertThat(actual.getOwner(), equalTo(expected.getOwner()));
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
     *
     * @throws Exception
     *             the exception
     */
    @After
    public void cleanup() throws Exception {
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
