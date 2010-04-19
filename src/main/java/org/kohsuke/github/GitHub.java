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

import org.apache.commons.io.IOUtils;
import org.codehaus.jackson.map.DeserializationConfig.Feature;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.introspect.VisibilityChecker.Std;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
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

    private final Map<String,GHUser> users = new HashMap<String, GHUser>();

    private GitHub(String login, String apiToken) {
        this.login = login;
        this.token = apiToken;
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
        return new GitHub(props.getProperty("login"),props.getProperty("token"));
    }

    public static GitHub connect(String login, String apiToken) throws IOException {
        return new GitHub(login,apiToken);
    }

    /**
     * Connects to GitHub anonymously.
     *
     * All operations that requires authentication will fail.
     */
    public static GitHub connectAnonymously() {
        return new GitHub(null,null);
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


    /*package*/ static final ObjectMapper MAPPER = new ObjectMapper();

    static {
        MAPPER.setVisibilityChecker(new Std(NONE, NONE, NONE, NONE, ANY));
        MAPPER.getDeserializationConfig().set(Feature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    }
}
