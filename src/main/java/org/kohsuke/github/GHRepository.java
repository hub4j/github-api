/*
 * The MIT License
 *
 * Copyright (c) 2010, Kohsuke Kawaguchi
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package org.kohsuke.github;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.infradna.tool.bridge_method_injector.WithBridgeMethods;
import java.io.FileNotFoundException;

import javax.xml.bind.DatatypeConverter;
import java.io.IOException;
import java.io.InterruptedIOException;
import java.net.URL;
import java.util.AbstractSet;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import static java.util.Arrays.*;

/**
 * A repository on GitHub.
 * 
 * @author Kohsuke Kawaguchi
 */
@SuppressWarnings({"UnusedDeclaration"})
public class GHRepository {
    /*package almost final*/ GitHub root;

    private String description, homepage, name;
    private String url; // this is the API url
    private String html_url;    // this is the UI
    private String git_url, ssh_url, clone_url, svn_url;
    private GHUser owner;   // not fully populated. beware.
    private boolean has_issues, has_wiki, fork, has_downloads;
    @JsonProperty("private")
    private boolean _private;
    private int watchers,forks,open_issues,size,network_count,subscribers_count;
    private String created_at, pushed_at;
    private Map<Integer,GHMilestone> milestones = new HashMap<Integer, GHMilestone>();
    
    private String default_branch,language;
    private Map<String,GHCommit> commits = new HashMap<String, GHCommit>();

    private GHRepoPermission permissions;

    private static class GHRepoPermission {
        boolean pull,push,admin;
    }


    public String getDescription() {
        return description;
    }

    public String getHomepage() {
        return homepage;
    }

    /**
     * URL of this repository, like 'http://github.com/kohsuke/jenkins'
     */
    public String getUrl() {
        return html_url;
    }

    /**
     * Gets the git:// URL to this repository, such as "git://github.com/kohsuke/jenkins.git"
     * This URL is read-only.
     */
    public String getGitTransportUrl() {
        return git_url;
    }

    /**
     * Gets the HTTPS URL to this repository, such as "https://github.com/kohsuke/jenkins.git"
     * This URL is read-only.
     */
    public String gitHttpTransportUrl() {
        return clone_url;
    }

    /**
     * Gets the Subversion URL to access this repository: https://github.com/rails/rails
     */
    public String getSvnUrl() {
        return svn_url;
    }

    /**
     * Gets the SSH URL to access this repository, such as git@github.com:rails/rails.git
     */
    public String getSshUrl() {
        return ssh_url;
    }

    /**
     * Short repository name without the owner. For example 'jenkins' in case of http://github.com/jenkinsci/jenkins
     */
    public String getName() {
        return name;
    }

    public boolean hasPullAccess() {
        return permissions!=null && permissions.pull;
    }

    public boolean hasPushAccess() {
        return permissions!=null && permissions.push;
    }

    public boolean hasAdminAccess() {
        return permissions!=null && permissions.admin;
    }

    /**
     * Gets the primary programming language.
     */
    public String getLanguage() {
        return language;
    }

    public GHUser getOwner() throws IOException {
        return root.getUser(owner.login);   // because 'owner' isn't fully populated
    }

    public GHIssue getIssue(int id) throws IOException {
        return root.retrieve().to("/repos/" + owner.login + "/" + name + "/issues/" + id, GHIssue.class).wrap(this);
    }

    public GHIssueBuilder createIssue(String title) {
        return new GHIssueBuilder(this,title);
    }

    public List<GHIssue> getIssues(GHIssueState state) throws IOException {
        return listIssues(state).asList();
    }

    public List<GHIssue> getIssues(GHIssueState state, GHMilestone milestone) throws IOException {
        return Arrays.asList(GHIssue.wrap(root.retrieve()
                .to(String.format("/repos/%s/%s/issues?state=%s&milestone=%s", owner.login, name,
                        state.toString().toLowerCase(), milestone == null ? "none" : "" + milestone.getNumber()),
                        GHIssue[].class
                ), this));
    }

    /**
     * Lists up all the issues in this repository.
     */
    public PagedIterable<GHIssue> listIssues(final GHIssueState state) {
        return new PagedIterable<GHIssue>() {
            public PagedIterator<GHIssue> iterator() {
                return new PagedIterator<GHIssue>(root.retrieve().asIterator(getApiTailUrl("issues?state="+state.toString().toLowerCase(Locale.ENGLISH)), GHIssue[].class)) {
                    @Override
                    protected void wrapUp(GHIssue[] page) {
                        for (GHIssue c : page)
                            c.wrap(GHRepository.this);
                    }
                };
            }
        };
    }

