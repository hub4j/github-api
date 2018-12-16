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

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.introspect.VisibilityChecker.Std;
import com.infradna.tool.bridge_method_injector.WithBridgeMethods;
import org.apache.commons.codec.Charsets;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.io.IOUtils;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;
import java.io.ByteArrayInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TimeZone;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.logging.Logger;

import static com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility.*;
import static java.net.HttpURLConnection.*;
import static java.util.logging.Level.*;
import static org.kohsuke.github.Previews.*;

/**
 * Root of the GitHub API.
 *
 * <h2>Thread safety</h2>
 * <p>
 * This library aims to be safe for use by multiple threads concurrently, although
 * the library itself makes no attempt to control/serialize potentially conflicting
 * operations to GitHub, such as updating &amp; deleting a repository at the same time.
 *
 * @author Kohsuke Kawaguchi
 */
public class GitHub {
    /*package*/ final String login;

    /**
     * Value of the authorization header to be sent with the request.
     */
    /*package*/ final String encodedAuthorization;

    private final ConcurrentMap<String,GHUser> users;
    private final ConcurrentMap<String,GHOrganization> orgs;
    // Cache of myself object.
    private GHMyself myself;
    private final String apiUrl;

    /*package*/ final RateLimitHandler rateLimitHandler;
    /*package*/ final AbuseLimitHandler abuseLimitHandler;

    private HttpConnector connector = HttpConnector.DEFAULT;

    private final Object headerRateLimitLock = new Object();
    private GHRateLimit headerRateLimit = null;
    private volatile GHRateLimit rateLimit = null;

    /**
     * Creates a client API root object.
     *
     * <p>
     * Several different combinations of the login/oauthAccessToken/password parameters are allowed
     * to represent different ways of authentication.
     *
     * <dl>
     *     <dt>Loging anonymously
     *     <dd>Leave all three parameters null and you will be making HTTP requests without any authentication.
     *
     *     <dt>Log in with password
     *     <dd>Specify the login and password, then leave oauthAccessToken null.
     *         This will use the HTTP BASIC auth with the GitHub API.
     *
     *     <dt>Log in with OAuth token
     *     <dd>Specify oauthAccessToken, and optionally specify the login. Leave password null.
     *         This will send OAuth token to the GitHub API. If the login parameter is null,
     *         The constructor makes an API call to figure out the user name that owns the token.
     * </dl>
     *
     * @param apiUrl
     *      The URL of GitHub (or GitHub enterprise) API endpoint, such as "https://api.github.com" or
     *      "http://ghe.acme.com/api/v3". Note that GitHub Enterprise has <tt>/api/v3</tt> in the URL.
     *      For historical reasons, this parameter still accepts the bare domain name, but that's considered deprecated.
     *      Password is also considered deprecated as it is no longer required for api usage.
     * @param login
     *      The use ID on GitHub that you are logging in as. Can be omitted if the OAuth token is
     *      provided or if logging in anonymously. Specifying this would save one API call.
     * @param oauthAccessToken
     *      Secret OAuth token.
     * @param password
     *      User's password. Always used in conjunction with the {@code login} parameter
     * @param connector
     *      HttpConnector to use. Pass null to use default connector.
     */
    /* package */ GitHub(String apiUrl, String login, String oauthAccessToken, String password, HttpConnector connector, RateLimitHandler rateLimitHandler, AbuseLimitHandler abuseLimitHandler) throws IOException {
        if (apiUrl.endsWith("/")) apiUrl = apiUrl.substring(0, apiUrl.length()-1); // normalize
        this.apiUrl = apiUrl;
        if (null != connector) this.connector = connector;

        if (oauthAccessToken!=null) {
            encodedAuthorization = "token "+oauthAccessToken;
        } else {
            if (password!=null) {
                String authorization = (login + ':' + password);
                String charsetName = Charsets.UTF_8.name();
                encodedAuthorization = "Basic "+new String(Base64.encodeBase64(authorization.getBytes(charsetName)), charsetName);
            } else {// anonymous access
                encodedAuthorization = null;
            }
        }

        users = new ConcurrentHashMap<String, GHUser>();
        orgs = new ConcurrentHashMap<String, GHOrganization>();
        this.rateLimitHandler = rateLimitHandler;
        this.abuseLimitHandler = abuseLimitHandler;

        if (login==null && encodedAuthorization!=null)
            login = getMyself().getLogin();
        this.login = login;
    }

