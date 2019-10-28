package org.opencb.commons.datastore.mongodb;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mongodb.MongoClient;
import com.mongodb.MongoException;
import com.mongodb.client.MongoDatabase;
import org.bson.Document;
import org.opencb.commons.datastore.core.DataResult;
import org.opencb.commons.datastore.core.ObjectMap;

import java.io.*;
import java.util.*;
import java.util.stream.Collectors;

public class MongoDBIndexUtils {

    public void createIndexes(MongoClient mongoClient, MongoDatabase db, MongoDBConfiguration mongoDBConfiguration,
                              InputStream resourceAsStream) {

        MongoDataStore mongoDataStore = new MongoDataStore(mongoClient, db, mongoDBConfiguration);
        if (mongoDataStore == null) {
            throw new MongoException("Unable to connect to MongoDB");
        }
        ObjectMapper objectMapper = generateDefaultObjectMapper();
        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(resourceAsStream));
        // We store all the indexes that are in the file in the indexes object
        Map<String, List<Map<String, ObjectMap>>> indexes = new HashMap<>();
        bufferedReader.lines().filter(s -> !s.trim().isEmpty()).forEach(s -> {
            try {
                HashMap hashMap = objectMapper.readValue(s, HashMap.class);

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
        try {
            bufferedReader.close();
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }

        for (String collectionName : indexes.keySet()) {
            MongoDBCollection mongoDBCollection = mongoDataStore.getCollection(collectionName);
            createIndexes(mongoDBCollection, indexes.get(collectionName));
        }
    }

    private void createIndexes(MongoDBCollection mongoCollection, List<Map<String, ObjectMap>> indexes) {
        DataResult<Document> index = mongoCollection.getIndex();
        // We store the existing indexes
        Set<String> existingIndexes = index.getResults()
                .stream()
                .map(document -> (String) document.get("name"))
                .collect(Collectors.toSet());

        if (index.getNumResults() != indexes.size() + 1) { // It is + 1 because mongo always create the _id index by default
            for (Map<String, ObjectMap> userIndex : indexes) {
                String indexName = "";
                Document keys = new Document();
                Iterator fieldsIterator = userIndex.get("fields").entrySet().iterator();
                while (fieldsIterator.hasNext()) {
                    Map.Entry pair = (Map.Entry) fieldsIterator.next();
                    keys.append((String) pair.getKey(), pair.getValue());

                    if (!indexName.isEmpty()) {
                        indexName += "_";
                    }
                    indexName += pair.getKey() + "_" + pair.getValue();
                }

                if (!existingIndexes.contains(indexName)) {
                    mongoCollection.createIndex(keys, new ObjectMap(userIndex.get("options")));
                }
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
