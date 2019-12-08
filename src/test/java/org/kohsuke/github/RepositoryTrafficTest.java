package org.kohsuke.github;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.io.IOUtils;
import org.junit.Assert;
import org.junit.Test;
import org.kohsuke.github.GHRepositoryTraffic.DailyInfo;
import org.mockito.Mockito;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.TimeZone;

public class RepositoryTrafficTest extends AbstractGitHubApiTestBase {
    final private String login = "kohsuke", repositoryName = "github-api";

    @Override
    protected GitHubBuilder getGitHubBuilder() {
        return new GitHubBuilder().withPassword(login, null);
    }

    @SuppressWarnings("unchecked")
    private <T extends GHRepositoryTraffic> void checkResponse(T expected, T actual) {
        Assert.assertEquals(expected.getCount(), actual.getCount());
        Assert.assertEquals(expected.getUniques(), actual.getUniques());

        List<? extends DailyInfo> expectedList = expected.getDailyInfo();
        List<? extends DailyInfo> actualList = actual.getDailyInfo();
        Iterator<? extends DailyInfo> expectedIt;
        Iterator<? extends DailyInfo> actualIt;

        Assert.assertEquals(expectedList.size(), actualList.size());
        expectedIt = expectedList.iterator();
        actualIt = actualList.iterator();

        while (expectedIt.hasNext() && actualIt.hasNext()) {
            DailyInfo expectedDailyInfo = expectedIt.next();
            DailyInfo actualDailyInfo = actualIt.next();
            Assert.assertEquals(expectedDailyInfo.getCount(), actualDailyInfo.getCount());
            Assert.assertEquals(expectedDailyInfo.getUniques(), actualDailyInfo.getUniques());
            Assert.assertEquals(expectedDailyInfo.getTimestamp(), actualDailyInfo.getTimestamp());
        }
    }

    private <T extends GHRepositoryTraffic> void testTraffic(T expectedResult) throws IOException {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
        dateFormat.setTimeZone(TimeZone.getTimeZone("GMT"));
        ObjectMapper mapper = new ObjectMapper().setDateFormat(dateFormat);
        String mockedResponse = mapper.writeValueAsString(expectedResult);

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
        URL trafficURL = gitHub.getApiURL("/repos/" + login + "/" + repositoryName + "/traffic/"
                + ((expectedResult instanceof GHRepositoryViewTraffic) ? "views" : "clones"));
        Mockito.doReturn(mockHttpURLConnection).when(connectorSpy).connect(Mockito.eq(trafficURL));

        // make Requester.parse work
        Mockito.doReturn(200).when(mockHttpURLConnection).getResponseCode();
        Mockito.doReturn("OK").when(mockHttpURLConnection).getResponseMessage();
        InputStream stubInputStream = IOUtils.toInputStream(mockedResponse, "UTF-8");
        Mockito.doReturn(stubInputStream).when(mockHttpURLConnection).getInputStream();

        if (expectedResult instanceof GHRepositoryViewTraffic) {
            GHRepositoryViewTraffic views = repo.getViewTraffic();
            checkResponse(expectedResult, views);
        } else if (expectedResult instanceof GHRepositoryCloneTraffic) {
            GHRepositoryCloneTraffic clones = repo.getCloneTraffic();
            checkResponse(expectedResult, clones);
        }
    }

