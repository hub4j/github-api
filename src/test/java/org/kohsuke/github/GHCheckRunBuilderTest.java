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

import org.junit.Assert;
import org.junit.Test;
import org.kohsuke.github.GHCheckRun.Status;

import java.io.IOException;
import java.util.Date;

import static org.hamcrest.Matchers.*;

// TODO: Auto-generated Javadoc
/**
 * The Class GHCheckRunBuilderTest.
 */
@SuppressWarnings("deprecation") // preview
public class GHCheckRunBuilderTest extends AbstractGHAppInstallationTest {

    /**
     * Gets the installation github.
     *
     * @return the installation github
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     */
    protected GitHub getInstallationGithub() throws IOException {
        return getAppInstallationWithToken(jwtProvider3.getEncodedAuthorization()).root();
    }

    /**
     * Creates the check run.
     *
     * @throws Exception
     *             the exception
     */
    @Test
    public void createCheckRun() throws Exception {
        GHCheckRun checkRun = getInstallationGithub().getRepository("hub4j-test-org/test-checks")
                .createCheckRun("foo", "89a9ae301e35e667756034fdc933b1fc94f63fc1")
                .withStatus(GHCheckRun.Status.COMPLETED)
                .withConclusion(GHCheckRun.Conclusion.SUCCESS)
                .withDetailsURL("http://nowhere.net/stuff")
                .withExternalID("whatever")
                .withStartedAt(new Date(999_999_000))
                .withCompletedAt(new Date(999_999_999))
                .add(new GHCheckRunBuilder.Output("Some Title", "what happened…").withText("Hello Text!")
                        .add(new GHCheckRunBuilder.Annotation("stuff.txt",
                                1,
                                GHCheckRun.AnnotationLevel.NOTICE,
                                "hello to you too").withTitle("Look here"))
                        .add(new GHCheckRunBuilder.Image("Unikitty",
                                "https://i.pinimg.com/474x/9e/65/c0/9e65c0972294f1e10f648c9780a79fab.jpg")
                                        .withCaption("Princess Unikitty")))
                .add(new GHCheckRunBuilder.Action("Help", "what I need help with", "doit"))
                .create();
        assertThat(checkRun.getStatus(), equalTo(Status.COMPLETED));
        assertThat(checkRun.getOutput().getAnnotationsCount(), equalTo(1));
        assertThat(checkRun.getId(), equalTo(1424883286L));
        assertThat(checkRun.getOutput().getText(), equalTo("Hello Text!"));
    }

    /**
     * Creates the check run many annotations.
     *
     * @throws Exception
     *             the exception
     */
    @Test
    public void createCheckRunManyAnnotations() throws Exception {
        GHCheckRunBuilder.Output output = new GHCheckRunBuilder.Output("Big Run", "Lots of stuff here »")
                .withText("Hello Text!");

        for (int i = 0; i < 101; i++) {
            output.add(
                    new GHCheckRunBuilder.Annotation("stuff.txt", 1, GHCheckRun.AnnotationLevel.NOTICE, "hello #" + i));
        }
        GHCheckRun checkRun = getInstallationGithub().getRepository("hub4j-test-org/test-checks")
                .createCheckRun("big", "89a9ae301e35e667756034fdc933b1fc94f63fc1")
                .withConclusion(GHCheckRun.Conclusion.SUCCESS)
                .add(output)
                .create();
        assertThat(checkRun.getStatus(), equalTo(Status.COMPLETED));
        assertThat(checkRun.getOutput().getTitle(), equalTo("Big Run"));
        assertThat(checkRun.getOutput().getSummary(), equalTo("Lots of stuff here »"));
        assertThat(checkRun.getOutput().getAnnotationsCount(), equalTo(101));
        assertThat(checkRun.getOutput().getText(), equalTo("Hello Text!"));
        assertThat(checkRun.getId(), equalTo(1424883599L));
    }

    /**
     * Creates the check run no annotations.
     *
     * @throws Exception
     *             the exception
     */
    @Test
    public void createCheckRunNoAnnotations() throws Exception {
        GHCheckRun checkRun = getInstallationGithub().getRepository("hub4j-test-org/test-checks")
                .createCheckRun("quick", "89a9ae301e35e667756034fdc933b1fc94f63fc1")
                .withConclusion(GHCheckRun.Conclusion.NEUTRAL)
                .add(new GHCheckRunBuilder.Output("Quick note", "nothing more to see here"))
                .create();
        assertThat(checkRun.getStatus(), equalTo(Status.COMPLETED));
        assertThat(checkRun.getOutput().getAnnotationsCount(), equalTo(0));
        assertThat(checkRun.getId(), equalTo(1424883957L));
    }

