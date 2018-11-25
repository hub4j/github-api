package org.kohsuke.github;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

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
    public String getDescription() {
        return description;
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
        repo.root.retrieve().method("PATCH")
                .with("name", name)
                .with("color", newColor)
                .with("description", description)
                .to(url);
    }

    /**
     * @param newDescription
     *      Description of label
     */
    public void setDescription(String newDescription) throws IOException {
        repo.root.retrieve().method("PATCH")
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
}
