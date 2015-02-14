package org.kohsuke.github;

import java.net.URL;
import java.util.Date;

public class Identifiable {
    protected String url;
    protected int id;
    protected String created_at;
    protected String updated_at;

    public Date getCreatedAt() {
        return GitHub.parseDate(created_at);
    }

    public URL getUrl() {
        return GitHub.parseURL(url);
    }

    public Date getUpdatedAt() {
        return GitHub.parseDate(updated_at);
    }

    public int getId() {
        return id;
    }
}
