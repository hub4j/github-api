package org.kohsuke.github;

import java.util.Date;
import java.util.Locale;

/**
 * 
 * @author Yusuke Kokubo
 *
 */
public class GHBranch {
    GitHub root;
	GHRepository owner;

	GHUser creator;
	private String name;
        private GHCommitPointer commit;

	public GitHub getRoot() {
		return root;
	}
	
	public GHRepository getOwner() {
		return owner;
	}
	
	public GHUser getCreator() {
		return creator;
	}

	public String getName() {
		return name;
	}

        public GHCommitPointer getCommit() {
		return commit;
	}

        @Override
        public String toString() {
            return "Branch:"+name+" in "+owner.getUrl();
        }

        
	public GHBranch wrap(GHRepository repo) {
		this.owner = repo;
		this.root = repo.root;
		return this;
	}
}
