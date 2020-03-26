package org.kohsuke.github;

import org.junit.Test;

/**
 * @author Sourabh Sarvotham Parkala
 */
public class GHVerificationReasonTest extends AbstractGitHubWireMockTest {
    // Issue 737
    @Test
    public void testExpiredKeyVerification() throws Exception {
        GHRepository r = gitHub.getRepository("github-api/github-api");
        GHCommit commit = r.getCommit("86a2e245aa6d71d54923655066049d9e21a15f01");
        assertEquals(commit.getCommitShortInfo().getAuthor().getName(), "Sourabh Parkala");
        assertFalse(commit.getCommitShortInfo().getVerification().isVerified());
        assertEquals(commit.getCommitShortInfo().getVerification().getReason(), GHVerification.Reason.EXPIRED_KEY);
    }

    @Test
    public void testNotSigningKeyVerification() throws Exception {
        GHRepository r = gitHub.getRepository("github-api/github-api");
        GHCommit commit = r.getCommit("86a2e245aa6d71d54923655066049d9e21a15f02");
        assertEquals(commit.getCommitShortInfo().getAuthor().getName(), "Sourabh Parkala");
        assertFalse(commit.getCommitShortInfo().getVerification().isVerified());
        assertEquals(commit.getCommitShortInfo().getVerification().getReason(), GHVerification.Reason.NOT_SIGNING_KEY);
    }

    @Test
    public void testGpgverifyErrorVerification() throws Exception {
        GHRepository r = gitHub.getRepository("github-api/github-api");
        GHCommit commit = r.getCommit("86a2e245aa6d71d54923655066049d9e21a15f03");
        assertEquals(commit.getCommitShortInfo().getAuthor().getName(), "Sourabh Parkala");
        assertFalse(commit.getCommitShortInfo().getVerification().isVerified());
        assertEquals(commit.getCommitShortInfo().getVerification().getReason(), GHVerification.Reason.GPGVERIFY_ERROR);
    }

    @Test
    public void testGpgverifyUnavailableVerification() throws Exception {
        GHRepository r = gitHub.getRepository("github-api/github-api");
        GHCommit commit = r.getCommit("86a2e245aa6d71d54923655066049d9e21a15f04");
        assertEquals(commit.getCommitShortInfo().getAuthor().getName(), "Sourabh Parkala");
        assertFalse(commit.getCommitShortInfo().getVerification().isVerified());
        assertEquals(commit.getCommitShortInfo().getVerification().getReason(),
                GHVerification.Reason.GPGVERIFY_UNAVAILABLE);
    }

    @Test
    public void testUnsignedVerification() throws Exception {
        GHRepository r = gitHub.getRepository("github-api/github-api");
        GHCommit commit = r.getCommit("86a2e245aa6d71d54923655066049d9e21a15f05");
        assertEquals(commit.getCommitShortInfo().getAuthor().getName(), "Sourabh Parkala");
        assertFalse(commit.getCommitShortInfo().getVerification().isVerified());
        assertEquals(commit.getCommitShortInfo().getVerification().getReason(), GHVerification.Reason.UNSIGNED);
    }

    @Test
    public void testUnknownSignatureTypeVerification() throws Exception {
        GHRepository r = gitHub.getRepository("github-api/github-api");
        GHCommit commit = r.getCommit("86a2e245aa6d71d54923655066049d9e21a15f06");
        assertEquals(commit.getCommitShortInfo().getAuthor().getName(), "Sourabh Parkala");
        assertFalse(commit.getCommitShortInfo().getVerification().isVerified());
        assertEquals(commit.getCommitShortInfo().getVerification().getReason(),
                GHVerification.Reason.UNKNOWN_SIGNATURE_TYPE);
    }

