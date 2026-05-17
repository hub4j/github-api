package org.kohsuke.github;

import org.junit.Assert;
import org.junit.Test;
import java.io.IOException;

public class GHCommitPointerTest {
    //week8 failing test:
    //test the null value
    // @Test
    // public void nullRepoNpe() throws Exception {

    //     GHCommitPointer pointer = new GHCommitPointer(); //new object value is null
    // 
         // throws NPE because repository is null.
    //     pointer.getCommit();
    // }

    //week9 fixed test
    //test the IOException， which is more controllable
    @Test
    public void nullRepoNpe() {
        GHCommitPointer pointer = new GHCommitPointer();

        //expect throw IOException, test fail otherwise
        IOException ex = Assert.assertThrows(IOException.class, pointer::getCommit); //method reference

        //continue to test if error message also contains "repository is null", test fail otherwise
        Assert.assertTrue(ex.getMessage().contains("repository is null"));
    }
}