    /**
     * Creates the pending check run.
     *
     * @throws Exception
     *             the exception
     */
    @Test
    public void createPendingCheckRun() throws Exception {
        GHCheckRun checkRun = getInstallationGithub().getRepository("hub4j-test-org/test-checks")
                .createCheckRun("outstanding", "89a9ae301e35e667756034fdc933b1fc94f63fc1")
                .withStatus(GHCheckRun.Status.IN_PROGRESS)
                .create();
        assertThat(checkRun.getStatus(), equalTo(Status.IN_PROGRESS));
        assertThat(checkRun.getConclusion(), nullValue());
        assertThat(checkRun.getId(), equalTo(1424883451L));
    }

    /**
     * Creates the check run err missing conclusion.
     *
     * @throws Exception
     *             the exception
     */
    @Test
    public void createCheckRunErrMissingConclusion() throws Exception {
        try {
            getInstallationGithub().getRepository("hub4j-test-org/test-checks")
                    .createCheckRun("outstanding", "89a9ae301e35e667756034fdc933b1fc94f63fc1")
                    .withStatus(GHCheckRun.Status.COMPLETED)
                    .create();
            fail("should have been rejected");
        } catch (HttpException x) {
            assertThat(x.getResponseCode(), equalTo(422));
            assertThat(x.getMessage(), containsString("\\\"conclusion\\\" wasn't supplied"));
            assertThat(x.getUrl(), containsString("/repos/hub4j-test-org/test-checks/check-runs"));
            assertThat(x.getResponseMessage(), containsString("Unprocessable Entity"));
        }
    }

    /**
     * Update check run.
     *
     * @throws Exception
     *             the exception
     */
    @Test
    public void updateCheckRun() throws Exception {
        GHCheckRun checkRun = getInstallationGithub().getRepository("hub4j-test-org/test-checks")
                .createCheckRun("foo", "89a9ae301e35e667756034fdc933b1fc94f63fc1")
                .withStatus(GHCheckRun.Status.IN_PROGRESS)
                .withStartedAt(new Date(999_999_000))
                .add(new GHCheckRunBuilder.Output("Some Title", "what happened…")
                        .add(new GHCheckRunBuilder.Annotation("stuff.txt",
                                1,
                                GHCheckRun.AnnotationLevel.NOTICE,
                                "hello to you too").withTitle("Look here")))
                .create();
        GHCheckRun updated = checkRun.update()
                .withStatus(GHCheckRun.Status.COMPLETED)
                .withConclusion(GHCheckRun.Conclusion.SUCCESS)
                .withCompletedAt(new Date(999_999_999))
                .create();
        assertThat(new Date(999_999_000), equalTo(updated.getStartedAt()));
        assertThat("foo", equalTo(updated.getName()));
        assertThat(checkRun.getOutput().getAnnotationsCount(), equalTo(1));
    }

    /**
     * Update check run with name.
     *
     * @throws Exception
     *             the exception
     */
    @Test
    public void updateCheckRunWithName() throws Exception {
        GHCheckRun checkRun = getInstallationGithub().getRepository("hub4j-test-org/test-checks")
                .createCheckRun("foo", "89a9ae301e35e667756034fdc933b1fc94f63fc1")
                .withStatus(GHCheckRun.Status.IN_PROGRESS)
                .withStartedAt(new Date(999_999_000))
                .add(new GHCheckRunBuilder.Output("Some Title", "what happened…")
                        .add(new GHCheckRunBuilder.Annotation("stuff.txt",
                                1,
                                GHCheckRun.AnnotationLevel.NOTICE,
                                "hello to you too").withTitle("Look here")))
                .create();
        GHCheckRun updated = checkRun.update()
                .withStatus(GHCheckRun.Status.COMPLETED)
                .withConclusion(GHCheckRun.Conclusion.SUCCESS)
                .withCompletedAt(new Date(999_999_999))
                .withName("bar", checkRun.getName())
                .create();
        assertThat(new Date(999_999_000), equalTo(updated.getStartedAt()));
        assertThat("bar", equalTo(updated.getName()));
        assertThat(checkRun.getOutput().getAnnotationsCount(), equalTo(1));
    }

    /**
     * Update the check run with name exception.
     *
     * @throws Exception
     *             the exception
     */
    @Test
    public void updateCheckRunWithNameException() throws Exception {
        snapshotNotAllowed();
        GHCheckRun checkRun = getInstallationGithub().getRepository("hub4j-test-org/test-checks")
                .createCheckRun("foo", "89a9ae301e35e667756034fdc933b1fc94f63fc1")
                .withStatus(GHCheckRun.Status.IN_PROGRESS)
                .withStartedAt(new Date(999_999_000))
                .add(new GHCheckRunBuilder.Output("Some Title", "what happened…")
                        .add(new GHCheckRunBuilder.Annotation("stuff.txt",
                                1,
                                GHCheckRun.AnnotationLevel.NOTICE,
                                "hello to you too").withTitle("Look here")))
                .create();
        Assert.assertThrows(GHException.class, () -> checkRun.update()
                .withStatus(GHCheckRun.Status.COMPLETED)
                .withConclusion(GHCheckRun.Conclusion.SUCCESS)
                .withCompletedAt(new Date(999_999_999))
                .withName("bar", null)
                .create());
    }
}
