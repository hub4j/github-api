package org.kohsuke.github;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import org.kohsuke.github.BranchProtection.RequiredStatusChecks;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;

import static org.kohsuke.github.Previews.LOKI;

/**
 * A branch in a repository.
 * 
 * @author Yusuke Kokubo
 */
@SuppressFBWarnings(value = {"UWF_UNWRITTEN_PUBLIC_OR_PROTECTED_FIELD", "UWF_UNWRITTEN_FIELD", 
    "NP_UNWRITTEN_FIELD", "URF_UNREAD_FIELD"}, justification = "JSON API")
public class GHBranch {
    private GitHub root;
    private GHRepository owner;

    private String name;
    private Commit commit;

    public static class Commit {
        String sha;
        
        @SuppressFBWarnings(value = "UUF_UNUSED_FIELD", justification = "We don't provide it in API now")
        String url;
    }

    public GitHub getRoot() {
        return root;
    }

    /**
     * Repository that this branch is in.
     */
    public GHRepository getOwner() {
        return owner;
    }

    public String getName() {
        return name;
    }

    /**
     * The commit that this branch currently points to.
     */
    public String getSHA1() {
        return commit.sha;
    }

    /**
     * Disables branch protection and allows anyone with push access to push changes.
     */
    @Preview @Deprecated
    public void disableProtection() throws IOException {
        BranchProtection bp = new BranchProtection();
        bp.enabled = false;
        setProtection(bp);
    }

    /**
     * Enables branch protection to control what commit statuses are required to push.
     *
     * @see GHCommitStatus#getContext()
     */
    @Preview @Deprecated
    public void enableProtection(EnforcementLevel level, Collection<String> contexts) throws IOException {
        BranchProtection bp = new BranchProtection();
        bp.enabled = true;
        bp.requiredStatusChecks = new RequiredStatusChecks();
        bp.requiredStatusChecks.enforcement_level = level;
        bp.requiredStatusChecks.contexts.addAll(contexts);
        setProtection(bp);
    }

    @Preview @Deprecated
    public void enableProtection(EnforcementLevel level, String... contexts) throws IOException {
        enableProtection(level, Arrays.asList(contexts));
    }

    private void setProtection(BranchProtection bp) throws IOException {
        new Requester(root).method("PATCH").withPreview(LOKI)._with("protection",bp).to(getApiRoute());
    }

    String getApiRoute() {
        return owner.getApiTailUrl("/branches/"+name);
    }
    
    @Override
    public String toString() {
        final String url = owner != null ? owner.getUrl().toString() : "unknown";
        return "Branch:" + name + " in " + url;
    }

    /*package*/ GHBranch wrap(GHRepository repo) {
        this.owner = repo;
        this.root = repo.root;
        return this;
    }
}
