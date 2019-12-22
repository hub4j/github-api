package org.kohsuke.github;

import org.junit.Test;

import java.util.List;

import static org.hamcrest.Matchers.*;

public class GHLabelTest extends org.kohsuke.github.AbstractGitHubWireMockTest {

    @Test
    public void test_toString() throws Exception {
        GHRepository rep = getTempRepository();

        GHLabel label = rep.createLabel("foo", "001122", "test foo label");
        assertThat(label.toString(), containsString("name=foo,color=001122,description=test foo label"));

        List<GHLabel> list = rep.listLabels().asList();

        assertEquals(10, list.size());
        assertThat(list.stream().filter(l -> "foo".equals(l.getName())).findAny().toString(),
                containsString("name=foo,color=001122,description=test foo label"));
    }

    @Test
    public void test_create_updateLabel() throws Exception {
        GHRepository rep = getTempRepository();

        GHLabel label = rep.createLabel("foo", "001122", "test foo label");
        assertThat(label.toString(), containsString("name=foo,color=001122,description=test foo label"));
        label.setColor("221100");
        assertThat(label.toString(), containsString("name=foo,color=221100,description=test foo label"));
        label.setDescription("label foo test");
        assertThat(label.toString(), containsString("name=foo,color=221100,description=label foo test"));
        label.setName("newfoo");
        assertThat(label.toString(), containsString("name=newfoo,color=221100,description=label foo test"));
        label.delete();
    }
}
