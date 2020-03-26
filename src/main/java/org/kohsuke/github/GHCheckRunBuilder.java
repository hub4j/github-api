/*
 * The MIT License
 *
 * Copyright 2020 CloudBees, Inc.
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

import com.fasterxml.jackson.annotation.JsonInclude;
import edu.umd.cs.findbugs.annotations.CheckForNull;
import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

import java.io.IOException;
import java.util.Collections;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;

/**
 * Drafts a check run.
 *
 * @see GHCheckRun
 * @see GHRepository#createCheckRun
 * @see <a href="https://developer.github.com/v3/checks/runs/">documentation</a>
 */
@SuppressFBWarnings(value = "URF_UNREAD_FIELD", justification = "Jackson serializes these even without a getter")
@Preview
@Deprecated
public final class GHCheckRunBuilder {

    private final GHRepository repo;
    private final Requester requester;
    DraftOutput output;
    List<DraftAction> actions;

    GHCheckRunBuilder(GHRepository repo, String name, String headSHA) {
        this.repo = repo;
        requester = repo.root.createRequest()
                .withPreview(Previews.ANTIOPE)
                .method("POST")
                .with("name", name)
                .with("head_sha", headSHA)
                .withUrlPath(repo.getApiTailUrl("check-runs"));
    }

    public @NonNull GHCheckRunBuilder withDetailsURL(@CheckForNull String detailsURL) {
        requester.with("details_url", detailsURL);
        return this;
    }

    public @NonNull GHCheckRunBuilder withExternalID(@CheckForNull String externalID) {
        requester.with("external_id", externalID);
        return this;
    }

    public @NonNull GHCheckRunBuilder withStatus(@CheckForNull GHCheckRunStatus status) {
        if (status != null) {
            // Do *not* use the overload taking Enum, as that s/_/-/g which would be wrong here.
            requester.with("status", status.toString().toLowerCase(Locale.ROOT));
        }
        return this;
    }

    public @NonNull GHCheckRunBuilder withConclusion(@CheckForNull GHCheckRunConclusion conclusion) {
        if (conclusion != null) {
            requester.with("conclusion", conclusion.toString().toLowerCase(Locale.ROOT));
        }
        return this;
    }

    public @NonNull GHCheckRunBuilder withStartedAt(@CheckForNull Date startedAt) {
        if (startedAt != null) {
            requester.with("started_at", GitHubClient.printDate(startedAt));
        }
        return this;
    }

    public @NonNull GHCheckRunBuilder withCompletedAt(@CheckForNull Date completedAt) {
        if (completedAt != null) {
            requester.with("completed_at", GitHubClient.printDate(completedAt));
        }
        return this;
    }

    /**
     * Drafts the output section; use {@link DraftOutput#done} to continue.
     */
    public @NonNull DraftOutput withOutput(@NonNull String title, @NonNull String summary) {
        return new DraftOutput(this, title, summary);
    }

    /**
     * Drafts an action section; use {@link DraftOutput#done} to continue.
     */
    public @NonNull GHCheckRunBuilder withAction(@NonNull String label,
            @NonNull String description,
            @NonNull String identifier) {
        if (actions == null) {
            actions = new LinkedList<>();
        }
        actions.add(new DraftAction(label, description, identifier));
        return this;
    }

