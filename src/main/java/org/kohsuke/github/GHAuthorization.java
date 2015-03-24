package org.kohsuke.github;

import java.net.URL;
import java.util.Collection;
import java.util.Date;
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

	private GitHub root;
	private List<String> scopes;
	private String token;
	private App app;
	private String note;
	private String note_url;

    public GitHub getRoot() {
        return root;
    }

	public List<String> getScopes() {
		return scopes;
	}

	public String getToken(){
		return token;
	}

	public URL getAppUrl(){
        return GitHub.parseURL(app.url);
	}

	public String getAppName() {
		return app.name;
	}
   
	public URL getApiURL(){
        return GitHub.parseURL(url);
	}

	public String getNote() {
		return note;
	}

	public URL getNoteUrl(){
        return GitHub.parseURL(note_url);
	}

	/*package*/ GHAuthorization wrap(GitHub root) {
		this.root = root;
		return this;
	}





	private static class App{
		private String url;
		private String name;
	}
}
