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

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;

/**
 * Created by imedina on 13/04/14.
 */
public class MongoDataStoreTest {

    private static MongoDataStoreManager mongoDataStoreManager;
    private static MongoDataStore mongoDataStore;

    @BeforeClass
    public static void setUp() throws Exception {
        mongoDataStoreManager = new MongoDataStoreManager("localhost", 27017);

        mongoDataStoreManager.drop("datastore_test");
        mongoDataStore = mongoDataStoreManager.get("datastore_test");
        mongoDataStore.createCollection("JUnitTest");
    }

    @AfterClass
    public static void tearDown() throws Exception {
        mongoDataStoreManager.close("datastore_test");
    }


    @Test
    public void testTest() throws Exception {
        mongoDataStore.testConnection();
    }

    @Test
    public void testGetCollection() throws Exception {
        mongoDataStore.getCollection("JUnitTest");
    }

    @Test
    public void testCreateCollection() throws Exception {

    }

    @Test
    public void testDropCollection() throws Exception {

    }

    @Test
    public void testGetCollectionNames() throws Exception {
        List<String> colNames = mongoDataStore.getCollectionNames();
        Arrays.toString(colNames.toArray());
    }

    @Test
    public void testGetStats() throws Exception {

    }
}
