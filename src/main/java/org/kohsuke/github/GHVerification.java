package org.kohsuke.github;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

/**
 * The commit/tag would be signed by user. This object would hold the verification status. Whether the Commit/Tag is
 * signed or not.
 *
 * @author Sourabh Sarvotham Parkala
 */
@SuppressFBWarnings(value = { "UWF_UNWRITTEN_PUBLIC_OR_PROTECTED_FIELD", "UWF_UNWRITTEN_FIELD", "NP_UNWRITTEN_FIELD" },
        justification = "JSON API")
public class GHVerification {
    private String signature, payload;
    private boolean verified;
    private GHReason reason;

    /**
     * Indicates whether GitHub considers the signature in this commit to be verified.
     *
     * @return true if the signature is valid else returns false.
     */
    public boolean getVerified() {
        return verified;
    }

    /**
     * Gets reason for verification value.
     *
     * @return return reason of type {@link GHReason}, such as "valid" or "unsigned". The possible values can be found
     *         in {@link GHReason}}
     */
    public GHReason getReason() {
        return reason;
    }

    /**
     * Gets signature used for the verification.
     *
     * @return null if not signed else encoded signature.
     */
    public String getSignature() {
        return signature;
    }

    /**
     * Gets the payload that was signed.
     *
     * @return null if not signed else encoded signature.
     */
    public String getPayload() {
        return payload;
    }
}
