package org.kohsuke.github;

import com.squareup.okhttp.OkUrlFactory;
import com.squareup.okhttp.Cache;
import com.squareup.okhttp.OkHttpClient;
import org.apache.commons.io.FileUtils;
import org.kohsuke.github.extras.OkHttpConnector;

import java.io.File;
import java.io.IOException;

/**
 * @author Liam Newman
 */
public abstract class OkHttpConnectorTest extends AbstractGitHubApiWireMockTest {

    protected GitHubBuilder getGitHubBuilder() {
        OkHttpClient client = new OkHttpClient();

        File cacheDir = new File("target/cache/" + baseFilesClassPath + "/" +  githubApi.getMethodName());
        cacheDir.mkdirs();
        try {
            FileUtils.cleanDirectory(cacheDir);
        } catch (IOException e) {}
        Cache cache = new Cache(cacheDir, 100 * 1024L * 1024L);

        client.setCache(cache);

        return super.getGitHubBuilder()
            .withConnector(new OkHttpConnector(new OkUrlFactory(client)));
    }

    // TODO: Show the same actions with Default, OkHttp, and OkHttp with Cache
    // Verify how each behaves


}
