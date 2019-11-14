package org.kohsuke.github;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Class that wraps the list of GitHub's IP addresses.
 *
 * @author Paulo Miguel Almeida
 *
 * @see GitHub#getMeta()
 * @see <a href="https://developer.github.com/v3/meta/#meta">Get Meta</a>
 */

public class GHMeta {

    @JsonProperty("verifiable_password_authentication")
    private boolean verifiablePasswordAuthentication;
    private List<String> hooks;
    private List<String> git;
    private List<String> web;
    private List<String> api;
    private List<String> pages;
    private List<String> importer = new ArrayList<>();

    public boolean isVerifiablePasswordAuthentication() {
        return verifiablePasswordAuthentication;
    }

    public List<String> getHooks() {
        return Collections.unmodifiableList(hooks);
    }

    public List<String> getGit() {
        return Collections.unmodifiableList(git);
    }

    public List<String> getWeb() {
        return Collections.unmodifiableList(web);
    }

    public List<String> getApi() {
        return Collections.unmodifiableList(api);
    }

    public List<String> getPages() {
        return Collections.unmodifiableList(pages);
    }

    public List<String> getImporter() {
        return Collections.unmodifiableList(importer);
    }
}
