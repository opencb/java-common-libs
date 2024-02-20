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

import com.mongodb.BasicDBObject;
import com.mongodb.MongoBulkWriteException;
import com.mongodb.MongoWriteException;
import com.mongodb.client.model.Filters;
import org.bson.Document;
import org.bson.conversions.Bson;
import org.hamcrest.CoreMatchers;
import org.junit.*;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.Test;
import org.junit.rules.ExpectedException;
import org.opencb.commons.datastore.core.DataResult;
import org.opencb.commons.datastore.core.ObjectMap;
import org.opencb.commons.datastore.core.QueryOptions;
import org.opencb.commons.datastore.core.QueryResultWriter;

import java.io.DataOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.*;

import static org.junit.Assert.*;

/**
 * Created by imedina on 29/03/14.
 */
public class MongoDBCollectionTest {

    private static MongoDataStoreManager mongoDataStoreManager;
    private static MongoDataStore mongoDataStore;
    private static MongoDBCollection mongoDBCollection;
    private static MongoDBCollection mongoDBCollectionInsertTest;
    private static MongoDBCollection mongoDBCollectionUpdateTest;
    private static MongoDBCollection mongoDBCollectionRemoveTest;

    private static int N = 1000;

    @Rule
    public ExpectedException thrown = ExpectedException.none();
    public static final List<String> NAMES = Arrays.asList("John", "Jack", "Javi");
    public static final List<String> SURNAMES = Arrays.asList("Doe", "Davis", null);

    @BeforeAll
    public static void beforeClass() throws Exception {
        mongoDataStoreManager = new MongoDataStoreManager("localhost", 27017);

        mongoDataStoreManager.get("datastore_test");
        mongoDataStoreManager.drop("datastore_test");
        mongoDataStore = mongoDataStoreManager.get("datastore_test");

        mongoDBCollection = createTestCollection("test", N);
        mongoDBCollectionInsertTest = createTestCollection("insert_test", 50);
        mongoDBCollectionUpdateTest = createTestCollection("update_test", 50);
        mongoDBCollectionRemoveTest = createTestCollection("remove_test", 50);
    }

    @BeforeEach
    public void setUp() throws Exception {


    }

    @AfterAll
    public static void afterClass() throws Exception {
//        mongoDataStoreManager.drop("datastore_test");
        mongoDataStore.close();
    }

    public static class User {
        public long id;
        public String name;
        public String surname;
        public int age;
        public int number;

        @Override
        public String toString() {
            return "User{"
                    + "id:" + id
                    + ", name:\"" + name + '"'
                    + ", surname:\"" + surname + '"'
                    + ", age:" + age
                    + ", number:" + number
                    + '}';
        }
    }

    private static MongoDBCollection createTestCollection(String test, int size) {
        MongoDBCollection mongoDBCollection = mongoDataStore.getCollection(test);
        Document document;
        Random random = new Random();
        for (long i = 0; i < size; i++) {
            document = new Document("id", i);
            document.put("name", NAMES.get(random.nextInt(NAMES.size())));
            document.put("surname", SURNAMES.get(random.nextInt(SURNAMES.size())));
            document.put("age", (int) i % 5);
            document.put("number", (int) i * i);
            mongoDBCollection.nativeQuery().insert(document, null);
        }
        return mongoDBCollection;
    }

    @Test
    public void testQueryResultWriter() throws Exception {

        MongoDBCollection mongoDBCollection = createTestCollection("testQueryResultWriter", N);
        for (int i = 0; i < 100; i++) {
            mongoDBCollection.insert(new Document("id", i), null);
        }

        BasicQueryResultWriter queryResultWriter = new BasicQueryResultWriter();
        mongoDBCollection.setQueryResultWriter(queryResultWriter);
        DataResult<Document> dbObjectQueryResult = mongoDBCollection.find(new Document("id", new Document("$gt", 50)), null);
        System.out.println(dbObjectQueryResult);
        assert (dbObjectQueryResult.getResults().isEmpty());

        mongoDBCollection.setQueryResultWriter(null);
        dbObjectQueryResult = mongoDBCollection.find(new Document("id", new Document("$gt", 50)), null);
        System.out.println(dbObjectQueryResult);
        assert (!dbObjectQueryResult.getResults().isEmpty());

    }

