package example;

import org.kohsuke.github.GHCommit;
import org.kohsuke.github.GHRepository;
import org.kohsuke.github.GitHub;
import org.kohsuke.github.PagedIterable;

import java.io.IOException;
import java.util.Iterator;

/**
 * Created by Gao on 2015/3/10.
 */
public class MyTest {
    public static void main(String[] args){
        try {
            GitHub gitHub = GitHub.connectAnonymously();//GitHub.connectUsingPassword("xwuguo@gmail.com","1314woaini");//GitHub.connect();
//            GHRepository repo = gitHub.createRepository("new-repository","this is my new repository",
//                    "http://www.kohsuke.org/",true/*public*/);
//            repo.addCollaborators(gitHub.getUser("abayer"),gitHub.getUser("rtyler"));
            GHRepository repo = gitHub.getRepository("1ujun1in/github-api");
            System.out.println("Repo description!");
            System.out.println(repo.getDescription());
            System.out.println("Repo Name!");
            System.out.println(repo.getName());
            System.out.println("OpenIssueCount");
            System.out.println(repo.getOpenIssueCount());
            System.out.println("README CONTENT");
            System.out.println(repo.getReadme().getContent());
            System.out.println("END!!");
            PagedIterable<GHCommit> commitsList = repo.listCommits();
            Iterator<GHCommit> iterator = commitsList.iterator();
            while(iterator.hasNext()){
               GHCommit each =  iterator.next();
                
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

}
