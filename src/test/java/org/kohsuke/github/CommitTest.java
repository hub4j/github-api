package org.kohsuke.github;

import org.junit.Test;

import java.io.IOException;

/**
 * @author Kohsuke Kawaguchi
 */
public class CommitTest extends AbstractGitHubApiTestBase {
    @Test // issue 152
    public void lastStatus() throws IOException {
        GHTag t = gitHub.getRepository("stapler/stapler").listTags().iterator().next();
        t.getCommit().getLastStatus();
    }
}
