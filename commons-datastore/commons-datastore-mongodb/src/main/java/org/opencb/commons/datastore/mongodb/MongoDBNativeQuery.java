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

import com.mongodb.MongoExecutionTimeoutException;
import com.mongodb.WriteConcern;
import com.mongodb.bulk.BulkWriteResult;
import com.mongodb.client.*;
import com.mongodb.client.model.*;
import com.mongodb.client.result.DeleteResult;
import com.mongodb.client.result.UpdateResult;
import org.bson.Document;
import org.bson.conversions.Bson;
import org.opencb.commons.datastore.core.ComplexTypeConverter;
import org.opencb.commons.datastore.core.QueryOptions;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.*;

import static org.opencb.commons.datastore.mongodb.MongoDBQueryUtils.getProjection;
import static org.opencb.commons.datastore.mongodb.MongoDBQueryUtils.parseQueryOptions;


public class MongoDBNativeQuery {

    private final MongoCollection<Document> dbCollection;


    MongoDBNativeQuery(MongoCollection<Document> dbCollection) {
        this.dbCollection = dbCollection;
    }

    MongoCollection<Document> getDbCollection() {
        return dbCollection;
    }

    public long count() {
        return dbCollection.countDocuments();
    }

    public long count(Bson query) {
        return count(null, query);
    }

    public long count(ClientSession clientSession, Bson query) {
        if (clientSession != null) {
            return dbCollection.countDocuments(clientSession, query);
        } else {
            return dbCollection.countDocuments(query);
        }
    }

    public DistinctIterable<Document> distinct(String key) {
        return distinct(key, null, Document.class);
    }

    public <T> DistinctIterable<T> distinct(String key, Class<T> resultClass) {
        return distinct(key, null, resultClass);
    }

    public <T> DistinctIterable<T> distinct(String key, Bson query, Class<T> resultClass) {
        return dbCollection.distinct(key, query, resultClass);
    }


    public MongoDBIterator<Document> find(Bson query, QueryOptions options) {
        return find(null, query, null, options);
    }

    public MongoDBIterator<Document> find(ClientSession clientSession, Bson query, QueryOptions options) {
        return find(clientSession, query, null, options);
    }

    public MongoDBIterator<Document> aggregate(List<? extends Bson> operations) {
        return aggregate(null, operations, null, null);
    }

//    public AggregateIterable<Document> aggregate(List<? extends Bson> operations, QueryOptions options) {
//        // we need to be sure that the List is mutable
//        List<Bson> bsonOperations = new ArrayList<>(operations);
//        parseQueryOptions(bsonOperations, options);
//        return (bsonOperations.size() > 0) ? dbCollection.aggregate(bsonOperations) : null;
//    }

    public <T> MongoDBIterator<T> aggregate(List<? extends Bson> operations, ComplexTypeConverter<T, Document> converter,
                                            QueryOptions options) {
        return aggregate(null, operations, converter, options);
    }

    public <T> MongoDBIterator<T> aggregate(ClientSession clientSession, List<? extends Bson> operations,
                                            ComplexTypeConverter<T, Document> converter, QueryOptions options) {
        Future<AggregateIterable<Document>> countResults = null;
        if (options != null && options.getBoolean(QueryOptions.COUNT)) {
            ExecutorService executor = Executors.newSingleThreadExecutor();
            List<Bson> countOperations = new ArrayList<>(operations);
            countOperations.add(Aggregates.count("id"));
            if (clientSession != null) {
                countResults = executor.submit(() -> dbCollection.aggregate(clientSession, countOperations));
            } else {
                countResults = executor.submit(() -> dbCollection.aggregate(countOperations));
            }
        }

        // we need to be sure that the List is mutable
        List<Bson> bsonOperations = new ArrayList<>(operations);
        parseQueryOptions(bsonOperations, options);
        MongoDBIterator<T> iterator = null;
        if (bsonOperations.size() > 0) {
            long numMatches = -1;
            if (options != null && options.getBoolean(QueryOptions.COUNT) && countResults != null) {
                try {
                    if (countResults.get().iterator().hasNext()) {
                        Document results = countResults.get().iterator().next();
                        numMatches = results.getInteger("count", -1);
                    }
                } catch (MongoExecutionTimeoutException | InterruptedException | ExecutionException e) {
                    // ignore error, just return default count of -1
                }
            }
            if (clientSession != null) {
                iterator = new MongoDBIterator<T>(dbCollection.aggregate(clientSession, bsonOperations).iterator(),
                        converter, numMatches);
            } else {
                iterator = new MongoDBIterator<T>(dbCollection.aggregate(bsonOperations).iterator(), converter,
                        numMatches);
            }
        }

        return iterator;
    }


