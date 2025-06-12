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

import com.mongodb.MongoClientSettings;
import com.mongodb.MongoDriverInformation;
import com.mongodb.ServerAddress;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.internal.MongoClientImpl;
import org.junit.Test;

import java.util.Collections;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

/**
 * Created by imedina on 25/03/14.
 *
 * @author Ignacio Medina Castelo &lt;imedina@ebi.ac.uk&gt;
 * @author Cristina Yenyxe Gonzalez Garcia &lt;cyenyxe@ebi.ac.uk&gt;
 */
public class MongoDBConfigurationTest {

    @Test
    public void testInit() throws Exception {
        MongoDBConfiguration mongoDBConfiguration = MongoDBConfiguration.builder().init()
                .add("writeConcern", "ACK")
                .build();
        System.out.println(mongoDBConfiguration.toJson());

        MongoClientSettings settings = MongoClientSettings.builder()
                .applyToClusterSettings(builder -> builder.hosts(Collections.singletonList(new ServerAddress("localhost"))))
                .build();
        MongoClient client = new MongoClientImpl(settings, MongoDriverInformation.builder().build());
        MongoDatabase db = client.getDatabase("test");

        MongoDataStore mongoDataStore = new MongoDataStore(client, db, mongoDBConfiguration);

        assertNotNull(mongoDataStore);
        assertEquals(db, mongoDataStore.getDb());

        client.close();
    }
}