    /**
     * Obtains the credential from "~/.github" or from the System Environment Properties.
     */
    public static GitHub connect() throws IOException {
        return GitHubBuilder.fromCredentials().build();
    }

    /**
     * Version that connects to GitHub Enterprise.
     *
     * @deprecated
     *      Use {@link #connectToEnterpriseWithOAuth(String, String, String)}
     */
    public static GitHub connectToEnterprise(String apiUrl, String oauthAccessToken) throws IOException {
        return connectToEnterpriseWithOAuth(apiUrl,null,oauthAccessToken);
    }

    /**
     * Version that connects to GitHub Enterprise.
     *
     * @param apiUrl
     *      The URL of GitHub (or GitHub enterprise) API endpoint, such as "https://api.github.com" or
     *      "http://ghe.acme.com/api/v3". Note that GitHub Enterprise has <tt>/api/v3</tt> in the URL.
     *      For historical reasons, this parameter still accepts the bare domain name, but that's considered deprecated.
     */
    public static GitHub connectToEnterpriseWithOAuth(String apiUrl, String login, String oauthAccessToken) throws IOException {
        return new GitHubBuilder().withEndpoint(apiUrl).withOAuthToken(oauthAccessToken, login).build();
    }

    /**
     * Version that connects to GitHub Enterprise.
     *
     * @deprecated
     *      Use with caution. Login with password is not a preferred method.
     */
    public static GitHub connectToEnterprise(String apiUrl, String login, String password) throws IOException {
        return new GitHubBuilder().withEndpoint(apiUrl).withPassword(login, password).build();
    }

    public static GitHub connect(String login, String oauthAccessToken) throws IOException {
        return new GitHubBuilder().withOAuthToken(oauthAccessToken, login).build();
    }

    /**
     * @deprecated
     *      Either OAuth token or password is sufficient, so there's no point in passing both.
     *      Use {@link #connectUsingPassword(String, String)} or {@link #connectUsingOAuth(String)}.
     */
    public static GitHub connect(String login, String oauthAccessToken, String password) throws IOException {
        return new GitHubBuilder().withOAuthToken(oauthAccessToken, login).withPassword(login, password).build();
    }

    public static GitHub connectUsingPassword(String login, String password) throws IOException {
        return new GitHubBuilder().withPassword(login, password).build();
    }

    public static GitHub connectUsingOAuth(String oauthAccessToken) throws IOException {
        return new GitHubBuilder().withOAuthToken(oauthAccessToken).build();
    }

    public static GitHub connectUsingOAuth(String githubServer, String oauthAccessToken) throws IOException {
        return new GitHubBuilder().withEndpoint(githubServer).withOAuthToken(oauthAccessToken).build();
    }
    /**
     * Connects to GitHub anonymously.
     *
     * All operations that requires authentication will fail.
     */
    public static GitHub connectAnonymously() throws IOException {
        return new GitHubBuilder().build();
    }

    /**
     * Connects to GitHub Enterprise anonymously.
     *
     * All operations that requires authentication will fail.
     */
    public static GitHub connectToEnterpriseAnonymously(String apiUrl) throws IOException {
        return new GitHubBuilder().withEndpoint(apiUrl).build();
    }

    /**
     * An offline-only {@link GitHub} useful for parsing event notification from an unknown source.
     *
     * All operations that require a connection will fail.
     *
     * @return An offline-only {@link GitHub}.
     */
    public static GitHub offline() {
        try {
            return new GitHubBuilder()
                    .withEndpoint("https://api.github.invalid")
                    .withConnector(HttpConnector.OFFLINE)
                    .build();
        } catch (IOException e) {
            throw new IllegalStateException("The offline implementation constructor should not connect", e);
        }
    }

    /**
     * Is this an anonymous connection
     * @return {@code true} if operations that require authentication will fail.
     */
    public boolean isAnonymous() {
        return login==null && encodedAuthorization==null;
    }

    /**
     * Is this an always offline "connection".
     * @return {@code true} if this is an always offline "connection".
     */
    public boolean isOffline() {
        return connector == HttpConnector.OFFLINE;
    }

