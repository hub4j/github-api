package org.kohsuke.github.extras.authorization;

import java.lang.reflect.Method;
import java.security.PrivateKey;
import java.time.Instant;
import java.util.Date;
import java.util.logging.Logger;

import io.jsonwebtoken.JwtBuilder;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.jackson.io.JacksonSerializer;

/**
 * This is a util to build a JWT.
 *
 * <p>
 * This class is used to build a JWT using the jjwt library. It uses reflection
 * to support older versions of jjwt. The class may be removed again, once we
 * are sure, we do no longer need to support pre-0.12.x versions of jjwt.
 * </p>
 */
final class JwtBuilderUtil {

    private static final Logger LOGGER = Logger.getLogger(JwtBuilderUtil.class.getName());

    /**
     * Get a method from an object.
     *
     * @param obj    object
     * @param method method name
     * @param params parameters of the method
     * @return method
     * @throws NoSuchMethodException if the method does not exist
     */
    private static Method getMethod(Object obj, String method, Class<?>... params) throws NoSuchMethodException {
        Class<?> type = obj.getClass();
        return type.getMethod(method, params);
    }

    /**
     * Check if an object has a method.
     *
     * @param obj    object
     * @param method method name
     * @param params parameters of the method
     * @return true if the method exists
     */
    private static boolean hasMethod(Object obj, String method, Class<?>... params) {
        try {
            return JwtBuilderUtil.getMethod(obj, method, params) != null;
        } catch (NoSuchMethodException e) {
            return false;
        }
    }

    /**
     * Build a JWT.
     *
     * @param issuedAt      issued at
     * @param expiration    expiration
     * @param applicationId application id
     * @param privateKey    private key
     * @return JWT
     */
    static String buildJwt(Instant issuedAt, Instant expiration, String applicationId, PrivateKey privateKey) {
        JwtBuilder jwtBuilder = Jwts.builder();
        if (JwtBuilderUtil.hasMethod(jwtBuilder, "issuedAt", Date.class)) {
            jwtBuilder = jwtBuilder.issuedAt(Date.from(issuedAt))
                    .expiration(Date.from(expiration))
                    .issuer(applicationId)
                    .signWith(privateKey, Jwts.SIG.RS256);
            return jwtBuilder.json(new JacksonSerializer<>()).compact();
        }

        LOGGER.warning(
                "You are using an outdated version of the io.jsonwebtoken:jjwt-* suite. Please consider an update.");

        // older jjwt library versions
        try {
            return JwtBuilderUtil.buildByReflection(jwtBuilder, issuedAt, expiration, applicationId, privateKey);
        } catch (ReflectiveOperationException e) {
            throw new JwtReflectiveBuilderException(
                    "Exception building a JWT with reflective access to outdated versions of the io.jsonwebtoken:jjwt-* suite. Please consider an update.",
                    e);
        }
    }

    /**
     * This method builds a JWT using older (pre 0.12.x) versions of jjwt library by
     * leveraging reflection.
     *
     * @param jwtBuilder    builder object
     * @param issuedAt      issued at
     * @param expiration    expiration
     * @param applicationId application id
     * @param privateKey    private key
     * @return JWT
     * @throws ReflectiveOperationException if reflection fails
     */
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
