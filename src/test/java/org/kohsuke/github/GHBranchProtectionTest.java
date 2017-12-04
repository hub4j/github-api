/*
 * GitHub API for Java
 * Copyright (C) 2009-2016 SonarSource SA
 * mailto:contact AT sonarsource DOT com
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

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.kohsuke.github.GHBranchProtection.EnforceAdmins;
import org.kohsuke.github.GHBranchProtection.RequiredReviews;
import org.kohsuke.github.GHBranchProtection.RequiredStatusChecks;

import java.io.FileNotFoundException;

@Ignore("ignored as out of scope of SonarSource's fork's changes")
public class GHBranchProtectionTest extends AbstractGitHubApiTestBase {
    private static final String BRANCH = "bp-test";
    private static final String BRANCH_REF = "heads/" + BRANCH;

    private GHBranch branch;

    private GHRepository repo;

    @Before
    @Override
    public void setUp() throws Exception {
        super.setUp();

        repo = gitHub.getRepository("github-api-test-org/GHContentIntegrationTest").fork();

        try {
            repo.getRef(BRANCH_REF);
        } catch (FileNotFoundException e) {
            repo.createRef("refs/" + BRANCH_REF, repo.getBranch("master").getSHA1());
        }

        branch = repo.getBranch(BRANCH);

        if (branch.isProtected()) {
            branch.disableProtection();
        }

        branch = repo.getBranch(BRANCH);
        assertFalse(branch.isProtected());
    }

    @Test
    public void testEnableBranchProtections() throws Exception {
        // team/user restrictions require an organization repo to test against
        GHBranchProtection protection = branch.enableProtection()
                .addRequiredChecks("test-status-check")
                .requireBranchIsUpToDate()
                .requireCodeOwnReviews()
                .dismissStaleReviews()
                .includeAdmins()
                .enable();

        RequiredStatusChecks statusChecks = protection.getRequiredStatusChecks();
        assertNotNull(statusChecks);
        assertTrue(statusChecks.isRequiresBranchUpToDate());
        assertTrue(statusChecks.getContexts().contains("test-status-check"));
        
        RequiredReviews requiredReviews = protection.getRequiredReviews();
        assertNotNull(requiredReviews);
        assertTrue(requiredReviews.isDismissStaleReviews());
        assertTrue(requiredReviews.isRequireCodeOwnerReviews());
        
        EnforceAdmins enforceAdmins = protection.getEnforceAdmins();
        assertNotNull(enforceAdmins);
        assertTrue(enforceAdmins.isEnabled());
    }

    @Test
    public void testEnableProtectionOnly() throws Exception {
        branch.enableProtection().enable();
        assertTrue(repo.getBranch(BRANCH).isProtected());
    }

    @Test
    public void testEnableRequireReviewsOnly() throws Exception {
        GHBranchProtection protection = branch.enableProtection()
                .requireReviews()
                .enable();
        
       assertNotNull(protection.getRequiredReviews());
    }
}
