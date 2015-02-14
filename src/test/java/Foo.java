import org.kohsuke.github.GHIssue;
import org.kohsuke.github.GHOrganization;
import org.kohsuke.github.GHTeam;
import org.kohsuke.github.GitHub;
import org.kohsuke.github.PagedIterable;
import org.kohsuke.github.PagedSearchIterable;

import java.util.Arrays;
import java.util.Map;

/**
 * @author Kohsuke Kawaguchi
 */
public class Foo {
    public static void main(String[] args) throws Exception {
        PagedSearchIterable<GHIssue> reviewbybees = GitHub.connect().searchIssues().mentions("reviewbybees").isOpen().list();
        for (GHIssue r : reviewbybees) {
            System.out.println(r.getTitle());
        }
        System.out.println("total="+reviewbybees.getTotalCount());
    }
}
