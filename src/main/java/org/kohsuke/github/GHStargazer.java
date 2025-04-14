package org.kohsuke.github;

import com.infradna.tool.bridge_method_injector.WithBridgeMethods;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

import java.time.Instant;
import java.util.Date;

// TODO: Auto-generated Javadoc
/**
 * A stargazer at a repository on GitHub.
 *
 * @author noctarius
 */
@SuppressFBWarnings(value = { "UWF_UNWRITTEN_FIELD", "NP_UNWRITTEN_FIELD" }, justification = "JSON API")
public class GHStargazer extends GitHubBridgeAdapterObject {

    private GHRepository repository;

    private String starredAt;
    private GHUser user;
    /**
     * Create default GHStargazer instance
     */
    public GHStargazer() {
    }

    /**
     * Gets the repository that is stargazed.
     *
     * @return the starred repository
     */
    @SuppressFBWarnings(value = { "EI_EXPOSE_REP" }, justification = "Expected behavior")
    public GHRepository getRepository() {
        return repository;
    }

    /**
     * Gets the date when the repository was starred, however old stars before August 2012, will all show the date the
     * API was changed to support starredAt.
     *
     * @return the date the stargazer was added
     */
    @WithBridgeMethods(value = Date.class, adapterMethod = "instantToDate")
    public Instant getStarredAt() {
        return GitHubClient.parseInstant(starredAt);
    }

    /**
     * Gets the user that starred the repository.
     *
     * @return the stargazer user
     */
    @SuppressFBWarnings(value = { "EI_EXPOSE_REP" }, justification = "Expected behavior")
    public GHUser getUser() {
        return user;
    }

    /**
     * Wrap up.
     *
     * @param repository
     *            the repository
     */
    void wrapUp(GHRepository repository) {
        this.repository = repository;
    }
}