    @Test
    public void testNoUserVerification() throws Exception {
        GHRepository r = gitHub.getRepository("github-api/github-api");
        GHCommit commit = r.getCommit("86a2e245aa6d71d54923655066049d9e21a15f07");
        assertEquals(commit.getCommitShortInfo().getAuthor().getName(), "Sourabh Parkala");
        assertFalse(commit.getCommitShortInfo().getVerification().isVerified());
        assertEquals(commit.getCommitShortInfo().getVerification().getReason(), GHVerification.Reason.NO_USER);
    }

    @Test
    public void testUnverifiedEmailVerification() throws Exception {
        GHRepository r = gitHub.getRepository("github-api/github-api");
        GHCommit commit = r.getCommit("86a2e245aa6d71d54923655066049d9e21a15f08");
        assertEquals(commit.getCommitShortInfo().getAuthor().getName(), "Sourabh Parkala");
        assertFalse(commit.getCommitShortInfo().getVerification().isVerified());
        assertEquals(commit.getCommitShortInfo().getVerification().getReason(), GHVerification.Reason.UNVERIFIED_EMAIL);
    }

    @Test
    public void testBadEmailVerification() throws Exception {
        GHRepository r = gitHub.getRepository("github-api/github-api");
        GHCommit commit = r.getCommit("86a2e245aa6d71d54923655066049d9e21a15f09");
        assertEquals(commit.getCommitShortInfo().getAuthor().getName(), "Sourabh Parkala");
        assertFalse(commit.getCommitShortInfo().getVerification().isVerified());
        assertEquals(commit.getCommitShortInfo().getVerification().getReason(), GHVerification.Reason.BAD_EMAIL);
    }

    @Test
    public void testUnknownKeyVerification() throws Exception {
        GHRepository r = gitHub.getRepository("github-api/github-api");
        GHCommit commit = r.getCommit("86a2e245aa6d71d54923655066049d9e21a15f10");
        assertEquals(commit.getCommitShortInfo().getAuthor().getName(), "Sourabh Parkala");
        assertFalse(commit.getCommitShortInfo().getVerification().isVerified());
        assertEquals(commit.getCommitShortInfo().getVerification().getReason(), GHVerification.Reason.UNKNOWN_KEY);
    }

    @Test
    public void testMalformedSignatureVerification() throws Exception {
        GHRepository r = gitHub.getRepository("github-api/github-api");
        GHCommit commit = r.getCommit("86a2e245aa6d71d54923655066049d9e21a15f11");
        assertEquals(commit.getCommitShortInfo().getAuthor().getName(), "Sourabh Parkala");
        assertFalse(commit.getCommitShortInfo().getVerification().isVerified());
        assertEquals(commit.getCommitShortInfo().getVerification().getReason(),
                GHVerification.Reason.MALFORMED_SIGNATURE);
    }

    @Test
    public void testInvalidVerification() throws Exception {
        GHRepository r = gitHub.getRepository("github-api/github-api");
        GHCommit commit = r.getCommit("86a2e245aa6d71d54923655066049d9e21a15f12");
        assertEquals(commit.getCommitShortInfo().getAuthor().getName(), "Sourabh Parkala");
        assertFalse(commit.getCommitShortInfo().getVerification().isVerified());
        assertEquals(commit.getCommitShortInfo().getVerification().getReason(), GHVerification.Reason.INVALID);
    }

    @Test
    public void testValidVerification() throws Exception {
        GHRepository r = gitHub.getRepository("github-api/github-api");
        GHCommit commit = r.getCommit("86a2e245aa6d71d54923655066049d9e21a15f13");
        assertEquals(commit.getCommitShortInfo().getAuthor().getName(), "Sourabh Parkala");
        assertTrue(commit.getCommitShortInfo().getVerification().isVerified());
        assertEquals(commit.getCommitShortInfo().getVerification().getReason(), GHVerification.Reason.VALID);
        assertNotNull(commit.getCommitShortInfo().getVerification().getPayload());
        assertNotNull(commit.getCommitShortInfo().getVerification().getSignature());
    }
}
