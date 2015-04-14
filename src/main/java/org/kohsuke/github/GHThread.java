package org.kohsuke.github;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URL;
import java.util.Date;

/**
 * A conversation in the notification API.
 *
 * @see <a href="https://developer.github.com/v3/activity/notifications/">documentation</a>
 * @see GHNotificationStream
 * @author Kohsuke Kawaguchi
 */
public class GHThread extends GHObject {
    private GitHub root;
    private GHRepository repository;
    private Subject subject;
    private String reason;
    private boolean unread;
    private String last_read_at;
    private String url,subscription_url;

    static class Subject {
        String title;
        String url;
        String latest_comment_url;
        String type;
    }

    private GHThread() {// no external construction allowed
    }

    /**
     * Returns null if the entire thread has never been read.
     */
    public Date getLastReadAt() {
        return GitHub.parseDate(last_read_at);
    }

    /**
     * @deprecated This object has no HTML URL.
     */
    @Override
    public URL getHtmlUrl() {
        return null;
    }

    public String getReason() {
        return reason;
    }

    public GHRepository getRepository() {
        return repository;
    }

    // TODO: how to expose the subject?

    public boolean isRead() {
        return !unread;
    }

    public String getTitle() {
        return subject.title;
    }

    public String getType() {
        return subject.type;
    }

    /**
     * If this thread is about an issue, return that issue.
     * 
     * @return null if this thread is not about an issue.
     */
    public GHIssue getBoundIssue() throws IOException {
        if (!"Issue".equals(subject.type) && "PullRequest".equals(subject.type))
            return null;
        return repository.getIssue(
                Integer.parseInt(subject.url.substring(subject.url.lastIndexOf('/') + 1)));
    }

    /**
     * If this thread is about a pull request, return that pull request.
     *
     * @return null if this thread is not about a pull request.
     */
    public GHPullRequest getBoundPullRequest() throws IOException {
        if (!"PullRequest".equals(subject.type))
            return null;
        return repository.getPullRequest(
                Integer.parseInt(subject.url.substring(subject.url.lastIndexOf('/') + 1)));
    }

    /**
     * If this thread is about a commit, return that commit.
     *
     * @return null if this thread is not about a commit.
     */
    public GHCommit getBoundCommit() throws IOException {
        if (!"Commit".equals(subject.type))
            return null;
        return repository.getCommit(subject.url.substring(subject.url.lastIndexOf('/') + 1));
    }

    /*package*/ GHThread wrap(GitHub root) {
        this.root = root;
        if (this.repository!=null)
            this.repository.wrap(root);
        return this;
    }

    /**
     * Marks this thread as read.
     */
    public void markAsRead() throws IOException {
        new Requester(root).method("PATCH").to(url);
    }

    /**
     * Subscribes to this conversation to get notifications.
     */
    public GHSubscription subscribe(boolean subscribed, boolean ignored) throws IOException {
        return new Requester(root)
            .with("subscribed", subscribed)
            .with("ignored", ignored)
            .method("PUT").to(subscription_url, GHSubscription.class).wrapUp(root);
    }

    /**
     * Returns the current subscription for this thread.
     *
     * @return null if no subscription exists.
     */
    public GHSubscription getSubscription() throws IOException {
        try {
            return new Requester(root).to(subscription_url, GHSubscription.class).wrapUp(root);
        } catch (FileNotFoundException e) {
            return null;
        }
    }
}
