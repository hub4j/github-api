package org.kohsuke.github;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

/**
 * Content of reactions.
 *
 * @see <a href="https://developer.github.com/v3/reactions/">API documentation</a>
 * @see GHReaction
 */
public enum ReactionContent {
    PLUS_ONE("+1"),
    MINUS_ONE("-1"),
    LAUGH("laugh"),
    CONFUSED("confused"),
    HEART("heart"),
    HOORAY("hooray"),
    ROCKET("rocket"),
    EYES("eyes");

    private final String content;

    ReactionContent(String content) {
        this.content = content;
    }

    /**
     * Gets content.
     *
     * @return the content
     */
    @JsonValue
    public String getContent() {
        return content;
    }

    /**
     * For content reaction content.
     *
     * @param content
     *            the content
     * @return the reaction content
     */
    @JsonCreator
    public static ReactionContent forContent(String content) {
        for (ReactionContent c : ReactionContent.values()) {
            if (c.getContent().equals(content))
                return c;
        }
        return null;
    }
}
