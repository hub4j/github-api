/*
 * GitHub API for Java
 * Copyright (C) 2009-2017 SonarSource SA
 * mailto:info AT sonarsource DOT com
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */
package org.kohsuke.github;

import com.google.common.collect.Iterables;
import org.junit.Ignore;
import org.junit.Test;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.*;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.*;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

/**
 * Unit test for {@link GitHub}.
 */
@Ignore("ignored as out of scope of SonarSource's fork's changes")
public class GitHubTest {
    @Test
    public void testOffline() throws Exception {
        GitHub hub = GitHub.offline();
        assertEquals("https://api.github.invalid/test", hub.getApiURL("/test").toString());
        assertTrue(hub.isAnonymous());
        try {
            hub.getRateLimit();
            fail("Offline instance should always fail");
        } catch (IOException e) {
            assertEquals("Offline", e.getMessage());
        }
    }

    @Test
    public void testGitHubServerWithHttp() throws Exception {
        GitHub hub = GitHub.connectToEnterprise("http://enterprise.kohsuke.org/api/v3", "bogus", "bogus");
        assertEquals("http://enterprise.kohsuke.org/api/v3/test", hub.getApiURL("/test").toString());
    }

    @Test
    public void testGitHubServerWithHttps() throws Exception {
        GitHub hub = GitHub.connectToEnterprise("https://enterprise.kohsuke.org/api/v3", "bogus", "bogus");
        assertEquals("https://enterprise.kohsuke.org/api/v3/test", hub.getApiURL("/test").toString());
    }

    @Test
    public void testGitHubServerWithoutServer() throws Exception {
        GitHub hub = GitHub.connectUsingPassword("kohsuke", "bogus");
        assertEquals("https://api.github.com/test", hub.getApiURL("/test").toString());
    }

    @Test
    public void testGitHubBuilderFromEnvironment() throws IOException {

        Map<String, String> props = new HashMap<String, String>();

        props.put("login", "bogus");
        props.put("oauth", "bogus");
        props.put("password", "bogus");

        setupEnvironment(props);

        GitHubBuilder builder = GitHubBuilder.fromEnvironment();

        assertEquals("bogus", builder.user);
        assertEquals("bogus", builder.oauthToken);
        assertEquals("bogus", builder.password);

    }

    /*
     * Copied from StackOverflow: http://stackoverflow.com/a/7201825/2336755
     *
     * This allows changing the in memory process environment.
     *
     * Its used to wire in values for the github credentials to test that the GitHubBuilder works properly to resolve them.
     */
    private void setupEnvironment(Map<String, String> newenv) {
        try {
            Class<?> processEnvironmentClass = Class.forName("java.lang.ProcessEnvironment");
            Field theEnvironmentField = processEnvironmentClass.getDeclaredField("theEnvironment");
            theEnvironmentField.setAccessible(true);
            Map<String, String> env = (Map<String, String>) theEnvironmentField.get(null);
            env.putAll(newenv);
            Field theCaseInsensitiveEnvironmentField = processEnvironmentClass.getDeclaredField("theCaseInsensitiveEnvironment");
            theCaseInsensitiveEnvironmentField.setAccessible(true);
            Map<String, String> cienv = (Map<String, String>) theCaseInsensitiveEnvironmentField.get(null);
            cienv.putAll(newenv);
        } catch (NoSuchFieldException e) {
            try {
                Class[] classes = Collections.class.getDeclaredClasses();
                Map<String, String> env = System.getenv();
                for (Class cl : classes) {
                    if ("java.util.Collections$UnmodifiableMap".equals(cl.getName())) {
                        Field field = cl.getDeclaredField("m");
                        field.setAccessible(true);
                        Object obj = field.get(env);
                        Map<String, String> map = (Map<String, String>) obj;
                        map.clear();
                        map.putAll(newenv);
                    }
                }
            } catch (Exception e2) {
                e2.printStackTrace();
            }
        } catch (Exception e1) {
            e1.printStackTrace();
        }
    }

    @Test
    public void testGitHubBuilderFromCustomEnvironment() throws IOException {
        Map<String, String> props = new HashMap<String, String>();

        props.put("customLogin", "bogusLogin");
        props.put("customOauth", "bogusOauth");
        props.put("customPassword", "bogusPassword");
        props.put("customEndpoint", "bogusEndpoint");

        setupEnvironment(props);

        GitHubBuilder builder = GitHubBuilder.fromEnvironment("customLogin", "customPassword", "customOauth", "customEndpoint");

        assertEquals("bogusLogin", builder.user);
        assertEquals("bogusOauth", builder.oauthToken);
        assertEquals("bogusPassword", builder.password);
        assertEquals("bogusEndpoint", builder.endpoint);
    }

    @Test
    public void testGitHubEnterpriseDoesNotHaveRateLimit() throws IOException {
        GitHub github = spy(new GitHubBuilder().build());
        when(github.retrieve()).thenThrow(FileNotFoundException.class);

        GHRateLimit rateLimit = github.getRateLimit();
        assertThat(rateLimit.getResetDate(), notNullValue());
    }

    @Test
    public void testGitHubIsApiUrlValid() throws IOException {
        GitHub github = GitHub.connectAnonymously();
        //GitHub github = GitHub.connectToEnterpriseAnonymously("https://github.mycompany.com/api/v3/");
        try {
            github.checkApiUrlValidity();
        } catch (IOException ioe) {
            assertTrue(ioe.getMessage().contains("private mode enabled"));
        }
    }

    @Test
    public void listUsers() throws IOException {
        GitHub hub = GitHub.connect();
        for (GHUser u : Iterables.limit(hub.listUsers(), 10)) {
            assert u.getName() != null;
            System.out.println(u.getName());
        }
    }

    @Test
    public void getOrgs() throws IOException {
        GitHub hub = GitHub.connect();
        int iterations = 10;
        Set<Long> orgIds = new HashSet<Long>();
        for (GHOrganization org : Iterables.limit(hub.listOrganizations().withPageSize(2), iterations)) {
            orgIds.add(org.getId());
            System.out.println(org.getName());
        }
        assertThat(orgIds.size(), equalTo(iterations));
    }
}
