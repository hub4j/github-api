package org.kohsuke.github;

// TODO: Auto-generated Javadoc
/**
 * A file inside {@link GHGist}.
 *
 * @author Kohsuke Kawaguchi
 * @see GHGist#getFile(String) GHGist#getFile(String)
 * @see GHGist#getFiles() GHGist#getFiles()
 */
public class GHGistFile {

    private String rawUrl, type, language, content;

    private int size;

    private boolean truncated;
    /** The file name. */
    /* package almost final */ String fileName;
    /**
     * Create default GHGistFile instance
     */
    public GHGistFile() {
    }

    /**
     * Content of this file.
     *
     * @return the content
     */
    public String getContent() {
        return content;
    }

    /**
     * Gets file name.
     *
     * @return the file name
     */
    public String getFileName() {
        return fileName;
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
     * URL that serves this file as-is.
     *
     * @return the raw url
     */
    public String getRawUrl() {
        return rawUrl;
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
     * Content type of this Gist, such as "text/plain".
     *
     * @return the type
     */
    public String getType() {
        return type;
    }

    /**
     * (?) indicates if {@link #getContent()} contains a truncated content.
     *
     * @return the boolean
     */
    public boolean isTruncated() {
        return truncated;
    }
}