    public MongoDBIterator<Document> find(Bson query, Bson projection, QueryOptions options) {
        return find(null, query, projection, options);
    }

    public FindIterable<Document> nativeFind(ClientSession clientSession, Bson query, Bson projection,
                                             QueryOptions options) {
        if (projection == null) {
            // FIXME projecttion is alwys null, why is it passed as parameter?
            projection = getProjection(projection, options);
        }

        FindIterable<Document> findIterable;
        if (clientSession != null) {
            findIterable = dbCollection.find(clientSession, query).projection(projection);
        } else {
            findIterable = dbCollection.find(query).projection(projection);
        }

        int limit = (options != null) ? options.getInt(QueryOptions.LIMIT, 0) : 0;
        if (limit > 0) {
            findIterable.limit(limit);
        }

        int skip = (options != null) ? options.getInt(QueryOptions.SKIP, 0) : 0;
        if (skip > 0) {
            findIterable.skip(skip);
        }

        Object sortObject = (options != null) ? options.get(QueryOptions.SORT) : null;
        if (sortObject != null) {
            if (sortObject instanceof Bson) {
                findIterable.sort(((Bson) sortObject));
            } else if (sortObject instanceof String) {
                String order = options.getString(QueryOptions.ORDER, "DESC");
                if (order.equalsIgnoreCase(QueryOptions.ASCENDING) || order.equalsIgnoreCase("ASC")
                        || order.equals("1")) {
                    findIterable.sort(Sorts.ascending(((String) sortObject)));
                } else {
                    findIterable.sort(Sorts.descending(((String) sortObject)));
                }
            } else if (sortObject instanceof Collection) {
                List<String> fieldList = options.getAsStringList(QueryOptions.SORT);
                List<Bson> sortedList = new ArrayList<>(fieldList.size());
                for (String field : fieldList) {
                    String[] fieldArray = field.split(":");
                    String order = null;
                    String sortField = null;
                    if (fieldArray.length == 2) {
                        sortField = fieldArray[0];
                        order = fieldArray[1];
                    } else if (fieldArray.length == 1) {
                        sortField = field;
                        order = options.getString(QueryOptions.ORDER, "DESC");
                    }
                    if (QueryOptions.ASCENDING.equalsIgnoreCase(order) || "ASC".equalsIgnoreCase(order)
                            || "1".equals(order)) {
                        sortedList.add(Sorts.ascending(sortField));
                    } else {
                        sortedList.add(Sorts.descending(sortField));
                    }
                }

                findIterable.sort(Sorts.orderBy(sortedList));
            }
        }

        if (options != null && options.containsKey(MongoDBCollection.BATCH_SIZE)) {
            findIterable.batchSize(options.getInt(MongoDBCollection.BATCH_SIZE, 20));
        }

        if (options != null && options.containsKey(QueryOptions.TIMEOUT)) {
            findIterable.maxTime(options.getLong(QueryOptions.TIMEOUT), TimeUnit.MILLISECONDS);
        }

        if (options != null && options.getBoolean(MongoDBCollection.NO_CURSOR_TIMEOUT)) {
            findIterable.noCursorTimeout(true);
        }

        return findIterable;
    }

    public MongoDBIterator<Document> find(ClientSession clientSession, Bson query, Bson projection,
                                          QueryOptions options) {
        return find(clientSession, query, projection, null, options);
    }

    public <T> MongoDBIterator<T> find(ClientSession clientSession, Bson query, Bson projection,
                                          ComplexTypeConverter<T, Document> converter, QueryOptions options) {
        Future<Long> countFuture = null;
        if (options != null && options.getBoolean(QueryOptions.COUNT)) {
            ExecutorService executor = Executors.newSingleThreadExecutor();
            countFuture = executor.submit(() -> count(clientSession, query));
        }

        FindIterable<Document> findIterable = null;
        if (options == null || options.getInt(QueryOptions.LIMIT, Integer.MAX_VALUE) > 0) {
            findIterable = nativeFind(clientSession, query, projection, options);
        }

        long numMatches = -1;
        if (options != null && options.getBoolean(QueryOptions.COUNT)) {
            try {
                numMatches = countFuture.get();
            } catch (MongoExecutionTimeoutException | InterruptedException | ExecutionException e) {
                // ignore error, just return default count of -1
            }
        }
        return new MongoDBIterator<T>(findIterable != null ? findIterable.iterator() : null, converter, numMatches);
    }

