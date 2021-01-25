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

import org.junit.Test;

import java.io.IOException;
import java.util.Date;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;

@SuppressWarnings("deprecation") // preview
public class GHCheckRunBuilderTest extends AbstractGHAppInstallationTest {

    protected GitHub getInstallationGithub() throws IOException {
        return getAppInstallationWithTokenApp3().getRoot();
    }

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
                .add(new GHCheckRunBuilder.Output("Some Title", "what happened…")
                        .add(new GHCheckRunBuilder.Annotation("stuff.txt",
                                1,
                                GHCheckRun.AnnotationLevel.NOTICE,
                                "hello to you too").withTitle("Look here"))
                        .add(new GHCheckRunBuilder.Image("Unikitty",
                                "https://i.pinimg.com/474x/9e/65/c0/9e65c0972294f1e10f648c9780a79fab.jpg")
                                        .withCaption("Princess Unikitty")))
                .add(new GHCheckRunBuilder.Action("Help", "what I need help with", "doit"))
                .create();
        assertEquals("completed", checkRun.getStatus());
        assertEquals(1, checkRun.getOutput().getAnnotationsCount());
        assertEquals(1424883286, checkRun.getId());
    }

    @Test
    public void createCheckRunManyAnnotations() throws Exception {
        GHCheckRunBuilder.Output output = new GHCheckRunBuilder.Output("Big Run", "Lots of stuff here »");
        for (int i = 0; i < 101; i++) {
            output.add(
                    new GHCheckRunBuilder.Annotation("stuff.txt", 1, GHCheckRun.AnnotationLevel.NOTICE, "hello #" + i));
        }
        GHCheckRun checkRun = getInstallationGithub().getRepository("hub4j-test-org/test-checks")
                .createCheckRun("big", "89a9ae301e35e667756034fdc933b1fc94f63fc1")
                .withConclusion(GHCheckRun.Conclusion.SUCCESS)
                .add(output)
                .create();
        assertEquals("completed", checkRun.getStatus());
        assertEquals("Big Run", checkRun.getOutput().getTitle());
        assertEquals("Lots of stuff here »", checkRun.getOutput().getSummary());
        assertEquals(101, checkRun.getOutput().getAnnotationsCount());
        assertEquals(1424883599, checkRun.getId());
    }

    @Test
    public void createCheckRunNoAnnotations() throws Exception {
        GHCheckRun checkRun = getInstallationGithub().getRepository("hub4j-test-org/test-checks")
                .createCheckRun("quick", "89a9ae301e35e667756034fdc933b1fc94f63fc1")
                .withConclusion(GHCheckRun.Conclusion.NEUTRAL)
                .add(new GHCheckRunBuilder.Output("Quick note", "nothing more to see here"))
                .create();
        assertEquals("completed", checkRun.getStatus());
        assertEquals(0, checkRun.getOutput().getAnnotationsCount());
        assertEquals(1424883957, checkRun.getId());
    }

    @Test
    public void createPendingCheckRun() throws Exception {
        GHCheckRun checkRun = getInstallationGithub().getRepository("hub4j-test-org/test-checks")
                .createCheckRun("outstanding", "89a9ae301e35e667756034fdc933b1fc94f63fc1")
                .withStatus(GHCheckRun.Status.IN_PROGRESS)
                .create();
        assertEquals("in_progress", checkRun.getStatus());
        assertNull(checkRun.getConclusion());
        assertEquals(1424883451, checkRun.getId());
    }

    @Test
    public void createCheckRunErrMissingConclusion() throws Exception {
        try {
            getInstallationGithub().getRepository("hub4j-test-org/test-checks")
                    .createCheckRun("outstanding", "89a9ae301e35e667756034fdc933b1fc94f63fc1")
                    .withStatus(GHCheckRun.Status.COMPLETED)
                    .create();
            fail("should have been rejected");
        } catch (HttpException x) {
            assertEquals(422, x.getResponseCode());
            assertThat(x.getMessage(), containsString("\\\"conclusion\\\" wasn't supplied"));
            assertThat(x.getUrl(), containsString("/repos/hub4j-test-org/test-checks/check-runs"));
            assertThat(x.getResponseMessage(), equalTo("422 Unprocessable Entity"));
        }
    }

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
        assertEquals(updated.getStartedAt(), new Date(999_999_000));
        assertEquals(updated.getName(), "foo");
        assertEquals(1, checkRun.getOutput().getAnnotationsCount());
    }

}
