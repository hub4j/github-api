package org.kohsuke.github;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;

import static org.kohsuke.github.Previews.SYMMETRA;
/**
 * @author Kohsuke Kawaguchi
 * @see GHIssue#getLabels()
 * @see GHRepository#listLabels()
 */
public class GHLabel {
    private String url, name, color, description;
    private GHRepository repo;

    public String getUrl() {
        return url;
    }

    public String getName() {
        return name;
    }

    /**
     * Color code without leading '#', such as 'f29513'
     */
    public String getColor() {
        return color;
    }

    /**
     * Purpose of Label
     */
    @Preview @Deprecated
    public String getDescription() {
        return description;
    }

    /*package*/ GHLabel wrapUp(GHRepository repo) {
        this.repo = repo;
        return this;
    }

    public void delete() throws IOException {
        repo.getRoot().createRequester().method("DELETE").to(url);
    }

    /**
     * @param newColor
     *      6-letter hex color code, like "f29513"
     */
    public void setColor(String newColor) throws IOException {
        repo.getRoot().createRequester().method("PATCH")
                .withPreview(SYMMETRA)
                .with("name", name)
                .with("color", newColor)
                .with("description", description)
                .to(url);
    }

    /**
     * @param newDescription
     *      Description of label
     */
    @Preview @Deprecated
    public void setDescription(String newDescription) throws IOException {
        repo.getRoot().createRequester().method("PATCH")
                .withPreview(SYMMETRA)
                .with("name", name)
                .with("color", color)
                .with("description", newDescription)
                .to(url);
    }

    /*package*/ static Collection<String> toNames(Collection<GHLabel> labels) {
        List<String> r = new ArrayList<String>();
        for (GHLabel l : labels) {
            r.add(l.getName());
        }
        return r;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        final GHLabel ghLabel = (GHLabel) o;
        return Objects.equals(url, ghLabel.url) &&
                Objects.equals(name, ghLabel.name) &&
                Objects.equals(color, ghLabel.color) &&
                Objects.equals(repo, ghLabel.repo);
    }

    @Override
    public int hashCode() {
        return Objects.hash(url, name, color, repo);
    }
}
