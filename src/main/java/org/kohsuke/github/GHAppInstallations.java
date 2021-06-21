package org.kohsuke.github;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public class GHAppInstallations {

    @JsonProperty("total_count")
    private int totalCount;

    private List<GHAppInstallation> installations;

    public int getTotalCount() {
        return this.totalCount;
    }

    public List<GHAppInstallation> getInstallations() {
        return this.installations;
    }
}
