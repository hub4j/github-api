package org.kohsuke.github;

/**
 * {@link GHContent} with license information.
 *
 * @author Kohsuke Kawaguchi
 * @see <a href="https://developer.github.com/v3/licenses/#get-a-repositorys-license">documentation</a>
 * @see GHRepository#getLicense()
 */
class GHContentWithLicense extends GHContent {
    GHLicense license;

    @Override
    GHContentWithLicense wrap(GHRepository owner) {
        super.wrap(owner);
        if (license!=null)
            license.wrap(owner.root);
        return this;
    }
}
