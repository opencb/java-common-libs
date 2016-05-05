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

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.mongodb.MongoExecutionTimeoutException;
import com.mongodb.bulk.BulkWriteResult;
import com.mongodb.client.AggregateIterable;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.model.Aggregates;
import com.mongodb.client.model.IndexOptions;
import com.mongodb.client.result.DeleteResult;
import com.mongodb.client.result.UpdateResult;
import org.bson.Document;
import org.bson.conversions.Bson;
import org.opencb.commons.datastore.core.*;

import java.io.IOException;
import java.util.*;

/**
 * @author Ignacio Medina &lt;imedina@ebi.ac.uk&gt;
 * @author Cristina Yenyxe Gonzalez Garcia &lt;cyenyxe@ebi.ac.uk&gt;
 */
public class MongoDBCollection {

    public static final String INCLUDE = "include";
    public static final String EXCLUDE = "exclude";
    public static final String LIMIT = "limit";
    public static final String SKIP = "skip";
    public static final String SORT = "sort";
    public static final String ORDER = "order";

    public static final String TIMEOUT = "timeout";
    public static final String SKIP_COUNT = "skipCount";
    public static final String BATCH_SIZE = "batchSize";
    public static final String ELEM_MATCH = "elemMatch";

    public static final String UPSERT = "upsert";
    public static final String MULTI = "multi";
    public static final String REPLACE = "replace";

    public static final String UNIQUE = "unique";
    public static final String BACKGROUND = "background";
    public static final String SPARSE = "sparse";
    public static final String NAME = "index_name";
    public static final String ASCENDING = "ascending";
    public static final String DESCENDING = "descending";

    private MongoCollection<Document> dbCollection;

    private MongoDBNativeQuery mongoDBNativeQuery;
    private QueryResultWriter<Object> queryResultWriter;

    private ObjectMapper objectMapper;
    private ObjectWriter objectWriter;

    MongoDBCollection(MongoCollection dbCollection) {
        this(dbCollection, null);
    }

    MongoDBCollection(MongoCollection dbCollection, QueryResultWriter<Object> queryResultWriter) {
        this.dbCollection = dbCollection;
        this.queryResultWriter = queryResultWriter;

        mongoDBNativeQuery = new MongoDBNativeQuery(dbCollection);

        objectMapper = new ObjectMapper();
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        objectWriter = objectMapper.writer();
    }


    private long startQuery() {
        return System.currentTimeMillis();
    }

    private <T> QueryResult<T> endQuery(List result, double start) {
        int numResults = (result != null) ? result.size() : 0;
        return endQuery(result, numResults, start);
    }

    private <T> QueryResult<T> endQuery(List result, int numTotalResults, double start) {
        long end = System.currentTimeMillis();
        int numResults = (result != null) ? result.size() : 0;

        QueryResult<T> queryResult = new QueryResult(null, (int) (end - start), numResults, numTotalResults, null, null, result);
        // If a converter is provided, convert DBObjects to the requested type
//        if (converter != null) {
//            List convertedResult = new ArrayList<>(numResults);
//            for (Object o : result) {
//                convertedResult.add(converter.convertToDataModelType(o));
//            }
//            queryResult.setResult(convertedResult);
//        } else {
//            queryResult.setResult(result);
//        }

        return queryResult;

    }

    public QueryResult<Long> count() {
        long start = startQuery();
        long l = mongoDBNativeQuery.count();
        return endQuery(Arrays.asList(l), start);
    }

    public QueryResult<Long> count(Bson query) {
        long start = startQuery();
        long l = mongoDBNativeQuery.count(query);
        return endQuery(Arrays.asList(l), start);
    }


    public QueryResult<String> distinct(String key, Bson query) {
        return distinct(key, query, String.class);
    }

    public <T> QueryResult<T> distinct(String key, Bson query, Class<T> clazz) {
        long start = startQuery();
        List<T> l = new ArrayList<>();
        MongoCursor iterator = mongoDBNativeQuery.distinct(key, query, clazz).iterator();
        while (iterator.hasNext()) {
            l.add((T) iterator.next());
        }
        return endQuery(l, start);
    }
//
//    public <DataModelType, StorageType> QueryResult<DataModelType> distinct(String key, Bson query, Class<StorageType> clazz,
//                                                                            ComplexTypeConverter<DataModelType, StorageType> converter) {
//        long start = startQuery();
//
//        List<DataModelType> convertedresultList = new ArrayList<>();
//        List<StorageType> distinct = new ArrayList<>();
//        MongoCursor<StorageType> iterator = mongoDBNativeQuery.distinct(key, query, clazz).iterator();
////        List<O> distinct = mongoDBNativeQuery.distinct(key, query, Object.class);
//        while (iterator.hasNext()) {
//            distinct.add(iterator.next());
//        }
//
//        for (StorageType storageType : distinct) {
//            convertedresultList.add(converter.convertToDataModelType(storageType));
//        }
//        return endQuery(convertedresultList);
//    }
//

