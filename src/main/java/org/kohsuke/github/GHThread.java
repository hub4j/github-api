package org.kohsuke.github;

import java.util.Date;

/**
 * A conversation in the notification API.
 *
 *
 * @see <a href="https://developer.github.com/v3/activity/notifications/">documentation</a>
 * @author Kohsuke Kawaguchi
 */
public class GHThread extends GHObject {
    private GHRepository repository;
    private Subject subject;
    private String reason;
    private boolean unread;
    private String last_read_at;

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
}
