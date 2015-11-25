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

import com.mongodb.*;
import com.mongodb.client.MongoDatabase;
import org.opencb.commons.datastore.core.DataStoreServerAddress;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

/**
 * Created by imedina on 22/03/14.
 */
public class MongoDataStoreManager {

    private static Map<String, MongoDataStore> mongoDataStores = new HashMap<>();
    private List<DataStoreServerAddress> dataStoreServerAddresses;

    //    private MongoDBConfiguration mongoDBConfiguration;
    private MongoDBConfiguration.ReadPreference readPreference;
    private String writeConcern;

    private Logger logger;


    public MongoDataStoreManager(String host, int port) {
        init();
        this.addServerAddress(new DataStoreServerAddress(host, port));
    }

    public MongoDataStoreManager(DataStoreServerAddress dataStoreServerAddress) {
        init();
        this.addServerAddress(dataStoreServerAddress);
    }

    public MongoDataStoreManager(List<DataStoreServerAddress> dataStoreServerAddresses) {
        init();
        this.addServerAddresses(dataStoreServerAddresses);
    }

//    public MongoDataStoreManager(MongoDBConfiguration mongoDBConfiguration) {
//        this.mongoDBConfiguration = mongoDBConfiguration;
//    }

    private void init() {
        dataStoreServerAddresses = new ArrayList<>();
        readPreference = MongoDBConfiguration.ReadPreference.PRIMARY_PREFERRED;
        writeConcern = "";

        logger = LoggerFactory.getLogger(MongoDataStoreManager.class);
    }


    public final void addServerAddress(DataStoreServerAddress dataStoreServerAddress) {
        if (dataStoreServerAddress != null) {
            if (this.dataStoreServerAddresses != null) {
                this.dataStoreServerAddresses.add(dataStoreServerAddress);
            }
        }
    }

    public final void addServerAddresses(List<DataStoreServerAddress> dataStoreServerAddresses) {
        if (dataStoreServerAddresses != null) {
            if (this.dataStoreServerAddresses != null) {
                this.dataStoreServerAddresses.addAll(dataStoreServerAddresses);
            }
        }
    }


    public MongoDataStore get(String database) {
        return get(database, MongoDBConfiguration.builder().init().build());
    }

    public MongoDataStore get(String database, MongoDBConfiguration mongoDBConfiguration) {
        if (!mongoDataStores.containsKey(database)) {
            MongoDataStore mongoDataStore = create(database, mongoDBConfiguration);
            logger.debug("MongoDataStoreManager: new MongoDataStore database '{}' created", database);
            mongoDataStores.put(database, mongoDataStore);
        }
        return mongoDataStores.get(database);
    }

