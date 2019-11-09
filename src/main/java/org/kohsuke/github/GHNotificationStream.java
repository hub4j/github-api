package org.kohsuke.github;

import java.io.IOException;
import java.util.Date;
import java.util.Iterator;
import java.util.NoSuchElementException;

/**
 * Listens to GitHub notification stream.
 *
 * <p>
 * This class supports two modes of retrieving notifications that can
 * be controlled via {@link #nonBlocking(boolean)}.
 *
 * <p>
 * In the blocking mode, which is the default, iterator will be infinite.
 * The call to {@link Iterator#next()} will block until a new notification
 * arrives. This is useful for application that runs perpetually and reacts
 * to notifications.
 *
 * <p>
 * In the non-blocking mode, the iterator will only report the set of
 * notifications initially retrieved from GitHub, then quit. This is useful
 * for a batch application to process the current set of notifications.
 *
 * @author Kohsuke Kawaguchi
 * @see GitHub#listNotifications()
 * @see GHRepository#listNotifications()
 */
public class GHNotificationStream extends GHObjectBase implements Iterable<GHThread> {

    private Boolean all, participating;
    private String since;
    private String apiUrl;
    private boolean nonBlocking = false;

    /*package*/ GHNotificationStream(GitHub root, String apiUrl) {
        this.root = root;
        this.apiUrl = apiUrl;
    }

    /**
     * Should the stream include notifications that are already read?
     */
    public GHNotificationStream read(boolean v) {
        all = v;
        return this;
    }

    /**
     * Should the stream be restricted to notifications in which the user
     * is directly participating or mentioned?
     */
    public GHNotificationStream participating(boolean v) {
        participating = v;
        return this;
    }

    public GHNotificationStream since(long timestamp) {
        return since(new Date(timestamp));
    }

    public GHNotificationStream since(Date dt) {
        since = GitHub.printDate(dt);
        return this;
    }

    /**
     * If set to true, {@link #iterator()} will stop iterating instead of blocking and
     * waiting for the updates to arrive.
     */
    public GHNotificationStream nonBlocking(boolean v) {
        this.nonBlocking = v;
        return this;
    }

    /**
     * Returns an infinite blocking {@link Iterator} that returns
     * {@link GHThread} as notifications arrive.
     */
    public Iterator<GHThread> iterator() {
        // capture the configuration setting here
        final Requester req = new Requester(root).method("GET")
                .with("all", all).with("participating", participating).with("since", since);

        return new Iterator<GHThread>() {
            /**
             * Stuff we've fetched but haven't returned to the caller.
             * Newer ones first.
             */
            private GHThread[] threads = EMPTY_ARRAY;

            /**
             * Next element in {@link #threads} to return. This counts down.
             */
            private int idx=-1;

            /**
             * threads whose updated_at is older than this should be ignored.
             */
            private long lastUpdated = -1;

            /**
             * Next request should have "If-Modified-Since" header with this value.
             */
            private String lastModified;

            /**
             * When is the next polling allowed?
             */
            private long nextCheckTime = -1;

            private GHThread next;

            public GHThread next() {
                if (next==null) {
                    next = fetch();
                    if (next==null)
                        throw new NoSuchElementException();
                }

                GHThread r = next;
                next = null;
                return r;
            }

            public boolean hasNext() {
                if (next==null)
                    next = fetch();
                return next!=null;
            }

            GHThread fetch() {
                try {
                    while (true) {// loop until we get new threads to return

                        // if we have fetched un-returned threads, use them first
                        while (idx>=0) {
                            GHThread n = threads[idx--];
                            long nt = n.getUpdatedAt().getTime();
                            if (nt >= lastUpdated) {
                                lastUpdated = nt;
                                return n.wrap(root);
                            }
                        }

                        if (nonBlocking && nextCheckTime>=0)
                            return null;    // nothing more to report, and we aren't blocking

                        // observe the polling interval before making the call
                        while (true) {
                            long now = System.currentTimeMillis();
                            if (nextCheckTime < now) break;
                            long waitTime = Math.min(Math.max(nextCheckTime - now, 1000), 60 * 1000);
                            Thread.sleep(waitTime);
                        }

                        req.setHeader("If-Modified-Since", lastModified);

                        threads = req.to(apiUrl, GHThread[].class);
                        if (threads==null) {
                            threads = EMPTY_ARRAY;  // if unmodified, we get empty array
                        } else {
                            // we get a new batch, but we want to ignore the ones that we've seen
                            lastUpdated++;
                        }
                        idx = threads.length-1;

                        nextCheckTime = calcNextCheckTime();
                        lastModified = req.getResponseHeader("Last-Modified");
                    }
                } catch (IOException e) {
                    throw new RuntimeException(e);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }

            private long calcNextCheckTime() {
                String v = req.getResponseHeader("X-Poll-Interval");
                if (v==null)    v="60";
                long seconds = Integer.parseInt(v);
                return System.currentTimeMillis() + seconds*1000;
            }

            public void remove() {
                throw new UnsupportedOperationException();
            }
        };
    }

    public void markAsRead() throws IOException {
        markAsRead(-1);
    }

    /**
     * Marks all the notifications as read.
     */
    public void markAsRead(long timestamp) throws IOException {
        final Requester req = new Requester(root).method("PUT");
        if (timestamp>=0)
            req.with("last_read_at", GitHub.printDate(new Date(timestamp)));
        req.asHttpStatusCode(apiUrl);
    }

    private static final GHThread[] EMPTY_ARRAY = new GHThread[0];
}
