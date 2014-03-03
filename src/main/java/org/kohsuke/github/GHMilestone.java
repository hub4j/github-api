package org.kohsuke.github;

import java.io.IOException;
import java.util.Date;
import java.util.Locale;

/**
 * 
 * @author Yusuke Kokubo
 *
 */
public class GHMilestone {
    GitHub root;
	GHRepository owner;

	GHUser creator;
	private String state, due_on, title, url, created_at, description;
	private int closed_issues, open_issues, number;

	public GitHub getRoot() {
		return root;
	}
	
	public GHRepository getOwner() {
		return owner;
	}
	
	public GHUser getCreator() {
		return creator;
	}
	
	public Date getDueOn() {
		if (due_on == null) return null;
		return GitHub.parseDate(due_on);
	}
	
	public String getTitle() {
		return title;
	}
	
	public String getUrl() {
		return url;
	}
	
	public Date getCreatedAt() {
		return GitHub.parseDate(created_at);
	}
	
	public String getDescription() {
		return description;
	}
	
	public int getClosedIssues() {
		return closed_issues;
	}
	
	public int getOpenIssues() {
		return open_issues;
	}
	
	public int getNumber() {
		return number;
	}
	
	public GHMilestoneState getState() {
		return Enum.valueOf(GHMilestoneState.class, state.toUpperCase(Locale.ENGLISH));
	}

    /**
     * Closes this issue.
     */
    public void close() throws IOException {
        edit("state", "closed");
    }

    private void edit(String key, Object value) throws IOException {
        new Requester(root)._with(key, value).method("PATCH").to(getApiRoute());
    }

    protected String getApiRoute() {
        return "/repos/"+owner.getOwnerName()+"/"+owner.getName()+"/milestones/"+number;
    }

    public GHMilestone wrap(GHRepository repo) {
		this.owner = repo;
		this.root = repo.root;
		return this;
	}
}
