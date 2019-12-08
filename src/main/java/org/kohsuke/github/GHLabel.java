package org.kohsuke.github;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;

import static org.kohsuke.github.Previews.SYMMETRA;

/**
 * The type GHLabel.
 *
 * @author Kohsuke Kawaguchi
 * @see GHIssue#getLabels() GHIssue#getLabels()
 * @see GHRepository#listLabels() GHRepository#listLabels()
 */
public class GHLabel {
    private String url, name, color, description;
    private GHRepository repo;

    /**
     * Gets url.
     *
     * @return the url
     */
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
     * Color code without leading '#', such as 'f29513'
     *
     * @return the color
     */
    public String getColor() {
        return color;
    }

    /**
     * Purpose of Label
     *
     * @return the description
     */
    @Preview
    @Deprecated
    public String getDescription() {
        return description;
    }

    GHLabel wrapUp(GHRepository repo) {
        this.repo = repo;
        return this;
    }

    /**
     * Delete.
     *
     * @throws IOException
     *             the io exception
     */
    public void delete() throws IOException {
        repo.root.retrieve().method("DELETE").to(url);
    }

    /**
     * Sets color.
     *
     * @param newColor
     *            6-letter hex color code, like "f29513"
     * @throws IOException
     *             the io exception
     */
    public void setColor(String newColor) throws IOException {
        repo.root.retrieve()
                .method("PATCH")
                .withPreview(SYMMETRA)
                .with("name", name)
                .with("color", newColor)
                .with("description", description)
                .to(url);
    }

    /**
     * Sets description.
     *
     * @param newDescription
     *            Description of label
     * @throws IOException
     *             the io exception
     */
    @Preview
    @Deprecated
    public void setDescription(String newDescription) throws IOException {
        repo.root.retrieve()
                .method("PATCH")
                .withPreview(SYMMETRA)
                .with("name", name)
                .with("color", color)
                .with("description", newDescription)
                .to(url);
    }

    static Collection<String> toNames(Collection<GHLabel> labels) {
        List<String> r = new ArrayList<String>();
        for (GHLabel l : labels) {
            r.add(l.getName());
        }
        return r;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        final GHLabel ghLabel = (GHLabel) o;
        return Objects.equals(url, ghLabel.url) && Objects.equals(name, ghLabel.name)
                && Objects.equals(color, ghLabel.color) && Objects.equals(repo, ghLabel.repo);
    }

    @Override
    public int hashCode() {
        return Objects.hash(url, name, color, repo);
    }
}
