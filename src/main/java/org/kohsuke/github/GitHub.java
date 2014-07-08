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

import static com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility.*;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.Reader;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.TimeZone;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.io.IOUtils;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.introspect.VisibilityChecker.Std;
import com.infradna.tool.bridge_method_injector.WithBridgeMethods;

/**
 * Root of the GitHub API.
 *
 * @author Kohsuke Kawaguchi
 */
public class GitHub {
    /*package*/ final String login;

    /**
     * Value of the authorization header to be sent with the request.
     */
    /*package*/ final String encodedAuthorization;

    private final Map<String,GHUser> users = new HashMap<String, GHUser>();
    private final Map<String,GHOrganization> orgs = new HashMap<String, GHOrganization>();

    private final String apiUrl;

    private HttpConnector connector = HttpConnector.DEFAULT;

    /**
     * Connects to GitHub.com
     */
    private GitHub(String login, String oauthAccessToken, String password) throws IOException {
      this (GITHUB_URL, login, oauthAccessToken, password);
    }

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
     */
    private GitHub(String apiUrl, String login, String oauthAccessToken, String password) throws IOException {
        if (apiUrl.endsWith("/")) apiUrl = apiUrl.substring(0, apiUrl.length()-1); // normalize
        this.apiUrl = apiUrl;

        if (oauthAccessToken!=null) {
            encodedAuthorization = "token "+oauthAccessToken;
        } else {
            if (password!=null) {
                String authorization = (login + ':' + password);
                encodedAuthorization = "Basic "+new String(Base64.encodeBase64(authorization.getBytes()));
            } else {// anonymous access
                encodedAuthorization = null;
            }
        }

        if (login==null && encodedAuthorization!=null)
            login = getMyself().getLogin();
        this.login = login;
    }

    /**
     * Obtains the credential from "~/.github"
     */
    public static GitHub connect() throws IOException {
        Properties props = new Properties();
        File homeDir = new File(System.getProperty("user.home"));
        FileInputStream in = new FileInputStream(new File(homeDir, ".github"));
        try {
            props.load(in);
        } finally {
            IOUtils.closeQuietly(in);
        }
        return new GitHub(GITHUB_URL,props.getProperty("login"), props.getProperty("oauth"),props.getProperty("password"));
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
        return connectUsingOAuth(apiUrl, oauthAccessToken);
    }

    public static GitHub connectToEnterprise(String apiUrl, String login, String password) throws IOException {
        return new GitHub(apiUrl, login, null, password);
    }

    public static GitHub connect(String login, String oauthAccessToken) throws IOException {
        return new GitHub(login,oauthAccessToken,null);
    }

    /**
     * @deprecated
     *      Either OAuth token or password is sufficient, so there's no point in passing both.
     *      Use {@link #connectUsingPassword(String, String)} or {@link #connectUsingOAuth(String)}.
     */
    public static GitHub connect(String login, String oauthAccessToken, String password) throws IOException {
        return new GitHub(login,oauthAccessToken,password);
    }

    public static GitHub connectUsingPassword(String login, String password) throws IOException {
        return new GitHub(login,null,password);
    }

    public static GitHub connectUsingOAuth(String oauthAccessToken) throws IOException {
    	return new GitHub(null, oauthAccessToken, null);
    }

    public static GitHub connectUsingOAuth(String githubServer, String oauthAccessToken) throws IOException {
    	return new GitHub(githubServer,null, oauthAccessToken,null);
    }
    /**
     * Connects to GitHub anonymously.
     *
     * All operations that requires authentication will fail.
     */
    public static GitHub connectAnonymously() throws IOException {
        return new GitHub(null,null,null);
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
            users.put(login,orig);
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
        Requester requester = new Requester(this)
                .with("name", name).with("description", description).with("homepage", homepage)
                .with("public", isPublic ? 1 : 0);
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

    private static final String GITHUB_URL = "https://api.github.com";
}
