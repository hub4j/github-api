package org.kohsuke.github;

import org.eclipse.jgit.api.CloneCommand;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.api.errors.InvalidRemoteException;
import org.eclipse.jgit.api.errors.JGitInternalException;
import org.eclipse.jgit.api.errors.TransportException;
import org.eclipse.jgit.internal.storage.file.FileRepository;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.storage.file.FileRepositoryBuilder;
import org.eclipse.jgit.transport.CredentialsProvider;
import org.eclipse.jgit.transport.UsernamePasswordCredentialsProvider;
import org.junit.Test;

import java.io.File;
import java.io.IOException;

/**
 * JGit API测试
 */
public class GitTest {

    public String remotePath = "https://github.com/qiyuezhetian/test.git";//远程库路径
    //    public String remotePath = "http://user@10.1.2.1:8080/project.git";//远程库路径
    public String localPath = "F:\\qiyuezhetian4\\test\\";//下载已有仓库到本地路径
    public String initPath = "F:\\test\\";//本地路径新建

    public Git getGit(String repositoryUrl, String privateToken, String repositoryDir)
            throws GitAPIException {
        CredentialsProvider credentialsProvider = new UsernamePasswordCredentialsProvider("PRIVATE-TOKEN",
                privateToken);
        Git git = Git.cloneRepository().setCredentialsProvider(credentialsProvider).setURI(repositoryUrl)
                .setDirectory(new File(repositoryDir)).call();
        return git;
    }

    /**
     * 克隆远程库
     *
     * @throws IOException
     * @throws GitAPIException
     */
    @Test
    public void testCloneWithToken() throws GitAPIException {
        // 设置远程服务器上的用户名和密码
        CredentialsProvider credentialsProvider = new
                UsernamePasswordCredentialsProvider("PRIVATE-TOKEN", "32f999835a75b76876f475435e37f20f17eb0f9b");

        // 克隆代码库命令
        CloneCommand cloneCommand = Git.cloneRepository();

        Git git = cloneCommand.setURI(remotePath) //设置远程URI
                .setBranch("master") //设置clone下来的分支
                .setDirectory(new File(localPath)) //设置下载存放路径
                .setCredentialsProvider(credentialsProvider) //设置权限验证
                .call();

        System.out.print(git.tag());
    }


    /**
     * 本地仓库新增文件
     */
    @Test
    public void testAddWithToken() throws IOException, GitAPIException {
        String fileName = "myfile6.txt";
        File myfile = new File(localPath + fileName);
        myfile.createNewFile();
        // git仓库地址
        Git git = new Git(new FileRepository(localPath + ".git"));

        // 添加文件
        git.add().addFilepattern(fileName).call();
    }


    /**
     * 本地提交代码
     */
    @Test
    public void testCommitWithToken() throws IOException, GitAPIException, JGitInternalException {
        // git仓库地址
        Git git = new Git(new FileRepository(localPath + ".git"));
        // 提交代码
        git.commit().setMessage("test jGit").call();
    }

    /**
     * push本地代码到远程仓库地址
     */
    @Test
    public void testPushWithToken() throws IOException, JGitInternalException, GitAPIException {
        CredentialsProvider credentialsProvider = new
                UsernamePasswordCredentialsProvider("PRIVATE-TOKEN", "32f999835a75b76876f475435e37f20f17eb0f9b");
        // git仓库地址
        Git git = new Git(new FileRepository(localPath + ".git"));
        git.push().setRemote("origin").setCredentialsProvider(credentialsProvider).call();
    }

    /**
     * 克隆远程库
     *
     * @throws IOException
     * @throws GitAPIException
     */
    @Test
    public void testClone() throws IOException, GitAPIException {
        //设置远程服务器上的用户名和密码
        UsernamePasswordCredentialsProvider usernamePasswordCredentialsProvider = new
                UsernamePasswordCredentialsProvider("qiyuezhetian", "Zhulinhao@123");

        //克隆代码库命令
        CloneCommand cloneCommand = Git.cloneRepository();

        Git git = cloneCommand.setURI(remotePath) //设置远程URI
                .setBranch("master") //设置clone下来的分支
                .setDirectory(new File(localPath)) //设置下载存放路径
                .setCredentialsProvider(usernamePasswordCredentialsProvider) //设置权限验证
                .call();

        System.out.print(git.tag());
    }

    /**
     * 本地新建仓库
     */
    @Test
    public void testCreate() throws IOException {
        //本地新建仓库地址
        Repository newRepo = FileRepositoryBuilder.create(new File(initPath + "/.git"));
        newRepo.create();
    }

    /**
     * 本地仓库新增文件
     */
    @Test
    public void testAdd() throws IOException, GitAPIException {
        String fileName = "myfile3.txt";
        File myfile = new File(localPath + fileName);
        myfile.createNewFile();
        //git仓库地址
        Git git = new Git(new FileRepository(localPath + ".git"));

        //添加文件
        git.add().addFilepattern(fileName).call();
    }

    /**
     * 本地提交代码
     */
    @Test
    public void testCommit() throws IOException, GitAPIException,
            JGitInternalException {
        //git仓库地址
        Git git = new Git(new FileRepository(localPath + ".git"));
        //提交代码
        git.commit().setMessage("test jGit").call();
    }

    /**
     * 拉取远程仓库内容到本地
     */
    @Test
    public void testPull() throws IOException, GitAPIException {

        UsernamePasswordCredentialsProvider usernamePasswordCredentialsProvider = new
                UsernamePasswordCredentialsProvider("qiyuezhetian", "Zhulinhao@123");
        //git仓库地址
        Git git = new Git(new FileRepository(localPath + ".git"));
        git.pull().setRemoteBranchName("master").
                setCredentialsProvider(usernamePasswordCredentialsProvider).call();
    }

    /**
     * push本地代码到远程仓库地址
     */
    @Test
    public void testPush() throws IOException, JGitInternalException,
            GitAPIException {

        UsernamePasswordCredentialsProvider usernamePasswordCredentialsProvider = new
                UsernamePasswordCredentialsProvider("qiyuezhetian", "Zhulinhao@123");
        //git仓库地址
        Git git = new Git(new FileRepository(localPath + ".git"));
        git.push().setRemote("origin").setCredentialsProvider(usernamePasswordCredentialsProvider).call();
    }
}