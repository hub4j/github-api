package org.kohsuke.github;

import java.io.IOException;

import javax.xml.bind.DatatypeConverter;

/**
 * A Content of a repository.
 *
 * @author Alexandre COLLIGNON
 * @see GHRepository#getFileContent(String)
 */
@SuppressWarnings({"UnusedDeclaration"})
public class GHContent {
    private GHRepository owner;

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
        return owner;
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
     * Due to the nature of GitHub's API, you're not guaranteed that
     * the content will already be populated, so this may trigger
     * network activity, and can throw an IOException.
    **/
    public String getContent() throws IOException {
        return new String(DatatypeConverter.parseBase64Binary(getEncodedContent()));
    }

    /**
     * Retrieve the raw content that is stored at this location.
     *
     * Due to the nature of GitHub's API, you're not guaranteed that
     * the content will already be populated, so this may trigger
     * network activity, and can throw an IOException.
    **/
    public String getEncodedContent() throws IOException {
        if (content != null)
            return content;

        GHContent retrievedContent = owner.getFileContent(path);

        this.size = retrievedContent.size;
        this.sha = retrievedContent.sha;
        this.content = retrievedContent.content;
        this.url = retrievedContent.url;
        this.git_url = retrievedContent.git_url;
        this.html_url = retrievedContent.html_url;

        return content;
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
     * URL to retrieve the raw content of the file. Null if this is a directory.
     */
    public String getDownloadUrl() { return download_url; }

    public boolean isFile() {
        return "file".equals(type);
    }

    public boolean isDirectory() {
        return "dir".equals(type);
    }

    /**
     * List immediate children of this directory.
     */
    public PagedIterable<GHContent> listDirectoryContent() throws IOException {
        if (!isDirectory())
            throw new IllegalStateException(path+" is not a directory");

        return new PagedIterable<GHContent>() {
            public PagedIterator<GHContent> iterator() {
                return new PagedIterator<GHContent>(owner.root.retrieve().asIterator(url, GHContent[].class)) {
                    @Override
                    protected void wrapUp(GHContent[] page) {
                        GHContent.wrap(page,owner);
                    }
                };
            }
        };
    }

    public GHContentUpdateResponse update(String newContent, String commitMessage) throws IOException {
        return update(newContent.getBytes(), commitMessage, null);
    }

    public GHContentUpdateResponse update(String newContent, String commitMessage, String branch) throws IOException {
        return update(newContent.getBytes(), commitMessage, branch);
    }

    public GHContentUpdateResponse update(byte[] newContentBytes, String commitMessage) throws IOException {
        return update(newContentBytes, commitMessage, null);
    }

    public GHContentUpdateResponse update(byte[] newContentBytes, String commitMessage, String branch) throws IOException {
        String encodedContent = DatatypeConverter.printBase64Binary(newContentBytes);

        Requester requester = new Requester(owner.root)
            .with("path", path)
            .with("message", commitMessage)
            .with("sha", sha)
            .with("content", encodedContent)
            .method("PUT");

        if (branch != null) {
            requester.with("branch", branch);
        }

        GHContentUpdateResponse response = requester.to(getApiRoute(), GHContentUpdateResponse.class);

        response.getContent().wrap(owner);
        response.getCommit().wrapUp(owner);

        this.content = encodedContent;
        return response;
    }

    public GHContentUpdateResponse delete(String message) throws IOException {
        return delete(message, null);
    }

    public GHContentUpdateResponse delete(String commitMessage, String branch) throws IOException {
        Requester requester = new Requester(owner.root)
            .with("path", path)
            .with("message", commitMessage)
            .with("sha", sha)
            .method("DELETE");

        if (branch != null) {
            requester.with("branch", branch);
        }

        GHContentUpdateResponse response = requester.to(getApiRoute(), GHContentUpdateResponse.class);

        response.getCommit().wrapUp(owner);
        return response;
    }

    private String getApiRoute() {
        return "/repos/" + owner.getOwnerName() + "/" + owner.getName() + "/contents/" + path;
    }

    GHContent wrap(GHRepository owner) {
        this.owner = owner;
        return this;
    }

    public static GHContent[] wrap(GHContent[] contents, GHRepository repository) {
        for (GHContent unwrappedContent : contents) {
            unwrappedContent.wrap(repository);
        }
        return contents;
    }
}
