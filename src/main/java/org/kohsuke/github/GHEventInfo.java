package org.kohsuke.github;

import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.map.type.TypeFactory;
import org.codehaus.jackson.node.ObjectNode;

import java.io.IOException;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Date;
import java.util.Iterator;
import java.util.Map.Entry;

/**
 * Represents an event.
 *
 * @author Kohsuke Kawaguchi
 */
public class GHEventInfo {
    private GitHub root;

    // we don't want to expose Jackson dependency to the user. This needs databinding
    private ObjectNode payload;

    private String created_at;
    private String type;

    // these are all shallow objects
    private GHRepository repo;
    private GHUser actor;
    private GHOrganization org;
    
    public GHEvent getType() {
        String t = type;
        if (t.endsWith("Event"))    t=t.substring(0,t.length()-5);
        for (GHEvent e : GHEvent.values()) {
            if (e.name().replace("_","").equalsIgnoreCase(t))
                return e;
        }
        return null;    // unknown event type
    }

    /*package*/ GHEventInfo wrapUp(GitHub root) {
        this.root = root;
        return this;
    }

    public Date getCreatedAt() {
        return GitHub.parseDate(created_at);
    }

    /**
     * Repository where the change was made.
     */
    public GHRepository getRepository() throws IOException {
        return root.getRepository(repo.getName());
    }
    
    public GHUser getActor() throws IOException {
        return root.getUser(actor.getLogin());
    }

    public GHOrganization getOrganization() throws IOException {
        return (org==null || org.getLogin()==null) ? null : root.getOrganization(org.getLogin());
    }

    /**
     * Retrieves the payload.
     * 
     * @param type
     *      Specify one of the {@link Payload} subtype that defines a type-safe access to the payload.
     *      This must match the {@linkplain #getType() event type}.
     */
    public <T extends Payload> T getPayload(Class<T> type) {
        return type.cast(Proxy.newProxyInstance(getClass().getClassLoader(), new Class<?>[]{type}, new InvocationHandler() {
            public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
                if (method.getDeclaringClass() == Object.class)
                    return method.invoke(this, method, args); // hashCode, equals

                String name = method.getName();
                if (name.startsWith("get")) name = name.substring(3);

                Iterator<Entry<String,JsonNode>> itr = payload.getFields();
                while (itr.hasNext()) {
                    Entry<String, JsonNode> e = itr.next();
                    if (e.getKey().replace("_","").equalsIgnoreCase(name)) {
                        return GitHub.MAPPER.readValue(e.getValue().traverse(),
                                TypeFactory.type(method.getGenericReturnType()));
                    }
                }
                
                return null;
            }
        }));
    }

    /**
     * Marker interface for types used for databinding of the event payload.
     */
    public interface Payload {}

    public interface CommitComment extends Payload {
        // GHComment getComment();
    }

    public interface PullRequest extends Payload {
        String getAction();
        int getNumber();
        GHPullRequest getPullRequest();
    }
}
