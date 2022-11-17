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

import com.mongodb.ReadPreference;
import com.mongodb.*;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.internal.MongoClientImpl;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.StopWatch;
import org.opencb.commons.datastore.core.DataStoreServerAddress;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.*;
import java.util.concurrent.TimeUnit;

import static org.opencb.commons.datastore.mongodb.MongoDBConfiguration.*;

/**
 * Created by imedina on 22/03/14.
 */
public class MongoDataStoreManager implements AutoCloseable {

    private Map<String, MongoDataStore> mongoDataStores = new HashMap<>();
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
        return get(database, builder().init().build());
    }

    public MongoDataStore get(String database, MongoDBConfiguration mongoDBConfiguration) {
        if (!mongoDataStores.containsKey(database)) {
            MongoDataStore mongoDataStore = create(database, mongoDBConfiguration);
            logger.info("MongoDataStoreManager: new MongoDataStore database '{}' created", database);
            mongoDataStores.put(database, mongoDataStore);
        }
        return mongoDataStores.get(database);
    }

    private MongoDataStore create(String database, MongoDBConfiguration mongoDBConfiguration) {
        if (StringUtils.isBlank(database)) {
            throw new IllegalArgumentException("MongoDB database is null or empty");
        }

        logger.info("MongoDataStoreManager: creating a MongoDataStore object for database: '" + database + "' configuration: "
                + mongoDBConfiguration.toJson());
        StopWatch stopWatch = StopWatch.createStarted();
        // read DB configuration for that SPECIES.VERSION, by default
        // PRIMARY_DB is selected
//            String dbPrefix = applicationProperties.getProperty(speciesVersionPrefix + ".DB", "PRIMARY_DB");
        // We create the MongoClientOptions
        MongoClientSettings.Builder builder = MongoClientSettings.builder()
                .applyToSocketSettings(b -> b.connectTimeout(mongoDBConfiguration.getInt(CONNECT_TIMEOUT,
                        CONNECT_TIMEOUT_DEFAULT), TimeUnit.MILLISECONDS))
                .readPreference(ReadPreference.valueOf(mongoDBConfiguration.getString(READ_PREFERENCE,
                        READ_PREFERENCE_DEFAULT.getValue())))
                .applyToConnectionPoolSettings(b -> b.maxSize(mongoDBConfiguration.getInt(CONNECTIONS_PER_HOST,
                        CONNECTIONS_PER_HOST_DEFAULT)));

        if (mongoDBConfiguration.getString(REPLICA_SET) != null && !mongoDBConfiguration.getString(REPLICA_SET).isEmpty()) {
            logger.debug("Setting replicaSet to " + mongoDBConfiguration.getString(REPLICA_SET));
            builder.applyToClusterSettings(b -> b.requiredReplicaSetName(mongoDBConfiguration.getString(REPLICA_SET)));
        }

        if (mongoDBConfiguration.getBoolean(SSL_ENABLED)) {
            logger.debug("SSL connections enabled for " + database);
            builder.applyToSslSettings(b -> b.enabled(true));
        }
        if (mongoDBConfiguration.getBoolean(SSL_INVALID_HOSTNAME_ALLOWED)) {
            logger.debug("SSL invalid hostnames allowed for " + database);
            builder.applyToSslSettings(b -> b.invalidHostNameAllowed(true));
        }
        if (mongoDBConfiguration.getBoolean(SSL_INVALID_CERTIFICATES_ALLOWED)) {
            logger.debug("SSL invalid certificates allowed for " + database);

            try {
                TrustManager[] trustAllCerts = new TrustManager[] {
                        new X509TrustManager() {
                            public X509Certificate[] getAcceptedIssuers() {
                                return new X509Certificate[0];
                            }

                            @Override
                            public void checkClientTrusted(X509Certificate[] certs, String authType) throws CertificateException {
                            }

                            @Override
                            public void checkServerTrusted(X509Certificate[] certs, String authType) throws CertificateException {
                            }
                        },
                };

                SSLContext sc = SSLContext.getInstance("SSL");
                sc.init(null, trustAllCerts, new SecureRandom());
                builder.applyToSslSettings(b -> b.context(sc));
            } catch (NoSuchAlgorithmException | KeyManagementException e) {
                throw new RuntimeException(e);
            }
        }

        assert (dataStoreServerAddresses != null);

        // We create the MongoCredential object
        String user = mongoDBConfiguration.getString(USERNAME, "");
        String pass = mongoDBConfiguration.getString(PASSWORD, "");
        MongoCredential mongoCredential = null;
        if ((user != null && !user.equals("")) || (pass != null && !pass.equals(""))) {
            String authMechanismStr = mongoDBConfiguration
                    .getString(AUTHENTICATION_MECHANISM, AuthenticationMechanism.SCRAM_SHA_1.toString());
            String authDatabase = mongoDBConfiguration.getString(AUTHENTICATION_DATABASE, "");

            logger.debug("Using " + AUTHENTICATION_MECHANISM + " " + AuthenticationMechanism.fromMechanismName(authMechanismStr));
            mongoCredential = MongoCredential.createCredential(user, authDatabase, pass.toCharArray())
                    .withMechanism(AuthenticationMechanism.fromMechanismName(authMechanismStr));
        }
        MongoClient mc = newMongoClient(builder, mongoCredential);
        MongoDatabase db = mc.getDatabase(database);

        logger.info("MongoDataStoreManager: MongoDataStore object for database: '" + database + "' created in "
                + stopWatch.getTime(TimeUnit.MILLISECONDS) + "ms");

        return new MongoDataStore(mc, db, mongoDBConfiguration);
    }

    public boolean exists(String database) {

        if (database != null && !database.trim().equals("")) {
            try (MongoClient mc = newMongoClient()) {
                MongoCursor<String> dbsCursor = mc.listDatabaseNames().iterator();
                while (dbsCursor.hasNext()) {
                    if (dbsCursor.next().equals(database)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    public void drop(String database) {
        if (database != null && !database.trim().equals("")) {
            if (mongoDataStores.containsKey(database)) {
                mongoDataStores.get(database).drop();
                // Do not close or remove from map
            }
        } else {
            logger.info("MongoDB database is null or empty");
        }
    }

    public void close(String database) {
        if (mongoDataStores.containsKey(database)) {
            mongoDataStores.get(database).close();
            mongoDataStores.remove(database);
        }
    }

    @Override
    public void close() {
        for (Entry<String, MongoDataStore> entry : mongoDataStores.entrySet()) {
            entry.getValue().close();
        }
        mongoDataStores.clear();
    }

    private MongoClient newMongoClient() {
        return new MongoClientImpl(MongoClientSettings.builder().build(), MongoDriverInformation.builder().build());
    }

    private MongoClient newMongoClient(MongoClientSettings.Builder builder, MongoCredential mongoCredential) {
        List<ServerAddress> serverAddressList;
        if (dataStoreServerAddresses.size() == 1) {
            serverAddressList = Collections.singletonList(new ServerAddress(dataStoreServerAddresses.get(0).getHost(),
                    dataStoreServerAddresses.get(0).getPort()));
        } else {
            serverAddressList = new ArrayList<>(dataStoreServerAddresses.size());
            for (DataStoreServerAddress serverAddress : dataStoreServerAddresses) {
                serverAddressList.add(new ServerAddress(serverAddress.getHost(), serverAddress.getPort()));
            }
        }

        builder.applyToClusterSettings(b -> b.hosts(serverAddressList));
        if (mongoCredential != null) {
            builder.credential(mongoCredential);
        }
        MongoClientSettings mongoClientSettings = builder.build();

        return MongoClients.create(mongoClientSettings);
    }

    /*
     * GETTERS AND SETTERS
     */

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
