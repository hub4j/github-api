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

import com.infradna.tool.bridge_method_injector.WithBridgeMethods;

import java.io.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.function.Supplier;
import java.util.logging.Logger;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;

import static org.kohsuke.github.Previews.INERTIA;
import static org.kohsuke.github.Previews.MACHINE_MAN;

/**
 * Root of the GitHub API.
 *
 * <h2>Thread safety</h2>
 * <p>
 * This library aims to be safe for use by multiple threads concurrently, although the library itself makes no attempt
 * to control/serialize potentially conflicting operations to GitHub, such as updating &amp; deleting a repository at
 * the same time.
 *
 * @author Kohsuke Kawaguchi
 */
public class GitHub {

    @Nonnull
    private final GitHubClient client;

    @CheckForNull
    private GHMyself myself;

    private final ConcurrentMap<String, GHUser> users;
    private final ConcurrentMap<String, GHOrganization> orgs;

    /**
     * Creates a client API root object.
     *
     * <p>
     * Several different combinations of the login/oauthAccessToken/password parameters are allowed to represent
     * different ways of authentication.
     *
     * <dl>
     * <dt>Log in anonymously
     * <dd>Leave all three parameters null and you will be making HTTP requests without any authentication.
     *
     * <dt>Log in with password
     * <dd>Specify the login and password, then leave oauthAccessToken null. This will use the HTTP BASIC auth with the
     * GitHub API.
     *
     * <dt>Log in with OAuth token
     * <dd>Specify oauthAccessToken, and optionally specify the login. Leave password null. This will send OAuth token
     * to the GitHub API. If the login parameter is null, The constructor makes an API call to figure out the user name
     * that owns the token.
     *
     * <dt>Log in with JWT token
     * <dd>Specify jwtToken. Leave password null. This will send JWT token to the GitHub API via the Authorization HTTP
     * header. Please note that only operations in which permissions have been previously configured and accepted during
     * the GitHub App will be executed successfully.
     * </dl>
     *
     * @param apiUrl
     *            The URL of GitHub (or GitHub enterprise) API endpoint, such as "https://api.github.com" or
     *            "http://ghe.acme.com/api/v3". Note that GitHub Enterprise has <code>/api/v3</code> in the URL. For
     *            historical reasons, this parameter still accepts the bare domain name, but that's considered
     *            deprecated. Password is also considered deprecated as it is no longer required for api usage.
     * @param login
     *            The user ID on GitHub that you are logging in as. Can be omitted if the OAuth token is provided or if
     *            logging in anonymously. Specifying this would save one API call.
     * @param oauthAccessToken
     *            Secret OAuth token.
     * @param password
     *            User's password. Always used in conjunction with the {@code login} parameter
     * @param connector
     *            HttpConnector to use. Pass null to use default connector.
     */
    GitHub(String apiUrl,
            String login,
            String oauthAccessToken,
            String jwtToken,
            String password,
            HttpConnector connector,
            RateLimitHandler rateLimitHandler,
            AbuseLimitHandler abuseLimitHandler) throws IOException {
        this.client = new GitHubClient(apiUrl,
                login,
                oauthAccessToken,
                jwtToken,
                password,
                connector,
                rateLimitHandler,
                abuseLimitHandler,
                (myself) -> setMyself(myself));
        users = new ConcurrentHashMap<>();
        orgs = new ConcurrentHashMap<>();
    }

    /**
     * Obtains the credential from "~/.github" or from the System Environment Properties.
     *
     * @return the git hub
     * @throws IOException
     *             the io exception
     */
    public static GitHub connect() throws IOException {
        return GitHubBuilder.fromCredentials().build();
    }

    /**
     * Version that connects to GitHub Enterprise.
     *
     * @param apiUrl
     *            the api url
     * @param oauthAccessToken
     *            the oauth access token
     * @return the git hub
     * @throws IOException
     *             the io exception
     * @deprecated Use {@link #connectToEnterpriseWithOAuth(String, String, String)}
     */
    @Deprecated
    public static GitHub connectToEnterprise(String apiUrl, String oauthAccessToken) throws IOException {
        return connectToEnterpriseWithOAuth(apiUrl, null, oauthAccessToken);
    }

