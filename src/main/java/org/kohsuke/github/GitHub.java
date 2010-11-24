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
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import static org.codehaus.jackson.annotate.JsonAutoDetect.Visibility.ANY;
import static org.codehaus.jackson.annotate.JsonAutoDetect.Visibility.NONE;

/**
 * Root of the GitHub API.
 *
 * @author Kohsuke Kawaguchi
 */
public class GitHub {
    /*package*/ final String login;
    /*package*/ final String token;
    /*package*/ final String password;

    private final Map<String,GHUser> users = new HashMap<String, GHUser>();
    private final Map<String,GHOrganization> orgs = new HashMap<String, GHOrganization>();

    private GitHub(String login, String apiToken, String password) {
        this.login = login;
        this.token = apiToken;
        this.password = password;
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

    /**
     * Connects to GitHub anonymously.
     *
     * All operations that requires authentication will fail.
     */
    public static GitHub connectAnonymously() {
        return new GitHub(null,null,null);
    }

    /*package*/ void requireCredential() {
        if (login ==null || token ==null)
            throw new IllegalStateException("This operation requires a credential but none is given to the GitHub constructor");
    }

    /*package*/ URL getApiURL(String tail) throws IOException {
        return new URL("http://github.com/api/v2/json"+tail);
    }

    /*package*/ <T> T retrieve(String tail, Class<T> type) throws IOException {
        return MAPPER.readValue(getApiURL(tail),type);
    }

    /*package*/ <T> T retrieveWithAuth(URL url, Class<T> type) throws IOException {
        return retrieveWithAuth(url,type,"GET");
    }
    /*package*/ <T> T retrieveWithAuth(URL url, Class<T> type, String method) throws IOException {
        HttpURLConnection uc = (HttpURLConnection) url.openConnection();

        BASE64Encoder enc = new sun.misc.BASE64Encoder();
        String userpassword = login + "/token" + ":" + token;
        String encodedAuthorization = enc.encode(userpassword.getBytes());
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
            throw (IOException)new IOException(IOUtils.toString(uc.getErrorStream(),"UTF-8")).initCause(e);
        }
    }

    /**
     * Obtains the object that represents the named user.
     */
    public GHUser getUser(String login) throws IOException {
        GHUser u = users.get(login);
        if (u==null) {
            u = MAPPER.readValue(getApiURL("/user/show/"+login), JsonUser.class).user;
            u.root = this;
            users.put(login,u);
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
            o = MAPPER.readValue(getApiURL("/organizations/"+name), JsonOrganization.class).organization;
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
        GHRepository r = new Poster(this).withCredential()
                .with("name", name).with("description", description).with("homepage", homepage)
                .with("public", isPublic ? 1 : 0).to(getApiURL("/repos/create"), JsonRepository.class).repository;
        r.root = this;
        return r;
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


    /*package*/ static final ObjectMapper MAPPER = new ObjectMapper();

    static {
        MAPPER.setVisibilityChecker(new Std(NONE, NONE, NONE, NONE, ANY));
        MAPPER.getDeserializationConfig().set(Feature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    }
}
