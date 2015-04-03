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

import java.io.IOException;

/**
 * Review comment to the pull request
 *
 * @author Julien Henry
 */
public class GHPullRequestReviewComment extends GHObject {
  GHPullRequest owner;

  private String body;
  private GHUser user;
  private String path;
  private int position;
  private int originalPosition;

  /* package */GHPullRequestReviewComment wrapUp(GHPullRequest owner) {
    this.owner = owner;
    return this;
  }

  /**
   * Gets the pull request to which this review comment is associated.
   */
  public GHPullRequest getParent() {
    return owner;
  }

  /**
   * The comment itself.
   */
  public String getBody() {
    return body;
  }

  /**
   * Gets the user who posted this comment.
   */
  public GHUser getUser() throws IOException {
    return owner.root.getUser(user.getLogin());
  }

  public String getPath() {
    return path;
  }

  public int getPosition() {
    return position;
  }

  public int getOriginalPosition() {
    return originalPosition;
  }
}
