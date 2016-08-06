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
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang.StringUtils;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.InterruptedIOException;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
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
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import static java.util.Arrays.asList;

/**
 * A repository on GitHub.
 *
 * @author Kohsuke Kawaguchi
 */
@SuppressWarnings({"UnusedDeclaration"})
@SuppressFBWarnings(value = {"UWF_UNWRITTEN_PUBLIC_OR_PROTECTED_FIELD", "UWF_UNWRITTEN_FIELD", 
    "NP_UNWRITTEN_FIELD"}, justification = "JSON API")
public class GHRepository extends GHObject {
    /*package almost final*/ GitHub root;

    private String description, homepage, name, full_name;
    private String html_url;    // this is the UI
    /*
     * The license information makes use of the preview API.
     *
     * See: https://developer.github.com/v3/licenses/
     */
    private GHLicense license;

    private String git_url, ssh_url, clone_url, svn_url, mirror_url;
    private GHUser owner;   // not fully populated. beware.
    private boolean has_issues, has_wiki, fork, has_downloads;
    @JsonProperty("private")
    private boolean _private;
    private int watchers,forks,open_issues,size,network_count,subscribers_count;
    private String pushed_at;
    private Map<Integer,GHMilestone> milestones = new HashMap<Integer, GHMilestone>();

    private String default_branch,language;
    private Map<String,GHCommit> commits = new HashMap<String, GHCommit>();

    @SkipFromToString
    private GHRepoPermission permissions;

    private GHRepository source, parent;

    public GHDeploymentBuilder createDeployment(String ref) {
        return new GHDeploymentBuilder(this,ref);
    }

    public PagedIterable<GHDeploymentStatus> getDeploymentStatuses(final int id) {
        return new PagedIterable<GHDeploymentStatus>() {
            public PagedIterator<GHDeploymentStatus> _iterator(int pageSize) {
                return new PagedIterator<GHDeploymentStatus>(root.retrieve().asIterator(getApiTailUrl("deployments")+"/"+id+"/statuses", GHDeploymentStatus[].class, pageSize)) {
                    @Override
                    protected void wrapUp(GHDeploymentStatus[] page) {
                        for (GHDeploymentStatus c : page)
                            c.wrap(GHRepository.this);
                    }
                };
            }
        };
    }

    public PagedIterable<GHDeployment> listDeployments(String sha,String ref,String task,String environment){
        List<String> params = Arrays.asList(getParam("sha", sha), getParam("ref", ref), getParam("task", task), getParam("environment", environment));
        final String deploymentsUrl = getApiTailUrl("deployments") + "?"+ join(params,"&");
        return new PagedIterable<GHDeployment>() {
            public PagedIterator<GHDeployment> _iterator(int pageSize) {
                return new PagedIterator<GHDeployment>(root.retrieve().asIterator(deploymentsUrl, GHDeployment[].class, pageSize)) {
                    @Override
                    protected void wrapUp(GHDeployment[] page) {
                        for (GHDeployment c : page)
                            c.wrap(GHRepository.this);
                    }
                };
            }
        };

    }

    private String join(List<String> params, String joinStr) {
        StringBuilder output = new StringBuilder();
        for(String param: params){
            if(param != null){
               output.append(param+joinStr);
            }
        }
        return output.toString();
    }

    private String getParam(String name, String value) {
        return StringUtils.trimToNull(value)== null? null: name+"="+value;
    }

    public GHDeploymentStatusBuilder createDeployStatus(int deploymentId, GHDeploymentState ghDeploymentState) {
        return new GHDeploymentStatusBuilder(this,deploymentId,ghDeploymentState);
    }

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
     * Gets the Mirror URL to access this repository: https://github.com/apache/tomee
     * mirrored from git://git.apache.org/tomee.git
     */
    public String getMirrorUrl() {
        return mirror_url;
    }