    @Test
    public void testDistinct() throws Exception {
        DataResult<Long> id1 = mongoDBCollection.distinct("id", null, Long.class);
//        DataResult<Integer> id2 = mongoDBCollection.distinct("id", null, Object.class, new ComplexTypeConverter<Object, Integer>() {
//            @Override
//            public Integer convertToStorageType(Object object) {
//                if (object instanceof Integer) {
//                    return (Integer) object;
//                } else {
//                    System.out.println("Non integer result : " + object);
//                    return 0;
//                }
//            }
//
//            @Override
//            public Object convertToDataModelType(Integer object) {
//                return null;
//            }
//        });
//        System.out.println(mongoDBCollection.distinct("name", null).getNumResults());
//        System.out.println(mongoDBCollection.nativeQuery().distinct("name"));
    }

    @Test
    public void testSortOrder() throws Exception {
        Document query = new Document();
        QueryOptions queryOptions = new QueryOptions(QueryOptions.LIMIT, 10).append(QueryOptions.SORT, "number")
                .append(QueryOptions.ORDER, "asc");
        List<Document> result = mongoDBCollection.find(query, queryOptions).getResults();
        assertEquals(0L, result.get(0).get("number"));
    }

    @Test
    public void testMultipleSortOrder() throws Exception {
        Document query = new Document();
        QueryOptions queryOptions = new QueryOptions(QueryOptions.LIMIT, 500)
                .append(QueryOptions.SORT, Arrays.asList("age:ASC", "number:DESC"))
                .append(QueryOptions.ORDER, "asc");
        int age = 0;
        long number = Long.MAX_VALUE;
        List<Document> result = mongoDBCollection.find(query, queryOptions).getResults();
        for (Document document : result) {
            if (age < document.getInteger("age")) {
                number = Long.MAX_VALUE;
            }

            assertTrue(age <= document.getInteger("age"));
            assertTrue(number >= document.getLong("number"));

            age = document.getInteger("age");
            number = document.getLong("number");
        }
    }

    @Test
    public void testCount() throws Exception {
        DataResult<Long> queryResult = mongoDBCollection.count();
        assertEquals("The number of documents must be equals", N, queryResult.getNumMatches());
    }

    @Test
    public void testCount1() throws Exception {
        DataResult<Long> queryResult = mongoDBCollection.count();
        assertEquals("The number must be equals", N, queryResult.getNumMatches());
    }

    @Test
    public void testDistinct1() throws Exception {
        DataResult<Integer> queryResult = mongoDBCollection.distinct("age", null, Integer.class);
        assertNotNull("Object cannot be null", queryResult);
        assertEquals("ResultType must be 'java.lang.Integer'", "java.lang.Integer", queryResult.getResultType());
    }

    @Test
    public void testDistinct2() throws Exception {
        DataResult<String> queryResult = mongoDBCollection.distinct("name", null, String.class);
        assertNotNull("Object cannot be null", queryResult);
        assertEquals("ResultType must be 'java.lang.String'", String.class.getName(), queryResult.getResultType());
    }

    @Test
    public void testDistinct3() throws Exception {
        DataResult<String> queryResult = mongoDBCollection.distinct("surename", null, String.class);
        assertNotNull("Object cannot be null", queryResult);
        assertEquals("ResultType must be 'java.lang.String'", String.class.getName(), queryResult.getResultType());
    }

//    @Test
//    public void testDistinct3() throws Exception {
//        DataResult<Integer> queryResult = mongoDBCollection.distinct("age", null, new ComplexTypeConverter<Integer, Object>() {
//            @Override
//            public Integer convertToDataModelType(Object object) {
//                return Integer.parseInt(object.toString());
//            }
//
//            @Override
//            public Object convertToStorageType(Integer object) {
//                return null;
//            }
//        });
//        assertNotNull("Object cannot be null", queryResult);
//        assertEquals("ResultType must be 'java.lang.Integer'", "java.lang.Integer", queryResult.getResultType());
//    }

