/*
 * Copyright 2015-2020 OpenCB
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.opencb.commons.datastore.mongodb.test;

import org.junit.rules.ExternalResource;

/**
 * JUnit {@link org.junit.rules.ExternalResource} wrapper around the process-singleton
 * {@link EmbeddedMongoDBManager}. The rule ensures the embedded mongod is running when the
 * test class starts; it intentionally does NOT stop it in {@link #after()} — the singleton's
 * JVM shutdown hook owns the teardown.
 *
 * <p>Intended usage:</p>
 * <pre>
 * public class MyMongoTest {
 *     &#064;ClassRule
 *     public static final EmbeddedMongoDBRule EMBEDDED_MONGO = new EmbeddedMongoDBRule();
 *
 *     &#064;Test public void ... { }
 * }
 * </pre>
 *
 * <p>Subclasses may extend this rule to add per-class housekeeping (e.g. closing engine-level
 * connections) in {@link #after()}.</p>
 */
public class EmbeddedMongoDBRule extends ExternalResource {

    @Override
    protected void before() throws Throwable {
        EmbeddedMongoDBManager.getInstance().start();
    }

    @Override
    protected void after() {
        // Intentionally empty: the embedded mongod is a JVM-wide singleton owned by
        // EmbeddedMongoDBManager. Its shutdown hook stops it on JVM exit.
    }

    public EmbeddedMongoDBManager getManager() {
        return EmbeddedMongoDBManager.getInstance();
    }

    public String getConnectionString() {
        return EmbeddedMongoDBManager.getInstance().getConnectionString();
    }

    public int getPort() {
        return EmbeddedMongoDBManager.getInstance().getPort();
    }
}
