package org.kohsuke.github;

import com.fasterxml.jackson.annotation.JacksonInject;
import com.fasterxml.jackson.annotation.JsonCreator;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;

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
        return this.url();
    }

    /**
     * Gets name.
     *
     * @return the name
     */
    @Deprecated
    public String getName() {
        return this.name();
    }

    /**
     * Color code without leading '#', such as 'f29513'
     *
     * @return the color
     */
    @Deprecated
    public String getColor() {
        return this.color();
    }

    /**
     * Purpose of Label
     *
     * @return the description
     */
    @Deprecated
    public String getDescription() {
        return this.description();
    }

    /**
     * Sets color.
     *
     * @param newColor
     *            6-letter hex color code, like "f29513"
     * @throws IOException
     *             the io exception
     * @deprecated use {@link #update(Consumer)} instead
     */
    @Deprecated
    public void setColor(String newColor) throws IOException {
        this.update(i -> i.color(newColor));
    }

    /**
     * Sets description.
     *
     * @param newDescription
     *            Description of label
     * @throws IOException
     *             the io exception
     * @deprecated use {@link #update(Consumer)} instead
     */
    @Deprecated
    public void setDescription(String newDescription) throws IOException {
        this.update(i -> i.description(newDescription));
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
    private final GitHub root;

    @Nonnull
    private final String url, name, color, description;

    private GHRepository repository;

    @JsonCreator(mode = JsonCreator.Mode.DELEGATING)
    private GHLabel(@Nonnull Builder builder) {
        this.root = builder.root;
        this.url = builder.url;
        this.name = builder.name;
        this.color = builder.color;
        this.description = builder.description;
    }

    /**
     * Creates a label in a repository.
     *
     * @throws IOException
     *             the io exception
     */
    public static GHLabel create(GHRepository repository, Consumer<Builder> initializer) throws IOException {
        Builder builder = new Builder();
        initializer.accept(builder);
        return repository.root.createRequest()
                .withUrlPath(repository.getApiTailUrl("labels"))
                .method("POST")
                .with("name", builder.name)
                .with("color", builder.color)
                .with("description", builder.description)
                .fetch(GHLabel.class)
                .lateBind(repository);

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
        return this.url;
    }

    /**
     * Gets name.
     *
     * @return the name
     */
    public String name() {
        return this.name;
    }

    /**
     * Color code without leading '#', such as 'f29513'
     *
     * @return the color
     */
    public String color() {
        return this.color;
    }

    /**
     * Purpose of Label
     *
     * @return the description
     */
    public String description() {
        return this.description;
    }

    GHLabel lateBind(GHRepository repo) {
        if (repository == null) {
            this.repository = repo;
        }
        return this;
    }

    /**
     * Modifies a label in a repository.
     *
     * @throws IOException
     *             the io exception
     */
    public GHLabel update(Consumer<Builder> updater) throws IOException {
        Builder builder = new Builder(this);
        updater.accept(builder);

        return repository.root.createRequest()
                .method("PATCH")
                .with("name", builder.name)
                .with("color", builder.color)
                .with("description", builder.description)
                .setRawUrlPath(url)
                .fetch(GHLabel.class)
                .lateBind(repository);
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

    public static class Builder {
        private String url, name, color, description;

        @JacksonInject
        private GitHub root;

        public Builder() {
            url = "";
            name = "";
            color = "";
            description = "";
        }

        public Builder(GHLabel label) {
            this.root = label.root;
            // Url is maintained on the mutator but cannot be changed locally.
            url = label.url();
            name = label.name();
            color = label.color();
            description = label.description();
        }

        public Builder name(String value) {
            name = value;
            return this;
        }

        public Builder color(String value) {
            color = value;
            return this;
        }

        public Builder description(String value) {
            description = value;
            return this;
        }
    }
}
