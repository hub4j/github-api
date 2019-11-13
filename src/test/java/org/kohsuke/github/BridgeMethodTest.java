package org.kohsuke.github;

import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.Matchers.equalTo;

/**
 * @author Kohsuke Kawaguchi
 */
public class BridgeMethodTest extends Assert {

    @Test
    public void lastStatus() throws IOException {
        GHObject obj = new GHIssue();

        List<Method> createdAtMethods = new ArrayList<>();
        for (Method method : obj.getClass().getMethods()) {
            if (method.getName().equalsIgnoreCase("getCreatedAt")) {
                if (method.getReturnType() == Date.class) {
                    createdAtMethods.add(0, method);
                } else {
                    createdAtMethods.add(method);
                }
            }
        }

        assertThat(createdAtMethods.size(), equalTo(2));

        assertThat(createdAtMethods.get(0).getParameterCount(), equalTo(0));
        assertThat(createdAtMethods.get(1).getParameterCount(), equalTo(0));

        assertThat(createdAtMethods.get(0).getReturnType(), is(Date.class));
        assertThat(createdAtMethods.get(1).getReturnType(), is(String.class));
    }
}
