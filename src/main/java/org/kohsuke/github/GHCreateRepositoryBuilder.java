package org.kohsuke.github;

import java.io.IOException;

import static org.kohsuke.github.internal.Previews.BAPTISTE;

/**
 * Creates a repository
 *
 * @author Kohsuke Kawaguchi
 */
public class GHCreateRepositoryBuilder extends GHRepositoryBuilder<GHCreateRepositoryBuilder> {

    public GHCreateRepositoryBuilder(String name, GitHub root, String apiTail) {
        super(GHCreateRepositoryBuilder.class, root, null);
        requester.method("POST").withUrlPath(apiTail);

        try {
            name(name);
        } catch (IOException e) {
            // not going to happen here
        }
    }

    /**
     * Creates a default .gitignore
     *
     * @param language
     *            template to base the ignore file on
     * @return a builder to continue with building See https://developer.github.com/v3/repos/#create
     * @throws IOException
     *             In case of any networking error or error from the server.
     */
    public GHCreateRepositoryBuilder gitignoreTemplate(String language) throws IOException {
        return with("gitignore_template", language);
    }

    /**
     * Desired license template to apply
     *
     * @param license
     *            template to base the license file on
     * @return a builder to continue with building See https://developer.github.com/v3/repos/#create
     * @throws IOException
     *             In case of any networking error or error from the server.
     */
    public GHCreateRepositoryBuilder licenseTemplate(String license) throws IOException {
        return with("license_template", license);
    }

    /**
     * If true, create an initial commit with empty README.
     *
     * @param enabled
     *            true if enabled
     * @return a builder to continue with building
     * @throws IOException
     *             In case of any networking error or error from the server.
     */
    public GHCreateRepositoryBuilder autoInit(boolean enabled) throws IOException {
        return with("auto_init", enabled);
    }

    /**
     * The team that gets granted access to this repository. Only valid for creating a repository in an organization.
     *
     * @param team
     *            team to grant access to
     * @return a builder to continue with building
     * @throws IOException
     *             In case of any networking error or error from the server.
     */
    public GHCreateRepositoryBuilder team(GHTeam team) throws IOException {
        if (team != null)
            return with("team_id", team.getId());
        return this;
    }

    /**
     * Specifies whether the repository is a template.
     *
     * @param enabled
     *            true if enabled
     * @return a builder to continue with building
     * @throws IOException
     *             In case of any networking error or error from the server.
     * @deprecated Use {@link #isTemplate(boolean)} method instead
     */
    @Deprecated
    public GHCreateRepositoryBuilder templateRepository(boolean enabled) throws IOException {
        return isTemplate(enabled);
    }

    /**
     * Specifies the ownership of the repository.
     *
     * @param owner
     *            organization or personage
     * @return a builder to continue with building
     * @throws IOException
     *             In case of any networking error or error from the server.
     */
    public GHCreateRepositoryBuilder owner(String owner) throws IOException {
        return with("owner", owner);
    }

    /**
     * Create repository from template repository
     *
     * @param templateOwner
     *            template repository owner
     * @param templateRepo
     *            template repository
     * @return a builder to continue with building
     * @see <a href="https://developer.github.com/v3/previews/">GitHub API Previews</a>
     */
    @Preview(BAPTISTE)
    public GHCreateRepositoryBuilder fromTemplateRepository(String templateOwner, String templateRepo) {
        requester.withPreview(BAPTISTE).withUrlPath("/repos/" + templateOwner + "/" + templateRepo + "/generate");
        return this;
    }

    /**
     * Creates a repository with all the parameters.
     *
     * @return the gh repository
     * @throws IOException
     *             if repository cannot be created
     */
    public GHRepository create() throws IOException {
        return done();
    }
}
