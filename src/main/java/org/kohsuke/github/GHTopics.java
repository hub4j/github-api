/*
 * The MIT License
 *
 * Copyright (c) 2018, Johannes Gerbershagen <johannes.gerbershagen@kabelmail.de>
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

import java.util.ArrayList;
import java.util.HashMap;
import java.io.IOException;

public class GHTopics{
    GitHub root;    
    
    public GHTopics (GitHub root){
	this.root = root;
    }
    /**
     * Get's all topics for a specified repository as array list
     * @param org the organization or user
     * @param repoName the repository name (without organization, user)
     */
    @Preview
    public ArrayList<String> getAll(String org, String repoName)throws IOException{
	HashMap names = root.retrieve().withPreview(Previews.MERCY).to("/repos/"+ org + '/' + repoName + "/topics", HashMap.class);	
	return (ArrayList<String>)names.get("names");
    }
    
}
