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
import org.apache.commons.lang3.StringUtils;
import org.bson.Document;
import org.bson.conversions.Bson;
import org.hamcrest.CoreMatchers;
import org.junit.*;
import org.junit.rules.ExpectedException;
import org.opencb.commons.datastore.core.*;

import java.io.DataOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.*;

import static org.junit.Assert.*;
import static org.opencb.commons.datastore.mongodb.MongoDBQueryUtils.*;
import static org.opencb.commons.datastore.mongodb.MongoDBQueryUtils.Accumulator.*;

/**
 * Created by imedina on 29/03/14.
 */
public class MongoDBCollectionTest {

    public static final String EMPTY = "***EMPTY***";
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
    public static final List<String> COLORS = Arrays.asList("red", "green", "yellow", "blue");

    @BeforeClass
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
        public int number;
        public boolean tall;
        public House house;
        public List<Dog> dogs;

        public static class House {
            public String color;
            public int numRooms;
            public int m2;

            @Override
            public String toString() {
                final StringBuilder sb = new StringBuilder("House{");
                sb.append("color='").append(color).append('\'');
                sb.append(", numRooms=").append(numRooms);
                sb.append(", m2=").append(m2);
                sb.append('}');
                return sb.toString();
            }
        }

        public static class Dog {
            public int age;
            public String color;

            @Override
            public String toString() {
                final StringBuilder sb = new StringBuilder("Dog{");
                sb.append("age=").append(age);
                sb.append("color=").append(color);
                sb.append('}');
                return sb.toString();
            }
        }

        @Override
        public String toString() {
            final StringBuilder sb = new StringBuilder("User{");
            sb.append("id=").append(id);
            sb.append(", name='").append(name).append('\'');
            sb.append(", surname='").append(surname).append('\'');
            sb.append(", age=").append(age);
            sb.append(", number=").append(number);
            sb.append(", tall=").append(tall);
            sb.append(", house=").append(house);
            sb.append(", dogs=").append(dogs);
            sb.append('}');
            return sb.toString();
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
            document.put("tall", (i % 6 == 0));
            Document house = new Document();
            house.put("color", COLORS.get(random.nextInt(COLORS.size())));
            house.put("numRooms", (int) (i % 7) + 1);
            house.put("m2", (int) i * 23);
            document.put("house", house);
            int numDogs = random.nextInt(5);
            List<Document> dogs = new ArrayList<>();
            for (int j = 0 ; j < numDogs; j++) {
                Document dog = new Document();
                dog.put("age", random.nextInt(20));
                dog.put("color", COLORS.get(random.nextInt(COLORS.size())));
                dogs.add(dog);
            }
            document.put("dogs", dogs);
            mongoDBCollection.nativeQuery().insert(document, null);
            System.out.println("document.toJson() = " + document.toJson());
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
    @Ignore
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
        // As the order of result list change between executions, we must ensure the assertTrue doesn't depend on the order
        assertTrue(result.contains(queryResult.getResults().get(0)));

        queryResult = mongoDBCollection.aggregate(dbObjectList, new QueryOptions(QueryOptions.LIMIT, 1).append(QueryOptions.SKIP, 1));
        assertEquals("There must be 1 results", 1, queryResult.getResults().size());
        // As the order of result list change between executions, we must ensure the assertTrue doesn't depend on the order
        assertTrue(result.contains(queryResult.getResults().get(0)));
    }

