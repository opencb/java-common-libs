/*
 * Copyright 2015-2017 OpenCB
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

import com.mongodb.*;
import com.mongodb.client.ClientSession;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.TransactionBody;
import org.bson.Document;
import org.opencb.commons.datastore.core.result.WriteResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.function.Consumer;

/**
 * Created by imedina on 22/03/14.
 * <p>
 * This class models and configure a physical connection to a specific database of a MongoDB server, notice this is
 * different from the Java driver where all databases from a single MongoDB server share the same configuration.
 * Therefore, different configurations can be applied to different databases.
 *
 * @author imedina
 * @author cyenyxe
 */

public class MongoDataStore {

    private static final String REPL_SET_KEY = "repl";
    private MongoClient mongoClient;
    private MongoDatabase db;
    private MongoDBConfiguration mongoDBConfiguration;

    protected Logger logger = LoggerFactory.getLogger(MongoDataStore.class);

    public static final TransactionOptions DEFAULT_TRANSACTION_OPTION =
            TransactionOptions.builder()
                    .readPreference(ReadPreference.primary())
                    .readConcern(ReadConcern.LOCAL)
                    .writeConcern(WriteConcern.MAJORITY)
                    .build();

    MongoDataStore(MongoClient mongoClient, MongoDatabase db, MongoDBConfiguration mongoDBConfiguration) {
        this.mongoClient = mongoClient;
        this.db = db;
        this.mongoDBConfiguration = mongoDBConfiguration;
    }

    public boolean testConnection() {
//        CommandResult commandResult = db.getStats();
//        return commandResult != null && commandResult.getBoolean("ok");
        return true;
    }


    public MongoDBCollection getCollection(String collection) {
        return getCollection(collection, null, null);
    }

    public MongoDBCollection getCollection(String collection, WriteConcern writeConcern, ReadPreference readPreference) {
        MongoDBCollection mongoDBCollection = new MongoDBCollection(db.getCollection(collection));
        if (writeConcern != null) {
            mongoDBCollection.withWriteConcern(writeConcern);
        }
        if (readPreference != null) {
            mongoDBCollection.withReadPreference(readPreference);
        }
        logger.debug("MongoDataStore: new MongoDB collection '{}' created", collection);
        return mongoDBCollection;
    }

    public Document getServerStatus() {
        return db.runCommand(new Document("serverStatus", 1));
    }

    public Document getReplSetStatus() {
        return db.runCommand(new Document("replSetGetStatus", 1));
    }

    public boolean isReplSet() {
        Document document = getServerStatus();
        return document.containsKey(REPL_SET_KEY);
    }

    public ClientSession startSession() {
        return mongoClient.startSession(
                ClientSessionOptions.builder()
                        .defaultTransactionOptions(
                                TransactionOptions.builder().writeConcern(WriteConcern.MAJORITY).build())
                        .build());
    }

    public WriteResult commitSession(ClientSession clientSession, TransactionBody<WriteResult> txnBody) {
        WriteResult writeResult = null;
        try {
            writeResult = clientSession.withTransaction(txnBody);
        } catch (RuntimeException e) {
            writeResult = new WriteResult(-1, -1, -1, null, Collections.singletonList(new WriteResult.Fail("", e.getMessage())));
            logger.error("Transaction error: {}", e.getMessage(), e);
        } finally {
            clientSession.close();
            return writeResult;
        }
    }

    public MongoDBCollection createCollection(String collectionName) {
        if (!Arrays.asList(db.listCollectionNames()).contains(collectionName)) {
            db.createCollection(collectionName);
        }
        return getCollection(collectionName);
    }

    public void dropCollection(String collectionName) {
        db.listCollectionNames().forEach((Consumer<String>) s -> {
            if (s.equals(collectionName)) {
                db.getCollection(collectionName).drop();
            }
        });
    }

    public List<String> getCollectionNames() {
        Iterator<String> iterator = db.listCollectionNames().iterator();
        List<String> list = new ArrayList<>();
        while (iterator.hasNext()) {
            list.add(iterator.next());
        }
        return list;
    }

    public Map<String, Object> getStats(String collectionName) {
//        return new HashMap<>(db.getStats());
        return null;
    }


    void drop() {
        logger.debug("MongoDataStore: drop database '{}'", getDatabaseName());
        db.drop();
    }

    void close() {
        logger.debug("MongoDataStore: connection closed for database '{}'", getDatabaseName());
        mongoClient.close();
    }


    /*
     * GETTERS, NO SETTERS ARE AVAILABLE TO MAKE THIS CLASS IMMUTABLE
     */

    @Deprecated
    public Map<String, MongoDBCollection> getMongoDBCollections() {
        return Collections.emptyMap();
    }

    public MongoDatabase getDb() {
        return db;
    }

    public String getDatabaseName() {
        return db.getName();
    }

    public MongoDBConfiguration getMongoDBConfiguration() {
        return mongoDBConfiguration;
    }

}
