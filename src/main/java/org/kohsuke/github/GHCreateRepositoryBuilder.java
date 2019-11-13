package org.kohsuke.github;

import java.io.IOException;
import java.net.URL;

/**
 * Creates a repository
 *
 * @author Kohsuke Kawaguchi
 */
public class GHCreateRepositoryBuilder {
    private final GitHub root;
    protected final Requester builder;
    private final String apiUrlTail;

    /* package */ GHCreateRepositoryBuilder(GitHub root, String apiUrlTail, String name) {
        this.root = root;
        this.apiUrlTail = apiUrlTail;
        this.builder = new Requester(root);
        this.builder.with("name", name);
    }

    /**
     * Description for repository
     *
     * @param description
     *            description of repository
     *
     * @return a builder to continue with building
     */
    public GHCreateRepositoryBuilder description(String description) {
        this.builder.with("description", description);
        return this;
    }

    /**
     * Homepage for repository
     *
     * @param homepage
     *            homepage of repository
     *
     * @return a builder to continue with building
     */
    public GHCreateRepositoryBuilder homepage(URL homepage) {
        return homepage(homepage.toExternalForm());
    }

    /**
     * Homepage for repository
     * 
     * @param homepage
     *            homepage of repository
     *
     * @return a builder to continue with building
     */
    public GHCreateRepositoryBuilder homepage(String homepage) {
        this.builder.with("homepage", homepage);
        return this;
    }

    /**
     * Creates a private repository
     *
     * @param enabled
     *            private if true
     * @return a builder to continue with building
     */
    public GHCreateRepositoryBuilder private_(boolean enabled) {
        this.builder.with("private", enabled);
        return this;
    }

    /**
     * Enables issue tracker
     * 
     * @param enabled
     *            true if enabled
     * @return a builder to continue with building
     */
    public GHCreateRepositoryBuilder issues(boolean enabled) {
        this.builder.with("has_issues", enabled);
        return this;
    }

    /**
     * Enables wiki
     * 
     * @param enabled
     *            true if enabled
     * @return a builder to continue with building
     */
    public GHCreateRepositoryBuilder wiki(boolean enabled) {
        this.builder.with("has_wiki", enabled);
        return this;
    }

    /**
     * Enables downloads
     * 
     * @param enabled
     *            true if enabled
     * @return a builder to continue with building
     */
    public GHCreateRepositoryBuilder downloads(boolean enabled) {
        this.builder.with("has_downloads", enabled);
        return this;
    }

    /**
     * If true, create an initial commit with empty README.
     * 
     * @param enabled
     *            true if enabled
     * @return a builder to continue with building
     */
    public GHCreateRepositoryBuilder autoInit(boolean enabled) {
        this.builder.with("auto_init", enabled);
        return this;
    }

    /**
     * Allow or disallow squash-merging pull requests.
     * 
     * @param enabled
     *            true if enabled
     * @return a builder to continue with building
     */
    public GHCreateRepositoryBuilder allowSquashMerge(boolean enabled) {
        this.builder.with("allow_squash_merge", enabled);
        return this;
    }

    /**
     * Allow or disallow merging pull requests with a merge commit.
     * 
     * @param enabled
     *            true if enabled
     * @return a builder to continue with building
     */
    public GHCreateRepositoryBuilder allowMergeCommit(boolean enabled) {
        this.builder.with("allow_merge_commit", enabled);
        return this;
    }

    /**
     * Allow or disallow rebase-merging pull requests.
     * 
     * @param enabled
     *            true if enabled
     * @return a builder to continue with building
     */
    public GHCreateRepositoryBuilder allowRebaseMerge(boolean enabled) {
        this.builder.with("allow_rebase_merge", enabled);
        return this;
    }

    /**
     * Creates a default .gitignore
     * 
     * @param language
     *            template to base the ignore file on
     * @return a builder to continue with building
     *
     *         See https://developer.github.com/v3/repos/#create
     */
    public GHCreateRepositoryBuilder gitignoreTemplate(String language) {
        this.builder.with("gitignore_template", language);
        return this;
    }

    /**
     * Desired license template to apply
     * 
     * @param license
     *            template to base the license file on
     * @return a builder to continue with building
     *
     *         See https://developer.github.com/v3/repos/#create
     */
    public GHCreateRepositoryBuilder licenseTemplate(String license) {
        this.builder.with("license_template", license);
        return this;
    }

    /**
     * The team that gets granted access to this repository. Only valid for creating a repository in an organization.
     * 
     * @param team
     *            team to grant access to
     * @return a builder to continue with building
     */
    public GHCreateRepositoryBuilder team(GHTeam team) {
        if (team != null)
            this.builder.with("team_id", team.getId());
        return this;
    }

    /**
     * Creates a repository with all the parameters.
     * 
     * @throws IOException
     *             if repsitory cannot be created
     */
    public GHRepository create() throws IOException {
        return builder.method("POST").to(apiUrlTail, GHRepository.class).wrap(root);
    }

}
