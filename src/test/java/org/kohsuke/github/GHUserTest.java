package org.kohsuke.github;

import org.junit.Test;

import java.io.IOException;
import java.util.*;

import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.Matchers.emptyString;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.notNullValue;

public class GHUserTest extends AbstractGitHubWireMockTest {

    @Test
    public void isMemberOf() throws IOException {
        GHUser u = gitHub.getUser("bitwiseman");
        String teamSlug = "dummy-team";
        GHOrganization org = gitHub.getOrganization(GITHUB_API_TEST_ORG);
        GHTeam team = org.getTeamBySlug(teamSlug);

        assertThat(u.isMemberOf(org), is(true));
        assertThat(u.isMemberOf(team), is(true));
        assertThat(u.isPublicMemberOf(org), is(false));

        org = gitHub.getOrganization("hub4j");
        assertThat(u.isMemberOf(org), is(true));
        assertThat(u.isPublicMemberOf(org), is(true));

        u = gitHub.getUser("rtyler");
        assertThat(u.isMemberOf(org), is(false));
        assertThat(u.isMemberOf(team), is(false));
        assertThat(u.isPublicMemberOf(org), is(false));
    }

    @Test
    public void listFollowsAndFollowers() throws IOException {
        GHUser u = gitHub.getUser("rtyler");
        assertThat(count30(u.listFollows()), not(count30(u.listFollowers())));
    }

    private Set<GHUser> count30(PagedIterable<GHUser> l) {
        Set<GHUser> users = new HashSet<GHUser>();
        PagedIterator<GHUser> itr = l.iterator();
        for (int i = 0; i < 30 && itr.hasNext(); i++) {
            users.add(itr.next());
        }
        assertThat(users.size(), equalTo(30));
        return users;
    }

    @Test
    public void getKeys() throws IOException {
        GHUser u = gitHub.getUser("rtyler");
        List<GHKey> ghKeys = new ArrayList<>(u.getKeys());

        assertThat(ghKeys.size(), equalTo(3));
        Collections.sort(ghKeys, new Comparator<GHKey>() {
            @Override
            public int compare(GHKey ghKey, GHKey t1) {
                return ghKey.getId() - t1.getId();
            }
        });
        assertThat(ghKeys.get(0).getId(), equalTo(1066173));
        assertThat(ghKeys.get(0).getTitle(), nullValue());
        assertThat(ghKeys.get(0).getUrl(), nullValue());
        assertThat(ghKeys.get(0).isVerified(), equalTo(false));
        assertThat(ghKeys.get(0).toString(),
                containsString(
                        "title=<null>,id=1066173,key=ssh-rsa AAAAB3NzaC1yc2EAAAABIwAAAQEAueiy12T5bvFhsc9YjfLc3aVIxgySd3gDxQWy/bletIoZL8omKmzocBYJ7F58U1asoyfWsy2ToTOY8jJp1eToXmbD6L5+xvHba0A7djYh9aQRrFam7doKQ0zp0ZSUF6+R1v0OM4nnWqK4n2ECIYd+Bdzrp+xA5+XlW3ZSNzlnW2BeWznzmgRMcp6wI+zQ9GMHWviR1cxpml5Z6wrxTZ0aX91btvnNPqoOGva976B6e6403FOEkkIFTk6CC1TFKwc/VjbqxYBg4kU0JhiTP+iEZibcQrYjWdYUgAotYbFVe5/DneHMLNsMPdeihba4PUwt62rXyNegenuCRmCntLcaFQ=="));

        assertThat(ghKeys.get(0).getKey(),
                equalTo("ssh-rsa AAAAB3NzaC1yc2EAAAABIwAAAQEAueiy12T5bvFhsc9YjfLc3aVIxgySd3gDxQWy/bletIoZL8omKmzocBYJ7F58U1asoyfWsy2ToTOY8jJp1eToXmbD6L5+xvHba0A7djYh9aQRrFam7doKQ0zp0ZSUF6+R1v0OM4nnWqK4n2ECIYd+Bdzrp+xA5+XlW3ZSNzlnW2BeWznzmgRMcp6wI+zQ9GMHWviR1cxpml5Z6wrxTZ0aX91btvnNPqoOGva976B6e6403FOEkkIFTk6CC1TFKwc/VjbqxYBg4kU0JhiTP+iEZibcQrYjWdYUgAotYbFVe5/DneHMLNsMPdeihba4PUwt62rXyNegenuCRmCntLcaFQ=="));
        assertThat(ghKeys.get(1).getId(), equalTo(28136459));
        assertThat(ghKeys.get(1).getTitle(), nullValue());
        assertThat(ghKeys.get(1).getUrl(), nullValue());
        assertThat(ghKeys.get(1).isVerified(), equalTo(false));
        assertThat(ghKeys.get(1).getKey(),
                equalTo("ssh-rsa AAAAB3NzaC1yc2EAAAADAQABAAABAQDTU0s5OKCC6VpKZGL9NJD4mNLY0AtujkVB1JkkuQ4OkMi2YGUHJtGhTbTwEVhNxpm0x2dM5KSzse6MLDYuGBW0qkE/VVuD9+9I73hbq461KqP0+WlupNh+Qc86kbiLBDv64+vWc+50mp1dbINpoM5xvaPYxgjnemydPv7vu5bhCHBugW7aN8VcLgfFgcp8vZCEanMtd3hIRjRU8v8Skk233ZGu1bXkG8iIOBQPabvEtZ0VDMg9pT3Q1R6lnnKqfCwHXd6zP6uAtejFSxvKRGKpu3OLGQMHwk7NlImVuhkVdaEFBq7pQtpOaGuP2eLKcN1wy5jsTYE+ZB6pvHCi2ecb"));
        assertThat(ghKeys.get(2).getId(), equalTo(31452581));
        assertThat(ghKeys.get(2).getTitle(), nullValue());
        assertThat(ghKeys.get(2).getUrl(), nullValue());
        assertThat(ghKeys.get(2).isVerified(), equalTo(false));
        assertThat(ghKeys.get(2).getKey(),
                equalTo("ssh-rsa AAAAB3NzaC1yc2EAAAADAQABAAACAQC3JhH2FZBDmHLjXTcBoV6tdcYKmsQ7sgu8k1RsUhwxGsXm65+Cuas6GcMVoA1DncKfJGQkulHDFiTxIROIBmedh9/otHWBlZ4HqYZ4MQ1A8W5quULkXwX/kF+UdRBUxFvjigibEbuHB+LARVxRRzFlPnTSE9rAfAv8OOEsb3lNUGT/IGhN8w1vwe8GclB90tgqN1RBDgrVqwLFwn5AfrW9kUIa2f2oT4RjYu1OrhKhVIIzfHADo85aD+s8wEhqwI96BCJG3qTWrypoHwBUoj1O6Ak5CGc1iKz9o8XyTMjudRt2ddCjfOtxsuwSlTbVtQXJGIpgKviX1sgh4pPvGh7BVAFP+mdAK4F+mEugDnuj47GO/K5KGGDRCL56kh9+h28l4q/+fZvp7DhtmSN2EzrVAdQFskF8yY/6Xit/aAvjeKm03DcjbylSXbG26EJefaLHlwYFq2mUFRMak25wuuCZS71GF3RC3Sl/bMoxBKRYkyfYtGafeaYTFNGn8Dbd+hfVUCz31ebI8cvmlQR5b5AbCre3T7HTVgw8FKbAxWRf1Fio56PnqHsj+sT1KVj255Zo1F8iD9GrgERSVAlkh5bY/CKszQ8ZSd01c9Qp2a47/gR7XAAbxhzGHP+cSOlrqDlJ24fbPtcpVsM0llqKUcxpmoOBFNboRmE1QqnSmAf9ww=="));
    }

