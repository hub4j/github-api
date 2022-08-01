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
import edu.umd.cs.findbugs.annotations.CheckForNull;
import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import org.apache.commons.lang3.StringUtils;
import org.kohsuke.github.function.InputStreamFunction;
import org.kohsuke.github.internal.EnumUtils;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.InterruptedIOException;
import java.io.Reader;
import java.net.URL;
import java.util.AbstractSet;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.WeakHashMap;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import static java.util.Arrays.asList;
import static java.util.Objects.requireNonNull;
import static org.kohsuke.github.internal.Previews.ANTIOPE;
import static org.kohsuke.github.internal.Previews.ANT_MAN;
import static org.kohsuke.github.internal.Previews.BAPTISTE;
import static org.kohsuke.github.internal.Previews.FLASH;
import static org.kohsuke.github.internal.Previews.INERTIA;
import static org.kohsuke.github.internal.Previews.MERCY;
import static org.kohsuke.github.internal.Previews.NEBULA;
import static org.kohsuke.github.internal.Previews.SHADOW_CAT;

/**
 * A repository on GitHub.
 *
 * @author Kohsuke Kawaguchi
 */
@SuppressWarnings({ "UnusedDeclaration" })
@SuppressFBWarnings(value = { "UWF_UNWRITTEN_PUBLIC_OR_PROTECTED_FIELD", "UWF_UNWRITTEN_FIELD", "NP_UNWRITTEN_FIELD" },
        justification = "JSON API")
public class GHRepository extends GHObject {

    private String nodeId, description, homepage, name, full_name;

    private String html_url; // this is the UI

    /*
     * The license information makes use of the preview API.
     *
     * See: https://developer.github.com/v3/licenses/
     */
    private GHLicense license;

    private String git_url, ssh_url, clone_url, svn_url, mirror_url;

    private GHUser owner; // not fully populated. beware.

    private boolean has_issues, has_wiki, fork, has_downloads, has_pages, archived, disabled, has_projects;

    private boolean allow_squash_merge;

    private boolean allow_merge_commit;

    private boolean allow_rebase_merge;

    private boolean delete_branch_on_merge;

    @JsonProperty("private")
    private boolean _private;

    private String visibility;

    private int forks_count, stargazers_count, watchers_count, size, open_issues_count, subscribers_count;

    private String pushed_at;

    private Map<Integer, GHMilestone> milestones = Collections.synchronizedMap(new WeakHashMap<>());

    private String default_branch, language;

    private Map<String, GHCommit> commits = Collections.synchronizedMap(new WeakHashMap<>());

    @SkipFromToString
    private GHRepoPermission permissions;

    private GHRepository source, parent;

    private Boolean isTemplate;
    private boolean compareUsePaginatedCommits;

    static GHRepository read(GitHub root, String owner, String name) throws IOException {
        return root.createRequest().withUrlPath("/repos/" + owner + '/' + name).fetch(GHRepository.class);
    }

    /**
     * Create deployment gh deployment builder.
     *
     * @param ref
     *            the ref
     * @return the gh deployment builder
     */
    public GHDeploymentBuilder createDeployment(String ref) {
        return new GHDeploymentBuilder(this, ref);
    }

    /**
     * Gets deployment statuses.
     *
     * @param id
     *            the id
     * @return the deployment statuses
     * @throws IOException
     *             the io exception
     * @deprecated Use {@code getDeployment(id).listStatuses()}
     */
    @Deprecated
    public PagedIterable<GHDeploymentStatus> getDeploymentStatuses(final int id) throws IOException {
        return getDeployment(id).listStatuses();
    }

    /**
     * List deployments paged iterable.
     *
     * @param sha
     *            the sha
     * @param ref
     *            the ref
     * @param task
     *            the task
     * @param environment
     *            the environment
     * @return the paged iterable
     */
    public PagedIterable<GHDeployment> listDeployments(String sha, String ref, String task, String environment) {
        return root().createRequest()
                .with("sha", sha)
                .with("ref", ref)
                .with("task", task)
                .with("environment", environment)
                .withUrlPath(getApiTailUrl("deployments"))
                .withPreview(ANT_MAN)
                .withPreview(FLASH)
                .toIterable(GHDeployment[].class, item -> item.wrap(this));
    }

    /**
     * Obtains a single {@link GHDeployment} by its ID.
     *
     * @param id
     *            the id
     * @return the deployment
     * @throws IOException
     *             the io exception
     */
    public GHDeployment getDeployment(long id) throws IOException {
        return root().createRequest()
                .withUrlPath(getApiTailUrl("deployments/" + id))
                .withPreview(ANT_MAN)
                .withPreview(FLASH)
                .fetch(GHDeployment.class)
                .wrap(this);
    }

    /**
     * Gets deploy status.
     *
     * @param deploymentId
     *            the deployment id
     * @param ghDeploymentState
     *            the gh deployment state
     * @return the deploy status
     * @throws IOException
     *             the io exception
     * @deprecated Use {@code getDeployment(deploymentId).createStatus(ghDeploymentState)}
     */
    @Deprecated
    public GHDeploymentStatusBuilder createDeployStatus(int deploymentId, GHDeploymentState ghDeploymentState)
            throws IOException {
        return getDeployment(deploymentId).createStatus(ghDeploymentState);
    }

    private static class GHRepoPermission {
        boolean pull, push, admin;
    }

    /**
     * Gets node id
     *
     * @return the node id
     */
    public String getNodeId() {
        return nodeId;
    }

    /**
     * Gets description.
     *
     * @return the description
     */
    public String getDescription() {
        return description;
    }

    /**
     * Gets homepage.
     *
     * @return the homepage
     */
    public String getHomepage() {
        return homepage;
    }

    /**
     * Gets the git:// URL to this repository, such as "git://github.com/kohsuke/jenkins.git" This URL is read-only.
     *
     * @return the git transport url
     */
    public String getGitTransportUrl() {
        return git_url;
    }

    /**
     * Gets the HTTPS URL to this repository, such as "https://github.com/kohsuke/jenkins.git" This URL is read-only.
     *
     * @return the http transport url
     */
    public String getHttpTransportUrl() {
        return clone_url;
    }

    /**
     * Git http transport url string.
     *
     * @return the string
     * @deprecated Typo of {@link #getHttpTransportUrl()}
     */
    @Deprecated
    public String gitHttpTransportUrl() {
        return clone_url;
    }

    /**
     * Gets the Subversion URL to access this repository: https://github.com/rails/rails
     *
     * @return the svn url
     */
    public String getSvnUrl() {
        return svn_url;
    }

    /**
     * Gets the Mirror URL to access this repository: https://github.com/apache/tomee mirrored from
     * git://git.apache.org/tomee.git
     *
     * @return the mirror url
     */
    public String getMirrorUrl() {
        return mirror_url;
    }

    /**
     * Gets the SSH URL to access this repository, such as git@github.com:rails/rails.git
     *
     * @return the ssh url
     */
    public String getSshUrl() {
        return ssh_url;
    }

    public URL getHtmlUrl() {
        return GitHubClient.parseURL(html_url);
    }

    /**
     * Short repository name without the owner. For example 'jenkins' in case of http://github.com/jenkinsci/jenkins
     *
     * @return the name
     */
    public String getName() {
        return name;
    }

    /**
     * Full repository name including the owner or organization. For example 'jenkinsci/jenkins' in case of
     * http://github.com/jenkinsci/jenkins
     *
     * @return the full name
     */
    public String getFullName() {
        return full_name;
    }

    /**
     * Has pull access boolean.
     *
     * @return the boolean
     */
    public boolean hasPullAccess() {
        return permissions != null && permissions.pull;
    }

    /**
     * Has push access boolean.
     *
     * @return the boolean
     */
    public boolean hasPushAccess() {
        return permissions != null && permissions.push;
    }

    /**
     * Has admin access boolean.
     *
     * @return the boolean
     */
    public boolean hasAdminAccess() {
        return permissions != null && permissions.admin;
    }

    /**
     * Gets the primary programming language.
     *
     * @return the language
     */
    public String getLanguage() {
        return language;
    }

    /**
     * Gets owner.
     *
     * @return the owner
     * @throws IOException
     *             the io exception
     */
    @SuppressFBWarnings(value = { "EI_EXPOSE_REP" }, justification = "Expected behavior")
    public GHUser getOwner() throws IOException {
        return isOffline() ? owner : root().getUser(getOwnerName()); // because 'owner' isn't fully populated
    }

    /**
     * Gets issue.
     *
     * @param id
     *            the id
     * @return the issue
     * @throws IOException
     *             the io exception
     */
    public GHIssue getIssue(int id) throws IOException {
        return root().createRequest().withUrlPath(getApiTailUrl("issues/" + id)).fetch(GHIssue.class).wrap(this);
    }

    /**
     * Create issue gh issue builder.
     *
     * @param title
     *            the title
     * @return the gh issue builder
     */
    public GHIssueBuilder createIssue(String title) {
        return new GHIssueBuilder(this, title);
    }

    /**
     * Gets issues.
     *
     * @param state
     *            the state
     * @return the issues
     * @throws IOException
     *             the io exception
     */
    public List<GHIssue> getIssues(GHIssueState state) throws IOException {
        return queryIssues().state(state).list().toList();
    }

    /**
     * Gets issues.
     *
     * @param state
     *            the state
     * @param milestone
     *            the milestone
     * @return the issues
     * @throws IOException
     *             the io exception
     */
    public List<GHIssue> getIssues(GHIssueState state, GHMilestone milestone) throws IOException {
        return queryIssues().milestone(milestone == null ? "none" : "" + milestone.getNumber())
                .state(state)
                .list()
                .toList();
    }

    /**
     * Lists up all the issues in this repository.
     *
     * @param state
     *            the state
     * @return the paged iterable
     * @deprecated Use {@link #queryIssues()}
     */
    @Deprecated
    public PagedIterable<GHIssue> listIssues(final GHIssueState state) {
        return queryIssues().state(state).list();
    }

    /**
     * Retrieves issues.
     *
     * @return the gh issue query builder
     */
    public GHIssueQueryBuilder.ForRepository queryIssues() {
        return new GHIssueQueryBuilder.ForRepository(this);
    }

    /**
     * Create release gh release builder.
     *
     * @param tag
     *            the tag
     * @return the gh release builder
     */
    public GHReleaseBuilder createRelease(String tag) {
        return new GHReleaseBuilder(this, tag);
    }

    /**
     * Creates a named ref, such as tag, branch, etc.
     *
     * @param name
     *            The name of the fully qualified reference (ie: refs/heads/main). If it doesn't start with 'refs' and
     *            have at least two slashes, it will be rejected.
     * @param sha
     *            The SHA1 value to set this reference to
     * @return the gh ref
     * @throws IOException
     *             the io exception
     */
    public GHRef createRef(String name, String sha) throws IOException {
        return root().createRequest()
                .method("POST")
                .with("ref", name)
                .with("sha", sha)
                .withUrlPath(getApiTailUrl("git/refs"))
                .fetch(GHRef.class);
    }

