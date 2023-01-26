package org.kohsuke.github;

import com.infradna.tool.bridge_method_injector.WithBridgeMethods;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

// TODO: Auto-generated Javadoc
/**
 * The response that is returned when updating repository content.
 */
public class GHContentUpdateResponse {
    private GHContent content;
    private GitCommit commit;

    /**
     * Gets content.
     *
     * @return the content
     */
    @SuppressFBWarnings(value = { "EI_EXPOSE_REP" }, justification = "Expected behavior")
    public GHContent getContent() {
        return content;
    }

    /**
     * Gets commit.
     *
     * @return the commit
     */
    @SuppressFBWarnings(value = { "EI_EXPOSE_REP" }, justification = "Expected behavior")
    @WithBridgeMethods(value = GHCommit.class, adapterMethod = "gitCommitToGHCommit")
    public GitCommit getCommit() {
        return commit;
    }

    @SuppressFBWarnings(value = "UPM_UNCALLED_PRIVATE_METHOD", justification = "bridge method of getCommit")
    private Object gitCommitToGHCommit(GitCommit commit, Class targetType) {
        return new GHCommit(new GHCommit.ShortInfo(commit));
    }

}