    /**
     * Gets the SSH URL to access this repository, such as git@github.com:rails/rails.git
     */
    public String getSshUrl() {
        return ssh_url;
    }

    public URL getHtmlUrl() {
        return GitHub.parseURL(html_url);
    }

    /**
     * Short repository name without the owner. For example 'jenkins' in case of http://github.com/jenkinsci/jenkins
     */
    public String getName() {
        return name;
    }

    /**
     * Full repository name including the owner or organization. For example 'jenkinsci/jenkins' in case of http://github.com/jenkinsci/jenkins
     */
    public String getFullName() {
        return full_name;
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
        return root.retrieve().to(getApiTailUrl("issues/" + id), GHIssue.class).wrap(this);
    }

    public GHIssueBuilder createIssue(String title) {
        return new GHIssueBuilder(this,title);
    }

    public List<GHIssue> getIssues(GHIssueState state) throws IOException {
        return listIssues(state).asList();
    }

    public List<GHIssue> getIssues(GHIssueState state, GHMilestone milestone) throws IOException {
        return Arrays.asList(GHIssue.wrap(root.retrieve()
                .with("state", state)
                .with("milestone", milestone == null ? "none" : "" + milestone.getNumber())
                .to(getApiTailUrl("issues"),
            GHIssue[].class), this));
    }

