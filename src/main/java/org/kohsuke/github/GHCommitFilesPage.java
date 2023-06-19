package org.kohsuke.github;

import org.kohsuke.github.GHCommit.File;

/**
 * Represents the array of files returned by github in a commit.
 *
 * @author Stephen Horgan
 */
class GHCommitFilesPage {
    private File[] files;

    /**
     * Gets the files.
     *
     * @param owner
     *            the owner
     * @return the files
     */
    File[] getFiles() {
        return files;
    }
}
