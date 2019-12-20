package org.kohsuke.github;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;

/**
 * The type GHLabel.
 *
 * @author Kohsuke Kawaguchi
 * @see GHIssue#getLabels() GHIssue#getLabels()
 * @see GHRepository#listLabels() GHRepository#listLabels()
 */
public class GHLabel extends GHObject {
    private String url, name, color, description;
    private GHRepository repo;

    /**
     * Gets url.
     *
     * @return the url
     */
    public URL getUrl() {
        try {
            return new URL(url);
        } catch (MalformedURLException e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public URL getHtmlUrl() throws IOException {
        return null;
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
        repo.root.createRequest().method("DELETE").setRawUrlPath(url).send();
    }

    /**
     * Updates an existing github label
     *
     * @param name
     *            the name of the label
     * @param color
     *            the color
     * @param description
     *            the description
     * @return gh label
     * @throws IOException
     *             the io exception
     */
    public GHLabel update(String name, String color, String description) throws IOException {
        return repo.root.createRequest()
                .method("PATCH")
                .with("new_name", name)
                .with("color", color)
                .with("description", description)
                .setRawUrlPath(url)
                .fetchInto(this)
                .wrapUp(this.repo);
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
        repo.root.createRequest()
                .method("PATCH")
                .with("name", name)
                .with("color", newColor)
                .with("description", description)
                .setRawUrlPath(url)
                .send();
    }

    /**
     * Sets description.
     *
     * @param newDescription
     *            Description of label
     * @throws IOException
     *             the io exception
     */
    public void setDescription(String newDescription) throws IOException {
        repo.root.createRequest()
                .method("PATCH")
                .with("name", name)
                .with("color", color)
                .with("description", newDescription)
                .setRawUrlPath(url)
                .send();
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
