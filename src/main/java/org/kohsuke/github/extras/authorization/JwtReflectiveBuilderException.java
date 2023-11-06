package org.kohsuke.github.extras.authorization;

public class JwtReflectiveBuilderException extends RuntimeException {
    JwtReflectiveBuilderException(String msg, Throwable cause) {
        super(msg, cause);
    }
}
