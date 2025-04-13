package org.kohsuke.github;

import java.io.IOException;
import java.util.Collections;
import java.util.LinkedHashMap;

import javax.annotation.Nonnull;

// TODO: Auto-generated Javadoc
/**
 * Builder pattern for creating a new Gist.
 *
 * @author Kohsuke Kawaguchi
 * @see GitHub#createGist() GitHub#createGist()
 */
public class GHGistBuilder {
    private final LinkedHashMap<String, Object> files = new LinkedHashMap<String, Object>();
    private final Requester req;

    /**
     * Instantiates a new Gh gist builder.
     *
     * @param root
     *            the root
     */
    public GHGistBuilder(GitHub root) {
        req = root.createRequest().method("POST");
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
        return req.withUrlPath("/gists").fetch(GHGist.class);
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
     * File gh gist builder.
     *
     * @param fileName
     *            the file name
     * @param content
     *            the content
     * @return Adds a new file.
     */
    public GHGistBuilder file(@Nonnull String fileName, @Nonnull String content) {
        files.put(fileName, Collections.singletonMap("content", content));
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
}
