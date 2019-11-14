package org.kohsuke.github;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

/**
 * Class that wraps the list of GitHub's IP addresses.
 *
 * @author Paulo Miguel Almeida
 *
 * @see GitHub#getMeta()
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

    public void setVerifiablePasswordAuthentication(boolean verifiablePasswordAuthentication) {
        this.verifiablePasswordAuthentication = verifiablePasswordAuthentication;
    }

    public List<String> getHooks() {
        return hooks;
    }

    public void setHooks(List<String> hooks) {
        this.hooks = hooks;
    }

    public List<String> getGit() {
        return git;
    }

    public void setGit(List<String> git) {
        this.git = git;
    }

    public List<String> getWeb() {
        return web;
    }

    public void setWeb(List<String> web) {
        this.web = web;
    }

    public List<String> getApi() {
        return api;
    }

    public void setApi(List<String> api) {
        this.api = api;
    }

    public List<String> getPages() {
        return pages;
    }

    public void setPages(List<String> pages) {
        this.pages = pages;
    }

    public List<String> getImporter() {
        return importer;
    }

    public void setImporter(List<String> importer) {
        this.importer = importer;
    }

}
