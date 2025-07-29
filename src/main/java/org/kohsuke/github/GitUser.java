package org.kohsuke.github;

import com.infradna.tool.bridge_method_injector.WithBridgeMethods;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

import java.time.Instant;
import java.util.Date;

import javax.annotation.CheckForNull;

// TODO: Auto-generated Javadoc
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
public class GitUser extends GitHubBridgeAdapterObject {
    private String name, email, date, username;

    /**
     * Instantiates a new git user.
     */
    public GitUser() {
        // Empty constructor for Jackson binding
    }

    /**
     * Gets date.
     *
     * @return Commit Date.
     */
    @WithBridgeMethods(value = Date.class, adapterMethod = "instantToDate")
    public Instant getDate() {
        return GitHubClient.parseInstant(date);
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
     * Gets the git user name for an author or committer on a git commit.
     *
     * @return Human readable name of the user, such as "Kohsuke Kawaguchi"
     */
    public String getName() {
        return name;
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
}
