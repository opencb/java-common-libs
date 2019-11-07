package org.opencb.commons.datastore.mongodb;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mongodb.MongoException;
import org.bson.Document;
import org.opencb.commons.datastore.core.DataResult;
import org.opencb.commons.datastore.core.ObjectMap;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.Collectors;

public class MongoDBIndexUtils {

    /**
     * Create indexes in a given database.
     * @param mongoDataStore Database name
     * @param indexFile Input stream with the index information
     * @param dropIndexesFirst if TRUE, deletes existing indexes before creating new ones. defaults to FALSE, indexes
     *                         will not be recreated if they already exist
     * @throws IOException if index file can't be read
     */
    public static void createAllIndexes(MongoDataStore mongoDataStore, Path indexFile, boolean dropIndexesFirst) throws IOException {
        if (mongoDataStore == null) {
            throw new MongoException("Unable to connect to MongoDB");
        }
        InputStream inputStream = Files.newInputStream(indexFile);
        createAllIndexes(mongoDataStore, inputStream, dropIndexesFirst);
        inputStream.close();
    }

    /**
     * Create indexes in a given database.
     * @param mongoDataStore Database name
     * @param resourceAsStream Input stream with the index information
     * @param dropIndexesFirst if TRUE, deletes existing indexes before creating new ones. defaults to FALSE, indexes
     *                         will not be recreated if they already exist
     * @throws IOException if index file can't be read
     */
    public static void createAllIndexes(MongoDataStore mongoDataStore, InputStream resourceAsStream, boolean dropIndexesFirst)
            throws IOException {
        if (mongoDataStore == null) {
            throw new MongoException("Unable to connect to MongoDB");
        }
        Map<String, List<Map<String, ObjectMap>>> indexes = getIndexes(resourceAsStream);
        for (String collectionName : indexes.keySet()) {
            MongoDBCollection mongoDBCollection = mongoDataStore.getCollection(collectionName);
            createIndexes(mongoDBCollection, indexes.get(collectionName), dropIndexesFirst);
        }
    }

    /**
     * Create indexes for a specific collection in a given database.
     * @param mongoDataStore Database name
     * @param resourceAsStream Input stream with the index information
     * @param collectionName Name of collection to index
     * @param dropIndexesFirst if TRUE, deletes existing indexes before creating new ones. defaults to FALSE, indexes
     *                         will not be recreated if they already exist
     * @throws IOException if index file can't be read
     */
    public static void createIndexes(MongoDataStore mongoDataStore, InputStream resourceAsStream, String collectionName,
                                     boolean dropIndexesFirst) throws IOException {
        if (mongoDataStore == null) {
            throw new MongoException("Unable to connect to MongoDB");
        }
        Map<String, List<Map<String, ObjectMap>>> indexes = getIndexes(resourceAsStream);
        MongoDBCollection mongoDBCollection = mongoDataStore.getCollection(collectionName);
        createIndexes(mongoDBCollection, indexes.get(collectionName), dropIndexesFirst);
    }

    private static Map<String, List<Map<String, ObjectMap>>> getIndexes(InputStream resourceAsStream)
            throws IOException {
        ObjectMapper objectMapper = generateDefaultObjectMapper();
        Map<String, List<Map<String, ObjectMap>>> indexes = new HashMap<>();
        try (BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(resourceAsStream))) {
            bufferedReader.lines()
                    .filter(line -> !line.trim().isEmpty())
                    .forEach(line -> {
                        try {
                            HashMap hashMap = objectMapper.readValue(line, HashMap.class);
                            String collection = (String) hashMap.get("collection");
                            if (!indexes.containsKey(collection)) {
                                indexes.put(collection, new ArrayList<>());
                            }
                            Map<String, ObjectMap> myIndexes = new HashMap<>();
                            myIndexes.put("fields", new ObjectMap((Map) hashMap.get("fields")));
                            myIndexes.put("options", new ObjectMap((Map) hashMap.getOrDefault("options", Collections.emptyMap())));
                            indexes.get(collection).add(myIndexes);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    });
        }
        return indexes;
    }

    private static void createIndexes(MongoDBCollection mongoCollection, List<Map<String, ObjectMap>> indexes,
                                      boolean dropIndexesFirst) {

        Set<String> existingIndexes = null;
        if (!dropIndexesFirst) {
            DataResult<Document> index = mongoCollection.getIndex();
            existingIndexes = index.getResults()
                    .stream()
                    .map(document -> (String) document.get("name"))
                    .collect(Collectors.toSet());

            // It is + 1 because mongo always create the _id index by default
            if (index.getNumResults() == indexes.size() + 1) {
                // we already have the indexes we need, nothing to do here.
                return;
            }
        }

        for (Map<String, ObjectMap> userIndex : indexes) {
            StringBuilder indexName = new StringBuilder();
            Document keys = new Document();
            Iterator<Map.Entry<String, Object>> fieldsIterator = userIndex.get("fields").entrySet().iterator();
            while (fieldsIterator.hasNext()) {
                Map.Entry<String, Object> pair = fieldsIterator.next();
                keys.append(pair.getKey(), pair.getValue());

                if (indexName.length() > 0) {
                    indexName.append("_");
                }
                indexName.append(pair.getKey()).append("_").append(pair.getValue());
            }

            if (dropIndexesFirst || !existingIndexes.contains(indexName.toString())) {
                mongoCollection.createIndex(keys, new ObjectMap(userIndex.get("options")));
            }
        }
    }

    private static ObjectMapper generateDefaultObjectMapper() {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.configure(MapperFeature.REQUIRE_SETTERS_FOR_GETTERS, true);
        objectMapper.configure(DeserializationFeature.FAIL_ON_IGNORED_PROPERTIES, false);
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        objectMapper.configure(DeserializationFeature.FAIL_ON_NULL_FOR_PRIMITIVES, false);
        objectMapper.configure(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY, true);
        return objectMapper;
    }
}
