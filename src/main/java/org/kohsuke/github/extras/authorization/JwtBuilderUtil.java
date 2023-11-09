package org.kohsuke.github.extras.authorization;

import io.jsonwebtoken.JwtBuilder;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Serializer;
import io.jsonwebtoken.jackson.io.JacksonSerializer;
import io.jsonwebtoken.security.SignatureAlgorithm;
import org.kohsuke.github.GHException;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.security.Key;
import java.security.PrivateKey;
import java.time.Instant;
import java.util.Date;
import java.util.logging.Logger;

/**
 * This is a util to build a JWT.
 *
 * <p>
 * This class is used to build a JWT using the jjwt library. It uses reflection to support older versions of jjwt. The
 * class may be removed once we are sure we no longer need to support pre-0.12.x versions of jjwt.
 * </p>
 */
final class JwtBuilderUtil {

    private static final Logger LOGGER = Logger.getLogger(JwtBuilderUtil.class.getName());

    private static IJwtBuilder builder;

    /**
     * Build a JWT.
     *
     * @param issuedAt
     *            issued at
     * @param expiration
     *            expiration
     * @param applicationId
     *            application id
     * @param privateKey
     *            private key
     * @return JWT
     */
    static String buildJwt(Instant issuedAt, Instant expiration, String applicationId, PrivateKey privateKey) {
        if (builder == null) {
            createBuilderImpl(issuedAt, expiration, applicationId, privateKey);
        }
        return builder.buildJwt(issuedAt, expiration, applicationId, privateKey);
    }

    private static void createBuilderImpl(Instant issuedAt,
            Instant expiration,
            String applicationId,
            PrivateKey privateKey) {
        // Figure out which builder to use and cache it. We don't worry about thread safety here because we're fine if
        // the builder is assigned multiple times. The end result will be the same.
        try {
            builder = new DefaultBuilderImpl();
        } catch (NoSuchMethodError | NoClassDefFoundError e) {
            LOGGER.warning(
                    "You are using an outdated version of the io.jsonwebtoken:jjwt-* suite. v0.12.x or later is recommended.");

            try {
                ReflectionBuilderImpl reflectionBuider = new ReflectionBuilderImpl();
                // Build a JWT to eagerly check for any reflection errors.
                reflectionBuider.buildJwtWithReflection(issuedAt, expiration, applicationId, privateKey);

                builder = reflectionBuider;
            } catch (ReflectiveOperationException re) {
                throw new GHException(
                        "Could not build JWT using reflection on io.jsonwebtoken:jjwt-* suite."
                                + "The minimum supported version is v0.11.x, v0.12.x or later is recommended.",
                        re);
            }
        }
    }

    /**
     * IJwtBuilder interface to isolate loading of JWT classes allowing us to catch and handle linkage errors.
     */
    interface IJwtBuilder {
        /**
         * Build a JWT.
         *
         * @param issuedAt
         *            issued at
         * @param expiration
         *            expiration
         * @param applicationId
         *            application id
         * @param privateKey
         *            private key
         * @return JWT
         */
        String buildJwt(Instant issuedAt, Instant expiration, String applicationId, PrivateKey privateKey);
    }

    /**
     * A class to isolate loading of JWT classes allowing us to catch and handle linkage errors.
     *
     * Without this class, JwtBuilderUtil.buildJwt() immediately throws NoClassDefFoundError when called. With this
     * class the error is thrown when DefaultBuilder.build() is called allowing us to catch and handle it.
     */
    private static class DefaultBuilderImpl implements IJwtBuilder {
        /**
         * This method builds a JWT using 0.12.x or later versions of jjwt library
         *
         * @param issuedAt
         *            issued at
         * @param expiration
         *            expiration
         * @param applicationId
         *            application id
         * @param privateKey
         *            private key
         * @return JWT
         */
        public String buildJwt(Instant issuedAt, Instant expiration, String applicationId, PrivateKey privateKey) {

            // io.jsonwebtoken.security.SignatureAlgorithm is not present in v0.11.x and below.
            // Trying to call a method that uses it causes "NoClassDefFoundError" if v0.11.x is being used.
            SignatureAlgorithm rs256 = Jwts.SIG.RS256;

            JwtBuilder jwtBuilder = Jwts.builder();
            jwtBuilder = jwtBuilder.issuedAt(Date.from(issuedAt))
                    .expiration(Date.from(expiration))
                    .issuer(applicationId)
                    .signWith(privateKey, rs256)
                    .json(new JacksonSerializer<>());
            return jwtBuilder.compact();
        }
    }

    /**
     * A class to encapsulate building a JWT using reflection.
     */
    private static class ReflectionBuilderImpl implements IJwtBuilder {

        private Method setIssuedAtMethod;
        private Method setExpirationMethod;
        private Method setIssuerMethod;
        private Enum<?> rs256SignatureAlgorithm;
        private Method signWithMethod;
        private Method serializeToJsonMethod;

        ReflectionBuilderImpl() throws ReflectiveOperationException {
            JwtBuilder jwtBuilder = Jwts.builder();
            Class<?> jwtReflectionClass = jwtBuilder.getClass();

            setIssuedAtMethod = jwtReflectionClass.getMethod("setIssuedAt", Date.class);
            setIssuerMethod = jwtReflectionClass.getMethod("setIssuer", String.class);
            setExpirationMethod = jwtReflectionClass.getMethod("setExpiration", Date.class);
            Class<?> signatureAlgorithmClass = Class.forName("io.jsonwebtoken.SignatureAlgorithm");
            rs256SignatureAlgorithm = createEnumInstance(signatureAlgorithmClass, "RS256");
            signWithMethod = jwtReflectionClass.getMethod("signWith", Key.class, signatureAlgorithmClass);
            serializeToJsonMethod = jwtReflectionClass.getMethod("serializeToJsonWith", Serializer.class);
        }

        /**
         * This method builds a JWT using older (pre 0.12.x) versions of jjwt library by leveraging reflection.
         *
         * @param issuedAt
         *            issued at
         * @param expiration
         *            expiration
         * @param applicationId
         *            application id
         * @param privateKey
         *            private key
         * @return JWTBuilder
         */
        public String buildJwt(Instant issuedAt, Instant expiration, String applicationId, PrivateKey privateKey) {

            try {
                return buildJwtWithReflection(issuedAt, expiration, applicationId, privateKey);
            } catch (ReflectiveOperationException e) {
                // This should never happen. Reflection errors should have been caught during initialization.
                throw new GHException("Reflection errors during JWT creation should have been checked already.", e);
            }
        }

        private String buildJwtWithReflection(Instant issuedAt,
                Instant expiration,
                String applicationId,
                PrivateKey privateKey) throws IllegalAccessException, InvocationTargetException {
            JwtBuilder jwtBuilder = Jwts.builder();
            Object builderObj = jwtBuilder;
            builderObj = setIssuedAtMethod.invoke(builderObj, Date.from(issuedAt));
            builderObj = setExpirationMethod.invoke(builderObj, Date.from(expiration));
            builderObj = setIssuerMethod.invoke(builderObj, applicationId);
            builderObj = signWithMethod.invoke(builderObj, privateKey, rs256SignatureAlgorithm);
            builderObj = serializeToJsonMethod.invoke(builderObj, new JacksonSerializer<>());
            return ((JwtBuilder) builderObj).compact();
        }

        @SuppressWarnings("unchecked")
        private static <T extends Enum<T>> T createEnumInstance(Class<?> type, String name) {
            return Enum.valueOf((Class<T>) type, name);
        }
    }
}
