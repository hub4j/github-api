package org.kohsuke.github;

import javax.annotation.Nonnull;
import java.io.IOException;

public class GHEnvironmentVariable extends GitHubInteractiveObject {

    private String name;
    private String value;
    private String createdAt;
    private String updatedAt;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }

    public String getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(String updatedAt) {
        this.updatedAt = updatedAt;
    }

    static GHEnvironmentVariable read(@Nonnull GHRepository repository, @Nonnull String environment, @Nonnull String name) throws IOException {
        String url = "environments/" + environment + "/variables/" + name;
        GHEnvironmentVariable variable = repository.root()
                .createRequest()
                .withUrlPath(repository.getApiTailUrl(url))
                .fetch(GHEnvironmentVariable.class);
        return variable;
    }

}
