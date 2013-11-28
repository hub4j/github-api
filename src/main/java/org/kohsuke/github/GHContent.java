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

    public void update(String newContent, String commitMessage) throws IOException {
        new Requester(owner.root)
            .with("path", path)
            .with("message", commitMessage)
            .with("sha", sha)
            .with("content", DatatypeConverter.printBase64Binary(newContent.getBytes()))
            .method("PUT")
            .to(getApiRoute());

        this.content = newContent;
    }

    private String getApiRoute() {
        return "/repos/" + owner.getOwnerName() + "/" + owner.getName() + "/contents/" + path;
    }

    GHContent wrap(GHRepository owner) {
        this.owner = owner;
        return this;
    }
}
