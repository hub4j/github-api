package org.kohsuke.github;

import org.kohsuke.github.GHCommit.File;

/**
 * Represents the array of files in a commit returned by github.
 *
 * @author Stephen Horgan
 */
class GHCommitFilesPage implements GitHubPage<GHCommit.File> {
    private File[] files;

    public GHCommitFilesPage() {
    }

    GHCommitFilesPage(File[] files) {
        this.files = files;
    }

    /**
     * Gets the files.
     *
     * @return the files
     */
    @Override
    public File[] getItems() {
        return files;
    }
}