    @Test
    public void testFacetBuckets() {
        Document match = new Document("age", new BasicDBObject("$gt", 2));
        DataResult<Document> matchedResults = mongoDBCollection.find(match, null);

        String fieldName = "name";
        List<Bson> facets = MongoDBQueryUtils.createFacet(match, fieldName);
        MongoDBDocumentToFacetFieldsConverter converter = new MongoDBDocumentToFacetFieldsConverter();
        DataResult<List<FacetField>> aggregate = mongoDBCollection.aggregate(facets, converter, null);

        String value;
        long totalCount = 0;
        Map<String, Integer> map = new HashMap<>();
        for (Document result : matchedResults.getResults()) {
            value = result.getString(fieldName);
            if (StringUtils.isEmpty(value)) {
                value = EMPTY;
                map.put(value, 0);
            } else if (!map.containsKey(value)) {
                map.put(value, 0);
            }
            map.put(value, 1 + map.get(value));
            totalCount++;
        }
        for (List<FacetField> result : aggregate.getResults()) {
            for (FacetField facetField : result) {
                Assert.assertFalse(facetField.getCount() == null);
                Assert.assertEquals(totalCount, facetField.getCount().longValue());
                Assert.assertEquals(map.size(), facetField.getBuckets().size());
                for (FacetField.Bucket bucket : facetField.getBuckets()) {
                    value = bucket.getValue();
                    if (StringUtils.isEmpty(value)) {
                        value = EMPTY;
                    }
                    Assert.assertEquals(map.get(value).longValue(), bucket.getCount());
                }
            }
        }
    }

    @Test
    public void testFacetBucketsBoolean() {
        Document match = new Document("age", new BasicDBObject("$gt", 2));
        DataResult<Document> matchedResults = mongoDBCollection.find(match, null);

        String fieldName = "tall";
        List<Bson> facets = MongoDBQueryUtils.createFacet(match, fieldName);
        MongoDBDocumentToFacetFieldsConverter converter = new MongoDBDocumentToFacetFieldsConverter();
        DataResult<List<FacetField>> aggregate = mongoDBCollection.aggregate(facets, converter, null);

        String value;
        long totalCount = 0;
        Map<String, Integer> map = new HashMap<>();
        for (Document result : matchedResults.getResults()) {
            value = "" + result.getBoolean(fieldName);
            if (StringUtils.isEmpty(value)) {
                value = EMPTY;
                map.put(value, 0);
            } else if (!map.containsKey(value)) {
                map.put(value, 0);
            }
            map.put(value, 1 + map.get(value));
            totalCount++;
        }
        for (List<FacetField> result : aggregate.getResults()) {
            for (FacetField facetField : result) {
                Assert.assertFalse(facetField.getCount() == null);
                Assert.assertEquals(totalCount, facetField.getCount().longValue());
                Assert.assertEquals(map.size(), facetField.getBuckets().size());
                for (FacetField.Bucket bucket : facetField.getBuckets()) {
                    value = bucket.getValue();
                    if (StringUtils.isEmpty(value)) {
                        value = EMPTY;
                    }
                    Assert.assertEquals(map.get(value).longValue(), bucket.getCount());
                }
            }
        }
    }

    @Test
    public void testFacetBucketsDotNotation() {
        Document match = new Document("age", new BasicDBObject("$gt", 2));
        DataResult<Document> matchedResults = mongoDBCollection.find(match, null);

        String fieldName = "house.color";
        List<Bson> facets = MongoDBQueryUtils.createFacet(match, fieldName);
        MongoDBDocumentToFacetFieldsConverter converter = new MongoDBDocumentToFacetFieldsConverter();
        DataResult<List<FacetField>> aggregate = mongoDBCollection.aggregate(facets, converter, null);

        String value;
        long totalCount = 0;
        Map<String, Integer> map = new HashMap<>();
        for (Document result : matchedResults.getResults()) {
            Document house = (Document) result.get("house");
            value = house.getString("color");
            if (StringUtils.isEmpty(value)) {
                value = EMPTY;
                map.put(value, 0);
            } else if (!map.containsKey(value)) {
                map.put(value, 0);
            }
            map.put(value, 1 + map.get(value));
            totalCount++;
        }
        for (List<FacetField> result : aggregate.getResults()) {
            for (FacetField facetField : result) {
                Assert.assertFalse(facetField.getCount() == null);
                Assert.assertEquals(totalCount, facetField.getCount().longValue());
                Assert.assertEquals(map.size(), facetField.getBuckets().size());
                for (FacetField.Bucket bucket : facetField.getBuckets()) {
                    value = bucket.getValue();
                    if (StringUtils.isEmpty(value)) {
                        value = EMPTY;
                    }
                    Assert.assertEquals(map.get(value).longValue(), bucket.getCount());
                }
            }
        }
    }