    public HttpConnector getConnector() {
        return connector;
    }

    public String getApiUrl() {
        return apiUrl;
    }

    /**
     * Sets the custom connector used to make requests to GitHub.
     */
    public void setConnector(HttpConnector connector) {
        this.connector = connector;
    }

    /*package*/ void requireCredential() {
        if (isAnonymous())
            throw new IllegalStateException("This operation requires a credential but none is given to the GitHub constructor");
    }

    /*package*/ URL getApiURL(String tailApiUrl) throws IOException {
        if (tailApiUrl.startsWith("/")) {
            if ("github.com".equals(apiUrl)) {// backward compatibility
                return new URL(GITHUB_URL + tailApiUrl);
            } else {
                return new URL(apiUrl + tailApiUrl);
            }
        } else {
            return new URL(tailApiUrl);
        }
    }

    /*package*/ Requester retrieve() {
        return new Requester(this).method("GET");
    }

    /**
     * Gets the current rate limit.
     */
    public GHRateLimit getRateLimit() throws IOException {
        try {
            return rateLimit = retrieve().to("/rate_limit", JsonRateLimit.class).rate;
        } catch (FileNotFoundException e) {
            // GitHub Enterprise doesn't have the rate limit, so in that case
            // return some big number that's not too big.
            // see issue #78
            GHRateLimit r = new GHRateLimit();
            r.limit = r.remaining = 1000000;
            long hour = 60L * 60L; // this is madness, storing the date as seconds in a Date object
            r.reset = new Date(System.currentTimeMillis() / 1000L + hour);
            return rateLimit = r;
        }
    }

    /*package*/ void updateRateLimit(@Nonnull GHRateLimit observed) {
        synchronized (headerRateLimitLock) {
            if (headerRateLimit == null
                    || headerRateLimit.getResetDate().getTime() < observed.getResetDate().getTime()
                    || headerRateLimit.remaining > observed.remaining) {
                headerRateLimit = observed;
                LOGGER.log(FINE, "Rate limit now: {0}", headerRateLimit);
            }
        }
    }

    /**
     * Returns the most recently observed rate limit data or {@code null} if either there is no rate limit
     * (for example GitHub Enterprise) or if no requests have been made.
     *
     * @return the most recently observed rate limit data or {@code null}.
     */
    @CheckForNull
    public GHRateLimit lastRateLimit() {
        synchronized (headerRateLimitLock) {
            return headerRateLimit;
        }
    }

    /**
     * Gets the current rate limit while trying not to actually make any remote requests unless absolutely necessary.
     *
     * @return the current rate limit data.
     * @throws IOException if we couldn't get the current rate limit data.
     */
    @Nonnull
    public GHRateLimit rateLimit() throws IOException {
        synchronized (headerRateLimitLock) {
            if (headerRateLimit != null) {
                return headerRateLimit;
            }
        }
        GHRateLimit rateLimit = this.rateLimit;
        if (rateLimit == null || rateLimit.getResetDate().getTime() < System.currentTimeMillis()) {
            rateLimit = getRateLimit();
        }
        return rateLimit;
    }

    /**
     * Gets the {@link GHUser} that represents yourself.
     */
    @WithBridgeMethods(GHUser.class)
    public GHMyself getMyself() throws IOException {
        requireCredential();
        synchronized (this) {
            if (this.myself != null) return myself;
            
            GHMyself u = retrieve().to("/user", GHMyself.class);

            u.root = this;
            this.myself = u;
            return u;
        }
    }

    /**
     * Obtains the object that represents the named user.
     */
    public GHUser getUser(String login) throws IOException {
        GHUser u = users.get(login);
        if (u == null) {
            u = retrieve().to("/users/" + login, GHUser.class);
            u.root = this;
            users.put(u.getLogin(), u);
        }
        return u;
    }

    
    /**
     * clears all cached data in order for external changes (modifications and del
     */
    public void refreshCache() {
        users.clear();
        orgs.clear();
    }

    /**
     * Interns the given {@link GHUser}.
     */
    protected GHUser getUser(GHUser orig) {
        GHUser u = users.get(orig.getLogin());
        if (u==null) {
            orig.root = this;
            users.put(orig.getLogin(),orig);
            return orig;
        }
        return u;
    }

