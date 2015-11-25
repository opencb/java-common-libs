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

import java.util.*;
import com.mongodb.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by imedina on 22/03/14.
 *
 * This class models and configure a physical connection to a specific database of a MongoDB server, notice this is
 * different from the Java driver where all databases from a single MongoDB server share the same configuration.
 * Therefore, different configurations can be applied to different databases.
 *
 * @author imedina
 * @author cyenyxe
 */

public class MongoDataStore {

    private Map<String, MongoDBCollection> mongoDBCollections = new HashMap<>();

    private MongoClient mongoClient;
    private DB db;
    private MongoDBConfiguration mongoDBConfiguration;

    protected Logger logger = LoggerFactory.getLogger(MongoDataStore.class);

    MongoDataStore(MongoClient mongoClient, DB db, MongoDBConfiguration mongoDBConfiguration) {
        this.mongoClient = mongoClient;
        this.db = db;
        this.mongoDBConfiguration = mongoDBConfiguration;
    }

    public boolean testConnection() {
        CommandResult commandResult = db.getStats();
        return commandResult != null && commandResult.getBoolean("ok");
    }


    public MongoDBCollection getCollection(String collection) {
        if(!mongoDBCollections.containsKey(collection)) {
            MongoDBCollection mongoDBCollection = new MongoDBCollection(db.getCollection(collection));
            mongoDBCollections.put(collection, mongoDBCollection);
            logger.debug("MongoDataStore: new MongoDB collection '{}' created", collection);
        }
        return mongoDBCollections.get(collection);
    }

    public MongoDBCollection createCollection(String collectionName) {
        if(!db.getCollectionNames().contains(collectionName)) {
            db.createCollection(collectionName, null);
        }
        return getCollection(collectionName);
    }

    public void dropCollection(String collectionName) {
        if(db.getCollectionNames().contains(collectionName)) {
            db.getCollection(collectionName).drop();
            mongoDBCollections.remove(collectionName);
        }
    }

    public List<String> getCollectionNames() {
        Iterator<String> iterator = db.getCollectionNames().iterator();
        List<String> list = new ArrayList<>();
        while(iterator.hasNext()) {
            list.add(iterator.next());
        }
        return list;
    }

    public Map<String, Object> getStats(String collectionName) {
        return new HashMap<>(db.getStats());
    }


    void close() {
        logger.info("MongoDataStore: connection closed");
        mongoClient.close();
    }


    /*
     * GETTERS, NO SETTERS ARE AVAILABLE TO MAKE THIS CLASS IMMUTABLE
     */

    public Map<String, MongoDBCollection> getMongoDBCollections() {
        return mongoDBCollections;
    }

    public DB getDb() {
        return db;
    }

    public String getDatabaseName() {
        return db.getName();
    }

    public MongoDBConfiguration getMongoDBConfiguration() {
        return mongoDBConfiguration;
    }

}
