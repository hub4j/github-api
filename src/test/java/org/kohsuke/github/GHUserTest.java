package org.kohsuke.github;

import org.junit.Test;

import java.io.IOException;
import java.util.*;

import static org.hamcrest.CoreMatchers.*;


public class GHUserTest extends AbstractGitHubWireMockTest {
    @Test
    public void listFollowsAndFollowers() throws IOException {
        GHUser u = gitHub.getUser("rtyler");
        assertNotEquals(
            count30(u.listFollowers()),
            count30(u.listFollows()));
    }

    private Set<GHUser> count30(PagedIterable<GHUser> l) {
        Set<GHUser> users = new HashSet<GHUser>();
        PagedIterator<GHUser> itr = l.iterator();
        for (int i = 0; i < 30 && itr.hasNext(); i++) {
            users.add(itr.next());
        }
        assertEquals(30, users.size());
        return users;
    }

    @Test
    public void getKeys() throws IOException {
        GHUser u = gitHub.getUser("rtyler");
        List<GHKey> ghKeys = new ArrayList<>(u.getKeys());

        assertEquals(3, ghKeys.size());
        Collections.sort(ghKeys, new Comparator<GHKey>() {
            @Override
            public int compare(GHKey ghKey, GHKey t1) {
                return ghKey.getId() - t1.getId();
            }
        });
        assertEquals(1066173, ghKeys.get(0).getId());
        assertEquals("ssh-rsa AAAAB3NzaC1yc2EAAAABIwAAAQEAueiy12T5bvFhsc9YjfLc3aVIxgySd3gDxQWy/bletIoZL8omKmzocBYJ7F58U1asoyfWsy2ToTOY8jJp1eToXmbD6L5+xvHba0A7djYh9aQRrFam7doKQ0zp0ZSUF6+R1v0OM4nnWqK4n2ECIYd+Bdzrp+xA5+XlW3ZSNzlnW2BeWznzmgRMcp6wI+zQ9GMHWviR1cxpml5Z6wrxTZ0aX91btvnNPqoOGva976B6e6403FOEkkIFTk6CC1TFKwc/VjbqxYBg4kU0JhiTP+iEZibcQrYjWdYUgAotYbFVe5/DneHMLNsMPdeihba4PUwt62rXyNegenuCRmCntLcaFQ==",
            ghKeys.get(0).getKey());
        assertEquals(28136459, ghKeys.get(1).getId());
        assertEquals("ssh-rsa AAAAB3NzaC1yc2EAAAADAQABAAABAQDTU0s5OKCC6VpKZGL9NJD4mNLY0AtujkVB1JkkuQ4OkMi2YGUHJtGhTbTwEVhNxpm0x2dM5KSzse6MLDYuGBW0qkE/VVuD9+9I73hbq461KqP0+WlupNh+Qc86kbiLBDv64+vWc+50mp1dbINpoM5xvaPYxgjnemydPv7vu5bhCHBugW7aN8VcLgfFgcp8vZCEanMtd3hIRjRU8v8Skk233ZGu1bXkG8iIOBQPabvEtZ0VDMg9pT3Q1R6lnnKqfCwHXd6zP6uAtejFSxvKRGKpu3OLGQMHwk7NlImVuhkVdaEFBq7pQtpOaGuP2eLKcN1wy5jsTYE+ZB6pvHCi2ecb",
            ghKeys.get(1).getKey());
        assertEquals(31452581, ghKeys.get(2).getId());
        assertEquals("ssh-rsa AAAAB3NzaC1yc2EAAAADAQABAAACAQC3JhH2FZBDmHLjXTcBoV6tdcYKmsQ7sgu8k1RsUhwxGsXm65+Cuas6GcMVoA1DncKfJGQkulHDFiTxIROIBmedh9/otHWBlZ4HqYZ4MQ1A8W5quULkXwX/kF+UdRBUxFvjigibEbuHB+LARVxRRzFlPnTSE9rAfAv8OOEsb3lNUGT/IGhN8w1vwe8GclB90tgqN1RBDgrVqwLFwn5AfrW9kUIa2f2oT4RjYu1OrhKhVIIzfHADo85aD+s8wEhqwI96BCJG3qTWrypoHwBUoj1O6Ak5CGc1iKz9o8XyTMjudRt2ddCjfOtxsuwSlTbVtQXJGIpgKviX1sgh4pPvGh7BVAFP+mdAK4F+mEugDnuj47GO/K5KGGDRCL56kh9+h28l4q/+fZvp7DhtmSN2EzrVAdQFskF8yY/6Xit/aAvjeKm03DcjbylSXbG26EJefaLHlwYFq2mUFRMak25wuuCZS71GF3RC3Sl/bMoxBKRYkyfYtGafeaYTFNGn8Dbd+hfVUCz31ebI8cvmlQR5b5AbCre3T7HTVgw8FKbAxWRf1Fio56PnqHsj+sT1KVj255Zo1F8iD9GrgERSVAlkh5bY/CKszQ8ZSd01c9Qp2a47/gR7XAAbxhzGHP+cSOlrqDlJ24fbPtcpVsM0llqKUcxpmoOBFNboRmE1QqnSmAf9ww==",
            ghKeys.get(2).getKey());
    }

    @Test
    public void listPublicRepositories() throws IOException {
        GHUser user = gitHub.getUser("kohsuke");
        Iterator<GHRepository> itr = user.listRepositories().iterator();
        int i = 0;
        for (; i < 115; i++) {
            assertTrue(itr.hasNext());
            GHRepository r = itr.next();
            System.out.println(r.getFullName());
            assertNotNull(r.getUrl());
            assertNotEquals(0L, r.getId());
        }

        assertThat(i, equalTo(115));
    }

    @Test
    public void listPublicRepositoriesPageSize62() throws IOException {
        GHUser user = gitHub.getUser("kohsuke");
        Iterator<GHRepository> itr = user.listRepositories().withPageSize(62).iterator();
        int i = 0;
        for (; i < 115; i++) {
            assertTrue(itr.hasNext());
            GHRepository r = itr.next();
            System.out.println(r.getFullName());
            assertNotNull(r.getUrl());
            assertNotEquals(0L, r.getId());
        }

        assertThat(i, equalTo(115));
    }

}
