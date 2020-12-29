package org.kohsuke.github;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

import java.util.Date;

import javax.annotation.CheckForNull;

/**
 * Represents a user in Git who authors/commits a commit.
 * <p>
 * In contrast, {@link GHUser} is an user of GitHub. Because Git allows a person to use multiple e-mail addresses and
 * names when creating a commit, there's generally no meaningful mapping between {@link GHUser} and {@link GitUser}.
 *
 * @author Kohsuke Kawaguchi
 */
@SuppressFBWarnings(value = { "UWF_UNWRITTEN_PUBLIC_OR_PROTECTED_FIELD", "UWF_UNWRITTEN_FIELD", "NP_UNWRITTEN_FIELD" },
        justification = "JSON API")
public class GitUser {
    private String name, email, date, username;

    /**
     * Gets the git user name for an author or committer on a git commit.
     *
     * @return Human readable name of the user, such as "Kohsuke Kawaguchi"
     */
    public String getName() {
        return name;
    }

    /**
     * Gets the git email for an author or committer on a git commit.
     *
     * @return E-mail address, such as "foo@example.com"
     */
    public String getEmail() {
        return email;
    }

    /**
     * Gets username. Note: it presents only in events.
     *
     * @return GitHub username
     */
    @CheckForNull
    public String getUsername() {
        return username;
    }

    /**
     * Gets date.
     *
     * @return Commit Date.
     */
    public Date getDate() {
        return GitHubClient.parseDate(date);
    }
}
