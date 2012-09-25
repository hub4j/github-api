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

import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.HtmlForm;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import com.infradna.tool.bridge_method_injector.WithBridgeMethods;
import org.apache.commons.io.IOUtils;
import org.codehaus.jackson.map.DeserializationConfig.Feature;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.introspect.VisibilityChecker.Std;
import sun.misc.BASE64Encoder;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.Reader;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.TimeZone;

import static org.codehaus.jackson.annotate.JsonAutoDetect.Visibility.*;

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

    /**
     *
     * @param githubServer
     *      The host name of the GitHub (or GitHub enterprise) server, such as "github.com".
     */
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

    public static GitHub connect(String login, String apiToken){
        return new GitHub(login,apiToken,null);
    }

    public static GitHub connect(String login, String apiToken, String password){
        return new GitHub(login,apiToken,password);
    }

    public static GitHub connect(String githubServer, String login, String apiToken, String password){
        return new GitHub(githubServer,login,apiToken,password);
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

    /*package*/ URL getApiURL(String tailApiUrl) throws IOException {
    	if (oauthAccessToken != null) {
    		// append the access token
    		tailApiUrl = tailApiUrl +  (tailApiUrl.indexOf('?')>=0 ?'&':'?') + "access_token=" + oauthAccessToken;
    	}

        if (tailApiUrl.startsWith("/"))
            if("github.com".equals(githubServer)) {
                return new URL("https://api."+githubServer+tailApiUrl);
            } else {
                //we must have an enterprise instance
                //TODO: what if it is plain text?
                return new URL("https://"+githubServer+"/api/v3"+tailApiUrl);
            }
        else
            return new URL(tailApiUrl);
    }

    /*package*/ Requester retrieve() {
        return new Requester(this).method("GET");
    }

    /**
     * Gets the current rate limit.
     */
    public GHRateLimit getRateLimit() throws IOException {
        return retrieve().to("/rate_limit", JsonRateLimit.class).rate;
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
        return getUser(tokens[0]).getRepository(tokens[1]);
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
        // TODO: pagenation
        GHEventInfo[] events = retrieve().to("/events", GHEventInfo[].class);
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
        Requester requester = new Requester(this)
                .with("name", name).with("description", description).with("homepage", homepage)
                .with("public", isPublic ? 1 : 0);
        return requester.method("POST").to("/user/repos", GHRepository.class).wrap(this);
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

    /*package*/ static final ObjectMapper MAPPER = new ObjectMapper();

    private static final String[] TIME_FORMATS = {"yyyy/MM/dd HH:mm:ss ZZZZ","yyyy-MM-dd'T'HH:mm:ss'Z'"};

    static {
        MAPPER.setVisibilityChecker(new Std(NONE, NONE, NONE, NONE, ANY));
        MAPPER.getDeserializationConfig().set(Feature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    }
}
