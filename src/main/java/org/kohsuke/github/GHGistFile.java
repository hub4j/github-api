package org.kohsuke.github;

import org.apache.commons.io.IOUtils;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;

/**
 * A file inside {@link GHGist}
 *
 * @author Kohsuke Kawaguchi
 * @see GHGist#getFile(String) GHGist#getFile(String)
 * @see GHGist#getFiles() GHGist#getFiles()
 */
public class GHGistFile {
    /* package almost final */ String fileName;

    private int size;
    private String raw_url, type, language, content;
    private boolean truncated;
    private GHGist owner;

    /**
     * Gets file name.
     *
     * @return the file name
     */
    public String getFileName() {
        return fileName;
    }

    /**
     * File size in bytes.
     *
     * @return the size
     */
    public int getSize() {
        return size;
    }

    /**
     * URL that serves this file as-is.
     *
     * @return the raw url
     */
    public String getRawUrl() {
        return raw_url;
    }

    /**
     * Content type of this Gist, such as "text/plain"
     *
     * @return the type
     */
    public String getType() {
        return type;
    }

    /**
     * Gets language.
     *
     * @return the language
     */
    public String getLanguage() {
        return language;
    }

    /**
     * Content of this file.
     *
     * Content is lazy downloaded and cached locally the first time this method is called.
     *
     * @return the content
     */
    public String getContent() {
        if (this.content == null) {
            if (this.size == 0) {
                this.content = "";
            } else {
                try (InputStream inputStream = this.owner.root()
                        .createRequest()
                        .setRawUrlPath(raw_url)
                        .fetchStream(Requester::copyInputStream)) {
                    this.content = IOUtils.toString(inputStream, Charset.defaultCharset());
                } catch (IOException e) {
                    throw new GHException("Failed to retrieve the gist file content", e);
                }
            }
        }
        return this.content;
    }

    /**
     * (?) indicates if {@link #getContent()} contains a truncated content.
     *
     * @return the boolean
     */
    public boolean isTruncated() {
        return truncated;
    }

    /**
     * set the owner object of the GHGistFile.
     *
     * @param owner
     *            the object stores owner information
     */
    void wrapUp(GHGist owner) {
        this.owner = owner;
    }
}
