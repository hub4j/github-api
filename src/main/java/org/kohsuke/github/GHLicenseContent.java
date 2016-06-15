package org.kohsuke.github;

/**
 *
 * @see org.kohsuke.github.extras.PreviewHttpConnector
 */
public class GHLicenseContent extends GHContent {
    GHLicenseBase license;

    public GHLicenseBase getLicense() {
        return license;
    }

    @Override
    public String toString() {
        return "GHLicenseContent{" +
                "licenseBase=" + license +
                "} " + super.toString();
    }
}
