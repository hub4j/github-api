package org.kohsuke.github;

import java.io.IOException;
import java.net.URL;
import java.util.AbstractList;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * A commit in a repository.
 *
 * @author Kohsuke Kawaguchi
 * @see GHRepository#getCommit(String)
 * @see GHCommitComment#getCommit()
 */
public class GHCommit {
    private GHRepository owner;

    public static class Stats {
        int total,additions,deletions;
    }

    /**
     * A file that was modified.
     */
    public static class File {
        String status;
        int changes,additions,deletions;
        String raw_url, blob_url, filename, sha, patch;

        /**
         * Number of lines added + removed.
         */
        public int getLinesChanged() {
            return changes;
        }

        /**
         * Number of lines added.
         */
        public int getLinesAdded() {
            return additions;
        }

        /**
         * Number of lines removed.
         */
        public int getLinesDeleted() {
            return deletions;
        }

        /**
         * "modified", "added", or "deleted"
         */
        public String getStatus() {
            return status;
        }

        /**
         * Just the base name and the extension without any directory name.
         */
        public String getFileName() {
            return filename;
        }

        /**
         * The actual change.
         */
        public String getPatch() {
            return patch;
        }

        /**
         * URL like 'https://raw.github.com/jenkinsci/jenkins/4eb17c197dfdcf8ef7ff87eb160f24f6a20b7f0e/core/pom.xml'
         * that resolves to the actual content of the file.
         */
        public URL getRawUrl() {
            return GitHub.parseURL(raw_url);
        }

        /**
         * URL like 'https://github.com/jenkinsci/jenkins/blob/1182e2ebb1734d0653142bd422ad33c21437f7cf/core/pom.xml'
         * that resolves to the HTML page that describes this file.
         */
        public URL getBlobUrl() {
            return GitHub.parseURL(blob_url);
        }

        /**
         * [0-9a-f]{40} SHA1 checksum.
         */
        public String getSha() {
            return sha;
        }
    }

    public static class Parent {
        String url,sha;
    }

    static class User {
        // TODO: what if someone who doesn't have an account on GitHub makes a commit?
        String url,avatar_url,login,gravatar_id;
        int id;
    }

    String url,sha;
    List<File> files;
    Stats stats;
    List<Parent> parents;
    User author,committer;

    /**
     * The repository that contains the commit.
     */
    public GHRepository getOwner() {
        return owner;
    }

    /**
     * Number of lines added + removed.
     */
    public int getLinesChanged() {
        return stats.total;
    }

    /**
     * Number of lines added.
     */
    public int getLinesAdded() {
        return stats.additions;
    }

    /**
     * Number of lines removed.
     */
    public int getLinesDeleted() {
        return stats.deletions;
    }

    /**
     * [0-9a-f]{40} SHA1 checksum.
     */
    public String getSHA1() {
        return sha;
    }

    /**
     * List of files changed/added/removed in this commit.
     *
     * @return
     *      Can be empty but never null.
     */
    public List<File> getFiles() {
        return files!=null ? Collections.unmodifiableList(files) : Collections.<File>emptyList();
    }

    /**
     * Returns the SHA1 of parent commit objects.
     */
    public List<String> getParentSHA1s() {
        if (parents==null)  return Collections.emptyList();
        return new AbstractList<String>() {
            @Override
            public String get(int index) {
                return parents.get(index).sha;
            }

            @Override
            public int size() {
                return parents.size();
            }
        };
    }

    /**
     * Resolves the parent commit objects and return them.
     */
    public List<GHCommit> getParents() throws IOException {
        List<GHCommit> r = new ArrayList<GHCommit>();
        for (String sha1 : getParentSHA1s())
            r.add(owner.getCommit(sha1));
        return r;
    }

    public GHUser getAuthor() throws IOException {
        return resolveUser(author);
    }

    public GHUser getCommitter() throws IOException {
        return resolveUser(committer);
    }

    private GHUser resolveUser(User author) throws IOException {
        if (author==null || author.login==null) return null;
        return owner.root.getUser(author.login);
    }

    /**
     * Lists up all the commit comments in this repository.
     */
    public PagedIterable<GHCommitComment> listComments() {
        return new PagedIterable<GHCommitComment>() {
            public PagedIterator<GHCommitComment> iterator() {
                return new PagedIterator<GHCommitComment>(owner.root.retrieve().asIterator(String.format("/repos/%s/%s/commits/%s/comments", owner.getOwnerName(), owner.getName(), sha), GHCommitComment[].class)) {
                    @Override
                    protected void wrapUp(GHCommitComment[] page) {
                        for (GHCommitComment c : page)
                            c.wrap(owner);
                    }
                };
            }
        };
    }

    /**
     * Creates a commit comment.
     *
     * I'm not sure how path/line/position parameters interact with each other.
     */
    public GHCommitComment createComment(String body, String path, Integer line, Integer position) throws IOException {
        GHCommitComment r = new Requester(owner.root)
                .with("body",body)
                .with("path",path)
                .with("line",line)
                .with("position",position)
                .withCredential()
                .to(String.format("/repos/%s/%s/commits/%s/comments",owner.getOwnerName(),owner.getName(),sha),GHCommitComment.class);
        return r.wrap(owner);
    }

    public GHCommitComment createComment(String body) throws IOException {
        return createComment(body,null,null,null);
    }

    /**
     * Gets the status of this commit, newer ones first.
     */
    public PagedIterable<GHCommitStatus> listStatuses() throws IOException {
        return owner.listCommitStatuses(sha);
    }

    /**
     * Gets the last status of this commit, which is what gets shown in the UI.
     */
    public GHCommitStatus getLastStatus() throws IOException {
        return owner.getLastCommitStatus(sha);
    }

    GHCommit wrapUp(GHRepository owner) {
        this.owner = owner;
        return this;
    }
}
