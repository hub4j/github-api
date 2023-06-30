package org.kohsuke.github;

import org.kohsuke.github.GHCommit.File;

/**
 * Represents the array of files in a commit returned by github.
 *
 * @author Stephen Horgan
 */
class GHCommitFilesPage {
    private File[] files;

    public GHCommitFilesPage() {
	}
    
    public GHCommitFilesPage(File[] files) {
		this.files = files;
	}

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
