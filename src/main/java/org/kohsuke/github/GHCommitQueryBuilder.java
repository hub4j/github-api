package org.kohsuke.github;

import java.util.Date;

/**
 * Builds up query for listing commits.
 *
 * <p>
 * Call various methods that set the filter criteria, then {@link #list()} method to actually list up the commit.
 *
 * <pre>
 * GHRepository r = ...;
 * for (GHCommit c : r.queryCommits().since(x).until(y).author("kohsuke")) {
 *     ...
 * }
 * </pre>
 *
 * @author Kohsuke Kawaguchi
 * @see GHRepository#queryCommits()
*/
public class GHCommitQueryBuilder {
    private final Requester req;
    private final GHRepository repo;

    /*package*/ GHCommitQueryBuilder(GHRepository repo) {
        this.repo = repo;
        this.req = repo.root.retrieve();    // requester to build up
    }

    /**
     * GItHub login or email address by which to filter by commit author.
     */
    public GHCommitQueryBuilder author(String author) {
        req.with("author",author);
        return this;
    }

    /**
     * Only commits containing this file path will be returned.
     */
    public GHCommitQueryBuilder path(String path) {
        req.with("path",path);
        return this;
    }

    /**
     * Specifies the SHA1 commit / tag / branch / etc to start listing commits from.
     *
     */
    public GHCommitQueryBuilder from(String ref) {
        req.with("sha",ref);
        return this;
    }

    public GHCommitQueryBuilder pageSize(int pageSize) {
        req.with("per_page",pageSize);
        return this;
    }

    public GHCommitQueryBuilder page(int startPage) {
    	if(startPage>1)
    		req.with("page",startPage);
        return this;
    }
    
    /**
     * Only commits after this date will be returned
     */
    public GHCommitQueryBuilder since(Date dt) {
        req.with("since",GitHub.printDate(dt));
        return this;
    }

    /**
     * Only commits after this date will be returned
     */
    public GHCommitQueryBuilder since(long timestamp) {
        return since(new Date(timestamp));
    }

    /**
     * Only commits before this date will be returned
     */
    public GHCommitQueryBuilder until(Date dt) {
        req.with("until",GitHub.printDate(dt));
        return this;
    }

    /**
     * Only commits before this date will be returned
     */
    public GHCommitQueryBuilder until(long timestamp) {
        return until(new Date(timestamp));
    }

    /**
     * Lists up the commits with the criteria built so far.
     */
    public PagedIterable<GHCommit> list() {
        return new PagedIterable<GHCommit>() {
            public PagedIterator<GHCommit> _iterator(int pageSize) {
                return new PagedIterator<GHCommit>(req.asIterator(repo.getApiTailUrl("commits"), GHCommit[].class, pageSize)) {
                    protected void wrapUp(GHCommit[] page) {
                        for (GHCommit c : page)
                            c.wrapUp(repo);
                    }
                };
            }
        };
    }
}
