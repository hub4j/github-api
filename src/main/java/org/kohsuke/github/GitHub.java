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

import static org.codehaus.jackson.annotate.JsonAutoDetect.Visibility.ANY;
import static org.codehaus.jackson.annotate.JsonAutoDetect.Visibility.NONE;
import static org.kohsuke.github.ApiVersion.V2;
import static org.kohsuke.github.ApiVersion.V3;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.InterruptedIOException;
import java.io.Reader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Properties;
import java.util.TimeZone;
import java.util.zip.GZIPInputStream;

import com.infradna.tool.bridge_method_injector.WithBridgeMethods;
import org.apache.commons.io.IOUtils;
import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.map.DeserializationConfig.Feature;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.introspect.VisibilityChecker.Std;

import sun.misc.BASE64Encoder;

import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.HtmlForm;
import com.gargoylesoftware.htmlunit.html.HtmlPage;

/**
 * Root of the GitHub API.
 *
 * @author Kohsuke Kawaguchi
 */
public class GitHub {
    /*package*/ final String login;
    /*package*/ final String encodedAuthorization;
    /*package*/ final String password;
    /*package*/ final String apiToken;

    private final Map<String,GHUser> users = new HashMap<String, GHUser>();
    private final Map<String,GHOrganization> orgs = new HashMap<String, GHOrganization>();
	/*package*/ String oauthAccessToken;
	
	private final String githubServer;

	private GitHub(String login, String apiToken, String password) {
		this ("github.com", login, apiToken, password);
	}
	
    private GitHub(String githubServer, String login, String apiToken, String password) {
        this.githubServer = githubServer;
		this.login = login;
        this.apiToken = apiToken;
        this.password = password;

        BASE64Encoder enc = new sun.misc.BASE64Encoder();
        if (apiToken!=null || password!=null) {
            String userpassword = password==null ? (login + "/token" + ":" + apiToken) : (login + ':'+password);
            encodedAuthorization = enc.encode(userpassword.getBytes());
        } else
            encodedAuthorization = null;
    }

    private GitHub (String githubServer, String oauthAccessToken) throws IOException {
    	
		this.githubServer = githubServer;
		this.password = null;
		this.encodedAuthorization = null;
		
		this.oauthAccessToken = oauthAccessToken;
        this.apiToken = oauthAccessToken;
		
		this.login = getMyself().getLogin();
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
        return new GitHub(props.getProperty("login"),props.getProperty("token"),props.getProperty("password"));
    }

    public static GitHub connect(String login, String apiToken) throws IOException {
        return new GitHub(login,apiToken,null);
    }

    public static GitHub connect(String login, String apiToken, String password) throws IOException {
        return new GitHub(login,apiToken,password);
    }

    public static GitHub connectUsingOAuth (String accessToken) throws IOException {
    	return connectUsingOAuth("github.com", accessToken);
    }
    
    public static GitHub connectUsingOAuth (String githubServer, String accessToken) throws IOException {
    	return new GitHub(githubServer, accessToken);
    }
    /**
     * Connects to GitHub anonymously.
     *
     * All operations that requires authentication will fail.
     */
    public static GitHub connectAnonymously() {
        return new GitHub(null,null,null);
    }

    /*package*/ void requireCredential() {
        if ((login==null || encodedAuthorization==null) && oauthAccessToken == null)
            throw new IllegalStateException("This operation requires a credential but none is given to the GitHub constructor");
    }

    /*package*/ URL getApiURL(ApiVersion v, String tailApiUrl) throws IOException {
    	if (oauthAccessToken != null) {
    		// append the access token
    		tailApiUrl = tailApiUrl +  (tailApiUrl.indexOf('?')>=0 ?'&':'?') + "access_token=" + oauthAccessToken;
    	}
    	
        return new URL(v.getApiVersionBaseUrl(githubServer)+tailApiUrl);
    }

    /*package*/ <T> T retrieve(String tailApiUrl, Class<T> type) throws IOException {
        return _retrieve(tailApiUrl, type, "GET", false, V2);
    }

    /*package*/ <T> T retrieve3(String tailApiUrl, Class<T> type) throws IOException {
        return _retrieve(tailApiUrl, type, "GET", false, V3);
    }