    /**
     * Gets releases.
     *
     * @return the releases
     * @throws IOException
     *             the io exception
     * @deprecated use {@link #listReleases()}
     */
    public List<GHRelease> getReleases() throws IOException {
        return listReleases().toList();
    }

    /**
     * Gets release.
     *
     * @param id
     *            the id
     * @return the release
     * @throws IOException
     *             the io exception
     */
    public GHRelease getRelease(long id) throws IOException {
        try {
            return root().createRequest()
                    .withUrlPath(getApiTailUrl("releases/" + id))
                    .fetch(GHRelease.class)
                    .wrap(this);
        } catch (FileNotFoundException e) {
            return null; // no release for this id
        }
    }

    /**
     * Gets release by tag name.
     *
     * @param tag
     *            the tag
     * @return the release by tag name
     * @throws IOException
     *             the io exception
     */
    public GHRelease getReleaseByTagName(String tag) throws IOException {
        try {
            return root().createRequest()
                    .withUrlPath(getApiTailUrl("releases/tags/" + tag))
                    .fetch(GHRelease.class)
                    .wrap(this);
        } catch (FileNotFoundException e) {
            return null; // no release for this tag
        }
    }

    /**
     * Gets latest release.
     *
     * @return the latest release
     * @throws IOException
     *             the io exception
     */
    public GHRelease getLatestRelease() throws IOException {
        try {
            return root().createRequest()
                    .withUrlPath(getApiTailUrl("releases/latest"))
                    .fetch(GHRelease.class)
                    .wrap(this);
        } catch (FileNotFoundException e) {
            return null; // no latest release
        }
    }

    /**
     * List releases paged iterable.
     *
     * @return the paged iterable
     * @throws IOException
     *             the io exception
     */
    public PagedIterable<GHRelease> listReleases() throws IOException {
        return root().createRequest()
                .withUrlPath(getApiTailUrl("releases"))
                .toIterable(GHRelease[].class, item -> item.wrap(this));
    }

    /**
     * List tags paged iterable.
     *
     * @return the paged iterable
     * @throws IOException
     *             the io exception
     */
    public PagedIterable<GHTag> listTags() throws IOException {
        return root().createRequest()
                .withUrlPath(getApiTailUrl("tags"))
                .toIterable(GHTag[].class, item -> item.wrap(this));
    }

    /**
     * List languages for the specified repository. The value on the right of a language is the number of bytes of code
     * written in that language. { "C": 78769, "Python": 7769 }
     *
     * @return the map
     * @throws IOException
     *             the io exception
     */
    public Map<String, Long> listLanguages() throws IOException {
        HashMap<String, Long> result = new HashMap<>();
        root().createRequest().withUrlPath(getApiTailUrl("languages")).fetch(HashMap.class).forEach((key, value) -> {
            Long addValue = -1L;
            if (value instanceof Integer) {
                addValue = Long.valueOf((Integer) value);
            }
            result.put(key.toString(), addValue);
        });
        return result;
    }

    /**
     * Gets owner name.
     *
     * @return the owner name
     */
    public String getOwnerName() {
        // consistency of the GitHub API is super... some serialized forms of GHRepository populate
        // a full GHUser while others populate only the owner and email. This later form is super helpful
        // in putting the login in owner.name not owner.login... thankfully we can easily identify this
        // second set because owner.login will be null
        return owner.login != null ? owner.login : owner.name;
    }

    /**
     * Has issues boolean.
     *
     * @return the boolean
     */
    public boolean hasIssues() {
        return has_issues;
    }

    /**
     * Has projects boolean.
     *
     * @return the boolean
     */
    public boolean hasProjects() {
        return has_projects;
    }

    /**
     * Has wiki boolean.
     *
     * @return the boolean
     */
    public boolean hasWiki() {
        return has_wiki;
    }

    /**
     * Is fork boolean.
     *
     * @return the boolean
     */
    public boolean isFork() {
        return fork;
    }

    /**
     * Is archived boolean.
     *
     * @return the boolean
     */
    public boolean isArchived() {
        return archived;
    }

    /**
     * Is disabled boolean.
     *
     * @return the boolean
     */
    public boolean isDisabled() {
        return disabled;
    }

    /**
     * Is allow squash merge boolean.
     *
     * @return the boolean
     */
    public boolean isAllowSquashMerge() {
        return allow_squash_merge;
    }

    /**
     * Is allow merge commit boolean.
     *
     * @return the boolean
     */
    public boolean isAllowMergeCommit() {
        return allow_merge_commit;
    }

    /**
     * Is allow rebase merge boolean.
     *
     * @return the boolean
     */
    public boolean isAllowRebaseMerge() {
        return allow_rebase_merge;
    }

    /**
     * Automatically deleting head branches when pull requests are merged
     *
     * @return the boolean
     */
    public boolean isDeleteBranchOnMerge() {
        return delete_branch_on_merge;
    }

    /**
     * Returns the number of all forks of this repository. This not only counts direct forks, but also forks of forks,
     * and so on.
     *
     * @return the forks
     * @deprecated use {@link #getForksCount()} instead
     */
    @Deprecated
    public int getForks() {
        return getForksCount();
    }

    /**
     * Returns the number of all forks of this repository. This not only counts direct forks, but also forks of forks,
     * and so on.
     *
     * @return the forks
     */
    public int getForksCount() {
        return forks_count;
    }

    /**
     * Gets stargazers count.
     *
     * @return the stargazers count
     */
    public int getStargazersCount() {
        return stargazers_count;
    }

    /**
     * Is private boolean.
     *
     * @return the boolean
     */
    public boolean isPrivate() {
        return _private;
    }

    /**
     * Visibility of a repository.
     */
    public enum Visibility {
        PUBLIC,
        INTERNAL,
        PRIVATE,

        /**
         * Placeholder for unexpected data values.
         *
         * This avoids throwing exceptions during data binding or reading when the list of allowed values returned from
         * GitHub is expanded.
         *
         * Do not pass this value to any methods. If this value is returned during a request, check the log output and
         * report an issue for the missing value.
         */
        UNKNOWN;

        public static Visibility from(String value) {
            return EnumUtils.getNullableEnumOrDefault(Visibility.class, value, Visibility.UNKNOWN);
        }

        @Override
        public String toString() {
            return name().toLowerCase(Locale.ROOT);
        }
    }

    /**
     * Gets the visibility of the repository.
     *
     * @return the visibility
     */
    @Preview(NEBULA)
    public Visibility getVisibility() {
        if (visibility == null) {
            try {
                populate();
            } catch (final IOException e) {
                // Convert this to a runtime exception to avoid messy method signature
                throw new GHException("Could not populate the visibility of the repository", e);
            }
        }
        return Visibility.from(visibility);
    }

    /**
     * Is template boolean.
     *
     * @return the boolean
     */
    @Preview(BAPTISTE)
    public boolean isTemplate() {
        // isTemplate is still in preview, we do not want to retrieve it unless needed.
        if (isTemplate == null) {
            try {
                populate();
            } catch (IOException e) {
                // Convert this to a runtime exception to avoid messy method signature
                throw new GHException("Could not populate the template setting of the repository", e);
            }
            // if this somehow is not populated, set it to false;
            isTemplate = Boolean.TRUE.equals(isTemplate);
        }
        return isTemplate;
    }

    /**
     * Has downloads boolean.
     *
     * @return the boolean
     */
    public boolean hasDownloads() {
        return has_downloads;
    }

    /**
     * Has pages boolean.
     *
     * @return the boolean
     */
    public boolean hasPages() {
        return has_pages;
    }

    /**
     * Gets watchers.
     *
     * @return the watchers
     * @deprecated use {@link #getWatchersCount()} instead
     */
    @Deprecated
    public int getWatchers() {
        return getWatchersCount();
    }

    /**
     * Gets the count of watchers.
     *
     * @return the watchers
     */
    public int getWatchersCount() {
        return watchers_count;
    }

    /**
     * Gets open issue count.
     *
     * @return the open issue count
     */
    public int getOpenIssueCount() {
        return open_issues_count;
    }

    /**
     * Gets subscribers count.
     *
     * @return the subscribers count
     */
    public int getSubscribersCount() {
        return subscribers_count;
    }

    /**
     * Gets pushed at.
     *
     * @return null if the repository was never pushed at.
     */
    public Date getPushedAt() {
        return GitHubClient.parseDate(pushed_at);
    }

    /**
     * Returns the primary branch you'll configure in the "Admin &gt; Options" config page.
     *
     * @return This field is null until the user explicitly configures the default branch.
     */
    public String getDefaultBranch() {
        return default_branch;
    }

    /**
     * Gets default branch.
     *
     * Name is an artifact of when "master" was the most common default.
     *
     * @return the default branch
     * @deprecated Renamed to {@link #getDefaultBranch()}
     */
    @Deprecated
    public String getMasterBranch() {
        return default_branch;
    }

    /**
     * Gets size.
     *
     * @return the size
     */
    public int getSize() {
        return size;
    }

    /**
     * Affiliation of a repository collaborator
     */
    public enum CollaboratorAffiliation {
        ALL, DIRECT, OUTSIDE
    }

    /**
     * Gets the collaborators on this repository. This set always appear to include the owner.
     *
     * @return the collaborators
     * @throws IOException
     *             the io exception
     */
    @WithBridgeMethods(Set.class)
    public GHPersonSet<GHUser> getCollaborators() throws IOException {
        return new GHPersonSet<GHUser>(listCollaborators().toList());
    }

    /**
     * Lists up the collaborators on this repository.
     *
     * @return Users paged iterable
     * @throws IOException
     *             the io exception
     */
    public PagedIterable<GHUser> listCollaborators() throws IOException {
        return listUsers("collaborators");
    }

    /**
     * Lists up the collaborators on this repository.
     *
     * @param affiliation
     *            Filter users by affiliation
     * @return Users paged iterable
     * @throws IOException
     *             the io exception
     */
    public PagedIterable<GHUser> listCollaborators(CollaboratorAffiliation affiliation) throws IOException {
        return listUsers(root().createRequest().with("affiliation", affiliation), "collaborators");
    }

    /**
     * Lists all
     * <a href="https://help.github.com/articles/assigning-issues-and-pull-requests-to-other-github-users/">the
     * available assignees</a> to which issues may be assigned.
     *
     * @return the paged iterable
     * @throws IOException
     *             the io exception
     */
    public PagedIterable<GHUser> listAssignees() throws IOException {
        return listUsers("assignees");
    }

    /**
     * Checks if the given user is an assignee for this repository.
     *
     * @param u
     *            the u
     * @return the boolean
     * @throws IOException
     *             the io exception
     */
    public boolean hasAssignee(GHUser u) throws IOException {
        return root().createRequest().withUrlPath(getApiTailUrl("assignees/" + u.getLogin())).fetchHttpStatusCode()
                / 100 == 2;
    }

