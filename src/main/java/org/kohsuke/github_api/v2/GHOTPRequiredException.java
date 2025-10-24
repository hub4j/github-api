package org.kohsuke.github_api.v2;

/**
 * This exception is thrown when GitHub is requesting an OTP from the user.
 *
 * @author Kevin Harrington mad.hephaestus@gmail.com
 */
public class GHOTPRequiredException extends GHIOException {

    /**
     * Create default GHOTPRequiredException instance
     */
    public GHOTPRequiredException() {
    }

    // ...
}
