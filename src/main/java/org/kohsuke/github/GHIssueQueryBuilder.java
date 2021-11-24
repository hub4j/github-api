package org.kohsuke.github;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public abstract class GHIssueQueryBuilder extends GHQueryBuilder<GHIssue> {
    private final List<String> labels = new ArrayList<>();

    GHIssueQueryBuilder(GitHub root) {
        super(root);
    }

    /**
     * State gh issue query builder.
     *
     * @param state
     *            the state
     * @return the gh issue query builder
     */
    public GHIssueQueryBuilder state(GHIssueState state) {
        req.with("state", state);
        return this;
    }

    /**
     * Labels gh issue query builder.
     *
     * @param label
     *            the labels
     * @return the gh issue query builder
     */
    public GHIssueQueryBuilder label(String label) {
        if (label != null && !label.trim().isEmpty()) {
            labels.add(label);
            req.with("labels", String.join(",", labels));
        }
        return this;
    }

    /**
     * Sort gh issue query builder.
     *
     * @param sort
     *            the sort
     * @return the gh issue query builder
     */
    public GHIssueQueryBuilder sort(Sort sort) {
        req.with("sort", sort);
        return this;
    }

    /**
     * Direction gh issue query builder.
     *
     * @param direction
     *            the direction
     * @return the gh issue query builder
     */
    public GHIssueQueryBuilder direction(GHDirection direction) {
        req.with("direction", direction);
        return this;
    }

    /**
     * Only issues after this date will be returned.
     *
     * @param date
     *            the date
     * @return the gh issue query builder
     */
    public GHIssueQueryBuilder since(Date date) {
        req.with("since", GitHubClient.printDate(date));
        return this;
    }

    /**
     * Only issues after this date will be returned.
     *
     * @param timestamp
     *            the timestamp
     * @return the gh issue query builder
     */
    public GHIssueQueryBuilder since(long timestamp) {
        return since(new Date(timestamp));
    }

    /**
     * Page size gh issue query builder.
     *
     * @param pageSize
     *            the page size
     * @return the gh issue query builder
     */
    public GHIssueQueryBuilder pageSize(int pageSize) {
        req.with("per_page", pageSize);
        return this;
    }

    /**
     * The enum Sort.
     */
    public enum Sort {
        CREATED, UPDATED, COMMENTS
    }

    /**
     * Gets the api url.
     *
     * @return the api url
     */
    public abstract String getApiUrl();

    public static class ForRepository extends GHIssueQueryBuilder {
        private final GHRepository repo;

        ForRepository(final GHRepository repo) {
            super(repo.root());
            this.repo = repo;
        }

        /**
         * Milestone gh issue query builder.
         * <p>
         * The milestone must be either an integer (the milestone number), the string * (issues with any milestone) or
         * the string none (issues without milestone).
         *
         * @param milestone
         *            the milestone
         * @return the gh issue request query builder
         */
        public ForRepository milestone(String milestone) {
            req.with("milestone", milestone);
            return this;
        }

        /**
         * Assignee gh issue query builder.
         *
         * @param assignee
         *            the assignee
         * @return the gh issue query builder
         */
        public ForRepository assignee(String assignee) {
            req.with("assignee", assignee);
            return this;
        }

        /**
         * Creator gh issue query builder.
         *
         * @param creator
         *            the creator
         * @return the gh issue query builder
         */
        public ForRepository creator(String creator) {
            req.with("creator", creator);
            return this;
        }

        /**
         * Mentioned gh issue query builder.
         *
         * @param mentioned
         *            the mentioned
         * @return the gh issue query builder
         */
        public ForRepository mentioned(String mentioned) {
            req.with("mentioned", mentioned);
            return this;
        }

        @Override
        public String getApiUrl() {
            return repo.getApiTailUrl("issues");
        }

        @Override
        public PagedIterable<GHIssue> list() {
            return req.withUrlPath(getApiUrl()).toIterable(GHIssue[].class, item -> item.wrap(repo));
        }
    }
}
