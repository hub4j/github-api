/*
 * GitHub API for Java
 * Copyright (C) 2009-2017 SonarSource SA
 * mailto:info AT sonarsource DOT com
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */
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
        System.out.println(str);
        GHEventPayload.PullRequest o = GitHub.connect().parseEventPayload(new StringReader(str),GHEventPayload.PullRequest.class);
        System.out.println(o);
    }
}