    /**
     * Gets the names of the collaborators on this repository. This method deviates from the principle of this library
     * but it works a lot faster than {@link #getCollaborators()}.
     *
     * @return the collaborator names
     * @throws IOException
     *             the io exception
     */
    public Set<String> getCollaboratorNames() throws IOException {
        Set<String> r = new HashSet<>();
        // no initializer - we just want to the logins
        PagedIterable<GHUser> users = root().createRequest()
                .withUrlPath(getApiTailUrl("collaborators"))
                .toIterable(GHUser[].class, null);
        for (GHUser u : users.toArray()) {
            r.add(u.login);
        }
        return r;
    }

    /**
     * Gets the names of the collaborators on this repository. This method deviates from the principle of this library
     * but it works a lot faster than {@link #getCollaborators()}.
     *
     * @param affiliation
     *            Filter users by affiliation
     * @return the collaborator names
     * @throws IOException
     *             the io exception
     */
    public Set<String> getCollaboratorNames(CollaboratorAffiliation affiliation) throws IOException {
        Set<String> r = new HashSet<>();
        // no initializer - we just want to the logins
        PagedIterable<GHUser> users = root().createRequest()
                .withUrlPath(getApiTailUrl("collaborators"))
                .with("affiliation", affiliation)
                .toIterable(GHUser[].class, null);
        for (GHUser u : users.toArray()) {
            r.add(u.login);
        }
        return r;
    }

    /**
     * Obtain permission for a given user in this repository.
     *
     * @param user
     *            a {@link GHUser#getLogin}
     * @return the permission
     * @throws IOException
     *             the io exception
     */
    public GHPermissionType getPermission(String user) throws IOException {
        GHPermission perm = root().createRequest()
                .withUrlPath(getApiTailUrl("collaborators/" + user + "/permission"))
                .fetch(GHPermission.class);
        return perm.getPermissionType();
    }

    /**
     * Obtain permission for a given user in this repository.
     *
     * @param u
     *            the user
     * @return the permission
     * @throws IOException
     *             the io exception
     */
    public GHPermissionType getPermission(GHUser u) throws IOException {
        return getPermission(u.getLogin());
    }

    /**
     * Check if a user has at least the given permission in this repository.
     *
     * @param user
     *            a {@link GHUser#getLogin}
     * @param permission
     *            the permission to check
     * @return true if the user has at least this permission level
     * @throws IOException
     *             the io exception
     */
    public boolean hasPermission(String user, GHPermissionType permission) throws IOException {
        return getPermission(user).implies(permission);
    }

    /**
     * Check if a user has at least the given permission in this repository.
     *
     * @param user
     *            the user
     * @param permission
     *            the permission to check
     * @return true if the user has at least this permission level
     * @throws IOException
     *             the io exception
     */
    public boolean hasPermission(GHUser user, GHPermissionType permission) throws IOException {
        return hasPermission(user.getLogin(), permission);
    }

    /**
     * If this repository belongs to an organization, return a set of teams.
     *
     * @return the teams
     * @throws IOException
     *             the io exception
     */
    public Set<GHTeam> getTeams() throws IOException {
        GHOrganization org = root().getOrganization(getOwnerName());
        return root().createRequest()
                .withUrlPath(getApiTailUrl("teams"))
                .toIterable(GHTeam[].class, item -> item.wrapUp(org))
                .toSet();
    }

    /**
     * Add collaborators.
     *
     * @param permission
     *            the permission level
     * @param users
     *            the users
     * @throws IOException
     *             the io exception
     * @deprecated #addCollaborators(GHOrganization.RolePermission, GHUser)
     */
    @Deprecated
    public void addCollaborators(GHOrganization.Permission permission, GHUser... users) throws IOException {
        addCollaborators(asList(users), permission);
    }

    /**
     * Add collaborators.
     *
     * @param permission
     *            the permission level
     * @param users
     *            the users
     *
     * @throws IOException
     *             the io exception
     */
    public void addCollaborators(GHOrganization.RepositoryRole permission, GHUser... users) throws IOException {
        addCollaborators(asList(users), permission);
    }

    /**
     * Add collaborators.
     *
     * @param users
     *            the users
     * @throws IOException
     *             the io exception
     */
    public void addCollaborators(GHUser... users) throws IOException {
        addCollaborators(asList(users));
    }

    /**
     * Add collaborators.
     *
     * @param users
     *            the users
     * @throws IOException
     *             the io exception
     */
    public void addCollaborators(Collection<GHUser> users) throws IOException {
        modifyCollaborators(users, "PUT", null);
    }

    /**
     * Add collaborators.
     *
     * @param users
     *            the users
     * @param permission
     *            the permission level
     * @throws IOException
     *             the io exception
     * @deprecated #addCollaborators(Collection, GHOrganization.RolePermission)
     */
    @Deprecated
    public void addCollaborators(Collection<GHUser> users, GHOrganization.Permission permission) throws IOException {
        modifyCollaborators(users, "PUT", GHOrganization.RepositoryRole.from(permission));
    }

    /**
     * Add collaborators.
     *
     * @param users
     *            the users
     * @param permission
     *            the permission level
     * @throws IOException
     *             the io exception
     */
    public void addCollaborators(Collection<GHUser> users, GHOrganization.RepositoryRole permission)
            throws IOException {
        modifyCollaborators(users, "PUT", permission);
    }

    /**
     * Remove collaborators.
     *
     * @param users
     *            the users
     * @throws IOException
     *             the io exception
     */
    public void removeCollaborators(GHUser... users) throws IOException {
        removeCollaborators(asList(users));
    }

    /**
     * Remove collaborators.
     *
     * @param users
     *            the users
     * @throws IOException
     *             the io exception
     */
    public void removeCollaborators(Collection<GHUser> users) throws IOException {
        modifyCollaborators(users, "DELETE", null);
    }

    private void modifyCollaborators(@NonNull Collection<GHUser> users,
            @NonNull String method,
            @CheckForNull GHOrganization.RepositoryRole permission) throws IOException {
        Requester requester = root().createRequest().method(method);
        if (permission != null) {
            requester = requester.with("permission", permission.toString()).inBody();
        }

        // Make sure that the users collection doesn't have any duplicates
        for (GHUser user : new LinkedHashSet<>(users)) {
            requester.withUrlPath(getApiTailUrl("collaborators/" + user.getLogin())).send();
        }
    }

    /**
     * Sets email service hook.
     *
     * @param address
     *            the address
     * @throws IOException
     *             the io exception
     */
    public void setEmailServiceHook(String address) throws IOException {
        Map<String, String> config = new HashMap<>();
        config.put("address", address);
        root().createRequest()
                .method("POST")
                .with("name", "email")
                .with("config", config)
                .with("active", true)
                .withUrlPath(getApiTailUrl("hooks"))
                .send();
    }

    /**
     * Enables or disables the issue tracker for this repository.
     *
     * @param v
     *            the v
     * @throws IOException
     *             the io exception
     */
    public void enableIssueTracker(boolean v) throws IOException {
        set().issues(v);
    }

    /**
     * Enables or disables projects for this repository.
     *
     * @param v
     *            the v
     * @throws IOException
     *             the io exception
     */
    public void enableProjects(boolean v) throws IOException {
        set().projects(v);
    }

    /**
     * Enables or disables Wiki for this repository.
     *
     * @param v
     *            the v
     * @throws IOException
     *             the io exception
     */
    public void enableWiki(boolean v) throws IOException {
        set().wiki(v);
    }

    /**
     * Enable downloads.
     *
     * @param v
     *            the v
     * @throws IOException
     *             the io exception
     */
    public void enableDownloads(boolean v) throws IOException {
        set().downloads(v);
    }

    /**
     * Rename this repository.
     *
     * @param name
     *            the name
     * @throws IOException
     *             the io exception
     */
    public void renameTo(String name) throws IOException {
        set().name(name);
    }

    /**
     * Sets description.
     *
     * @param value
     *            the value
     * @throws IOException
     *             the io exception
     */
    public void setDescription(String value) throws IOException {
        set().description(value);
    }

    /**
     * Sets homepage.
     *
     * @param value
     *            the value
     * @throws IOException
     *             the io exception
     */
    public void setHomepage(String value) throws IOException {
        set().homepage(value);
    }

    /**
     * Sets default branch.
     *
     * @param value
     *            the value
     * @throws IOException
     *             the io exception
     */
    public void setDefaultBranch(String value) throws IOException {
        set().defaultBranch(value);
    }

    /**
     * Sets private.
     *
     * @param value
     *            the value
     * @throws IOException
     *             the io exception
     */
    public void setPrivate(boolean value) throws IOException {
        set().private_(value);
    }

    /**
     * Sets visibility.
     *
     * @param value
     *            the value
     * @throws IOException
     *             the io exception
     */
    @Preview(NEBULA)
    public void setVisibility(final Visibility value) throws IOException {
        root().createRequest()
                .method("PATCH")
                .withPreview(NEBULA)
                .with("name", name)
                .with("visibility", value)
                .withUrlPath(getApiTailUrl(""))
                .send();
    }

    /**
     * Allow squash merge.
     *
     * @param value
     *            the value
     * @throws IOException
     *             the io exception
     */
    public void allowSquashMerge(boolean value) throws IOException {
        set().allowSquashMerge(value);
    }

    /**
     * Allow merge commit.
     *
     * @param value
     *            the value
     * @throws IOException
     *             the io exception
     */
    public void allowMergeCommit(boolean value) throws IOException {
        set().allowMergeCommit(value);
    }

    /**
     * Allow rebase merge.
     *
     * @param value
     *            the value
     * @throws IOException
     *             the io exception
     */
    public void allowRebaseMerge(boolean value) throws IOException {
        set().allowRebaseMerge(value);
    }

    /**
     * After pull requests are merged, you can have head branches deleted automatically.
     *
     * @param value
     *            the value
     * @throws IOException
     *             the io exception
     */
    public void deleteBranchOnMerge(boolean value) throws IOException {
        set().deleteBranchOnMerge(value);
    }

    /**
     * Deletes this repository.
     *
     * @throws IOException
     *             the io exception
     */
    public void delete() throws IOException {
        try {
            root().createRequest().method("DELETE").withUrlPath(getApiTailUrl("")).send();
        } catch (FileNotFoundException x) {
            throw (FileNotFoundException) new FileNotFoundException("Failed to delete " + getOwnerName() + "/" + name
                    + "; might not exist, or you might need the delete_repo scope in your token: http://stackoverflow.com/a/19327004/12916")
                            .initCause(x);
        }
    }

    /**
     * Will archive and this repository as read-only. When a repository is archived, any operation that can change its
     * state is forbidden. This applies symmetrically if trying to unarchive it.
     *
     * <p>
     * When you try to do any operation that modifies a read-only repository, it returns the response:
     *
     * <pre>
     * org.kohsuke.github.HttpException: {
     *     "message":"Repository was archived so is read-only.",
     *     "documentation_url":"https://developer.github.com/v3/repos/#edit"
     * }
     * </pre>
     *
     * @throws IOException
     *             In case of any networking error or error from the server.
     */
    public void archive() throws IOException {
        set().archive();
        // Generally would not update this record,
        // but doing so here since this will result in any other update actions failing
        archived = true;
    }

