package org.kohsuke.github;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URL;
import java.util.Date;

/**
 * A conversation in the notification API.
 *
 * @author Kohsuke Kawaguchi
 * @see <a href="https://developer.github.com/v3/activity/notifications/">documentation</a>
 * @see GHNotificationStream
 */
@SuppressFBWarnings(value = { "UWF_UNWRITTEN_PUBLIC_OR_PROTECTED_FIELD", "UWF_UNWRITTEN_FIELD", "NP_UNWRITTEN_FIELD" },
        justification = "JSON API")
public class GHThread extends GHObject {
    private GitHub root;
    private GHRepository repository;
    private Subject subject;
    private String reason;
    private boolean unread;
    private String last_read_at;
    private String url, subscription_url;

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
     *
     * @return the last read at
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

    /**
     * Gets reason.
     *
     * @return the reason
     */
    public String getReason() {
        return reason;
    }

    /**
     * Gets repository.
     *
     * @return the repository
     */
    public GHRepository getRepository() {
        return repository;
    }

    // TODO: how to expose the subject?

    /**
     * Is read boolean.
     *
     * @return the boolean
     */
    public boolean isRead() {
        return !unread;
    }

    /**
     * Gets title.
     *
     * @return the title
     */
    public String getTitle() {
        return subject.title;
    }

    /**
     * Gets type.
     *
     * @return the type
     */
    public String getType() {
        return subject.type;
    }

    /**
     * Gets last comment url.
     *
     * @return the last comment url
     */
    public String getLastCommentUrl() {
        return subject.latest_comment_url;
    }

    /**
     * If this thread is about an issue, return that issue.
     *
     * @return null if this thread is not about an issue.
     * @throws IOException
     *             the io exception
     */
    public GHIssue getBoundIssue() throws IOException {
        if (!"Issue".equals(subject.type) && "PullRequest".equals(subject.type))
            return null;
        return repository.getIssue(Integer.parseInt(subject.url.substring(subject.url.lastIndexOf('/') + 1)));
    }

    /**
     * If this thread is about a pull request, return that pull request.
     *
     * @return null if this thread is not about a pull request.
     * @throws IOException
     *             the io exception
     */
    public GHPullRequest getBoundPullRequest() throws IOException {
        if (!"PullRequest".equals(subject.type))
            return null;
        return repository.getPullRequest(Integer.parseInt(subject.url.substring(subject.url.lastIndexOf('/') + 1)));
    }

    /**
     * If this thread is about a commit, return that commit.
     *
     * @return null if this thread is not about a commit.
     * @throws IOException
     *             the io exception
     */
    public GHCommit getBoundCommit() throws IOException {
        if (!"Commit".equals(subject.type))
            return null;
        return repository.getCommit(subject.url.substring(subject.url.lastIndexOf('/') + 1));
    }

    GHThread wrap(GitHub root) {
        this.root = root;
        if (this.repository != null)
            this.repository.wrap(root);
        return this;
    }

    /**
     * Marks this thread as read.
     *
     * @throws IOException
     *             the io exception
     */
    public void markAsRead() throws IOException {
        root.createRequest().method("PATCH").withUrlPath(url).send();
    }

    /**
     * Subscribes to this conversation to get notifications.
     *
     * @param subscribed
     *            the subscribed
     * @param ignored
     *            the ignored
     * @return the gh subscription
     * @throws IOException
     *             the io exception
     */
    public GHSubscription subscribe(boolean subscribed, boolean ignored) throws IOException {
        return root.createRequest()
                .method("PUT")
                .with("subscribed", subscribed)
                .with("ignored", ignored)
                .withUrlPath(subscription_url)
                .fetch(GHSubscription.class)
                .wrapUp(root);
    }

    /**
     * Returns the current subscription for this thread.
     *
     * @return null if no subscription exists.
     * @throws IOException
     *             the io exception
     */
    public GHSubscription getSubscription() throws IOException {
        try {
            return root.createRequest()
                    .method("POST")
                    .withUrlPath(subscription_url)
                    .fetch(GHSubscription.class)
                    .wrapUp(root);
        } catch (FileNotFoundException e) {
            return null;
        }
    }
}