    public Document explain(Bson query, Bson projection, QueryOptions options) {
        return nativeFind(null, query, projection, options)
                .explain();
    }

    /**
     * This method insert a single document into a collection. Params w and wtimeout are read from QueryOptions.
     *
     * @param document The new document to be inserted
     * @param options  Some options like timeout
     */
    public void insert(Document document, QueryOptions options) {
        insert(null, document, options);
    }

    /**
     * This method insert a single document into a collection. Params w and wtimeout are read from QueryOptions.
     *
     * @param clientSession Session in which the insert will be performed. Can be null.
     * @param document The new document to be inserted
     * @param options  Some options like timeout
     */
    public void insert(ClientSession clientSession, Document document, QueryOptions options) {
        int writeConcern = 1;
        int ms = 0;
        if (options != null && (options.containsKey("w") || options.containsKey("wtimeout"))) {
            // Some info about params: http://api.mongodb.org/java/current/com/mongodb/WriteConcern.html
//            return dbCollection.insert(dbObject, new WriteConcern(options.getInt("w", 1), options.getInt("wtimeout", 0)));
            writeConcern = options.getInt("w", 1);
            ms = options.getInt("wtimeout", 0);
        }
        dbCollection.withWriteConcern(new WriteConcern(writeConcern, ms));
        if (clientSession != null) {
            dbCollection.insertOne(clientSession, document);
        } else {
            dbCollection.insertOne(document);
        }
    }

    /**
     * This method insert a list of documents into a collection. Params w and wtimeout are read from QueryOptions.
     *
     * @param documentList The new list of documents to be inserted
     * @param options      Some options like timeout
     * @return A BulkWriteResult from MongoDB API
     */
    public BulkWriteResult insert(List<Document> documentList, QueryOptions options) {
        return insert(null, documentList, options);
    }

    /**
     * This method insert a list of documents into a collection. Params w and wtimeout are read from QueryOptions.
     *
     * @param clientSession Session in which the insert will be performed. Can be null.
     * @param documentList The new list of documents to be inserted
     * @param options      Some options like timeout
     * @return A BulkWriteResult from MongoDB API
     */
    public BulkWriteResult insert(ClientSession clientSession, List<Document> documentList, QueryOptions options) {
        List<WriteModel<Document>> actions = new ArrayList<>(documentList.size());
        for (Document document : documentList) {
            actions.add(new InsertOneModel<>(document));
        }

        int writeConcern = 1;
        int ms = 0;
        if (options != null && (options.containsKey("w") || options.containsKey("wtimeout"))) {
            writeConcern = options.getInt("w", 1);
            ms = options.getInt("wtimeout", 0);
        }
        dbCollection.withWriteConcern(new WriteConcern(writeConcern, ms));

        if (clientSession != null) {
            return dbCollection.bulkWrite(clientSession, actions, new BulkWriteOptions().ordered(false));
        } else {
            return dbCollection.bulkWrite(actions, new BulkWriteOptions().ordered(false));
        }
    }

    public UpdateResult update(Bson query, Bson updates, boolean upsert, boolean multi) {
        return update(null, query, updates, upsert, multi);
    }

    public UpdateResult update(ClientSession clientSession, Bson query, Bson updates, boolean upsert, boolean multi) {
        UpdateOptions updateOptions = new UpdateOptions().upsert(upsert);
        if (multi) {
            if (clientSession != null) {
                return dbCollection.updateMany(clientSession, query, updates, updateOptions);
            } else {
                return dbCollection.updateMany(query, updates, updateOptions);
            }
        } else {
            if (clientSession != null) {
                return dbCollection.updateOne(clientSession, query, updates, updateOptions);
            } else {
                return dbCollection.updateOne(query, updates, updateOptions);
            }
        }
    }

    public BulkWriteResult replace(List<? extends Bson> queries, List<? extends Bson> updates, boolean upsert) {
        return replace(null, queries, updates, upsert);
    }