    /**
     * Gets {@link GHOrganization} specified by name.
     */
    public GHOrganization getOrganization(String name) throws IOException {
        GHOrganization o = orgs.get(name);
        if (o==null) {
            o = retrieve().to("/orgs/" + name, GHOrganization.class).wrapUp(this);
            orgs.put(name,o);
        }
        return o;
    }

    /**
     * Gets a list of all organizations.
     */
    public PagedIterable<GHOrganization> listOrganizations() {
        return listOrganizations(null);
    }

    /**
     * Gets a list of all organizations starting after the organization identifier specified by 'since'.
     *
     * @see <a href="https://developer.github.com/v3/orgs/#parameters">List All Orgs - Parameters</a>
     */
    public PagedIterable<GHOrganization> listOrganizations(final String since) {
        return new PagedIterable<GHOrganization>() {
            @Override
            public PagedIterator<GHOrganization> _iterator(int pageSize) {
                System.out.println("page size: " + pageSize);
                return new PagedIterator<GHOrganization>(retrieve().with("since",since)
                        .asIterator("/organizations", GHOrganization[].class, pageSize)) {
                    @Override
                    protected void wrapUp(GHOrganization[] page) {
                        for (GHOrganization c : page)
                            c.wrapUp(GitHub.this);
                    }
                };
            }
        };
    }

    /**
     * Gets the repository object from 'user/reponame' string that GitHub calls as "repository name"
     *
     * @see GHRepository#getName()
     */
    public GHRepository getRepository(String name) throws IOException {
        String[] tokens = name.split("/");
        return retrieve().to("/repos/" + tokens[0] + '/' + tokens[1], GHRepository.class).wrap(this);
    }
    /**
     * Returns a list of popular open source licenses
     *
     * WARNING: This uses a PREVIEW API.
     *
     * @see <a href="https://developer.github.com/v3/licenses/">GitHub API - Licenses</a>
     *
     * @return a list of popular open source licenses
     */
    @Preview @Deprecated
    public PagedIterable<GHLicense> listLicenses() throws IOException {
        return new PagedIterable<GHLicense>() {
            public PagedIterator<GHLicense> _iterator(int pageSize) {
                return new PagedIterator<GHLicense>(retrieve().withPreview(DRAX).asIterator("/licenses", GHLicense[].class, pageSize)) {
                    @Override
                    protected void wrapUp(GHLicense[] page) {
                        for (GHLicense c : page)
                            c.wrap(GitHub.this);
                    }
                };
            }
        };
    }

    /**
     * Returns a list of all users.
     */
    public PagedIterable<GHUser> listUsers() throws IOException {
        return new PagedIterable<GHUser>() {
            public PagedIterator<GHUser> _iterator(int pageSize) {
                return new PagedIterator<GHUser>(retrieve().asIterator("/users", GHUser[].class, pageSize)) {
                    @Override
                    protected void wrapUp(GHUser[] page) {
                        for (GHUser u : page)
                            u.wrapUp(GitHub.this);
                    }
                };
            }
        };
    }

    /**
     * Returns the full details for a license
     *
     * WARNING: This uses a PREVIEW API.
     *
     * @param key The license key provided from the API
     * @return The license details
     * @see GHLicense#getKey()
     */
    @Preview @Deprecated
    public GHLicense getLicense(String key) throws IOException {
        return retrieve().withPreview(DRAX).to("/licenses/" + key, GHLicense.class);
    }

    /**
     * Gets complete list of open invitations for current user.
     */
    public List<GHInvitation> getMyInvitations() throws IOException {
        GHInvitation[] invitations = retrieve().to("/user/repository_invitations", GHInvitation[].class);
        for (GHInvitation i : invitations) {
            i.wrapUp(this);
        }
        return Arrays.asList(invitations);
    }

    /**
     * This method returns a shallowly populated organizations.
     *
     * To retrieve full organization details, you need to call {@link #getOrganization(String)}
     * TODO: make this automatic.
     */
    public Map<String, GHOrganization> getMyOrganizations() throws IOException {
        GHOrganization[] orgs = retrieve().to("/user/orgs", GHOrganization[].class);
        Map<String, GHOrganization> r = new HashMap<String, GHOrganization>();
        for (GHOrganization o : orgs) {
            // don't put 'o' into orgs because they are shallow
            r.put(o.getLogin(),o.wrapUp(this));
        }
        return r;
    }

