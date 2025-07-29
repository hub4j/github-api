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
import java.time.Instant;
import java.util.Collections;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;

// TODO: Auto-generated Javadoc
/**
 * Drafts or updates a check run.
 *
 * @see GHCheckRun
 * @see GHRepository#createCheckRun
 * @see <a href="https://developer.github.com/v3/checks/runs/#create-a-check-run">documentation</a>
 * @see GHCheckRun#update()
 * @see <a href="https://developer.github.com/v3/checks/runs/#update-a-check-run">documentation</a>
 */
@SuppressFBWarnings(value = "URF_UNREAD_FIELD", justification = "Jackson serializes these even without a getter")
public final class GHCheckRunBuilder {

    /**
     * The Class Action.
     *
     * @see <a href="https://developer.github.com/v3/checks/runs/#actions-object">documentation</a>
     */
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static final class Action {

        private final String description;
        private final String identifier;
        private final String label;

        /**
         * Instantiates a new action.
         *
         * @param label
         *            the label
         * @param description
         *            the description
         * @param identifier
         *            the identifier
         */
        public Action(@NonNull String label, @NonNull String description, @NonNull String identifier) {
            this.label = label;
            this.description = description;
            this.identifier = identifier;
        }

    }

    /**
     * The Class Annotation.
     *
     * @see <a href="https://developer.github.com/v3/checks/runs/#annotations-object">documentation</a>
     */
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static final class Annotation {

        private final String annotationLevel;
        private Integer endColumn;
        private final int endLine;
        private final String message;
        private final String path;
        private String rawDetails;
        private Integer startColumn;
        private final int startLine;
        private String title;

        /**
         * Instantiates a new annotation.
         *
         * @param path
         *            the path
         * @param line
         *            the line
         * @param annotationLevel
         *            the annotation level
         * @param message
         *            the message
         */
        public Annotation(@NonNull String path,
                int line,
                @NonNull GHCheckRun.AnnotationLevel annotationLevel,
                @NonNull String message) {
            this(path, line, line, annotationLevel, message);
        }

        /**
         * Instantiates a new annotation.
         *
         * @param path
         *            the path
         * @param startLine
         *            the start line
         * @param endLine
         *            the end line
         * @param annotationLevel
         *            the annotation level
         * @param message
         *            the message
         */
        public Annotation(@NonNull String path,
                int startLine,
                int endLine,
                @NonNull GHCheckRun.AnnotationLevel annotationLevel,
                @NonNull String message) {
            this.path = path;
            this.startLine = startLine;
            this.endLine = endLine;
            this.annotationLevel = annotationLevel.toString().toLowerCase(Locale.ROOT);
            this.message = message;
        }

        /**
         * With end column.
         *
         * @param endColumn
         *            the end column
         * @return the annotation
         */
        public @NonNull Annotation withEndColumn(@CheckForNull Integer endColumn) {
            this.endColumn = endColumn;
            return this;
        }

        /**
         * With raw details.
         *
         * @param rawDetails
         *            the raw details
         * @return the annotation
         */
        public @NonNull Annotation withRawDetails(@CheckForNull String rawDetails) {
            this.rawDetails = rawDetails;
            return this;
        }

        /**
         * With start column.
         *
         * @param startColumn
         *            the start column
         * @return the annotation
         */
        public @NonNull Annotation withStartColumn(@CheckForNull Integer startColumn) {
            this.startColumn = startColumn;
            return this;
        }

        /**
         * With title.
         *
         * @param title
         *            the title
         * @return the annotation
         */
        public @NonNull Annotation withTitle(@CheckForNull String title) {
            this.title = title;
            return this;
        }

    }
    /**
     * The Class Image.
     *
     * @see <a href="https://developer.github.com/v3/checks/runs/#images-object">documentation</a>
     */
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static final class Image {

        private final String alt;
        private String caption;
        private final String imageUrl;

        /**
         * Instantiates a new image.
         *
         * @param alt
         *            the alt
         * @param imageURL
         *            the image URL
         */
        public Image(@NonNull String alt, @NonNull String imageURL) {
            this.alt = alt;
            this.imageUrl = imageURL;
        }

        /**
         * With caption.
         *
         * @param caption
         *            the caption
         * @return the image
         */
        public @NonNull Image withCaption(@CheckForNull String caption) {
            this.caption = caption;
            return this;
        }

    }
    /**
     * The Class Output.
     *
     * @see <a href="https://developer.github.com/v3/checks/runs/#output-object">documentation</a>
     */
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static final class Output {

        private List<Annotation> annotations;
        private List<Image> images;
        private final String summary;
        private String text;
        private final String title;

        /**
         * Instantiates a new output.
         *
         * @param title
         *            the title
         * @param summary
         *            the summary
         */
        public Output(@NonNull String title, @NonNull String summary) {
            this.title = title;
            this.summary = summary;
        }

        /**
         * Adds the.
         *
         * @param annotation
         *            the annotation
         * @return the output
         */
        public @NonNull Output add(@NonNull Annotation annotation) {
            if (annotations == null) {
                annotations = new LinkedList<>();
            }
            annotations.add(annotation);
            return this;
        }

        /**
         * Adds the.
         *
         * @param image
         *            the image
         * @return the output
         */
        public @NonNull Output add(@NonNull Image image) {
            if (images == null) {
                images = new LinkedList<>();
            }
            images.add(image);
            return this;
        }

        /**
         * With text.
         *
         * @param text
         *            the text
         * @return the output
         */
        public @NonNull Output withText(@CheckForNull String text) {
            this.text = text;
            return this;
        }

    }

