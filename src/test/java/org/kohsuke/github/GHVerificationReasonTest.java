package org.kohsuke.github;

import org.junit.Test;

import static org.hamcrest.Matchers.*;

/**
 * @author Sourabh Sarvotham Parkala
 */
public class GHVerificationReasonTest extends AbstractGitHubWireMockTest {
    // Issue 737
    @Test
    public void testExpiredKey() throws Exception {
        GHRepository r = gitHub.getRepository("hub4j/github-api");
        GHCommit commit = r.getCommit("86a2e245aa6d71d54923655066049d9e21a15f01");
        assertThat(commit.getCommitShortInfo().getAuthor().getName(), equalTo("Sourabh Parkala"));
        assertThat(commit.getCommitShortInfo().getVerification().isVerified(), is(false));
        assertThat(commit.getCommitShortInfo().getVerification().getReason(),
                equalTo(GHVerification.Reason.EXPIRED_KEY));
    }

    @Test
    public void testNotSigningKey() throws Exception {
        GHRepository r = gitHub.getRepository("hub4j/github-api");
        GHCommit commit = r.getCommit("86a2e245aa6d71d54923655066049d9e21a15f02");
        assertThat(commit.getCommitShortInfo().getAuthor().getName(), equalTo("Sourabh Parkala"));
        assertThat(commit.getCommitShortInfo().getVerification().isVerified(), is(false));
        assertThat(commit.getCommitShortInfo().getVerification().getReason(),
                equalTo(GHVerification.Reason.NOT_SIGNING_KEY));
    }

    @Test
    public void testGpgverifyError() throws Exception {
        GHRepository r = gitHub.getRepository("hub4j/github-api");
        GHCommit commit = r.getCommit("86a2e245aa6d71d54923655066049d9e21a15f03");
        assertThat(commit.getCommitShortInfo().getAuthor().getName(), equalTo("Sourabh Parkala"));
        assertThat(commit.getCommitShortInfo().getVerification().isVerified(), is(false));
        assertThat(commit.getCommitShortInfo().getVerification().getReason(),
                equalTo(GHVerification.Reason.GPGVERIFY_ERROR));
    }

    @Test
    public void testGpgverifyUnavailable() throws Exception {
        GHRepository r = gitHub.getRepository("hub4j/github-api");
        GHCommit commit = r.getCommit("86a2e245aa6d71d54923655066049d9e21a15f04");
        assertThat(commit.getCommitShortInfo().getAuthor().getName(), equalTo("Sourabh Parkala"));
        assertThat(commit.getCommitShortInfo().getVerification().isVerified(), is(false));
        assertThat(commit.getCommitShortInfo().getVerification().getReason(),
                equalTo(GHVerification.Reason.GPGVERIFY_UNAVAILABLE));
    }

    @Test
    public void testUnsigned() throws Exception {
        GHRepository r = gitHub.getRepository("hub4j/github-api");
        GHCommit commit = r.getCommit("86a2e245aa6d71d54923655066049d9e21a15f05");
        assertThat(commit.getCommitShortInfo().getAuthor().getName(), equalTo("Sourabh Parkala"));
        assertThat(commit.getCommitShortInfo().getVerification().isVerified(), is(false));
        assertThat(commit.getCommitShortInfo().getVerification().getReason(), equalTo(GHVerification.Reason.UNSIGNED));
    }

    @Test
    public void testUnknownSignatureType() throws Exception {
        GHRepository r = gitHub.getRepository("hub4j/github-api");
        GHCommit commit = r.getCommit("86a2e245aa6d71d54923655066049d9e21a15f06");
        assertThat(commit.getCommitShortInfo().getAuthor().getName(), equalTo("Sourabh Parkala"));
        assertThat(commit.getCommitShortInfo().getVerification().isVerified(), is(false));
        assertThat(commit.getCommitShortInfo().getVerification().getReason(),
                equalTo(GHVerification.Reason.UNKNOWN_SIGNATURE_TYPE));
    }