    public GHReleaseBuilder createRelease(String tag) {
        return new GHReleaseBuilder(this,tag);
    }

    /**
     * Creates a named ref, such as tag, branch, etc.
     *
     * @param name
     *      The name of the fully qualified reference (ie: refs/heads/master).
     *      If it doesn't start with 'refs' and have at least two slashes, it will be rejected.
     * @param sha
     *      The SHA1 value to set this reference to
     */
    public GHRef createRef(String name, String sha) throws IOException {
        return new Requester(root)
                .with("ref", name).with("sha", sha).method("POST").to(getApiTailUrl("git/refs"), GHRef.class).wrap(root);
    }

    /**
     * @deprecated
     *      use {@link #listReleases()}
     */
    public List<GHRelease> getReleases() throws IOException {
        return listReleases().asList();
    }

    public PagedIterable<GHRelease> listReleases() throws IOException {
        return new PagedIterable<GHRelease>() {
            public PagedIterator<GHRelease> iterator() {
                return new PagedIterator<GHRelease>(root.retrieve().asIterator(getApiTailUrl("releases"), GHRelease[].class)) {
                    @Override
                    protected void wrapUp(GHRelease[] page) {
                        for (GHRelease c : page)
                            c.wrap(GHRepository.this);
                    }
                };
            }
        };
    }

    public PagedIterable<GHTag> listTags() throws IOException {
        return new PagedIterable<GHTag>() {
            public PagedIterator<GHTag> iterator() {
                return new PagedIterator<GHTag>(root.retrieve().asIterator(getApiTailUrl("tags"), GHTag[].class)) {
                    @Override
                    protected void wrapUp(GHTag[] page) {
                        for (GHTag c : page)
                            c.wrap(GHRepository.this);
                    }
                };
            }
        };
    }

    protected String getOwnerName() {
        return owner.login;
    }

    public boolean hasIssues() {
        return has_issues;
    }

    public boolean hasWiki() {
        return has_wiki;
    }

    public boolean isFork() {
        return fork;
    }

    public int getForks() {
        return forks;
    }

    public boolean isPrivate() {
        return _private;
    }

    public boolean hasDownloads() {
        return has_downloads;
    }

    public int getWatchers() {
        return watchers;
    }

    public int getOpenIssueCount() {
        return open_issues;
    }

    public int getNetworkCount() {
        return network_count;
    }

    public int getSubscribersCount() {
        return subscribers_count;
    }

    /**
     *
     * @return
     *      null if the repository was never pushed at.
     */
    public Date getPushedAt() {
        return GitHub.parseDate(pushed_at);
    }

    public Date getCreatedAt() {
        return GitHub.parseDate(created_at);
    }

    /**
     * Returns the primary branch you'll configure in the "Admin > Options" config page.
     *
     * @return
     *      This field is null until the user explicitly configures the master branch.
     */
    public String getMasterBranch() {
        return default_branch;
    }

    public int getSize() {
        return size;
    }

    /**
     * Gets the collaborators on this repository.
     * This set always appear to include the owner.
     */
    @WithBridgeMethods(Set.class)
    public GHPersonSet<GHUser> getCollaborators() throws IOException {
        return new GHPersonSet<GHUser>(listCollaborators().asList());
    }

    /**
     * Lists up the collaborators on this repository.
     *
     * @return Users
     * @throws IOException
     */
    public PagedIterable<GHUser> listCollaborators() throws IOException {
        return new PagedIterable<GHUser>() {
            public PagedIterator<GHUser> iterator() {

                return new PagedIterator<GHUser>(root.retrieve().asIterator("/repos/" + owner.login + "/" + name + "/collaborators", GHUser[].class)) {

                    @Override
                    protected void wrapUp(GHUser[] users) {
                        for (GHUser user : users) {
                            user.wrapUp(root);
                        }
                    }
                };

            }
        };

    }

    /**
     * Gets the names of the collaborators on this repository.
     * This method deviates from the principle of this library but it works a lot faster than {@link #getCollaborators()}.
     */
    public Set<String> getCollaboratorNames() throws IOException {
        Set<String> r = new HashSet<String>();
        for (GHUser u : GHUser.wrap(root.retrieve().to("/repos/" + owner.login + "/" + name + "/collaborators", GHUser[].class),root))
            r.add(u.login);
        return r;
    }

