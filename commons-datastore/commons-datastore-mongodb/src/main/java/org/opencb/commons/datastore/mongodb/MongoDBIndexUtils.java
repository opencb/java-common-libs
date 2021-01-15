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
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Creates and validates indexes in specifed Mongo DB instance.
 */
public class MongoDBIndexUtils {

    private MongoDataStore mongoDataStore;
    private Path indexFile;

    public MongoDBIndexUtils(MongoDataStore mongoDataStore, Path indexFile) {
        this.mongoDataStore = mongoDataStore;
        this.indexFile = indexFile;

        if (mongoDataStore == null) {
            throw new MongoException("Unable to connect to MongoDB");
        }
    }

    /**
     * Create indexes in a given database.
     * @param dropIndexesFirst if TRUE, deletes existing indexes before creating new ones. defaults to FALSE, indexes
     *                         will not be recreated if they already exist
     * @throws IOException if index file can't be read
     */
    public void createAllIndexes(boolean dropIndexesFirst) throws IOException {
        Map<String, List<Map<String, ObjectMap>>> indexes = getIndexesFromFile();
        for (String collectionName : indexes.keySet()) {
            createIndexes(collectionName, indexes.get(collectionName), dropIndexesFirst);
        }
    }

    private Map<String, List<Map<String, ObjectMap>>> getIndexesFromFile() throws IOException {
        ObjectMapper objectMapper = generateDefaultObjectMapper();
        Map<String, List<Map<String, ObjectMap>>> indexes = new HashMap<>();
        try (BufferedReader bufferedReader = Files.newBufferedReader(indexFile)) {
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

    public void createIndexes(String collectionName, boolean dropIndexesFirst) throws IOException {
        Map<String, List<Map<String, ObjectMap>>> indexesFromFile = getIndexesFromFile();
        createIndexes(collectionName, indexesFromFile.get(collectionName), dropIndexesFirst);
    }

    public void createIndexes(String collectionName, List<Map<String, ObjectMap>> indexes, boolean dropIndexesFirst) {
        MongoDBCollection mongoDBCollection = mongoDataStore.getCollection(collectionName);

        DataResult<Document> index = mongoDBCollection.getIndex();
        Set<String> existingIndexes = index.getResults()
                .stream()
                .map(document -> (String) document.get("name"))
                .collect(Collectors.toSet());

        if (!dropIndexesFirst) {
            // It is + 1 because mongo always create the _id index by default
            if (index.getNumResults() == indexes.size() + 1) {
                // We already have the indexes we need, nothing to do here.
                return;
            }
        } else {
            mongoDBCollection.dropIndexes();
        }

        for (Map<String, ObjectMap> indexFromFile : indexes) {
            StringBuilder indexName = new StringBuilder();
            Document keys = new Document();
            for (Map.Entry<String, Object> pair : indexFromFile.get("fields").entrySet()) {
                keys.append(pair.getKey(), pair.getValue());

                if (indexName.length() > 0) {
                    indexName.append("_");
                }
                indexName.append(pair.getKey()).append("_").append(pair.getValue());
            }

            if (dropIndexesFirst || !existingIndexes.contains(indexName.toString())) {
                mongoDBCollection.createIndex(keys, new ObjectMap(indexFromFile.get("options")));
            }
        }
    }

    private ObjectMapper generateDefaultObjectMapper() {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.configure(MapperFeature.REQUIRE_SETTERS_FOR_GETTERS, true);
        objectMapper.configure(DeserializationFeature.FAIL_ON_IGNORED_PROPERTIES, false);
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        objectMapper.configure(DeserializationFeature.FAIL_ON_NULL_FOR_PRIMITIVES, false);
        objectMapper.configure(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY, true);
        return objectMapper;
    }

    /**
     * Validate all indexes in a given database.
     *
     * @throws IOException if index file can't be read
     */
    public void validateAllIndexes() throws IOException {
        Map<String, List<Map<String, ObjectMap>>> indexes = getIndexesFromFile();
        for (String collectionName : indexes.keySet()) {
            validateIndexes(collectionName, indexes.get(collectionName));
        }
    }

    public void validateIndexes(String collectionName) throws IOException {
        Map<String, List<Map<String, ObjectMap>>> indexes = getIndexesFromFile();
        validateIndexes(collectionName, indexes.get(collectionName));
    }

    private void validateIndexes(String collectionName, List<Map<String, ObjectMap>> indexesFromFile) {
        MongoDBCollection mongoDBCollection = mongoDataStore.getCollection(collectionName);
        DataResult<Document> index = mongoDBCollection.getIndex();
        Set<String> existingIndexes = index.getResults()
                .stream()
                .map(document -> (String) document.get("name"))
                .collect(Collectors.toSet());


        for (Map<String, ObjectMap> indexFromFile : indexesFromFile) {
            StringBuilder indexName = new StringBuilder();
            Document keys = new Document();
            for (Map.Entry<String, Object> pair : indexFromFile.get("fields").entrySet()) {
                keys.append(pair.getKey(), pair.getValue());

                if (indexName.length() > 0) {
                    indexName.append("_");
                }
                indexName.append(pair.getKey()).append("_").append(pair.getValue());
            }
            if (!existingIndexes.contains(indexName.toString())) {
                System.out.println("ERROR: " + indexName.toString() + " not found");
            } else {
                System.out.println("OK: " + indexName.toString() + " exists");
            }
        }
    }
}
