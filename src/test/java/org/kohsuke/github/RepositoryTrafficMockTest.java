package org.kohsuke.github;

import org.apache.commons.io.IOUtils;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Arrays;
import java.util.Iterator;

public class RepositoryTrafficMockTest {
    final private String login = "kohsuke", repositoryName = "github-api";
    private void checkResponse(GHRepositoryViews expected, GHRepositoryViews actual){
        Assert.assertEquals(expected.getCount(), actual.getCount());
        Assert.assertEquals(expected.getUniques(), actual.getUniques());
        Assert.assertEquals(expected.getViews().size(), actual.getViews().size());
        Iterator<GHRepositoryViews.DayViews> expectedIt = expected.getViews().iterator();
        Iterator<GHRepositoryViews.DayViews> actualIt = actual.getViews().iterator();
        while(expectedIt.hasNext() && actualIt.hasNext()) {
            GHRepositoryViews.DayViews expectedDayViews = expectedIt.next();
            GHRepositoryViews.DayViews actualDayViews = actualIt.next();
            Assert.assertEquals(expectedDayViews.getCount(), actualDayViews.getCount());
            Assert.assertEquals(expectedDayViews.getUniques(), actualDayViews.getUniques());
            Assert.assertEquals(expectedDayViews.getTimestamp(), actualDayViews.getTimestamp());
        }
    }

    @Test
    public void getViews() throws IOException{
        // example taken from the docs https://developer.github.com/v3/repos/traffic/#views
        GHRepositoryViews expectedResult = new GHRepositoryViews(
                14850,
                3782,
                Arrays.asList(
                        new GHRepositoryViews.DayViews("2016-10-10T00:00:00Z", 440,143),
                        new GHRepositoryViews.DayViews("2016-10-11T00:00:00Z", 1308,414),
                        new GHRepositoryViews.DayViews("2016-10-12T00:00:00Z", 1486,452),
                        new GHRepositoryViews.DayViews("2016-10-13T00:00:00Z", 1170,401),
                        new GHRepositoryViews.DayViews("2016-10-14T00:00:00Z", 868,266),
                        new GHRepositoryViews.DayViews("2016-10-15T00:00:00Z", 495,157),
                        new GHRepositoryViews.DayViews("2016-10-16T00:00:00Z", 524,175),
                        new GHRepositoryViews.DayViews("2016-10-17T00:00:00Z", 1263,412),
                        new GHRepositoryViews.DayViews("2016-10-18T00:00:00Z", 1402,417),
                        new GHRepositoryViews.DayViews("2016-10-19T00:00:00Z", 1394,424),
                        new GHRepositoryViews.DayViews("2016-10-20T00:00:00Z", 1492,448),
                        new GHRepositoryViews.DayViews("2016-10-21T00:00:00Z", 1153,332),
                        new GHRepositoryViews.DayViews("2016-10-22T00:00:00Z", 566,168),
                        new GHRepositoryViews.DayViews("2016-10-23T00:00:00Z", 675,184),
                        new GHRepositoryViews.DayViews("2016-10-24T00:00:00Z", 614,237)
                )
        );
        String mockedGHRepositoryViewsResponse = GitHub.MAPPER.writeValueAsString(expectedResult);

        GitHub gitHub = GitHub.connect(login, null);
        GitHub gitHubSpy = Mockito.spy(gitHub);
        GHRepository repo = gitHubSpy.getUser(login).getRepository(repositoryName);


        // accessing traffic info requires push access to the repo
        // since we don't have that, let the mocking begin...

        HttpConnector connectorSpy = Mockito.spy(gitHubSpy.getConnector());
        Mockito.doReturn(connectorSpy).when(gitHubSpy).getConnector();


        // also known as the "uc" in the Requester class
        HttpURLConnection mockHttpURLConnection = Mockito.mock(HttpURLConnection.class);


        // needed for Requester.setRequestMethod
        Mockito.doReturn("GET").when(mockHttpURLConnection).getRequestMethod();


        // this covers calls on "uc" in Requester.setupConnection and Requester.buildRequest
        URL trafficURL = new URL("https://api.github.com/repos/"+login+"/"+repositoryName+"/traffic/views");
        Mockito.doReturn(mockHttpURLConnection).when(connectorSpy).connect(Mockito.eq(trafficURL));


        // make Requester.parse work
        Mockito.doReturn(200).when(mockHttpURLConnection).getResponseCode();
        Mockito.doReturn("OK").when(mockHttpURLConnection).getResponseMessage();
        InputStream stubInputStream = IOUtils.toInputStream(mockedGHRepositoryViewsResponse, "UTF-8");
        Mockito.doReturn(stubInputStream).when(mockHttpURLConnection).getInputStream();


        GHRepositoryViews views =  repo.getViews();


        checkResponse(expectedResult, views);
    }

    @Test
    public void getViewsAccessFailureDueToInsufficientPermissions() throws IOException {
        GitHub gitHub = GitHub.connect(login, null);
        GHRepository repo = gitHub.getUser(login).getRepository(repositoryName);
        try {
            repo.getViews();
            Assert.fail("Exception should be thrown, since we don't have permission to access repo's traffic info.");
        }
        catch (HttpException ex){
        }
    }
}
