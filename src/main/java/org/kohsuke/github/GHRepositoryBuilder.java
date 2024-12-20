package org.kohsuke.github;

import org.kohsuke.github.GHRepository.Visibility;

import java.io.IOException;
import java.net.URL;

// TODO: Auto-generated Javadoc
/**
 * The Class GHRepositoryBuilder.
 *
 * @param <S>
 *            the generic type
 */
abstract class GHRepositoryBuilder<S> extends AbstractBuilder<GHRepository, S> {

    /**
     * Instantiates a new GH repository builder.
     *
     * @param intermediateReturnType
     *            the intermediate return type
     * @param root
     *            the root
     * @param baseInstance
     *            the base instance
     */
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
    public S allowSquashMerge(boolean enabled) {
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
    public S allowMergeCommit(boolean enabled) {
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
    public S allowRebaseMerge(boolean enabled) {
        return with("allow_rebase_merge", enabled);
    }

    /**
     * Allow or disallow private forks
     *
     * @param enabled
     *            true if enabled
     * @return a builder to continue with building
     * @throws IOException
     *             In case of any networking error or error from the server.
     */
    public S allowForking(boolean enabled) {
        return with("allow_forking", enabled);
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
    public S deleteBranchOnMerge(boolean enabled) {
        return with("delete_branch_on_merge", enabled);
    }

    /**
     * Default repository branch.
     *
     * @param branch
     *            branch name
     * @return a builder to continue with building
     * @throws IOException
     *             In case of any networking error or error from the server.
     */
    public S defaultBranch(String branch) {
        return with("default_branch", branch);
    }

    /**
     * Description for repository.
     *
     * @param description
     *            description of repository
     * @return a builder to continue with building
     * @throws IOException
     *             In case of any networking error or error from the server.
     */
    public S description(String description) {
        return with("description", description);
    }

    /**
     * Homepage for repository.
     *
     * @param homepage
     *            homepage of repository
     * @return a builder to continue with building
     * @throws IOException
     *             In case of any networking error or error from the server.
     */
    public S homepage(URL homepage) {
        return homepage(homepage.toExternalForm());
    }

    /**
     * Homepage for repository.
     *
     * @param homepage
     *            homepage of repository
     * @return a builder to continue with building
     * @throws IOException
     *             In case of any networking error or error from the server.
     */
    public S homepage(String homepage) {
        return with("homepage", homepage);
    }

    /**
     * Sets the repository to private.
     *
     * @param enabled
     *            private if true
     * @return a builder to continue with building
     * @throws IOException
     *             In case of any networking error or error from the server.
     */
    public S private_(boolean enabled) {
        return with("private", enabled);
    }

    /**
     * Sets the repository visibility.
     *
     * @param visibility
     *            visibility of repository
     * @return a builder to continue with building
     * @throws IOException
     *             In case of any networking error or error from the server.
     */
    public S visibility(final Visibility visibility) {
        return with("visibility", visibility.toString());
    }

    /**
     * Enables issue tracker.
     *
     * @param enabled
     *            true if enabled
     * @return a builder to continue with building
     * @throws IOException
     *             In case of any networking error or error from the server.
     */
    public S issues(boolean enabled) {
        return with("has_issues", enabled);
    }

    /**
     * Enables projects.
     *
     * @param enabled
     *            true if enabled
     * @return a builder to continue with building
     * @throws IOException
     *             In case of any networking error or error from the server.
     */
    public S projects(boolean enabled) {
        return with("has_projects", enabled);
    }

    /**
     * Enables wiki.
     *
     * @param enabled
     *            true if enabled
     * @return a builder to continue with building
     * @throws IOException
     *             In case of any networking error or error from the server.
     */
    public S wiki(boolean enabled) {
        return with("has_wiki", enabled);
    }

    /**
     * Enables downloads.
     *
     * @param enabled
     *            true if enabled
     * @return a builder to continue with building
     * @throws IOException
     *             In case of any networking error or error from the server.
     */
    public S downloads(boolean enabled) {
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
    public S isTemplate(boolean enabled) {
        return with("is_template", enabled);
    }

    /**
     * Done.
     *
     * @return the GH repository
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     */
    @Override
    public GHRepository done() {
        return super.done();
    }

    /**
     * Archive.
     *
     * @return the s
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     */
    S archive() {
        return with("archived", true);
    }

    /**
     * Name.
     *
     * @param name
     *            the name
     * @return the s
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     */
    S name(String name) {
        return with("name", name);
    }
}
