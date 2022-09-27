package org.kohsuke.github;

import org.apache.commons.io.IOUtils;
import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringReader;
import java.nio.charset.Charset;
import java.util.function.Function;

import javax.annotation.Nonnull;

// TODO: Auto-generated Javadoc
/**
 * The Class PayloadRule.
 *
 * @author Stephen Connolly
 */
public class PayloadRule implements TestRule {

    private final String type;

    private Class<?> testClass;

    private String resourceName;

    /**
     * Instantiates a new payload rule.
     *
     * @param type the type
     */
    public PayloadRule(String type) {
        this.type = type;
    }

    /**
     * Apply.
     *
     * @param base the base
     * @param description the description
     * @return the statement
     */
    public Statement apply(final Statement base, final Description description) {
        return new Statement() {
            @Override
            public void evaluate() throws Throwable {
                Payload payload = description.getAnnotation(Payload.class);
                resourceName = payload == null ? description.getMethodName() : payload.value();
                testClass = description.getTestClass();
                try {
                    base.evaluate();
                } finally {
                    resourceName = null;
                }
            }
        };
    }

    /**
     * As input stream.
     *
     * @return the input stream
     * @throws FileNotFoundException the file not found exception
     */
    public InputStream asInputStream() throws FileNotFoundException {
        String name = resourceName.startsWith("/")
                ? resourceName + type
                : testClass.getSimpleName() + "/" + resourceName + type;
        InputStream stream = testClass.getResourceAsStream(name);
        if (stream == null) {
            throw new FileNotFoundException(String.format("Resource %s from class %s", name, testClass));
        }
        return stream;
    }

    /**
     * As bytes.
     *
     * @return the byte[]
     * @throws IOException Signals that an I/O exception has occurred.
     */
    public byte[] asBytes() throws IOException {
        InputStream input = asInputStream();
        try {
            return IOUtils.toByteArray(input);
        } finally {
            IOUtils.closeQuietly(input);
        }
    }

    /**
     * As string.
     *
     * @param encoding the encoding
     * @return the string
     * @throws IOException Signals that an I/O exception has occurred.
     */
    public String asString(Charset encoding) throws IOException {
        return new String(asBytes(), encoding.name());
    }

    /**
     * As string.
     *
     * @param encoding the encoding
     * @return the string
     * @throws IOException Signals that an I/O exception has occurred.
     */
    public String asString(String encoding) throws IOException {
        return new String(asBytes(), encoding);
    }

    /**
     * As string.
     *
     * @return the string
     * @throws IOException Signals that an I/O exception has occurred.
     */
    public String asString() throws IOException {
        return new String(asBytes(), Charset.defaultCharset().name());
    }

    /**
     * As reader.
     *
     * @return the reader
     * @throws FileNotFoundException the file not found exception
     */
    public Reader asReader() throws FileNotFoundException {
        return new InputStreamReader(asInputStream(), Charset.defaultCharset());
    }

    /**
     * As reader.
     *
     * @param transformer the transformer
     * @return the reader
     * @throws IOException Signals that an I/O exception has occurred.
     */
    public Reader asReader(@Nonnull Function<String, String> transformer) throws IOException {
        String payloadString = asString();
        return new StringReader(transformer.apply(payloadString));
    }

    /**
     * As reader.
     *
     * @param encoding the encoding
     * @return the reader
     * @throws IOException Signals that an I/O exception has occurred.
     */
    public Reader asReader(String encoding) throws IOException {
        return new InputStreamReader(asInputStream(), encoding);
    }

    /**
     * As reader.
     *
     * @param encoding the encoding
     * @return the reader
     * @throws FileNotFoundException the file not found exception
     */
    public Reader asReader(Charset encoding) throws FileNotFoundException {
        return new InputStreamReader(asInputStream(), encoding);
    }
}
