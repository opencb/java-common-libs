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

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.mongodb.ReadPreference;
import com.mongodb.WriteConcern;
import com.mongodb.bulk.BulkWriteResult;
import com.mongodb.client.ClientSession;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.model.IndexOptions;
import com.mongodb.client.result.DeleteResult;
import com.mongodb.client.result.UpdateResult;
import org.bson.BsonType;
import org.bson.BsonValue;
import org.bson.Document;
import org.bson.conversions.Bson;
import org.opencb.commons.datastore.core.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

/**
 * @author Ignacio Medina &lt;imedina@ebi.ac.uk&gt;
 * @author Cristina Yenyxe Gonzalez Garcia &lt;cyenyxe@ebi.ac.uk&gt;
 */
public class MongoDBCollection {

    public static final String BATCH_SIZE = "batchSize";
    public static final String ELEM_MATCH = "elemMatch";

    public static final String UPSERT = "upsert";
    public static final String MULTI = "multi";
    public static final String REPLACE = "replace";

    public static final String UNIQUE = "unique";
    public static final String BACKGROUND = "background";
    public static final String SPARSE = "sparse";
    public static final String NAME = "index_name";

    private MongoDBNativeQuery mongoDBNativeQuery;
    private QueryResultWriter<Object> queryResultWriter;

    private ObjectMapper objectMapper;
    private ObjectWriter objectWriter;

    MongoDBCollection(MongoCollection<Document> dbCollection) {
        this(dbCollection, null);
    }

    MongoDBCollection(MongoCollection<Document> dbCollection, QueryResultWriter<Object> queryResultWriter) {
        this.queryResultWriter = queryResultWriter;

        mongoDBNativeQuery = new MongoDBNativeQuery(dbCollection);

        objectMapper = new ObjectMapper();
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        objectWriter = objectMapper.writer();
    }


    private long startQuery() {
        return System.currentTimeMillis();
    }

    private <T> DataResult<T> endQuery(List result, double start) {
        int numResults = (result != null) ? result.size() : 0;
        return endQuery(result, numResults, start);
    }

    private <T> DataResult<T> endQuery(List result, long numMatches, double start) {
        long end = System.currentTimeMillis();
        int numResults = (result != null) ? result.size() : 0;

        DataResult<T> queryResult = new DataResult((int) (end - start), Collections.emptyList(), numResults, result, numMatches, null);
        return queryResult;
    }

    private DataResult endWrite(long start) {
        return endWrite(1, 0, 1, 0, start);
    }

    private DataResult endWrite(long numMatches, long numUpdated, long start) {
        long end = System.currentTimeMillis();
        return new DataResult((int) (end - start), Collections.emptyList(), numMatches, 0, numUpdated, 0, null);
    }

    private DataResult endWrite(long numMatches, long numInserted, long numUpdated, long numDeleted, long start) {
        long end = System.currentTimeMillis();
        return new DataResult((int) (end - start), Collections.emptyList(), numMatches, numInserted, numUpdated, numDeleted, null);
    }

    public DataResult<Long> count() {
        long start = startQuery();
        long l = mongoDBNativeQuery.count();
        return endQuery(Collections.emptyList(), l, start);
    }

    public DataResult<Long> count(Bson query) {
        return count(null, query);
    }

    public DataResult<Long> count(ClientSession clientSession, Bson query) {
        long start = startQuery();
        long l = mongoDBNativeQuery.count(clientSession, query);
        return endQuery(Collections.emptyList(), l, start);
    }

    public DataResult<String> distinct(String key, Bson query) {
        long start = startQuery();
        List<String> l = new ArrayList<>();
        MongoCursor<BsonValue> iterator = mongoDBNativeQuery.distinct(key, query, BsonValue.class).iterator();
        while (iterator.hasNext()) {
            BsonValue value = iterator.next();
            if (value == null || value.isNull()) {
                l.add(null);
            } else if (value.isString()) {
                l.add(value.asString().getValue());
            } else {
                throw new IllegalArgumentException("Found result with BsonType != " + BsonType.STRING + " : " + value.getBsonType());
            }
        }
        return endQuery(l, start);
    }