    /**
     * Creates a builder that can be used to bulk update repository settings.
     *
     * @return the repository updater
     */
    public Updater update() {
        return new Updater(this);
    }

    /**
     * Creates a builder that can be used to bulk update repository settings.
     *
     * @return the repository updater
     */
    public Setter set() {
        return new Setter(this);
    }

    /**
     * Sort orders for listing forks
     */
    public enum ForkSort {
        NEWEST, OLDEST, STARGAZERS
    }

    /**
     * Lists all the direct forks of this repository, sorted by github api default, currently {@link ForkSort#NEWEST
     * ForkSort.NEWEST}*.
     *
     * @return the paged iterable
     */
    public PagedIterable<GHRepository> listForks() {
        return listForks(null);
    }

    /**
     * Lists all the direct forks of this repository, sorted by the given sort order.
     *
     * @param sort
     *            the sort order. If null, defaults to github api default, currently {@link ForkSort#NEWEST
     *            ForkSort.NEWEST}.
     * @return the paged iterable
     */
    public PagedIterable<GHRepository> listForks(final ForkSort sort) {
        return root().createRequest()
                .with("sort", sort)
                .withUrlPath(getApiTailUrl("forks"))
                .toIterable(GHRepository[].class, null);
    }

    /**
     * Forks this repository as your repository.
     *
     * @return Newly forked repository that belong to you.
     * @throws IOException
     *             the io exception
     */
    public GHRepository fork() throws IOException {
        root().createRequest().method("POST").withUrlPath(getApiTailUrl("forks")).send();

        // this API is asynchronous. we need to wait for a bit
        for (int i = 0; i < 10; i++) {
            GHRepository r = root().getMyself().getRepository(name);
            if (r != null) {
                return r;
            }
            try {
                Thread.sleep(3000);
            } catch (InterruptedException e) {
                throw (IOException) new InterruptedIOException().initCause(e);
            }
        }
        throw new IOException(this + " was forked but can't find the new repository");
    }

    /**
     * Forks this repository into an organization.
     *
     * @param org
     *            the org
     * @return Newly forked repository that belong to you.
     * @throws IOException
     *             the io exception
     */
    public GHRepository forkTo(GHOrganization org) throws IOException {
        root().createRequest()
                .method("POST")
                .with("organization", org.getLogin())
                .withUrlPath(getApiTailUrl("forks"))
                .send();

        // this API is asynchronous. we need to wait for a bit
        for (int i = 0; i < 10; i++) {
            GHRepository r = org.getRepository(name);
            if (r != null) {
                return r;
            }
            try {
                Thread.sleep(3000);
            } catch (InterruptedException e) {
                throw (IOException) new InterruptedIOException().initCause(e);
            }
        }
        throw new IOException(this + " was forked into " + org.getLogin() + " but can't find the new repository");
    }

    /**
     * Retrieves a specified pull request.
     *
     * @param i
     *            the
     * @return the pull request
     * @throws IOException
     *             the io exception
     */
    public GHPullRequest getPullRequest(int i) throws IOException {
        return root().createRequest()
                .withPreview(SHADOW_CAT)
                .withUrlPath(getApiTailUrl("pulls/" + i))
                .fetch(GHPullRequest.class)
                .wrapUp(this);
    }

    /**
     * Retrieves all the pull requests of a particular state.
     *
     * @param state
     *            the state
     * @return the pull requests
     * @throws IOException
     *             the io exception
     * @see #listPullRequests(GHIssueState) #listPullRequests(GHIssueState)
     */
    public List<GHPullRequest> getPullRequests(GHIssueState state) throws IOException {
        return queryPullRequests().state(state).list().toList();
    }

    /**
     * Retrieves all the pull requests of a particular state.
     *
     * @param state
     *            the state
     * @return the paged iterable
     * @deprecated Use {@link #queryPullRequests()}
     */
    @Deprecated
    public PagedIterable<GHPullRequest> listPullRequests(GHIssueState state) {
        return queryPullRequests().state(state).list();
    }

    /**
     * Retrieves pull requests.
     *
     * @return the gh pull request query builder
     */
    public GHPullRequestQueryBuilder queryPullRequests() {
        return new GHPullRequestQueryBuilder(this);
    }

    /**
     * Creates a new pull request.
     *
     * @param title
     *            Required. The title of the pull request.
     * @param head
     *            Required. The name of the branch where your changes are implemented. For cross-repository pull
     *            requests in the same network, namespace head with a user like this: username:branch.
     * @param base
     *            Required. The name of the branch you want your changes pulled into. This should be an existing branch
     *            on the current repository.
     * @param body
     *            The contents of the pull request. This is the markdown description of a pull request.
     * @return the gh pull request
     * @throws IOException
     *             the io exception
     */
    public GHPullRequest createPullRequest(String title, String head, String base, String body) throws IOException {
        return createPullRequest(title, head, base, body, true);
    }

    /**
     * Creates a new pull request. Maintainer's permissions aware.
     *
     * @param title
     *            Required. The title of the pull request.
     * @param head
     *            Required. The name of the branch where your changes are implemented. For cross-repository pull
     *            requests in the same network, namespace head with a user like this: username:branch.
     * @param base
     *            Required. The name of the branch you want your changes pulled into. This should be an existing branch
     *            on the current repository.
     * @param body
     *            The contents of the pull request. This is the markdown description of a pull request.
     * @param maintainerCanModify
     *            Indicates whether maintainers can modify the pull request.
     * @return the gh pull request
     * @throws IOException
     *             the io exception
     */
    public GHPullRequest createPullRequest(String title,
            String head,
            String base,
            String body,
            boolean maintainerCanModify) throws IOException {
        return createPullRequest(title, head, base, body, maintainerCanModify, false);
    }

    /**
     * Creates a new pull request. Maintainer's permissions and draft aware.
     *
     * @param title
     *            Required. The title of the pull request.
     * @param head
     *            Required. The name of the branch where your changes are implemented. For cross-repository pull
     *            requests in the same network, namespace head with a user like this: username:branch.
     * @param base
     *            Required. The name of the branch you want your changes pulled into. This should be an existing branch
     *            on the current repository.
     * @param body
     *            The contents of the pull request. This is the markdown description of a pull request.
     * @param maintainerCanModify
     *            Indicates whether maintainers can modify the pull request.
     * @param draft
     *            Indicates whether to create a draft pull request or not.
     * @return the gh pull request
     * @throws IOException
     *             the io exception
     */
    public GHPullRequest createPullRequest(String title,
            String head,
            String base,
            String body,
            boolean maintainerCanModify,
            boolean draft) throws IOException {
        return root().createRequest()
                .method("POST")
                .withPreview(SHADOW_CAT)
                .with("title", title)
                .with("head", head)
                .with("base", base)
                .with("body", body)
                .with("maintainer_can_modify", maintainerCanModify)
                .with("draft", draft)
                .withUrlPath(getApiTailUrl("pulls"))
                .fetch(GHPullRequest.class)
                .wrapUp(this);
    }

    /**
     * Retrieves the currently configured hooks.
     *
     * @return the hooks
     * @throws IOException
     *             the io exception
     */
    public List<GHHook> getHooks() throws IOException {
        return GHHooks.repoContext(this, owner).getHooks();
    }

    /**
     * Gets hook.
     *
     * @param id
     *            the id
     * @return the hook
     * @throws IOException
     *             the io exception
     */
    public GHHook getHook(int id) throws IOException {
        return GHHooks.repoContext(this, owner).getHook(id);
    }

    /**
     * Deletes hook.
     *
     * @param id
     *            the id
     * @throws IOException
     *             the io exception
     */
    public void deleteHook(int id) throws IOException {
        GHHooks.repoContext(this, owner).deleteHook(id);
    }

    /**
     * Gets a single repository secret without revealing its encrypted value.
     * https://docs.github.com/en/rest/actions/secrets#get-a-repository-secret
     *
     * @param secretName
     *            the name of the secret
     * @return the secret
     * @throws IOException
     *             the io exception
     */
    public GHRepoSecret getSecret(String secretName) throws IOException {
        return root().createRequest()
                .withUrlPath(String.format("/repos/%s/%s/actions/secrets/%s", getOwnerName(), name, secretName))
                .fetch(GHRepoSecret.class)
                .wrapUp(this);
    }

    /**
     * Creates or updates a repository secret with an encrypted value.
     * "https://docs.github.com/rest/reference/actions#create-or-update-a-repository-secret"
     *
     * @param secretName
     *            the name of the secret
     * @param encryptedValue
     *            The encrypted value for this secret
     * @param publicKeyId
     *            The id of the Public Key used to encrypt this secret
     * @throws IOException
     *             the io exception
     */
    public void createSecret(String secretName, String encryptedValue, String publicKeyId) throws IOException {
        root().createRequest()
                .method("PUT")
                .with("encrypted_value", encryptedValue)
                .with("key_id", publicKeyId)
                .withUrlPath(String.format("/repos/%s/%s/actions/secrets/%s", getOwnerName(), name, secretName))
                .send();
    }

    /**
     * Deletes a secret in a repository using the secret name.
     * "https://docs.github.com/rest/reference/actions#delete-a-repository-secret"
     *
     * @param secretName
     *            the name of the secret
     * @throws IOException
     *             the io exception
     */
    public void deleteSecret(String secretName) throws IOException {
        root().createRequest()
                .method("DELETE")
                .withUrlPath(String.format("/repos/%s/%s/actions/secrets/%s", getOwnerName(), name, secretName))
                .send();
    }

    /**
     * Sets {@link #getCompare(String, String)} to return a {@link GHCompare} that uses a paginated commit list instead
     * of limiting to 250 results.
     *
     * By default, {@link GHCompare} returns all commits in the comparison as part of the request, limited to 250
     * results. More recently GitHub added the ability to return the commits as a paginated query allowing for more than
     * 250 results.
     *
     * @param value
     *            true if you want commits returned in paginated form.
     */
    public void setCompareUsePaginatedCommits(boolean value) {
        compareUsePaginatedCommits = value;
    }

    /**
     * Gets a comparison between 2 points in the repository. This would be similar to calling
     * <code>git log id1...id2</code> against a local repository.
     *
     * @param id1
     *            an identifier for the first point to compare from, this can be a sha1 ID (for a commit, tag etc) or a
     *            direct tag name
     * @param id2
     *            an identifier for the second point to compare to. Can be the same as the first point.
     * @return the comparison output
     * @throws IOException
     *             on failure communicating with GitHub
     */
    public GHCompare getCompare(String id1, String id2) throws IOException {
        final Requester requester = root().createRequest()
                .withUrlPath(getApiTailUrl(String.format("compare/%s...%s", id1, id2)));

        if (compareUsePaginatedCommits) {
            requester.with("per_page", 1).with("page", 1);
        }
        requester.injectMappingValue("GHCompare_usePaginatedCommits", compareUsePaginatedCommits);
        GHCompare compare = requester.fetch(GHCompare.class);
        return compare.lateBind(this);
    }

