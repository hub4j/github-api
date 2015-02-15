package org.kohsuke.github;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import java.util.Iterator;

import static org.mockito.Mockito.when;

/**
 * @author Luciano P. Sabenca (luciano.sabenca [at] movile [com] | lucianosabenca [at] gmail [dot] com
 */
public class RepositoryTest {

    @Mock
    GitHub mockGitHub;

    @Mock
    Iterator<GHUser[]> iterator;

    @Mock
    GHRepository mockRepository;


    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void listCollaborators() throws Exception {
        GHUser user1 = new GHUser();
        user1.login = "login1";

        GHUser user2 = new GHUser();
        user2.login = "login2";


        when(iterator.hasNext()).thenReturn(true, false, true);
        when(iterator.next()).thenReturn(new GHUser[]{user1}, new GHUser[]{user2});

        Requester requester = Mockito.mock(Requester.class);
        when(mockGitHub.retrieve()).thenReturn(requester);


        when(requester.asIterator("/repos/*/*/collaborators",
                GHUser[].class)).thenReturn(iterator, iterator);


        PagedIterable<GHUser> pagedIterable = Mockito.mock(PagedIterable.class);
        when(mockRepository.listCollaborators()).thenReturn(pagedIterable);

        PagedIterator<GHUser> userPagedIterator = new PagedIterator<GHUser>(iterator) {
            @Override
            protected void wrapUp(GHUser[] page) {

            }
        };
        PagedIterator<GHUser> userPagedIterator2 = new PagedIterator<GHUser>(iterator) {
            @Override
            protected void wrapUp(GHUser[] page) {

            }
        };


        when(pagedIterable.iterator()).thenReturn(userPagedIterator, userPagedIterator2);

        Iterator<GHUser> returnIterator1 = mockRepository.listCollaborators().iterator();


        Assert.assertTrue(returnIterator1.hasNext());
        GHUser user = returnIterator1.next();
        Assert.assertEquals(user, user1);
        Assert.assertFalse(returnIterator1.hasNext());


        Iterator returnIterator2 = mockRepository.listCollaborators().iterator();


        Assert.assertTrue(returnIterator2.hasNext());
        user = returnIterator1.next();
        Assert.assertEquals(user, user2);
    }
}