    public BulkWriteResult replace(ClientSession clientSession, List<? extends Bson> queries,
                                   List<? extends Bson> updates, boolean upsert) {
        if (queries.size() != updates.size()) {
            throw wrongQueryUpdateSize(queries, updates);
        }

        Iterator<? extends Bson> queryIterator = queries.iterator();
        Iterator<? extends Bson> updateIterator = updates.iterator();

        List<WriteModel<Document>> actions = new ArrayList<>(queries.size());
        ReplaceOptions replaceOptions = new ReplaceOptions().upsert(upsert);


        while (queryIterator.hasNext()) {
            Bson query = queryIterator.next();
            Bson update = updateIterator.next();

            actions.add(new ReplaceOneModel<>(query, (Document) update, replaceOptions));
        }

        if (clientSession != null) {
            return dbCollection.bulkWrite(clientSession, actions, new BulkWriteOptions().ordered(false));
        } else {
            return dbCollection.bulkWrite(actions, new BulkWriteOptions().ordered(false));
        }
    }

    public UpdateResult replace(Bson query, Bson updates, boolean upsert) {
        return replace(null, query, updates, upsert);
    }

    public UpdateResult replace(ClientSession clientSession, Bson query, Bson updates, boolean upsert) {
        ReplaceOptions replaceOptions = new ReplaceOptions().upsert(upsert);
        if (clientSession != null) {
            return dbCollection.replaceOne(clientSession, query, (Document) updates, replaceOptions);
        } else {
            return dbCollection.replaceOne(query, (Document) updates, replaceOptions);
        }
    }

    public BulkWriteResult update(List<? extends Bson> documentList, List<? extends Bson> updatesList, boolean upsert,
                                  boolean multi) {
        return update(null, documentList, updatesList, upsert, multi);
    }

    public BulkWriteResult update(ClientSession clientSession, List<? extends Bson> documentList,
                                  List<? extends Bson> updatesList, boolean upsert, boolean multi) {
        if (documentList.size() != updatesList.size()) {
            throw wrongQueryUpdateSize(documentList, updatesList);
        }

        Iterator<? extends Bson> queryIterator = documentList.iterator();
        Iterator<? extends Bson> updateIterator = updatesList.iterator();

        List<WriteModel<Document>> actions = new ArrayList<>(documentList.size());
        UpdateOptions updateOptions = new UpdateOptions().upsert(upsert);

        while (queryIterator.hasNext()) {
            Bson query = queryIterator.next();
            Bson update = updateIterator.next();

            if (multi) {
                actions.add(new UpdateManyModel<>(query, update, updateOptions));
            } else {
                actions.add(new UpdateOneModel<>(query, update, updateOptions));
            }


//        BulkWriteOperation bulk = dbCollection.initializeUnorderedBulkOperation();
//            BulkWriteRequestBuilder builder = bulk.find(query);
//            if (upsert) {
//                if (multi) {
////                    builder.upsert().update(update);
//
//                } else {
////                    builder.upsert().updateOne(update);
//                }
//            } else {
//                if (multi) {
////                    builder.update(update);
//                } else {
////                    builder.updateOne(update);
//                }
//            }


        }
//        return bulk.execute();
        if (clientSession != null) {
            return dbCollection.bulkWrite(clientSession, actions, new BulkWriteOptions().ordered(false));
        } else {
            return dbCollection.bulkWrite(actions, new BulkWriteOptions().ordered(false));
        }
    }

    private IndexOutOfBoundsException wrongQueryUpdateSize(List<? extends Bson> queries, List<? extends Bson> updates) {
        return new IndexOutOfBoundsException("QueryList.size=" + queries.size()
                + " and UpdatesList.size=" + updates.size() + " must be the same size.");
    }

    public DeleteResult remove(Bson query) {
        return remove(null, query);
    }

    public DeleteResult remove(ClientSession clientSession, Bson query) {
        if (clientSession != null) {
            return dbCollection.deleteMany(clientSession, query);
        } else {
            return dbCollection.deleteMany(query);
        }
    }

    public DeleteResult remove(Bson query, boolean multi) {
        return remove(null, query, multi);
    }

    public DeleteResult remove(ClientSession clientSession, Bson query, boolean multi) {
        if (multi) {
            if (clientSession != null) {
                return dbCollection.deleteMany(clientSession, query);
            } else {
                return dbCollection.deleteMany(query);
            }
        } else {
            if (clientSession != null) {
                return dbCollection.deleteOne(clientSession, query);
            } else {
                return dbCollection.deleteOne(query);
            }
        }
    }

