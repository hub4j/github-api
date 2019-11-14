package org.kohsuke.github;

import java.io.IOException;
import java.util.Collections;
import java.util.LinkedHashMap;

/**
 * Builder pattern for updating a Gist.
 *
 * @author Martin van Zijl
 */
public class GHGistUpdater {
    private final GHGist base;
    private final Requester builder;
    LinkedHashMap<String, Object> files;

    GHGistUpdater(GHGist base) {
        this.base = base;
        this.builder = new Requester(base.root);

        files = new LinkedHashMap<>();
    }

    public GHGistUpdater addFile(String fileName, String content) throws IOException {
        updateFile(fileName, content);
        return this;
    }

    // // This method does not work.
    // public GHGistUpdater deleteFile(String fileName) throws IOException {
    // files.put(fileName, Collections.singletonMap("filename", null));
    // return this;
    // }

    public GHGistUpdater renameFile(String fileName, String newFileName) throws IOException {
        files.put(fileName, Collections.singletonMap("filename", newFileName));
        return this;
    }

    public GHGistUpdater updateFile(String fileName, String content) throws IOException {
        files.put(fileName, Collections.singletonMap("content", content));
        return this;
    }

    public GHGistUpdater description(String desc) {
        builder.with("description", desc);
        return this;
    }

    /**
     * Updates the Gist based on the parameters specified thus far.
     */
    public GHGist update() throws IOException {
        builder._with("files", files);
        return builder.method("PATCH").to(base.getApiTailUrl(""), GHGist.class).wrap(base.owner);
    }
}
