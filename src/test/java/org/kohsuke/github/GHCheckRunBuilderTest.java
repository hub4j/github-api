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

import java.util.Date;

@SuppressWarnings("deprecation") // preview
public class GHCheckRunBuilderTest extends AbstractGitHubWireMockTest {

    @Test
    public void createCheckRun() throws Exception {
        GHCheckRun checkRun = gitHub.getRepository("jglick/github-api-test")
                .createCheckRun("foo", "4a929d464a2fae7ee899ce603250f7dab304bc4b")
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
        assertEquals(546384586, checkRun.id);
    }

    @Test
    public void createCheckRunManyAnnotations() throws Exception {
        GHCheckRunBuilder.Output output = new GHCheckRunBuilder.Output("Big Run", "Lots of stuff here »");
        for (int i = 0; i < 101; i++) {
            output.add(
                    new GHCheckRunBuilder.Annotation("stuff.txt", 1, GHCheckRun.AnnotationLevel.NOTICE, "hello #" + i));
        }
        GHCheckRun checkRun = gitHub.getRepository("jglick/github-api-test")
                .createCheckRun("big", "4a929d464a2fae7ee899ce603250f7dab304bc4b")
                .withConclusion(GHCheckRun.Conclusion.SUCCESS)
                .add(output)
                .create();
        assertEquals("completed", checkRun.getStatus());
        assertEquals("Big Run", checkRun.getOutput().getTitle());
        assertEquals("Lots of stuff here »", checkRun.getOutput().getSummary());
        assertEquals(101, checkRun.getOutput().getAnnotationsCount());
        assertEquals(546384622, checkRun.id);
    }

    @Test
    public void createCheckRunNoAnnotations() throws Exception {
        GHCheckRun checkRun = gitHub.getRepository("jglick/github-api-test")
                .createCheckRun("quick", "4a929d464a2fae7ee899ce603250f7dab304bc4b")
                .withConclusion(GHCheckRun.Conclusion.NEUTRAL)
                .add(new GHCheckRunBuilder.Output("Quick note", "nothing more to see here"))
                .create();
        assertEquals("completed", checkRun.getStatus());
        assertEquals(0, checkRun.getOutput().getAnnotationsCount());
        assertEquals(546384705, checkRun.id);
    }

}
