package org.kohsuke.github;

import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

/**
 * Builder pattern for creating a new commit.
 * Based on https://developer.github.com/v3/git/commits/#create-a-commit
 */
public class GHCommitBuilder {
    private final GHRepository repo;
    private final Requester req;

    private final List<String> parents = new ArrayList<String>();

    private static final class UserInfo {
        private final String name;
        private final String email;
        private final String date;

        private UserInfo(String name, String email, Date date) {
            this.name = name;
            this.email = email;
            TimeZone tz = TimeZone.getTimeZone("UTC");
            DateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
            df.setTimeZone(tz);
            this.date = df.format((date != null) ? date : new Date());
        }
    }

    public GHCommitBuilder(GHRepository repo) {
        this.repo = repo;
        req = new Requester(repo.root);
    }

    /**
     * @param message the commit message
     */
    public GHCommitBuilder message(String message) {
        req.with("message", message);
        return this;
    }

    /**
     * @param tree the SHA of the tree object this commit points to
     */
    public GHCommitBuilder tree(String tree) {
        req.with("tree", tree);
        return this;
    }

    /**
     * @param parent the SHA of a parent commit.
     */
    public GHCommitBuilder parent(String parent) {
        parents.add(parent);
        return this;
    }

    /**
     * Configures the author of this commit.
     */
    public GHCommitBuilder author(String name, String email, Date date) {
        req._with("author", new UserInfo(name, email, date));
        return this;
    }

    /**
     * Configures the committer of this commit.
     */
    public GHCommitBuilder committer(String name, String email, Date date) {
        req._with("committer", new UserInfo(name, email, date));
        return this;
    }

    private String getApiTail() {
        return String.format("/repos/%s/%s/git/commits", repo.getOwnerName(), repo.getName());
    }

    /**
     * Creates a blob based on the parameters specified thus far.
     */
    public GHCommit create() throws IOException {
        req._with("parents", parents);
        return req.method("POST").to(getApiTail(), GHCommit.class).wrapUp(repo);
    }
}
