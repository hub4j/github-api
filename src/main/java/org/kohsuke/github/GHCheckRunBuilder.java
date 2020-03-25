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

import edu.umd.cs.findbugs.annotations.CheckForNull;
import edu.umd.cs.findbugs.annotations.NonNull;

import java.io.IOException;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

/**
 * Drafts a check run.
 *
 * @see GHCheckRun
 * @see GHRepository#createCheckRun
 * @see <a href="https://developer.github.com/v3/checks/runs/">documentation</a>
 */
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

    public @NonNull GHCheckRunBuilder withStatus(@CheckForNull /* TODO enum? */String status) {
        requester.with("status", status);
        return this;
    }

    public @NonNull GHCheckRunBuilder withConclusion(@CheckForNull /* TODO enum? */String conclusion) {
        requester.with("conclusion", conclusion);
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

    public @NonNull DraftOutput withOutput(@NonNull String title, @NonNull String summary) {
        return new DraftOutput(this, title, summary);
    }

    public @NonNull GHCheckRunBuilder withAction(@NonNull String label,
            @NonNull String description,
            @NonNull String identifier) {
        if (actions == null) {
            actions = new LinkedList<>();
        }
        actions.add(new DraftAction(label, description, identifier));
        return this;
    }

    public @NonNull GHCheckRun create() throws IOException {
        return requester
                // TODO if >50 annotations, https://developer.github.com/v3/checks/runs/#update-a-check-run
                .with("output", output)
                .with("actions", actions)
                .fetch(GHCheckRun.class)
                .wrap(repo);
    }

    public static final class DraftOutput {

        private final GHCheckRunBuilder builder;
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

        public @NonNull DraftAnnotation withAnnotation(@NonNull String path,
                int line,
                @NonNull String annotationLevel,
                @NonNull String message) {
            return withAnnotation(path, line, line, annotationLevel, message);
        }

        public @NonNull DraftAnnotation withAnnotation(@NonNull String path,
                int startLine,
                int endLine,
                @NonNull /* TODO enum? */String annotationLevel,
                @NonNull String message) {
            return new DraftAnnotation(this, path, startLine, endLine, annotationLevel, message);
        }

        public @NonNull DraftImage withImage(@NonNull String alt, @NonNull String imageURL) {
            return new DraftImage(this, alt, imageURL);
        }

        public @NonNull GHCheckRunBuilder done() {
            builder.output = this;
            return builder;
        }

        public String getTitle() {
            return title;
        }

        public String getSummary() {
            return summary;
        }

        public String getText() {
            return text;
        }

        public List<DraftAnnotation> getAnnotations() {
            return annotations;
        }

        public List<DraftImage> getImages() {
            return images;
        }

    }

    public static final class DraftAnnotation {

        private final DraftOutput output;
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
                String annotation_level,
                String message) {
            this.output = output;
            this.path = path;
            this.start_line = start_line;
            this.end_line = end_line;
            this.annotation_level = annotation_level;
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

        public String getPath() {
            return path;
        }

        public int getStart_line() {
            return start_line;
        }

        public int getEnd_line() {
            return end_line;
        }

        public Integer getStart_column() {
            return start_column;
        }

        public Integer getEnd_column() {
            return end_column;
        }

        public String getAnnotation_level() {
            return annotation_level;
        }

        public String getMessage() {
            return message;
        }

        public String getTitle() {
            return title;
        }

        public String getRaw_details() {
            return raw_details;
        }

    }

    public static final class DraftImage {

        private final DraftOutput output;
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

        public String getAlt() {
            return alt;
        }

        public String getImage_url() {
            return image_url;
        }

        public String getCaption() {
            return caption;
        }

    }

    public static final class DraftAction {

        private final String label;
        private final String description;
        private final String identifier;

        DraftAction(String label, String description, String identifier) {
            this.label = label;
            this.description = description;
            this.identifier = identifier;
        }

        public String getLabel() {
            return label;
        }

        public String getDescription() {
            return description;
        }

        public String getIdentifier() {
            return identifier;
        }

    }

}
