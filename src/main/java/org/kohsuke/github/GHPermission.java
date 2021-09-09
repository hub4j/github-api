/*
 * The MIT License
 *
 * Copyright 2016 CloudBees, Inc.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

package org.kohsuke.github;

import java.util.Locale;

/**
 * Permission for a user in a repository.
 *
 * @see <a href="https://developer.github.com/v3/repos/collaborators/#review-a-users-permission-level">API</a>
 */
class GHPermission {

    private String permission;
    private GHUser user;

    /**
     * Gets permission.
     *
     * @return one of {@code admin}, {@code write}, {@code read}, or {@code none}
     */
    public String getPermission() {
        return permission;
    }

    /**
     * Gets permission type.
     *
     * @return the permission type
     */
    public GHPermissionType getPermissionType() {
        return Enum.valueOf(GHPermissionType.class, permission.toUpperCase(Locale.ENGLISH));
    }

    /**
     * Gets user.
     *
     * @return the user
     */
    public GHUser getUser() {
        return user;
    }

    void wrapUp(GitHub root) {
        if (user != null) {
        }
    }

}