    private static final int MAX_ANNOTATIONS = 50;

    private List<Action> actions;

    private Output output;

    /** The repo. */
    protected final GHRepository repo;

    /** The requester. */
    protected final Requester requester;

    private GHCheckRunBuilder(GHRepository repo, Requester requester) {
        this.repo = repo;
        this.requester = requester;
    }

    /**
     * Instantiates a new GH check run builder.
     *
     * @param repo
     *            the repo
     * @param name
     *            the name
     * @param headSHA
     *            the head SHA
     */
    GHCheckRunBuilder(GHRepository repo, String name, String headSHA) {
        this(repo,
                repo.root()
                        .createRequest()
                        .method("POST")
                        .with("name", name)
                        .with("head_sha", headSHA)
                        .withUrlPath(repo.getApiTailUrl("check-runs")));
    }

    /**
     * Instantiates a new GH check run builder.
     *
     * @param repo
     *            the repo
     * @param checkId
     *            the check id
     */
    GHCheckRunBuilder(GHRepository repo, long checkId) {
        this(repo,
                repo.root().createRequest().method("PATCH").withUrlPath(repo.getApiTailUrl("check-runs/" + checkId)));
    }

    /**
     * Adds the.
     *
     * @param action
     *            the action
     * @return the GH check run builder
     */
    public @NonNull GHCheckRunBuilder add(@NonNull Action action) {
        if (actions == null) {
            actions = new LinkedList<>();
        }
        actions.add(action);
        return this;
    }

    /**
     * Adds the.
     *
     * @param output
     *            the output
     * @return the GH check run builder
     */
    public @NonNull GHCheckRunBuilder add(@NonNull Output output) {
        if (this.output != null) {
            throw new IllegalStateException("cannot add Output twice");
        }
        this.output = output;
        return this;
    }