    /**
     * If this repository belongs to an organization, return a set of teams.
     */
    public Set<GHTeam> getTeams() throws IOException {
        return Collections.unmodifiableSet(new HashSet<GHTeam>(Arrays.asList(GHTeam.wrapUp(root.retrieve().to("/repos/" + owner.login + "/" + name + "/teams", GHTeam[].class), root.getOrganization(owner.login)))));
    }

    public void addCollaborators(GHUser... users) throws IOException {
        addCollaborators(asList(users));
    }

    public void addCollaborators(Collection<GHUser> users) throws IOException {
        modifyCollaborators(users, "PUT");
    }

    public void removeCollaborators(GHUser... users) throws IOException {
        removeCollaborators(asList(users));
    }

    public void removeCollaborators(Collection<GHUser> users) throws IOException {
        modifyCollaborators(users, "DELETE");
    }

    private void modifyCollaborators(Collection<GHUser> users, String method) throws IOException {
        verifyMine();
        for (GHUser user : users) {
            new Requester(root).method(method).to("/repos/" + owner.login + "/" + name + "/collaborators/" + user.getLogin());
        }
    }

    public void setEmailServiceHook(String address) throws IOException {
        Map<String, String> config = new HashMap<String, String>();
        config.put("address", address);
        new Requester(root).method("POST").with("name", "email").with("config", config).with("active", "true")
                .to(String.format("/repos/%s/%s/hooks", owner.login, name));
    }

    private void edit(String key, String value) throws IOException {
        Requester requester = new Requester(root);
        if (!key.equals("name"))
            requester.with("name", name);   // even when we don't change the name, we need to send it in
        requester.with(key, value).method("PATCH").to("/repos/" + owner.login + "/" + name);
    }

    /**
     * Enables or disables the issue tracker for this repository.
     */
    public void enableIssueTracker(boolean v) throws IOException {
        edit("has_issues", String.valueOf(v));
    }

    /**
     * Enables or disables Wiki for this repository.
     */
    public void enableWiki(boolean v) throws IOException {
        edit("has_wiki", String.valueOf(v));
    }

    public void enableDownloads(boolean v) throws IOException {
        edit("has_downloads",String.valueOf(v));
    }

    /**
     * Rename this repository.
     */
    public void renameTo(String name) throws IOException {
        edit("name",name);
    }

    public void setDescription(String value) throws IOException {
        edit("description",value);
    }

    public void setHomepage(String value) throws IOException {
        edit("homepage",value);
    }

    /**
     * Deletes this repository.
     */
    public void delete() throws IOException {
        try {
            new Requester(root).method("DELETE").to("/repos/" + owner.login + "/" + name);
        } catch (FileNotFoundException x) {
            throw (FileNotFoundException) new FileNotFoundException("Failed to delete " + owner.login + "/" + name + "; might not exist, or you might need the delete_repo scope in your token: http://stackoverflow.com/a/19327004/12916").initCause(x);
        }
    }

    /**
     * Forks this repository as your repository.
     *
     * @return
     *      Newly forked repository that belong to you.
     */
    public GHRepository fork() throws IOException {
        return new Requester(root).method("POST").to("/repos/" + owner.login + "/" + name + "/forks", GHRepository.class).wrap(root);
    }

    /**
     * Forks this repository into an organization.
     *
     * @return
     *      Newly forked repository that belong to you.
     */
    public GHRepository forkTo(GHOrganization org) throws IOException {
        new Requester(root).to(String.format("/repos/%s/%s/forks?org=%s", owner.login, name, org.getLogin()));

        // this API is asynchronous. we need to wait for a bit
        for (int i=0; i<10; i++) {
            GHRepository r = org.getRepository(name);
            if (r!=null)    return r;
            try {
                Thread.sleep(3000);
            } catch (InterruptedException e) {
                throw (IOException)new InterruptedIOException().initCause(e);
            }
        }
        throw new IOException(this+" was forked into "+org.getLogin()+" but can't find the new repository");
    }

    /**
     * Retrieves a specified pull request.
     */
    public GHPullRequest getPullRequest(int i) throws IOException {
        return root.retrieve().to("/repos/" + owner.login + '/' + name + "/pulls/" + i, GHPullRequest.class).wrapUp(this);
    }

