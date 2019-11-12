package org.kohsuke.github;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

import java.net.URL;
import java.util.Collection;
import java.util.List;

/**
 * Generated OAuth token
 *
 * @author janinko
 * @see GitHub#createToken(Collection, String, String)
 * @see <a href="http://developer.github.com/v3/oauth/#create-a-new-authorization">API documentation</a>
 */
public class GHAuthorization extends GHObject {
    public static final String USER = "user";
    public static final String USER_EMAIL = "user:email";
    public static final String USER_FOLLOW = "user:follow";
    public static final String PUBLIC_REPO = "public_repo";
    public static final String REPO = "repo";
    public static final String REPO_STATUS = "repo:status";
    public static final String DELETE_REPO = "delete_repo";
    public static final String NOTIFICATIONS = "notifications";
    public static final String GIST = "gist";
    public static final String READ_HOOK = "read:repo_hook";
    public static final String WRITE_HOOK = "write:repo_hook";
    public static final String AMIN_HOOK = "admin:repo_hook";
    public static final String READ_ORG = "read:org";
    public static final String WRITE_ORG = "write:org";
    public static final String ADMIN_ORG = "admin:org";
    public static final String READ_KEY = "read:public_key";
    public static final String WRITE_KEY = "write:public_key";
    public static final String ADMIN_KEY = "admin:public_key";

    private List<String> scopes;
    private String token;
    private String token_last_eight;
    private String hashed_token;
    private App app;
    private String note;
    private String note_url;
    private String fingerprint;
    //TODO add some user class for https://developer.github.com/v3/oauth_authorizations/#check-an-authorization ?
    //private GHUser user;

    public GitHub getRoot() {
        return root;
    }

    public List<String> getScopes() {
        return scopes;
    }

    public String getToken() {
        return token;
    }

    public String getTokenLastEight() {
        return token_last_eight;
    }

    public String getHashedToken() {
        return hashed_token;
    }

    public URL getAppUrl() {
        return GitHub.parseURL(app.url);
    }

    public String getAppName() {
        return app.name;
    }

    @SuppressFBWarnings(value = "NM_CONFUSING", 
            justification = "It's a part of the library API, cannot be changed")
    public URL getApiURL() {
        return GitHub.parseURL(url);
    }

    /**
     * @deprecated This object has no HTML URL.
     */
    @Override
    public URL getHtmlUrl() {
        return null;
    }

    public String getNote() {
        return note;
    }

    public URL getNoteUrl() {
        return GitHub.parseURL(note_url);
    }

    public String getFingerprint() {
        return fingerprint;
    }

    /*package*/ GHAuthorization wrap(GitHub root) {
        this.root = root;
        return this;
    }

    @SuppressFBWarnings(value = {"UWF_UNWRITTEN_PUBLIC_OR_PROTECTED_FIELD", "UWF_UNWRITTEN_FIELD"}, 
        justification = "JSON API")
    private static class App {
        private String url;
        private String name;
        // private String client_id; not yet used
    }
}