    @Test
    public void testFind() throws Exception {
        Document dbObject = new Document("id", 4);
        QueryOptions queryOptions = new QueryOptions("include", Arrays.asList("id"));
        DataResult<Document> queryResult = mongoDBCollection.find(dbObject, queryOptions);
        assertNotNull("Object cannot be null", queryResult.getResults());
        assertEquals("Returned Id does not match", 4L, queryResult.first().get("id"));
//        System.out.println("queryResult 'include' = " + queryResult.toString());
    }

    @Test
    public void testFind1() throws Exception {
        Document dbObject = new Document("id", 4);
        Document returnFields = new Document("id", 1);
        QueryOptions queryOptions = new QueryOptions("exclude", Arrays.asList("id"));
        DataResult<Document> queryResult = mongoDBCollection.find(dbObject, returnFields, queryOptions);
        assertNotNull("Object cannot be null", queryResult.getResults());
        assertNull("Field 'name' must not exist", queryResult.first().get("name"));
//        System.out.println("queryResult 'projection' = " + queryResult);
    }

    @Test
    public void testFind2() throws Exception {
        Document dbObject = new Document("id", 4);
        Document returnFields = new Document("id", 1);
        QueryOptions queryOptions = new QueryOptions("exclude", Arrays.asList("id"));
        DataResult<HashMap> queryResult = mongoDBCollection.find(dbObject, returnFields, HashMap.class, queryOptions);
        assertNotNull("Object cannot be null", queryResult.getResults());
        assertTrue("Returned field must instance of Hashmap", queryResult.first() instanceof HashMap);
    }

//    @Test
//    public void testFind3() throws Exception {
//        final Document dbObject = new Document("id", 4);
//        Document returnFields = new Document("id", 1);
//        QueryOptions queryOptions = new QueryOptions("exclude", Arrays.asList("id"));
//        DataResult<HashMap> queryResult = mongoDBCollection.find(dbObject, returnFields,
//                new ComplexTypeConverter<HashMap, Object>() {
//                    @Override
//                    public HashMap convertToDataModelType(Object object) {
//                        return new HashMap(dbObject.toMap());
//                    }
//
//                    @Override
//                    public DBObject convertToStorageType(HashMap object) {
//                        return null;
//                    }
//                }, queryOptions);
//        assertNotNull("Object cannot be null", queryResult.getResults());
//        assertTrue("Returned field must instance of Hashmap", queryResult.first() instanceof HashMap);
//    }

    @Test
    public void testFind4() throws Exception {
        List<Bson> dbObjectList = new ArrayList<>(10);
        for (int i = 0; i < 10; i++) {
//            dbObjectList.add(new Document("id", i));
            dbObjectList.add(Filters.eq("id", i));
        }

        QueryOptions queryOptions = new QueryOptions("include", Arrays.asList("id"));
        List<DataResult<Document>> queryResultList = mongoDBCollection.find(dbObjectList, queryOptions);
        assertEquals("List must contain 10 results", 10, queryResultList.size());
        assertNotNull("Object cannot be null", queryResultList.get(0).getResults());
        assertEquals("Returned Id does not match", 9L, queryResultList.get(9).first().get("id"));
    }

