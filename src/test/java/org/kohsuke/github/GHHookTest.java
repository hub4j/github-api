package org.kohsuke.github;

import org.apache.commons.lang.StringUtils;
import org.junit.Ignore;
import org.junit.Test;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static java.util.Collections.singletonList;
import static java.util.Collections.singletonMap;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasKey;
import static org.hamcrest.core.IsInstanceOf.instanceOf;
import static org.junit.Assert.assertThat;

/**
 * @author Kanstantsin Shautsou
 */
public class GHHookTest {

    @Ignore
    @Test
    public void exposeResponceHeaders() throws Exception {
        String user1Login = "KostyaSha-auto";
        String user1Pass = "secret";

        String clientId = "90140219451";
        String clientSecret = "1451245425";

        String orgRepo = "KostyaSha-org/test";

        // some login based user that has access to application
        final GitHub gitHub = GitHub.connectUsingPassword(user1Login, user1Pass);
        gitHub.getMyself();

        // we request read
        final List<String> scopes = Arrays.asList("repo", "read:org", "user:email", "read:repo_hook");

        // application creates token with scopes
        final GHAuthorization auth = gitHub.createOrGetAuth(clientId, clientSecret, scopes, "", "");
        String token = auth.getToken();
        if (StringUtils.isEmpty(token)) {
            gitHub.deleteAuth(auth.getId());
            token = gitHub.createOrGetAuth(clientId, clientSecret, scopes, "", "").getToken();
        }

        /// now create connection using token
        final GitHub gitHub2 = GitHub.connectUsingOAuth(token);
        // some repo in organisation
        final GHRepository repository = gitHub2.getRepository(orgRepo);

        // doesn't fail because we have read access
        final List<GHHook> hooks = repository.getHooks();

        try {
            // fails because application isn't approved in organisation and you can find it only after doing real call
            final GHHook hook = repository
                    .createHook("my-hook", singletonMap("url", "http://localhost"), singletonList(GHEvent.PUSH), true);
        } catch (IOException ex) {
            assertThat(ex, instanceOf(GHFileNotFoundException.class));
            final GHFileNotFoundException ghFileNotFoundException = (GHFileNotFoundException) ex;
            final Map<String, List<String>> responseHeaderFields = ghFileNotFoundException.getResponseHeaderFields();
            assertThat(responseHeaderFields, hasKey("X-Accepted-OAuth-Scopes"));
            assertThat(responseHeaderFields.get("X-Accepted-OAuth-Scopes"),
                    hasItem("admin:repo_hook, public_repo, repo, write:repo_hook"));
        }
    }
}