    /**
     * Actually creates the check run. (If more than fifty annotations were requested, this is done in batches.)
     *
     * @return the resulting run
     * @throws IOException
     *             for the usual reasons
     */
    public @NonNull GHCheckRun create() throws IOException {
        List<Annotation> extraAnnotations;
        if (output != null && output.annotations != null && output.annotations.size() > MAX_ANNOTATIONS) {
            extraAnnotations = output.annotations.subList(MAX_ANNOTATIONS, output.annotations.size());
            output.annotations = output.annotations.subList(0, MAX_ANNOTATIONS);
        } else {
            extraAnnotations = Collections.emptyList();
        }
        GHCheckRun run = requester.with("output", output).with("actions", actions).fetch(GHCheckRun.class).wrap(repo);
        while (!extraAnnotations.isEmpty()) {
            Output output2 = new Output(output.title, output.summary).withText(output.text);
            int i = Math.min(extraAnnotations.size(), MAX_ANNOTATIONS);
            output2.annotations = extraAnnotations.subList(0, i);
            extraAnnotations = extraAnnotations.subList(i, extraAnnotations.size());
            run = repo.root()
                    .createRequest()
                    .method("PATCH")
                    .with("output", output2)
                    .withUrlPath(repo.getApiTailUrl("check-runs/" + run.getId()))
                    .fetch(GHCheckRun.class)
                    .wrap(repo);
        }
        return run;
    }

    /**
     * With completed at.
     *
     * @param completedAt
     *            the completed at
     * @return the GH check run builder
     * @deprecated Use {@link #withCompletedAt(Instant)}
     */
    @Deprecated
    public @NonNull GHCheckRunBuilder withCompletedAt(@CheckForNull Date completedAt) {
        return withCompletedAt(GitHubClient.toInstantOrNull(completedAt));
    }

    /**
     * With completed at.
     *
     * @param completedAt
     *            the completed at
     * @return the GH check run builder
     */
    public @NonNull GHCheckRunBuilder withCompletedAt(@CheckForNull Instant completedAt) {
        if (completedAt != null) {
            requester.with("completed_at", GitHubClient.printInstant(completedAt));
        }
        return this;
    }

    /**
     * With conclusion.
     *
     * @param conclusion
     *            the conclusion
     * @return the GH check run builder
     */
    public @NonNull GHCheckRunBuilder withConclusion(@CheckForNull GHCheckRun.Conclusion conclusion) {
        if (conclusion != null) {
            requester.with("conclusion", conclusion.toString().toLowerCase(Locale.ROOT));
        }
        return this;
    }

    /**
     * With details URL.
     *
     * @param detailsURL
     *            the details URL
     * @return the GH check run builder
     */
    public @NonNull GHCheckRunBuilder withDetailsURL(@CheckForNull String detailsURL) {
        requester.with("details_url", detailsURL);
        return this;
    }
    /**
     * With external ID.
     *
     * @param externalID
     *            the external ID
     * @return the GH check run builder
     */
    public @NonNull GHCheckRunBuilder withExternalID(@CheckForNull String externalID) {
        requester.with("external_id", externalID);
        return this;
    }

    /**
     * With name.
     *
     * @param name
     *            the name
     * @param oldName
     *            the old name
     * @return the GH check run builder
     */
    public @NonNull GHCheckRunBuilder withName(@CheckForNull String name, String oldName) {
        if (oldName == null) {
            throw new GHException("Can not update uncreated check run");
        }
        requester.with("name", name);
        return this;
    }

    /**
     * With started at.
     *
     * @param startedAt
     *            the started at
     * @return the GH check run builder
     * @deprecated Use {@link #withStartedAt(Instant)}
     */
    @Deprecated
    public @NonNull GHCheckRunBuilder withStartedAt(@CheckForNull Date startedAt) {
        return withStartedAt(GitHubClient.toInstantOrNull(startedAt));
    }

    /**
     * With started at.
     *
     * @param startedAt
     *            the started at
     * @return the GH check run builder
     */
    public @NonNull GHCheckRunBuilder withStartedAt(@CheckForNull Instant startedAt) {
        if (startedAt != null) {
            requester.with("started_at", GitHubClient.printInstant(startedAt));
        }
        return this;
    }

    /**
     * With status.
     *
     * @param status
     *            the status
     * @return the GH check run builder
     */
    public @NonNull GHCheckRunBuilder withStatus(@CheckForNull GHCheckRun.Status status) {
        if (status != null) {
            // Do *not* use the overload taking Enum, as that s/_/-/g which would be wrong here.
            requester.with("status", status.toString().toLowerCase(Locale.ROOT));
        }
        return this;
    }

}