    /**
     * Version that connects to GitHub Enterprise.
     *
     * @param apiUrl
     *            The URL of GitHub (or GitHub Enterprise) API endpoint, such as "https://api.github.com" or
     *            "http://ghe.acme.com/api/v3". Note that GitHub Enterprise has <code>/api/v3</code> in the URL. For
     *            historical reasons, this parameter still accepts the bare domain name, but that's considered
     *            deprecated.
     * @param login
     *            the login
     * @param oauthAccessToken
     *            the oauth access token
     * @return the git hub
     * @throws IOException
     *             the io exception
     */
    public static GitHub connectToEnterpriseWithOAuth(String apiUrl, String login, String oauthAccessToken)
            throws IOException {
        return new GitHubBuilder().withEndpoint(apiUrl).withOAuthToken(oauthAccessToken, login).build();
    }

    /**
     * Version that connects to GitHub Enterprise.
     *
     * @param apiUrl
     *            the api url
     * @param login
     *            the login
     * @param password
     *            the password
     * @return the git hub
     * @throws IOException
     *             the io exception
     * @deprecated Use with caution. Login with password is not a preferred method.
     */
    @Deprecated
    public static GitHub connectToEnterprise(String apiUrl, String login, String password) throws IOException {
        return new GitHubBuilder().withEndpoint(apiUrl).withPassword(login, password).build();
    }

    /**
     * Connect git hub.
     *
     * @param login
     *            the login
     * @param oauthAccessToken
     *            the oauth access token
     * @return the git hub
     * @throws IOException
     *             the io exception
     */
    public static GitHub connect(String login, String oauthAccessToken) throws IOException {
        return new GitHubBuilder().withOAuthToken(oauthAccessToken, login).build();
    }

    /**
     * Connect git hub.
     *
     * @param login
     *            the login
     * @param oauthAccessToken
     *            the oauth access token
     * @param password
     *            the password
     * @return the git hub
     * @throws IOException
     *             the io exception
     * @deprecated Either OAuth token or password is sufficient, so there's no point in passing both. Use
     *             {@link #connectUsingPassword(String, String)} or {@link #connectUsingOAuth(String)}.
     */
    @Deprecated
    public static GitHub connect(String login, String oauthAccessToken, String password) throws IOException {
        return new GitHubBuilder().withOAuthToken(oauthAccessToken, login).withPassword(login, password).build();
    }

    /**
     * Connect using password git hub.
     *
     * @param login
     *            the login
     * @param password
     *            the password
     * @return the git hub
     * @throws IOException
     *             the io exception
     */
    public static GitHub connectUsingPassword(String login, String password) throws IOException {
        return new GitHubBuilder().withPassword(login, password).build();
    }

    /**
     * Connect using o auth git hub.
     *
     * @param oauthAccessToken
     *            the oauth access token
     * @return the git hub
     * @throws IOException
     *             the io exception
     */
    public static GitHub connectUsingOAuth(String oauthAccessToken) throws IOException {
        return new GitHubBuilder().withOAuthToken(oauthAccessToken).build();
    }

    /**
     * Connect using o auth git hub.
     *
     * @param githubServer
     *            the github server
     * @param oauthAccessToken
     *            the oauth access token
     * @return the git hub
     * @throws IOException
     *             the io exception
     */
    public static GitHub connectUsingOAuth(String githubServer, String oauthAccessToken) throws IOException {
        return new GitHubBuilder().withEndpoint(githubServer).withOAuthToken(oauthAccessToken).build();
    }

    /**
     * Connects to GitHub anonymously.
     * <p>
     * All operations that require authentication will fail.
     *
     * @return the git hub
     * @throws IOException
     *             the io exception
     */
    public static GitHub connectAnonymously() throws IOException {
        return new GitHubBuilder().build();
    }

    /**
     * Connects to GitHub Enterprise anonymously.
     * <p>
     * All operations that require authentication will fail.
     *
     * @param apiUrl
     *            the api url
     * @return the git hub
     * @throws IOException
     *             the io exception
     */
    public static GitHub connectToEnterpriseAnonymously(String apiUrl) throws IOException {
        return new GitHubBuilder().withEndpoint(apiUrl).build();
    }

