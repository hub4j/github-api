package org.kohsuke;

import org.kohsuke.github.GHEventPayload;
import org.kohsuke.github.GitHub;
import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.jetty.JettyRunner;

import java.io.IOException;
import java.io.StringReader;

/**
 * App to test the hook script. You need some internet-facing server that can forward the request to you
 * (typically via SSH reverse port forwarding.)
 *
 * @author Kohsuke Kawaguchi
 */
public class HookApp {
    public static void main(String[] args) throws Exception {
//        GitHub.connect().getMyself().getRepository("sandbox").createWebHook(
//                new URL("http://173.203.118.45:18080/"), EnumSet.of(GHEvent.PULL_REQUEST));
        JettyRunner jr = new JettyRunner(new HookApp());
        jr.addHttpListener(8080);
        jr.start();
    }

    public void doIndex(StaplerRequest req) throws IOException {
        String str = req.getParameter("payload");
        // System.out.println(str);
        GHEventPayload.PullRequest o = GitHub.connect().parseEventPayload(new StringReader(str), GHEventPayload.PullRequest.class);
        // System.out.println(o);
    }
}
