package org.kohsuke.github;

import org.apache.commons.io.IOUtils;
import org.junit.Assert;
import org.junit.Before;
import org.kohsuke.github.GitHub;

import java.io.FileInputStream;
import java.util.Properties;

/**
 * @author Kohsuke Kawaguchi
 */
public abstract class AbstractGitHubApiTestBase extends Assert {

    protected GitHub gitHub;

    @Before
    public void setUp() throws Exception {
        Properties props = new Properties();
        java.io.File f = new java.io.File(System.getProperty("user.home"), ".github.kohsuke2");
        if (f.exists()) {
            FileInputStream in = new FileInputStream(f);
            try {
                props.load(in);
                gitHub = GitHub.connect(props.getProperty("login"),props.getProperty("oauth"));
            } finally {
                IOUtils.closeQuietly(in);
            }
        } else {
            gitHub = GitHub.connect();
        }
    }
}