    @Test
    public void testFacetCountBucketsArray() {
        Document match = new Document("age", new BasicDBObject("$gt", 2));
        DataResult<Document> matchedResults = mongoDBCollection.find(match, null);

        String fieldName = "dogs.color";
        List<Bson> facets = MongoDBQueryUtils.createFacet(match, fieldName);
        System.out.println("facets = " + facets);
        MongoDBDocumentToFacetFieldsConverter converter = new MongoDBDocumentToFacetFieldsConverter();
        DataResult<List<FacetField>> aggregate = mongoDBCollection.aggregate(facets, converter, null);
        for (List<FacetField> facetFieldList : aggregate.getResults()) {
            System.out.println("facetFieldList = " + facetFieldList);
        }

        String value;
        long totalCount = 0;
        Map<String, Integer> map = new HashMap<>();
        for (Document result : matchedResults.getResults()) {
            List<Document> dogs = (List<Document>) result.get("dogs");
            for (Document dog : dogs) {
                totalCount++;
                String color = dog.getString("color");
                if (StringUtils.isEmpty(color)) {
                    color = EMPTY;
                    map.put(color, 0);
                } else if (!map.containsKey(color)) {
                    map.put(color, 0);
                }
                map.put(color, 1 + map.get(color));
            }
        }

        for (List<FacetField> result : aggregate.getResults()) {
            for (FacetField facetField : result) {
                Assert.assertEquals(totalCount, facetField.getCount().longValue());
                Assert.assertEquals(map.size(), facetField.getBuckets().size());
                for (FacetField.Bucket bucket : facetField.getBuckets()) {
                    value = bucket.getValue();
                    if (StringUtils.isEmpty(value)) {
                        value = EMPTY;
                    }
                    Assert.assertEquals(map.get(value).longValue(), bucket.getCount());
                }
            }
        }
    }

    @Test
    public void testFacetAvgBucketsArray() {
        Document match = new Document("age", new BasicDBObject("$gt", 2));
        DataResult<Document> matchedResults = mongoDBCollection.find(match, null);

        String fieldName = "avg(dogs.age)";
        List<Bson> facets = MongoDBQueryUtils.createFacet(match, fieldName);
        System.out.println("facets = " + facets);
        MongoDBDocumentToFacetFieldsConverter converter = new MongoDBDocumentToFacetFieldsConverter();
        DataResult<List<FacetField>> aggregate = mongoDBCollection.aggregate(facets, converter, null);
        for (List<FacetField> facetFieldList : aggregate.getResults()) {
            System.out.println("facetFieldList = " + facetFieldList);
        }

        int counter = 0;
        int acc = 0;
        for (Document doc : matchedResults.getResults()) {
            List<Document> dogs = (List<Document>) doc.get("dogs");
            for (Document dog : dogs) {
                counter++;
                acc += (int) dog.get("age");
            }
        }
        System.out.println("counter = " + counter);
        System.out.println("(acc/counter) = " + (1.0d * acc / counter));
        Assert.assertEquals(aggregate.getResults().get(0).get(0).getAggregationValues().get(0), 1.0d * acc / counter, 0.0001);
    }

