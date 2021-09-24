package org.kohsuke.github;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

public class GHIssueQueryBuilder extends GHQueryBuilder<GHIssue> {
    private final GHRepository repo;
    private List<String> labels = new ArrayList<>();

    GHIssueQueryBuilder(GHRepository repo) {
        super(repo.root());
        this.repo = repo;
    }

    /**
     * Milestone gh issue query builder.
     *
     * @param milestone
     *            the milestone
     * @return the gh issue request query builder
     */
    public GHIssueQueryBuilder milestone(String milestone) {
        req.with("milestone", milestone);
        return this;
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
     * Assignee gh issue query builder.
     *
     * @param assignee
     *            the assignee
     * @return the gh issue query builder
     */
    public GHIssueQueryBuilder assignee(String assignee) {
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
    public GHIssueQueryBuilder creator(String creator) {
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
    public GHIssueQueryBuilder mentioned(String mentioned) {
        req.with("mentioned", mentioned);
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
        if (label != null) {
            labels.add(label);
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

    @Override
    public PagedIterable<GHIssue> list() {
        return req.with("labels", labels.stream().collect(Collectors.joining(",")))
                .withUrlPath(repo.getApiTailUrl("issues"))
                .toIterable(GHIssue[].class, item -> item.wrap(repo));
    }

    /**
     * The enum Sort.
     */
    public enum Sort {
        CREATED, UPDATED, COMMENTS
    }
}
