package org.kohsuke.github;

import java.net.URL;
import java.util.Date;

/**
 * Most (all?) domain objects in GitHub seems to have these 4 properties.
 */
public class Identifiable {
    protected String url;
    protected int id;
    protected String created_at;
    protected String updated_at;

    /**
     * When was this resource created?
     */
    public Date getCreatedAt() {
        return GitHub.parseDate(created_at);
    }

    /**
     * API URL of this object.
     */
    public URL getUrl() {
        return GitHub.parseURL(url);
    }

    /**
     * When was this resource last updated?
     */
    public Date getUpdatedAt() {
        return GitHub.parseDate(updated_at);
    }

    /**
     * Unique ID number of this resource.
     */
    public int getId() {
        return id;
    }
}
