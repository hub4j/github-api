/*
 * The MIT License
 *
 * Copyright (c) 2011, Eric Maupin, Kohsuke Kawaguchi
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

import java.io.IOException;
import java.net.URL;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * Represents an issue on GitHub.
 *
 * @author Eric Maupin
 * @author Kohsuke Kawaguchi
 */
public class GHIssue {
    GitHub root;
    GHRepository owner;
	
	// API v3
	private GHSmallUser assignee;
	private String state;
	private int number;
	private String closed_at;
	private int comments;
	private String body;
    private List<String> labels;
	private GHSmallUser user;
	private String title, created_at, html_url;
	private GHIssue.PullRequest pull_request;
	private GHMilestone milestone;
	private String url, updated_at;
	private int id;
	private GHSmallUser closed_by;

    /*package*/ GHIssue wrap(GHRepository owner) {
        this.owner = owner;
        this.root = owner.root;
		if(milestone != null) milestone.wrap(owner);
		if(assignee != null) assignee.wrapUp(root);
		if(user != null) user.wrapUp(root);
		if(closed_by != null) closed_by.wrapUp(root);
        return this;
    }

    /*package*/ static GHIssue[] wrap(GHIssue[] issues, GHRepository owner) {
        for (GHIssue i : issues)
            i.wrap(owner);
        return issues;
    }

    /**
     * Repository to which the issue belongs.
     */
    public GHRepository getRepository() {
        return owner;
    }

    /**
     * The description of this pull request.
     */
    public String getBody() {
        return body;
    }

    /**
     * ID.
     */
    public int getNumber() {
        return number;
    }

    /**
     * The HTML page of this issue,
     * like https://github.com/jenkinsci/jenkins/issues/100
     */
    public URL getUrl() {
        return GitHub.parseURL(html_url);
    }

    public String getTitle() {
        return title;
    }

    public GHIssueState getState() {
        return Enum.valueOf(GHIssueState.class, state.toUpperCase(Locale.ENGLISH));
    }

    public Collection<String> getLabels() {
        if(labels == null){
            return Collections.EMPTY_LIST;
        }
        return Collections.unmodifiableList(labels);
    }

    public Date getCreatedAt() {
        return GitHub.parseDate(created_at);
    }

    public Date getUpdatedAt() {
        return GitHub.parseDate(updated_at);
    }

    public Date getClosedAt() {
        return GitHub.parseDate(closed_at);
    }

    /**
     * Updates the issue by adding a comment.
     */
    public void comment(String message) throws IOException {
        new Poster(root).withCredential().with("body",message).to(getApiRoute()+"/comments",null,"POST");
    }

    private void edit(String key, Object value) throws IOException {
        new Poster(root).withCredential()._with(key, value)
                .to(getApiRoute(),null,"PATCH");
    }

    /**
     * Closes this issue.
     */
    public void close() throws IOException {
        edit("state", "closed");
    }

    /**
     * Reopens this issue.
     */
    public void reopen() throws IOException {
        edit("state", "open");
    }

    public void setTitle(String title) throws IOException {
        edit("title",title);
    }

    public void setBody(String body) throws IOException {
        edit("body",body);
    }

    public void assignTo(GHUser user) throws IOException {
        edit("assignee",user.getLogin());
    }

    public void setLabels(String... labels) throws IOException {
        edit("assignee",labels);
    }

    /**
     * Obtains all the comments associated with this issue.
	 * 
	 * @see #listComments() 
     */
	public List<GHIssueComment> getComments() throws IOException {
		return listComments().asList();
	}
	
	/**
	 * Obtains all the comments associated with this issue.
	 */
    public PagedIterable<GHIssueComment> listComments() throws IOException {
        return new PagedIterable<GHIssueComment>() {
            public PagedIterator<GHIssueComment> iterator() {
                return new PagedIterator<GHIssueComment>(root.retrievePaged(getApiRoute() + "/comments",GHIssueComment[].class,false)) {
                    protected void wrapUp(GHIssueComment[] page) {
                        for (GHIssueComment c : page)
                            c.wrapUp(GHIssue.this);
                    }
                };
            }
        };
    }

    private String getApiRoute() {
        return "/repos/"+owner.getOwnerName()+"/"+owner.getName()+"/issues/"+number;
    }

	public GHSmallUser getAssignee() {
		return assignee;
	}
	
    /**
     * User who submitted the issue.
	 * 
	 * @return May return null when IOException occures. Prefered way is getSmallUser().getUser().
	 * @see #getSmallUser() 
     */
	@Deprecated
	public GHUser getUser() {
		try {
			return user.getUser();
		} catch (IOException ex) {
			return null;
		}
	}
	
	/**
     * Shallow user who submitted the issue.
     */
	public GHSmallUser getSmallUser(){
		return user;
	}

	public GHSmallUser getClosedBy() {
		if(!"closed".equals(state)) return null;
		if(closed_by != null) return closed_by;
		
		//TODO closed_by = owner.getIssue(number).getClosed_by();
		return closed_by;
	}
	
	public int getCommentsCount(){
		return comments;
	}

	public PullRequest getPullRequest() {
		return pull_request;
	}

	public GHMilestone getMilestone() {
		return milestone;
	}

	public class PullRequest{
		private String diff_url, patch_url, html_url;
		
		public URL getDiffUrl() {
			return GitHub.parseURL(diff_url);
		}
		
		public URL getPatchUrl() {
			return GitHub.parseURL(patch_url);
		}
		
		public URL getUrl() {
			return GitHub.parseURL(html_url);
		}
	}
}