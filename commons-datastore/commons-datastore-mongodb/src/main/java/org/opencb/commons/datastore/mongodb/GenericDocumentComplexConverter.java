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

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.bson.Document;
import org.opencb.commons.datastore.core.ComplexTypeConverter;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

/**
 * Created on 26/04/16.
 *
 * @author Jacobo Coll &lt;jacobo167@gmail.com&gt;
 */
public class GenericDocumentComplexConverter<T> implements ComplexTypeConverter<T, Document> {

    public static final String TO_REPLACE_DOTS = "&#46;";

    private final Class<T> clazz;
    private final ObjectMapper objectMapper;

    public GenericDocumentComplexConverter(Class<T> clazz) {
        this.clazz = clazz;

        objectMapper = new ObjectMapper();
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        objectMapper.configure(DeserializationFeature.FAIL_ON_NULL_FOR_PRIMITIVES, false);
        objectMapper.configure(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY, true);
    }

    public GenericDocumentComplexConverter(Class<T> clazz, ObjectMapper objectMapper) {
        this.clazz = clazz;
        this.objectMapper = objectMapper;
    }

    public ObjectMapper getObjectMapper() {
        return objectMapper;
    }

    @Override
    public T convertToDataModelType(Document document) {
        try {
            restoreDots(document);
            String json = objectMapper.writeValueAsString(document);
            return objectMapper.readValue(json, clazz);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    @Override
    public Document convertToStorageType(T object) {
        try {
            String json = objectMapper.writeValueAsString(object);
            Document document = Document.parse(json);
            replaceDots(document);
            return document;
        } catch (JsonProcessingException e) {
            throw new UncheckedIOException(e);
        }
    }

    /**
     * @deprecated This method should not be here, must be moved to a better class
     * @param document Document with the field
     * @param key Field name to be converted
     * @return Long converted value
     */
    @Deprecated
    public static Long getLongValue(Document document, String key) {
        try {
            return document.getLong(key);
        } catch (ClassCastException e) {
            return document.getInteger(key).longValue();
        }
    }

    /**
     * Replace all the dots in the keys with {@link #TO_REPLACE_DOTS}.
     *
     * MongoDB is not able to store dots in key fields.
     *
     * @param document      Document to modify
     * @return              Document modified
     */
    public static Document replaceDots(Document document) {
        return modifyKeys(document, key -> key.replace(".", TO_REPLACE_DOTS), ".");
    }

    /**
     * Restore all the dots in the keys where {@link #TO_REPLACE_DOTS} is found.
     * @param document      Document to modify
     * @return              Restored document
     */
    public static Document restoreDots(Document document) {
        return modifyKeys(document, key -> key.replace(TO_REPLACE_DOTS, "."), TO_REPLACE_DOTS);
    }


    /**
     * For each element in the document, applies a mapper {@link Function} to the key.
     *
     * Goes over all the elements in the document recursively.
     *
     * @param object        Object to modify
     * @param keyMapper     Key mapper
     * @param toReplace     String to be replaced
     * @param <T>           Type of the input object.
     * @return              The modified object.
     */
    protected static <T> T modifyKeys(T object, Function<String, String> keyMapper, String toReplace) {
        if (object instanceof Map) {
            Map<String, Object> document = (Map<String, Object>) object;
            List<String> keysWithDots = new LinkedList<>();
            for (Map.Entry<String, Object> entry : document.entrySet()) {
                if (entry.getKey().contains(toReplace)) {
                    keysWithDots.add(entry.getKey());
                }
                modifyKeys(entry.getValue(), keyMapper, toReplace);
            }
            for (String key : keysWithDots) {
                Object o = document.remove(key);
                document.put(keyMapper.apply(key), o);
            }
        } else if (object instanceof Collection) {
            Collection collection = (Collection) object;
            for (Object o : collection) {
                modifyKeys(o, keyMapper, toReplace);
            }
        }
        return object;
    }
}
