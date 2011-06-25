package org.kohsuke.github;

import java.util.List;

/**
 * @author Kohsuke Kawaguchi
 */
class JsonIssueComments {
    List<GHIssueComment> comments;

    List<GHIssueComment> wrap(GHIssue owner) {
        for (GHIssueComment c : comments)
            c.owner = owner;
        return comments;
    }
}