    /**
     * Retrieves all the pull requests of a particular state.
     *
     * @see #listPullRequests(GHIssueState)
     */
    public List<GHPullRequest> getPullRequests(GHIssueState state) throws IOException {
        return listPullRequests(state).asList();
    }

    /**
     * Retrieves all the pull requests of a particular state.
     */
    public PagedIterable<GHPullRequest> listPullRequests(final GHIssueState state) {
        return new PagedIterable<GHPullRequest>() {
            public PagedIterator<GHPullRequest> iterator() {
                return new PagedIterator<GHPullRequest>(root.retrieve().asIterator(String.format("/repos/%s/%s/pulls?state=%s", owner.login, name, state.name().toLowerCase(Locale.ENGLISH)), GHPullRequest[].class)) {
                    @Override
                    protected void wrapUp(GHPullRequest[] page) {
                        for (GHPullRequest pr : page)
                            pr.wrap(GHRepository.this);
                    }
                };
            }
        };
    }

    /**
     * Creates a new pull request.
     *
     * @param title
     *      Required. The title of the pull request.
     * @param head
     *      Required. The name of the branch where your changes are implemented.
     *      For cross-repository pull requests in the same network,
     *      namespace head with a user like this: username:branch.
     * @param base
     *      Required. The name of the branch you want your changes pulled into.
     *      This should be an existing branch on the current repository.
     * @param body
     *      The contents of the pull request. This is the markdown description
     *      of a pull request.
     */
    public GHPullRequest createPullRequest(String title, String head, String base, String body) throws IOException {
        return new Requester(root).with("title",title)
                .with("head",head)
                .with("base",base)
                .with("body",body).to(getApiTailUrl("pulls"),GHPullRequest.class).wrapUp(this);
    }

    /**
     * Retrieves the currently configured hooks.
     */
    public List<GHHook> getHooks() throws IOException {
        List<GHHook> list = new ArrayList<GHHook>(Arrays.asList(
                root.retrieve().to(String.format("/repos/%s/%s/hooks", owner.login, name), GHHook[].class)));
        for (GHHook h : list)
            h.wrap(this);
        return list;
    }

    public GHHook getHook(int id) throws IOException {
        return root.retrieve().to(String.format("/repos/%s/%s/hooks/%d", owner.login, name, id), GHHook.class).wrap(this);
    }

    /**
     * Gets a comparison between 2 points in the repository. This would be similar
     * to calling <tt>git log id1...id2</tt> against a local repository.
     * @param id1 an identifier for the first point to compare from, this can be a sha1 ID (for a commit, tag etc) or a direct tag name
     * @param id2 an identifier for the second point to compare to. Can be the same as the first point.
     * @return the comparison output
     * @throws IOException on failure communicating with GitHub
     */
    public GHCompare getCompare(String id1, String id2) throws IOException {
        GHCompare compare = root.retrieve().to(String.format("/repos/%s/%s/compare/%s...%s", owner.login, name, id1, id2), GHCompare.class);
        return compare.wrap(this);
    }

    public GHCompare getCompare(GHCommit id1, GHCommit id2) throws IOException {
        return getCompare(id1.getSHA1(), id2.getSHA1());
    }

    public GHCompare getCompare(GHBranch id1, GHBranch id2) throws IOException {
        return getCompare(id1.getName(),id2.getName());
    }

    /**
     * Retrieves all refs for the github repository.
     * @return an array of GHRef elements coresponding with the refs in the remote repository.
     * @throws IOException on failure communicating with GitHub
     */
    public GHRef[] getRefs() throws IOException {
       return GHRef.wrap(root.retrieve().to(String.format("/repos/%s/%s/git/refs", owner.login, name), GHRef[].class),root);
    }

