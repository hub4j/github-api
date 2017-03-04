package org.kohsuke.github;

public class GHPermissions {
	private boolean admin;
	private boolean push;
	private boolean pull;
	
	public boolean isAdmin() {
		return admin;
	}
	public void setAdmin(boolean admin) {
		this.admin = admin;
	}
	public boolean isPush() {
		return push;
	}
	public void setPush(boolean push) {
		this.push = push;
	}
	public boolean isPull() {
		return pull;
	}
	public void setPull(boolean pull) {
		this.pull = pull;
	}
	
}