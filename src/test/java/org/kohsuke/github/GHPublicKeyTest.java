package org.kohsuke.github;

import org.junit.Test;

/**
 * The Class GHPublicKeyTest.
 *
 * @author Jonas van Vliet
 */
public class GHPublicKeyTest extends AbstractGitHubWireMockTest {

    private static final String TMP_KEY_NAME = "Temporary user key";
    private static final String WIREMOCK_SSH_PUBLIC_KEY = "ssh-rsa AAAAB3NzaC1yc2EAAAADAQABAAAAgQDepW2/BSVFM2AfuGGsvi+vjQzC0EBD3R+/7PNEvP0/nvTWxiC/tthfvvCJR6TKrsprCir5tiJFm73gX+K18W0RKYpkyg8H6d1eZu3q/JOiGvoDPeN8Oe9hOGeeexw1WOiz7ESPHzZYXI981evzHAzxxn8zibr2EryopVNsXyoenw==";

    /**
     * Test adding a public key to the user
     *
     * @throws Exception
     *             the exception
     */
    @Test
    public void testAddPublicKey() throws Exception {
        GHKey newPublicKey = null;
        try {
            GHMyself me = gitHub.getMyself();
            newPublicKey = me.addPublicKey(TMP_KEY_NAME, WIREMOCK_SSH_PUBLIC_KEY);
        } finally {
            if (newPublicKey != null) {
                newPublicKey.delete();
            }
        }
    }
}