    /**
     * An offline-only {@link GitHub} useful for parsing event notification from an unknown source.
     * <p>
     * All operations that require a connection will fail.
     *
     * @return An offline-only {@link GitHub}.
     */
    public static GitHub offline() {
        try {
            return new GitHubBuilder().withEndpoint("https://api.github.invalid")
                    .withConnector(HttpConnector.OFFLINE)
                    .build();
        } catch (IOException e) {
            throw new IllegalStateException("The offline implementation constructor should not connect", e);
        }
    }

    /**
     * Is this an anonymous connection
     *
     * @return {@code true} if operations that require authentication will fail.
     */
    public boolean isAnonymous() {
        return client.isAnonymous();
    }

    /**
     * Is this an always offline "connection".
     *
     * @return {@code true} if this is an always offline "connection".
     */
    public boolean isOffline() {
        return client.isOffline();
    }

    /**
     * Gets connector.
     *
     * @return the connector
     */
    public HttpConnector getConnector() {
        return client.getConnector();
    }

    /**
     * Sets the custom connector used to make requests to GitHub.
     *
     * @param connector
     *            the connector
     */
    public void setConnector(HttpConnector connector) {
        client.setConnector(connector);
    }

    /**
     * Gets api url.
     *
     * @return the api url
     */
    public String getApiUrl() {
        return client.getApiUrl();
    }

    /**
     * Gets the current rate limit.
     *
     * @return the rate limit
     * @throws IOException
     *             the io exception
     */
    public GHRateLimit getRateLimit() throws IOException {
        return client.getRateLimit();
    }

    /**
     * Returns the most recently observed rate limit data or {@code null} if either there is no rate limit (for example
     * GitHub Enterprise) or if no requests have been made.
     *
     * @return the most recently observed rate limit data or {@code null}.
     */
    @CheckForNull
    public GHRateLimit lastRateLimit() {
        return client.lastRateLimit();
    }

    /**
     * Gets the current rate limit while trying not to actually make any remote requests unless absolutely necessary.
     *
     * @return the current rate limit data.
     * @throws IOException
     *             if we couldn't get the current rate limit data.
     */
    @Nonnull
    public GHRateLimit rateLimit() throws IOException {
        return client.rateLimit();
    }

    /**
     * Gets the {@link GHUser} that represents yourself.
     *
     * @return the myself
     * @throws IOException
     *             the io exception
     */
    @WithBridgeMethods(GHUser.class)
    GHMyself getMyself() throws IOException {
        client.requireCredential();
        synchronized (this) {
            if (this.myself == null) {
                GHMyself u = createRequest().withUrlPath("/user").fetch(GHMyself.class);
                setMyself(u);
            }
            return myself;
        }
    }

    private void setMyself(GHMyself myself) {
        synchronized (this) {
            myself.wrapUp(this);
            this.myself = myself;
        }
    }

    /**
     * Obtains the object that represents the named user.
     *
     * @param login
     *            the login
     * @return the user
     * @throws IOException
     *             the io exception
     */
    public GHUser getUser(String login) throws IOException {
        GHUser u = users.get(login);
        if (u == null) {
            u = createRequest().withUrlPath("/users/" + login).fetch(GHUser.class);
            u.root = this;
            users.put(u.getLogin(), u);
        }
        return u;
    }

    /**
     * clears all cached data in order for external changes (modifications and del) to be reflected
     */
    public void refreshCache() {
        users.clear();
        orgs.clear();
    }

    /**
     * Interns the given {@link GHUser}.
     *
     * @param orig
     *            the orig
     * @return the user
     */
    protected GHUser getUser(GHUser orig) {
        GHUser u = users.get(orig.getLogin());
        if (u == null) {
            orig.root = this;
            users.put(orig.getLogin(), orig);
            return orig;
        }
        return u;
    }

    /**
     * Gets {@link GHOrganization} specified by name.
     *
     * @param name
     *            the name
     * @return the organization
     * @throws IOException
     *             the io exception
     */
    public GHOrganization getOrganization(String name) throws IOException {
        GHOrganization o = orgs.get(name);
        if (o == null) {
            o = createRequest().withUrlPath("/orgs/" + name).fetch(GHOrganization.class).wrapUp(this);
            orgs.put(name, o);
        }
        return o;
    }

    /**
     * Gets a list of all organizations.
     *
     * @return the paged iterable
     */
    public PagedIterable<GHOrganization> listOrganizations() {
        return listOrganizations(null);
    }

