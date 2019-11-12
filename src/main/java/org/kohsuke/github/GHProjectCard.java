package org.kohsuke.github;

import org.apache.commons.lang3.StringUtils;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URL;

import static org.kohsuke.github.Previews.INERTIA;

/**
 * @author Gunnar Skjold
 */
public class GHProjectCard extends GHObject {
	private GHProject project;
	private GHProjectColumn column;

	private String note;
	private GHUser creator;
	private String content_url, project_url, column_url;
	private boolean archived;

	public URL getHtmlUrl() throws IOException {
		return null;
	}

	public GHProjectCard wrap(GitHub root) {
		this.setRoot(root);
		return this;
	}

	public GHProjectCard wrap(GHProjectColumn column) {
		this.column = column;
		this.project = column.project;
		this.setRoot(column.getRoot());
		return this;
	}

	public GHProject getProject() throws IOException {
		if(project == null) {
			try {
				project = createRequester().method("GET").to(getProjectUrl().getPath(), GHProject.class).wrap(getRoot());
			} catch (FileNotFoundException e) {
				return null;
			}
		}
		return project;
	}

	public GHProjectColumn getColumn() throws IOException {
		if(column == null) {
			try {
				column = createRequester().method("GET").to(getColumnUrl().getPath(), GHProjectColumn.class).wrap(getRoot());
			} catch (FileNotFoundException e) {
				return null;
			}
		}
		return column;
	}

	public GHIssue getContent() throws IOException {
		if(StringUtils.isEmpty(content_url))
			return null;
		try {
			if(content_url.contains("/pulls")) {
				return createRequester().method("GET").to(getContentUrl().getPath(), GHPullRequest.class).wrap(getRoot());
			} else {
				return createRequester().method("GET").to(getContentUrl().getPath(), GHIssue.class).wrap(getRoot());
			}
		} catch (FileNotFoundException e) {
			return null;
		}
	}

	public String getNote() {
		return note;
	}

	public GHUser getCreator() {
		return creator;
	}

	public URL getContentUrl() {
		return GitHub.parseURL(content_url);
	}

	public URL getProjectUrl() {
		return GitHub.parseURL(project_url);
	}

	public URL getColumnUrl() {
		return GitHub.parseURL(column_url);
	}

	public boolean isArchived() {
		return archived;
	}

	public void setNote(String note) throws IOException {
		edit("note", note);
	}

	public void setArchived(boolean archived) throws IOException {
		edit("archived", archived);
	}

	private void edit(String key, Object value) throws IOException {
		createRequester().withPreview(INERTIA).with(key, value).method("PATCH").to(getApiRoute());
	}

	protected String getApiRoute() {
		return String.format("/projects/columns/cards/%d", id);
	}

	public void delete() throws IOException {
		createRequester().withPreview(INERTIA).method("DELETE").to(getApiRoute());
	}
}
