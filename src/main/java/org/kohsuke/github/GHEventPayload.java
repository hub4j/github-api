package org.kohsuke.github;

import java.io.Reader;

/**
 * Base type for types used in databinding of the event payload.
 * 
 * @see GitHub#parseEventPayload(Reader, Class)
 * @see GHEventInfo#getPayload(Class)
 */
public abstract class GHEventPayload {
    protected GitHub root;

    /*package*/ GHEventPayload() {
    }

    /*package*/ void wrapUp(GitHub root) {
        this.root = root;
    }

    public static class PullRequest extends GHEventPayload {
        private String action;
        private int number;
        GHPullRequest pull_request;

        public String getAction() {
            return action;
        }

        public int getNumber() {
            return number;
        }

        public GHPullRequest getPullRequest() {
            pull_request.root = root;
            return pull_request;
        }
    }

}