    @Test
    public void testFacetFilterAccumulatorBucketsArray() {
        Document match = new Document("age", new BasicDBObject("$gt", 2));
        DataResult<Document> matchedResults = mongoDBCollection.find(match, null);

        String fieldName = "dogs.color:avg(dogs.age)";
        List<Bson> facets = MongoDBQueryUtils.createFacet(match, fieldName);
        System.out.println("facets = " + facets);
        MongoDBDocumentToFacetFieldsConverter converter = new MongoDBDocumentToFacetFieldsConverter();
        DataResult<List<FacetField>> aggregate = mongoDBCollection.aggregate(facets, converter, null);
        for (List<FacetField> facetFieldList : aggregate.getResults()) {
            System.out.println("facetFieldList = " + facetFieldList);
        }

        String value;
        long totalCount = 0;
        Map<String, Integer> counterMap = new HashMap<>();
        Map<String, Integer> accMap = new HashMap<>();
        for (Document result : matchedResults.getResults()) {
            List<Document> dogs = (List<Document>) result.get("dogs");
            for (Document dog : dogs) {
                totalCount++;
                String color = dog.getString("color");
                int age = (int) dog.get("age");
                if (StringUtils.isEmpty(color)) {
                    color = EMPTY;
                    counterMap.put(color, 0);
                    accMap.put(color, 0);
                } else if (!counterMap.containsKey(color)) {
                    counterMap.put(color, 0);
                    accMap.put(color, 0);
                }
                counterMap.put(color, 1 + counterMap.get(color));
                accMap.put(color, age + accMap.get(color));
            }
        }

        for (List<FacetField> result : aggregate.getResults()) {
            for (FacetField facetField : result) {
                Assert.assertEquals(totalCount, facetField.getCount().longValue());
                Assert.assertEquals(counterMap.size(), facetField.getBuckets().size());
                for (FacetField.Bucket bucket : facetField.getBuckets()) {
                    value = bucket.getValue();
                    if (StringUtils.isEmpty(value)) {
                        value = EMPTY;
                    }
                    Assert.assertEquals(counterMap.get(value).longValue(), bucket.getCount());
                    Assert.assertEquals(counterMap.get(value).longValue(), bucket.getFacetFields().get(0).getCount().longValue());
                    Assert.assertEquals("avg", bucket.getFacetFields().get(0).getAggregationName());
                    Assert.assertEquals(1.0 * accMap.get(value) / counterMap.get(value), bucket.getFacetFields().get(0).getAggregationValues().get(0), 0.0001);
                }
            }
        }
    }

    @Test
    public void testFacetRangeArray() {
        Document match = new Document("age", new BasicDBObject("$gt", 2));
        DataResult<Document> matchedResults = mongoDBCollection.find(match, null);

        int start = 1;
        int end = 20;
        int step = 5;
        String fieldName = "dogs.age" + RANGE_MARK1 + start + RANGE_MARK + end + RANGE_MARK2 + ":" + step;
        List<Bson> facets = MongoDBQueryUtils.createFacet(match, fieldName);
        System.out.println("facets = " + facets);
        MongoDBDocumentToFacetFieldsConverter converter = new MongoDBDocumentToFacetFieldsConverter();
        DataResult<List<FacetField>> aggregate = mongoDBCollection.aggregate(facets, converter, null);
        for (List<FacetField> facetFieldList : aggregate.getResults()) {
            System.out.println("facetFieldList = " + facetFieldList);
        }

        long outOfRange = 0;
        List<Double> rangeValues = new ArrayList<>(Arrays.asList(0d, 0d, 0d, 0d));

        Map<String, Integer> map = new HashMap<>();
        for (Document result : matchedResults.getResults()) {
            int bucketNum;
            List<Document> dogs = (List<Document>) result.get("dogs");
            for (Document dog : dogs) {
                int value = (int) dog.get("age");
                if (value < start || value > end) {
                    outOfRange++;
                } else {
                    bucketNum = (int) (value - start) / step;
                    rangeValues.set(bucketNum, 1 + rangeValues.get(bucketNum));
                }
            }
        }
        for (List<FacetField> result : aggregate.getResults()) {
            Assert.assertEquals(1, result.size());
            for (FacetField facetField : result) {
                Assert.assertTrue(facetField.getCount() == null);
                Assert.assertTrue(facetField.getName().contains("" + (1.0d * outOfRange)));
                for (int i = 0; i < facetField.getAggregationValues().size(); i++) {
                    Assert.assertEquals(rangeValues.get(i), facetField.getAggregationValues().get(i));
                }
            }
        }
    }

