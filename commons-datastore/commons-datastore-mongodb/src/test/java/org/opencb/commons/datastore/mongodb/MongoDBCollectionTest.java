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
import com.mongodb.client.result.DeleteResult;
import com.mongodb.client.result.UpdateResult;
import org.bson.Document;
import org.bson.conversions.Bson;
import org.junit.*;
import org.junit.rules.ExpectedException;
import org.opencb.commons.datastore.core.QueryOptions;
import org.opencb.commons.datastore.core.QueryResult;
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

    @BeforeClass
    public static void beforeClass() throws Exception {
        mongoDataStoreManager = new MongoDataStoreManager("localhost", 27017);

        mongoDataStoreManager.drop("datastore_test");
        mongoDataStore = mongoDataStoreManager.get("datastore_test");

        mongoDBCollection = createTestCollection("test", N);
        mongoDBCollectionInsertTest = createTestCollection("insert_test", 50);
        mongoDBCollectionUpdateTest = createTestCollection("update_test", 50);
        mongoDBCollectionRemoveTest = createTestCollection("remove_test", 50);
    }

    @Before
    public void setUp() throws Exception {


    }

    @AfterClass
    public static void afterClass() throws Exception {
//        mongoDataStoreManager.drop("datastore_test");
        mongoDataStore.close();
    }

    public static class User {
        public long id;
        public String name;
        public String surname;
        public int age;

        @Override
        public String toString() {
            return "User{"
                    + "id:" + id
                    + ", name:\"" + name + '"'
                    + ", surname:\"" + surname + '"'
                    + ", age:" + age
                    + '}';
        }
    }

    private static MongoDBCollection createTestCollection(String test, int size) {
        MongoDBCollection mongoDBCollection = mongoDataStore.getCollection(test);
        Document document;
        for (long i = 0; i < size; i++) {
            document = new Document("id", i);
            document.put("name", "John");
            document.put("surname", "Doe");
            document.put("age", i % 5);
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
        QueryResult<Document> dbObjectQueryResult = mongoDBCollection.find(new Document("id", new Document("$gt", 50)), null);
        System.out.println(dbObjectQueryResult);
        assert (dbObjectQueryResult.getResult().isEmpty());

        mongoDBCollection.setQueryResultWriter(null);
        dbObjectQueryResult = mongoDBCollection.find(new Document("id", new Document("$gt", 50)), null);
        System.out.println(dbObjectQueryResult);
        assert (!dbObjectQueryResult.getResult().isEmpty());

    }

    @Test
    public void testDistinct() throws Exception {
        QueryResult<Integer> id1 = mongoDBCollection.distinct("id", null, Integer.class);
//        QueryResult<Integer> id2 = mongoDBCollection.distinct("id", null, Object.class, new ComplexTypeConverter<Object, Integer>() {
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
    public void testCount() throws Exception {
        QueryResult<Long> queryResult = mongoDBCollection.count();
        assertEquals("The number of documents must be equals", new Long(N), queryResult.getResult().get(0));
    }

    @Test
    public void testCount1() throws Exception {
        QueryResult<Long> queryResult = mongoDBCollection.count();
        assertEquals("The number must be equals", new Long(N), queryResult.first());
    }

    @Test
    public void testDistinct1() throws Exception {
        QueryResult<Integer> queryResult = mongoDBCollection.distinct("age", null, Integer.class);
        assertNotNull("Object cannot be null", queryResult);
        assertEquals("ResultType must be 'java.lang.Integer'", "java.lang.Integer", queryResult.getResultType());
    }

    @Test
    public void testDistinct2() throws Exception {
        QueryResult<String> queryResult = mongoDBCollection.distinct("name", null, String.class);
        assertNotNull("Object cannot be null", queryResult);
        assertEquals("ResultType must be 'java.lang.String'", "java.lang.String", queryResult.getResultType());
    }

//    @Test
//    public void testDistinct3() throws Exception {
//        QueryResult<Integer> queryResult = mongoDBCollection.distinct("age", null, new ComplexTypeConverter<Integer, Object>() {
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
        QueryResult<Document> queryResult = mongoDBCollection.find(dbObject, queryOptions);
        assertNotNull("Object cannot be null", queryResult.getResult());
        assertEquals("Returned Id does not match", 4, queryResult.first().get("id"));
//        System.out.println("queryResult 'include' = " + queryResult.toString());
    }

    @Test
    public void testFind1() throws Exception {
        Document dbObject = new Document("id", 4);
        Document returnFields = new Document("id", 1);
        QueryOptions queryOptions = new QueryOptions("exclude", Arrays.asList("id"));
        QueryResult<Document> queryResult = mongoDBCollection.find(dbObject, returnFields, queryOptions);
        assertNotNull("Object cannot be null", queryResult.getResult());
        assertNull("Field 'name' must not exist", queryResult.first().get("name"));
//        System.out.println("queryResult 'projection' = " + queryResult);
    }

    @Test
    public void testFind2() throws Exception {
        Document dbObject = new Document("id", 4);
        Document returnFields = new Document("id", 1);
        QueryOptions queryOptions = new QueryOptions("exclude", Arrays.asList("id"));
        QueryResult<HashMap> queryResult = mongoDBCollection.find(dbObject, returnFields, HashMap.class, queryOptions);
        assertNotNull("Object cannot be null", queryResult.getResult());
        assertTrue("Returned field must instance of Hashmap", queryResult.first() instanceof HashMap);
    }

//    @Test
//    public void testFind3() throws Exception {
//        final Document dbObject = new Document("id", 4);
//        Document returnFields = new Document("id", 1);
//        QueryOptions queryOptions = new QueryOptions("exclude", Arrays.asList("id"));
//        QueryResult<HashMap> queryResult = mongoDBCollection.find(dbObject, returnFields,
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
//        assertNotNull("Object cannot be null", queryResult.getResult());
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
        List<QueryResult<Document>> queryResultList = mongoDBCollection.find(dbObjectList, queryOptions);
        assertEquals("List must contain 10 results", 10, queryResultList.size());
        assertNotNull("Object cannot be null", queryResultList.get(0).getResult());
        assertEquals("Returned Id does not match", 9, queryResultList.get(9).first().get("id"));
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
        List<QueryResult<Document>> queryResultList = mongoDBCollection.find(dbObjectList, returnFields, queryOptions);
        assertEquals("List must contain 10 results", 10, queryResultList.size());
        assertNotNull("Object cannot be null", queryResultList.get(0).getResult());
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
        List<QueryResult<HashMap>> queryResultList = mongoDBCollection.find(dbObjectList, returnFields, HashMap.class, queryOptions);
        assertNotNull("Object queryResultList cannot be null", queryResultList);
        assertNotNull("Object queryResultList.get(0) cannot be null", queryResultList.get(0).getResult());
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
//        List<QueryResult<HashMap>> queryResultList = mongoDBCollection.find(dbObjectList, returnFields, new ComplexTypeConverter<HashMap, Object>() {
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
//        assertNotNull("Object queryResultList.get(0) cannot be null", queryResultList.get(0).getResult());
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
        List<QueryResult<User>> queryResultList = mongoDBCollection.find(dbObjectList, returnFields, User.class, queryOptions);
        assertNotNull("Object queryResultList cannot be null", queryResultList);
        assertNotNull("Object queryResultList.get(0) cannot be null", queryResultList.get(0).getResult());
        assertTrue("Returned field must instance of User", queryResultList.get(0).first() instanceof User);
        assertEquals("resultType must '" + User.class.getCanonicalName() + "'", User.class.getCanonicalName(), queryResultList.get(0).getResultType());
        for (QueryResult<User> queryResult : queryResultList) {
            assertEquals(1, queryResult.getNumResults());
            assertEquals("John", queryResult.first().name);
            assertEquals("Doe", queryResult.first().surname);
        }
    }

    @Test
    public void testAggregate() throws Exception {
        List<Bson> dbObjectList = new ArrayList<>();
        Document match = new Document("$match", new Document("age", new BasicDBObject("$gt", 2)));
        Document group = new Document("$group", new Document("_id", "$age"));

        dbObjectList.add(match);
        dbObjectList.add(group);

        QueryResult queryResult = mongoDBCollection.aggregate(dbObjectList, null);
        assertNotNull("Object queryResult cannot be null", queryResult);
        assertNotNull("Object queryResult.getResult() cannot be null", queryResult.getResult());
        assertEquals("There must be 2 results", 2, queryResult.getResult().size());
    }

    @Test
    public void testInsert() throws Exception {
        Long countBefore = mongoDBCollectionInsertTest.count().first();
        for (int i = 1; i < 50; i++) {
            mongoDBCollectionInsertTest.insert(new Document("insertedObject", i), null);
            assertEquals("Insert operation must insert 1 element each time.", countBefore + i, mongoDBCollectionInsertTest.count().first().longValue());
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
        Long countBefore = mongoDBCollectionInsertTest.count().first();
        int numBulkInsertions = 50;
        int bulkInsertSize = 100;

        for (int b = 1; b < numBulkInsertions; b++) {
            ArrayList<Document> list = new ArrayList<>(bulkInsertSize);
            for (int i = 0; i < bulkInsertSize; i++) {
                list.add(new Document("bulkInsertedObject", i));
            }
            mongoDBCollectionInsertTest.insert(list, null);
            assertEquals("Bulk insert operation must insert " + bulkInsertSize + " elements each time.", countBefore + bulkInsertSize * b, mongoDBCollectionInsertTest.count().first().longValue());
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
    public void testUpdate() throws Exception {
        Document query = new Document("name", "John");
        long count = mongoDBCollectionUpdateTest.count(query).first();
        UpdateResult writeResult = mongoDBCollectionUpdateTest.update(query,
                new Document("$set", new Document("modified", true)),
                new QueryOptions("multi", true)
        ).first();
        assertEquals("All the objects are named \"John\", so all objects should be modified", count, writeResult.getModifiedCount());
    }

    @Test
    public void testUpdate1() throws Exception {
        UpdateResult writeResult = mongoDBCollectionUpdateTest.update(new Document("surname", "Johnson"),
                new Document("$set", new Document("modifiedAgain", true)),
                new QueryOptions("multi", true)
        ).first();
        assertEquals("Any objects have the surname \"Johnson\", so any objects should be modified", 0, writeResult.getModifiedCount());
    }

    @Test
    public void testUpdate2() throws Exception {
        UpdateResult writeResult = mongoDBCollectionUpdateTest.update(new Document("surname", "Johnson"),
                new Document("$set", new Document("modifiedAgain", true)),
                new QueryOptions("upsert", true)
        ).first();
        assertEquals("Any objects have the surname \"Johnson\", so there are no matched documents", 0, writeResult.getMatchedCount());
        assertNotNull("Any objects have the surname \"Johnson\", so one object should be inserted", writeResult.getUpsertedId());
    }

    @Test
    public void testUpdate3() throws Exception {
        int count = mongoDBCollectionUpdateTest.count().first().intValue();
        int modifiedDocuments = count / 2;
        ArrayList<Bson> queries = new ArrayList<>(modifiedDocuments);
        ArrayList<Bson> updates = new ArrayList<>(modifiedDocuments);

        for (int i = 0; i < modifiedDocuments; i++) {
            queries.add(new Document("id", i));
            updates.add(new Document("$set", new BasicDBObject("bulkUpdated", i)));
        }
        com.mongodb.bulk.BulkWriteResult bulkWriteResult = mongoDBCollectionUpdateTest.update(queries, updates, new QueryOptions("multi", false)).first();
        assertEquals("", modifiedDocuments, bulkWriteResult.getModifiedCount());
    }

    @Test
    public void testUpdate4() throws Exception {
        int count = mongoDBCollectionUpdateTest.count().first().intValue();
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
    public void testRemove() throws Exception {
        int count = mongoDBCollectionRemoveTest.count().first().intValue();
        Document query = new Document("age", 1);
        int matchingDocuments = mongoDBCollectionRemoveTest.count(query).first().intValue();
        DeleteResult writeResult = mongoDBCollectionRemoveTest.remove(query, null).first();
        assertEquals(matchingDocuments, writeResult.getDeletedCount());
        assertEquals(mongoDBCollectionRemoveTest.count().first().intValue(), count - matchingDocuments);
    }

    @Test
    public void testRemove1() throws Exception {
        int count = mongoDBCollectionRemoveTest.count().first().intValue();

        int numDeletions = 10;
        List<Bson> remove = new ArrayList<>(numDeletions);
        for (int i = 0; i < numDeletions; i++) {
            remove.add(new Document("name", "John"));
        }

        com.mongodb.bulk.BulkWriteResult bulkWriteResult = mongoDBCollectionRemoveTest.remove(remove, null).first();
        assertEquals(numDeletions, bulkWriteResult.getDeletedCount());
        assertEquals(mongoDBCollectionRemoveTest.count().first().intValue(), count - numDeletions);
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