    /**
     * Gets compare.
     *
     * @param id1
     *            the id 1
     * @param id2
     *            the id 2
     * @return the compare
     * @throws IOException
     *             the io exception
     */
    public GHCompare getCompare(GHCommit id1, GHCommit id2) throws IOException {
        return getCompare(id1.getSHA1(), id2.getSHA1());
    }

    /**
     * Gets compare.
     *
     * @param id1
     *            the id 1
     * @param id2
     *            the id 2
     * @return the compare
     * @throws IOException
     *             the io exception
     */
    public GHCompare getCompare(GHBranch id1, GHBranch id2) throws IOException {

        GHRepository owner1 = id1.getOwner();
        GHRepository owner2 = id2.getOwner();

        // If the owner of the branches is different, we have a cross-fork compare.
        if (owner1 != null && owner2 != null) {
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
     *
     * @return an array of GHRef elements coresponding with the refs in the remote repository.
     * @throws IOException
     *             on failure communicating with GitHub
     */
    public GHRef[] getRefs() throws IOException {
        return listRefs().toArray();
    }

    /**
     * Retrieves all refs for the github repository.
     *
     * @return paged iterable of all refs
     * @throws IOException
     *             on failure communicating with GitHub, potentially due to an invalid ref type being requested
     */
    public PagedIterable<GHRef> listRefs() throws IOException {
        return listRefs("");
    }

    /**
     * Retrieves all refs of the given type for the current GitHub repository.
     *
     * @param refType
     *            the type of reg to search for e.g. <code>tags</code> or <code>commits</code>
     * @return an array of all refs matching the request type
     * @throws IOException
     *             on failure communicating with GitHub, potentially due to an invalid ref type being requested
     */
    public GHRef[] getRefs(String refType) throws IOException {
        return listRefs(refType).toArray();
    }

    /**
     * Retrieves all refs of the given type for the current GitHub repository.
     *
     * @param refType
     *            the type of reg to search for e.g. <code>tags</code> or <code>commits</code>
     * @return paged iterable of all refs of the specified type
     * @throws IOException
     *             on failure communicating with GitHub, potentially due to an invalid ref type being requested
     */
    public PagedIterable<GHRef> listRefs(String refType) throws IOException {
        return GHRef.readMatching(this, refType);
    }

    /**
     * Retrive a ref of the given type for the current GitHub repository.
     *
     * @param refName
     *            eg: heads/branch
     * @return refs matching the request type
     * @throws IOException
     *             on failure communicating with GitHub, potentially due to an invalid ref type being requested
     */
    public GHRef getRef(String refName) throws IOException {
        return GHRef.read(this, refName);
    }

    /**
     * Returns the <strong>annotated</strong> tag object. Only valid if the {@link GHRef#getObject()} has a
     * {@link GHRef.GHObject#getType()} of {@code tag}.
     *
     * @param sha
     *            the sha of the tag object
     * @return the annotated tag object
     * @throws IOException
     *             the io exception
     */
    public GHTagObject getTagObject(String sha) throws IOException {
        return root().createRequest().withUrlPath(getApiTailUrl("git/tags/" + sha)).fetch(GHTagObject.class).wrap(this);
    }

    /**
     * Retrive a tree of the given type for the current GitHub repository.
     *
     * @param sha
     *            sha number or branch name ex: "main"
     * @return refs matching the request type
     * @throws IOException
     *             on failure communicating with GitHub, potentially due to an invalid tree type being requested
     */
    public GHTree getTree(String sha) throws IOException {
        String url = String.format("/repos/%s/%s/git/trees/%s", getOwnerName(), name, sha);
        return root().createRequest().withUrlPath(url).fetch(GHTree.class).wrap(this);
    }

    /**
     * Create tree gh tree builder.
     *
     * @return the gh tree builder
     */
    public GHTreeBuilder createTree() {
        return new GHTreeBuilder(this);
    }

    /**
     * Retrieves the tree for the current GitHub repository, recursively as described in here:
     * https://developer.github.com/v3/git/trees/#get-a-tree-recursively
     *
     * @param sha
     *            sha number or branch name ex: "main"
     * @param recursive
     *            use 1
     * @return the tree recursive
     * @throws IOException
     *             on failure communicating with GitHub, potentially due to an invalid tree type being requested
     */
    public GHTree getTreeRecursive(String sha, int recursive) throws IOException {
        String url = String.format("/repos/%s/%s/git/trees/%s", getOwnerName(), name, sha);
        return root().createRequest().with("recursive", recursive).withUrlPath(url).fetch(GHTree.class).wrap(this);
    }

    /**
     * Obtains the metadata &amp; the content of a blob.
     *
     * <p>
     * This method retrieves the whole content in memory, so beware when you are dealing with large BLOB.
     *
     * @param blobSha
     *            the blob sha
     * @return the blob
     * @throws IOException
     *             the io exception
     * @see <a href="https://developer.github.com/v3/git/blobs/#get-a-blob">Get a blob</a>
     * @see #readBlob(String) #readBlob(String)
     */
    public GHBlob getBlob(String blobSha) throws IOException {
        String target = getApiTailUrl("git/blobs/" + blobSha);
        return root().createRequest().withUrlPath(target).fetch(GHBlob.class);
    }

    /**
     * Create blob gh blob builder.
     *
     * @return the gh blob builder
     */
    public GHBlobBuilder createBlob() {
        return new GHBlobBuilder(this);
    }

    /**
     * Reads the content of a blob as a stream for better efficiency.
     *
     * @param blobSha
     *            the blob sha
     * @return the input stream
     * @throws IOException
     *             the io exception
     * @see <a href="https://developer.github.com/v3/git/blobs/#get-a-blob">Get a blob</a>
     * @see #getBlob(String) #getBlob(String)
     */
    public InputStream readBlob(String blobSha) throws IOException {
        String target = getApiTailUrl("git/blobs/" + blobSha);

        // https://developer.github.com/v3/media/ describes this media type
        return root().createRequest()
                .withHeader("Accept", "application/vnd.github.v3.raw")
                .withUrlPath(target)
                .fetchStream(Requester::copyInputStream);
    }

    /**
     * Gets a commit object in this repository.
     *
     * @param sha1
     *            the sha 1
     * @return the commit
     * @throws IOException
     *             the io exception
     */
    public GHCommit getCommit(String sha1) throws IOException {
        GHCommit c = commits.get(sha1);
        if (c == null) {
            c = root().createRequest()
                    .withUrlPath(String.format("/repos/%s/%s/commits/%s", getOwnerName(), name, sha1))
                    .fetch(GHCommit.class)
                    .wrapUp(this);
            commits.put(sha1, c);
        }
        return c;
    }

    /**
     * Create commit gh commit builder.
     *
     * @return the gh commit builder
     */
    public GHCommitBuilder createCommit() {
        return new GHCommitBuilder(this);
    }

    /**
     * Lists all the commits.
     *
     * @return the paged iterable
     */
    public PagedIterable<GHCommit> listCommits() {
        return root().createRequest()
                .withUrlPath(String.format("/repos/%s/%s/commits", getOwnerName(), name))
                .toIterable(GHCommit[].class, item -> item.wrapUp(this));
    }

    /**
     * Search commits by specifying filters through a builder pattern.
     *
     * @return the gh commit query builder
     */
    public GHCommitQueryBuilder queryCommits() {
        return new GHCommitQueryBuilder(this);
    }

    /**
     * Lists up all the commit comments in this repository.
     *
     * @return the paged iterable
     */
    public PagedIterable<GHCommitComment> listCommitComments() {
        return root().createRequest()
                .withUrlPath(String.format("/repos/%s/%s/comments", getOwnerName(), name))
                .toIterable(GHCommitComment[].class, item -> item.wrap(this));
    }

    /**
     * Lists all comments on a specific commit.
     *
     * @param commitSha
     *            the hash of the commit
     *
     * @return the paged iterable
     */
    public PagedIterable<GHCommitComment> listCommitComments(String commitSha) {
        return root().createRequest()
                .withUrlPath(String.format("/repos/%s/%s/commits/%s/comments", getOwnerName(), name, commitSha))
                .toIterable(GHCommitComment[].class, item -> item.wrap(this));
    }

    /**
     * Gets the basic license details for the repository.
     * <p>
     *
     * @return null if there's no license.
     * @throws IOException
     *             as usual but also if you don't use the preview connector
     */
    public GHLicense getLicense() throws IOException {
        GHContentWithLicense lic = getLicenseContent_();
        return lic != null ? lic.license : null;
    }

    /**
     * Retrieves the contents of the repository's license file - makes an additional API call
     * <p>
     *
     * @return details regarding the license contents, or null if there's no license.
     * @throws IOException
     *             as usual but also if you don't use the preview connector
     */
    public GHContent getLicenseContent() throws IOException {
        return getLicenseContent_();
    }

    private GHContentWithLicense getLicenseContent_() throws IOException {
        try {
            return root().createRequest()
                    .withUrlPath(getApiTailUrl("license"))
                    .fetch(GHContentWithLicense.class)
                    .wrap(this);
        } catch (FileNotFoundException e) {
            return null;
        }
    }

    /**
     * /** Lists all the commit statuses attached to the given commit, newer ones first.
     *
     * @param sha1
     *            the sha 1
     * @return the paged iterable
     * @throws IOException
     *             the io exception
     */
    public PagedIterable<GHCommitStatus> listCommitStatuses(final String sha1) throws IOException {
        return root().createRequest()
                .withUrlPath(String.format("/repos/%s/%s/statuses/%s", getOwnerName(), name, sha1))
                .toIterable(GHCommitStatus[].class, null);
    }

    /**
     * Gets the last status of this commit, which is what gets shown in the UI.
     *
     * @param sha1
     *            the sha 1
     * @return the last commit status
     * @throws IOException
     *             the io exception
     */
    public GHCommitStatus getLastCommitStatus(String sha1) throws IOException {
        List<GHCommitStatus> v = listCommitStatuses(sha1).toList();
        return v.isEmpty() ? null : v.get(0);
    }

    /**
     * Gets check runs for given ref.
     *
     * @param ref
     *            ref
     * @return check runs for given ref
     * @throws IOException
     *             the io exception
     * @see <a href="https://developer.github.com/v3/checks/runs/#list-check-runs-for-a-specific-ref">List check runs
     *      for a specific ref</a>
     */
    @Preview(ANTIOPE)
    public PagedIterable<GHCheckRun> getCheckRuns(String ref) throws IOException {
        GitHubRequest request = root().createRequest()
                .withUrlPath(String.format("/repos/%s/%s/commits/%s/check-runs", getOwnerName(), name, ref))
                .withPreview(ANTIOPE)
                .build();
        return new GHCheckRunsIterable(this, request);
    }

    /**
     * Creates a commit status
     *
     * @param sha1
     *            the sha 1
     * @param state
     *            the state
     * @param targetUrl
     *            Optional parameter that points to the URL that has more details.
     * @param description
     *            Optional short description.
     * @param context
     *            Optinal commit status context.
     * @return the gh commit status
     * @throws IOException
     *             the io exception
     */
    public GHCommitStatus createCommitStatus(String sha1,
            GHCommitState state,
            String targetUrl,
            String description,
            String context) throws IOException {
        return root().createRequest()
                .method("POST")
                .with("state", state)
                .with("target_url", targetUrl)
                .with("description", description)
                .with("context", context)
                .withUrlPath(String.format("/repos/%s/%s/statuses/%s", getOwnerName(), this.name, sha1))
                .fetch(GHCommitStatus.class);
    }

    /**
     * Create commit status gh commit status.
     *
     * @param sha1
     *            the sha 1
     * @param state
     *            the state
     * @param targetUrl
     *            the target url
     * @param description
     *            the description
     * @return the gh commit status
     * @throws IOException
     *             the io exception
     * @see #createCommitStatus(String, GHCommitState, String, String, String) #createCommitStatus(String,
     *      GHCommitState,String,String,String)
     */
    public GHCommitStatus createCommitStatus(String sha1, GHCommitState state, String targetUrl, String description)
            throws IOException {
        return createCommitStatus(sha1, state, targetUrl, description, null);
    }

    /**
     * Creates a check run for a commit.
     *
     * @param name
     *            an identifier for the run
     * @param headSHA
     *            the commit hash
     * @return a builder which you should customize, then call {@link GHCheckRunBuilder#create}
     */
    @Preview(ANTIOPE)
    public @NonNull GHCheckRunBuilder createCheckRun(@NonNull String name, @NonNull String headSHA) {
        return new GHCheckRunBuilder(this, name, headSHA);
    }

    /**
     * Updates an existing check run.
     *
     * @param checkId
     *            the existing checkId
     * @return a builder which you should customize, then call {@link GHCheckRunBuilder#create}
     */
    @Preview(BAPTISTE)
    public @NonNull GHCheckRunBuilder updateCheckRun(long checkId) {
        return new GHCheckRunBuilder(this, checkId);
    }

    /**
     * Lists repository events.
     *
     * @return the paged iterable
     * @throws IOException
     *             the io exception
     */
    public PagedIterable<GHEventInfo> listEvents() throws IOException {
        return root().createRequest()
                .withUrlPath(String.format("/repos/%s/%s/events", getOwnerName(), name))
                .toIterable(GHEventInfo[].class, null);
    }

    /**
     * Lists labels in this repository.
     * <p>
     * https://developer.github.com/v3/issues/labels/#list-all-labels-for-this-repository
     *
     * @return the paged iterable
     * @throws IOException
     *             the io exception
     */
    public PagedIterable<GHLabel> listLabels() throws IOException {
        return GHLabel.readAll(this);
    }

    /**
     * Gets label.
     *
     * @param name
     *            the name
     * @return the label
     * @throws IOException
     *             the io exception
     */
    public GHLabel getLabel(String name) throws IOException {
        return GHLabel.read(this, name);
    }

    /**
     * Create label gh label.
     *
     * @param name
     *            the name
     * @param color
     *            the color
     * @return the gh label
     * @throws IOException
     *             the io exception
     */
    public GHLabel createLabel(String name, String color) throws IOException {
        return GHLabel.create(this).name(name).color(color).description("").done();
    }

    /**
     * Description is still in preview.
     *
     * @param name
     *            the name
     * @param color
     *            the color
     * @param description
     *            the description
     * @return gh label
     * @throws IOException
     *             the io exception
     */
    public GHLabel createLabel(String name, String color, String description) throws IOException {
        return GHLabel.create(this).name(name).color(color).description(description).done();
    }

    /**
     * Lists all the invitations.
     *
     * @return the paged iterable
     */
    public PagedIterable<GHInvitation> listInvitations() {
        return root().createRequest()
                .withUrlPath(String.format("/repos/%s/%s/invitations", getOwnerName(), name))
                .toIterable(GHInvitation[].class, null);
    }

    /**
     * Lists all the subscribers (aka watchers.)
     * <p>
     * https://developer.github.com/v3/activity/watching/
     *
     * @return the paged iterable
     */
    public PagedIterable<GHUser> listSubscribers() {
        return listUsers("subscribers");
    }

    /**
     * Lists all the users who have starred this repo based on the old version of the API. For additional information,
     * like date when the repository was starred, see {@link #listStargazers2()}
     *
     * @return the paged iterable
     */
    public PagedIterable<GHUser> listStargazers() {
        return listUsers("stargazers");
    }

    /**
     * Lists all the users who have starred this repo based on new version of the API, having extended information like
     * the time when the repository was starred. For compatibility with the old API see {@link #listStargazers()}
     *
     * @return the paged iterable
     */
    public PagedIterable<GHStargazer> listStargazers2() {
        return root().createRequest()
                .withPreview("application/vnd.github.v3.star+json")
                .withUrlPath(getApiTailUrl("stargazers"))
                .toIterable(GHStargazer[].class, item -> item.wrapUp(this));
    }

    private PagedIterable<GHUser> listUsers(final String suffix) {
        return listUsers(root().createRequest(), suffix);
    }

    private PagedIterable<GHUser> listUsers(Requester requester, final String suffix) {
        return requester.withUrlPath(getApiTailUrl(suffix)).toIterable(GHUser[].class, null);
    }

    /**
     * See https://api.github.com/hooks for possible names and their configuration scheme. TODO: produce type-safe
     * binding
     *
     * @param name
     *            Type of the hook to be created. See https://api.github.com/hooks for possible names.
     * @param config
     *            The configuration hash.
     * @param events
     *            Can be null. Types of events to hook into.
     * @param active
     *            the active
     * @return the gh hook
     * @throws IOException
     *             the io exception
     */
    public GHHook createHook(String name, Map<String, String> config, Collection<GHEvent> events, boolean active)
            throws IOException {
        return GHHooks.repoContext(this, owner).createHook(name, config, events, active);
    }

    /**
     * Create web hook gh hook.
     *
     * @param url
     *            the url
     * @param events
     *            the events
     * @return the gh hook
     * @throws IOException
     *             the io exception
     */
    public GHHook createWebHook(URL url, Collection<GHEvent> events) throws IOException {
        return createHook("web", Collections.singletonMap("url", url.toExternalForm()), events, true);
    }

    /**
     * Create web hook gh hook.
     *
     * @param url
     *            the url
     * @return the gh hook
     * @throws IOException
     *             the io exception
     */
    public GHHook createWebHook(URL url) throws IOException {
        return createWebHook(url, null);
    }

    /**
     * Returns a set that represents the post-commit hook URLs. The returned set is live, and changes made to them are
     * reflected to GitHub.
     *
     * @return the post commit hooks
     * @deprecated Use {@link #getHooks()} and {@link #createHook(String, Map, Collection, boolean)}
     */
    @SuppressFBWarnings(value = { "DMI_COLLECTION_OF_URLS", "EI_EXPOSE_REP" },
            justification = "It causes a performance degradation, but we have already exposed it to the API")
    @Deprecated
    public Set<URL> getPostCommitHooks() {
        synchronized (this) {
            if (postCommitHooks == null) {
                postCommitHooks = setupPostCommitHooks();
            }
            return postCommitHooks;
        }
    }

    /**
     * Live set view of the post-commit hook.
     */
    @SuppressFBWarnings(value = "DMI_COLLECTION_OF_URLS",
            justification = "It causes a performance degradation, but we have already exposed it to the API")
    @SkipFromToString
    private /* final */ transient Set<URL> postCommitHooks;

    @SuppressFBWarnings(value = "DMI_COLLECTION_OF_URLS",
            justification = "It causes a performance degradation, but we have already exposed it to the API")
    private Set<URL> setupPostCommitHooks() {
        return new AbstractSet<URL>() {
            private List<URL> getPostCommitHooks() {
                try {
                    List<URL> r = new ArrayList<>();
                    for (GHHook h : getHooks()) {
                        if (h.getName().equals("web")) {
                            r.add(new URL(h.getConfig().get("url")));
                        }
                    }
                    return r;
                } catch (IOException e) {
                    throw new GHException("Failed to retrieve post-commit hooks", e);
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
                    throw new GHException("Failed to update post-commit hooks", e);
                }
            }

            @Override
            public boolean remove(Object url) {
                try {
                    String _url = ((URL) url).toExternalForm();
                    for (GHHook h : getHooks()) {
                        if (h.getName().equals("web") && h.getConfig().get("url").equals(_url)) {
                            h.delete();
                            return true;
                        }
                    }
                    return false;
                } catch (IOException e) {
                    throw new GHException("Failed to update post-commit hooks", e);
                }
            }
        };
    }

    /**
     * Gets branches by {@linkplain GHBranch#getName() their names}.
     *
     * @return the branches
     * @throws IOException
     *             the io exception
     */
    public Map<String, GHBranch> getBranches() throws IOException {
        Map<String, GHBranch> r = new TreeMap<String, GHBranch>();
        for (GHBranch p : root().createRequest()
                .withUrlPath(getApiTailUrl("branches"))
                .toIterable(GHBranch[].class, item -> item.wrap(this))
                .toArray()) {
            r.put(p.getName(), p);
        }
        return r;
    }

    /**
     * Gets branch.
     *
     * @param name
     *            the name
     * @return the branch
     * @throws IOException
     *             the io exception
     */
    public GHBranch getBranch(String name) throws IOException {
        return root().createRequest().withUrlPath(getApiTailUrl("branches/" + name)).fetch(GHBranch.class).wrap(this);
    }

    /**
     * Gets milestones.
     *
     * @return the milestones
     * @throws IOException
     *             the io exception
     * @deprecated Use {@link #listMilestones(GHIssueState)}
     */
    public Map<Integer, GHMilestone> getMilestones() throws IOException {
        Map<Integer, GHMilestone> milestones = new TreeMap<Integer, GHMilestone>();
        for (GHMilestone m : listMilestones(GHIssueState.OPEN)) {
            milestones.put(m.getNumber(), m);
        }
        return milestones;
    }

    /**
     * Lists up all the milestones in this repository.
     *
     * @param state
     *            the state
     * @return the paged iterable
     */
    public PagedIterable<GHMilestone> listMilestones(final GHIssueState state) {
        return root().createRequest()
                .with("state", state)
                .withUrlPath(getApiTailUrl("milestones"))
                .toIterable(GHMilestone[].class, item -> item.lateBind(this));
    }

    /**
     * Gets milestone.
     *
     * @param number
     *            the number
     * @return the milestone
     * @throws IOException
     *             the io exception
     */
    public GHMilestone getMilestone(int number) throws IOException {
        GHMilestone m = milestones.get(number);
        if (m == null) {
            m = root().createRequest().withUrlPath(getApiTailUrl("milestones/" + number)).fetch(GHMilestone.class);
            m.owner = this;
            milestones.put(m.getNumber(), m);
        }
        return m;
    }

    /**
     * Gets file content.
     *
     * @param path
     *            the path
     * @return the file content
     * @throws IOException
     *             the io exception
     */
    public GHContent getFileContent(String path) throws IOException {
        return getFileContent(path, null);
    }

    /**
     * Gets file content.
     *
     * @param path
     *            the path
     * @param ref
     *            the ref
     * @return the file content
     * @throws IOException
     *             the io exception
     */
    public GHContent getFileContent(String path, String ref) throws IOException {
        Requester requester = root().createRequest();
        String target = getApiTailUrl("contents/" + path);

        return requester.with("ref", ref).withUrlPath(target).fetch(GHContent.class).wrap(this);
    }

    /**
     * Gets directory content.
     *
     * @param path
     *            the path
     * @return the directory content
     * @throws IOException
     *             the io exception
     */
    public List<GHContent> getDirectoryContent(String path) throws IOException {
        return getDirectoryContent(path, null);
    }

    /**
     * Gets directory content.
     *
     * @param path
     *            the path
     * @param ref
     *            the ref
     * @return the directory content
     * @throws IOException
     *             the io exception
     */
    public List<GHContent> getDirectoryContent(String path, String ref) throws IOException {
        Requester requester = root().createRequest();
        while (path.endsWith("/")) {
            path = path.substring(0, path.length() - 1);
        }
        String target = getApiTailUrl("contents/" + path);

        return requester.with("ref", ref)
                .withUrlPath(target)
                .toIterable(GHContent[].class, item -> item.wrap(this))
                .toList();
    }

    /**
     * https://developer.github.com/v3/repos/contents/#get-the-readme
     *
     * @return the readme
     * @throws IOException
     *             the io exception
     */
    public GHContent getReadme() throws IOException {
        Requester requester = root().createRequest();
        return requester.withUrlPath(getApiTailUrl("readme")).fetch(GHContent.class).wrap(this);
    }

    /**
     * Creates a new content, or update an existing content.
     *
     * @return the gh content builder
     */
    public GHContentBuilder createContent() {
        return new GHContentBuilder(this);
    }

    /**
     * Use {@link #createContent()}.
     *
     * @param content
     *            the content
     * @param commitMessage
     *            the commit message
     * @param path
     *            the path
     * @return the gh content update response
     * @throws IOException
     *             the io exception
     */
    @Deprecated
    public GHContentUpdateResponse createContent(String content, String commitMessage, String path) throws IOException {
        return createContent().content(content).message(commitMessage).path(path).commit();
    }

    /**
     * Use {@link #createContent()}.
     *
     * @param content
     *            the content
     * @param commitMessage
     *            the commit message
     * @param path
     *            the path
     * @param branch
     *            the branch
     * @return the gh content update response
     * @throws IOException
     *             the io exception
     */
    @Deprecated
    public GHContentUpdateResponse createContent(String content, String commitMessage, String path, String branch)
            throws IOException {
        return createContent().content(content).message(commitMessage).path(path).branch(branch).commit();
    }

    /**
     * Use {@link #createContent()}.
     *
     * @param contentBytes
     *            the content bytes
     * @param commitMessage
     *            the commit message
     * @param path
     *            the path
     * @return the gh content update response
     * @throws IOException
     *             the io exception
     */
    @Deprecated
    public GHContentUpdateResponse createContent(byte[] contentBytes, String commitMessage, String path)
            throws IOException {
        return createContent().content(contentBytes).message(commitMessage).path(path).commit();
    }

    /**
     * Use {@link #createContent()}.
     *
     * @param contentBytes
     *            the content bytes
     * @param commitMessage
     *            the commit message
     * @param path
     *            the path
     * @param branch
     *            the branch
     * @return the gh content update response
     * @throws IOException
     *             the io exception
     */
    @Deprecated
    public GHContentUpdateResponse createContent(byte[] contentBytes, String commitMessage, String path, String branch)
            throws IOException {
        return createContent().content(contentBytes).message(commitMessage).path(path).branch(branch).commit();
    }

    /**
     * Create milestone gh milestone.
     *
     * @param title
     *            the title
     * @param description
     *            the description
     * @return the gh milestone
     * @throws IOException
     *             the io exception
     */
    public GHMilestone createMilestone(String title, String description) throws IOException {
        return root().createRequest()
                .method("POST")
                .with("title", title)
                .with("description", description)
                .withUrlPath(getApiTailUrl("milestones"))
                .fetch(GHMilestone.class)
                .lateBind(this);
    }

    /**
     * Add deploy key gh deploy key.
     *
     * @param title
     *            the title
     * @param key
     *            the key
     * @return the gh deploy key
     * @throws IOException
     *             the io exception
     */
    public GHDeployKey addDeployKey(String title, String key) throws IOException {
        return root().createRequest()
                .method("POST")
                .with("title", title)
                .with("key", key)
                .withUrlPath(getApiTailUrl("keys"))
                .fetch(GHDeployKey.class)
                .lateBind(this);

    }

    /**
     * Gets deploy keys.
     *
     * @return the deploy keys
     * @throws IOException
     *             the io exception
     */
    public List<GHDeployKey> getDeployKeys() throws IOException {
        return root().createRequest()
                .withUrlPath(getApiTailUrl("keys"))
                .toIterable(GHDeployKey[].class, item -> item.lateBind(this))
                .toList();
    }

    /**
     * Forked repositories have a 'source' attribute that specifies the ultimate source of the forking chain.
     *
     * @return {@link GHRepository} that points to the root repository where this repository is forked (indirectly or
     *         directly) from. Otherwise null.
     * @throws IOException
     *             the io exception
     * @see #getParent() #getParent()
     */
    @SuppressFBWarnings(value = { "EI_EXPOSE_REP" }, justification = "Expected behavior")
    public GHRepository getSource() throws IOException {
        if (fork && source == null) {
            populate();
        }
        if (source == null) {
            return null;
        }

        return source;
    }

    /**
     * Forked repositories have a 'parent' attribute that specifies the repository this repository is directly forked
     * from. If we keep traversing {@link #getParent()} until it returns null, that is {@link #getSource()}.
     *
     * @return {@link GHRepository} that points to the repository where this repository is forked directly from.
     *         Otherwise null.
     * @throws IOException
     *             the io exception
     * @see #getSource() #getSource()
     */
    @SuppressFBWarnings(value = { "EI_EXPOSE_REP" }, justification = "Expected behavior")
    public GHRepository getParent() throws IOException {
        if (fork && parent == null) {
            populate();
        }

        if (parent == null) {
            return null;
        }
        return parent;
    }

    /**
     * Subscribes to this repository to get notifications.
     *
     * @param subscribed
     *            the subscribed
     * @param ignored
     *            the ignored
     * @return the gh subscription
     * @throws IOException
     *             the io exception
     */
    public GHSubscription subscribe(boolean subscribed, boolean ignored) throws IOException {
        return root().createRequest()
                .method("PUT")
                .with("subscribed", subscribed)
                .with("ignored", ignored)
                .withUrlPath(getApiTailUrl("subscription"))
                .fetch(GHSubscription.class)
                .wrapUp(this);
    }

    /**
     * Returns the current subscription.
     *
     * @return null if no subscription exists.
     * @throws IOException
     *             the io exception
     */
    public GHSubscription getSubscription() throws IOException {
        try {
            return root().createRequest()
                    .withUrlPath(getApiTailUrl("subscription"))
                    .fetch(GHSubscription.class)
                    .wrapUp(this);
        } catch (FileNotFoundException e) {
            return null;
        }
    }

    /**
     * List contributors paged iterable.
     *
     * @return the paged iterable
     * @throws IOException
     *             the io exception
     */
    public PagedIterable<Contributor> listContributors() throws IOException {
        return root().createRequest().withUrlPath(getApiTailUrl("contributors")).toIterable(Contributor[].class, null);
    }

    /**
     * The type Contributor.
     */
    public static class Contributor extends GHUser {
        private int contributions;

        /**
         * Gets contributions.
         *
         * @return the contributions
         */
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
     * Returns the statistics for this repository.
     *
     * @return the statistics
     */
    public GHRepositoryStatistics getStatistics() {
        // TODO: Use static object and introduce refresh() method,
        // instead of returning new object each time.
        return new GHRepositoryStatistics(this);
    }

    /**
     * Create a project for this repository.
     *
     * @param name
     *            the name
     * @param body
     *            the body
     * @return the gh project
     * @throws IOException
     *             the io exception
     */
    public GHProject createProject(String name, String body) throws IOException {
        return root().createRequest()
                .method("POST")
                .withPreview(INERTIA)
                .with("name", name)
                .with("body", body)
                .withUrlPath(getApiTailUrl("projects"))
                .fetch(GHProject.class)
                .lateBind(this);
    }

    /**
     * Returns the projects for this repository.
     *
     * @param status
     *            The status filter (all, open or closed).
     * @return the paged iterable
     * @throws IOException
     *             the io exception
     */
    public PagedIterable<GHProject> listProjects(final GHProject.ProjectStateFilter status) throws IOException {
        return root().createRequest()
                .withPreview(INERTIA)
                .with("state", status)
                .withUrlPath(getApiTailUrl("projects"))
                .toIterable(GHProject[].class, item -> item.lateBind(this));
    }

    /**
     * Returns open projects for this repository.
     *
     * @return the paged iterable
     * @throws IOException
     *             the io exception
     */
    public PagedIterable<GHProject> listProjects() throws IOException {
        return listProjects(GHProject.ProjectStateFilter.OPEN);
    }

    /**
     * Render a Markdown document.
     * <p>
     * In {@linkplain MarkdownMode#GFM GFM mode}, issue numbers and user mentions are linked accordingly.
     *
     * @param text
     *            the text
     * @param mode
     *            the mode
     * @return the reader
     * @throws IOException
     *             the io exception
     * @see GitHub#renderMarkdown(String) GitHub#renderMarkdown(String)
     */
    public Reader renderMarkdown(String text, MarkdownMode mode) throws IOException {
        return new InputStreamReader(
                root().createRequest()
                        .method("POST")
                        .with("text", text)
                        .with("mode", mode == null ? null : mode.toString())
                        .with("context", getFullName())
                        .withUrlPath("/markdown")
                        .fetchStream(Requester::copyInputStream),
                "UTF-8");
    }

    /**
     * List all the notifications in a repository for the current user.
     *
     * @return the gh notification stream
     */
    public GHNotificationStream listNotifications() {
        return new GHNotificationStream(root(), getApiTailUrl("/notifications"));
    }

    /**
     * <a href=
     * "https://developer.github.com/v3/repos/traffic/#views">https://developer.github.com/v3/repos/traffic/#views</a>
     *
     * @return the view traffic
     * @throws IOException
     *             the io exception
     */
    public GHRepositoryViewTraffic getViewTraffic() throws IOException {
        return root().createRequest().withUrlPath(getApiTailUrl("/traffic/views")).fetch(GHRepositoryViewTraffic.class);
    }

    /**
     * <a href=
     * "https://developer.github.com/v3/repos/traffic/#clones">https://developer.github.com/v3/repos/traffic/#clones</a>
     *
     * @return the clone traffic
     * @throws IOException
     *             the io exception
     */
    public GHRepositoryCloneTraffic getCloneTraffic() throws IOException {
        return root().createRequest()
                .withUrlPath(getApiTailUrl("/traffic/clones"))
                .fetch(GHRepositoryCloneTraffic.class);
    }

    @Override
    public int hashCode() {
        return ("Repository:" + getOwnerName() + ":" + name).hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof GHRepository) {
            GHRepository that = (GHRepository) obj;
            return this.getOwnerName().equals(that.getOwnerName()) && this.name.equals(that.name);
        }
        return false;
    }

    String getApiTailUrl(String tail) {
        if (tail.length() > 0 && !tail.startsWith("/")) {
            tail = '/' + tail;
        }
        return "/repos/" + full_name + tail;
    }

    /**
     * Get all issue events for this repository. See
     * https://developer.github.com/v3/issues/events/#list-events-for-a-repository
     *
     * @return the paged iterable
     * @throws IOException
     *             the io exception
     */
    public PagedIterable<GHIssueEvent> listIssueEvents() throws IOException {
        return root().createRequest()
                .withUrlPath(getApiTailUrl("issues/events"))
                .toIterable(GHIssueEvent[].class, null);
    }

    /**
     * Get a single issue event. See https://developer.github.com/v3/issues/events/#get-a-single-event
     *
     * @param id
     *            the id
     * @return the issue event
     * @throws IOException
     *             the io exception
     */
    public GHIssueEvent getIssueEvent(long id) throws IOException {
        return root().createRequest().withUrlPath(getApiTailUrl("issues/events/" + id)).fetch(GHIssueEvent.class);
    }

    /**
     * Lists all the workflows of this repository.
     *
     * @return the paged iterable
     */
    public PagedIterable<GHWorkflow> listWorkflows() {
        return new GHWorkflowsIterable(this);
    }

    /**
     * Gets a workflow by id.
     *
     * @param id
     *            the id of the workflow run
     * @return the workflow run
     * @throws IOException
     *             the io exception
     */
    public GHWorkflow getWorkflow(long id) throws IOException {
        return getWorkflow(String.valueOf(id));
    }

    /**
     * Gets a workflow by name of the file.
     *
     * @param nameOrId
     *            either the name of the file (e.g. my-workflow.yml) or the id as a string
     * @return the workflow run
     * @throws IOException
     *             the io exception
     */
    public GHWorkflow getWorkflow(String nameOrId) throws IOException {
        return root().createRequest()
                .withUrlPath(getApiTailUrl("actions/workflows"), nameOrId)
                .fetch(GHWorkflow.class)
                .wrapUp(this);
    }

    /**
     * Retrieves workflow runs.
     *
     * @return the workflow run query builder
     */
    public GHWorkflowRunQueryBuilder queryWorkflowRuns() {
        return new GHWorkflowRunQueryBuilder(this);
    }

    /**
     * Gets a workflow run.
     *
     * @param id
     *            the id of the workflow run
     * @return the workflow run
     * @throws IOException
     *             the io exception
     */
    public GHWorkflowRun getWorkflowRun(long id) throws IOException {
        return root().createRequest()
                .withUrlPath(getApiTailUrl("actions/runs"), String.valueOf(id))
                .fetch(GHWorkflowRun.class)
                .wrapUp(this);
    }

    /**
     * Lists all the artifacts of this repository.
     *
     * @return the paged iterable
     */
    public PagedIterable<GHArtifact> listArtifacts() {
        return new GHArtifactsIterable(this, root().createRequest().withUrlPath(getApiTailUrl("actions/artifacts")));
    }

    /**
     * Gets an artifact by id.
     *
     * @param id
     *            the id of the artifact
     * @return the artifact
     * @throws IOException
     *             the io exception
     */
    public GHArtifact getArtifact(long id) throws IOException {
        return root().createRequest()
                .withUrlPath(getApiTailUrl("actions/artifacts"), String.valueOf(id))
                .fetch(GHArtifact.class)
                .wrapUp(this);
    }

    /**
     * Gets a job from a workflow run by id.
     *
     * @param id
     *            the id of the job
     * @return the job
     * @throws IOException
     *             the io exception
     */
    public GHWorkflowJob getWorkflowJob(long id) throws IOException {
        return root().createRequest()
                .withUrlPath(getApiTailUrl("/actions/jobs"), String.valueOf(id))
                .fetch(GHWorkflowJob.class)
                .wrapUp(this);
    }

    /**
     * Gets the public key for the given repo
     *
     * @return the public key
     * @throws IOException
     *             the io exception
     */
    public GHPublicKey getPublicKey() throws IOException {
        return GHPublicKeys.repoContext(this, owner).getPublicKey();
    }

    // Only used within listTopics().
    private static class Topics {
        public List<String> names;
    }

    /**
     * Return the topics for this repository. See
     * https://developer.github.com/v3/repos/#list-all-topics-for-a-repository
     *
     * @return the list
     * @throws IOException
     *             the io exception
     */
    public List<String> listTopics() throws IOException {
        Topics topics = root().createRequest()
                .withPreview(MERCY)
                .withUrlPath(getApiTailUrl("topics"))
                .fetch(Topics.class);
        return topics.names;
    }

    /**
     * Set the topics for this repository. See
     * https://developer.github.com/v3/repos/#replace-all-topics-for-a-repository
     *
     * @param topics
     *            the topics
     * @throws IOException
     *             the io exception
     */
    public void setTopics(List<String> topics) throws IOException {
        root().createRequest()
                .method("PUT")
                .with("names", topics)
                .withPreview(MERCY)
                .withUrlPath(getApiTailUrl("topics"))
                .send();
    }

    /**
     * Create a tag. See https://developer.github.com/v3/git/tags/#create-a-tag-object
     *
     * @param tag
     *            The tag's name.
     * @param message
     *            The tag message.
     * @param object
     *            The SHA of the git object this is tagging.
     * @param type
     *            The type of the object we're tagging: "commit", "tree" or "blob".
     * @return The newly created tag.
     * @throws java.io.IOException
     *             The IO exception.
     */
    public GHTagObject createTag(String tag, String message, String object, String type) throws IOException {
        return root().createRequest()
                .method("POST")
                .with("tag", tag)
                .with("message", message)
                .with("object", object)
                .with("type", type)
                .withUrlPath(getApiTailUrl("git/tags"))
                .fetch(GHTagObject.class)
                .wrap(this);
    }

    /**
     * Streams a zip archive of the repository, optionally at a given <code>ref</code>.
     *
     * @param <T>
     *            the type of result
     * @param streamFunction
     *            The {@link InputStreamFunction} that will process the stream
     * @param ref
     *            if <code>null</code> the repository's default branch, usually <code>main</code>,
     * @throws IOException
     *             The IO exception.
     * @return the result of reading the stream.
     */
    public <T> T readZip(InputStreamFunction<T> streamFunction, String ref) throws IOException {
        return downloadArchive("zip", ref, streamFunction);
    }

    /**
     * Streams a tar archive of the repository, optionally at a given <code>ref</code>.
     *
     * @param <T>
     *            the type of result
     * @param streamFunction
     *            The {@link InputStreamFunction} that will process the stream
     * @param ref
     *            if <code>null</code> the repository's default branch, usually <code>main</code>,
     * @throws IOException
     *             The IO exception.
     * @return the result of reading the stream.
     */
    public <T> T readTar(InputStreamFunction<T> streamFunction, String ref) throws IOException {
        return downloadArchive("tar", ref, streamFunction);
    }

    /**
     * Create a repository dispatch event, which can be used to start a workflow/action from outside github, as
     * described on https://docs.github.com/en/rest/reference/repos#create-a-repository-dispatch-event
     *
     * @param eventType
     *            the eventType
     * @param clientPayload
     *            a custom payload , can be nullable
     * @param <T>
     *            type of client payload
     * @throws IOException
     *             the io exception
     */
    public <T> void dispatch(String eventType, @Nullable T clientPayload) throws IOException {
        root().createRequest()
                .method("POST")
                .withUrlPath(getApiTailUrl("dispatches"))
                .with("event_type", eventType)
                .with("client_payload", clientPayload)
                .send();
    }

    private <T> T downloadArchive(@Nonnull String type,
            @CheckForNull String ref,
            @Nonnull InputStreamFunction<T> streamFunction) throws IOException {
        requireNonNull(streamFunction, "Sink must not be null");
        String tailUrl = getApiTailUrl(type + "ball");
        if (ref != null) {
            tailUrl += "/" + ref;
        }
        final Requester builder = root().createRequest().method("GET").withUrlPath(tailUrl);
        return builder.fetchStream(streamFunction);
    }

    /**
     * Populate this object.
     *
     * @throws java.io.IOException
     *             The IO exception
     */
    void populate() throws IOException {
        if (isOffline()) {
            return; // can't populate if the root is offline
        }

        // We don't use the URL provided in the JSON because it is not reliable:
        // 1. There is bug in Push event payloads that returns the wrong url.
        // For Push event repository records, they take the form "https://github.com/{fullName}".
        // All other occurrences of "url" take the form "https://api.github.com/...".
        // 2. For Installation event payloads, the URL is not provided at all.

        root().createRequest().withPreview(BAPTISTE).withPreview(NEBULA).withUrlPath(getApiTailUrl("")).fetchInto(this);
    }

    /**
     * A {@link GHRepositoryBuilder} that allows multiple properties to be updated per request.
     *
     * Consumer must call {@link #done()} to commit changes.
     */
    @BetaApi
    public static class Updater extends GHRepositoryBuilder<Updater> {
        protected Updater(@Nonnull GHRepository repository) {
            super(Updater.class, repository.root(), null);
            // even when we don't change the name, we need to send it in
            // this requirement may be out-of-date, but we do not want to break it
            requester.with("name", repository.name);

            requester.method("PATCH").withUrlPath(repository.getApiTailUrl(""));
        }
    }

    /**
     * A {@link GHRepositoryBuilder} that allows multiple properties to be updated per request.
     *
     * Consumer must call {@link #done()} to commit changes.
     */
    @BetaApi
    public static class Setter extends GHRepositoryBuilder<GHRepository> {
        protected Setter(@Nonnull GHRepository repository) {
            super(GHRepository.class, repository.root(), null);
            // even when we don't change the name, we need to send it in
            // this requirement may be out-of-date, but we do not want to break it
            requester.with("name", repository.name);

            requester.method("PATCH").withUrlPath(repository.getApiTailUrl(""));
        }
    }
}
