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

package org.opencb.commons.datastore.core.config;

import org.opencb.commons.datastore.core.ObjectMap;

import java.util.Iterator;
import java.util.Map;

/**
 * Created by imedina on 23/03/14.
 */
@Deprecated
public class DataStoreConfiguration extends ObjectMap {

    public DataStoreConfiguration() {

    }

    public DataStoreConfiguration(int size) {
        super(size);
    }

    public DataStoreConfiguration(final String key, final Object value) {
        super(key, value);
    }

    public DataStoreConfiguration(final Map<String, Object> inputOptions) {
        super(inputOptions);
    }

    public DataStoreConfiguration(String json) {
        super(json);
    }


    public void addConfiguration(final DataStoreConfiguration dataStoreConfiguration) {
        Iterator<String> iter = dataStoreConfiguration.keySet().iterator();
        while (iter.hasNext()) {
            String next =  iter.next();
            this.put(next, dataStoreConfiguration.get(next));
        }
    }

    public void addConfiguration(final Map<String, Object> inputOptions) {
        Iterator<String> iter = inputOptions.keySet().iterator();
        while (iter.hasNext()) {
            String next =  iter.next();
            this.put(next, inputOptions.get(next));
        }
    }

    public void setConfiguration(final Map<String, Object> inputOptions) {
        this.clear();
        this.putAll(inputOptions);
    }

    @Override
    public String toString() {
        return this.toString();
    }
}
