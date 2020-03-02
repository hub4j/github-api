package org.kohsuke.github;

import com.fasterxml.jackson.annotation.JacksonInject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;

/**
 * The type GHLabel.
 *
 * @author Kohsuke Kawaguchi
 * @see GHIssue#getLabels() GHIssue#getLabels()
 * @see GHRepository#listLabels() GHRepository#listLabels()
 */
public class GHLabel {

    /**
     * Gets url.
     *
     * @return the url
     */
    @Deprecated
    public String getUrl() {
        return url();
    }

    /**
     * Gets name.
     *
     * @return the name
     */
    @Deprecated
    public String getName() {
        return name();
    }

    /**
     * Color code without leading '#', such as 'f29513'
     *
     * @return the color
     */
    @Deprecated
    public String getColor() {
        return color();
    }

    /**
     * Purpose of Label
     *
     * @return the description
     */
    @Deprecated
    public String getDescription() {
        return description();
    }

    /**
     * Sets color.
     *
     * @param newColor
     *            6-letter hex color code, like "f29513"
     * @throws IOException
     *             the io exception
     * @deprecated use {@link #set()} instead
     */
    @Deprecated
    public void setColor(String newColor) throws IOException {
        set().color(newColor);
    }

    /**
     * Sets description.
     *
     * @param newDescription
     *            Description of label
     * @throws IOException
     *             the io exception
     * @deprecated use {@link #set()} instead
     */
    @Deprecated
    public void setDescription(String newDescription) throws IOException {
        set().description(newDescription);
    }

    static Collection<String> toNames(Collection<GHLabel> labels) {
        List<String> r = new ArrayList<>();
        for (GHLabel l : labels) {
            r.add(l.name());
        }
        return r;
    }

    // NEW IMPLEMENTATION STARTS HERE

    @Nonnull
    private String url, name, color, description;

    @JacksonInject
    protected GitHub root;

    // Late bind
    protected GHRepository repository;

    GHLabel() {
        url = "";
        name = "";
        color = "";
        description = "";
    }

    /**
     * Creates a label in a repository.
     *
     * @throws IOException
     *             the io exception
     */
    public static Creator create(GHRepository repository) throws IOException {
        return new Creator(repository);
    }

    /**
     * Creates a label in a repository.
     *
     * @throws IOException
     *             the io exception
     */
    public static GHLabel read(@Nonnull GHRepository repository, @Nonnull String name) throws IOException {
        return repository.root.createRequest()
                .withUrlPath(repository.getApiTailUrl("labels"), name)
                .fetch(GHLabel.class)
                .lateBind(repository);

    }

    /**
     * Creates a label in a repository.
     *
     * @throws IOException
     *             the io exception
     */
    public static PagedIterable<GHLabel> readAll(@Nonnull final GHRepository repository) throws IOException {
        return repository.root.createRequest()
                .withUrlPath(repository.getApiTailUrl("labels"))
                .toIterable(GHLabel[].class, item -> item.lateBind(repository));

    }

    /**
     * Gets url.
     *
     * @return the url
     */
    public String url() {
        return url;
    }

    /**
     * Gets name.
     *
     * @return the name
     */
    public String name() {
        return name;
    }

    /**
     * Color code without leading '#', such as 'f29513'
     *
     * @return the color
     */
    public String color() {
        return color;
    }

    /**
     * Purpose of Label
     *
     * @return the description
     */
    public String description() {
        return description;
    }

    GHLabel lateBind(GHRepository repo) {
        if (repository == null) {
            repository = repo;
        }
        return this;
    }

    /**
     * Modifies a label in a repository.
     *
     * @throws IOException
     *             the io exception
     */
    public BatchUpdater update() throws IOException {
        return new BatchUpdater(this);
    }

    /**
     * Modifies a label in a repository.
     *
     * @throws IOException
     *             the io exception
     */
    public SingleUpdater set() throws IOException {
        return new SingleUpdater(this);
    }

    /**
     * Delete this label from this repository. Made static to make it clearer that this deletes the label entirely -
     * different from adding removing labels from objects.
     *
     * @throws IOException
     *             the io exception
     */
    public void delete() throws IOException {
        root.createRequest().method("DELETE").setRawUrlPath(url()).send();
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        final GHLabel ghLabel = (GHLabel) o;
        return Objects.equals(url, ghLabel.url) && Objects.equals(name, ghLabel.name)
                && Objects.equals(color, ghLabel.color) && Objects.equals(repository, ghLabel.repository);
    }