    @Test
    public void testGetViews() throws IOException {
        GHRepositoryViewTraffic expectedResult = new GHRepositoryViewTraffic(21523359, 65534,
                Arrays.asList(new GHRepositoryViewTraffic.DailyInfo("2016-10-10T00:00:00Z", 3, 2),
                        new GHRepositoryViewTraffic.DailyInfo("2016-10-11T00:00:00Z", 9, 4),
                        new GHRepositoryViewTraffic.DailyInfo("2016-10-12T00:00:00Z", 27, 8),
                        new GHRepositoryViewTraffic.DailyInfo("2016-10-13T00:00:00Z", 81, 16),
                        new GHRepositoryViewTraffic.DailyInfo("2016-10-14T00:00:00Z", 243, 32),
                        new GHRepositoryViewTraffic.DailyInfo("2016-10-15T00:00:00Z", 729, 64),
                        new GHRepositoryViewTraffic.DailyInfo("2016-10-16T00:00:00Z", 2187, 128),
                        new GHRepositoryViewTraffic.DailyInfo("2016-10-17T00:00:00Z", 6561, 256),
                        new GHRepositoryViewTraffic.DailyInfo("2016-10-18T00:00:00Z", 19683, 512),
                        new GHRepositoryViewTraffic.DailyInfo("2016-10-19T00:00:00Z", 59049, 1024),
                        new GHRepositoryViewTraffic.DailyInfo("2016-10-20T00:00:00Z", 177147, 2048),
                        new GHRepositoryViewTraffic.DailyInfo("2016-10-21T00:00:00Z", 531441, 4096),
                        new GHRepositoryViewTraffic.DailyInfo("2016-10-22T00:00:00Z", 1594323, 8192),
                        new GHRepositoryViewTraffic.DailyInfo("2016-10-23T00:00:00Z", 4782969, 16384),
                        new GHRepositoryViewTraffic.DailyInfo("2016-10-24T00:00:00Z", 14348907, 32768)));
        testTraffic(expectedResult);
    }

    @Test
    public void testGetClones() throws IOException {
        GHRepositoryCloneTraffic expectedResult = new GHRepositoryCloneTraffic(1500, 455,
                Arrays.asList(new GHRepositoryCloneTraffic.DailyInfo("2016-10-10T00:00:00Z", 10, 3),
                        new GHRepositoryCloneTraffic.DailyInfo("2016-10-11T00:00:00Z", 20, 6),
                        new GHRepositoryCloneTraffic.DailyInfo("2016-10-12T00:00:00Z", 30, 5),
                        new GHRepositoryCloneTraffic.DailyInfo("2016-10-13T00:00:00Z", 40, 7),
                        new GHRepositoryCloneTraffic.DailyInfo("2016-10-14T00:00:00Z", 50, 11),
                        new GHRepositoryCloneTraffic.DailyInfo("2016-10-15T00:00:00Z", 60, 12),
                        new GHRepositoryCloneTraffic.DailyInfo("2016-10-16T00:00:00Z", 70, 19),
                        new GHRepositoryCloneTraffic.DailyInfo("2016-10-17T00:00:00Z", 170, 111),
                        new GHRepositoryCloneTraffic.DailyInfo("2016-10-18T00:00:00Z", 180, 70),
                        new GHRepositoryCloneTraffic.DailyInfo("2016-10-19T00:00:00Z", 190, 10),
                        new GHRepositoryCloneTraffic.DailyInfo("2016-10-20T00:00:00Z", 200, 18),
                        new GHRepositoryCloneTraffic.DailyInfo("2016-10-21T00:00:00Z", 210, 8),
                        new GHRepositoryCloneTraffic.DailyInfo("2016-10-22T00:00:00Z", 220, 168),
                        new GHRepositoryCloneTraffic.DailyInfo("2016-10-23T00:00:00Z", 5, 2),
                        new GHRepositoryCloneTraffic.DailyInfo("2016-10-24T00:00:00Z", 45, 5)));
        testTraffic(expectedResult);
    }

    @Test
    public void testGetTrafficStatsAccessFailureDueToInsufficientPermissions() throws IOException {
        String errorMsg = "Exception should be thrown, since we don't have permission to access repo traffic info.";

        GHRepository repo = gitHub.getUser(login).getRepository(repositoryName);
        try {
            repo.getViewTraffic();
            Assert.fail(errorMsg);
        } catch (HttpException ex) {
        }
        try {
            repo.getCloneTraffic();
            Assert.fail(errorMsg);
        } catch (HttpException ex) {
        }
    }
}
