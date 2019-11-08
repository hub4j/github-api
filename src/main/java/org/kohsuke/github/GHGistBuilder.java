package org.kohsuke.github;

import java.io.IOException;
import java.util.Collections;
import java.util.LinkedHashMap;

/**
 * Builder pattern for creating a new Gist.
 *
 * @author Kohsuke Kawaguchi
 * @see GitHub#createGist()
 */
public class GHGistBuilder {
    private final GitHub root;
    private final Requester req;
    private final LinkedHashMap<String,Object> files = new LinkedHashMap<String, Object>();

    public GHGistBuilder(GitHub root) {
        this.root = root;
        req = new Requester(root);
    }

    public GHGistBuilder description(String desc) {
        req.with("description",desc);
        return this;
    }

    public GHGistBuilder public_(boolean v) {
        req.with("public",v);
        return this;
    }

    /**
     * Adds a new file.
     */
    public GHGistBuilder file(String fileName, String content) {
        files.put(fileName, Collections.singletonMap("content", content));
        return this;
    }

    /**
     * Creates a Gist based on the parameters specified thus far.
     */
    public GHGist create() throws IOException {
        req._with("files",files);
        return req.to("/gists",GHGist.class);
    }
}