    @Test
    public void testFind5() throws Exception {
        List<Bson> dbObjectList = new ArrayList<>(10);
        for (int i = 0; i < 10; i++) {
//            dbObjectList.add(new Document("id", i));
            dbObjectList.add(Filters.eq("id", i));
        }
        Document returnFields = new Document("id", 1);
        QueryOptions queryOptions = new QueryOptions("exclude", Arrays.asList("id"));
        List<DataResult<Document>> queryResultList = mongoDBCollection.find(dbObjectList, returnFields, queryOptions);
        assertEquals("List must contain 10 results", 10, queryResultList.size());
        assertNotNull("Object cannot be null", queryResultList.get(0).getResults());
        assertNull("Field 'name' must not exist", queryResultList.get(0).first().get("name"));
        assertEquals("resultType must be 'org.bson.Document'", "org.bson.Document", queryResultList.get(0).getResultType());
    }

    @Test
    public void testFind6() throws Exception {
        List<Bson> dbObjectList = new ArrayList<>(10);
        for (int i = 0; i < 10; i++) {
//            dbObjectList.add(new Document("id", i));
            dbObjectList.add(Filters.eq("id", i));
        }
        Document returnFields = new Document("id", 1);
        QueryOptions queryOptions = new QueryOptions("exclude", Arrays.asList("id"));
        List<DataResult<HashMap>> queryResultList = mongoDBCollection.find(dbObjectList, returnFields, HashMap.class, queryOptions);
        assertNotNull("Object queryResultList cannot be null", queryResultList);
        assertNotNull("Object queryResultList.get(0) cannot be null", queryResultList.get(0).getResults());
        assertTrue("Returned field must instance of HashMap", queryResultList.get(0).first() instanceof HashMap);
        assertEquals("resultType must 'java.util.HashMap'", "java.util.HashMap", queryResultList.get(0).getResultType());
    }

//    @Test
//    public void testFind7() throws Exception {
//        final List<Document> dbObjectList = new ArrayList<>(10);
//        for (int i = 0; i < 10; i++) {
//            dbObjectList.add(new Document("id", i));
//        }
//        Document returnFields = new Document("id", 1);
//        QueryOptions queryOptions = new QueryOptions("exclude", Arrays.asList("id"));
//        List<DataResult<HashMap>> queryResultList = mongoDBCollection.find(dbObjectList, returnFields, new ComplexTypeConverter<HashMap, Object>() {
//            @Override
//            public HashMap convertToDataModelType(Object object) {
//                return new HashMap(object.toMap());
//            }
//
//            @Override
//            public DBObject convertToStorageType(HashMap object) {
//                return null;
//            }
//        }, queryOptions);
//        assertNotNull("Object queryResultList cannot be null", queryResultList);
//        assertNotNull("Object queryResultList.get(0) cannot be null", queryResultList.get(0).getResults());
//        assertTrue("Returned field must instance of Hashmap", queryResultList.get(0).first() instanceof HashMap);
//        assertEquals("resultType must 'java.util.HashMap'", "java.util.HashMap", queryResultList.get(0).getResultType());
//    }

    @Test
    public void testFind8() throws Exception {
        List<Bson> dbObjectList = new ArrayList<>(10);
        for (int i = 0; i < 10; i++) {
//            dbObjectList.add(new Document("id", i));
            dbObjectList.add(Filters.eq("id", i));
        }
        Document returnFields = new Document();
        QueryOptions queryOptions = new QueryOptions();
//        QueryOptions queryOptions = new QueryOptions("exclude", Collections.singletonList("id"));
        List<DataResult<User>> queryResultList = mongoDBCollection.find(dbObjectList, returnFields, User.class, queryOptions);
        assertNotNull("Object queryResultList cannot be null", queryResultList);
        assertNotNull("Object queryResultList.get(0) cannot be null", queryResultList.get(0).getResults());
        assertTrue("Returned field must instance of User", queryResultList.get(0).first() instanceof User);
        assertEquals("resultType must '" + User.class.getCanonicalName() + "'", User.class.getCanonicalName(), queryResultList.get(0).getResultType());
        for (DataResult<User> queryResult : queryResultList) {
            assertEquals(1, queryResult.getNumResults());
            assertThat(NAMES, CoreMatchers.hasItem(queryResult.first().name));
            assertThat(SURNAMES, CoreMatchers.hasItem(queryResult.first().surname));
        }
    }

