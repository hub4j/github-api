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

    private String gravatar_id,body,title,state,created_at,updated_at,html_url;
    private List<String> labels;
    private int number,votes,comments;
    private int position;

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
        return Enum.valueOf(GHIssueState.class, state);
    }

    public Collection<String> getLabels() {
        return Collections.unmodifiableList(labels);
    }

    public Date getCreatedAt() {
        return GitHub.parseDate(created_at);
    }

    public Date getUpdatedAt() {
        return GitHub.parseDate(updated_at);
    }

    /**
     * Updates the issue by adding a comment.
     */
    public void comment(String message) throws IOException {
        new Poster(root).withCredential().with("comment",message).to("/issues/comment/"+
        owner.getOwnerName()+"/"+owner.getName()+"/"+number);
    }
}