    @Test
    public void testFacetMax() {
        Document match = new Document("age", new BasicDBObject("$gt", 2));
        DataResult<Document> matchedResults = mongoDBCollection.find(match, null);

        String fieldName = "number";
        List<Bson> facets = MongoDBQueryUtils.createFacet(match, "max(" + fieldName + ")");
        MongoDBDocumentToFacetFieldsConverter converter = new MongoDBDocumentToFacetFieldsConverter();
        DataResult<List<FacetField>> aggregate = mongoDBCollection.aggregate(facets, converter, null);

        long totalCount = 0;
        double maxValue = 0;
        Map<String, Integer> map = new HashMap<>();
        for (Document result : matchedResults.getResults()) {
            Long value = result.getLong(fieldName);
            totalCount++;
            if (value != null) {
                if (value > maxValue) {
                    maxValue = value;
                }
            }
        }
        for (List<FacetField> result : aggregate.getResults()) {
            Assert.assertEquals(1, result.size());
            for (FacetField facetField : result) {
                Assert.assertEquals(totalCount, facetField.getCount().longValue());
                Assert.assertEquals(max.name(), facetField.getAggregationName());
                Assert.assertEquals(maxValue, facetField.getAggregationValues().get(0), 0.0001);
            }
        }
    }

    @Test
    public void testFacetMin() {
        Document match = new Document("age", new BasicDBObject("$gt", 2));
        DataResult<Document> matchedResults = mongoDBCollection.find(match, null);

        String fieldName = "number";
        List<Bson> facets = MongoDBQueryUtils.createFacet(match, "min(" + fieldName + ")");
        MongoDBDocumentToFacetFieldsConverter converter = new MongoDBDocumentToFacetFieldsConverter();
        DataResult<List<FacetField>> aggregate = mongoDBCollection.aggregate(facets, converter, null);

        long count = 0;
        double minValue = Double.MAX_VALUE;
        Map<String, Integer> map = new HashMap<>();
        for (Document result : matchedResults.getResults()) {
            Long value = result.getLong(fieldName);
            count++;
            if (value != null) {
                if (value < minValue) {
                    minValue = value;
                }
            }
        }
        for (List<FacetField> result : aggregate.getResults()) {
            Assert.assertEquals(1, result.size());
            for (FacetField facetField : result) {
                Assert.assertEquals(count, facetField.getCount().longValue());
                Assert.assertEquals(min.name(), facetField.getAggregationName());
                Assert.assertEquals(minValue, facetField.getAggregationValues().get(0), 0.0001);
            }
        }
    }

    @Test
    public void testFacetAvg() {
        Document match = new Document("age", new BasicDBObject("$gt", 2));
        DataResult<Document> matchedResults = mongoDBCollection.find(match, null);

        String fieldName = "number";
        List<Bson> facets = MongoDBQueryUtils.createFacet(match, "avg(" + fieldName + ")");
        MongoDBDocumentToFacetFieldsConverter converter = new MongoDBDocumentToFacetFieldsConverter();
        DataResult<List<FacetField>> aggregate = mongoDBCollection.aggregate(facets, converter, null);

        long totalCount = 0;
        double totalSum = 0;
        Map<String, Integer> map = new HashMap<>();
        for (Document result : matchedResults.getResults()) {
            Long value = result.getLong(fieldName);
            if (value != null) {
                totalSum += value;
                totalCount++;
            }
        }
        for (List<FacetField> result : aggregate.getResults()) {
            Assert.assertEquals(1, result.size());
            for (FacetField facetField : result) {
                Assert.assertEquals(totalCount, facetField.getCount().longValue());
                Assert.assertEquals(avg.name(), facetField.getAggregationName());
                Assert.assertEquals(totalSum / totalCount, facetField.getAggregationValues().get(0), 0.0001);
            }
        }
    }

