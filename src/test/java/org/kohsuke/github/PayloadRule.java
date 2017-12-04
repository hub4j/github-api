/*
 * GitHub API for Java
 * Copyright (C) 2009-2016 SonarSource SA
 * mailto:contact AT sonarsource DOT com
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
package org.kohsuke.github;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.Charset;
import org.apache.commons.io.IOUtils;
import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;

/**
 * @author Stephen Connolly
 */
public class PayloadRule implements TestRule {

    private final String type;

    private Class<?> testClass;

    private String resourceName;

    public PayloadRule(String type) {
        this.type = type;
    }

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

    public byte[] asBytes() throws IOException {
        InputStream input = asInputStream();
        try {
            return IOUtils.toByteArray(input);
        } finally {
            IOUtils.closeQuietly(input);
        }
    }

    public String asString(Charset encoding) throws IOException {
        return new String(asBytes(), encoding.name());
    }

    public String asString(String encoding) throws IOException {
        return new String(asBytes(), encoding);
    }

    public String asString() throws IOException {
        return new String(asBytes(), Charset.defaultCharset().name());
    }

    public Reader asReader() throws FileNotFoundException {
        return new InputStreamReader(asInputStream(), Charset.defaultCharset());
    }

    public Reader asReader(String encoding) throws IOException {
        return new InputStreamReader(asInputStream(), encoding);
    }
    public Reader asReader(Charset encoding) throws FileNotFoundException {
        return new InputStreamReader(asInputStream(), encoding);
    }
}