    @Test
    @Disabled
    public void testPermanentCursor() throws Exception {
        Document query = new Document();
        QueryOptions queryOptions = new QueryOptions();
        int documents = 50000;
        MongoDBCollection collection = createTestCollection("cursor5", documents);

        MongoPersistentCursor cursor = new MongoPersistentCursor(collection, query, null, queryOptions);

        int i = 0;
        while (cursor.hasNext()) {
            Document document = cursor.next();
            if (i % (documents / 50) == 0) {
                System.out.println("document.get(\"_id\") = " + document.get("_id"));
            }
            i++;
            if (i == 10) {
                System.out.println("SLEEP!!! " + i + " document.get(\"_id\") = " + document.get("_id"));
                int totalMin = 1;
                for (int min = 0; min < totalMin; min++) {
                    System.out.println("Continue sleeping: " + min + "/" + totalMin);
                    Thread.sleep(60 * 1000);
                }
                System.out.println("Woke up!!!");
                document = cursor.next();
                i++;
                System.out.println("Woke up!!! " + i + " document.get(\"_id\") = " + document.get("_id"));

            }
        }

        assertEquals(1, cursor.getNumExceptions());
        assertEquals(documents, cursor.getCount());
        assertEquals(documents, i);
    }

    @Test
    public void testAggregate() {
        List<Bson> dbObjectList = new ArrayList<>();
        Document match = new Document("$match", new Document("age", new BasicDBObject("$gt", 2)));
        Document group = new Document("$group", new Document("_id", "$age"));

        dbObjectList.add(match);
        dbObjectList.add(group);

        DataResult<Document> queryResult = mongoDBCollection.aggregate(dbObjectList, null);
        assertNotNull("Object queryResult cannot be null", queryResult);
        assertNotNull("Object queryResult.getResults() cannot be null", queryResult.getResults());
        assertEquals("There must be 2 results", 2, queryResult.getResults().size());
        List<Document> result = queryResult.getResults();

        queryResult = mongoDBCollection.aggregate(dbObjectList, new QueryOptions(QueryOptions.LIMIT, 1).append(QueryOptions.SKIP, 0));
        assertEquals("There must be 1 results", 1, queryResult.getResults().size());
        assertTrue(queryResult.getResults().contains(result.get(0)));

        queryResult = mongoDBCollection.aggregate(dbObjectList, new QueryOptions(QueryOptions.LIMIT, 1).append(QueryOptions.SKIP, 1));
        assertEquals("There must be 1 results", 1, queryResult.getResults().size());

        System.out.println("result = " + result);
        System.out.println("queryResult.getResults() = " + queryResult.getResults());

        assertTrue(queryResult.getResults().contains(result.get(1)));
    }

    @Test
    public void testInsert() throws Exception {
        Long countBefore = mongoDBCollectionInsertTest.count().getNumMatches();
        for (int i = 1; i < 50; i++) {
            mongoDBCollectionInsertTest.insert(new Document("insertedObject", i), null);
            assertEquals("Insert operation must insert 1 element each time.", countBefore + i, mongoDBCollectionInsertTest.count().getNumMatches());
        }
    }

    @Test
    public void testInsert1() throws Exception {
        Document uniqueObject = new Document("_id", "myUniqueId");
        mongoDBCollectionInsertTest.insert(uniqueObject, null);

        thrown.expect(MongoWriteException.class);
        mongoDBCollectionInsertTest.insert(uniqueObject, null);
    }

