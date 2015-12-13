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

import static com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility.ANY;
import static com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility.NONE;

import java.io.ByteArrayInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;

import org.apache.commons.codec.Charsets;
import org.apache.commons.codec.binary.Base64;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.introspect.VisibilityChecker.Std;
import com.infradna.tool.bridge_method_injector.WithBridgeMethods;
import java.nio.charset.Charset;

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

    private final Map<String,GHUser> users = new Hashtable<String, GHUser>();
    private final Map<String,GHOrganization> orgs = new Hashtable<String, GHOrganization>();

    private final String apiUrl;

    /*package*/ final RateLimitHandler rateLimitHandler;

    private HttpConnector connector = HttpConnector.DEFAULT;

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
    /* package */ GitHub(String apiUrl, String login, String oauthAccessToken, String password, HttpConnector connector, RateLimitHandler rateLimitHandler) throws IOException {
        if (apiUrl.endsWith("/")) apiUrl = apiUrl.substring(0, apiUrl.length()-1); // normalize
        this.apiUrl = apiUrl;
        if (null != connector) this.connector = connector;

        if (oauthAccessToken!=null) {
            encodedAuthorization = "token "+oauthAccessToken;
        } else {
            if (password!=null) {
                String authorization = (login + ':' + password);
                Charset charset = Charsets.UTF_8;
                encodedAuthorization = "Basic "+new String(Base64.encodeBase64(authorization.getBytes(charset)), charset);
            } else {// anonymous access
                encodedAuthorization = null;
            }
        }

        this.rateLimitHandler = rateLimitHandler;

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
     * @param apiUrl
     *      The URL of GitHub (or GitHub enterprise) API endpoint, such as "https://api.github.com" or
     *      "http://ghe.acme.com/api/v3". Note that GitHub Enterprise has <tt>/api/v3</tt> in the URL.
     *      For historical reasons, this parameter still accepts the bare domain name, but that's considered deprecated.
     */
    public static GitHub connectToEnterprise(String apiUrl, String oauthAccessToken) throws IOException {
        return new GitHubBuilder().withEndpoint(apiUrl).withOAuthToken(oauthAccessToken).build();
    }

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
     * Is this an anonymous connection
     * @return {@code true} if operations that require authentication will fail.
     */
    public boolean isAnonymous() {
        return login==null && encodedAuthorization==null;
    }

    public HttpConnector getConnector() {
        return connector;
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
            return retrieve().to("/rate_limit", JsonRateLimit.class).rate;
        } catch (FileNotFoundException e) {
            // GitHub Enterprise doesn't have the rate limit, so in that case
            // return some big number that's not too big.
            // see issue #78
            GHRateLimit r = new GHRateLimit();
            r.limit = r.remaining = 1000000;
            r.reset = new Date(System.currentTimeMillis() + TimeUnit.HOURS.toMillis(1));
            return r;
        }
    }

    /**
     * Gets the {@link GHUser} that represents yourself.
     */
    @WithBridgeMethods(GHUser.class)
    public GHMyself getMyself() throws IOException {
        requireCredential();

        GHMyself u = retrieve().to("/user", GHMyself.class);

        u.root = this;
        users.put(u.getLogin(), u);

        return u;
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
    protected GHUser getUser(GHUser orig) throws IOException {
        GHUser u = users.get(orig.getLogin());
        if (u==null) {
            orig.root = this;
            users.put(orig.getLogin(),orig);
            return orig;
        }
        return u;
    }

    public GHOrganization getOrganization(String name) throws IOException {
        GHOrganization o = orgs.get(name);
        if (o==null) {
            o = retrieve().to("/orgs/" + name, GHOrganization.class).wrapUp(this);
            orgs.put(name,o);
        }
        return o;
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
     * To create a repository in an organization, see
     * {@link GHOrganization#createRepository(String, String, String, GHTeam, boolean)}
     *
     * @return
     *      Newly created repository.
     */
    public GHRepository createRepository(String name, String description, String homepage, boolean isPublic) throws IOException {
        return createRepository(name, description, homepage, isPublic, false);
    }

    /**
     * Creates a new repository.
     *
     * To create a repository in an organization, see
     * {@link GHOrganization#createRepository(String, String, String, GHTeam, boolean, boolean)}
     *
     * @return
     *      Newly created repository.
     */
    public GHRepository createRepository(String name, String description, String homepage, boolean isPublic, boolean autoInit) throws IOException {
        Requester requester = new Requester(this)
            .with("name", name).with("description", description).with("homepage", homepage)
            .with("public", isPublic ? 1 : 0)
            .with("auto_init", autoInit);
        return requester.method("POST").to("/user/repos", GHRepository.class).wrap(this);
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
     * Ensures that the credential is valid.
     */
    public boolean isCredentialValid() throws IOException {
        try {
            retrieve().to("/user", GHUser.class);
            return true;
        } catch (IOException e) {
            return false;
        }
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
     * Ensures that the API URL is valid.
     *
     * <p>
     * This method returns normally if the endpoint is reachable and verified to be GitHub API URL.
     * Otherwise this method throws {@link IOException} to indicate the problem.
     */
    public void checkApiUrlValidity() throws IOException {
        retrieve().to("/", GHApiInfo.class).check(apiUrl);
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
     *      The integer ID of the last Repository that you’ve seen. See {@link GHRepository#getId()}
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
    }

    /* package */ static final String GITHUB_URL = "https://api.github.com";
}