    public BulkWriteResult remove(List<? extends Bson> queryList, boolean multi) {
        return remove(null, queryList, multi);
    }

    public BulkWriteResult remove(ClientSession clientSession, List<? extends Bson> queryList, boolean multi) {
        List<WriteModel<Document>> actions = new ArrayList<>(queryList.size());
        if (multi) {
            for (Bson document : queryList) {
                actions.add(new DeleteManyModel<>(document));
            }
        } else {
            for (Bson document : queryList) {
                actions.add(new DeleteOneModel<>(document));
            }
        }
        if (clientSession != null) {
            return dbCollection.bulkWrite(clientSession, actions, new BulkWriteOptions().ordered(false));
        } else {
            return dbCollection.bulkWrite(actions, new BulkWriteOptions().ordered(false));
        }
    }


    public Document findAndUpdate(Bson query, Bson projection, Bson sort, Bson update, QueryOptions options) {
        return findAndUpdate(null, query, projection, sort, update, options);
    }

    public Document findAndUpdate(ClientSession clientSession, Bson query, Bson projection, Bson sort, Bson update,
                                  QueryOptions options) {
        boolean upsert = false;
        boolean returnNew = false;

        if (options != null) {
            if (projection == null) {
                projection = getProjection(projection, options);
            }
            upsert = options.getBoolean("upsert", false);
            returnNew = options.getBoolean("returnNew", false);
        }

        FindOneAndUpdateOptions findOneAndUpdateOptions = new FindOneAndUpdateOptions()
                .sort(sort)
                .projection(projection)
                .upsert(upsert)
                .returnDocument(returnNew ? ReturnDocument.AFTER : ReturnDocument.BEFORE);

        if (clientSession != null) {
            return dbCollection.findOneAndUpdate(clientSession, query, update, findOneAndUpdateOptions);
        } else {
            return dbCollection.findOneAndUpdate(query, update, findOneAndUpdateOptions);
        }
    }

    public Document findAndModify(Bson query, Bson projection, Bson sort, Document update, QueryOptions options) {
        return findAndModify(null, query, projection, sort, update, options);
    }

    public Document findAndModify(ClientSession clientSession, Bson query, Bson projection, Bson sort, Document update,
                                  QueryOptions options) {
        boolean remove = false;
        boolean upsert = false;
        boolean returnNew = false;

        if (options != null) {
            if (projection == null) {
                projection = getProjection(projection, options);
            }
            remove = options.getBoolean("remove", false);
            upsert = options.getBoolean("upsert", false);
            returnNew = options.getBoolean("returnNew", false);
        }

        if (remove) {
            FindOneAndReplaceOptions findOneAndReplaceOptions = new FindOneAndReplaceOptions()
                    .sort(sort)
                    .projection(projection)
                    .upsert(upsert)
                    .returnDocument(returnNew ? ReturnDocument.AFTER : ReturnDocument.BEFORE);
            if (clientSession != null) {
                return dbCollection.findOneAndReplace(clientSession, query, update, findOneAndReplaceOptions);
            } else {
                return dbCollection.findOneAndReplace(query, update, findOneAndReplaceOptions);
            }
        } else {
            FindOneAndUpdateOptions findOneAndUpdateOptions = new FindOneAndUpdateOptions()
                    .sort(sort)
                    .projection(projection)
                    .upsert(upsert)
                    .returnDocument(returnNew ? ReturnDocument.AFTER : ReturnDocument.BEFORE);
            if (clientSession != null) {
                return dbCollection.findOneAndUpdate(clientSession, query, update, findOneAndUpdateOptions);
            } else {
                return dbCollection.findOneAndUpdate(query, update, findOneAndUpdateOptions);
            }
        }
//        return dbCollection.findOneAndUpdate(query, projection, sort, remove, update, returnNew, upsert);
    }

    public void createIndex(Bson keys, IndexOptions options) {
        dbCollection.createIndex(keys, options);
    }

    public List<Document> getIndex() {
        return dbCollection.listIndexes().into(new ArrayList<>());
    }

    public void dropIndex(Bson keys) {
        dbCollection.dropIndex(keys);
    }

    public void dropIndexes() {
        dbCollection.dropIndexes();
    }

}
