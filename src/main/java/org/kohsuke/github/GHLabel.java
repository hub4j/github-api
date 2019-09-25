package org.kohsuke.github;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;

/**
 * @author Kohsuke Kawaguchi
 * @see GHIssue#getLabels()
 * @see GHRepository#listLabels()
 */
public class GHLabel {
    private String url, name, color;
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

    /*package*/ GHLabel wrapUp(GHRepository repo) {
        this.repo = repo;
        return this;
    }

    public void delete() throws IOException {
        repo.root.retrieve().method("DELETE").to(url);
    }

    /**
     * @param newColor
     *      6-letter hex color code, like "f29513"
     */
    public void setColor(String newColor) throws IOException {
        repo.root.retrieve().method("PATCH").with("name", name).with("color", newColor).to(url);
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
