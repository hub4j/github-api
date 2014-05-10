import org.kohsuke.github.GHOrganization;
import org.kohsuke.github.GHTeam;
import org.kohsuke.github.GitHub;

import java.util.Arrays;
import java.util.Map;

/**
 * @author Kohsuke Kawaguchi
 */
public class Foo {
    public static void main(String[] args) throws Exception {
        GHOrganization org = GitHub.connect().getOrganization("jenkinsci");
        Map<String, GHTeam> teams = org.getTeams();
        System.out.println(teams.size());

        int sz = 0;
        for (GHTeam t : org.listTeams()) {
            sz++;
        }
        System.out.println(sz);
    }
}
