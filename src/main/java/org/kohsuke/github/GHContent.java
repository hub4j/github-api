package org.kohsuke.github;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.io.IOUtils;

import java.io.IOException;
import java.io.InputStream;

import javax.xml.bind.DatatypeConverter;

/**
 * A Content of a repository.
 *
 * @author Alexandre COLLIGNON
 * @see GHRepository#getFileContent(String)
 */
@SuppressWarnings({"UnusedDeclaration"})
public class GHContent {
    /*
        In normal use of this class, repository field is set via wrap(),
        but in the code search API, there's a nested 'repository' field that gets populated from JSON.
     */
    private GHRepository repository;

    private GitHub root;

    private String type;
    private String encoding;
    private long size;
    private String sha;
    private String name;
    private String path;
    private String content;
    private String url; // this is the API url
    private String git_url;    // this is the Blob url
    private String html_url;    // this is the UI
    private String download_url;

    public GHRepository getOwner() {
        return repository;
    }

    public String getType() {
        return type;
    }

    public String getEncoding() {
        return encoding;
    }

    public long getSize() {
        return size;
    }

    public String getSha() {
        return sha;
    }

    public String getName() {
        return name;
    }

    public String getPath() {
        return path;
    }

    /**
     * Retrieve the decoded content that is stored at this location.
     *
     * <p>
     * Due to the nature of GitHub's API, you're not guaranteed that
     * the content will already be populated, so this may trigger
     * network activity, and can throw an IOException.
     *
     * @deprecated
     *      Use {@link #read()}
     */
    public String getContent() throws IOException {
        return new String(DatatypeConverter.parseBase64Binary(getEncodedContent()), getEncoding());
    }

    /**
     * Retrieve the base64-encoded content that is stored at this location.
     *
     * <p>
     * Due to the nature of GitHub's API, you're not guaranteed that
     * the content will already be populated, so this may trigger
     * network activity, and can throw an IOException.
     *
     * @deprecated
     *      Use {@link #read()}
     */
    public String getEncodedContent() throws IOException {
        if (content!=null)
            return content;
        else
            return Base64.encodeBase64String(IOUtils.toByteArray(read()));
    }

    public String getUrl() {
        return url;
    }

    public String getGitUrl() {
        return git_url;
    }

    public String getHtmlUrl() {
        return html_url;
    }

    /**
     * Retrieves the actual content stored here.
     */
    public InputStream read() throws IOException {
        return new Requester(root).asStream(getDownloadUrl());
    }

    /**
     * URL to retrieve the raw content of the file. Null if this is a directory.
     */
    public String getDownloadUrl() throws IOException {
        populate();
        return download_url;
    }

    public boolean isFile() {
        return "file".equals(type);
    }

    public boolean isDirectory() {
        return "dir".equals(type);
    }

    /**
     * Fully populate the data by retrieving missing data.
     *
     * Depending on the original API call where this object is created, it may not contain everything.
     */
    protected synchronized void populate() throws IOException {
        if (download_url!=null)    return; // already populated
        root.retrieve().to(url, this);
    }

    /**
     * List immediate children of this directory.
     */
    public PagedIterable<GHContent> listDirectoryContent() throws IOException {
        if (!isDirectory())
            throw new IllegalStateException(path+" is not a directory");

        return new PagedIterable<GHContent>() {
            public PagedIterator<GHContent> iterator() {
                return new PagedIterator<GHContent>(root.retrieve().asIterator(url, GHContent[].class)) {
                    @Override
                    protected void wrapUp(GHContent[] page) {
                        GHContent.wrap(page, repository);
                    }
                };
            }
        };
    }

    public GHContentUpdateResponse update(String newContent, String commitMessage) throws IOException {
        return update(newContent.getBytes(getEncoding()), commitMessage, null);
    }

    public GHContentUpdateResponse update(String newContent, String commitMessage, String branch) throws IOException {
        return update(newContent.getBytes(getEncoding()), commitMessage, branch);
    }

    public GHContentUpdateResponse update(byte[] newContentBytes, String commitMessage) throws IOException {
        return update(newContentBytes, commitMessage, null);
    }

    public GHContentUpdateResponse update(byte[] newContentBytes, String commitMessage, String branch) throws IOException {
        String encodedContent = DatatypeConverter.printBase64Binary(newContentBytes);

        Requester requester = new Requester(root)
            .with("path", path)
            .with("message", commitMessage)
            .with("sha", sha)
            .with("content", encodedContent)
            .method("PUT");

        if (branch != null) {
            requester.with("branch", branch);
        }

        GHContentUpdateResponse response = requester.to(getApiRoute(), GHContentUpdateResponse.class);

        response.getContent().wrap(repository);
        response.getCommit().wrapUp(repository);

        this.content = encodedContent;
        return response;
    }

    public GHContentUpdateResponse delete(String message) throws IOException {
        return delete(message, null);
    }

    public GHContentUpdateResponse delete(String commitMessage, String branch) throws IOException {
        Requester requester = new Requester(root)
            .with("path", path)
            .with("message", commitMessage)
            .with("sha", sha)
            .method("DELETE");

        if (branch != null) {
            requester.with("branch", branch);
        }

        GHContentUpdateResponse response = requester.to(getApiRoute(), GHContentUpdateResponse.class);

        response.getCommit().wrapUp(repository);
        return response;
    }

    private String getApiRoute() {
        return "/repos/" + repository.getOwnerName() + "/" + repository.getName() + "/contents/" + path;
    }

    GHContent wrap(GHRepository owner) {
        this.repository = owner;
        this.root = owner.root;
        return this;
    }
    GHContent wrap(GitHub root) {
        this.root = root;
        if (repository!=null)
            repository.wrap(root);
        return this;
    }


    public static GHContent[] wrap(GHContent[] contents, GHRepository repository) {
        for (GHContent unwrappedContent : contents) {
            unwrappedContent.wrap(repository);
        }
        return contents;
    }
}