    @Test
    public void testFacetMaxDotNotationAndList() {
        Document match = new Document("age", new BasicDBObject("$gt", 2));
        DataResult<Document> matchedResults = mongoDBCollection.find(match, null);

        String fieldName = "dogs.age";
        List<Bson> facets = MongoDBQueryUtils.createFacet(match, "max(" + fieldName + ")");
        MongoDBDocumentToFacetFieldsConverter converter = new MongoDBDocumentToFacetFieldsConverter();
        DataResult<List<FacetField>> aggregate = mongoDBCollection.aggregate(facets, converter, null);

        DataResult<Document> aggregate2 = mongoDBCollection.aggregate(facets, null);

        int count = 0;
        List<Double> maxValues = new ArrayList<>(Arrays.asList(0D,0D,0D,0D,0D,0D,0D,0D,0D,0D,0D,0D));
        for (Document result : matchedResults.getResults()) {
            List<Document> dogs = (List<Document>) result.get("dogs");
            if (result.getInteger("age") > 2 && dogs.size() > 0) {
                System.out.println();
                for (int i = 0; i < dogs.size(); i++) {
                    Number value = (Number) dogs.get(i).get("age");
                    count++;
                    System.out.print("age = " + result.getInteger("age") + "; i = " + i + "; value = " + value + "; ");
                    if (value.doubleValue() > maxValues.get(i)) {
                        maxValues.set(i, value.doubleValue());
                    }
                }
            }
        }
        for (List<FacetField> result : aggregate.getResults()) {
            Assert.assertEquals(1, result.size());
            for (FacetField facetField : result) {
                Assert.assertEquals(count, facetField.getCount().longValue());
                Assert.assertEquals(max.name(), facetField.getAggregationName());
//                for (int i = 0; i < facetField.getAggregationValues().size() ; i++) {
//                    Assert.assertEquals(maxValues.get(i), facetField.getAggregationValues().get(i), 0.0001);
//                }
            }
        }
    }

    @Test
    public void testFacetSumAccumulator() {
        Document match = new Document("age", new BasicDBObject("$gt", 2));
        DataResult<Document> matchedResults = mongoDBCollection.find(match, null);
        int total = 0;
        int count = 0;
        String fieldName = "number";
        for (Document result : matchedResults.getResults()) {
            System.out.println("result = " + result);
            count++;
            total += result.getLong(fieldName);
        }
        double avg = total / matchedResults.getNumResults();

        List<Bson> facets = MongoDBQueryUtils.createFacet(match, "avg(" + fieldName + ")");
        MongoDBDocumentToFacetFieldsConverter converter = new MongoDBDocumentToFacetFieldsConverter();
        DataResult<List<FacetField>> aggregate = mongoDBCollection.aggregate(facets, converter, null);
        for (List<FacetField> result : aggregate.getResults()) {
            Assert.assertEquals(1, result.size());
            for (FacetField facetField : result) {
                Assert.assertEquals(count, facetField.getCount().longValue());
                Assert.assertEquals(Accumulator.avg.name(), facetField.getAggregationName());
                Assert.assertEquals(avg, facetField.getAggregationValues().get(0), 0.5);
//                for (int i = 0; i < facetField.getAggregationValues().size() ; i++) {
//                    Assert.assertEquals(maxValues.get(i), facetField.getAggregationValues().get(i), 0.0001);
//                }
            }
        }


        facets = MongoDBQueryUtils.createFacet(match, "sum(" + fieldName + ")");
        aggregate = mongoDBCollection.aggregate(facets, converter, null);
        for (List<FacetField> result : aggregate.getResults()) {
            Assert.assertEquals(1, result.size());
            for (FacetField facetField : result) {
                Assert.assertEquals(count, facetField.getCount().longValue());
                Assert.assertEquals(Accumulator.sum.name(), facetField.getAggregationName());
                Assert.assertEquals(total, facetField.getAggregationValues().get(0), 0.0001);
            }
        }
    }

