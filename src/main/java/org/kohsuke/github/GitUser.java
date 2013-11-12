package org.kohsuke.github;

import java.util.Date;

/**
 * Represents a user in Git who authors/commits a commit.
 *
 * In contrast, {@link GHUser} is an user of GitHub. Because Git allows a person to
 * use multiple e-mail addresses and names when creating a commit, there's generally
 * no meaningful mapping between {@link GHUser} and {@link GitUser}.
 *
 * @author Kohsuke Kawaguchi
 */
public class GitUser {
    private String name, email, date;

    /**
     * Human readable name of the user, such as "Kohsuke Kawaguchi"
     */
    public String getName() {
        return name;
    }

    /**
     * E-mail address, such as "foo@example.com"
     */
    public String getEmail() {
        return email;
    }

    /**
     * This field doesn't appear to be consistently available in all the situations where this class
     * is used.
     */
    public Date getDate() {
        return GitHub.parseDate(date);
    }
}
