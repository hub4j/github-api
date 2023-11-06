package org.kohsuke.github.extras.authorization;

import java.lang.reflect.Method;
import java.security.PrivateKey;
import java.time.Instant;
import java.util.Date;

import io.jsonwebtoken.JwtBuilder;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.jackson.io.JacksonSerializer;

final class JwtBuilderUtil {
    private static Method getMethod(Object obj, String method, Class<?>... params) throws NoSuchMethodException {
        Class<?> type = obj.getClass();
        return type.getMethod(method, params);
    }

    private static boolean hasMethod(Object obj, String method, Class<?>... params) {
        try {
            return JwtBuilderUtil.getMethod(obj, method, params) != null;
        } catch (NoSuchMethodException e) {
            return false;
        }
    }

    static String buildJwt(Instant issuedAt, Instant expiration, String applicationId, PrivateKey privateKey) {
        JwtBuilder jwtBuilder = Jwts.builder();
        if (JwtBuilderUtil.hasMethod(jwtBuilder, "issuedAt", Date.class)) {
            jwtBuilder = jwtBuilder.issuedAt(Date.from(issuedAt))
                    .expiration(Date.from(expiration))
                    .issuer(applicationId)
                    .signWith(privateKey, Jwts.SIG.RS256);
            return jwtBuilder.json(new JacksonSerializer<>()).compact();
        }

        // older jjwt library versions
        try {
            return JwtBuilderUtil.buildByReflection(jwtBuilder, issuedAt, expiration, applicationId, privateKey);
        } catch (ReflectiveOperationException e) {
            throw new JwtReflectiveBuilderException(
                    "Exception building a JWT with reflective access to outdated versions of jjwt. Please consider an update.",
                    e);
        }
    }

    private static String buildByReflection(JwtBuilder jwtBuilder, Instant issuedAt, Instant expiration,
            String applicationId,
            PrivateKey privateKey) throws ReflectiveOperationException {

        Object builderObj = jwtBuilder;

        Method setIssuedAtMethod = JwtBuilderUtil.getMethod(builderObj, "setIssuedAt", Date.class);
        builderObj = setIssuedAtMethod.invoke(builderObj, Date.from(issuedAt));

        Method setExpirationMethod = JwtBuilderUtil.getMethod(builderObj, "setExpiration", Date.class);
        builderObj = setExpirationMethod.invoke(builderObj, Date.from(expiration));

        Method setIssuerMethod = JwtBuilderUtil.getMethod(builderObj, "setIssuer", String.class);
        builderObj = setIssuerMethod.invoke(builderObj, applicationId);

        Method signWithMethod = JwtBuilderUtil.getMethod(builderObj, "signWith", PrivateKey.class,
                SignatureAlgorithm.class);
        builderObj = signWithMethod.invoke(builderObj, privateKey, SignatureAlgorithm.RS256);

        Method serializeToJsonMethod = JwtBuilderUtil.getMethod(builderObj, "serializeToJsonWith",
                JacksonSerializer.class);
        builderObj = serializeToJsonMethod.invoke(builderObj, new JacksonSerializer<>());

        JwtBuilder resultBuilder = (JwtBuilder) builderObj;
        return resultBuilder.compact();
    }
}
