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
 * @see http://developer.github.com/v3/oauth/#create-a-new-authorization
 */
public class GHAuthorization {
	public static final String USER = "user";
	public static final String USER_EMAIL = "user:email";
	public static final String USER_FOLLOW = "user:follow";
	public static final String PUBLIC_REPO = "public_repo";
	public static final String REPO = "repo";
	public static final String REPO_STATUS = "repo:status";
	public static final String DELETE_REPO = "delete_repo";
	public static final String NOTIFICATIONS = "notifications";
	public static final String GIST = "gist";

	private GitHub root;
	private int id;
	private String url;
	private List<String> scopes;
	private String token;
	private App app;
	private String note;
	private String note_url;
	private String updated_at;
	private String created_at;

    public GitHub getRoot() {
        return root;
    }

	public int getId() {
		return id;
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
	
	public Date getCreatedAt() {
        return GitHub.parseDate(created_at);
    }

    public Date getUpdatedAt() {
        return GitHub.parseDate(updated_at);
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
