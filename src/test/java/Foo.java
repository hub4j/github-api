import org.kohsuke.github.GitHub;

import java.util.Arrays;

/**
 * @author Kohsuke Kawaguchi
 */
public class Foo {
    public static void main(String[] args) throws Exception {
        System.out.println(GitHub.connect().getOrganization("cloudbees").getRepository("grandcentral").isPrivate());
    }
}
