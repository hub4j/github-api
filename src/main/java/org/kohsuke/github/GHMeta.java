package org.kohsuke.github;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Class that wraps the list of GitHub's IP addresses.
 *
 * @see GitHub#getMeta() GitHub#getMeta()
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

    /**
     * Is verifiable password authentication boolean.
     *
     * @return the boolean
     */
    public boolean isVerifiablePasswordAuthentication() {
        return verifiablePasswordAuthentication;
    }

    /**
     * Gets hooks.
     *
     * @return the hooks
     */
    public List<String> getHooks() {
        return Collections.unmodifiableList(hooks);
    }

    /**
     * Gets git.
     *
     * @return the git
     */
    public List<String> getGit() {
        return Collections.unmodifiableList(git);
    }

    /**
     * Gets web.
     *
     * @return the web
     */
    public List<String> getWeb() {
        return Collections.unmodifiableList(web);
    }

    /**
     * Gets api.
     *
     * @return the api
     */
    public List<String> getApi() {
        return Collections.unmodifiableList(api);
    }

    /**
     * Gets pages.
     *
     * @return the pages
     */
    public List<String> getPages() {
        return Collections.unmodifiableList(pages);
    }

    /**
     * Gets importer.
     *
     * @return the importer
     */
    public List<String> getImporter() {
        return Collections.unmodifiableList(importer);
    }
}
