package org.kohsuke.github;

import java.io.UnsupportedEncodingException;

/**
 * A file inside {@link GHGist}
 *
 * @author Kohsuke Kawaguchi
 * @see GHGist#getFile(String) GHGist#getFile(String)
 * @see GHGist#getFiles() GHGist#getFiles()
 */
public class GHGistFile {
    /* package almost final */ String fileName;
    private int oneMegabyte = 1000000;
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
     * Each time when we tried to get the content,
     * it is necessary to check the content size,
     * and decide whether truncate the content.
     *
     * The truncated condition is the size of content
     * greater than 1mb
     *
     * @return the content
     */
    public String getContent() throws UnsupportedEncodingException {

        String finalContent = "";

        long contentSize = contentSize();
        if (contentSize > 1){
            truncated = true;
        }
        else {
            truncated = false;
        }

        if (isTruncated()){
            for (int i = 0; i < oneMegabyte; i++){
                finalContent += content.charAt(i);
            }
        }
        else {
            finalContent = content;
        }
        return finalContent;
    }

    /**
     * (?) indicates if {@link #getContent()} contains a truncated content.
     *
     * @return the boolean
     */
    public boolean isTruncated() { return truncated; }

    /**
     * Check the content size.
     *
     * @return the long
     */
    public long contentSize() {
        // Get length of file in bytes
        long contentSizeInBytes = content.length();
        // Convert the bytes to Kilobytes (1 KB = 1024 Bytes)
        long contentSizeInKB = contentSizeInBytes / 1024;
        // Convert the KB to MegaBytes (1 MB = 1024 KBytes)
        long contentSizeInMB = contentSizeInKB / 1024;

        return contentSizeInMB;
    }
}