    /*package*/ <T> T retrieveWithAuth(String tailApiUrl, Class<T> type) throws IOException {
        return retrieveWithAuth(tailApiUrl, type, "GET");
    }

    /*package*/ <T> T retrieveWithAuth3(String tailApiUrl, Class<T> type) throws IOException {
        return _retrieve(tailApiUrl, type, "GET", true, V3);
    }

    /*package*/ <T> T retrieveWithAuth(String tailApiUrl, Class<T> type, String method) throws IOException {
        return _retrieve(tailApiUrl, type, method, true, V2);
    }

    /*package*/ <T> T retrieveWithAuth3(String tailApiUrl, Class<T> type, String method) throws IOException {
        return _retrieve(tailApiUrl, type, method, true, V3);
    }

    private <T> T _retrieve(String tailApiUrl, Class<T> type, String method, boolean withAuth, ApiVersion v) throws IOException {
        while (true) {// loop while API rate limit is hit
            HttpURLConnection uc = setupConnection(method, withAuth, getApiURL(v, tailApiUrl));
            try {
                return parse(uc,type);
            } catch (IOException e) {
                handleApiError(e,uc);
            }
        }
    }

    /**
     * Loads pagenated resources.
     *
     * Every iterator call reports a new batch.
     */
    /*package*/ <T> Iterator<T> retrievePaged(final String tailApiUrl, final Class<T> type, final boolean withAuth, final ApiVersion v) {
        return new Iterator<T>() {
            /**
             * The next batch to be returned from {@link #next()}.
             */
            T next;
            /**
             * URL of the next resource to be retrieved, or null if no more data is available.
             */
            URL url;

            {
                try {
                    url = getApiURL(v, tailApiUrl);
                } catch (IOException e) {
                    throw new Error(e);
                }
            }

            public boolean hasNext() {
                fetch();
                return next!=null;
            }

            public T next() {
                fetch();
                T r = next;
                if (r==null)    throw new NoSuchElementException();
                next = null;
                return r;
            }

            public void remove() {
                throw new UnsupportedOperationException();
            }

            private void fetch() {
                if (next!=null) return; // already fetched
                if (url==null)  return; // no more data to fetch

                try {
                    while (true) {// loop while API rate limit is hit
                        HttpURLConnection uc = setupConnection("GET", withAuth, url);
                        try {
                            next = parse(uc,type);
                            assert next!=null;
                            findNextURL(uc);
                            return;
                        } catch (IOException e) {
                            handleApiError(e,uc);
                        }
                    }
                } catch (IOException e) {
                    throw new Error(e);
                }
            }

            /**
             * Locate the next page from the pagination "Link" tag.
             */
            private void findNextURL(HttpURLConnection uc) throws MalformedURLException {
                url = null; // start defensively
                String link = uc.getHeaderField("Link");
                if (link==null) return;

                for (String token : link.split(", ")) {
                    if (token.endsWith("rel=\"next\"")) {
                        // found the next page. This should look something like
                        // <https://api.github.com/repos?page=3&per_page=100>; rel="next"
                        int idx = token.indexOf('>');
                        url = new URL(token.substring(1,idx));
                        return;
                    }
                }

                // no more "next" link. we are done.
            }
        };
    }

    private HttpURLConnection setupConnection(String method, boolean withAuth, URL url) throws IOException {
        HttpURLConnection uc = (HttpURLConnection) url.openConnection();

        if (withAuth && this.oauthAccessToken == null)
            uc.setRequestProperty("Authorization", "Basic " + encodedAuthorization);

        uc.setRequestMethod(method);
        if (method.equals("PUT")) {
            uc.setDoOutput(true);
            uc.setRequestProperty("Content-Length","0");
            uc.getOutputStream().close();
        }
        uc.setRequestProperty("Accept-Encoding", "gzip");
        return uc;
    }

    private <T> T parse(HttpURLConnection uc, Class<T> type) throws IOException {
        InputStreamReader r = null;
        try {
            r = new InputStreamReader(wrapStream(uc, uc.getInputStream()), "UTF-8");
            if (type==null) {
                String data = IOUtils.toString(r);
                return null;
            }
            return MAPPER.readValue(r,type);
        } finally {
            IOUtils.closeQuietly(r);
        }
    }