    /**
     * Retrieves all refs of the given type for the current GitHub repository.
     * @param refType the type of reg to search for e.g. <tt>tags</tt> or <tt>commits</tt>
     * @return an array of all refs matching the request type
     * @throws IOException on failure communicating with GitHub, potentially due to an invalid ref type being requested
     */
    public GHRef[] getRefs(String refType) throws IOException {
        return GHRef.wrap(root.retrieve().to(String.format("/repos/%s/%s/git/refs/%s", owner.login, name, refType), GHRef[].class),root);
    }
    /**
	 * Retrive a ref of the given type for the current GitHub repository.
	 * 
	 * @param refName
	 *            eg: heads/branch
	 * @return refs matching the request type
	 * @throws IOException
	 *             on failure communicating with GitHub, potentially due to an
	 *             invalid ref type being requested
	 */
	public GHRef getRef(String refName) throws IOException {
		return root.retrieve().to(String.format("/repos/%s/%s/git/refs/%s", owner.login, name, refName), GHRef.class).wrap(root);
	}
    /**
     * Gets a commit object in this repository.
     */
    public GHCommit getCommit(String sha1) throws IOException {
        GHCommit c = commits.get(sha1);
        if (c==null) {
            c = root.retrieve().to(String.format("/repos/%s/%s/commits/%s", owner.login, name, sha1), GHCommit.class).wrapUp(this);
            commits.put(sha1,c);
        }
        return c;
    }

    /**
     * Lists all the commits.
     */
    public PagedIterable<GHCommit> listCommits() {
        return new PagedIterable<GHCommit>() {
            public PagedIterator<GHCommit> iterator() {
                return new PagedIterator<GHCommit>(root.retrieve().asIterator(String.format("/repos/%s/%s/commits", owner.login, name), GHCommit[].class)) {
                    protected void wrapUp(GHCommit[] page) {
                        for (GHCommit c : page)
                            c.wrapUp(GHRepository.this);
                    }
                };
            }
        };
    }

    /**
     * Search commits by specifying filters through a builder pattern.
     */
    public GHCommitQueryBuilder queryCommits() {
        return new GHCommitQueryBuilder(this);
    }

    /**
     * Lists up all the commit comments in this repository.
     */
    public PagedIterable<GHCommitComment> listCommitComments() {
        return new PagedIterable<GHCommitComment>() {
            public PagedIterator<GHCommitComment> iterator() {
                return new PagedIterator<GHCommitComment>(root.retrieve().asIterator(String.format("/repos/%s/%s/comments", owner.login, name), GHCommitComment[].class)) {
                    @Override
                    protected void wrapUp(GHCommitComment[] page) {
                        for (GHCommitComment c : page)
                            c.wrap(GHRepository.this);
                    }
                };
            }
        };
    }

    /**
     * Lists all the commit statues attached to the given commit, newer ones first.
     */
    public PagedIterable<GHCommitStatus> listCommitStatuses(final String sha1) throws IOException {
        return new PagedIterable<GHCommitStatus>() {
            public PagedIterator<GHCommitStatus> iterator() {
                return new PagedIterator<GHCommitStatus>(root.retrieve().asIterator(String.format("/repos/%s/%s/statuses/%s", owner.login, name, sha1), GHCommitStatus[].class)) {
                    @Override
                    protected void wrapUp(GHCommitStatus[] page) {
                        for (GHCommitStatus c : page)
                            c.wrapUp(root);
                    }
                };
            }
        };
    }

    /**
     * Gets the last status of this commit, which is what gets shown in the UI.
     */
    public GHCommitStatus getLastCommitStatus(String sha1) throws IOException {
        List<GHCommitStatus> v = listCommitStatuses(sha1).asList();
        return v.isEmpty() ? null : v.get(0);
    }

    /**
     * Creates a commit status
     *
     * @param targetUrl
     *      Optional parameter that points to the URL that has more details.
     * @param description
     *      Optional short description.
     *  @param context
     *      Optinal commit status context.    
     */
    public GHCommitStatus createCommitStatus(String sha1, GHCommitState state, String targetUrl, String description, String context) throws IOException {
        return new Requester(root)
                .with("state", state.name().toLowerCase(Locale.ENGLISH))
                .with("target_url", targetUrl)
                .with("description", description)
                .with("context", context)
                .to(String.format("/repos/%s/%s/statuses/%s",owner.login,this.name,sha1),GHCommitStatus.class).wrapUp(root);
    }
    
    /**
     *  @see {@link #createCommitStatus(String, GHCommitState,String,String,String) createCommitStatus} 
     */
    public GHCommitStatus createCommitStatus(String sha1, GHCommitState state, String targetUrl, String description) throws IOException {
    	return createCommitStatus(sha1, state, targetUrl, description,null);
    }

