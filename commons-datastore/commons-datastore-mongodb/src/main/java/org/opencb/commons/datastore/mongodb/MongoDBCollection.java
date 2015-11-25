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

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.mongodb.*;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.TimeUnit;

import org.opencb.commons.datastore.core.ComplexTypeConverter;
import org.opencb.commons.datastore.core.QueryOptions;
import org.opencb.commons.datastore.core.QueryResult;
import org.opencb.commons.datastore.core.QueryResultWriter;

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

    public static final String TIMEOUT = "timeout";
    public static final String SKIP_COUNT = "skipCount";
    public static final String BATCH_SIZE = "batchSize";
    public static final String ELEM_MATCH = "elemMatch";

    public static final String UPSERT = "upsert";
    public static final String MULTI = "multi";

    private DBCollection dbCollection;

    private long start;
    private long end;

    private MongoDBNativeQuery mongoDBNativeQuery;
    private QueryResultWriter<DBObject> queryResultWriter;

    private ObjectMapper objectMapper;
    private ObjectWriter objectWriter;

    MongoDBCollection(DBCollection dbCollection) {
        this(dbCollection, null);
    }

    MongoDBCollection(DBCollection dbCollection, QueryResultWriter<DBObject> queryResultWriter) {
        this.dbCollection = dbCollection;
        this.queryResultWriter = queryResultWriter;

        mongoDBNativeQuery = new MongoDBNativeQuery(dbCollection);

        objectMapper = new ObjectMapper();
        objectWriter = objectMapper.writer();
    }


    private void startQuery() {
        start = System.currentTimeMillis();
    }

    private <T> QueryResult<T> endQuery(List result) {
        int numResults = (result != null) ? result.size() : 0;
        return endQuery(result, numResults);
    }

    private <T> QueryResult<T> endQuery(List result, int numTotalResults) {
        end = System.currentTimeMillis();
        int numResults = (result != null) ? result.size() : 0;

        QueryResult<T> queryResult = new QueryResult(null, (int) (end-start), numResults, numTotalResults, null, null, result);
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
        startQuery();
        long l = mongoDBNativeQuery.count();
        return endQuery(Arrays.asList(l));
    }

    public QueryResult<Long> count(DBObject query) {
        startQuery();
        long l = mongoDBNativeQuery.count(query);
        return endQuery(Arrays.asList(l));
    }



    public QueryResult<Object> distinct(String key, DBObject query) {
        startQuery();
        List<Object> l = mongoDBNativeQuery.distinct(key, query);
        return endQuery(l);
    }

    public <T> QueryResult<T> distinct(String key, DBObject query, Class<T> clazz) {
        startQuery();
        List<T> l = mongoDBNativeQuery.distinct(key, query);
        return endQuery(l);
    }

    public <T, O> QueryResult<T> distinct(String key, DBObject query, ComplexTypeConverter< T, O> converter) {
        startQuery();
        List<O> distinct = mongoDBNativeQuery.distinct(key, query);

        List<T> convertedresultList = new ArrayList<>(distinct.size());
        for (O o : distinct) {
            convertedresultList.add(converter.convertToDataModelType(o));
        }
        return endQuery(convertedresultList);
    }



    public QueryResult<DBObject> find(DBObject query, QueryOptions options) {
        return _find(query, null, DBObject.class, null, options);
    }

    public QueryResult<DBObject> find(DBObject query, DBObject projection, QueryOptions options) {
        return _find(query, projection, DBObject.class, null, options);
    }

    public <T> QueryResult<T> find(DBObject query, DBObject projection, Class<T> clazz, QueryOptions options) {
        return _find(query, projection, clazz, null, options);
    }

    public <T> QueryResult<T> find(DBObject query, DBObject projection, ComplexTypeConverter<T, DBObject> converter,
                                   QueryOptions options) {
        return _find(query, projection, null, converter, options);
    }


    public List<QueryResult<DBObject>> find(List<DBObject> queries, QueryOptions options) {
        return find(queries, null, options);
    }

    public List<QueryResult<DBObject>> find(List<DBObject> queries, DBObject projection, QueryOptions options) {
        return _find(queries, projection, null, null, options);
    }

    public <T> List<QueryResult<T>> find(List<DBObject> queries, DBObject projection, Class<T> clazz,
                                         QueryOptions options) {
        return _find(queries, projection, clazz, null, options);
    }

    public <T> List<QueryResult<T>> find(List<DBObject> queries, DBObject projection,
                                         ComplexTypeConverter<T, DBObject> converter, QueryOptions options) {
        return _find(queries, projection, null, converter, options);
    }

    public <T> List<QueryResult<T>> _find(List<DBObject> queries, DBObject projection, Class<T> clazz,
                                          ComplexTypeConverter<T, DBObject> converter, QueryOptions options) {
        List<QueryResult<T>> queryResultList = new ArrayList<>(queries.size());
        for(DBObject query: queries) {
            QueryResult<T> queryResult = _find(query, projection, clazz, converter, options);
            queryResultList.add(queryResult);
        }
        return  queryResultList;
    }

    private <T> QueryResult<T> _find(DBObject query, DBObject projection, Class<T> clazz,
                                     ComplexTypeConverter<T, DBObject> converter, QueryOptions options) {
        startQuery();

        /**
         * Getting the cursor and setting the batchSize from options. Default value set to 20.
         */
        DBCursor cursor = mongoDBNativeQuery.find(query, projection, options);

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
                    queryResult = endQuery(null);
                    queryResult.setErrorMsg(e.getMessage() + " " + Arrays.toString(e.getStackTrace()));
                    return queryResult;
                }
            } else {
                if(converter != null) {
                    while (cursor.hasNext()) {
                        list.add(converter.convertToDataModelType(cursor.next()));
                    }
                }else {
                    if(clazz != null && !clazz.equals(DBObject.class)) {
                        DBObject dbObject = null;
                        while (cursor.hasNext()) {
                            dbObject = cursor.next();
                            try {
                                list.add(objectMapper.readValue(dbObject.toString(), clazz));
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
                        numTotalResults = cursor.maxTime(options.getInt("countTimeout"), TimeUnit.MILLISECONDS).count();
                    } catch (MongoExecutionTimeoutException e) {
                        numTotalResults = -1;
                    }
                }
                queryResult = endQuery(list, numTotalResults);
            } else {
                queryResult = endQuery(list);
            }
            cursor.close();
        } else {
            queryResult = endQuery(list);
        }

        return queryResult;
    }


    public QueryResult<DBObject> aggregate(List<DBObject> operations, QueryOptions options) {
        startQuery();
        QueryResult<DBObject> queryResult;
        AggregationOutput output = mongoDBNativeQuery.aggregate(operations, options);
        Iterator<DBObject> iterator = output.results().iterator();
        List<DBObject> list = new LinkedList<>();
        if (queryResultWriter != null) {
            try {
                queryResultWriter.open();
                while (iterator.hasNext()) {
                    queryResultWriter.write(iterator.next());
                }
                queryResultWriter.close();
            } catch (IOException e) {
                queryResult = endQuery(list);
                queryResult.setErrorMsg(e.getMessage() + " " + Arrays.toString(e.getStackTrace()));
                return queryResult;
            }
        } else {
            while (iterator.hasNext()) {
                list.add(iterator.next());
            }
        }
        queryResult = endQuery(list);
        return queryResult;
    }


    public QueryResult<WriteResult> insert(DBObject object, QueryOptions options) {
        startQuery();
        WriteResult wr = mongoDBNativeQuery.insert(object, options);
        QueryResult<WriteResult> queryResult = endQuery(Arrays.asList(wr));
        if (!wr.getLastError().ok()) {
            queryResult.setErrorMsg(wr.getLastError().getErrorMessage());
        }
        return queryResult;
    }

    //Bulk insert
    public QueryResult<BulkWriteResult> insert(List<DBObject> objects, QueryOptions options) {
        startQuery();
        BulkWriteResult writeResult = mongoDBNativeQuery.insert(objects, options);
        QueryResult<BulkWriteResult> queryResult = endQuery(Collections.singletonList(writeResult));
        return queryResult;
    }


    public QueryResult<WriteResult> update(DBObject query, DBObject update, QueryOptions options) {
        startQuery();

        boolean upsert = false;
        boolean multi = false;
        if(options != null) {
            upsert = options.getBoolean(UPSERT);
            multi = options.getBoolean(MULTI);
        }

        WriteResult wr = mongoDBNativeQuery.update(query, update, upsert, multi);
        QueryResult<WriteResult> queryResult = endQuery(Arrays.asList(wr));
        if (!wr.getLastError().ok()) {
            queryResult.setErrorMsg(wr.getLastError().getErrorMessage());
        }
        return queryResult;
    }

    //Bulk update
    public QueryResult<BulkWriteResult> update(List<DBObject> queries, List<DBObject> updates, QueryOptions options) {
        startQuery();

        boolean upsert = false;
        boolean multi = false;
        if(options != null) {
            upsert = options.getBoolean(UPSERT);
            multi = options.getBoolean(MULTI);
        }

        BulkWriteResult wr = mongoDBNativeQuery.update(queries, updates, upsert, multi);
        QueryResult<BulkWriteResult> queryResult = endQuery(Arrays.asList(wr));
        return queryResult;
    }


    public QueryResult<WriteResult> remove(DBObject query, QueryOptions options) {
        startQuery();
        WriteResult wr = mongoDBNativeQuery.remove(query);
        QueryResult<WriteResult> queryResult = endQuery(Arrays.asList(wr));
        if (!wr.getLastError().ok()) {
            queryResult.setErrorMsg(wr.getLastError().getErrorMessage());
        }
        return queryResult;
    }

    //Bulk remove
    public QueryResult<BulkWriteResult> remove(List<DBObject> query, QueryOptions options) {
        startQuery();

        boolean multi = false;
        if(options != null) {
            multi = options.getBoolean(MULTI);
        }
        BulkWriteResult wr = mongoDBNativeQuery.remove(query, multi);
        QueryResult<BulkWriteResult> queryResult = endQuery(Arrays.asList(wr));

        return queryResult;
    }



    public QueryResult<DBObject> findAndModify(DBObject query, DBObject fields, DBObject sort, DBObject update,
                                               QueryOptions options) {
        return _findAndModify(query, fields, sort, update, options, null, null);
    }

    public <T> QueryResult<T> findAndModify(DBObject query, DBObject fields, DBObject sort, DBObject update,
                                            QueryOptions options, Class<T> clazz) {
        return _findAndModify(query, fields, sort, update, options, clazz, null);
    }

    public <T> QueryResult<T> findAndModify(DBObject query, DBObject fields, DBObject sort, DBObject update,
                                            QueryOptions options, ComplexTypeConverter<T, DBObject> converter) {
        return _findAndModify(query, fields, sort, update, options, null, converter);
    }

    private <T> QueryResult<T> _findAndModify(DBObject query, DBObject fields, DBObject sort, DBObject update,
                                              QueryOptions options, Class<T> clazz, ComplexTypeConverter<T, DBObject> converter) {
        startQuery();
        DBObject result = mongoDBNativeQuery.findAndModify(query, fields, sort, update, options);
        QueryResult<T> queryResult = endQuery(Arrays.asList(result));

        return queryResult;
    }



    public QueryResult createIndex(DBObject keys, DBObject options) {
        startQuery();
        mongoDBNativeQuery.createIndex(keys, options);
        QueryResult queryResult = endQuery(Collections.emptyList());
        return queryResult;
    }

    public QueryResult dropIndex(DBObject keys) {
        startQuery();
        mongoDBNativeQuery.dropIndex(keys);
        QueryResult queryResult = endQuery(Collections.emptyList());
        return queryResult;
    }

    public QueryResult<DBObject> getIndex() {
        startQuery();
        List<DBObject> index = mongoDBNativeQuery.getIndex();
        QueryResult<DBObject> queryResult = endQuery(index);
        return queryResult;
    }



    public QueryResultWriter<DBObject> getQueryResultWriter() {
        return queryResultWriter;
    }

    public void setQueryResultWriter(QueryResultWriter<DBObject> queryResultWriter) {
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