    @Test
    public void testFacetGroupSumAccumulator() {
        Document match = new Document("age", new BasicDBObject("$gt", 2));
        DataResult<Document> matchedResults = mongoDBCollection.find(match, null);
        int totalCount = 0;
        String groupFieldName = "name";
        String accumulatorFieldName = "age";
        Map<String, Integer> numberPerNames = new HashMap<>();
        Map<String, Integer> countPerNames = new HashMap<>();
        for (Document result : matchedResults.getResults()) {
            String name = result.getString(groupFieldName);
            if (!numberPerNames.containsKey(name)) {
                numberPerNames.put(name, 0);
                countPerNames.put(name, 0);
            }
            numberPerNames.put(name, result.getInteger(accumulatorFieldName) + numberPerNames.get(name));
            countPerNames.put(name, 1 + countPerNames.get(name));
        }

        for (Map.Entry<String, Integer> entry : numberPerNames.entrySet()) {
            System.out.println(entry.getKey() + " --> " + entry.getValue() + ", count = " + countPerNames.get(entry.getKey()));
            totalCount += countPerNames.get(entry.getKey());
        }
        System.out.println("totalCount = " + totalCount);

        String acc = "sum"; // "count"; // "avg";
        String facet = groupFieldName + ":" + acc + "(" + accumulatorFieldName + ")";
        List<Bson> facets = MongoDBQueryUtils.createFacet(match, facet);
        MongoDBDocumentToFacetFieldsConverter converter = new MongoDBDocumentToFacetFieldsConverter();
        DataResult<List<FacetField>> aggregate = mongoDBCollection.aggregate(facets, converter, null);
        System.out.println("aggregate = " + aggregate);
        Assert.assertEquals(1, aggregate.getResults().size());
        FacetField facetField = aggregate.getResults().get(0).get(0);
        Assert.assertEquals(groupFieldName, facetField.getName());
        Assert.assertEquals(totalCount, facetField.getCount(), 0.001);
        Assert.assertEquals(numberPerNames.size(), facetField.getBuckets().size(), 0.001);
        for (FacetField.Bucket bucket : facetField.getBuckets()) {
            Assert.assertTrue(countPerNames.containsKey(bucket.getValue()));
            Assert.assertEquals(countPerNames.get(bucket.getValue()), bucket.getCount(), 0.001);
            Assert.assertEquals(1, bucket.getFacetFields().size());
            Assert.assertEquals(accumulatorFieldName, bucket.getFacetFields().get(0).getName());
            Assert.assertEquals(acc, bucket.getFacetFields().get(0).getAggregationName());
            Assert.assertEquals(numberPerNames.get(bucket.getValue()), bucket.getFacetFields().get(0).getAggregationValues().get(0), 0.001);
        }
    }


    @Test(expected = IllegalArgumentException.class)
    public void testFacetInvalidAccumulator() {
        Document match = new Document("age", new BasicDBObject("$gt", 2));
        DataResult<Document> matchedResults = mongoDBCollection.find(match, null);

        String fieldName = "number";
        List<Bson> facets = MongoDBQueryUtils.createFacet(match, "toto(" + fieldName + ")");
        MongoDBDocumentToFacetFieldsConverter converter = new MongoDBDocumentToFacetFieldsConverter();
        mongoDBCollection.aggregate(facets, converter, null);
    }

