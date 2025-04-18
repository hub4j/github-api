package org.kohsuke.github;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

// TODO: Auto-generated Javadoc
/**
 * Content of reactions.
 *
 * @author Kohsuke Kawaguchi
 * @see <a href="https://developer.github.com/v3/reactions/">API documentation</a>
 * @see GHReaction
 */
public enum ReactionContent {

    /** The confused. */
    CONFUSED("confused"),

    /** The eyes. */
    EYES("eyes"),

    /** The heart. */
    HEART("heart"),

    /** The hooray. */
    HOORAY("hooray"),

    /** The laugh. */
    LAUGH("laugh"),

    /** The minus one. */
    MINUS_ONE("-1"),

    /** The plus one. */
    PLUS_ONE("+1"),

    /** The rocket. */
    ROCKET("rocket");

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

    private final String content;

    /**
     * Instantiates a new reaction content.
     *
     * @param content
     *            the content
     */
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
}