    public <T> DataResult<T> distinct(String key, Bson query, Class<T> clazz) {
        if (clazz == null || clazz.equals(String.class)) {
            DataResult<T> result = (DataResult<T>) distinct(key, query);
            result.setResultType(String.class.getName());
            return result;
        }
        long start = startQuery();
        List<T> list = new ArrayList<>();
        MongoCursor iterator = mongoDBNativeQuery.distinct(key, query, clazz).iterator();
        while (iterator.hasNext()) {
            Object next = iterator.next();
            if (next != null) {
                list.add((T) next);
            }
        }
        return endQuery(list, start);
    }

    public DataResult<Document> find(Bson query, QueryOptions options) {
        return privateFind(null, query, null, Document.class, null, options);
    }

    public DataResult<Document> find(ClientSession clientSession, Bson query, QueryOptions options) {
        return privateFind(clientSession, query, null, Document.class, null, options);
    }

    public DataResult<Document> find(Bson query, Bson projection, QueryOptions options) {
        return privateFind(null, query, projection, Document.class, null, options);
    }

    public DataResult<Document> find(ClientSession clientSession, Bson query, Bson projection, QueryOptions options) {
        return privateFind(clientSession, query, projection, Document.class, null, options);
    }

    public <T> DataResult<T> find(Bson query, Bson projection, Class<T> clazz, QueryOptions options) {
        return privateFind(null, query, projection, clazz, null, options);
    }

    public <T> DataResult<T> find(ClientSession clientSession, Bson query, Bson projection, Class<T> clazz, QueryOptions options) {
        return privateFind(clientSession, query, projection, clazz, null, options);
    }

    public <T> DataResult<T> find(Bson query, ComplexTypeConverter<T, Document> converter, QueryOptions options) {
        return privateFind(null, query, null, null, converter, options);
    }

    public <T> DataResult<T> find(ClientSession clientSession, Bson query, ComplexTypeConverter<T, Document> converter,
                                   QueryOptions options) {
        return privateFind(clientSession, query, null, null, converter, options);
    }

    public <T> DataResult<T> find(Bson query, Bson projection, ComplexTypeConverter<T, Document> converter, QueryOptions options) {
        return privateFind(null, query, projection, null, converter, options);
    }

    public <T> DataResult<T> find(ClientSession clientSession, Bson query, Bson projection, ComplexTypeConverter<T, Document> converter,
                                   QueryOptions options) {
        return privateFind(clientSession, query, projection, null, converter, options);
    }

    public List<DataResult<Document>> find(List<? extends Bson> queries, QueryOptions options) {
        return find(queries, null, options);
    }

    public List<DataResult<Document>> find(List<? extends Bson> queries, Bson projection, QueryOptions options) {
        return privateFind(queries, projection, null, null, options);
    }

    public <T> List<DataResult<T>> find(List<? extends Bson> queries, Bson projection, Class<T> clazz, QueryOptions options) {
        return privateFind(queries, projection, clazz, null, options);
    }

    public <T> List<DataResult<T>> find(List<? extends Bson> queries, Bson projection, ComplexTypeConverter<T, Document> converter,
                                         QueryOptions options) {
        return privateFind(queries, projection, null, converter, options);
    }

    public MongoDBIterator<Document> iterator(Bson query, QueryOptions options) {
        return iterator(null, query, null, null, options);
    }

    public <T> MongoDBIterator<T> iterator(Bson query, ComplexTypeConverter<T, Document> converter, QueryOptions options) {
        return iterator(null, query, null, converter, options);
    }

    public MongoDBIterator<Document> iterator(List<Bson> pipeline, QueryOptions options) {
        return mongoDBNativeQuery.aggregate(pipeline, null, options);
    }

    public <T> MongoDBIterator<T> iterator(List<Bson> pipeline, ComplexTypeConverter<T, Document> converter, QueryOptions options) {
        return mongoDBNativeQuery.aggregate(pipeline, converter, options);
    }