    @Test
    public void testNoUser() throws Exception {
        GHRepository r = gitHub.getRepository("hub4j/github-api");
        GHCommit commit = r.getCommit("86a2e245aa6d71d54923655066049d9e21a15f07");
        assertThat(commit.getCommitShortInfo().getAuthor().getName(), equalTo("Sourabh Parkala"));
        assertThat(commit.getCommitShortInfo().getVerification().isVerified(), is(false));
        assertThat(commit.getCommitShortInfo().getVerification().getReason(), equalTo(GHVerification.Reason.NO_USER));
    }

    @Test
    public void testUnverifiedEmail() throws Exception {
        GHRepository r = gitHub.getRepository("hub4j/github-api");
        GHCommit commit = r.getCommit("86a2e245aa6d71d54923655066049d9e21a15f08");
        assertThat(commit.getCommitShortInfo().getAuthor().getName(), equalTo("Sourabh Parkala"));
        assertThat(commit.getCommitShortInfo().getVerification().isVerified(), is(false));
        assertThat(commit.getCommitShortInfo().getVerification().getReason(),
                equalTo(GHVerification.Reason.UNVERIFIED_EMAIL));
    }

    @Test
    public void testBadEmail() throws Exception {
        GHRepository r = gitHub.getRepository("hub4j/github-api");
        GHCommit commit = r.getCommit("86a2e245aa6d71d54923655066049d9e21a15f09");
        assertThat(commit.getCommitShortInfo().getAuthor().getName(), equalTo("Sourabh Parkala"));
        assertThat(commit.getCommitShortInfo().getVerification().isVerified(), is(false));
        assertThat(commit.getCommitShortInfo().getVerification().getReason(), equalTo(GHVerification.Reason.BAD_EMAIL));
    }

    @Test
    public void testUnknownKey() throws Exception {
        GHRepository r = gitHub.getRepository("hub4j/github-api");
        GHCommit commit = r.getCommit("86a2e245aa6d71d54923655066049d9e21a15f10");
        assertThat(commit.getCommitShortInfo().getAuthor().getName(), equalTo("Sourabh Parkala"));
        assertThat(commit.getCommitShortInfo().getVerification().isVerified(), is(false));
        assertThat(commit.getCommitShortInfo().getVerification().getReason(),
                equalTo(GHVerification.Reason.UNKNOWN_KEY));
    }

    @Test
    public void testMalformedSignature() throws Exception {
        GHRepository r = gitHub.getRepository("hub4j/github-api");
        GHCommit commit = r.getCommit("86a2e245aa6d71d54923655066049d9e21a15f11");
        assertThat(commit.getCommitShortInfo().getAuthor().getName(), equalTo("Sourabh Parkala"));
        assertThat(commit.getCommitShortInfo().getVerification().isVerified(), is(false));
        assertThat(commit.getCommitShortInfo().getVerification().getReason(),
                equalTo(GHVerification.Reason.MALFORMED_SIGNATURE));
    }

    @Test
    public void testInvalid() throws Exception {
        GHRepository r = gitHub.getRepository("hub4j/github-api");
        GHCommit commit = r.getCommit("86a2e245aa6d71d54923655066049d9e21a15f12");
        assertThat(commit.getCommitShortInfo().getAuthor().getName(), equalTo("Sourabh Parkala"));
        assertThat(commit.getCommitShortInfo().getVerification().isVerified(), is(false));
        assertThat(commit.getCommitShortInfo().getVerification().getReason(), equalTo(GHVerification.Reason.INVALID));
    }

    @Test
    public void testValid() throws Exception {
        GHRepository r = gitHub.getRepository("hub4j/github-api");
        GHCommit commit = r.getCommit("86a2e245aa6d71d54923655066049d9e21a15f13");
        assertThat(commit.getCommitShortInfo().getAuthor().getName(), equalTo("Sourabh Parkala"));
        assertThat(commit.getCommitShortInfo().getVerification().isVerified(), is(true));
        assertThat(commit.getCommitShortInfo().getVerification().getReason(), equalTo(GHVerification.Reason.VALID));
        assertThat(commit.getCommitShortInfo().getVerification().getPayload(), notNullValue());
        assertThat(commit.getCommitShortInfo().getVerification().getSignature(), notNullValue());
    }
}
