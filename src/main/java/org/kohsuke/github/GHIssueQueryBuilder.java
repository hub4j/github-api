package org.kohsuke.github;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

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
            req.with("labels", labels.stream().collect(Collectors.joining(",")));
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

}
