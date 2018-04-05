/*
 * GitHub API for Java
 * Copyright (C) 2009-2017 SonarSource SA
 * mailto:info AT sonarsource DOT com
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */
package org.kohsuke.github;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Builds up a creation of new {@link GHPullRequestReview}.
 *
 * @author Kohsuke Kawaguchi
 * @see GHPullRequest#createReview()
 */
public class GHPullRequestReviewBuilder {
    private final GHPullRequest pr;
    private final Requester builder;
    private final List<DraftReviewComment> comments = new ArrayList<DraftReviewComment>();

    /*package*/ GHPullRequestReviewBuilder(GHPullRequest pr) {
        this.pr = pr;
        this.builder = new Requester(pr.root);
    }

    //     public GHPullRequestReview createReview(@Nullable String commitId, String body, GHPullRequestReviewEvent event,
    //                                            List<GHPullRequestReviewComment> comments) throws IOException

    /**
     * The SHA of the commit that needs a review. Not using the latest commit SHA may render your review comment outdated if a subsequent commit modifies the line you specify as the position. Defaults to the most recent commit in the pull request when you do not specify a value.
     */
    public GHPullRequestReviewBuilder commitId(String commitId) {
        builder.with("commit_id",commitId);
        return this;
    }

    /**
     * Required when using REQUEST_CHANGES or COMMENT for the event parameter. The body text of the pull request review.
     */
    public GHPullRequestReviewBuilder body(String body) {
        builder.with("body",body);
        return this;
    }

    /**
     * The review action you want to perform. The review actions include: APPROVE, REQUEST_CHANGES, or COMMENT.
     * By leaving this blank, you set the review action state to PENDING,
     * which means you will need to {@linkplain GHPullRequestReview#submit(String, GHPullRequestReviewEvent) submit the pull request review} when you are ready.
     */
    public GHPullRequestReviewBuilder event(GHPullRequestReviewEvent event) {
        builder.with("event",event.action());
        return this;
    }

    /**
     * @param body The relative path to the file that necessitates a review comment.
     * @param path The position in the diff where you want to add a review comment. Note this value is not the same as the line number in the file. For help finding the position value, read the note below.
     * @param position Text of the review comment.
     */
    public GHPullRequestReviewBuilder comment(String body, String path, int position) {
        comments.add(new DraftReviewComment(body,path,position));
        return this;
    }

    public GHPullRequestReview create() throws IOException {
        return builder.method("POST")._with("comments",comments)
                .to(pr.getApiRoute() + "/reviews", GHPullRequestReview.class)
                .wrapUp(pr);
    }

    private static class DraftReviewComment {
        private String body;
        private String path;
        private int position;

        DraftReviewComment(String body, String path, int position) {
            this.body = body;
            this.path = path;
            this.position = position;
        }

        public String getBody() {
            return body;
        }

        public String getPath() {
            return path;
        }

        public int getPosition() {
            return position;
        }
    }
}