    @Test
    public void testFacetCombine() {
        Document match = new Document("age", new BasicDBObject("$gt", 2));
        DataResult<Document> matchedResults = mongoDBCollection.find(match, null);

        String fieldName = "name,surname";
        List<Bson> facets = MongoDBQueryUtils.createFacet(match, fieldName);
        MongoDBDocumentToFacetFieldsConverter converter = new MongoDBDocumentToFacetFieldsConverter();
        DataResult<List<FacetField>> aggregate = mongoDBCollection.aggregate(facets, converter, null);

        String name;
        String surname;
        long totalCount = 0;
        Map<String, Integer> map = new HashMap<>();
        for (Document result : matchedResults.getResults()) {
            name = result.getString("name");
            if (StringUtils.isEmpty(name)) {
                name = null;
            }
            surname = result.getString("surname");
            if (StringUtils.isEmpty(surname)) {
                surname = null;
            }
            String key = "";
            if (name != null) {
                key += name;
            }
            key += AND_SEPARATOR;
            if (surname != null) {
                key += surname;
            }
            if (!map.containsKey(key)) {
                map.put(key, 0);
            }
            map.put(key, 1 + map.get(key));
            totalCount++;
        }
        String value;
        for (List<FacetField> result : aggregate.getResults()) {
            for (FacetField facetField : result) {
                Assert.assertFalse(facetField.getCount() == null);
                Assert.assertEquals(totalCount, facetField.getCount().longValue());
                Assert.assertEquals(map.size(), facetField.getBuckets().size());
                for (FacetField.Bucket bucket : facetField.getBuckets()) {
                    value = bucket.getValue();
                    Assert.assertEquals(map.get(value).longValue(), bucket.getCount());
                }
            }
        }
    }

    @Test
    public void testFacetRange() {
        Document match = new Document("age", new BasicDBObject("$gt", 2));
        DataResult<Document> matchedResults = mongoDBCollection.find(match, null);

        int start = 1000;
        int end = 5000;
        int step = 1000;
        String fieldName = "number" + RANGE_MARK1 + start + RANGE_MARK + end + RANGE_MARK2 + ":" + step;
        List<Bson> facets = MongoDBQueryUtils.createFacet(match, fieldName);
        System.out.println("facets = " + facets);
        MongoDBDocumentToFacetFieldsConverter converter = new MongoDBDocumentToFacetFieldsConverter();
        DataResult<List<FacetField>> aggregate = mongoDBCollection.aggregate(facets, converter, null);
        System.out.println("aggregate.first() = " + aggregate.first());

        long outOfRange = 0;
        List<Double> rangeValues = new ArrayList<>(Arrays.asList(0d, 0d, 0d, 0d, 0d));

        Map<String, Integer> map = new HashMap<>();
        for (Document result : matchedResults.getResults()) {
            int bucketNum;
            Long value = result.getLong("number");
            if (value != null) {
                bucketNum = (int) (value - start) / step;
                int numSections = (int) Math.ceil((end - start + 1) / step);
                if (value < start || bucketNum > numSections) {
                    outOfRange++;
                } else {
                    rangeValues.set(bucketNum, 1 + rangeValues.get(bucketNum));
                }
            }
        }
        System.out.println("rangeValues.toString() = " + rangeValues.toString());
        for (List<FacetField> result : aggregate.getResults()) {
            Assert.assertEquals(1, result.size());
            for (FacetField facetField : result) {
                Assert.assertTrue(facetField.getCount() == null);
                Assert.assertTrue(facetField.getName().contains("" + (1.0d * outOfRange)));
                for (int i = 0; i < facetField.getAggregationValues().size(); i++) {
                    Assert.assertEquals(rangeValues.get(i), facetField.getAggregationValues().get(i));
                }
            }
        }
    }

    @Test(expected = IllegalArgumentException.class)
    public void testFacetInvalidRangeFormat() {
        Document match = new Document("age", new BasicDBObject("$gt", 2));
        MongoDBQueryUtils.createFacet(match, "house.m2[toto0..20000]:1000");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testFacetInvalidRangeFormat1() {
        Document match = new Document("age", new BasicDBObject("$gt", 2));
        MongoDBQueryUtils.createFacet(match, "house.m2[0:20000]:1000");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testFacetInvalidRangeFormat2() {
        Document match = new Document("age", new BasicDBObject("$gt", 2));
        MongoDBQueryUtils.createFacet(match, "house.m2[toto0..20000]..1000");
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