    public QueryResult<Document> find(Bson query, QueryOptions options) {
        return privateFind(query, null, Document.class, null, options);
    }

    public QueryResult<Document> find(Bson query, Bson projection, QueryOptions options) {
        return privateFind(query, projection, Document.class, null, options);
    }

    public <T> QueryResult<T> find(Bson query, Bson projection, Class<T> clazz, QueryOptions options) {
        return privateFind(query, projection, clazz, null, options);
    }

    public <T> QueryResult<T> find(Bson query, ComplexTypeConverter<T, Document> converter, QueryOptions options) {
        return privateFind(query, null, null, converter, options);
    }

    public <T> QueryResult<T> find(Bson query, Bson projection, ComplexTypeConverter<T, Document> converter, QueryOptions options) {
        return privateFind(query, projection, null, converter, options);
    }


    public List<QueryResult<Document>> find(List<Bson> queries, QueryOptions options) {
        return find(queries, null, options);
    }

    public List<QueryResult<Document>> find(List<Bson> queries, Bson projection, QueryOptions options) {
        return privateFind(queries, projection, null, null, options);
    }

    public <T> List<QueryResult<T>> find(List<Bson> queries, Bson projection, Class<T> clazz, QueryOptions options) {
        return privateFind(queries, projection, clazz, null, options);
    }

    public <T> List<QueryResult<T>> find(List<Bson> queries, Bson projection, ComplexTypeConverter<T, Document> converter,
                                         QueryOptions options) {
        return privateFind(queries, projection, null, converter, options);
    }


