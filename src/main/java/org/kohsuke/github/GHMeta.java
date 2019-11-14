package org.kohsuke.github;

import com.fasterxml.jackson.annotation.JsonProperty;

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
    private List<String> importer;

    public boolean isVerifiablePasswordAuthentication() {
        return verifiablePasswordAuthentication;
    }

    void setVerifiablePasswordAuthentication(boolean verifiablePasswordAuthentication) {
        this.verifiablePasswordAuthentication = verifiablePasswordAuthentication;
    }

    public List<String> getHooks() {
        return hooks;
    }

    void setHooks(List<String> hooks) {
        this.hooks = hooks;
    }

    public List<String> getGit() {
        return git;
    }

    void setGit(List<String> git) {
        this.git = git;
    }

    public List<String> getWeb() {
        return web;
    }

    void setWeb(List<String> web) {
        this.web = web;
    }

    public List<String> getApi() {
        return api;
    }

    void setApi(List<String> api) {
        this.api = api;
    }

    public List<String> getPages() {
        return pages;
    }

    void setPages(List<String> pages) {
        this.pages = pages;
    }

    public List<String> getImporter() {
        return importer;
    }

    void setImporter(List<String> importer) {
        this.importer = importer;
    }

}
