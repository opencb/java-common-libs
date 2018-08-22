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

import java.util.Arrays;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * @author Jacobo Coll &lt;jacobo167@gmail.com&gt;
 */
public class Query extends ObjectMap {

    public Query() {
    }

    public Query(int size) {
        super(size);
    }

    public Query(String key, Object value) {
        super(key, value);
    }

    public Query(Map<String, Object> map) {
        super(map);
    }

    public Query(String json) {
        super(json);
    }

    public <E extends Enum<E> & QueryParam> void validate(Class<E> enumType)
            throws EnumConstantNotPresentException, NumberFormatException {
        Objects.requireNonNull(enumType);
        Map<String, E> enumFields = Arrays.asList(enumType.getEnumConstants()).stream().collect(
                Collectors.toMap(queryParam -> queryParam.key(), Function.<E>identity()));
        for (Map.Entry<String, Object> entry : entrySet()) {
            if (!enumFields.containsKey(entry.getKey())) {
                throw new EnumConstantNotPresentException(enumType, entry.getKey());
            } else {
                QueryParam queryParam = enumFields.get(entry.getKey());
                switch (queryParam.type()) {
                    case TEXT:
                        put(queryParam.key(), getString(queryParam.key()));
                        break;
                    case TEXT_ARRAY:
                        put(queryParam.key(), getAsStringList(queryParam.key()));
                        break;
                    case INTEGER:
                    case LONG:
                        put(queryParam.key(), getLong(queryParam.key()));
                        break;
                    case INTEGER_ARRAY:
                    case LONG_ARRAY:
                        put(queryParam.key(), getAsLongList(queryParam.key()));
                        break;
                    case DECIMAL:
                        put(queryParam.key(), getDouble(queryParam.key()));
                        break;
                    case DECIMAL_ARRAY:
                        put(queryParam.key(), getAsDoubleList(queryParam.key()));
                        break;
                    case BOOLEAN:
                        put(queryParam.key(), getBoolean(queryParam.key()));
                        break;
                    default:
                        break;
                }
            }
        }
    }

    @Override
    public Query append(String key, Object value) {
        return (Query) super.append(key, value);
    }
}
