package org.kohsuke.github;

import com.google.common.collect.Iterables;
import org.junit.Test;

/**
 * @author Sourabh Sarvotham Parkala
 */
public class SignatureVerificationTest extends AbstractGitHubWireMockTest {

    @Test // issue 737
    public void commitSignatureVerification() throws Exception {
        GHRepository repo = gitHub.getRepository("stapler/stapler");
        PagedIterable<GHCommit> commits = repo.queryCommits().path("pom.xml").list();
        for (GHCommit commit : Iterables.limit(commits, 10)) {
            GHCommit expected = repo.getCommit(commit.getSHA1());
            assertEquals(expected.getCommitShortInfo().getVerification().getVerified(),
                    commit.getCommitShortInfo().getVerification().getVerified());
            assertEquals(expected.getCommitShortInfo().getVerification().getReason(),
                    commit.getCommitShortInfo().getVerification().getReason());
            assertEquals(expected.getCommitShortInfo().getVerification().getSignature(),
                    commit.getCommitShortInfo().getVerification().getSignature());
            assertEquals(expected.getCommitShortInfo().getVerification().getPayload(),
                    commit.getCommitShortInfo().getVerification().getPayload());
        }
    }
}
