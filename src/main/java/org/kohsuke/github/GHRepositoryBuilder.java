package org.kohsuke.github;

import java.io.IOException;
import java.net.URL;

import static org.kohsuke.github.Previews.BAPTISTE;
import static org.kohsuke.github.Previews.NEBULA;

abstract class GHRepositoryBuilder<S> extends AbstractBuilder<GHRepository, S> {

    protected GHRepositoryBuilder(Class<S> intermediateReturnType, GitHub root, GHRepository baseInstance) {
        super(GHRepository.class, intermediateReturnType, root, baseInstance);
    }

    /**
     * Allow or disallow squash-merging pull requests.
     *
     * @param enabled
     *            true if enabled
     *
     * @return a builder to continue with building
     *
     * @throws IOException
     *             In case of any networking error or error from the server.
     */
    public S allowSquashMerge(boolean enabled) throws IOException {
        return with("allow_squash_merge", enabled);
    }

    /**
     * Allow or disallow merging pull requests with a merge commit.
     *
     * @param enabled
     *            true if enabled
     *
     * @return a builder to continue with building
     *
     * @throws IOException
     *             In case of any networking error or error from the server.
     */
    public S allowMergeCommit(boolean enabled) throws IOException {
        return with("allow_merge_commit", enabled);
    }

    /**
     * Allow or disallow rebase-merging pull requests.
     *
     * @param enabled
     *            true if enabled
     *
     * @return a builder to continue with building
     *
     * @throws IOException
     *             In case of any networking error or error from the server.
     */
    public S allowRebaseMerge(boolean enabled) throws IOException {
        return with("allow_rebase_merge", enabled);
    }

    /**
     * After pull requests are merged, you can have head branches deleted automatically.
     *
     * @param enabled
     *            true if enabled
     *
     * @return a builder to continue with building
     *
     * @throws IOException
     *             In case of any networking error or error from the server.
     */
    public S deleteBranchOnMerge(boolean enabled) throws IOException {
        return with("delete_branch_on_merge", enabled);
    }

    /**
     * Default repository branch
     *
     * @param branch
     *            branch name
     *
     * @return a builder to continue with building
     *
     * @throws IOException
     *             In case of any networking error or error from the server.
     */
    public S defaultBranch(String branch) throws IOException {
        return with("default_branch", branch);
    }

    /**
     * Description for repository
     *
     * @param description
     *            description of repository
     *
     * @return a builder to continue with building
     *
     * @throws IOException
     *             In case of any networking error or error from the server.
     */
    public S description(String description) throws IOException {
        return with("description", description);
    }

    /**
     * Homepage for repository
     *
     * @param homepage
     *            homepage of repository
     *
     * @return a builder to continue with building
     *
     * @throws IOException
     *             In case of any networking error or error from the server.
     */
    public S homepage(URL homepage) throws IOException {
        return homepage(homepage.toExternalForm());
    }

    /**
     * Homepage for repository
     *
     * @param homepage
     *            homepage of repository
     *
     * @return a builder to continue with building
     *
     * @throws IOException
     *             In case of any networking error or error from the server.
     */
    public S homepage(String homepage) throws IOException {
        return with("homepage", homepage);
    }

    /**
     * Sets the repository to private
     *
     * @param enabled
     *            private if true
     *
     * @return a builder to continue with building
     *
     * @throws IOException
     *             In case of any networking error or error from the server.
     */
    public S private_(boolean enabled) throws IOException {
        return with("private", enabled);
    }

    /**
     * Enables issue tracker
     *
     * @param enabled
     *            true if enabled
     *
     * @return a builder to continue with building
     *
     * @throws IOException
     *             In case of any networking error or error from the server.
     */
    public S issues(boolean enabled) throws IOException {
        return with("has_issues", enabled);
    }

    /**
     * Enables projects
     *
     * @param enabled
     *            true if enabled
     *
     * @return a builder to continue with building
     *
     * @throws IOException
     *             In case of any networking error or error from the server.
     */
    public S projects(boolean enabled) throws IOException {
        return with("has_projects", enabled);
    }

    /**
     * Enables wiki
     *
     * @param enabled
     *            true if enabled
     * @return a builder to continue with building
     * @throws IOException
     *             In case of any networking error or error from the server.
     */
    public S wiki(boolean enabled) throws IOException {
        return with("has_wiki", enabled);
    }

    /**
     * Enables downloads
     *
     * @param enabled
     *            true if enabled
     *
     * @return a builder to continue with building
     *
     * @throws IOException
     *             In case of any networking error or error from the server.
     */
    public S downloads(boolean enabled) throws IOException {
        return with("has_downloads", enabled);
    }

    /**
     * Specifies whether the repository is a template.
     *
     * @param enabled
     *            true if enabled
     * @return a builder to continue with building
     * @throws IOException
     *             In case of any networking error or error from the server.
     */
    @Preview(BAPTISTE)
    @Deprecated
    public S isTemplate(boolean enabled) throws IOException {
        requester.withPreview(BAPTISTE);
        return with("is_template", enabled);
    }

    /**
     * Specify Repository's Visibility
     *
     * @param visibility
     *            org.kohsuke.github.GHVisibility PUBLIC, PRIVATE, INTERNAL
     *
     * @deprecated until preview feature has graduated to stable
     *
     * @return a builder to continue with building
     * @throws IOException
     *             In case of any networking error or error from the server.
     */
    @Deprecated
    @Preview(NEBULA)
    public S visibility(GHVisibility visibility) throws IOException {
        requester.withPreview(NEBULA);
        return with("visibility", visibility);
    }

    @Override
    public GHRepository done() throws IOException {
        return super.done().wrap(this.root);
    }

    S archive() throws IOException {
        return with("archived", true);
    }

    S name(String name) throws IOException {
        return with("name", name);
    }
}
