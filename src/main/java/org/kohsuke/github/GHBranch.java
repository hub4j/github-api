package org.kohsuke.github;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

import java.io.IOException;
import java.net.URL;
import java.util.Collection;
import java.util.Objects;

/**
 * A branch in a repository.
 *
 * @author Yusuke Kokubo
 */
@SuppressFBWarnings(
        value = { "UWF_UNWRITTEN_PUBLIC_OR_PROTECTED_FIELD", "UWF_UNWRITTEN_FIELD", "NP_UNWRITTEN_FIELD",
                "URF_UNREAD_FIELD" },
        justification = "JSON API")
public class GHBranch {
    private GitHub root;
    private GHRepository owner;

    private String name;
    private Commit commit;
    @JsonProperty("protected")
    private boolean protection;
    private String protection_url;

    @JsonCreator
    GHBranch(@JsonProperty(value = "name", required = true) String name) throws Exception {
        Objects.requireNonNull(name);
        this.name = name;
    }

    /**
     * The type Commit.
     */
    public static class Commit {
        String sha;

        @SuppressFBWarnings(value = "UUF_UNUSED_FIELD", justification = "We don't provide it in API now")
        String url;
    }

    /**
     * Gets root.
     *
     * @return the root
     */
    public GitHub getRoot() {
        return root;
    }

    /**
     * Gets owner.
     *
     * @return the repository that this branch is in.
     */
    public GHRepository getOwner() {
        return owner;
    }

    /**
     * Gets name.
     *
     * @return the name
     */
    public String getName() {
        return name;
    }

    /**
     * Is protected boolean.
     *
     * @return true if the push to this branch is restricted via branch protection.
     */
    @Preview
    @Deprecated
    public boolean isProtected() {
        return protection;
    }

    /**
     * Gets protection url.
     *
     * @return API URL that deals with the protection of this branch.
     */
    @Preview
    @Deprecated
    public URL getProtectionUrl() {
        return GitHubClient.parseURL(protection_url);
    }

    /**
     * Gets protection.
     *
     * @return the protection
     * @throws IOException
     *             the io exception
     */
    public GHBranchProtection getProtection() throws IOException {
        return root.createRequest().setRawUrlPath(protection_url).fetch(GHBranchProtection.class).wrap(this);
    }

    /**
     * Gets sha 1.
     *
     * @return The SHA1 of the commit that this branch currently points to.
     */
    public String getSHA1() {
        return commit.sha;
    }

    /**
     * Disables branch protection and allows anyone with push access to push changes.
     *
     * @throws IOException
     *             if disabling protection fails
     */
    public void disableProtection() throws IOException {
        root.createRequest().method("DELETE").setRawUrlPath(protection_url).send();
    }

    /**
     * Enables branch protection to control what commit statuses are required to push.
     *
     * @return GHBranchProtectionBuilder for enabling protection
     * @see GHCommitStatus#getContext() GHCommitStatus#getContext()
     */
    @Preview
    @Deprecated
    public GHBranchProtectionBuilder enableProtection() {
        return new GHBranchProtectionBuilder(this);
    }

    /**
     * Enable protection.
     *
     * @param level
     *            the level
     * @param contexts
     *            the contexts
     * @throws IOException
     *             the io exception
     */
    // backward compatibility with previous signature
    @Deprecated
    public void enableProtection(EnforcementLevel level, Collection<String> contexts) throws IOException {
        switch (level) {
            case OFF :
                disableProtection();
                break;
            case NON_ADMINS :
            case EVERYONE :
                enableProtection().addRequiredChecks(contexts)
                        .includeAdmins(level == EnforcementLevel.EVERYONE)
                        .enable();
                break;
        }
    }

    String getApiRoute() {
        return owner.getApiTailUrl("/branches/" + name);
    }

    @Override
    public String toString() {
        final String url = owner != null ? owner.getUrl().toString() : "unknown";
        return "Branch:" + name + " in " + url;
    }

    GHBranch wrap(GHRepository repo) {
        this.owner = repo;
        this.root = repo.root;
        return this;
    }
}
