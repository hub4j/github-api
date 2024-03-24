package org.kohsuke.github;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

// TODO: Auto-generated Javadoc
/**
 * A Content of a repository.
 *
 * @author Alexandre COLLIGNON
 * @see GHRepository#getFileContent(String) GHRepository#getFileContent(String)
 */
@SuppressWarnings({ "UnusedDeclaration" })
public class GHContent extends GitHubInteractiveObject implements Refreshable {
    /*
     * In normal use of this class, repository field is set via wrap(), but in the code search API, there's a nested
     * 'repository' field that gets populated from JSON.
     */
    private GHRepository repository;

    private String type;
    private String encoding;
    private long size;
    private String sha;
    private String name;
    private String path;
    private String target;
    private String content;
    private String url; // this is the API url
    private String git_url; // this is the Blob url
    private String html_url; // this is the UI
    private String download_url;

    /**
     * Gets owner.
     *
     * @return the owner
     */
    @SuppressFBWarnings(value = { "EI_EXPOSE_REP" }, justification = "Expected behavior")
    public GHRepository getOwner() {
        return repository;
    }

    /**
     * Gets type.
     *
     * @return the type
     */
    public String getType() {
        return type;
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
     * Gets size.
     *
     * @return the size
     */
    public long getSize() {
        return size;
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
     * Gets name.
     *
     * @return the name
     */
    public String getName() {
        return name;
    }

    /**
     * Gets path.
     *
     * @return the path
     */
    public String getPath() {
        return path;
    }

    /**
     * Gets target of a symlink. This will only be set if {@code "symlink".equals(getType())}
     *
     * @return the target
     */
    public String getTarget() {
        return target;
    }

    /**
     * Retrieve the decoded content that is stored at this location.
     *
     * <p>
     * Due to the nature of GitHub's API, you're not guaranteed that the content will already be populated, so this may
     * trigger network activity, and can throw an IOException.
     *
     * @return the content
     * @throws IOException
     *             the io exception
     * @deprecated Use {@link #read()}
     */
    @Deprecated
    @SuppressFBWarnings("DM_DEFAULT_ENCODING")
    public String getContent() throws IOException {
        return new String(Base64.getMimeDecoder().decode(getEncodedContent()));
    }

    /**
     * Retrieve the base64-encoded content that is stored at this location.
     *
     * <p>
     * Due to the nature of GitHub's API, you're not guaranteed that the content will already be populated, so this may
     * trigger network activity, and can throw an IOException.
     *
     * @return the encoded content
     * @throws IOException
     *             the io exception
     * @deprecated Use {@link #read()}
     */
    public String getEncodedContent() throws IOException {
        refresh(content);
        return content;
    }

    /**
     * Gets url.
     *
     * @return the url
     */
    public String getUrl() {
        return url;
    }

    /**
     * Gets git url.
     *
     * @return the git url
     */
    public String getGitUrl() {
        return git_url;
    }

    /**
     * Gets html url.
     *
     * @return the html url
     */
    public String getHtmlUrl() {
        return html_url;
    }

    /**
     * Retrieves the actual bytes of the blob.
     *
     * @return the input stream
     * @throws IOException
     *             the io exception
     */
    public InputStream read() throws IOException {
        refresh(content);
        if (encoding.equals("base64")) {
            try {
                Base64.Decoder decoder = Base64.getMimeDecoder();
                return new ByteArrayInputStream(decoder.decode(content.getBytes(StandardCharsets.US_ASCII)));
            } catch (IllegalArgumentException e) {
                throw new AssertionError(e); // US-ASCII is mandatory
            }
        }

        throw new UnsupportedOperationException("Unrecognized encoding: " + encoding);
    }

    /**
     * URL to retrieve the raw content of the file. Null if this is a directory.
     *
     * @return the download url
     * @throws IOException
     *             the io exception
     */
    public String getDownloadUrl() throws IOException {
        refresh(download_url);
        return download_url;
    }

    /**
     * Is file boolean.
     *
     * @return the boolean
     */
    public boolean isFile() {
        return "file".equals(type);
    }

    /**
     * Is directory boolean.
     *
     * @return the boolean
     */
    public boolean isDirectory() {
        return "dir".equals(type);
    }

    /**
     * Fully populate the data by retrieving missing data.
     * <p>
     * Depending on the original API call where this object is created, it may not contain everything.
     *
     * @throws IOException
     *             the io exception
     */
    protected synchronized void populate() throws IOException {
        root().createRequest().withUrlPath(url).fetchInto(this);
    }

    /**
     * List immediate children of this directory.
     *
     * @return the paged iterable
     * @throws IOException
     *             the io exception
     */
    public PagedIterable<GHContent> listDirectoryContent() throws IOException {
        if (!isDirectory())
            throw new IllegalStateException(path + " is not a directory");

        return root().createRequest().setRawUrlPath(url).toIterable(GHContent[].class, item -> item.wrap(repository));
    }

    /**
     * Update gh content update response.
     *
     * @param newContent
     *            the new content
     * @param commitMessage
     *            the commit message
     * @return the gh content update response
     * @throws IOException
     *             the io exception
     */
    @SuppressFBWarnings("DM_DEFAULT_ENCODING")
    public GHContentUpdateResponse update(String newContent, String commitMessage) throws IOException {
        return update(newContent.getBytes(), commitMessage, null);
    }

    /**
     * Update gh content update response.
     *
     * @param newContent
     *            the new content
     * @param commitMessage
     *            the commit message
     * @param branch
     *            the branch
     * @return the gh content update response
     * @throws IOException
     *             the io exception
     */
    @SuppressFBWarnings("DM_DEFAULT_ENCODING")
    public GHContentUpdateResponse update(String newContent, String commitMessage, String branch) throws IOException {
        return update(newContent.getBytes(), commitMessage, branch);
    }

    /**
     * Update gh content update response.
     *
     * @param newContentBytes
     *            the new content bytes
     * @param commitMessage
     *            the commit message
     * @return the gh content update response
     * @throws IOException
     *             the io exception
     */
    public GHContentUpdateResponse update(byte[] newContentBytes, String commitMessage) throws IOException {
        return update(newContentBytes, commitMessage, null);
    }

    /**
     * Update gh content update response.
     *
     * @param newContentBytes
     *            the new content bytes
     * @param commitMessage
     *            the commit message
     * @param branch
     *            the branch
     * @return the gh content update response
     * @throws IOException
     *             the io exception
     */
    public GHContentUpdateResponse update(byte[] newContentBytes, String commitMessage, String branch)
            throws IOException {
        String encodedContent = Base64.getEncoder().encodeToString(newContentBytes);

        Requester requester = root().createRequest()
                .method("PUT")
                .with("path", path)
                .with("message", commitMessage)
                .with("sha", sha)
                .with("content", encodedContent);

        if (branch != null) {
            requester.with("branch", branch);
        }

        GHContentUpdateResponse response = requester.withUrlPath(getApiRoute(repository, path))
                .fetch(GHContentUpdateResponse.class);

        response.getContent().wrap(repository);
        response.getCommit().wrapUp(repository);

        this.content = encodedContent;
        return response;
    }

    /**
     * Delete gh content update response.
     *
     * @param message
     *            the message
     * @return the gh content update response
     * @throws IOException
     *             the io exception
     */
    public GHContentUpdateResponse delete(String message) throws IOException {
        return delete(message, null);
    }

    /**
     * Delete gh content update response.
     *
     * @param commitMessage
     *            the commit message
     * @param branch
     *            the branch
     * @return the gh content update response
     * @throws IOException
     *             the io exception
     */
    public GHContentUpdateResponse delete(String commitMessage, String branch) throws IOException {
        Requester requester = root().createRequest()
                .method("DELETE")
                .with("path", path)
                .with("message", commitMessage)
                .with("sha", sha);

        if (branch != null) {
            requester.with("branch", branch);
        }

        GHContentUpdateResponse response = requester.withUrlPath(getApiRoute(repository, path))
                .fetch(GHContentUpdateResponse.class);

        response.getCommit().wrapUp(repository);
        return response;
    }

    /**
     * Gets the api route.
     *
     * @param repository
     *            the repository
     * @param path
     *            the path
     * @return the api route
     */
    static String getApiRoute(GHRepository repository, String path) {
        return repository.getApiTailUrl("contents/" + path);
    }

    /**
     * Wrap.
     *
     * @param owner
     *            the owner
     * @return the GH content
     */
    GHContent wrap(GHRepository owner) {
        this.repository = owner;
        return this;
    }

    /**
     * Fully populate the data by retrieving missing data.
     *
     * Depending on the original API call where this object is created, it may not contain everything.
     *
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     */
    @Override
    public synchronized void refresh() throws IOException {
        root().createRequest().setRawUrlPath(url).fetchInto(this);
    }
}