    /**
     * Gets a list of all organizations starting after the organization identifier specified by 'since'.
     *
     * @param since
     *            the since
     * @return the paged iterable
     * @see <a href="https://developer.github.com/v3/orgs/#parameters">List All Orgs - Parameters</a>
     */
    public PagedIterable<GHOrganization> listOrganizations(final String since) {
        return createRequest().with("since", since)
                .withUrlPath("/organizations")
                .fetchIterable(GHOrganization[].class, item -> item.wrapUp(this));
    }

    /**
     * Gets the repository object from 'user/reponame' string that GitHub calls as "repository name"
     *
     * @param name
     *            the name
     * @return the repository
     * @throws IOException
     *             the io exception
     * @see GHRepository#getName() GHRepository#getName()
     */
    public GHRepository getRepository(String name) throws IOException {
        String[] tokens = name.split("/");
        return createRequest().withUrlPath("/repos/" + tokens[0] + '/' + tokens[1])
                .fetch(GHRepository.class)
                .wrap(this);
    }

    /**
     * Gets the repository object from its ID
     *
     * @param id
     *            the id
     * @return the repository by id
     * @throws IOException
     *             the io exception
     */
    public GHRepository getRepositoryById(String id) throws IOException {
        return createRequest().withUrlPath("/repositories/" + id).fetch(GHRepository.class).wrap(this);
    }

    /**
     * Returns a list of popular open source licenses
     *
     * @return a list of popular open source licenses
     * @throws IOException
     *             the io exception
     * @see <a href="https://developer.github.com/v3/licenses/">GitHub API - Licenses</a>
     */
    public PagedIterable<GHLicense> listLicenses() throws IOException {
        return createRequest().withUrlPath("/licenses").fetchIterable(GHLicense[].class, item -> item.wrap(this));
    }

    /**
     * Returns a list of all users.
     *
     * @return the paged iterable
     * @throws IOException
     *             the io exception
     */
    public PagedIterable<GHUser> listUsers() throws IOException {
        return createRequest().withUrlPath("/users").fetchIterable(GHUser[].class, item -> item.wrapUp(this));
    }

    /**
     * Returns the full details for a license
     *
     * @param key
     *            The license key provided from the API
     * @return The license details
     * @throws IOException
     *             the io exception
     * @see GHLicense#getKey() GHLicense#getKey()
     */
    public GHLicense getLicense(String key) throws IOException {
        return createRequest().withUrlPath("/licenses/" + key).fetch(GHLicense.class);
    }

    /**
     * Returns a list all plans for your Marketplace listing
     * <p>
     * GitHub Apps must use a JWT to access this endpoint.
     * <p>
     * OAuth Apps must use basic authentication with their client ID and client secret to access this endpoint.
     *
     * @return the paged iterable
     * @throws IOException
     *             the io exception
     * @see <a href="https://developer.github.com/v3/apps/marketplace/#list-all-plans-for-your-marketplace-listing">List
     *      Plans</a>
     */
    public PagedIterable<GHMarketplacePlan> listMarketplacePlans() throws IOException {
        return createRequest().withUrlPath("/marketplace_listing/plans")
                .fetchIterable(GHMarketplacePlan[].class, item -> item.wrapUp(this));
    }

    /**
     * Gets complete list of open invitations for current user.
     *
     * @return the my invitations
     * @throws IOException
     *             the io exception
     */
    public List<GHInvitation> getMyInvitations() throws IOException {
        GHInvitation[] invitations = createRequest().withUrlPath("/user/repository_invitations")
                .fetchArray(GHInvitation[].class);
        for (GHInvitation i : invitations) {
            i.wrapUp(this);
        }
        return Arrays.asList(invitations);
    }

    /**
     * This method returns shallowly populated organizations.
     * <p>
     * To retrieve full organization details, you need to call {@link #getOrganization(String)} TODO: make this
     * automatic.
     *
     * @return the my organizations
     * @throws IOException
     *             the io exception
     */
    public Map<String, GHOrganization> getMyOrganizations() throws IOException {
        GHOrganization[] orgs = createRequest().withUrlPath("/user/orgs").fetchArray(GHOrganization[].class);
        Map<String, GHOrganization> r = new HashMap<String, GHOrganization>();
        for (GHOrganization o : orgs) {
            // don't put 'o' into orgs because they are shallow
            r.put(o.getLogin(), o.wrapUp(this));
        }
        return r;
    }

