package org.kohsuke.github;

import java.io.IOException;
import java.util.Collections;
import java.util.LinkedHashMap;

/**
 * Builder pattern for creating a new Gist.
 *
 * @author Kohsuke Kawaguchi
 * @see GitHub#createGist() GitHub#createGist()
 */
public class GHGistBuilder {
    private final GitHub root;
    private final Requester req;
    private final LinkedHashMap<String, Object> files = new LinkedHashMap<String, Object>();

    /**
     * Instantiates a new Gh gist builder.
     *
     * @param root
     *            the root
     */
    public GHGistBuilder(GitHub root) {
        this.root = root;
        req = root.createRequest().method("POST");
    }

    /**
     * Description gh gist builder.
     *
     * @param desc
     *            the desc
     * @return the gh gist builder
     */
    public GHGistBuilder description(String desc) {
        req.with("description", desc);
        return this;
    }

    /**
     * Public gh gist builder.
     *
     * @param v
     *            the v
     * @return the gh gist builder
     */
    public GHGistBuilder public_(boolean v) {
        req.with("public", v);
        return this;
    }

    /**
     * File gh gist builder.
     *
     * @param fileName
     *            the file name
     * @param content
     *            the content
     * @return Adds a new file.
     */
    public GHGistBuilder file(String fileName, String content) {
        files.put(fileName, Collections.singletonMap("content", content));
        return this;
    }

    /**
     * Creates a Gist based on the parameters specified thus far.
     *
     * @return created Gist
     * @throws IOException
     *             if Gist cannot be created.
     */
    public GHGist create() throws IOException {
        req.with("files", files);
        return req.withUrlPath("/gists").fetch(GHGist.class).wrapUp(root);
    }
}
