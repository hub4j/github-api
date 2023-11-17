package org.kohsuke.github;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.net.URL;

/**
 * Reactions for a Pull Request Review comment.
 *
 * @author Vasilis Gakias
 * @see <a href="https://docs.github.com/en/rest/pulls/comments#get-a-review-comment-for-a-pull-request">API
 *      documentation in the response schema</a>
 * @see GHPullRequestReviewComment
 */
public class GHPullRequestReviewCommentReactions {

    private String url;

    private int total_count = -1;
    @JsonProperty("+1")
    private int plus_one = -1;
    @JsonProperty("-1")
    private int minus_one = -1;
    private int laugh = -1;
    private int confused = -1;
    private int heart = -1;
    private int hooray = -1;
    private int eyes = -1;
    private int rocket = -1;

    /**
     * Gets the URL of the comment's reactions
     *
     * @return the URL of the comment's reactions
     */
    public URL getUrl() {
        return GitHubClient.parseURL(url);
    }

    /**
     * Gets the total count of reactions
     *
     * @return the number of total reactions
     */
    public int getTotalCount() {
        return total_count;
    }

    /**
     * Gets the number of +1 reactions
     *
     * @return the number of +1 reactions
     */
    public int getPlusOne() {
        return plus_one;
    }

    /**
     * Gets the number of -1 reactions
     *
     * @return the number of -1 reactions
     */
    public int getMinusOne() {
        return minus_one;
    }

    /**
     * Gets the number of laugh reactions
     *
     * @return the number of laugh reactions
     */
    public int getLaugh() {
        return laugh;
    }

    /**
     * Gets the number of confused reactions
     *
     * @return the number of confused reactions
     */
    public int getConfused() {
        return confused;
    }

    /**
     * Gets the number of heart reactions
     *
     * @return the number of heart reactions
     */
    public int getHeart() {
        return heart;
    }

    /**
     * Gets the number of hooray reactions
     *
     * @return the number of hooray reactions
     */
    public int getHooray() {
        return hooray;
    }

    /**
     * Gets the number of eyes reactions
     *
     * @return the number of eyes reactions
     */
    public int getEyes() {
        return eyes;
    }

    /**
     * Gets the number of rocket reactions
     *
     * @return the number of rocket reactions
     */
    public int getRocket() {
        return rocket;
    }
}
