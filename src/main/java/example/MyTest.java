package example;

import org.kohsuke.github.*;

import java.io.IOException;
import java.util.Iterator;

/**
 * Created by Gao on 2015/3/10.
 */
public class MyTest {
    public static void main(String[] args){
        try {
            GitHub gitHub = GitHub.connectAnonymously();
//            GitHub gitHub=  GitHub.connectUsingPassword("45408735@qq.com","1ujunlin@github");//GitHub.connect();
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
                if(each==null) break;
                //提交人的信息
                GHUser user = each.getAuthor();
                if( user!=null){
                System.out.println("提交人:"+user.getName());
                System.out.println( "Email:"+user.getEmail());}
                //此次提交干的事情
                GHCommit.ShortInfo shortInfo = each.getCommitShortInfo();
                if( shortInfo != null)
                System.out.println("Short Information:"+shortInfo.getMessage());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

}
