package org.kohsuke.github;

/**
 * A file inside {@link GHGist}
 *
 * @author Kohsuke Kawaguchi
 * @see GHGist#getFile(String)
 * @see GHGist#getFiles()
 */
public class GHGistFile {
    /* package almost final */ String fileName;

    private int size;
    private String raw_url, type, language, content;
    private boolean truncated;

    public String getFileName() {
        return fileName;
    }

    /**
     * File size in bytes.
     */
    public int getSize() {
        return size;
    }

    /**
     * URL that serves this file as-is.
     */
    public String getRawUrl() {
        return raw_url;
    }

    /**
     * Content type of this Gist, such as "text/plain"
     */
    public String getType() {
        return type;
    }

    public String getLanguage() {
        return language;
    }

    /**
     * Content of this file.
     */
    public String getContent() {
        return content;
    }

    /**
     * (?) indicates if {@link #getContent()} contains a truncated content.
     */
    public boolean isTruncated() {
        return truncated;
    }
}
