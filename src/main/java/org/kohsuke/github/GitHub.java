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
import org.apache.commons.io.IOUtils;
import org.codehaus.jackson.map.DeserializationConfig.Feature;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.introspect.VisibilityChecker.Std;
import sun.misc.BASE64Encoder;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.InterruptedIOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import static org.codehaus.jackson.annotate.JsonAutoDetect.Visibility.*;

/**
 * Root of the GitHub API.
 *
 * @author Kohsuke Kawaguchi
 */
public class GitHub {
    /*package*/ final String login;
    /*package*/ final String encodedAuthorization;
    final String password;

    private final Map<String,GHUser> users = new HashMap<String, GHUser>();
    private final Map<String,GHOrganization> orgs = new HashMap<String, GHOrganization>();
	private String oauthAccessToken;

    private GitHub(String login, String apiToken, String password) {
        this.login = login;
        this.password = password;

        BASE64Encoder enc = new sun.misc.BASE64Encoder();
        if (apiToken!=null || password!=null) {
            String userpassword = apiToken!=null ? (login + "/token" + ":" + apiToken) : (login + ':'+password);
            encodedAuthorization = enc.encode(userpassword.getBytes());
        } else
            encodedAuthorization = null;
    }

    private GitHub (String oauthAccessToken) {
		this.login = null;
		this.password = null;
		this.encodedAuthorization = null;
		
		this.oauthAccessToken = oauthAccessToken;
    	
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

    public static GitHub connectUsingOAuth (String accessToken) {
    	return new GitHub(accessToken);
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
    		
    		tailApiUrl = tailApiUrl + "?access_token=" + oauthAccessToken;
    	}
    	
        return new URL("https://github.com/api/v2/json"+tailApiUrl);
        
    }

    /*package*/ <T> T retrieve(String tailApiUrl, Class<T> type) throws IOException {
        return _retrieve(tailApiUrl, type, "GET", false);
    }

    /*package*/ <T> T retrieveWithAuth(String tailApiUrl, Class<T> type) throws IOException {
        return retrieveWithAuth(tailApiUrl,type,"GET");
    }

    /*package*/ <T> T retrieveWithAuth(String tailApiUrl, Class<T> type, String method) throws IOException {
        return _retrieve(tailApiUrl, type, method, true);
    }

    private <T> T _retrieve(String tailApiUrl, Class<T> type, String method, boolean withAuth) throws IOException {
        while (true) {// loop while API rate limit is hit
        	
        	
            HttpURLConnection uc = (HttpURLConnection) getApiURL(tailApiUrl).openConnection();

            if (withAuth && this.oauthAccessToken == null)
                uc.setRequestProperty("Authorization", "Basic " + encodedAuthorization);
            
            uc.setRequestMethod(method);

            try {
                InputStreamReader r = new InputStreamReader(uc.getInputStream(), "UTF-8");
                if (type==null) {
                    String data = IOUtils.toString(r);
                    return null;
                }
                return MAPPER.readValue(r,type);
            } catch (IOException e) {
                handleApiError(e,uc);
            }
        }
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
        
        throw (IOException)new IOException(IOUtils.toString(uc.getErrorStream(),"UTF-8")).initCause(e);
    }

    /**
     * Obtains the object that represents the named user.
     */
    public GHUser getUser(String login) throws IOException {
        GHUser u = users.get(login);
        if (u==null) {
        	
        	if (oauthAccessToken != null) {
        		u = retrieve("/user/show",JsonUser.class).user;
        		u.root = this;
                users.put(u.getLogin(),u);
        	}
        	else {
            u = retrieve("/user/show/"+login,JsonUser.class).user;
            u.root = this;
            users.put(login,u);
        	}
            
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
     * Gets the {@link GHUser} that represents yourself.
     */
    public GHUser getMyself() throws IOException {
        requireCredential();
        return getUser(login);
    }

    /**
     * Creates a new repository.
     *
     * @return
     *      Newly created repository.
     */
    public GHRepository createRepository(String name, String description, String homepage, boolean isPublic) throws IOException {
        return new Poster(this).withCredential()
                .with("name", name).with("description", description).with("homepage", homepage)
                .with("public", isPublic ? 1 : 0).to("/repos/create", JsonRepository.class).wrap(this);
    }

    /**
     * Ensures that the credential is valid.
     */
    public boolean isCredentialValid() throws IOException {
        try {
            retrieveWithAuth("/user/show",JsonUser.class);
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
                return new SimpleDateFormat(f).parse(timestamp);
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
