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
    
    /** The file name. */
    /* package almost final */ String fileName;

    private int size;
    private String raw_url, type, language, content;
    private boolean truncated;

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
     * Content type of this Gist, such as "text/plain".
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
     * @return the content
     */
    public String getContent() {
        return content;
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