    private MongoDataStore create(String database, MongoDBConfiguration mongoDBConfiguration) {
        MongoDataStore mongoDataStore = null;
        MongoClient mc = null;
        logger.debug("MongoDataStoreManager: creating a MongoDataStore object for database: '" + database + "' ...");
        long t0 = System.currentTimeMillis();
        if (database != null && !database.trim().equals("")) {
            // read DB configuration for that SPECIES.VERSION, by default
            // PRIMARY_DB is selected
//            String dbPrefix = applicationProperties.getProperty(speciesVersionPrefix + ".DB", "PRIMARY_DB");
            // We create the MongoClientOptions
            MongoClientOptions mongoClientOptions = new MongoClientOptions.Builder()
                    .connectionsPerHost(mongoDBConfiguration.getInt("connectionsPerHost", 100))
                    .connectTimeout(mongoDBConfiguration.getInt("connectTimeout", 10000))
                    .build();

            assert (dataStoreServerAddresses != null);

            // We create the MongoCredential object
            String user = mongoDBConfiguration.getString("username", "");
            String pass = mongoDBConfiguration.getString("password", "");
            MongoCredential mongoCredential = null;
            if ((user != null && !user.equals("")) || (pass != null && !pass.equals(""))) {
//                final DB authenticationDatabase;
                if (mongoDBConfiguration.get("authenticationDatabase") != null
                        && !mongoDBConfiguration.getString("authenticationDatabase").isEmpty()) {
//                        authenticationDatabase = mc.getDB(mongoDBConfiguration.getString("authenticationDatabase"));
                    mongoCredential = MongoCredential.createScramSha1Credential(user,
                            mongoDBConfiguration.getString("authenticationDatabase"), pass.toCharArray());
                } else {
//                        authenticationDatabase = db;
                    mongoCredential = MongoCredential.createScramSha1Credential(user, "", pass.toCharArray());
                }
//                    authenticationDatabase.authenticate(user, pass.toCharArray());
            }


            if (dataStoreServerAddresses.size() == 1) {
                if (mongoCredential != null) {
                    mc = new MongoClient(
                            new ServerAddress(dataStoreServerAddresses.get(0).getHost(), dataStoreServerAddresses.get(0).getPort()),
                            Arrays.asList(mongoCredential),
                            mongoClientOptions);
                } else {
                    mc = new MongoClient(
                            new ServerAddress(dataStoreServerAddresses.get(0).getHost(), dataStoreServerAddresses.get(0).getPort()),
                            mongoClientOptions);
                }
            } else {
                List<ServerAddress> serverAddresses = new ArrayList<>(dataStoreServerAddresses.size());
                for (DataStoreServerAddress serverAddress : dataStoreServerAddresses) {
                    serverAddresses.add(new ServerAddress(serverAddress.getHost(), serverAddress.getPort()));
                }
                if (mongoCredential != null) {
                    mc = new MongoClient(serverAddresses, Arrays.asList(mongoCredential), mongoClientOptions);
                } else {
                    mc = new MongoClient(serverAddresses, mongoClientOptions);
                }
            }

//                mc.setReadPreference(ReadPreference.secondary(new BasicDBObject("dc", "PG")));
//                mc.setReadPreference(ReadPreference.primary());
//                System.out.println("Replica Status: "+mc.getReplicaSetStatus());
            logger.debug(mongoDBConfiguration.toString());
            MongoDatabase db = mc.getDatabase(database);
//                db.setReadPreference(ReadPreference.secondary(new BasicDBObject("dc", "PG")));
//                db.setReadPreference(ReadPreference.primary());

            long t1 = System.currentTimeMillis();
            logger.debug("MongoDataStoreManager: MongoDataStore object for database: '" + database + "' created in " + (t0 - t1) + "ms");
            mongoDataStore = new MongoDataStore(mc, db, mongoDBConfiguration);
        } else {
            logger.debug("MongoDB database is null or empty");
        }
        return mongoDataStore;
    }

    public void drop(String database) {
        MongoClient mc = null;
        if (database != null && !database.trim().equals("")) {
            //                MongoClientOptions mongoClientOptions = new MongoClientOptions.Builder()
//                        .connectionsPerHost(mongoDBConfiguration.getInt("connectionsPerHost", 100))
//                        .connectTimeout(mongoDBConfiguration.getInt("connectTimeout", 10000))
//                        .build();

            if (dataStoreServerAddresses.size() == 1) {
                mc = new MongoClient(new ServerAddress(dataStoreServerAddresses.get(0).getHost(), dataStoreServerAddresses.get(0).getPort()));
            } else {
                List<ServerAddress> serverAddresses = new ArrayList<>(dataStoreServerAddresses.size());
                for (ServerAddress serverAddress : serverAddresses) {
                    serverAddresses.add(new ServerAddress(serverAddress.getHost(), serverAddress.getPort()));
                }
                mc = new MongoClient(serverAddresses);
            }

//                logger.debug(mongoDBConfiguration.toString());
            MongoDatabase db = mc.getDatabase(database);
//                String user = mongoDBConfiguration.getString("username", "");
//                String pass = mongoDBConfiguration.getString("password", "");
//                if((user != null && !user.equals("")) || (pass != null && !pass.equals(""))) {
//                    db.authenticate(user, pass.toCharArray());
//                }
            db.drop();

            long t1 = System.currentTimeMillis();
            logger.debug("MongoDataStoreManager: remove MongoDataStore object for database");
            mongoDataStores.remove(database);
        } else {
            logger.debug("MongoDB database is null or empty");
        }
    }

    public void close(String database) {
        if (mongoDataStores.containsKey(database)) {
            mongoDataStores.get(database).close();
            mongoDataStores.remove(database);
        }
    }


    /**
     * GETTERS AND SETTERS
     **/

    public List<DataStoreServerAddress> getDataStoreServerAddresses() {
        return dataStoreServerAddresses;
    }

    public void setDataStoreServerAddresses(List<DataStoreServerAddress> dataStoreServerAddresses) {
        this.dataStoreServerAddresses = dataStoreServerAddresses;
    }


    public MongoDBConfiguration.ReadPreference getReadPreference() {
        return readPreference;
    }

    public void setReadPreference(MongoDBConfiguration.ReadPreference readPreference) {
        this.readPreference = readPreference;
    }


    public String getWriteConcern() {
        return writeConcern;
    }

    public void setWriteConcern(String writeConcern) {
        this.writeConcern = writeConcern;
    }

}
