package org.kohsuke.github;

import org.junit.Test;

import java.util.List;

import static org.hamcrest.Matchers.*;

public class GHLabelTest extends org.kohsuke.github.AbstractGitHubWireMockTest {

    @Test
    public void test_toString() throws Exception {
        GHOrganization org = gitHub.getOrganization(GITHUB_API_TEST_ORG);
        GHRepository rep = org.getRepository("test-labels");

        GHLabel label = rep.createLabel("foo", "001122", "test foo label");
        assertThat(label.toString(), containsString("name=foo,color=001122,description=test foo label"));

        List<GHLabel> list = rep.listLabels().asList();

        assertEquals(1, list.size());
        assertThat(list.get(0).toString(), containsString("name=foo,color=001122,description=test foo label"));
    }

    @Test
    public void test_create_updateLabel() throws Exception {
        GHOrganization org = gitHub.getOrganization(GITHUB_API_TEST_ORG);
        GHRepository rep = org.getRepository("test-labels");
        GHLabel label = rep.createLabel("foo", "001122", "test foo label");
        assertThat(label.toString(), containsString("name=foo,color=001122,description=test foo label"));
        label.update("newfoo", "221100", "label foo test");
        assertThat(label.toString(), containsString("name=newfoo,color=221100,description=label foo test"));
        label.delete();
    }
}
