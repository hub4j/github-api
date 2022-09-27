package org.kohsuke.github;

// TODO: Auto-generated Javadoc
/**
 * {@link GHContent} with license information.
 *
 * @author Kohsuke Kawaguchi
 * @see <a href="https://developer.github.com/v3/licenses/#get-a-repositorys-license">documentation</a>
 * @see GHRepository#getLicense()
 */
class GHContentWithLicense extends GHContent {
    
    /** The license. */
    GHLicense license;

    /**
     * Wrap.
     *
     * @param owner the owner
     * @return the GH content with license
     */
    @Override
    GHContentWithLicense wrap(GHRepository owner) {
        super.wrap(owner);
        return this;
    }
}