    /**
     * Gets complete map of organizations/teams that current user belongs to.
     *
     * Leverages the new GitHub API /user/teams made available recently to
     * get in a single call the complete set of organizations, teams and permissions
     * in a single call.
     */
    public Map<String, Set<GHTeam>> getMyTeams() throws IOException {
        Map<String, Set<GHTeam>> allMyTeams = new HashMap<String, Set<GHTeam>>();
        for (GHTeam team : retrieve().to("/user/teams", GHTeam[].class)) {
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
     * Public events visible to you. Equivalent of what's displayed on https://github.com/
     */
    public List<GHEventInfo> getEvents() throws IOException {
        GHEventInfo[] events = retrieve().to("/events", GHEventInfo[].class);
        for (GHEventInfo e : events)
            e.wrapUp(this);
        return Arrays.asList(events);
    }

    /**
     * Gets a sigle gist by ID.
     */
    public GHGist getGist(String id) throws IOException {
        return retrieve().to("/gists/"+id,GHGist.class).wrapUp(this);
    }

    public GHGistBuilder createGist() {
        return new GHGistBuilder(this);
    }

    /**
     * Parses the GitHub event object.
     *
     * This is primarily intended for receiving a POST HTTP call from a hook.
     * Unfortunately, hook script payloads aren't self-descriptive, so you need
     * to know the type of the payload you are expecting.
     */
    public <T extends GHEventPayload> T parseEventPayload(Reader r, Class<T> type) throws IOException {
        T t = MAPPER.readValue(r, type);
        t.wrapUp(this);
        return t;
    }

    /**
     * Creates a new repository.
     *
     * @return
     *      Newly created repository.
     * @deprecated
     *      Use {@link #createRepository(String)} that uses a builder pattern to let you control every aspect.
     */
    public GHRepository createRepository(String name, String description, String homepage, boolean isPublic) throws IOException {
        return createRepository(name).description(description).homepage(homepage).private_(!isPublic).create();
    }

    /**
     * Starts a builder that creates a new repository.
     *
     * <p>
     * You use the returned builder to set various properties, then call {@link GHCreateRepositoryBuilder#create()}
     * to finally createa repository.
     *
     * <p>
     * To create a repository in an organization, see
     * {@link GHOrganization#createRepository(String, String, String, GHTeam, boolean)}
     */
    public GHCreateRepositoryBuilder createRepository(String name) {
        return new GHCreateRepositoryBuilder(this,"/user/repos",name);
    }

    /**
     * Creates a new authorization.
     *
     * The token created can be then used for {@link GitHub#connectUsingOAuth(String)} in the future.
     *
     * @see <a href="http://developer.github.com/v3/oauth/#create-a-new-authorization">Documentation</a>
     */
    public GHAuthorization createToken(Collection<String> scope, String note, String noteUrl) throws IOException{
        Requester requester = new Requester(this)
                .with("scopes", scope)
                .with("note", note)
                .with("note_url", noteUrl);

        return requester.method("POST").to("/authorizations", GHAuthorization.class).wrap(this);
    }

    /**
     * @see <a href="https://developer.github.com/v3/oauth_authorizations/#get-or-create-an-authorization-for-a-specific-app">docs</a>
     */
    public GHAuthorization createOrGetAuth(String clientId, String clientSecret, List<String> scopes, String note,
                                           String note_url)
            throws IOException {
        Requester requester = new Requester(this)
                .with("client_secret", clientSecret)
                .with("scopes", scopes)
                .with("note", note)
                .with("note_url", note_url);

        return requester.method("PUT").to("/authorizations/clients/" + clientId, GHAuthorization.class);
    }

    /**
     * @see <a href="https://developer.github.com/v3/oauth_authorizations/#delete-an-authorization">Delete an authorization</a>
     */
    public void deleteAuth(long id) throws IOException {
        retrieve().method("DELETE").to("/authorizations/" + id);
    }

    /**
     * @see <a href="https://developer.github.com/v3/oauth_authorizations/#check-an-authorization">Check an authorization</a>
     */
    public GHAuthorization checkAuth(@Nonnull String clientId, @Nonnull String accessToken) throws IOException {
        return retrieve().to("/applications/" + clientId + "/tokens/" + accessToken, GHAuthorization.class);
    }

    /**
     * @see <a href="https://developer.github.com/v3/oauth_authorizations/#reset-an-authorization">Reset an authorization</a>
     */
    public GHAuthorization resetAuth(@Nonnull String clientId, @Nonnull String accessToken) throws IOException {
        return retrieve().method("POST").to("/applications/" + clientId + "/tokens/" + accessToken, GHAuthorization.class);
    }

    /**
     * Ensures that the credential is valid.
     */
    public boolean isCredentialValid() throws IOException {
        try {
            retrieve().to("/user", GHUser.class);
            return true;
        } catch (IOException e) {
            if (LOGGER.isLoggable(FINE))
                LOGGER.log(FINE, "Exception validating credentials on " + this.apiUrl + " with login '" + this.login + "' " + e, e);
            return false;
        }
    }

    /*package*/ GHUser intern(GHUser user) throws IOException {
        if (user==null) return user;

        // if we already have this user in our map, use it
        GHUser u = users.get(user.getLogin());
        if (u!=null)    return u;

        // if not, remember this new user
        users.putIfAbsent(user.getLogin(),user);
        return user;
    }

    public GHProject getProject(long id) throws IOException {
        return retrieve().withPreview(INERTIA).to("/projects/"+id,GHProject.class).wrap(this);
    }

    private static class GHApiInfo {
        private String rate_limit_url;

        void check(String apiUrl) throws IOException {
            if (rate_limit_url==null)
                throw new IOException(apiUrl+" doesn't look like GitHub API URL");

            // make sure that the URL is legitimate
            new URL(rate_limit_url);
        }
    }

    /**
     * Tests the connection.
     *
     * <p>
     * Verify that the API URL and credentials are valid to access this GitHub.
     *
     * <p>
     * This method returns normally if the endpoint is reachable and verified to be GitHub API URL.
     * Otherwise this method throws {@link IOException} to indicate the problem.
     */
    public void checkApiUrlValidity() throws IOException {
        try {
            retrieve().to("/", GHApiInfo.class).check(apiUrl);
        } catch (IOException e) {
            if (isPrivateModeEnabled()) {
                throw (IOException)new IOException("GitHub Enterprise server (" + apiUrl + ") with private mode enabled").initCause(e);
            }
            throw e;
        }
    }

    /**
     * Ensures if a GitHub Enterprise server is configured in private mode.
     *
     * @return {@code true} if private mode is enabled. If it tries to use this method with GitHub, returns {@code
     * false}.
     */
    private boolean isPrivateModeEnabled() {
        try {
            HttpURLConnection uc = getConnector().connect(getApiURL("/"));
            /*
                $ curl -i https://github.mycompany.com/api/v3/
                HTTP/1.1 401 Unauthorized
                Server: GitHub.com
                Date: Sat, 05 Mar 2016 19:45:01 GMT
                Content-Type: application/json; charset=utf-8
                Content-Length: 130
                Status: 401 Unauthorized
                X-GitHub-Media-Type: github.v3
                X-XSS-Protection: 1; mode=block
                X-Frame-Options: deny
                Content-Security-Policy: default-src 'none'
                Access-Control-Allow-Credentials: true
                Access-Control-Expose-Headers: ETag, Link, X-GitHub-OTP, X-RateLimit-Limit, X-RateLimit-Remaining, X-RateLimit-Reset, X-OAuth-Scopes, X-Accepted-OAuth-Scopes, X-Poll-Interval
                Access-Control-Allow-Origin: *
                X-GitHub-Request-Id: dbc70361-b11d-4131-9a7f-674b8edd0411
                Strict-Transport-Security: max-age=31536000; includeSubdomains; preload
                X-Content-Type-Options: nosniff
             */
            try {
                return uc.getResponseCode() == HTTP_UNAUTHORIZED
                        && uc.getHeaderField("X-GitHub-Media-Type") != null;
            } finally {
                // ensure that the connection opened by getResponseCode gets closed
                try {
                    IOUtils.closeQuietly(uc.getInputStream());
                } catch (IOException ignore) {
                    // ignore
                }
                IOUtils.closeQuietly(uc.getErrorStream());
            }
        } catch (IOException e) {
            return false;
        }
    }

    /**
     * Search commits.
     */
    @Preview @Deprecated
    public GHCommitSearchBuilder searchCommits() {
        return new GHCommitSearchBuilder(this);
    }

    /**
     * Search issues.
     */
    public GHIssueSearchBuilder searchIssues() {
        return new GHIssueSearchBuilder(this);
    }

    /**
     * Search users.
     */
    public GHUserSearchBuilder searchUsers() {
        return new GHUserSearchBuilder(this);
    }

    /**
     * Search repositories.
     */
    public GHRepositorySearchBuilder searchRepositories() {
        return new GHRepositorySearchBuilder(this);
    }

    /**
     * Search content.
     */
    public GHContentSearchBuilder searchContent() {
        return new GHContentSearchBuilder(this);
    }

    /**
     * List all the notifications.
     */
    public GHNotificationStream listNotifications() {
        return new GHNotificationStream(this,"/notifications");
    }

    /**
     * This provides a dump of every public repository, in the order that they were created.
     * @see <a href="https://developer.github.com/v3/repos/#list-all-public-repositories">documentation</a>
     */
    public PagedIterable<GHRepository> listAllPublicRepositories() {
        return listAllPublicRepositories(null);
    }

    /**
     * This provides a dump of every public repository, in the order that they were created.
     *
     * @param since
     *      The numeric ID of the last Repository that you’ve seen. See {@link GHRepository#getId()}
     * @see <a href="https://developer.github.com/v3/repos/#list-all-public-repositories">documentation</a>
     */
    public PagedIterable<GHRepository> listAllPublicRepositories(final String since) {
        return new PagedIterable<GHRepository>() {
            public PagedIterator<GHRepository> _iterator(int pageSize) {
                return new PagedIterator<GHRepository>(retrieve().with("since",since).asIterator("/repositories", GHRepository[].class, pageSize)) {
                    @Override
                    protected void wrapUp(GHRepository[] page) {
                        for (GHRepository c : page)
                            c.wrap(GitHub.this);
                    }
                };
            }
        };
    }

    /**
     * Render a Markdown document in raw mode.
     *
     * <p>
     * It takes a Markdown document as plaintext and renders it as plain Markdown
     * without a repository context (just like a README.md file is rendered – this
     * is the simplest way to preview a readme online).
     *
     * @see GHRepository#renderMarkdown(String, MarkdownMode)
     */
    public Reader renderMarkdown(String text) throws IOException {
        return new InputStreamReader(
            new Requester(this)
                    .with(new ByteArrayInputStream(text.getBytes("UTF-8")))
                    .contentType("text/plain;charset=UTF-8")
                    .asStream("/markdown/raw"),
            "UTF-8");
    }

    /*package*/ static URL parseURL(String s) {
        try {
            return s==null ? null : new URL(s);
        } catch (MalformedURLException e) {
            throw new IllegalStateException("Invalid URL: "+s);
        }
    }

    /*package*/ static Date parseDate(String timestamp) {
        if (timestamp==null)    return null;
        for (String f : TIME_FORMATS) {
            try {
                SimpleDateFormat df = new SimpleDateFormat(f);
                df.setTimeZone(TimeZone.getTimeZone("GMT"));
                return df.parse(timestamp);
            } catch (ParseException e) {
                // try next
            }
        }
        throw new IllegalStateException("Unable to parse the timestamp: "+timestamp);
    }

    /*package*/ static String printDate(Date dt) {
        return new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'").format(dt);
    }

    /*package*/ static final ObjectMapper MAPPER = new ObjectMapper();

    private static final String[] TIME_FORMATS = {"yyyy/MM/dd HH:mm:ss ZZZZ","yyyy-MM-dd'T'HH:mm:ss'Z'"};

    static {
        MAPPER.setVisibilityChecker(new Std(NONE, NONE, NONE, NONE, ANY));
        MAPPER.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        MAPPER.configure(MapperFeature.ACCEPT_CASE_INSENSITIVE_ENUMS, true);
    }

    /* package */ static final String GITHUB_URL = "https://api.github.com";

    private static final Logger LOGGER = Logger.getLogger(GitHub.class.getName());
}
