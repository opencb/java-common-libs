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

package org.opencb.commons.datastore.core;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Created by imedina on 20/03/14.
 */
public class QueryOptions extends ObjectMap {

    public static final String INCLUDE = "include";
    public static final String EXCLUDE = "exclude";
    public static final String SKIP = "skip";
    public static final String LIMIT = "limit";
    public static final String COUNT = "count";

    public static final String SORT = "sort";
    public static final String ORDER = "order";
    public static final String ASC = "asc";
    public static final String ASCENDING = "ascending";
    public static final String DESC = "desc";
    public static final String DESCENDING = "descending";

    public static final String TIMEOUT = "timeout";
    public static final String SKIP_COUNT = "skipCount";

    public static final String FACET = "facet";

    public QueryOptions() {

    }

    public QueryOptions(int size) {
        super(size);
    }

    public QueryOptions(final String key, final Object value) {
        super(key, value);
    }

    public QueryOptions(final Map<String, Object> inputOptions) {
        super(inputOptions);
    }

    public QueryOptions(final Map<String, ?> inputOptions, boolean pickFirstValue) {
        if (pickFirstValue) {
            for (Entry<String, ?> option : inputOptions.entrySet()) {
                if (option.getValue() instanceof List) {
                    this.put(option.getKey(), ((List) option.getValue()).get(0));
                }
            }
        } else {
            this.putAll(inputOptions);
        }
    }

    public QueryOptions(String json) {
        super(json);
    }


    /**
     * This method safely add new options. If the key already exists it does not overwrite the current value.
     * You can use put for overwritten the value.
     *
     * @param key The new option name
     * @param value The new value
     * @return null if the key was not present, or the existing object if the key exists.
     */
    public Object add(String key, Object value) {
        if (!this.containsKey(key)) {
            this.put(key, value);
            return null;
        }
        return this.get(key);
    }

    /**
     * This method safely add a new Object to an exiting option which type is List.
     *
     * @param key The new option name
     * @param value The new value
     * @return the list with the new Object inserted.
     */
    public Object addToListOption(String key, Object value) {
        if (key != null && !key.equals("")) {
            if (this.containsKey(key) && this.get(key) != null) {
                if (!(this.get(key) instanceof List)) { //If was not a list, getAsList returns an Unmodifiable List.
                    // Create new modifiable List with the content, and replace.
                    this.put(key, new ArrayList<>(this.getAsList(key)));
                }
                try {
                    this.getList(key).add(value);
                } catch (UnsupportedOperationException e) {
                    List<Object> list = new ArrayList<>(this.getList(key));
                    list.add(value);
                    this.put(key, list);
                }
            } else {
                //New List instead of "Arrays.asList" or "Collections.singletonList" to avoid unmodifiable list.
                List<Object> list = new ArrayList<>();
                list.add(value);
                this.put(key, list);
            }
            return this.getList(key);
        }
        return null;
    }

    @Override
    public QueryOptions append(String key, Object value) {
        return (QueryOptions) super.append(key, value);
    }

    public static QueryOptions empty() {
        return new QueryOptions();
    }
}
