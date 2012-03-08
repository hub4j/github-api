package org.kohsuke.github;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * Represents the account that's logging into GitHub.
 *
 * @author Kohsuke Kawaguchi
 */
public class GHMyself extends GHUser {
    /**
     * Returns the read-only list of e-mail addresses configured for you.
     *
     * This corresponds to the stuff you configure in https://github.com/settings/emails,
     * and not to be confused with {@link #getEmail()} that shows your public e-mail address
     * set in https://github.com/settings/profile
     * 
     * @return
     *      Always non-null.
     */
    public List<String> getEmails() throws IOException {
        String[] addresses = root.retrieveWithAuth3("/user/emails",String[].class);
        return Collections.unmodifiableList(Arrays.asList(addresses));
    }
    
//    public void addEmails(Collection<String> emails) throws IOException {
////        new Poster(root,ApiVersion.V3).withCredential().to("/user/emails");
//        root.retrieveWithAuth3()
//    }
}