    /**
     * Handles the "Content-Encoding" header.
     */
    private InputStream wrapStream(HttpURLConnection uc, InputStream in) throws IOException {
        String encoding = uc.getContentEncoding();
        if (encoding==null) return in;
        if (encoding.equals("gzip"))    return new GZIPInputStream(in);

        throw new UnsupportedOperationException("Unexpected Content-Encoding: "+encoding);
    }

    /**
     * If the error is because of the API limit, wait 10 sec and return normally.
     * Otherwise throw an exception reporting an error.
     */
    /*package*/ void handleApiError(IOException e, HttpURLConnection uc) throws IOException {
        if ("0".equals(uc.getHeaderField("X-RateLimit-Remaining"))) {
            // API limit reached. wait 10 secs and return normally
            try {
                Thread.sleep(10000);
                return;
            } catch (InterruptedException _) {
                throw (InterruptedIOException)new InterruptedIOException().initCause(e);
            }
        }
        
        if (e instanceof FileNotFoundException)
            throw e;    // pass through 404 Not Found to allow the caller to handle it intelligently

        InputStream es = wrapStream(uc, uc.getErrorStream());
        try {
            if (es!=null)
                throw (IOException)new IOException(IOUtils.toString(es,"UTF-8")).initCause(e);
            else
                throw e;
        } finally {
            IOUtils.closeQuietly(es);
        }
    }

    /**
	 * Gets the {@link GHUser} that represents yourself.
	 */
    @WithBridgeMethods(GHUser.class)
	public GHMyself getMyself() throws IOException {
		requireCredential();

        GHMyself u = retrieveWithAuth3("/user", GHMyself.class);

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
            u = retrieve3("/users/" + login, GHUser.class);
            u.root = this;
            users.put(u.getLogin(), u);
		}
		return u;
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
            o = retrieve("/organizations/"+name,JsonOrganization.class).organization;
            o.root = this;
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
        return getUser(tokens[0]).getRepository(tokens[1]);
    }

    public Map<String, GHOrganization> getMyOrganizations() throws IOException {
    	 return retrieveWithAuth("/organizations",JsonOrganizations.class).wrap(this);
    }

    /**
     * Public events visible to you. Equivalent of what's displayed on https://github.com/
     */
    public List<GHEventInfo> getEvents() throws IOException {
        // TODO: pagenation
        GHEventInfo[] events = retrieve3("/events", GHEventInfo[].class);
        for (GHEventInfo e : events)
            e.wrapUp(this);
        return Arrays.asList(events);
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
     */
    public GHRepository createRepository(String name, String description, String homepage, boolean isPublic) throws IOException {
        return new Poster(this,V3).withCredential()
                .with("name", name).with("description", description).with("homepage", homepage)
                .with("public", isPublic ? 1 : 0).to("/user/repos", GHRepository.class,"POST").wrap(this);
    }

    /**
     * Ensures that the credential is valid.
     */
    public boolean isCredentialValid() throws IOException {
        try {
            retrieveWithAuth3("/user",GHUser.class);
            return true;
        } catch (IOException e) {
            return false;
        }
    }

    WebClient createWebClient() throws IOException {
        WebClient wc = new WebClient();
        wc.setJavaScriptEnabled(false);
        wc.setCssEnabled(false);
        HtmlPage pg = (HtmlPage)wc.getPage("https://github.com/login");
        HtmlForm f = pg.getForms().get(0);
        f.getInputByName("login").setValueAttribute(login);
        f.getInputByName("password").setValueAttribute(password);
        f.submit();
        return wc;
    }

    /*package*/ static URL parseURL(String s) {
        try {
            return s==null ? null : new URL(s);
        } catch (MalformedURLException e) {
            throw new IllegalStateException("Invalid URL: "+s);
        }
    }

    /*package*/ static Date parseDate(String timestamp) {
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

    /*package*/ static final ObjectMapper MAPPER = new ObjectMapper();

    private static final String[] TIME_FORMATS = {"yyyy/MM/dd HH:mm:ss ZZZZ","yyyy-MM-dd'T'HH:mm:ss'Z'"};

    static {
        MAPPER.setVisibilityChecker(new Std(NONE, NONE, NONE, NONE, ANY));
        MAPPER.getDeserializationConfig().set(Feature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    }
}
