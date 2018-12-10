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

import org.opencb.commons.datastore.core.DataStoreServerAddress;
import org.opencb.commons.datastore.core.ObjectMap;

import java.util.*;

/**
 * Created by imedina on 22/03/14.
 */
public class MongoDBConfiguration extends ObjectMap {

    public static final String SERVER_ADDRESS = "serverAddress";
    public static final String USERNAME = "username";
    public static final String PASSWORD = "password";
    public static final String AUTHENTICATION_DATABASE = "authenticationDatabase";
    public static final String REPLICA_SET = "replicaSet";
    public static final String READ_PREFERENCE = "readPreference";
    public static final ReadPreference READ_PREFERENCE_DEFAULT = ReadPreference.PRIMARY;
    public static final String CONNECT_TIMEOUT = "connectTimeout";
    public static final int CONNECT_TIMEOUT_DEFAULT = 10000;
    public static final String SOCKET_TIMEOUT = "socketTimeout";
    public static final int SOCKET_TIMEOUT_DEFAULT = 10000;
    public static final String CONNECTIONS_PER_HOST = "connectionsPerHost";
    public static final int CONNECTIONS_PER_HOST_DEFAULT = 20;
    public static final String SSL_ENABLED = "sslEnabled";
    public static final Boolean SSL_ENABLED_DEFAULT = false;

    public enum ReadPreference {
        PRIMARY("primary"),
        PRIMARY_PREFERRED("primaryPreferred"),
        SECONDARY("secondary"),
        SECONDARY_PREFERRED("secondaryPreferred"),
        NEAREST("nearest");

        private String value;

        ReadPreference(String value) {
            this.value = value;
        }

        public String getValue() {
            return value;
        }
    }

    MongoDBConfiguration(final Map<String, Object> inputOptions) {
        super(inputOptions);
        this.putAll(inputOptions);
    }

    /**
     * A builder for MongoDBConfiguration so that MongoDBConfiguration can be immutable, and to support easier
     * construction through chaining.
     */
    public static class Builder {
        private ObjectMap optionsMap;
        private List<DataStoreServerAddress> serverAddresses;

        public Builder() {
            optionsMap = new ObjectMap();
            serverAddresses = new LinkedList<>();
            optionsMap.put(SERVER_ADDRESS, serverAddresses);
        }

        public Builder init() {
            serverAddresses.add(new DataStoreServerAddress("localhost", 27017));
            optionsMap.put(SERVER_ADDRESS, serverAddresses);
            optionsMap.put(CONNECTIONS_PER_HOST, CONNECTIONS_PER_HOST_DEFAULT);
            optionsMap.put(CONNECT_TIMEOUT, CONNECT_TIMEOUT_DEFAULT);
            optionsMap.put(SOCKET_TIMEOUT, SOCKET_TIMEOUT_DEFAULT);
            return this;
        }

        public Builder load(final Map<? extends String, ?> inputOptions) {
            optionsMap.putAll(inputOptions);
            return this;
        }

        public Builder add(String key, Object value) {
            optionsMap.put(key, value);
            return this;
        }

        public Builder setUserPassword(String username, String password) {
            optionsMap.put(USERNAME, username);
            optionsMap.put(PASSWORD, password);
            return this;
        }

        public Builder setServerAddress(List<DataStoreServerAddress> dataStoreServerAddresses) {
            serverAddresses.clear();
            serverAddresses.addAll(dataStoreServerAddresses);
            return this;
        }

        public Builder addServerAddress(DataStoreServerAddress dataStoreServerAddress) {
            serverAddresses.add(dataStoreServerAddress);
            return this;
        }

        public Builder setReadPreference(ReadPreference readPreference) {
            optionsMap.put(READ_PREFERENCE, readPreference.getValue());
            return this;
        }

        public Builder setReplicaSet(String replicaSet) {
            optionsMap.put(REPLICA_SET, replicaSet);
            return this;
        }

        public Builder setAuthenticationDatabase(String authenticationDatabase) {
            optionsMap.put(AUTHENTICATION_DATABASE, authenticationDatabase);
            return this;
        }

        public Builder setConnectionsPerHost(int connectionsPerHost) {
            optionsMap.put(CONNECTIONS_PER_HOST, connectionsPerHost);
            return this;
        }

        public Builder setConnectTimeout(int timeout) {
            optionsMap.put(CONNECT_TIMEOUT, timeout);
            return this;
        }

        public Builder setSocketTimeout(int timeout) {
            optionsMap.put(SOCKET_TIMEOUT, timeout);
            return this;
        }

        public Builder setSslEnabled(Boolean enabled) {
            optionsMap.put(SSL_ENABLED, enabled);
            return this;
        }

        public MongoDBConfiguration build() {
            return new MongoDBConfiguration(optionsMap);
        }

    }

    /**
     * Create a new Native instance.  This is a convenience method, equivalent to {@code new MongoClientOptions.Native()}.
     *
     * @return a new instance of a Native
     */
    public static Builder builder() {
        return new Builder();
    }

//    public void add(final String key, final Object value) {
//        if(dataStoreConfiguration == null) {
//            dataStoreConfiguration = new DataStoreConfiguration();
//        }
//        dataStoreConfiguration.put(key, value);
//    }
//
//    public void remove(final String key) {
//        if(dataStoreConfiguration != null) {
//            dataStoreConfiguration.remove(key);
//        }
//    }
//
//    public void addConfiguration(final Map<String, Object> inputOptions) {
//        if(dataStoreConfiguration == null) {
//            dataStoreConfiguration = new DataStoreConfiguration();
//        }
//        Iterator<String> iter = inputOptions.keySet().iterator();
//        while (iter.hasNext()) {
//            String next =  iter.next();
//            dataStoreConfiguration.put(next, inputOptions.get(next));
//        }
//    }
//
//    public void setConfiguration(final Map<String, Object> inputOptions) {
//        dataStoreConfiguration = new DataStoreConfiguration(inputOptions);
//    }
//
//    @Override
//    public String toString() {
//        return dataStoreConfiguration.toString();
//    }


}