    /**
     * Returns only active subscriptions.
     * <p>
     * You must use a user-to-server OAuth access token, created for a user who has authorized your GitHub App, to
     * access this endpoint
     * <p>
     * OAuth Apps must authenticate using an OAuth token.
     *
     * @return the paged iterable of GHMarketplaceUserPurchase
     * @throws IOException
     *             the io exception
     * @see <a href="https://developer.github.com/v3/apps/marketplace/#get-a-users-marketplace-purchases">Get a user's
     *      Marketplace purchases</a>
     */
    public PagedIterable<GHMarketplaceUserPurchase> getMyMarketplacePurchases() throws IOException {
        return createRequest().withUrlPath("/user/marketplace_purchases")
                .fetchIterable(GHMarketplaceUserPurchase[].class, item -> item.wrapUp(this));
    }

    /**
     * Alias for {@link #getUserPublicOrganizations(String)}.
     *
     * @param user
     *            the user
     * @return the user public organizations
     * @throws IOException
     *             the io exception
     */
    public Map<String, GHOrganization> getUserPublicOrganizations(GHUser user) throws IOException {
        return getUserPublicOrganizations(user.getLogin());
    }

    /**
     * This method returns a shallowly populated organizations.
     * <p>
     * To retrieve full organization details, you need to call {@link #getOrganization(String)}
     *
     * @param login
     *            the user to retrieve public Organization membership information for
     * @return the public Organization memberships for the user
     * @throws IOException
     *             the io exception
     */
    public Map<String, GHOrganization> getUserPublicOrganizations(String login) throws IOException {
        GHOrganization[] orgs = createRequest().withUrlPath("/users/" + login + "/orgs")
                .fetchArray(GHOrganization[].class);
        Map<String, GHOrganization> r = new HashMap<String, GHOrganization>();
        for (GHOrganization o : orgs) {
            // don't put 'o' into orgs because they are shallow
            r.put(o.getLogin(), o.wrapUp(this));
        }
        return r;
    }

    /**
     * Gets complete map of organizations/teams that current user belongs to.
     * <p>
     * Leverages the new GitHub API /user/teams made available recently to get in a single call the complete set of
     * organizations, teams and permissions in a single call.
     *
     * @return the my teams
     * @throws IOException
     *             the io exception
     */
    public Map<String, Set<GHTeam>> getMyTeams() throws IOException {
        Map<String, Set<GHTeam>> allMyTeams = new HashMap<String, Set<GHTeam>>();
        for (GHTeam team : createRequest().withUrlPath("/user/teams").fetchArray(GHTeam[].class)) {
            team.wrapUp(this);
            String orgLogin = team.getOrganization().getLogin();
            Set<GHTeam> teamsPerOrg = allMyTeams.get(orgLogin);
            if (teamsPerOrg == null) {
                teamsPerOrg = new HashSet<GHTeam>();
            }
            teamsPerOrg.add(team);
            allMyTeams.put(orgLogin, teamsPerOrg);
        }
        return allMyTeams;
    }

    /**
     * Gets a sigle team by ID.
     *
     * @param id
     *            the id
     * @return the team
     * @throws IOException
     *             the io exception
     */
    public GHTeam getTeam(int id) throws IOException {
        return createRequest().withUrlPath("/teams/" + id).fetch(GHTeam.class).wrapUp(this);
    }

    /**
     * Public events visible to you. Equivalent of what's displayed on https://github.com/
     *
     * @return the events
     * @throws IOException
     *             the io exception
     */
    public List<GHEventInfo> getEvents() throws IOException {
        GHEventInfo[] events = createRequest().withUrlPath("/events").fetchArray(GHEventInfo[].class);
        for (GHEventInfo e : events)
            e.wrapUp(this);
        return Arrays.asList(events);
    }

    /**
     * Gets a single gist by ID.
     *
     * @param id
     *            the id
     * @return the gist
     * @throws IOException
     *             the io exception
     */
    public GHGist getGist(String id) throws IOException {
        return createRequest().withUrlPath("/gists/" + id).fetch(GHGist.class).wrapUp(this);
    }

    /**
     * Create gist gh gist builder.
     *
     * @return the gh gist builder
     */
    public GHGistBuilder createGist() {
        return new GHGistBuilder(this);
    }

