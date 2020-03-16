package org.kohsuke.github;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.net.URL;
import java.util.Base64;

/**
 * The type GHBlob.
 *
 * @author Kanstantsin Shautsou
 * @author Kohsuke Kawaguchi
 * @see GHTreeEntry#asBlob() GHTreeEntry#asBlob()
 * @see GHRepository#getBlob(String) GHRepository#getBlob(String)
 * @see <a href="https://developer.github.com/v3/git/blobs/#get-a-blob">Get a blob</a>
 */
public class GHBlob {
    private String content, encoding, url, sha;
    private long size;

    /**
     * Gets url.
     *
     * @return API URL of this blob.
     */
    public URL getUrl() {
        return GitHubClient.parseURL(url);
    }

    /**
     * Gets sha.
     *
     * @return the sha
     */
    public String getSha() {
        return sha;
    }

    /**
     * Gets size.
     *
     * @return Number of bytes in this blob.
     */
    public long getSize() {
        return size;
    }

    /**
     * Gets encoding.
     *
     * @return the encoding
     */
    public String getEncoding() {
        return encoding;
    }

    /**
     * Gets content.
     *
     * @return Encoded content. You probably want {@link #read()}
     */
    public String getContent() {
        return content;
    }

    /**
     * Read input stream.
     *
     * @return the actual bytes of the blob.
     */
    public InputStream read() {
        if (encoding.equals("base64")) {
            try {
                Base64.Decoder decoder = Base64.getMimeDecoder();
                return new ByteArrayInputStream(decoder.decode(content));
            } catch (IllegalArgumentException e) {
                throw new AssertionError(e); // US-ASCII is mandatory
            }
        }

        throw new UnsupportedOperationException("Unrecognized encoding: " + encoding);
    }
}
