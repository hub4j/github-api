package org.kohsuke.github;

import org.junit.Assert;
import org.junit.Before;
import org.kohsuke.randname.RandomNameGenerator;

import java.io.File;

/**
 * @author Kohsuke Kawaguchi
 */
public abstract class AbstractGitHubApiTestBase extends Assert {

    protected GitHub gitHub;

    @Before
    public void setUp() throws Exception {
        File f = new File(System.getProperty("user.home"), ".github.kohsuke2");
        if (f.exists()) {
            // use the non-standard credential preferentially, so that developers of this library do not have
            // to clutter their event stream.
            gitHub = GitHubBuilder.fromPropertyFile(f.getPath()).withRateLimitHandler(RateLimitHandler.FAIL).build();
        } else {
            gitHub = GitHubBuilder.fromCredentials().withRateLimitHandler(RateLimitHandler.FAIL).build();
        }
    }

    protected static final RandomNameGenerator rnd = new RandomNameGenerator();
}