    /**
     * Lists up all the issues in this repository.
     */
    public PagedIterable<GHIssue> listIssues(final GHIssueState state) {
        return new PagedIterable<GHIssue>() {
            public PagedIterator<GHIssue> _iterator(int pageSize) {
                return new PagedIterator<GHIssue>(root.retrieve().with("state",state).asIterator(getApiTailUrl("issues"), GHIssue[].class, pageSize)) {
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
            public PagedIterator<GHRelease> _iterator(int pageSize) {
                return new PagedIterator<GHRelease>(root.retrieve().asIterator(getApiTailUrl("releases"), GHRelease[].class, pageSize)) {
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
            public PagedIterator<GHTag> _iterator(int pageSize) {
                return new PagedIterator<GHTag>(root.retrieve().asIterator(getApiTailUrl("tags"), GHTag[].class, pageSize)) {
                    @Override
                    protected void wrapUp(GHTag[] page) {
                        for (GHTag c : page)
                            c.wrap(GHRepository.this);
                    }
                };
            }
        };
    }

    /**
     * List languages for the specified repository.
     * The value on the right of a language is the number of bytes of code written in that language.
     * {
         "C": 78769,
         "Python": 7769
       }
     */
    public Map<String,Long> listLanguages() throws IOException {
        return root.retrieve().to(getApiTailUrl("languages"), HashMap.class);
    }

    public String getOwnerName() {
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

    /**
     * Returns the number of all forks of this repository.
     * This not only counts direct forks, but also forks of forks, and so on.
     */
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

    /**
     * Returns the primary branch you'll configure in the "Admin &gt; Options" config page.
     *
     * @return
     *      This field is null until the user explicitly configures the master branch.
     */
    public String getDefaultBranch() {
        return default_branch;
    }

    /**
     * @deprecated
     *      Renamed to {@link #getDefaultBranch()}
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
            public PagedIterator<GHUser> _iterator(int pageSize) {

                return new PagedIterator<GHUser>(root.retrieve().asIterator(getApiTailUrl("collaborators"), GHUser[].class, pageSize)) {

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
        for (GHUser u : GHUser.wrap(root.retrieve().to(getApiTailUrl("collaborators"), GHUser[].class),root))
            r.add(u.login);
        return r;
    }

    /**
     * If this repository belongs to an organization, return a set of teams.
     */
    public Set<GHTeam> getTeams() throws IOException {
        return Collections.unmodifiableSet(new HashSet<GHTeam>(Arrays.asList(GHTeam.wrapUp(root.retrieve().to(getApiTailUrl("teams"), GHTeam[].class), root.getOrganization(owner.login)))));
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
            new Requester(root).method(method).to(getApiTailUrl("collaborators/" + user.getLogin()));
        }
    }

    public void setEmailServiceHook(String address) throws IOException {
        Map<String, String> config = new HashMap<String, String>();
        config.put("address", address);
        new Requester(root).method("POST").with("name", "email").with("config", config).with("active", true)
                .to(getApiTailUrl("hooks"));
    }

    private void edit(String key, String value) throws IOException {
        Requester requester = new Requester(root);
        if (!key.equals("name"))
            requester.with("name", name);   // even when we don't change the name, we need to send it in
        requester.with(key, value).method("PATCH").to(getApiTailUrl(""));
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

    public void setDefaultBranch(String value) throws IOException {
        edit("default_branch", value);
    }

    /**
     * Deletes this repository.
     */
    public void delete() throws IOException {
        try {
            new Requester(root).method("DELETE").to(getApiTailUrl(""));
        } catch (FileNotFoundException x) {
            throw (FileNotFoundException) new FileNotFoundException("Failed to delete " + owner.login + "/" + name + "; might not exist, or you might need the delete_repo scope in your token: http://stackoverflow.com/a/19327004/12916").initCause(x);
        }
    }

    /**
     * Sort orders for listing forks
     */
    public enum ForkSort { NEWEST, OLDEST, STARGAZERS }

    /**
     * Lists all the direct forks of this repository, sorted by
     * github api default, currently {@link ForkSort#NEWEST ForkSort.NEWEST}.
     */
    public PagedIterable<GHRepository> listForks() {
      return listForks(null);
    }

    /**
     * Lists all the direct forks of this repository, sorted by the given sort order.
     * @param sort the sort order. If null, defaults to github api default,
     * currently {@link ForkSort#NEWEST ForkSort.NEWEST}.
     */
    public PagedIterable<GHRepository> listForks(final ForkSort sort) {
        return new PagedIterable<GHRepository>() {
            public PagedIterator<GHRepository> _iterator(int pageSize) {
                return new PagedIterator<GHRepository>(root.retrieve().with("sort",sort).asIterator(getApiTailUrl("forks"), GHRepository[].class, pageSize)) {
                    @Override
                    protected void wrapUp(GHRepository[] page) {
                        for (GHRepository c : page) {
                            c.wrap(root);
                        }
                    }
                };
            }
        };
    }

    /**
     * Forks this repository as your repository.
     *
     * @return
     *      Newly forked repository that belong to you.
     */
    public GHRepository fork() throws IOException {
        new Requester(root).method("POST").to(getApiTailUrl("forks"), null);

        // this API is asynchronous. we need to wait for a bit
        for (int i=0; i<10; i++) {
            GHRepository r = root.getMyself().getRepository(name);
            if (r!=null)    return r;
            try {
                Thread.sleep(3000);
            } catch (InterruptedException e) {
                throw (IOException)new InterruptedIOException().initCause(e);
            }
        }
        throw new IOException(this+" was forked but can't find the new repository");
    }

    /**
     * Forks this repository into an organization.
     *
     * @return
     *      Newly forked repository that belong to you.
     */
    public GHRepository forkTo(GHOrganization org) throws IOException {
        new Requester(root).to(getApiTailUrl("forks?org="+org.getLogin()));

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
        return root.retrieve().to(getApiTailUrl("pulls/" + i), GHPullRequest.class).wrapUp(this);
    }

    /**
     * Retrieves all the pull requests of a particular state.
     *
     * @see #listPullRequests(GHIssueState)
     */
    public List<GHPullRequest> getPullRequests(GHIssueState state) throws IOException {
        return queryPullRequests().state(state).list().asList();
    }

    /**
     * Retrieves all the pull requests of a particular state.
     *
     * @deprecated
     *      Use {@link #queryPullRequests()}
     */
    public PagedIterable<GHPullRequest> listPullRequests(GHIssueState state) {
        return queryPullRequests().state(state).list();
    }

    /**
     * Retrieves pull requests.
     */
    public GHPullRequestQueryBuilder queryPullRequests() {
        return new GHPullRequestQueryBuilder(this);
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
        return GHHooks.repoContext(this, owner).getHooks();
    }

    public GHHook getHook(int id) throws IOException {
        return GHHooks.repoContext(this, owner).getHook(id);
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
        GHCompare compare = root.retrieve().to(getApiTailUrl(String.format("compare/%s...%s", id1, id2)), GHCompare.class);
        return compare.wrap(this);
    }

    public GHCompare getCompare(GHCommit id1, GHCommit id2) throws IOException {
        return getCompare(id1.getSHA1(), id2.getSHA1());
    }

    public GHCompare getCompare(GHBranch id1, GHBranch id2) throws IOException {

        GHRepository owner1 = id1.getOwner();
        GHRepository owner2 = id2.getOwner();

        // If the owner of the branches is different, we have a cross-fork compare.
        if (owner1!=null && owner2!=null) {
            String ownerName1 = owner1.getOwnerName();
            String ownerName2 = owner2.getOwnerName();
            if (!StringUtils.equals(ownerName1, ownerName2)) {
                String qualifiedName1 = String.format("%s:%s", ownerName1, id1.getName());
                String qualifiedName2 = String.format("%s:%s", ownerName2, id2.getName());
                return getCompare(qualifiedName1, qualifiedName2);
            }
        }

        return getCompare(id1.getName(), id2.getName());

    }

    /**
     * Retrieves all refs for the github repository.
     * @return an array of GHRef elements coresponding with the refs in the remote repository.
     * @throws IOException on failure communicating with GitHub
     */
    public GHRef[] getRefs() throws IOException {
       return GHRef.wrap(root.retrieve().to(String.format("/repos/%s/%s/git/refs", owner.login, name), GHRef[].class), root);
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
     * Retrive a tree of the given type for the current GitHub repository.
     *
     * @param sha - sha number or branch name ex: "master"
     * @return refs matching the request type
     * @throws IOException
     *             on failure communicating with GitHub, potentially due to an
     *             invalid tree type being requested
     */
    public GHTree getTree(String sha) throws IOException {
        String url = String.format("/repos/%s/%s/git/trees/%s", owner.login, name, sha);
        return root.retrieve().to(url, GHTree.class).wrap(root);
    }

    /**
     * Retrieves the tree for the current GitHub repository, recursively as described in here:
     * https://developer.github.com/v3/git/trees/#get-a-tree-recursively
     *
     * @param sha - sha number or branch name ex: "master"
     * @param recursive use 1
     * @throws IOException
     *             on failure communicating with GitHub, potentially due to an
     *             invalid tree type being requested
     */
    public GHTree getTreeRecursive(String sha, int recursive) throws IOException {
        String url = String.format("/repos/%s/%s/git/trees/%s?recursive=%d", owner.login, name, sha, recursive);
        return root.retrieve().to(url, GHTree.class).wrap(root);
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
            public PagedIterator<GHCommit> _iterator(int pageSize) {
                return new PagedIterator<GHCommit>(root.retrieve().asIterator(String.format("/repos/%s/%s/commits", owner.login, name), GHCommit[].class, pageSize)) {
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
            public PagedIterator<GHCommitComment> _iterator(int pageSize) {
                return new PagedIterator<GHCommitComment>(root.retrieve().asIterator(String.format("/repos/%s/%s/comments", owner.login, name), GHCommitComment[].class, pageSize)) {
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
     * Gets the basic license details for the repository.
     * <p>
     * This is a preview item and subject to change.
     *
     * @throws IOException as usual but also if you don't use the preview connector
     */
    @Preview @Deprecated
    public GHLicense getLicense() throws IOException{
        return getLicenseContent_().license;
    }

    /**
     * Retrieves the contents of the repository's license file - makes an additional API call
     * <p>
     * This is a preview item and subject to change.
     *
     * @return details regarding the license contents
     * @throws IOException as usual but also if you don't use the preview connector
     */
    @Preview @Deprecated
    public GHContent getLicenseContent() throws IOException {
        return getLicenseContent_();
    }

    @Preview @Deprecated
    private GHContentWithLicense getLicenseContent_() throws IOException {
        return root.retrieve()
                .withHeader("Accept","application/vnd.github.drax-preview+json")
                .to(getApiTailUrl("license"), GHContentWithLicense.class).wrap(this);
    }

    /**

    /**
     * Lists all the commit statues attached to the given commit, newer ones first.
     */
    public PagedIterable<GHCommitStatus> listCommitStatuses(final String sha1) throws IOException {
        return new PagedIterable<GHCommitStatus>() {
            public PagedIterator<GHCommitStatus> _iterator(int pageSize) {
                return new PagedIterator<GHCommitStatus>(root.retrieve().asIterator(String.format("/repos/%s/%s/statuses/%s", owner.login, name, sha1), GHCommitStatus[].class, pageSize)) {
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
                .with("state", state)
                .with("target_url", targetUrl)
                .with("description", description)
                .with("context", context)
                .to(String.format("/repos/%s/%s/statuses/%s",owner.login,this.name,sha1),GHCommitStatus.class).wrapUp(root);
    }

    /**
     *  @see #createCommitStatus(String, GHCommitState,String,String,String)
     */
    public GHCommitStatus createCommitStatus(String sha1, GHCommitState state, String targetUrl, String description) throws IOException {
        return createCommitStatus(sha1, state, targetUrl, description, null);
    }

    /**
     * Lists repository events.
     */
    public PagedIterable<GHEventInfo> listEvents() throws IOException {
        return new PagedIterable<GHEventInfo>() {
            public PagedIterator<GHEventInfo> _iterator(int pageSize) {
                return new PagedIterator<GHEventInfo>(root.retrieve().asIterator(String.format("/repos/%s/%s/events", owner.login, name), GHEventInfo[].class, pageSize)) {
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
     * Lists labels in this repository.
     *
     * https://developer.github.com/v3/issues/labels/#list-all-labels-for-this-repository
     */
    public PagedIterable<GHLabel> listLabels() throws IOException {
        return new PagedIterable<GHLabel>() {
            public PagedIterator<GHLabel> _iterator(int pageSize) {
                return new PagedIterator<GHLabel>(root.retrieve().asIterator(getApiTailUrl("labels"), GHLabel[].class, pageSize)) {
                    @Override
                    protected void wrapUp(GHLabel[] page) {
                        for (GHLabel c : page)
                            c.wrapUp(GHRepository.this);
                    }
                };
            }
        };
    }

    public GHLabel getLabel(String name) throws IOException {
        return root.retrieve().to(getApiTailUrl("labels/"+name), GHLabel.class).wrapUp(this);
    }

    public GHLabel createLabel(String name, String color) throws IOException {
        return root.retrieve().method("POST")
                .with("name",name)
                .with("color", color)
                .to(getApiTailUrl("labels"), GHLabel.class).wrapUp(this);
    }

    /**
     * Lists all the subscribers (aka watchers.)
     *
     * https://developer.github.com/v3/activity/watching/
     */
    public PagedIterable<GHUser> listSubscribers() {
        return listUsers("subscribers");
    }

    /**
     * Lists all the users who have starred this repo based on the old version of the API. For
     * additional information, like date when the repository was starred, see {@link #listStargazers2()}
     */
    public PagedIterable<GHUser> listStargazers() {
        return listUsers("stargazers");
    }

    /**
     * Lists all the users who have starred this repo based on new version of the API, having extended
     * information like the time when the repository was starred. For compatibility with the old API
     * see {@link #listStargazers()}
     */
    public PagedIterable<GHStargazer> listStargazers2() {
        return new PagedIterable<GHStargazer>() {
            @Override
            public PagedIterator<GHStargazer> _iterator(int pageSize) {
                Requester requester = root.retrieve();
                requester.setHeader("Accept", "application/vnd.github.v3.star+json");
                return new PagedIterator<GHStargazer>(requester.asIterator(getApiTailUrl("stargazers"), GHStargazer[].class, pageSize)) {
                    @Override
                    protected void wrapUp(GHStargazer[] page) {
                        for (GHStargazer c : page) {
                            c.wrapUp(GHRepository.this);
                        }
                    }
                };
            }
        };
    }

    private PagedIterable<GHUser> listUsers(final String suffix) {
        return new PagedIterable<GHUser>() {
            public PagedIterator<GHUser> _iterator(int pageSize) {
                return new PagedIterator<GHUser>(root.retrieve().asIterator(getApiTailUrl(suffix), GHUser[].class, pageSize)) {
                    protected void wrapUp(GHUser[] page) {
                        for (GHUser c : page)
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
        return GHHooks.repoContext(this, owner).createHook(name, config, events, active);
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
    @SuppressFBWarnings(value = "DMI_COLLECTION_OF_URLS", 
            justification = "It causes a performance degradation, but we have already exposed it to the API")
    public Set<URL> getPostCommitHooks() {
        return postCommitHooks;
    }

    /**
     * Live set view of the post-commit hook.
     */
    @SuppressFBWarnings(value = "DMI_COLLECTION_OF_URLS", 
            justification = "It causes a performance degradation, but we have already exposed it to the API")
    @SkipFromToString
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

    public GHBranch getBranch(String name) throws IOException {
        return root.retrieve().to(getApiTailUrl("branches/"+name),GHBranch.class).wrap(this);
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
            public PagedIterator<GHMilestone> _iterator(int pageSize) {
                return new PagedIterator<GHMilestone>(root.retrieve().with("state",state).asIterator(getApiTailUrl("milestones"), GHMilestone[].class, pageSize)) {
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
        String target = getApiTailUrl("contents/" + path);

        return requester.with("ref",ref).to(target, GHContent.class).wrap(this);
    }

    public List<GHContent> getDirectoryContent(String path) throws IOException {
        return getDirectoryContent(path, null);
    }

    public List<GHContent> getDirectoryContent(String path, String ref) throws IOException {
        Requester requester = root.retrieve();
        while (path.endsWith("/")) {
            path = path.substring(0, path.length() - 1);
        }
        String target = getApiTailUrl("contents/" + path);

        GHContent[] files = requester.with("ref",ref).to(target, GHContent[].class);

        GHContent.wrap(files, this);

        return Arrays.asList(files);
    }

    /**
     * https://developer.github.com/v3/repos/contents/#get-the-readme
     */
    public GHContent getReadme() throws IOException {
        Requester requester = root.retrieve();
        return requester.to(getApiTailUrl("readme"), GHContent.class).wrap(this);
    }

    public GHContentUpdateResponse createContent(String content, String commitMessage, String path) throws IOException {
        return createContent(content, commitMessage, path, null);
    }

    public GHContentUpdateResponse createContent(String content, String commitMessage, String path, String branch) throws IOException {
        final byte[] payload;
        try {
            payload = content.getBytes("UTF-8");
        } catch (UnsupportedEncodingException ex) {
            throw (IOException) new IOException("UTF-8 encoding is not supported").initCause(ex);
        }
        return createContent(payload, commitMessage, path, branch);
    }

    public GHContentUpdateResponse createContent(byte[] contentBytes, String commitMessage, String path) throws IOException {
        return createContent(contentBytes, commitMessage, path, null);
    }

    public GHContentUpdateResponse createContent(byte[] contentBytes, String commitMessage, String path, String branch) throws IOException {
        Requester requester = new Requester(root)
            .with("path", path)
            .with("message", commitMessage)
            .with("content", Base64.encodeBase64String(contentBytes))
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
                    root.retrieve().to(getApiTailUrl("keys"), GHDeployKey[].class)));
            for (GHDeployKey h : list)
                h.wrap(this);
            return list;
    }

    /**
     * Forked repositories have a 'source' attribute that specifies the ultimate source of the forking chain.
     *
     * @return
     *      {@link GHRepository} that points to the root repository where this repository is forked
     *      (indirectly or directly) from. Otherwise null.
     * @see #getParent()
     */
    public GHRepository getSource() throws IOException {
        if (source == null) return null;
        if (source.root == null)
            source = root.getRepository(source.getFullName());
        return source;
    }

    /**
     * Forked repositories have a 'parent' attribute that specifies the repository this repository
     * is directly forked from. If we keep traversing {@link #getParent()} until it returns null, that
     * is {@link #getSource()}.
     *
     * @return
     *      {@link GHRepository} that points to the repository where this repository is forked
     *      directly from. Otherwise null.
     * @see #getSource()
     */
    public GHRepository getParent() throws IOException {
        if (parent == null) return null;
        if (parent.root == null)
            parent = root.getRepository(parent.getFullName());
        return parent;
    }

    /**
     * Subscribes to this repository to get notifications.
     */
    public GHSubscription subscribe(boolean subscribed, boolean ignored) throws IOException {
        return new Requester(root)
            .with("subscribed", subscribed)
            .with("ignored", ignored)
            .method("PUT").to(getApiTailUrl("subscription"), GHSubscription.class).wrapUp(this);
    }

    /**
     * Returns the current subscription.
     *
     * @return null if no subscription exists.
     */
    public GHSubscription getSubscription() throws IOException {
        try {
            return root.retrieve().to(getApiTailUrl("subscription"), GHSubscription.class).wrapUp(this);
        } catch (FileNotFoundException e) {
            return null;
        }
    }

    public PagedIterable<Contributor> listContributors() throws IOException {
        return new PagedIterable<Contributor>() {
            public PagedIterator<Contributor> _iterator(int pageSize) {
                return new PagedIterator<Contributor>(root.retrieve().asIterator(getApiTailUrl("contributors"), Contributor[].class, pageSize)) {
                    @Override
                    protected void wrapUp(Contributor[] page) {
                        for (Contributor c : page)
                            c.wrapUp(root);
                    }
                };
            }
        };
    }

    public static class Contributor extends GHUser {
        private int contributions;

        public int getContributions() {
            return contributions;
        }

        @Override
        public int hashCode() {
            // We ignore contributions in the calculation
            return super.hashCode();
        }

        @Override
        public boolean equals(Object obj) {
            // We ignore contributions in the calculation
            return super.equals(obj);
        }   
    }

    /**
     * Render a Markdown document.
     *
     * In {@linkplain MarkdownMode#GFM GFM mode}, issue numbers and user mentions
     * are linked accordingly.
     *
     * @see GitHub#renderMarkdown(String)
     */
    public Reader renderMarkdown(String text, MarkdownMode mode) throws IOException {
        return new InputStreamReader(
            new Requester(root)
                    .with("text", text)
                    .with("mode",mode==null?null:mode.toString())
                    .with("context", getFullName())
                    .asStream("/markdown"),
            "UTF-8");
    }

    /**
     * List all the notifications in a repository for the current user.
     */
    public GHNotificationStream listNotifications() {
        return new GHNotificationStream(root,getApiTailUrl("/notifications"));
    }


    @Override
    public int hashCode() {
        return ("Repository:"+owner.login+":"+name).hashCode();
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
        if (tail.length()>0 && !tail.startsWith("/"))    tail='/'+tail;
        return "/repos/" + owner.login + "/" + name +tail;
    }
}
