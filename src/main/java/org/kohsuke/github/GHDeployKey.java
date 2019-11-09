package org.kohsuke.github;

import com.fasterxml.jackson.annotation.JacksonInject;
import org.apache.commons.lang3.builder.ToStringBuilder;

import java.io.IOException;

public class GHDeployKey {

    protected String url, key, title;
    protected boolean verified;
    protected long id;

    @JacksonInject(value = "org.kohsuke.github.GHRepository")
    private GHRepository owner;

    public long getId() {
        return id;
    }

    public String getKey() {
        return key;
    }

    public String getTitle() {
        return title;
    }

    public String getUrl() {
        return url;
    }

    public boolean isVerified() {
        return verified;
    }

    public String toString() {
        return new ToStringBuilder(this).append("title",title).append("id",id).append("key",key).toString();
    }
    
    public void delete() throws IOException {
        owner.createRequest().method("DELETE").to(String.format("/repos/%s/%s/keys/%d", owner.getOwnerName(), owner.getName(), id));
    }
}
