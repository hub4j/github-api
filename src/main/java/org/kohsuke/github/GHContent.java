package org.kohsuke.github;

import java.io.IOException;

import javax.xml.bind.DatatypeConverter;

/**
 * A Content of a repository.
 *
 * @author Alexandre COLLIGNON
 */
public final class GHContent {
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

    public String getContent() {
        return new String(DatatypeConverter.parseBase64Binary(getEncodedContent()));
    }

    public String getEncodedContent() {
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

    public boolean isFile() {
        return "file".equals(type);
    }

    public boolean isDirectory() {
        return "dir".equals(type);
    }

    public GHContentUpdateResponse update(String newContent, String commitMessage) throws IOException {
        return update(newContent, commitMessage, null);
    }

    public GHContentUpdateResponse update(String newContent, String commitMessage, String branch) throws IOException {
        Requester requester = new Requester(owner.root)
            .with("path", path)
            .with("message", commitMessage)
            .with("sha", sha)
            .with("content", DatatypeConverter.printBase64Binary(newContent.getBytes()))
            .method("PUT");

        if (branch != null) {
            requester.with("branch", branch);
        }

        GHContentUpdateResponse response = requester.to(getApiRoute(), GHContentUpdateResponse.class);

        response.getContent().wrap(owner);
        response.getCommit().wrapUp(owner);

        this.content = newContent;
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
