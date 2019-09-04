package org.kohsuke.github;

import org.junit.Test;

import java.io.IOException;

public class StatisticsTest extends AbstractGitHubApiTestBase {

    public static final String REPOSITORY = "martinvanzijl/sandbox";
    public static int MAX_ITERATIONS = 3;
    public static int SLEEP_INTERVAL = 5000;

    private GitHub github;
    private GHRepository repo;

    @Override
    public void setUp() throws Exception {
        github = GitHub.connect();
        repo = github.getRepository(REPOSITORY);
    }

    @Test
    @SuppressWarnings("SleepWhileInLoop")
    public void testContributorStats() throws IOException, InterruptedException {
         for (int i = 0; i < MAX_ITERATIONS; i += 1) {
            if(repo.getContributorStats() == null) {
                Thread.sleep(SLEEP_INTERVAL);
            }
            else {
                return;
            }
        }
        fail("Statistics took too long to retrieve.");
    }

    @Test
    @SuppressWarnings("SleepWhileInLoop")
    public void testCommitActivity() throws IOException, InterruptedException {
         for (int i = 0; i < MAX_ITERATIONS; i += 1) {
            if(repo.getCommitActivity() == null) {
                Thread.sleep(SLEEP_INTERVAL);
            }
            else {
                return;
            }
        }
        fail("Statistics took too long to retrieve.");
    }

    @Test
    @SuppressWarnings("SleepWhileInLoop")
    public void testCodeFrequency() throws IOException, InterruptedException {
         for (int i = 0; i < MAX_ITERATIONS; i += 1) {
            if(repo.getCodeFrequency() == null) {
                Thread.sleep(SLEEP_INTERVAL);
            }
            else {
                return;
            }
        }
        fail("Statistics took too long to retrieve.");
    }

    @Test
    @SuppressWarnings("SleepWhileInLoop")
    public void testParticipation() throws IOException, InterruptedException {
         for (int i = 0; i < MAX_ITERATIONS; i += 1) {
            if(repo.getParticipation() == null) {
                Thread.sleep(SLEEP_INTERVAL);
            }
            else {
                return;
            }
        }
        fail("Statistics took too long to retrieve.");
    }

    @Test
    @SuppressWarnings("SleepWhileInLoop")
    public void testPunchCard() throws IOException, InterruptedException {
         for (int i = 0; i < MAX_ITERATIONS; i += 1) {
            if(repo.getPunchCard() == null) {
                Thread.sleep(SLEEP_INTERVAL);
            }
            else {
                return;
            }
        }
        fail("Statistics took too long to retrieve.");
    }
}
