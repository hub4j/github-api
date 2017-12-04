/*
 * The MIT License
 *
 * Copyright (c) 2011, Eric Maupin
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
 * Draft of a Pull Request review.
 *
 * @see GHPullRequest#newDraftReview(String, String, GHPullRequestReviewComment...)
 */
public class GHPullRequestReviewDraft extends GHPullRequestReviewAbstract {

    GHPullRequestReviewDraft wrapUp(GHPullRequest owner) {
        this.owner = owner;
        return this;
    }

    @Override
    public GHPullRequestReviewState getState() {
        return GHPullRequestReviewState.PENDING;
    }

    public void submit(String body, GHPullRequestReviewEvent event) throws IOException {
        new Requester(owner.root).method("POST")
                .with("body", body)
                .with("event", event.action())
                .to(getApiRoute() + "/events", this);
        this.body = body;
    }

}