    @Test
    public void listPublicRepositories() throws IOException {
        GHUser user = gitHub.getUser("kohsuke");
        Iterator<GHRepository> itr = user.listRepositories().iterator();
        int i = 0;
        for (; i < 115; i++) {
            assertThat(itr.hasNext(), is(true));
            GHRepository r = itr.next();
            // System.out.println(r.getFullName());
            assertThat(r.getUrl(), notNullValue());
            assertThat(r.getId(), not(0L));
        }

        assertThat(i, equalTo(115));
    }

    @Test
    public void listProjects() throws IOException {
        GHUser user = gitHub.getUser("t0m4uk1991");
        List<GHProject> projects = user.listProjects().toList();
        assertThat(projects, notNullValue());
        assertThat(projects.size(), is(3));
        assertThat(projects.get(0).getName(), is("Project 1"));
        assertThat(projects.get(1).getName(), is("Project 2"));
        assertThat(projects.get(2).getName(), is("Project 3"));
    }

    @Test
    public void listPublicRepositoriesPageSize62() throws IOException {
        GHUser user = gitHub.getUser("kohsuke");
        Iterator<GHRepository> itr = user.listRepositories().withPageSize(62).iterator();
        int i = 0;
        for (; i < 115; i++) {
            assertThat(itr.hasNext(), is(true));
            GHRepository r = itr.next();
            // System.out.println(r.getFullName());
            assertThat(r.getUrl(), notNullValue());
            assertThat(r.getId(), not(0L));
        }

        assertThat(i, equalTo(115));
    }

    @Test
    public void createAndCountPrivateRepos() throws IOException {
        String login = gitHub.getMyself().getLogin();

        GHRepository repository = gitHub.createRepository("github-user-test-private-repo")
                .description("a test private repository used to test kohsuke's github-api")
                .homepage("http://github-api.kohsuke.org/")
                .private_(true)
                .create();

        try {
            assertThat(repository, notNullValue());
            GHUser ghUser = gitHub.getUser(login);
            assertThat(ghUser.getTotalPrivateRepoCount().orElse(-1), greaterThan(0));
        } finally {
            repository.delete();
        }
    }

    @Test
    public void verifyBioAndHireable() throws IOException {
        GHUser u = gitHub.getUser("Chew");
        assertThat(u.getBio(), equalTo("I like to program things and I hope to program something cool one day :D"));
        assertThat(u.isHireable(), is(true));
        assertThat(u.getTwitterUsername(), notNullValue());
        assertThat(u.getBlog(), equalTo("https://chew.pw"));
        assertThat(u.getCompany(), equalTo("@Memerator"));
        assertThat(u.getFollowersCount(), equalTo(29));
        assertThat(u.getFollowingCount(), equalTo(3));
        assertThat(u.getPublicGistCount(), equalTo(4));
        assertThat(u.getPublicRepoCount(), equalTo(96));
    }

    @Test
    public void verifyLdapDn() throws IOException {
        GHUser u = gitHub.getUser("kartikpatodi");
        assertThat(u.getLdapDn().orElse(""), not(emptyString()));
    }
}
