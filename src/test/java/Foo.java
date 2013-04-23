import org.kohsuke.github.GitHub;

import java.util.Arrays;

/**
 * @author Kohsuke Kawaguchi
 */
public class Foo {
    public static void main(String[] args) throws Exception {
        System.out.println(GitHub.connect().createToken(
                Arrays.asList("user", "repo", "delete_repo", "notifications", "gist"), "GitHub API", null).getToken());
    }
}
