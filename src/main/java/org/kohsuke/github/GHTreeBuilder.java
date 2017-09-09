package org.kohsuke.github;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Builder pattern for creating a new tree.
 * Based on https://developer.github.com/v3/git/trees/#create-a-tree
 */
public class GHTreeBuilder {
    private final GHRepository repo;
    private final Requester req;

    private final List<TreeEntry> treeEntries = new ArrayList<TreeEntry>();

    private static final class TreeEntry {
        private final String path;
        private final String mode;
        private final String type;
        private String sha;
        private String content;

        private TreeEntry(String path, String mode, String type) {
            this.path = path;
            this.mode = mode;
            this.type = type;
        }
    }

    GHTreeBuilder(GHRepository repo) {
        this.repo = repo;
        req = new Requester(repo.root);
    }

    /**
     * @param baseTree the SHA of tree you want to update with new data
     */
    public GHTreeBuilder baseTree(String baseTree) {
        req.with("base_tree", baseTree);
        return this;
    }

    /**
     * Adds a new entry to the tree.
     * Exactly one of the parameters {@code sha} and {@code content} must be non-null.
     */
    public GHTreeBuilder entry(String path, String mode, String type, String sha, String content) {
        TreeEntry entry = new TreeEntry(path, mode, type);
        entry.sha = sha;
        entry.content = content;
        treeEntries.add(entry);
        return this;
    }

    /**
     * Specialized version of {@link #entry(String, String, String, String, String)} for adding an existing blob referred by its SHA.
     */
    public GHTreeBuilder shaEntry(String path, String sha, boolean executable) {
        TreeEntry entry = new TreeEntry(path, executable ? "100755" : "100644", "blob");
        entry.sha = sha;
        treeEntries.add(entry);
        return this;
    }

    /**
     * Specialized version of {@link #entry(String, String, String, String, String)} for adding a text file with the specified {@code content}.
     */
    public GHTreeBuilder textEntry(String path, String content, boolean executable) {
        TreeEntry entry = new TreeEntry(path, executable ? "100755" : "100644", "blob");
        entry.content = content;
        treeEntries.add(entry);
        return this;
    }

    private String getApiTail() {
        return String.format("/repos/%s/%s/git/trees", repo.getOwnerName(), repo.getName());
    }

    /**
     * Creates a tree based on the parameters specified thus far.
     */
    public GHTree create() throws IOException {
        req._with("tree", treeEntries);
        return req.method("POST").to(getApiTail(), GHTree.class).wrap(repo);
    }
}