    /**
     * Parses the GitHub event object.
     * <p>
     * This is primarily intended for receiving a POST HTTP call from a hook. Unfortunately, hook script payloads aren't
     * self-descriptive, so you need to know the type of the payload you are expecting.
     *
     * @param <T>
     *            the type parameter
     * @param r
     *            the r
     * @param type
     *            the type
     * @return the t
     * @throws IOException
     *             the io exception
     */
    public <T extends GHEventPayload> T parseEventPayload(Reader r, Class<T> type) throws IOException {
        T t = GitHubClient.MAPPER.readValue(r, type);
        t.wrapUp(this);
        return t;
    }

    /**
     * Creates a new repository.
     *
     * @param name
     *            the name
     * @param description
     *            the description
     * @param homepage
     *            the homepage
     * @param isPublic
     *            the is public
     * @return Newly created repository.
     * @throws IOException
     *             the io exception
     * @deprecated Use {@link #createRepository(String)} that uses a builder pattern to let you control every aspect.
     */
    @Deprecated
    public GHRepository createRepository(String name, String description, String homepage, boolean isPublic)
            throws IOException {
        return createRepository(name).description(description).homepage(homepage).private_(!isPublic).create();
    }

    /**
     * Starts a builder that creates a new repository.
     *
     * <p>
     * You use the returned builder to set various properties, then call {@link GHCreateRepositoryBuilder#create()} to
     * finally create a repository.
     *
     * <p>
     * To create a repository in an organization, see
     * {@link GHOrganization#createRepository(String, String, String, GHTeam, boolean)}
     *
     * @param name
     *            the name
     * @return the gh create repository builder
     */
    public GHCreateRepositoryBuilder createRepository(String name) {
        return new GHCreateRepositoryBuilder(this, "/user/repos", name);
    }

    /**
     * Creates a new authorization.
     * <p>
     * The token created can be then used for {@link GitHub#connectUsingOAuth(String)} in the future.
     *
     * @param scope
     *            the scope
     * @param note
     *            the note
     * @param noteUrl
     *            the note url
     * @return the gh authorization
     * @throws IOException
     *             the io exception
     * @see <a href="http://developer.github.com/v3/oauth/#create-a-new-authorization">Documentation</a>
     */
    public GHAuthorization createToken(Collection<String> scope, String note, String noteUrl) throws IOException {
        Requester requester = createRequest().with("scopes", scope).with("note", note).with("note_url", noteUrl);

        return requester.method("POST").withUrlPath("/authorizations").fetch(GHAuthorization.class).wrap(this);
    }

    /**
     * Creates a new authorization using an OTP.
     * <p>
     * Start by running createToken, if exception is thrown, prompt for OTP from user
     * <p>
     * Once OTP is received, call this token request
     * <p>
     * The token created can be then used for {@link GitHub#connectUsingOAuth(String)} in the future.
     *
     * @param scope
     *            the scope
     * @param note
     *            the note
     * @param noteUrl
     *            the note url
     * @param OTP
     *            the otp
     * @return the gh authorization
     * @throws IOException
     *             the io exception
     * @see <a href="http://developer.github.com/v3/oauth/#create-a-new-authorization">Documentation</a>
     */
    public GHAuthorization createToken(Collection<String> scope, String note, String noteUrl, Supplier<String> OTP)
            throws IOException {
        try {
            return createToken(scope, note, noteUrl);
        } catch (GHOTPRequiredException ex) {
            String OTPstring = OTP.get();
            Requester requester = createRequest().with("scopes", scope).with("note", note).with("note_url", noteUrl);
            // Add the OTP from the user
            requester.setHeader("x-github-otp", OTPstring);
            return requester.method("POST").withUrlPath("/authorizations").fetch(GHAuthorization.class).wrap(this);
        }
    }

    /**
     * Create or get auth gh authorization.
     *
     * @param clientId
     *            the client id
     * @param clientSecret
     *            the client secret
     * @param scopes
     *            the scopes
     * @param note
     *            the note
     * @param note_url
     *            the note url
     * @return the gh authorization
     * @throws IOException
     *             the io exception
     * @see <a href=
     *      "https://developer.github.com/v3/oauth_authorizations/#get-or-create-an-authorization-for-a-specific-app">docs</a>
     */
    public GHAuthorization createOrGetAuth(String clientId,
            String clientSecret,
            List<String> scopes,
            String note,
            String note_url) throws IOException {
        Requester requester = createRequest().with("client_secret", clientSecret)
                .with("scopes", scopes)
                .with("note", note)
                .with("note_url", note_url);

        return requester.method("PUT").withUrlPath("/authorizations/clients/" + clientId).fetch(GHAuthorization.class);
    }