    /**
     * Lists repository events.
     */
    public PagedIterable<GHEventInfo> listEvents() throws IOException {
        return new PagedIterable<GHEventInfo>() {
            public PagedIterator<GHEventInfo> iterator() {
                return new PagedIterator<GHEventInfo>(root.retrieve().asIterator(String.format("/repos/%s/%s/events", owner.login, name), GHEventInfo[].class)) {
                    @Override
                    protected void wrapUp(GHEventInfo[] page) {
                        for (GHEventInfo c : page)
                            c.wrapUp(root);
                    }
                };
            }
        };
    }

    /**
     * 
     * See https://api.github.com/hooks for possible names and their configuration scheme.
     * TODO: produce type-safe binding
     * 
     * @param name
     *      Type of the hook to be created. See https://api.github.com/hooks for possible names.
     * @param config
     *      The configuration hash.
     * @param events
     *      Can be null. Types of events to hook into.
     */
    public GHHook createHook(String name, Map<String,String> config, Collection<GHEvent> events, boolean active) throws IOException {
        List<String> ea = null;
        if (events!=null) {
            ea = new ArrayList<String>();
            for (GHEvent e : events)
                ea.add(e.name().toLowerCase(Locale.ENGLISH));
        }

        return new Requester(root)
                .with("name", name)
                .with("active", active)
                ._with("config", config)
                ._with("events",ea)
                .to(String.format("/repos/%s/%s/hooks",owner.login,this.name),GHHook.class).wrap(this);
    }
    
    public GHHook createWebHook(URL url, Collection<GHEvent> events) throws IOException {
        return createHook("web",Collections.singletonMap("url",url.toExternalForm()),events,true);
    }

    public GHHook createWebHook(URL url) throws IOException {
        return createWebHook(url,null);
    }

// this is no different from getPullRequests(OPEN)
//    /**
//     * Retrieves all the pull requests.
//     */
//    public List<GHPullRequest> getPullRequests() throws IOException {
//        return root.retrieveWithAuth("/pulls/"+owner+'/'+name,JsonPullRequests.class).wrap(root);
//    }

    private void verifyMine() throws IOException {
        if (!root.login.equals(owner.login))
            throw new IOException("Operation not applicable to a repository owned by someone else: "+owner.login);
    }

    /**
     * Returns a set that represents the post-commit hook URLs.
     * The returned set is live, and changes made to them are reflected to GitHub.
     * 
     * @deprecated 
     *      Use {@link #getHooks()} and {@link #createHook(String, Map, Collection, boolean)}
     */
    public Set<URL> getPostCommitHooks() {
        return postCommitHooks;
    }

    /**
     * Live set view of the post-commit hook.
     */
    private final Set<URL> postCommitHooks = new AbstractSet<URL>() {
        private List<URL> getPostCommitHooks() {
            try {
                List<URL> r = new ArrayList<URL>();
                for (GHHook h : getHooks()) {
                    if (h.getName().equals("web")) {
                        r.add(new URL(h.getConfig().get("url")));
                    }
                }
                return r;
            } catch (IOException e) {
                throw new GHException("Failed to retrieve post-commit hooks",e);
            }
        }

        @Override
        public Iterator<URL> iterator() {
            return getPostCommitHooks().iterator();
        }

        @Override
        public int size() {
            return getPostCommitHooks().size();
        }

        @Override
        public boolean add(URL url) {
            try {
                createWebHook(url);
                return true;
            } catch (IOException e) {
                throw new GHException("Failed to update post-commit hooks",e);
            }
        }

        @Override
        public boolean remove(Object url) {
            try {
                String _url = ((URL)url).toExternalForm();
                for (GHHook h : getHooks()) {
                    if (h.getName().equals("web") && h.getConfig().get("url").equals(_url)) {
                        h.delete();
                        return true;
                    }
                }
                return false;
            } catch (IOException e) {
                throw new GHException("Failed to update post-commit hooks",e);
            }
        }
    };

    /*package*/ GHRepository wrap(GitHub root) {
        this.root = root;
        return this;
    }

    /**
     * Gets branches by {@linkplain GHBranch#getName() their names}.
     */
    public Map<String,GHBranch> getBranches() throws IOException {
        Map<String,GHBranch> r = new TreeMap<String,GHBranch>();
        for (GHBranch p : root.retrieve().to(getApiTailUrl("branches"), GHBranch[].class)) {
            p.wrap(this);
            r.put(p.getName(),p);
        }
        return r;
    }