    private static final int MAX_ANNOTATIONS = 50;
    /**
     * Actually creates the check run. (If more than fifty annotations were requested, this is done in batches.)
     */
    public @NonNull GHCheckRun create() throws IOException {
        List<DraftAnnotation> extraAnnotations;
        if (output != null && output.annotations.size() > MAX_ANNOTATIONS) {
            extraAnnotations = output.annotations.subList(MAX_ANNOTATIONS, output.annotations.size());
            output.annotations = output.annotations.subList(0, MAX_ANNOTATIONS);
        } else {
            extraAnnotations = Collections.emptyList();
        }
        GHCheckRun run = requester.with("output", output).with("actions", actions).fetch(GHCheckRun.class).wrap(repo);
        while (!extraAnnotations.isEmpty()) {
            DraftOutput output2 = new DraftOutput(null, output.title, output.summary);
            int i = Math.min(extraAnnotations.size(), MAX_ANNOTATIONS);
            output2.annotations = extraAnnotations.subList(0, i);
            extraAnnotations = extraAnnotations.subList(i, extraAnnotations.size());
            run = repo.root.createRequest()
                    .withPreview(Previews.ANTIOPE)
                    .method("PATCH")
                    .with("output", output2)
                    .withUrlPath(repo.getApiTailUrl("check-runs/" + run.id))
                    .fetch(GHCheckRun.class)
                    .wrap(repo);
        }
        return run;
    }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static final class DraftOutput {

        private final transient GHCheckRunBuilder builder;
        private final String title;
        private final String summary;
        private String text;
        List<DraftAnnotation> annotations;
        List<DraftImage> images;

        DraftOutput(GHCheckRunBuilder builder, String title, String summary) {
            this.builder = builder;
            this.title = title;
            this.summary = summary;
        }

        public @NonNull DraftOutput withText(@CheckForNull String text) {
            this.text = text;
            return this;
        }

        /**
         * Drafts a single-line annotation section; use {@link DraftAnnotation#done} to continue.
         *
         * @param line
         *            a single line
         */
        public @NonNull DraftAnnotation withAnnotation(@NonNull String path,
                int line,
                @NonNull GHCheckRunAnnotationLevel annotationLevel,
                @NonNull String message) {
            return withAnnotation(path, line, line, annotationLevel, message);
        }

        /**
         * Drafts a potentially multiline annotation section; use {@link DraftAnnotation#done} to continue.
         */
        public @NonNull DraftAnnotation withAnnotation(@NonNull String path,
                int startLine,
                int endLine,
                @NonNull GHCheckRunAnnotationLevel annotationLevel,
                @NonNull String message) {
            return new DraftAnnotation(this, path, startLine, endLine, annotationLevel, message);
        }

        /**
         * Drafts an image section; use {@link DraftImage#done} to continue.
         */
        public @NonNull DraftImage withImage(@NonNull String alt, @NonNull String imageURL) {
            return new DraftImage(this, alt, imageURL);
        }

        public @NonNull GHCheckRunBuilder done() {
            builder.output = this;
            return builder;
        }

    }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static final class DraftAnnotation {

        private final transient DraftOutput output;
        private final String path;
        private final int start_line;
        private final int end_line;
        private final String annotation_level;
        private final String message;
        private Integer start_column;
        private Integer end_column;
        private String title;
        private String raw_details;

        DraftAnnotation(DraftOutput output,
                String path,
                int start_line,
                int end_line,
                GHCheckRunAnnotationLevel annotation_level,
                String message) {
            this.output = output;
            this.path = path;
            this.start_line = start_line;
            this.end_line = end_line;
            this.annotation_level = annotation_level.toString().toLowerCase(Locale.ROOT);
            this.message = message;
        }

        public @NonNull DraftAnnotation withStartColumn(@CheckForNull Integer startColumn) {
            start_column = startColumn;
            return this;
        }

        public @NonNull DraftAnnotation withEndColumn(@CheckForNull Integer endColumn) {
            end_column = endColumn;
            return this;
        }

        public @NonNull DraftAnnotation withTitle(@CheckForNull String title) {
            this.title = title;
            return this;
        }

        public @NonNull DraftAnnotation withRawDetails(@CheckForNull String rawDetails) {
            raw_details = rawDetails;
            return this;
        }

        public @NonNull DraftOutput done() {
            if (output.annotations == null) {
                output.annotations = new LinkedList<>();
            }
            output.annotations.add(this);
            return output;
        }

    }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static final class DraftImage {

        private final transient DraftOutput output;
        private final String alt;
        private final String image_url;
        private String caption;

        DraftImage(DraftOutput output, String alt, String image_url) {
            this.output = output;
            this.alt = alt;
            this.image_url = image_url;
        }

        public @NonNull DraftImage withCaption(@CheckForNull String caption) {
            this.caption = caption;
            return this;
        }

        public @NonNull DraftOutput done() {
            if (output.images == null) {
                output.images = new LinkedList<>();
            }
            output.images.add(this);
            return output;
        }

    }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static final class DraftAction {

        private final String label;
        private final String description;
        private final String identifier;

        DraftAction(String label, String description, String identifier) {
            this.label = label;
            this.description = description;
            this.identifier = identifier;
        }

    }

}
