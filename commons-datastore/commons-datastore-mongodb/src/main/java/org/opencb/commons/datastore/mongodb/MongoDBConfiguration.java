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

import org.opencb.commons.datastore.core.DataStoreServerAddress;
import org.opencb.commons.datastore.core.ObjectMap;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Created by imedina on 22/03/14.
 */
public class MongoDBConfiguration extends ObjectMap {

//    private MongoDBConfiguration() {
//        super();
////        initConfiguration();
//    }

//    private MongoDBConfiguration(final String key, final Object value) {
//        super();
////        initConfiguration();
//        this.put(key, value);
//    }

    public enum ReadPreference {
        PRIMARY("primary"),
        PRIMARY_PREFERRED("primaryPreferred"),
        SECONDARY("secondary"),
        SECONDARY_PREFERRED("secondaryPreferred"),
        NEAREST("nearest");

        private String value;

        private ReadPreference(String value) {
            this.value = value;
        }

        public String getValue() {
            return value;
        }
    }

    ;

    MongoDBConfiguration(final Map<String, Object> inputOptions) {
        super(inputOptions);
        this.putAll(inputOptions);
    }

//    private void initConfiguration() {
//        this.put("serverAddress", Arrays.asList(new DataStoreServerAddress("localhost", 27017)));
//        this.put("connectionsPerHost", 20);
//        this.put("connectTimeout", 10000);
//        this.put("socketTimeout", 10000);
//
//    }

    /**
     * A builder for MongoDBConfiguration so that MongoDBConfiguration can be immutable, and to support easier
     * construction through chaining.
     */
    public static class Builder {
        private Map<String, Object> optionsMap;

        public Builder() {
            optionsMap = new LinkedHashMap<>();
        }

        public Builder init() {
            optionsMap.put("serverAddress", Arrays.asList(new DataStoreServerAddress("localhost", 27017)));
            optionsMap.put("connectionsPerHost", 20);
            optionsMap.put("connectTimeout", 10000);
            optionsMap.put("socketTimeout", 10000);
            return this;
        }

        public Builder load(final Map<String, Object> inputOptions) {
            optionsMap.putAll(inputOptions);
            return this;
        }

        public Builder add(String key, Object value) {
            optionsMap.put(key, value);
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