    @Test
    public void testInsert2() throws Exception {
        Long countBefore = mongoDBCollectionInsertTest.count().getNumMatches();
        int numBulkInsertions = 50;
        int bulkInsertSize = 100;

        for (int b = 1; b < numBulkInsertions; b++) {
            ArrayList<Document> list = new ArrayList<>(bulkInsertSize);
            for (int i = 0; i < bulkInsertSize; i++) {
                list.add(new Document("bulkInsertedObject", i));
            }
            mongoDBCollectionInsertTest.insert(list, null);
            assertEquals("Bulk insert operation must insert " + bulkInsertSize + " elements each time.", countBefore + bulkInsertSize * b, mongoDBCollectionInsertTest.count().getNumMatches());
        }
    }

    @Test
    public void testInsert3() throws Exception {
        Document uniqueObject = new Document("_id", "myUniqueId");

        ArrayList<Document> list = new ArrayList<>(10);
        for (int i = 0; i < 10; i++) {
            list.add(uniqueObject);
        }

        thrown.expect(MongoBulkWriteException.class);
        mongoDBCollectionInsertTest.insert(list, null);
    }

    @Test
    public void testInsertUnique() throws Exception {
        MongoDBCollection uniqueIndexTest = createTestCollection("unique_index_test", 50);
        uniqueIndexTest.createIndex(new Document("number", 1), new ObjectMap(MongoDBCollection.UNIQUE, true));
        Document uniqueObject = new Document("number", -1);

        uniqueIndexTest.insert(uniqueObject, null);

        thrown.expect(MongoWriteException.class);
        uniqueIndexTest.insert(uniqueObject, null);
    }

    @Test
    public void testUpdate() throws Exception {
        Document query = new Document("name", "John");
        long count = mongoDBCollectionUpdateTest.count(query).getNumMatches();
        DataResult writeResult = mongoDBCollectionUpdateTest.update(query,
                new Document("$set", new Document("modified", true)),
                new QueryOptions("multi", true)
        );
        assertEquals("All the objects are named \"John\", so all objects should be modified", count, writeResult.getNumUpdated());
    }

    @Test
    public void testUpdate1() throws Exception {
        DataResult writeResult = mongoDBCollectionUpdateTest.update(new Document("surname", "Johnson"),
                new Document("$set", new Document("modifiedAgain", true)),
                new QueryOptions("multi", true)
        );
        assertEquals("Any objects have the surname \"Johnson\", so any objects should be modified", 0, writeResult.getNumUpdated());
    }

    @Test
    public void testUpdate2() throws Exception {
        DataResult writeResult = mongoDBCollectionUpdateTest.update(new Document("surname", "Johnson"),
                new Document("$set", new Document("modifiedAgain", true)),
                new QueryOptions("upsert", true)
        );
        assertEquals("Any objects have the surname \"Johnson\", so there are no matched documents", 0, writeResult.getNumMatches());
        assertEquals("Any objects have the surname \"Johnson\", so one object should be inserted", 1, writeResult.getNumInserted());
    }

    @Test
    public void testUpdate3() throws Exception {
        long count = mongoDBCollectionUpdateTest.count().getNumMatches();
        int modifiedDocuments = (int) count / 2;
        ArrayList<Bson> queries = new ArrayList<>(modifiedDocuments);
        ArrayList<Bson> updates = new ArrayList<>(modifiedDocuments);

        for (int i = 0; i < modifiedDocuments; i++) {
            queries.add(new Document("id", i));
            updates.add(new Document("$set", new BasicDBObject("bulkUpdated", i)));
        }
        DataResult writeResult = mongoDBCollectionUpdateTest.update(queries, updates, new QueryOptions("multi", false));
        assertEquals("", modifiedDocuments, writeResult.getNumUpdated());
    }

    @Test
    public void testUpdate4_error() throws Exception {
        int count = (int) mongoDBCollectionUpdateTest.count().getNumMatches();
        int modifiedDocuments = count / 2;
        ArrayList<Bson> queries = new ArrayList<>(modifiedDocuments);
        ArrayList<Bson> updates = new ArrayList<>(modifiedDocuments);

        for (int i = 0; i < modifiedDocuments; i++) {
            queries.add(new Document("id", i));
            updates.add(new Document("$set", new BasicDBObject("bulkUpdated", i)));
        }
        updates.remove(updates.size() - 1);

        thrown.expect(IndexOutOfBoundsException.class);
        mongoDBCollectionUpdateTest.update(queries, updates, new QueryOptions("multi", false));
    }

