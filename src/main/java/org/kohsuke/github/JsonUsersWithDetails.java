package org.kohsuke.github;

import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * @author Kohsuke Kawaguchi
 */
class JsonUsersWithDetails {
    public List<GHUser> users;

    public Set<GHUser> toSet(GitHub root) throws IOException {
        Set<GHUser> r = new HashSet<GHUser>();
        for (GHUser u : users)
            r.add(root.getUser(u));
        return r;
    }
}
