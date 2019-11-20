package org.kohsuke.github;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

/**
 * Builder pattern for creating a new tree. Based on https://developer.github.com/v3/git/trees/#create-a-tree
 */
public class GHTreeBuilder {
    private final GHRepository repo;
    private final Requester req;

    private final List<TreeEntry> treeEntries = new ArrayList<TreeEntry>();

    @SuppressFBWarnings("URF_UNREAD_FIELD")
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
     * Base tree gh tree builder.
     *
     * @param baseTree
     *            the SHA of tree you want to update with new data
     * @return the gh tree builder
     */
    public GHTreeBuilder baseTree(String baseTree) {
        req.with("base_tree", baseTree);
        return this;
    }

    /**
     * Adds a new entry to the tree. Exactly one of the parameters {@code sha} and {@code content} must be non-null.
     *
     * @param path
     *            the path
     * @param mode
     *            the mode
     * @param type
     *            the type
     * @param sha
     *            the sha
     * @param content
     *            the content
     * @return the gh tree builder
     * @deprecated use {@link #add(String, String, boolean)} or {@link #add(String, byte[], boolean)} instead.
     */
    @Deprecated
    public GHTreeBuilder entry(String path, String mode, String type, String sha, String content) {
        TreeEntry entry = new TreeEntry(path, mode, type);
        entry.sha = sha;
        entry.content = content;
        treeEntries.add(entry);
        return this;
    }

    /**
     * Specialized version of {@link #entry(String, String, String, String, String)} for adding an existing blob
     * referred by its SHA.
     *
     * @param path
     *            the path
     * @param sha
     *            the sha
     * @param executable
     *            the executable
     * @return the gh tree builder
     * @deprecated use {@link #add(String, String, boolean)} or {@link #add(String, byte[], boolean)} instead.
     */
    @Deprecated
    public GHTreeBuilder shaEntry(String path, String sha, boolean executable) {
        TreeEntry entry = new TreeEntry(path, executable ? "100755" : "100644", "blob");
        entry.sha = sha;
        treeEntries.add(entry);
        return this;
    }

    /**
     * Specialized version of {@link #entry(String, String, String, String, String)} for adding a text file with the
     * specified {@code content}.
     *
     * @param path
     *            the path
     * @param content
     *            the content
     * @param executable
     *            the executable
     * @return the gh tree builder
     * @apiNote It seems that GitHub no longer supports the 'content' parameter.
     * @deprecated use {@link #add(String, String, boolean)} or {@link #add(String, byte[], boolean)} instead.
     */
    @Deprecated
    public GHTreeBuilder textEntry(String path, String content, boolean executable) {
        TreeEntry entry = new TreeEntry(path, executable ? "100755" : "100644", "blob");
        entry.content = content;
        treeEntries.add(entry);
        return this;
    }

    /**
     * Adds a new entry with the given binary content to the tree.
     *
     * @param path
     *            the file path in the tree
     * @param content
     *            the file content as byte array
     * @param executable
     *            true, if the file should be executable
     * @return this GHTreeBuilder
     */
    public GHTreeBuilder add(String path, byte[] content, boolean executable) {
        try {
            String dataSha = repo.createBlob().binaryContent(content).create().getSha();
            return shaEntry(path, dataSha, executable);
        } catch (IOException e) {
            throw new GHException("Cannot create binary content of '" + path + "'", e);
        }
    }

    /**
     * Adds a new entry with the given text content to the tree.
     *
     * @param path
     *            the file path in the tree
     * @param content
     *            the file content as UTF-8 encoded string
     * @param executable
     *            true, if the file should be executable
     * @return this GHTreeBuilder
     */
    public GHTreeBuilder add(String path, String content, boolean executable) {
        return add(path, content.getBytes(StandardCharsets.UTF_8), executable);
    }

    private String getApiTail() {
        return String.format("/repos/%s/%s/git/trees", repo.getOwnerName(), repo.getName());
    }

    /**
     * Creates a tree based on the parameters specified thus far.
     *
     * @return the gh tree
     * @throws IOException
     *             the io exception
     */
    public GHTree create() throws IOException {
        req.with("tree", treeEntries);
        return req.method("POST").to(getApiTail(), GHTree.class).wrap(repo);
    }
}
