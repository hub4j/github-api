package org.kohsuke.github;

import org.junit.Test;

/**
 * @author Sourabh Sarvotham Parkala
 */
public class GHReasonTest extends AbstractGitHubWireMockTest {
    // Issue 737
    @Test
    public void testExpiredKeyVerification() throws Exception {
        GHRepository r = gitHub.getRepository("github-api/github-api");
        GHCommit commit = r.getCommit("86a2e245aa6d71d54923655066049d9e21a15f01");
        assertEquals(commit.getCommitShortInfo().getAuthor().getName(), "Sourabh Parkala");
        assertFalse(commit.getCommitShortInfo().getVerification().getVerified());
        assertEquals(commit.getCommitShortInfo().getVerification().getReason(), GHReason.expired_key);
    }

    @Test
    public void testNotSigningKeyVerification() throws Exception {
        GHRepository r = gitHub.getRepository("github-api/github-api");
        GHCommit commit = r.getCommit("86a2e245aa6d71d54923655066049d9e21a15f02");
        assertEquals(commit.getCommitShortInfo().getAuthor().getName(), "Sourabh Parkala");
        assertFalse(commit.getCommitShortInfo().getVerification().getVerified());
        assertEquals(commit.getCommitShortInfo().getVerification().getReason(), GHReason.not_signing_key);
    }

    @Test
    public void testGpgverifyErrorVerification() throws Exception {
        GHRepository r = gitHub.getRepository("github-api/github-api");
        GHCommit commit = r.getCommit("86a2e245aa6d71d54923655066049d9e21a15f03");
        assertEquals(commit.getCommitShortInfo().getAuthor().getName(), "Sourabh Parkala");
        assertFalse(commit.getCommitShortInfo().getVerification().getVerified());
        assertEquals(commit.getCommitShortInfo().getVerification().getReason(), GHReason.gpgverify_error);
    }

    @Test
    public void testGpgverifyUnavailableVerification() throws Exception {
        GHRepository r = gitHub.getRepository("github-api/github-api");
        GHCommit commit = r.getCommit("86a2e245aa6d71d54923655066049d9e21a15f04");
        assertEquals(commit.getCommitShortInfo().getAuthor().getName(), "Sourabh Parkala");
        assertFalse(commit.getCommitShortInfo().getVerification().getVerified());
        assertEquals(commit.getCommitShortInfo().getVerification().getReason(), GHReason.gpgverify_unavailable);
    }

    @Test
    public void testUnsignedVerification() throws Exception {
        GHRepository r = gitHub.getRepository("github-api/github-api");
        GHCommit commit = r.getCommit("86a2e245aa6d71d54923655066049d9e21a15f05");
        assertEquals(commit.getCommitShortInfo().getAuthor().getName(), "Sourabh Parkala");
        assertFalse(commit.getCommitShortInfo().getVerification().getVerified());
        assertEquals(commit.getCommitShortInfo().getVerification().getReason(), GHReason.unsigned);
    }

    @Test
    public void testUnknownSignatureTypeVerification() throws Exception {
        GHRepository r = gitHub.getRepository("github-api/github-api");
        GHCommit commit = r.getCommit("86a2e245aa6d71d54923655066049d9e21a15f06");
        assertEquals(commit.getCommitShortInfo().getAuthor().getName(), "Sourabh Parkala");
        assertFalse(commit.getCommitShortInfo().getVerification().getVerified());
        assertEquals(commit.getCommitShortInfo().getVerification().getReason(), GHReason.unknown_signature_type);
    }

    @Test
    public void testNoUserVerification() throws Exception {
        GHRepository r = gitHub.getRepository("github-api/github-api");
        GHCommit commit = r.getCommit("86a2e245aa6d71d54923655066049d9e21a15f07");
        assertEquals(commit.getCommitShortInfo().getAuthor().getName(), "Sourabh Parkala");
        assertFalse(commit.getCommitShortInfo().getVerification().getVerified());
        assertEquals(commit.getCommitShortInfo().getVerification().getReason(), GHReason.no_user);
    }

    @Test
    public void testUnverifiedEmailVerification() throws Exception {
        GHRepository r = gitHub.getRepository("github-api/github-api");
        GHCommit commit = r.getCommit("86a2e245aa6d71d54923655066049d9e21a15f08");
        assertEquals(commit.getCommitShortInfo().getAuthor().getName(), "Sourabh Parkala");
        assertFalse(commit.getCommitShortInfo().getVerification().getVerified());
        assertEquals(commit.getCommitShortInfo().getVerification().getReason(), GHReason.unverified_email);
    }

    @Test
    public void testBadEmailVerification() throws Exception {
        GHRepository r = gitHub.getRepository("github-api/github-api");
        GHCommit commit = r.getCommit("86a2e245aa6d71d54923655066049d9e21a15f09");
        assertEquals(commit.getCommitShortInfo().getAuthor().getName(), "Sourabh Parkala");
        assertFalse(commit.getCommitShortInfo().getVerification().getVerified());
        assertEquals(commit.getCommitShortInfo().getVerification().getReason(), GHReason.bad_email);
    }

    @Test
    public void testUnknownKeyVerification() throws Exception {
        GHRepository r = gitHub.getRepository("github-api/github-api");
        GHCommit commit = r.getCommit("86a2e245aa6d71d54923655066049d9e21a15f10");
        assertEquals(commit.getCommitShortInfo().getAuthor().getName(), "Sourabh Parkala");
        assertFalse(commit.getCommitShortInfo().getVerification().getVerified());
        assertEquals(commit.getCommitShortInfo().getVerification().getReason(), GHReason.unknown_key);
    }

    @Test
    public void testMalformedSignatureVerification() throws Exception {
        GHRepository r = gitHub.getRepository("github-api/github-api");
        GHCommit commit = r.getCommit("86a2e245aa6d71d54923655066049d9e21a15f11");
        assertEquals(commit.getCommitShortInfo().getAuthor().getName(), "Sourabh Parkala");
        assertFalse(commit.getCommitShortInfo().getVerification().getVerified());
        assertEquals(commit.getCommitShortInfo().getVerification().getReason(), GHReason.malformed_signature);
    }

    @Test
    public void testInvalidVerification() throws Exception {
        GHRepository r = gitHub.getRepository("github-api/github-api");
        GHCommit commit = r.getCommit("86a2e245aa6d71d54923655066049d9e21a15f12");
        assertEquals(commit.getCommitShortInfo().getAuthor().getName(), "Sourabh Parkala");
        assertFalse(commit.getCommitShortInfo().getVerification().getVerified());
        assertEquals(commit.getCommitShortInfo().getVerification().getReason(), GHReason.invalid);
    }

    @Test
    public void testValidVerification() throws Exception {
        GHRepository r = gitHub.getRepository("github-api/github-api");
        GHCommit commit = r.getCommit("86a2e245aa6d71d54923655066049d9e21a15f13");
        assertEquals(commit.getCommitShortInfo().getAuthor().getName(), "Sourabh Parkala");
        assertTrue(commit.getCommitShortInfo().getVerification().getVerified());
        assertEquals(commit.getCommitShortInfo().getVerification().getReason(), GHReason.valid);
        assertNotNull(commit.getCommitShortInfo().getVerification().getPayload());
        assertNotNull(commit.getCommitShortInfo().getVerification().getSignature());
    }
}