    public <T> MongoDBIterator<T> iterator(ClientSession clientSession, Bson query, Bson projection,
                                          ComplexTypeConverter<T, Document> converter, QueryOptions options) {
        return mongoDBNativeQuery.find(clientSession, query, projection, converter, options);
    }

    private <T> DataResult<T> privateFind(ClientSession clientSession, Bson query, Bson projection, Class<T> clazz,
                                           ComplexTypeConverter<T, Document> converter, QueryOptions options) {
        long start = startQuery();
        MongoDBIterator<T> findIterable = iterator(clientSession, query, projection, converter, options);
        DataResult<T> queryResult;
        List<T> list = new LinkedList<>();

        if (findIterable != null) {
            if (queryResultWriter != null) {
                try {
                    queryResultWriter.open();
                    while (findIterable.hasNext()) {
                        queryResultWriter.write(findIterable.next());
                    }
                    queryResultWriter.close();
                } catch (IOException e) {
                    throw new RuntimeException(e.getMessage(), e);
                }
            } else {
                if (converter == null && clazz != null && !clazz.equals(Document.class)) {
                    T document;
                    while (findIterable.hasNext()) {
                        document = findIterable.next();
                        try {
                            list.add(objectMapper.readValue(objectWriter.writeValueAsString(document), clazz));
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                } else {
                    while (findIterable.hasNext()) {
                        list.add(findIterable.next());
                    }
                }
            }

            queryResult = endQuery(list, start);
            findIterable.close();
        } else {
            queryResult = endQuery(list, start);
        }
        queryResult.setNumMatches(findIterable.getNumMatches());
        return queryResult;
    }

    public <T> List<DataResult<T>> privateFind(List<? extends Bson> queries, Bson projection, Class<T> clazz,
                                                ComplexTypeConverter<T, Document> converter, QueryOptions options) {
        List<DataResult<T>> queryResultList = new ArrayList<>(queries.size());
        for (Bson query : queries) {
            DataResult<T> queryResult = privateFind(null, query, projection, clazz, converter, options);
            queryResultList.add(queryResult);
        }
        return queryResultList;
    }

    public DataResult<Document> aggregate(List<? extends Bson> operations, QueryOptions options) {
        return aggregate(operations, null, options);
    }

    public <T> DataResult<T> aggregate(List<? extends Bson> operations, ComplexTypeConverter<T, Document> converter,
                                        QueryOptions options) {

        long start = startQuery();

        DataResult<T> queryResult;
        MongoDBIterator<T> iterator = mongoDBNativeQuery.aggregate(operations, converter, options);
//        MongoCursor<Document> iterator = output.iterator();
        List<T> list = new LinkedList<>();
        if (queryResultWriter != null) {
            try {
                queryResultWriter.open();
                while (iterator.hasNext()) {
                    queryResultWriter.write(iterator.next());
                }
                queryResultWriter.close();
            } catch (IOException e) {
                throw new RuntimeException(e.getMessage(), e);
            }
        } else {
//            if (converter != null) {
//                while (iterator.hasNext()) {
//                    list.add(converter.convertToDataModelType(iterator.next()));
//                }
//            } else {
                while (iterator.hasNext()) {
                    list.add((T) iterator.next());
                }
//            }
        }
        queryResult = endQuery(list, start);
        return queryResult;
    }

    public DataResult insert(Document object, QueryOptions options) {
        return insert(null, object, options);
    }

    public DataResult insert(ClientSession clientSession, Document object, QueryOptions options) {
        long start = startQuery();
        mongoDBNativeQuery.insert(clientSession, object, options);
        return endWrite(start);
    }

    //Bulk insert
    public DataResult insert(List<Document> objects, QueryOptions options) {
        return insert(null, objects, options);
    }

    public DataResult insert(ClientSession clientSession, List<Document> objects, QueryOptions options) {
        long start = startQuery();
        BulkWriteResult writeResult = mongoDBNativeQuery.insert(clientSession, objects, options);
        return endWrite(writeResult.getMatchedCount(), writeResult.getInsertedCount(), 0, 0, start);
    }

    public DataResult update(Bson query, Bson update, QueryOptions options) {
        return update(null, query, update, options);
    }

    public DataResult update(ClientSession clientSession, Bson query, Bson update, QueryOptions options) {
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
            updateResult = mongoDBNativeQuery.replace(clientSession, query, update, upsert);
        } else {
            updateResult = mongoDBNativeQuery.update(clientSession, query, update, upsert, multi);
        }
        return endWrite(updateResult.getMatchedCount(), updateResult.getUpsertedId() != null ? 1 : 0,
                updateResult.getUpsertedId() == null ? updateResult.getModifiedCount() : 0, 0, start);
    }

    //Bulk update
    public DataResult update(List<? extends Bson> queries, List<? extends Bson> updates, QueryOptions options) {
        return update(null, queries, updates, options);
    }

    public DataResult update(ClientSession clientSession, List<? extends Bson> queries, List<? extends Bson> updates,
                                               QueryOptions options) {
        long start = startQuery();

        boolean upsert = false;
        boolean multi = false;
        boolean replace = false;
        if (options != null) {
            upsert = options.getBoolean(UPSERT);
            multi = options.getBoolean(MULTI);
            replace = options.getBoolean(REPLACE);
        }

        com.mongodb.bulk.BulkWriteResult wr;
        if (replace) {
            wr = mongoDBNativeQuery.replace(clientSession, queries, updates, upsert);
        } else {
            wr = mongoDBNativeQuery.update(clientSession, queries, updates, upsert, multi);
        }

        return endWrite(
                wr.getMatchedCount(),
                wr.getInsertedCount() + wr.getUpserts().size(),
                wr.getModifiedCount(),
                wr.getDeletedCount(),
                start);
    }

    public DataResult remove(Bson query, QueryOptions options) {
        return remove(null, query, options);
    }

    public DataResult remove(ClientSession clientSession, Bson query, QueryOptions options) {
        long start = startQuery();
        DeleteResult wr = mongoDBNativeQuery.remove(clientSession, query);
        return endWrite(wr.getDeletedCount(), 0, 0, wr.getDeletedCount(), start);
    }

    //Bulk remove
    public DataResult remove(List<? extends Bson> query, QueryOptions options) {
        return remove(null, query, options);
    }

    public DataResult remove(ClientSession clientSession, List<? extends Bson> query, QueryOptions options) {
        long start = startQuery();

        boolean multi = false;
        if (options != null) {
            multi = options.getBoolean(MULTI);
        }
        com.mongodb.bulk.BulkWriteResult wr = mongoDBNativeQuery.remove(clientSession, query, multi);

        return endWrite(wr.getMatchedCount(), wr.getInsertedCount(), wr.getModifiedCount(), wr.getDeletedCount(), start);
    }

    public DataResult<Document> findAndUpdate(Bson query, Bson projection, Bson sort, Bson update, QueryOptions options) {
        return privateFindAndUpdate(null, query, projection, sort, update, options, null, null);
    }

    public DataResult<Document> findAndUpdate(ClientSession clientSession, Bson query, Bson projection, Bson sort, Bson update,
                                               QueryOptions options) {
        return privateFindAndUpdate(clientSession, query, projection, sort, update, options, null, null);
    }

    public <T> DataResult<T> findAndUpdate(Bson query, Bson projection, Bson sort, Bson update, Class<T> clazz, QueryOptions options) {
        return privateFindAndUpdate(null, query, projection, sort, update, options, clazz, null);
    }

    public <T> DataResult<T> findAndUpdate(ClientSession clientSession, Bson query, Bson projection, Bson sort, Bson update,
                                            Class<T> clazz, QueryOptions options) {
        return privateFindAndUpdate(clientSession, query, projection, sort, update, options, clazz, null);
    }

    private <T> DataResult<T> privateFindAndUpdate(ClientSession clientSession, Bson query, Bson projection, Bson sort, Bson update,
                                                    QueryOptions options, Class<T> clazz, ComplexTypeConverter<T, Document> converter) {
        long start = startQuery();
        Document result = mongoDBNativeQuery.findAndUpdate(clientSession, query, projection, sort, update, options);
        if (clazz != null && !clazz.equals(Document.class)) {
            try {
                return endQuery(Collections.singletonList(objectMapper.readValue(objectWriter.writeValueAsString(result), clazz)), start);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return endQuery(Collections.singletonList(result), start);
    }

    public DataResult<Document> findAndModify(ClientSession clientSession, Bson query, Bson fields, Bson sort, Document update,
                                               QueryOptions options) {
        return privateFindAndModify(clientSession, query, fields, sort, update, options, null, null);
    }

    public DataResult<Document> findAndModify(Bson query, Bson fields, Bson sort, Document update, QueryOptions options) {
        return privateFindAndModify(null, query, fields, sort, update, options, null, null);
    }

    public <T> DataResult<T> findAndModify(ClientSession clientSession, Bson query, Bson fields, Bson sort, Document update,
                                            QueryOptions options, Class<T> clazz) {
        return privateFindAndModify(clientSession, query, fields, sort, update, options, clazz, null);
    }

    public <T> DataResult<T> findAndModify(Bson query, Bson fields, Bson sort, Document update, QueryOptions options, Class<T> clazz) {
        return privateFindAndModify(null, query, fields, sort, update, options, clazz, null);
    }

    public <T> DataResult<T> findAndModify(ClientSession clientSession, Bson query, Bson fields, Bson sort, Document update,
                                            QueryOptions options, ComplexTypeConverter<T, Document> converter) {
        return privateFindAndModify(clientSession, query, fields, sort, update, options, null, converter);
    }

    public <T> DataResult<T> findAndModify(Bson query, Bson fields, Bson sort, Document update, QueryOptions options,
                                            ComplexTypeConverter<T, Document> converter) {
        return privateFindAndModify(null, query, fields, sort, update, options, null, converter);
    }

    private <T> DataResult<T> privateFindAndModify(ClientSession clientSession, Bson query, Bson fields, Bson sort, Document update,
                                                    QueryOptions options, Class<T> clazz, ComplexTypeConverter<T, Document> converter) {
        long start = startQuery();
        Object result = mongoDBNativeQuery.findAndModify(clientSession, query, fields, sort, update, options);
        return endQuery(Collections.singletonList(result), start);
    }

    public DataResult createIndex(Bson keys, ObjectMap options) {
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
        DataResult dataResult = endQuery(Collections.emptyList(), start);
        return dataResult;
    }

    public DataResult dropIndex(Bson keys) {
        long start = startQuery();
        mongoDBNativeQuery.dropIndex(keys);
        DataResult dataResult = endQuery(Collections.emptyList(), start);
        return dataResult;
    }

    public DataResult<Document> getIndex() {
        long start = startQuery();
        List<Document> index = mongoDBNativeQuery.getIndex();
        DataResult<Document> queryResult = endQuery(index, start);
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
    @Deprecated
    public MongoDBNativeQuery nativeQuery() {
        return mongoDBNativeQuery;
    }

    @Override
    public String toString() {
        return "MongoDBCollection{"
                + "dbCollection=" + getFullName() + '}';
    }

    private String getFullName() {
        return mongoDBNativeQuery.getDbCollection().getNamespace().getFullName();
    }

    public MongoDBCollection withWriteConcern(WriteConcern writeConcern) {
        mongoDBNativeQuery = new MongoDBNativeQuery(mongoDBNativeQuery.getDbCollection().withWriteConcern(writeConcern));
        return this;
    }

    public MongoDBCollection withReadPreference(ReadPreference readPreference) {
        mongoDBNativeQuery = new MongoDBNativeQuery(mongoDBNativeQuery.getDbCollection().withReadPreference(readPreference));
        return this;
    }

}