    /**
     * Delete auth.
     *
     * @param id
     *            the id
     * @throws IOException
     *             the io exception
     * @see <a href="https://developer.github.com/v3/oauth_authorizations/#delete-an-authorization">Delete an
     *      authorization</a>
     */
    public void deleteAuth(long id) throws IOException {
        createRequest().method("DELETE").withUrlPath("/authorizations/" + id).send();
    }

    /**
     * Check auth gh authorization.
     *
     * @param clientId
     *            the client id
     * @param accessToken
     *            the access token
     * @return the gh authorization
     * @throws IOException
     *             the io exception
     * @see <a href="https://developer.github.com/v3/oauth_authorizations/#check-an-authorization">Check an
     *      authorization</a>
     */
    public GHAuthorization checkAuth(@Nonnull String clientId, @Nonnull String accessToken) throws IOException {
        return createRequest().withUrlPath("/applications/" + clientId + "/tokens/" + accessToken)
                .fetch(GHAuthorization.class);
    }

    /**
     * Reset auth gh authorization.
     *
     * @param clientId
     *            the client id
     * @param accessToken
     *            the access token
     * @return the gh authorization
     * @throws IOException
     *             the io exception
     * @see <a href="https://developer.github.com/v3/oauth_authorizations/#reset-an-authorization">Reset an
     *      authorization</a>
     */
    public GHAuthorization resetAuth(@Nonnull String clientId, @Nonnull String accessToken) throws IOException {
        return createRequest().method("POST")
                .withUrlPath("/applications/" + clientId + "/tokens/" + accessToken)
                .fetch(GHAuthorization.class);
    }

    /**
     * Returns a list of all authorizations.
     *
     * @return the paged iterable
     * @throws IOException
     *             the io exception
     * @see <a href="https://developer.github.com/v3/oauth_authorizations/#list-your-authorizations">List your
     *      authorizations</a>
     */
    public PagedIterable<GHAuthorization> listMyAuthorizations() throws IOException {
        return createRequest().withUrlPath("/authorizations")
                .fetchIterable(GHAuthorization[].class, item -> item.wrap(this));
    }

    /**
     * Returns the GitHub App associated with the authentication credentials used.
     * <p>
     * You must use a JWT to access this endpoint.
     *
     * @return the app
     * @throws IOException
     *             the io exception
     * @see <a href="https://developer.github.com/v3/apps/#get-the-authenticated-github-app">Get the authenticated
     *      GitHub App</a>
     */
    @Preview
    @Deprecated
    public GHApp getApp() throws IOException {
        return createRequest().withPreview(MACHINE_MAN).withUrlPath("/app").fetch(GHApp.class).wrapUp(this);
    }

    /**
     * Ensures that the credential is valid.
     *
     * @return the boolean
     */
    public boolean isCredentialValid() {
        return client.isCredentialValid();
    }

    /**
     * Provides a list of GitHub's IP addresses.
     *
     * @return an instance of {@link GHMeta}
     * @throws IOException
     *             if the credentials supplied are invalid or if you're trying to access it as a GitHub App via the JWT
     *             authentication
     * @see <a href="https://developer.github.com/v3/meta/#meta">Get Meta</a>
     */
    public GHMeta getMeta() throws IOException {
        return createRequest().withUrlPath("/meta").fetch(GHMeta.class);
    }

    /**
     * Gets project.
     *
     * @param id
     *            the id
     * @return the project
     * @throws IOException
     *             the io exception
     */
    public GHProject getProject(long id) throws IOException {
        return createRequest().withPreview(INERTIA).withUrlPath("/projects/" + id).fetch(GHProject.class).wrap(this);
    }

    /**
     * Gets project column.
     *
     * @param id
     *            the id
     * @return the project column
     * @throws IOException
     *             the io exception
     */
    public GHProjectColumn getProjectColumn(long id) throws IOException {
        return createRequest().withPreview(INERTIA)
                .withUrlPath("/projects/columns/" + id)
                .fetch(GHProjectColumn.class)
                .wrap(this);
    }

