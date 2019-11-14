package org.kohsuke.github;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

/**
 * Content of reactions.
 *
 * @author Kohsuke Kawaguchi
 * @see <a href="https://developer.github.com/v3/reactions/">API documentation</a>
 * @see GHReaction
 */
public enum ReactionContent {
    PLUS_ONE("+1"), MINUS_ONE("-1"), LAUGH("laugh"), CONFUSED("confused"), HEART("heart"), HOORAY("hooray");

    private final String content;

    ReactionContent(String content) {
        this.content = content;
    }

    @JsonValue
    public String getContent() {
        return content;
    }

    @JsonCreator
    public static ReactionContent forContent(String content) {
        for (ReactionContent c : ReactionContent.values()) {
            if (c.getContent().equals(content))
                return c;
        }
        return null;
    }
}