    private <T> QueryResult<T> privateFind(Bson query, Bson projection, Class<T> clazz, ComplexTypeConverter<T, Document> converter,
                                           QueryOptions options) {
        long start = startQuery();

        /**
         * Getting the cursor and setting the batchSize from options. Default value set to 20.
         */
        FindIterable<Document> findIterable = mongoDBNativeQuery.find(query, projection, options);
        MongoCursor<Document> cursor = findIterable.iterator();

        QueryResult<T> queryResult;
        List<T> list = new LinkedList<>();
        if (cursor != null) {
            if (queryResultWriter != null) {
                try {
                    queryResultWriter.open();
                    while (cursor.hasNext()) {
                        queryResultWriter.write(cursor.next());
                    }
                    queryResultWriter.close();
                } catch (IOException e) {
                    cursor.close();
                    queryResult = endQuery(null, start);
                    queryResult.setErrorMsg(e.getMessage() + " " + Arrays.toString(e.getStackTrace()));
                    return queryResult;
                }
            } else {
                if (converter != null) {
                    while (cursor.hasNext()) {
                        list.add(converter.convertToDataModelType(cursor.next()));
                    }
                } else {
                    if (clazz != null && !clazz.equals(Document.class)) {
                        Document document;
                        while (cursor.hasNext()) {
                            document = cursor.next();
                            try {
                                list.add(objectMapper.readValue(objectWriter.writeValueAsString(document), clazz));
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                    } else {
                        while (cursor.hasNext()) {
                            list.add((T) cursor.next());
                        }
                    }
                }
            }

            if (options != null && options.getInt(LIMIT) > 0) {
                int numTotalResults;
                if (options.getBoolean(SKIP_COUNT)) {
                    numTotalResults = -1;
                } else {
                    try {
//                        numTotalResults = findIterable.maxTime(options.getInt("countTimeout"), TimeUnit.MILLISECONDS).count();
                        numTotalResults = (int) mongoDBNativeQuery.count(query);
                    } catch (MongoExecutionTimeoutException e) {
                        numTotalResults = -1;
                    }
                }
                queryResult = endQuery(list, numTotalResults, start);
            } else {
                queryResult = endQuery(list, start);
            }
            cursor.close();
        } else {
            queryResult = endQuery(list, start);
        }

        return queryResult;
    }

    public <T> List<QueryResult<T>> privateFind(List<Bson> queries, Bson projection, Class<T> clazz,
                                                ComplexTypeConverter<T, Document> converter, QueryOptions options) {
        List<QueryResult<T>> queryResultList = new ArrayList<>(queries.size());
        for (Bson query : queries) {
            QueryResult<T> queryResult = privateFind(query, projection, clazz, converter, options);
            queryResultList.add(queryResult);
        }
        return queryResultList;
    }


    public QueryResult<Document> aggregate(List<Bson> operations, QueryOptions options) {
        long start = startQuery();
        QueryResult<Document> queryResult;

        if (options != null && options.containsKey("limit")) {
            // we need to be sure that the List is mutable
            operations = new ArrayList<>(operations);
            operations.add(Aggregates.limit(options.getInt("limit")));
        }
        AggregateIterable output = mongoDBNativeQuery.aggregate(operations, options);
        MongoCursor<Document> iterator = output.iterator();
        List<Bson> list = new LinkedList<>();
        if (queryResultWriter != null) {
            try {
                queryResultWriter.open();
                while (iterator.hasNext()) {
                    queryResultWriter.write(iterator.next());
                }
                queryResultWriter.close();
            } catch (IOException e) {
                queryResult = endQuery(list, start);
                queryResult.setErrorMsg(e.getMessage() + " " + Arrays.toString(e.getStackTrace()));
                return queryResult;
            }
        } else {
            while (iterator.hasNext()) {
                list.add(iterator.next());
            }
        }
        queryResult = endQuery(list, start);
        return queryResult;
    }

    public <T> QueryResult<T> aggregate(List<Bson> operations, ComplexTypeConverter<T, Document> converter, QueryOptions options) {
        long start = startQuery();
        QueryResult<T> queryResult;
        AggregateIterable output = mongoDBNativeQuery.aggregate(operations, options);
        MongoCursor<Document> iterator = output.iterator();
        List<T> list = new LinkedList<>();
        if (queryResultWriter != null) {
            try {
                queryResultWriter.open();
                while (iterator.hasNext()) {
                    queryResultWriter.write(iterator.next());
                }
                queryResultWriter.close();
            } catch (IOException e) {
                queryResult = endQuery(list, start);
                queryResult.setErrorMsg(e.getMessage() + " " + Arrays.toString(e.getStackTrace()));
                return queryResult;
            }
        } else {
            if (converter != null) {
                while (iterator.hasNext()) {
                    list.add(converter.convertToDataModelType(iterator.next()));
                }
            } else {
                while (iterator.hasNext()) {
                    list.add((T) iterator.next());
                }
            }
        }
        queryResult = endQuery(list, start);
        return queryResult;
    }

    public QueryResult insert(Document object, QueryOptions options) {
        long start = startQuery();
        mongoDBNativeQuery.insert(object, options);
        return endQuery(Collections.singletonList(Collections.EMPTY_LIST), start);
    }

    //Bulk insert
    public QueryResult<BulkWriteResult> insert(List<Document> objects, QueryOptions options) {
        long start = startQuery();
        BulkWriteResult writeResult = mongoDBNativeQuery.insert(objects, options);
        return endQuery(Collections.singletonList(writeResult), start);
    }


    public QueryResult<UpdateResult> update(Bson query, Bson update, QueryOptions options) {
        long start = startQuery();

        boolean upsert = false;
        boolean multi = false;
        boolean replace = false;
        if (options != null) {
            upsert = options.getBoolean(UPSERT);
            multi = options.getBoolean(MULTI);
            replace = options.getBoolean(REPLACE);
        }

        UpdateResult updateResult;
        if (replace) {
            updateResult = mongoDBNativeQuery.replace(query, update, upsert);
        } else {
            updateResult = mongoDBNativeQuery.update(query, update, upsert, multi);
        }
        return endQuery(Collections.singletonList(updateResult), start);
    }

    //Bulk update
    public QueryResult<BulkWriteResult> update(List<Bson> queries, List<Bson> updates, QueryOptions options) {
        long start = startQuery();

        boolean upsert = false;
        boolean multi = false;
        if (options != null) {
            upsert = options.getBoolean(UPSERT);
            multi = options.getBoolean(MULTI);
        }

        com.mongodb.bulk.BulkWriteResult wr = mongoDBNativeQuery.update(queries, updates, upsert, multi);
        QueryResult<BulkWriteResult> queryResult = endQuery(Arrays.asList(wr), start);
        return queryResult;
    }


    public QueryResult<DeleteResult> remove(Bson query, QueryOptions options) {
        long start = startQuery();
        DeleteResult wr = mongoDBNativeQuery.remove(query);
        QueryResult<DeleteResult> queryResult = endQuery(Arrays.asList(wr), start);
        return queryResult;
    }

    //Bulk remove
    public QueryResult<BulkWriteResult> remove(List<Bson> query, QueryOptions options) {
        long start = startQuery();

        boolean multi = false;
        if (options != null) {
            multi = options.getBoolean(MULTI);
        }
        com.mongodb.bulk.BulkWriteResult wr = mongoDBNativeQuery.remove(query, multi);
        QueryResult<BulkWriteResult> queryResult = endQuery(Arrays.asList(wr), start);

        return queryResult;
    }

    public QueryResult<Document> findAndUpdate(Bson query, Bson projection, Bson sort, Bson update, QueryOptions options) {
        return privateFindAndUpdate(query, projection, sort, update, options, null, null);
    }

    public <T> QueryResult<T> findAndUpdate(Bson query, Bson projection, Bson sort, Bson update, Class<T> clazz, QueryOptions options) {
        return privateFindAndUpdate(query, projection, sort, update, options, clazz, null);
    }

    private <T> QueryResult<T> privateFindAndUpdate(Bson query, Bson projection, Bson sort, Bson update, QueryOptions options,
                                                    Class<T> clazz, ComplexTypeConverter<T, Document> converter) {
        long start = startQuery();
        Document result = mongoDBNativeQuery.findAndUpdate(query, projection, sort, update, options);
        if (clazz != null && !clazz.equals(Document.class)) {
            try {
                return endQuery(Collections.singletonList(objectMapper.readValue(objectWriter.writeValueAsString(result), clazz)), start);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return endQuery(Collections.singletonList(result), start);
    }


    public QueryResult<Document> findAndModify(Bson query, Bson fields, Bson sort, Document update, QueryOptions options) {
        return privateFindAndModify(query, fields, sort, update, options, null, null);
    }

    public <T> QueryResult<T> findAndModify(Bson query, Bson fields, Bson sort, Document update, QueryOptions options, Class<T> clazz) {
        return privateFindAndModify(query, fields, sort, update, options, clazz, null);
    }

    public <T> QueryResult<T> findAndModify(Bson query, Bson fields, Bson sort, Document update, QueryOptions options,
                                            ComplexTypeConverter<T, Document> converter) {
        return privateFindAndModify(query, fields, sort, update, options, null, converter);
    }

    private <T> QueryResult<T> privateFindAndModify(Bson query, Bson fields, Bson sort, Document update, QueryOptions options,
                                                    Class<T> clazz, ComplexTypeConverter<T, Document> converter) {
        long start = startQuery();
        Object result = mongoDBNativeQuery.findAndModify(query, fields, sort, update, options);
        return endQuery(Collections.singletonList(result), start);
    }

    public QueryResult createIndex(Bson keys, ObjectMap options) {
        long start = startQuery();
        IndexOptions i = new IndexOptions();
        if (options.containsKey(UNIQUE)) {
            i.unique(options.getBoolean(UNIQUE));
        }
        if (options.containsKey(BACKGROUND)) {
            i.background(options.getBoolean(BACKGROUND));
        }
        if (options.containsKey(SPARSE)) {
            i.sparse(options.getBoolean(SPARSE));
        }
        if (options.containsKey(NAME)) {
            i.name(options.getString(NAME));
        }

        mongoDBNativeQuery.createIndex(keys, i);
        QueryResult queryResult = endQuery(Collections.emptyList(), start);
        return queryResult;
    }

    public QueryResult dropIndex(Bson keys) {
        long start = startQuery();
        mongoDBNativeQuery.dropIndex(keys);
        QueryResult queryResult = endQuery(Collections.emptyList(), start);
        return queryResult;
    }

    public QueryResult<Document> getIndex() {
        long start = startQuery();
        List<Document> index = mongoDBNativeQuery.getIndex();
        QueryResult<Document> queryResult = endQuery(index, start);
        return queryResult;
    }


    public QueryResultWriter<Object> getQueryResultWriter() {
        return queryResultWriter;
    }

    public void setQueryResultWriter(QueryResultWriter<Object> queryResultWriter) {
        this.queryResultWriter = queryResultWriter;
    }

    /**
     * Returns a Native instance to MongoDB. This is a convenience method,
     * equivalent to {@code new MongoClientOptions.Native()}.
     *
     * @return a new instance of a Native
     */
    public MongoDBNativeQuery nativeQuery() {
        return mongoDBNativeQuery;
    }

}