    /**
     * Gets project card.
     *
     * @param id
     *            the id
     * @return the project card
     * @throws IOException
     *             the io exception
     */
    public GHProjectCard getProjectCard(long id) throws IOException {
        return createRequest().withPreview(INERTIA)
                .withUrlPath("/projects/columns/cards/" + id)
                .fetch(GHProjectCard.class)
                .wrap(this);
    }

    /**
     * Tests the connection.
     *
     * <p>
     * Verify that the API URL and credentials are valid to access this GitHub.
     *
     * <p>
     * This method returns normally if the endpoint is reachable and verified to be GitHub API URL. Otherwise this
     * method throws {@link IOException} to indicate the problem.
     *
     * @throws IOException
     *             the io exception
     */
    public void checkApiUrlValidity() throws IOException {
        client.checkApiUrlValidity();
    }

    /**
     * Search commits.
     *
     * @return the gh commit search builder
     */
    @Preview
    @Deprecated
    public GHCommitSearchBuilder searchCommits() {
        return new GHCommitSearchBuilder(this);
    }

    /**
     * Search issues.
     *
     * @return the gh issue search builder
     */
    public GHIssueSearchBuilder searchIssues() {
        return new GHIssueSearchBuilder(this);
    }

    /**
     * Search users.
     *
     * @return the gh user search builder
     */
    public GHUserSearchBuilder searchUsers() {
        return new GHUserSearchBuilder(this);
    }

    /**
     * Search repositories.
     *
     * @return the gh repository search builder
     */
    public GHRepositorySearchBuilder searchRepositories() {
        return new GHRepositorySearchBuilder(this);
    }

    /**
     * Search content.
     *
     * @return the gh content search builder
     */
    public GHContentSearchBuilder searchContent() {
        return new GHContentSearchBuilder(this);
    }

    /**
     * List all the notifications.
     *
     * @return the gh notification stream
     */
    public GHNotificationStream listNotifications() {
        return new GHNotificationStream(this, "/notifications");
    }

    /**
     * This provides a dump of every public repository, in the order that they were created.
     *
     * @return the paged iterable
     * @see <a href="https://developer.github.com/v3/repos/#list-all-public-repositories">documentation</a>
     */
    public PagedIterable<GHRepository> listAllPublicRepositories() {
        return listAllPublicRepositories(null);
    }

    /**
     * This provides a dump of every public repository, in the order that they were created.
     *
     * @param since
     *            The numeric ID of the last Repository that you’ve seen. See {@link GHRepository#getId()}
     * @return the paged iterable
     * @see <a href="https://developer.github.com/v3/repos/#list-all-public-repositories">documentation</a>
     */
    public PagedIterable<GHRepository> listAllPublicRepositories(final String since) {
        return createRequest().with("since", since)
                .withUrlPath("/repositories")
                .fetchIterable(GHRepository[].class, item -> item.wrap(this));
    }

    /**
     * Render a Markdown document in raw mode.
     *
     * <p>
     * It takes a Markdown document as plaintext and renders it as plain Markdown without a repository context (just
     * like a README.md file is rendered – this is the simplest way to preview a readme online).
     *
     * @param text
     *            the text
     * @return the reader
     * @throws IOException
     *             the io exception
     * @see GHRepository#renderMarkdown(String, MarkdownMode) GHRepository#renderMarkdown(String, MarkdownMode)
     */
    public Reader renderMarkdown(String text) throws IOException {
        return new InputStreamReader(
                createRequest().method("POST")
                        .with(new ByteArrayInputStream(text.getBytes("UTF-8")))
                        .contentType("text/plain;charset=UTF-8")
                        .withUrlPath("/markdown/raw")
                        .fetchStream(),
                "UTF-8");
    }

    @Nonnull
    GitHubClient getClient() {
        return client;
    }

    @Nonnull
    Requester createRequest() {
        return new Requester(client);
    }

    GHUser intern(GHUser user) throws IOException {
        if (user == null)
            return user;

        // if we already have this user in our map, use it
        GHUser u = users.get(user.getLogin());
        if (u != null)
            return u;

        // if not, remember this new user
        users.putIfAbsent(user.getLogin(), user);
        return user;
    }

    private static final Logger LOGGER = Logger.getLogger(GitHub.class.getName());
}
