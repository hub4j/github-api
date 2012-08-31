/*
 * The MIT License
 *
 * Copyright 2012 Honza Brázdil.
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

import java.io.IOException;

/**
 *
 * @author Honza Brázdil
 */
public class GHSmallUser {
	private GitHub root;
	private String avatar_url, login, url, gravatar_id;
	private Long id;
	
	/*package*/ GHSmallUser wrapUp(GitHub root) {
        this.root = root;
        return this;
    }

	
	public String getAvatar_url() {
		return avatar_url;
	}

	public String getLogin() {
		return login;
	}

	public String getUrl() {
		return url;
	}

	public String getGravatar_id() {
		return gravatar_id;
	}

	public Long getId() {
		return id;
	}
	
	public GHUser getUser() throws IOException{
		return root.getUser(login);
	}
}