    @Override
    public int hashCode() {
        return Objects.hash(url, name, color, repository);
    }

    public static class SingleUpdater extends Builder<GHLabel> {
        private SingleUpdater(@Nonnull GHLabel base) throws IOException {
            super(GHLabel.class, base);
            requester.method("PATCH").setRawUrlPath(base.url());
        }
    }

    public static class BatchUpdater extends Builder<BatchUpdater> {
        private BatchUpdater(@Nonnull GHLabel base) throws IOException {
            super(BatchUpdater.class, base);
            requester.method("PATCH").setRawUrlPath(base.url());
        }
    }

    public static class Creator extends Builder<Creator> {
        private Creator(@Nonnull GHRepository repository) throws IOException {
            super(Creator.class, repository);
            requester.method("POST").withUrlPath(repository.getApiTailUrl("labels"));
        }
    }

    public static class Builder<U> extends BaseBuilder<GHLabel, U> {

        final GHRepository repository;

        public Builder(@Nonnull Class<U> builderType, @Nonnull GHLabel label) throws IOException {
            super(label.root, builderType, GHLabel.class, label);
            repository = label.repository;
        }

        public Builder(@Nonnull Class<U> builderType, @Nonnull GHRepository repository) throws IOException {
            super(repository.root, builderType, GHLabel.class, new GHLabel());
            this.repository = repository;
        }

        public U name(String value) throws IOException {
            return with("name", value);
        }

        public U color(String value) throws IOException {
            return with("color", value);
        }

        public U description(String value) throws IOException {
            return with("description", value);
        }

        @Override
        protected void initialize(GHLabel base) throws IOException {
            // Set initial values
            name(base.name());
            color(base.color());
            description(base.description());
        }

        @Override
        public GHLabel done() throws IOException {
            return super.done().lateBind(repository);
        }
    }

    /**
     *
     * @param <T>
     * @param <U>
     */
    public abstract static class BaseBuilder<T, U> {

        private final boolean initialized;
        private final boolean immediate;

        // TODO: Not sure how update-in-place behavior should be controlled, but
        // it certainly can be controlled dynamically down to the instance level or inherited for all children of some
        // connection.
        protected boolean updateInPlace;
        protected final Class<T> returnType;
        protected final Requester requester;

        @CheckForNull
        protected final T baseInstance;

        protected BaseBuilder(@Nonnull GitHub root,
                @Nonnull Class<U> builderType,
                @Nonnull Class<T> returnType,
                @CheckForNull T baseInstance) throws IOException {
            this.requester = root.createRequest();
            this.immediate = returnType.equals(builderType);
            this.returnType = returnType;
            this.baseInstance = baseInstance;
            this.updateInPlace = false;
            if (baseInstance != null) {
                initialize(baseInstance);
            }
            this.initialized = true;
        }

        /**
         * Finishes an update, committing changes.
         *
         * This method may update-in-place or not. Either way it returns the resulting instance.
         *
         * @return an instance with updated current data
         * @throws IOException
         *             if there is an I/O Exception
         */
        public T done() throws IOException {
            T result;
            if (updateInPlace && baseInstance != null) {
                result = requester.fetchInto(baseInstance);
            } else {
                result = requester.fetch(returnType);
            }
            return result;
        };

        protected abstract void initialize(T base) throws IOException;

        /**
         * Applies a value to a name for this builder.
         *
         * The internals of this method look terrifying, but they they're actually basically safe due to previous
         * comparison of U and T determined by comparing class instances passed in during construction.
         *
         * If U is the same as T, this cause the builder to commit changes after the first value change and return a T
         * from done().
         *
         * If U is not the same as T, the builder will batch together multiple changes and let the user call done() when
         * they are ready.
         *
         * This little bit of roughness in this base class means all inheriting builders get to create BatchUpdater and
         * SingleUpdater classes from almost identical code. Creator can be implemented with significant code reuse as
         * well.
         *
         * There is probably a cleaner way to implement this, but I'm not sure what it is right now.
         *
         * @param name
         * @param value
         * @return
         * @throws IOException
         */
        protected U with(String name, Object value) throws IOException {
            requester.with(name, value);
            if (initialized) {
                if (immediate) {
                    return (U) done();
                }
                return (U) this;
            }
            return null;
        }
    }
}
