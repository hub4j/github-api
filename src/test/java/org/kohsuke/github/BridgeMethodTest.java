package org.kohsuke.github;

import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Set;

import javax.annotation.Nonnull;

import static org.hamcrest.Matchers.*;

// TODO: Auto-generated Javadoc
/**
 * The Class BridgeMethodTest.
 *
 * @author Kohsuke Kawaguchi
 */
public class BridgeMethodTest extends Assert {

    /**
     * Test bridge methods.
     *
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     */
    @Test
    public void testBridgeMethods() throws IOException {

        // Some would say this is redundant, given that bridge methods are so thin anyway
        // In the interest of maintaining binary compatibility, we'll do this anyway for a sampling of methods

        // Something odd here
        // verifyBridgeMethods(new GHCommit(), "getAuthor", GHCommit.GHAuthor.class, GitUser.class);
        // verifyBridgeMethods(new GHCommit(), "getCommitter", GHCommit.GHAuthor.class, GitUser.class);

        verifyBridgeMethods(GHIssue.class, "getCreatedAt", Date.class, String.class);
        verifyBridgeMethods(GHIssue.class, "getId", int.class, long.class, String.class);
        verifyBridgeMethods(GHIssue.class, "getUrl", String.class, URL.class);
        verifyBridgeMethods(GHIssue.class, "comment", 1, void.class, GHIssueComment.class);

        verifyBridgeMethods(GHOrganization.class, "getHtmlUrl", String.class, URL.class);
        verifyBridgeMethods(GHOrganization.class, "getId", int.class, long.class, String.class);
        verifyBridgeMethods(GHOrganization.class, "getUrl", String.class, URL.class);

        verifyBridgeMethods(GHRepository.class, "getCollaborators", GHPersonSet.class, Set.class);
        verifyBridgeMethods(GHRepository.class, "getHtmlUrl", String.class, URL.class);
        verifyBridgeMethods(GHRepository.class, "getId", int.class, long.class, String.class);
        verifyBridgeMethods(GHRepository.class, "getUrl", String.class, URL.class);

        verifyBridgeMethods(GHUser.class, "getFollows", GHPersonSet.class, Set.class);
        verifyBridgeMethods(GHUser.class, "getFollowers", GHPersonSet.class, Set.class);
        verifyBridgeMethods(GHUser.class, "getOrganizations", GHPersonSet.class, Set.class);
        verifyBridgeMethods(GHUser.class, "getId", int.class, long.class, String.class);

        verifyBridgeMethods(GHTeam.class, "getId", int.class, long.class, String.class);

        // verifyBridgeMethods(GitHub.class, "getMyself", GHMyself.class, GHUser.class);

    }

    /**
     * Verify bridge methods.
     *
     * @param targetClass
     *            the target class
     * @param methodName
     *            the method name
     * @param returnTypes
     *            the return types
     */
    void verifyBridgeMethods(@Nonnull Class<?> targetClass, @Nonnull String methodName, Class<?>... returnTypes) {
        verifyBridgeMethods(targetClass, methodName, 0, returnTypes);
    }

    /**
     * Verify bridge methods.
     *
     * @param targetClass
     *            the target class
     * @param methodName
     *            the method name
     * @param parameterCount
     *            the parameter count
     * @param returnTypes
     *            the return types
     */
    void verifyBridgeMethods(@Nonnull Class<?> targetClass,
            @Nonnull String methodName,
            int parameterCount,
            Class<?>... returnTypes) {
        List<Class<?>> foundMethods = new ArrayList<>();
        Method[] methods = targetClass.getMethods();
        for (Method method : methods) {
            if (method.getName().equalsIgnoreCase(methodName) && method.getParameterCount() == parameterCount) {
                foundMethods.add(method.getReturnType());
            }
        }

        assertThat(foundMethods, containsInAnyOrder(returnTypes));
    }
}
