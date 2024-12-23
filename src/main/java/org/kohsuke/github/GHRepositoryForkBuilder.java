package org.kohsuke.github;

import java.io.IOException;
import java.io.InterruptedIOException;

/**
 * A builder pattern object for creating a fork of a repository.
 *
 * @see GHRepository#createFork() GHRepository#createFork()GHRepository#createFork()
 * @see <a href="https://docs.github.com/en/rest/repos/forks#create-a-fork">Repository fork API</a>
 */
public class GHRepositoryForkBuilder {
    private final GHRepository repo;
    private final Requester req;
    private String organization;
    private String name;
    private Boolean defaultBranchOnly;

    static int FORK_RETRY_INTERVAL = 3000;

    /**
     * Instantiates a new Gh repository fork builder.
     *
     * @param repo
     *            the repository
     */
    GHRepositoryForkBuilder(GHRepository repo) {
        this.repo = repo;
        this.req = repo.root().createRequest();
    }

    /**
     * Sets whether to fork only the default branch.
     *
     * @param defaultBranchOnly
     *            the default branch only
     * @return the gh repository fork builder
     */
    public GHRepositoryForkBuilder defaultBranchOnly(boolean defaultBranchOnly) {
        this.defaultBranchOnly = defaultBranchOnly;
        return this;
    }

    /**
     * Specifies the target organization for the fork.
     *
     * @param organization
     *            the organization
     * @return the gh repository fork builder
     */
    public GHRepositoryForkBuilder organization(GHOrganization organization) {
        this.organization = organization.getLogin();
        return this;
    }

    /**
     * Sets a custom name for the forked repository.
     *
     * @param name
     *            the desired repository name
     * @return the builder
     */
    public GHRepositoryForkBuilder name(String name) {
        this.name = name;
        return this;
    }

    /**
     * Creates the fork with the specified parameters.
     *
     * @return the gh repository
     * @throws IOException
     *             the io exception
     */
    public GHRepository create() throws IOException {
        if (defaultBranchOnly != null) {
            req.with("default_branch_only", defaultBranchOnly);
        }
        if (organization != null) {
            req.with("organization", organization);
        }
        if (name != null) {
            req.with("name", name);
        }

        req.method("POST").withUrlPath(repo.getApiTailUrl("forks")).send();

        // this API is asynchronous. we need to wait for a bit
        for (int i = 0; i < 10; i++) {
            GHRepository r = lookupForkedRepository();
            if (r != null) {
                return r;
            }
            sleep(FORK_RETRY_INTERVAL);
        }
        throw new IOException(createTimeoutMessage());
    }

    private GHRepository lookupForkedRepository() throws IOException {
        String repoName = name != null ? name : repo.getName();

        if (organization != null) {
            return repo.root().getOrganization(organization).getRepository(repoName);
        }
        return repo.root().getMyself().getRepository(repoName);
    }

    /**
     * Sleep.
     *
     * @param millis
     *            the millis
     * @throws IOException
     *             the io exception
     */
    void sleep(int millis) throws IOException {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            throw (IOException) new InterruptedIOException().initCause(e);
        }
    }

    /**
     * Create timeout message string.
     *
     * @return the string
     */
    String createTimeoutMessage() {
        StringBuilder message = new StringBuilder(repo.getFullName());
        message.append(" was forked");

        if (organization != null) {
            message.append(" into ").append(organization);
        }

        if (name != null) {
            message.append(" with name ").append(name);
        }

        message.append(" but can't find the new repository");
        return message.toString();
    }
}
