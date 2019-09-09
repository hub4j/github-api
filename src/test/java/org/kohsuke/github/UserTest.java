package org.kohsuke.github;

import org.junit.Test;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

/**
 * @author Kohsuke Kawaguchi
 */
public class UserTest extends AbstractGitHubApiWireMockTest {
    @Test
    public void listFollowsAndFollowers() throws IOException {
        GHUser u = gitHub.getUser("rtyler");
        assertNotEquals(
            count30(u.listFollowers()),
            count30(u.listFollows()));
    }

    private Set<GHUser> count30(PagedIterable<GHUser> l) {
        Set<GHUser> users = new HashSet<GHUser>();
        PagedIterator<GHUser> itr = l.iterator();
        for (int i=0; i<30 && itr.hasNext(); i++) {
            users.add(itr.next());
        }
        assertEquals(30, users.size());
        return users;
    }
}
