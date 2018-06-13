package org.kohsuke.github;

public class GHContentUpdateRequest {
    private final String path;
    private final String branch;
    private final String sha;
    private final byte[] content;
    private final String commitMessage;

    public static GHContentUpdateRequest.Builder getBuilder() {
        return new GHContentUpdateRequest.Builder();
    }

    public GHContentUpdateRequest(String path, String branch, String sha, byte[] content, String commitMessage) {
        this.path = path;
        this.branch = branch;
        this.sha = sha;
        this.content = content;
        this.commitMessage = commitMessage;
    }

    private GHContentUpdateRequest(Builder builder) {
        this.path = builder.path;
        this.branch = builder.branch;
        this.sha = builder.sha;
        this.content = builder.content;
        this.commitMessage = builder.commitMessage;
    }

    public static Builder newGHContentUpdateRequest() {
        return new Builder();
    }

    public String getPath() {
        return path;
    }

    public String getBranch() {
        return branch;
    }

    public String getSha() {
        return sha;
    }

    public byte[] getContent() {
        return content;
    }

    public String getCommitMessage() {
        return commitMessage;
    }

    public static final class Builder {
        private String path;
        private String branch;
        private String sha;
        private byte[] content;
        private String commitMessage;

        private Builder() {
        }

        public GHContentUpdateRequest build() {
            return new GHContentUpdateRequest(this);
        }

        public Builder path(String path) {
            this.path = path;
            return this;
        }

        public Builder branch(String branch) {
            this.branch = branch;
            return this;
        }

        public Builder sha(String sha) {
            this.sha = sha;
            return this;
        }

        public Builder content(byte[] content) {
            this.content = content;
            return this;
        }
        public Builder content(String content) {
            this.content = content.getBytes();
            return this;
        }

        public Builder commitMessage(String commitMessage) {
            this.commitMessage = commitMessage;
            return this;
        }
    }
}
