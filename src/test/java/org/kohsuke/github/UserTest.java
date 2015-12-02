package org.kohsuke.github;

import org.junit.Test;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

/**
 * @author Kohsuke Kawaguchi
 */
public class UserTest extends AbstractGitHubApiTestBase {
    @Test
    public void listFollowsAndFollowers() throws IOException {
        GHUser u = gitHub.getUser("rtyler");
        assertNotEquals(
            count50(u.listFollowers()),
            count50(u.listFollows()));
    }

    private Set<GHUser> count50(PagedIterable<GHUser> l) {
        Set<GHUser> users = new HashSet<GHUser>();
        PagedIterator<GHUser> itr = l.iterator();
        for (int i=0; i<50 && itr.hasNext(); i++) {
            users.add(itr.next());
        }
        assertEquals(50, users.size());
        return users;
    }
}
