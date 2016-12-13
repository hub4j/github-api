package org.kohsuke.github;

/**
 * @author Kanstantsin Shautsou
 * @see <a href="https://developer.github.com/v3/git/blobs/#get-a-blob">Get a blob</a>
 */
public class GHBlob {
    private String content, encoding, url, sha;
    private long size;

    public String getEncoding() {
        return encoding;
    }

    public String getUrl() {
        return url;
    }

    public String getSha() {
        return sha;
    }

    public long getSize() {
        return size;
    }

    public String getContent() {
        return content;
    }
}
