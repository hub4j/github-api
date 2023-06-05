package org.kohsuke.github;

import java.io.IOException;
import java.util.Objects;

import javax.annotation.Nonnull;

/**
 * The type Gh repository variable.
 *
 * @author garridobarrera
 */
public class GHRepositoryVariable extends GitHubInteractiveObject {

    private static final String SLASH = "/";

    private static final String VARIABLE_NAMESPACE = "actions/variables";

    private String name;
    private String value;

    private String url;
    private String createdAt;
    private String updatedAt;

    /**
     * Gets url.
     *
     * @return the url
     */
    @Nonnull
    public String getUrl() {
        return url;
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
     * Sets name.
     *
     * @param name
     *            the name
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Gets value.
     *
     * @return the value
     */
    public String getValue() {
        return value;
    }

    /**
     * Sets value.
     *
     * @param value
     *            the value
     */
    public void setValue(String value) {
        this.value = value;
    }

    /**
     * Gets the api root.
     *
     * @return the api root
     */
    @Nonnull
    GitHub getApiRoot() {
        return Objects.requireNonNull(root());
    }

    /**
     * Reads a variable from a repository.
     *
     * @param repository
     *            the repository to read from
     * @param name
     *            the name of the variable
     * @return a variable
     * @throws IOException
     *             the io exception
     */
    static GHRepositoryVariable read(@Nonnull GHRepository repository, @Nonnull String name) throws IOException {
        GHRepositoryVariable variable = repository.root()
                .createRequest()
                .withUrlPath(repository.getApiTailUrl(VARIABLE_NAMESPACE), name)
                .fetch(GHRepositoryVariable.class);
        variable.url = repository.getApiTailUrl("actions/variables");
        return variable;
    }

    /**
     * Begins the creation of a new instance.
     * <p>
     * Consumer must call {@link GHRepositoryVariable.Creator#done()} to commit changes.
     *
     * @param repository
     *            the repository in which the variable will be created.
     * @return a {@link GHRepositoryVariable.Creator}
     * @throws IOException
     *             the io exception
     */
    @BetaApi
    static GHRepositoryVariable.Creator create(GHRepository repository) throws IOException {
        return new GHRepositoryVariable.Creator(repository);
    }

    /**
     * Delete this variable from the repository.
     *
     * @throws IOException
     *             the io exception
     */
    public void delete() throws IOException {
        root().createRequest().method("DELETE").withUrlPath(getUrl().concat(SLASH).concat(name)).send();
    }

    /**
     * Begins a single property update.
     *
     * @return a {@link GHRepositoryVariable.Setter}
     */
    @BetaApi
    public GHRepositoryVariable.Setter set() {
        return new GHRepositoryVariable.Setter(this);
    }

    /**
     * A {@link GHRepositoryVariableBuilder} that updates a single property per request
     * <p>
     * {@link #done()} is called automatically after the property is set.
     */
    @BetaApi
    public static class Setter extends GHRepositoryVariableBuilder<GHRepositoryVariable> {
        private Setter(@Nonnull GHRepositoryVariable base) {
            super(GHRepositoryVariable.class, base.getApiRoot(), base);
            requester.method("PATCH").withUrlPath(base.getUrl().concat(SLASH).concat(base.getName()));
        }
    }

    /**
     * Begins a batch update
     * <p>
     * Consumer must call {@link GHRepositoryVariable.Updater#done()} to commit changes.
     *
     * @return a {@link GHRepositoryVariable.Updater}
     * @throws IOException
     *             the io exception
     */
    @BetaApi
    public GHRepositoryVariable.Updater update() throws IOException {
        return new GHRepositoryVariable.Updater(this);
    }

    /**
     * A {@link GHRepositoryVariableBuilder} that creates a new {@link GHRepositoryVariable}
     * <p>
     * Consumer must call {@link #done()} to create the new instance.
     */
    @BetaApi
    public static class Creator extends GHRepositoryVariableBuilder<Creator> {
        private Creator(@Nonnull GHRepository repository) {
            super(GHRepositoryVariable.Creator.class, repository.root(), null);
            requester.method("POST").withUrlPath(repository.getApiTailUrl(VARIABLE_NAMESPACE));
        }
    }

    /**
     * A {@link GHRepositoryVariableBuilder} that allows multiple properties to be updated per request.
     * <p>
     * Consumer must call {@link #done()} to commit changes.
     */
    @BetaApi
    public static class Updater extends GHRepositoryVariableBuilder<Updater> {
        private Updater(@Nonnull GHRepositoryVariable base) {
            super(GHRepositoryVariable.Updater.class, base.getApiRoot(), base);
            requester.method("PATCH").withUrlPath(base.getUrl());
        }
    }
}
