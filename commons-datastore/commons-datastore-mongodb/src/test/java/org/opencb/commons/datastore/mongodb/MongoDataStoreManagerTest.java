/*
 * Copyright 2015 OpenCB
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

package org.opencb.commons.datastore.mongodb;

import org.junit.jupiter.api.AfterEach;
import org.junit.Assert;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Created by imedina on 27/03/14.
 */
public class MongoDataStoreManagerTest {

    private MongoDataStoreManager mongoDataStoreManager;
    private MongoDataStore mongoDataStore;

    @BeforeEach
    public void setUp() throws Exception {
        mongoDataStoreManager = new MongoDataStoreManager("127.0.0.1", 27017);
        mongoDataStoreManager.get("test").getDb().drop();
        mongoDataStore = mongoDataStoreManager.get("test", MongoDBConfiguration.builder().init().build());
    }

    @AfterEach
    public void tearDown() throws Exception {
        mongoDataStoreManager.close("test");
    }

    @Test
    public void testGet() throws Exception {
        Assert.assertTrue("MongoDB check connection to 'test' database failed", mongoDataStore.testConnection());
    }

    @Test
    public void testsGet() throws Exception {
        Assert.assertTrue(!mongoDataStoreManager.exists("test"));
        mongoDataStore.createCollection("collection1");
        Assert.assertTrue(mongoDataStoreManager.exists("test"));
    }
}
