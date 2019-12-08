package org.kohsuke.github;

import java.util.Locale;

/**
 * Rendering mode of markdown.
 *
 * @author Kohsuke Kawaguchi
 * @see GitHub#renderMarkdown(String)
 * @see GHRepository#renderMarkdown(String, MarkdownMode)
 */
public enum MarkdownMode {
    /**
     * Render a document as plain Markdown, just like README files are rendered.
     */
    MARKDOWN,
    /**
     * Render a document as user-content, e.g. like user comments or issues are rendered. In GFM mode, hard line breaks
     * are always taken into account, and issue and user mentions are linked accordingly.
     *
     * @see GHRepository#renderMarkdown(String, MarkdownMode)
     */
    GFM;

    public String toString() {
        return name().toLowerCase(Locale.ENGLISH);
    }
}