    /**
     * @deprecated
     *      Use {@link #listMilestones(GHIssueState)}
     */
    public Map<Integer, GHMilestone> getMilestones() throws IOException {
        Map<Integer,GHMilestone> milestones = new TreeMap<Integer, GHMilestone>();
    	for (GHMilestone m : listMilestones(GHIssueState.OPEN)) {
    		milestones.put(m.getNumber(), m);
    	}
    	return milestones;
    }

    /**
     * Lists up all the milestones in this repository.
     */
    public PagedIterable<GHMilestone> listMilestones(final GHIssueState state) {
        return new PagedIterable<GHMilestone>() {
            public PagedIterator<GHMilestone> iterator() {
                return new PagedIterator<GHMilestone>(root.retrieve().asIterator(getApiTailUrl("milestones?state="+state.toString().toLowerCase(Locale.ENGLISH)), GHMilestone[].class)) {
                    @Override
                    protected void wrapUp(GHMilestone[] page) {
                        for (GHMilestone c : page)
                            c.wrap(GHRepository.this);
                    }
                };
            }
        };
    }

	public GHMilestone getMilestone(int number) throws IOException {
		GHMilestone m = milestones.get(number);
		if (m == null) {
            m = root.retrieve().to(getApiTailUrl("milestones/" + number), GHMilestone.class);
    		m.owner = this;
    		m.root = root;
			milestones.put(m.getNumber(), m);
		}
		return m;
	}

    public GHContent getFileContent(String path) throws IOException {
        return getFileContent(path, null);
    }

    public GHContent getFileContent(String path, String ref) throws IOException {
        Requester requester = root.retrieve();
        String target = String.format("/repos/%s/%s/contents/%s", owner.login, name, path);

        if (ref != null)
            target = target + "?ref=" + ref;

        return requester.to(target, GHContent.class).wrap(this);
    }

    public List<GHContent> getDirectoryContent(String path) throws IOException {
        return getDirectoryContent(path, null);
    }

    public List<GHContent> getDirectoryContent(String path, String ref) throws IOException {
        Requester requester = root.retrieve();
        String target = String.format("/repos/%s/%s/contents/%s", owner.login, name, path);

        if (ref != null)
            target = target + "?ref=" + ref;

        GHContent[] files = requester.to(target, GHContent[].class);

        GHContent.wrap(files, this);

        return Arrays.asList(files);
    }

    public GHContent getReadme() throws Exception {
        return getFileContent("readme");
    }

    public GHContentUpdateResponse createContent(String content, String commitMessage, String path) throws IOException {
        return createContent(content, commitMessage, path, null);
    }

    public GHContentUpdateResponse createContent(String content, String commitMessage, String path, String branch) throws IOException {
        Requester requester = new Requester(root)
            .with("path", path)
            .with("message", commitMessage)
            .with("content", DatatypeConverter.printBase64Binary(content.getBytes()))
            .method("PUT");

        if (branch != null) {
            requester.with("branch", branch);
        }

        GHContentUpdateResponse response = requester.to(getApiTailUrl("contents/" + path), GHContentUpdateResponse.class);

        response.getContent().wrap(this);
        response.getCommit().wrapUp(this);

        return response;
    }

	public GHMilestone createMilestone(String title, String description) throws IOException {
        return new Requester(root)
                .with("title", title).with("description", description).method("POST").to(getApiTailUrl("milestones"), GHMilestone.class).wrap(this);
	}
	
	public GHDeployKey addDeployKey(String title,String key) throws IOException {
		 return new Requester(root)
         .with("title", title).with("key", key).method("POST").to(getApiTailUrl("keys"), GHDeployKey.class).wrap(this);
		
	}
	
	public List<GHDeployKey> getDeployKeys() throws IOException{
		 List<GHDeployKey> list = new ArrayList<GHDeployKey>(Arrays.asList(
	                root.retrieve().to(String.format("/repos/%s/%s/keys", owner.login, name), GHDeployKey[].class)));
	        for (GHDeployKey h : list)
	            h.wrap(this);
	        return list;	
	}
	
	

    @Override
    public String toString() {
        return "Repository:"+owner.login+":"+name;
    }

    @Override
    public int hashCode() {
        return toString().hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof GHRepository) {
            GHRepository that = (GHRepository) obj;
            return this.owner.login.equals(that.owner.login)
                && this.name.equals(that.name);
        }
        return false;
    }

    String getApiTailUrl(String tail) {
        return "/repos/" + owner.login + "/" + name +'/'+tail;
    }
}
