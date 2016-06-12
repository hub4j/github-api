/*
 *    Copyright $year slavinson
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */

package org.kohsuke.github;

import org.junit.Before;
import org.junit.Test;
import org.kohsuke.github.extras.PreviewHttpConnector;

import java.io.IOException;
import java.util.List;

public class GHLicenseTest {
    private GitHub gitHub;

    @Before
    public void setUp() throws Exception {
        gitHub = new GitHubBuilder()
                .fromCredentials()
                .withConnector(new PreviewHttpConnector())
                .build();
    }

    @Test
    public void listLicenses() throws IOException {
        List<GHLicense> licenses = gitHub.listLicenses();
        for (GHLicense lic : licenses) {
            System.out.println(lic.toString());
        }
        assert(true);
    }

    @Test(expected = IOException.class)
    public void ListLicensesWithoutPreviewConnection() throws IOException {
        GitHub.connect().listLicenses();
    }
}