    @Test
    public void testUpdate5_upsert() throws Exception {
        long count = mongoDBCollectionUpdateTest.count().getNumMatches();
        int modifiedDocuments = (int) count / 2;
        ArrayList<Bson> queries = new ArrayList<>(modifiedDocuments);
        ArrayList<Bson> updates = new ArrayList<>(modifiedDocuments);

        for (int i = 0; i < modifiedDocuments; i++) {
            queries.add(new Document("id", i));
            updates.add(new Document("$set", new BasicDBObject("bulkUpdated_b", i)));
        }

        int numUpserts = 10;
        for (int i = 0; i < numUpserts; i++) {
            queries.add(new Document("id", 10000 + i));
            updates.add(new Document("$set", new BasicDBObject("upsert", i)));
        }

        DataResult writeResult = mongoDBCollectionUpdateTest.update(queries, updates, new QueryOptions("multi", false)
                .append(MongoDBCollection.UPSERT, true));
        assertEquals(modifiedDocuments, writeResult.getNumUpdated());
        assertEquals(numUpserts, writeResult.getNumInserted());
    }

    @Test
    public void testRemove() throws Exception {
        int count = (int) mongoDBCollectionRemoveTest.count().getNumMatches();
        Document query = new Document("age", 1);
        int matchingDocuments = (int) mongoDBCollectionRemoveTest.count(query).getNumMatches();
        DataResult writeResult = mongoDBCollectionRemoveTest.remove(query, null);
        assertEquals(matchingDocuments, writeResult.getNumDeleted());
        assertEquals(mongoDBCollectionRemoveTest.count().getNumMatches(), count - matchingDocuments);
    }

    @Test
    public void testRemove1() throws Exception {
        int count = (int) mongoDBCollectionRemoveTest.count().getNumMatches();

        int numDeletions = 10;
        List<Bson> remove = new ArrayList<>(numDeletions);
        for (int i = 0; i < numDeletions; i++) {
            remove.add(new Document("name", "John"));
        }

        DataResult bulkDataResult = mongoDBCollectionRemoveTest.remove(remove, null);
        assertEquals(numDeletions, bulkDataResult.getNumDeleted());
        assertEquals(mongoDBCollectionRemoveTest.count().getNumMatches(), count - numDeletions);
    }

    @Test
    public void testFindAndModify() throws Exception {

    }

    @Test
    public void testFindAndModify1() throws Exception {

    }

    @Test
    public void testFindAndModify2() throws Exception {

    }

    @Test
    public void testCreateIndex() throws Exception {

    }

    @Test
    public void testDropIndex() throws Exception {

    }

    @Test
    public void testGetIndex() throws Exception {

    }

    @Test
    public void testExplain() throws Exception {
        Document explain = mongoDBCollection.nativeQuery().explain(new Document(), new Document(), new QueryOptions());
        assertNotNull(explain.get("queryPlanner"));
    }

    class BasicQueryResultWriter implements QueryResultWriter<Object> {
        int i = 0;
        String outfile = "/tmp/queryResultWriter.log";
        DataOutputStream fileOutputStream;

        @Override
        public void open() throws IOException {
            System.out.println("Opening!");
            this.fileOutputStream = new DataOutputStream(new FileOutputStream(outfile));
        }

        @Override
        public void write(Object elem) throws IOException {
            String s = String.format("Result %d : %s\n", i++, elem.toString());
            System.out.printf(s);
            fileOutputStream.writeBytes(s);
        }

        @Override
        public void close() throws IOException {
            System.out.println("Closing!");
            fileOutputStream.close();
        }
    }

}
