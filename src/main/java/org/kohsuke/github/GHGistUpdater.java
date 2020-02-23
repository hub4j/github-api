package org.kohsuke.github;

import java.io.IOException;
import java.util.Collections;
import java.util.LinkedHashMap;

/**
 * Builder pattern for updating a Gist.
 */
public class GHGistUpdater {
    private final GHGist base;
    private final Requester builder;
    LinkedHashMap<String, Object> files;

    GHGistUpdater(GHGist base) {
        this.base = base;
        this.builder = base.root.createRequest();

        files = new LinkedHashMap<>();
    }

    /**
     * Add file gh gist updater.
     *
     * @param fileName
     *            the file name
     * @param content
     *            the content
     * @return the gh gist updater
     * @throws IOException
     *             the io exception
     */
    public GHGistUpdater addFile(String fileName, String content) throws IOException {
        updateFile(fileName, content);
        return this;
    }

    // // This method does not work.
    // public GHGistUpdater deleteFile(String fileName) throws IOException {
    // files.put(fileName, Collections.singletonMap("filename", null));
    // return this;
    // }

    /**
     * Rename file gh gist updater.
     *
     * @param fileName
     *            the file name
     * @param newFileName
     *            the new file name
     * @return the gh gist updater
     * @throws IOException
     *             the io exception
     */
    public GHGistUpdater renameFile(String fileName, String newFileName) throws IOException {
        files.put(fileName, Collections.singletonMap("filename", newFileName));
        return this;
    }

    /**
     * Update file gh gist updater.
     *
     * @param fileName
     *            the file name
     * @param content
     *            the content
     * @return the gh gist updater
     * @throws IOException
     *             the io exception
     */
    public GHGistUpdater updateFile(String fileName, String content) throws IOException {
        files.put(fileName, Collections.singletonMap("content", content));
        return this;
    }

    /**
     * Description gh gist updater.
     *
     * @param desc
     *            the desc
     * @return the gh gist updater
     */
    public GHGistUpdater description(String desc) {
        builder.with("description", desc);
        return this;
    }

    /**
     * Updates the Gist based on the parameters specified thus far.
     *
     * @return the gh gist
     * @throws IOException
     *             the io exception
     */
    public GHGist update() throws IOException {
        builder.with("files", files);
        return builder.method("PATCH").withUrlPath(base.getApiTailUrl("")).fetch(GHGist.class).wrap(base.owner);
    }
}
