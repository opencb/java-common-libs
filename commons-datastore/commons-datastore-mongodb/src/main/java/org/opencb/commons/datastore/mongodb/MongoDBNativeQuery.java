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

import com.mongodb.WriteConcern;
import com.mongodb.bulk.BulkWriteResult;
import com.mongodb.client.AggregateIterable;
import com.mongodb.client.DistinctIterable;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.*;
import com.mongodb.client.result.DeleteResult;
import com.mongodb.client.result.UpdateResult;
import org.bson.Document;
import org.opencb.commons.datastore.core.QueryOptions;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.TimeUnit;


/**
 * Created by imedina on 28/03/14.
 */
public class MongoDBNativeQuery {

    private final MongoCollection<Document> dbCollection;

    MongoDBNativeQuery(MongoCollection<Document> dbCollection) {
        this.dbCollection = dbCollection;
    }

    public long count() {
        return dbCollection.count();
    }

    public long count(Document query) {
//        CountOptions c = new CountOptions().
        return dbCollection.count(query);
    }


    public DistinctIterable<Document> distinct(String key) {
        return distinct(key, null, Document.class);
    }

    public <T> DistinctIterable distinct(String key, Class<T> resultClass) {
        return distinct(key, null, resultClass);
    }

    public <T> DistinctIterable<T> distinct(String key, Document query, Class<T> resultClass) {
        return dbCollection.distinct(key, query, resultClass);
    }


    public FindIterable<Document> find(Document query, QueryOptions options) {
        return find(query, null, options);
    }

    public FindIterable<Document> find(Document query, Document projection, QueryOptions options) {
//        DBCursor cursor;

        if (projection == null) {
            projection = getProjection(projection, options);
        }
//        cursor = dbCollection.find(query, projection);
        FindIterable<Document> findIterable = dbCollection.find(query).projection(projection);

        int limit = (options != null) ? options.getInt(MongoDBCollection.LIMIT, 0) : 0;
        if (limit > 0) {
//            cursor.limit(limit);
            findIterable.limit(limit);
        }

        int skip = (options != null) ? options.getInt(MongoDBCollection.SKIP, 0) : 0;
        if (skip > 0) {
//            cursor.skip(skip);
            findIterable.skip(skip);
        }

        Document sort = (options != null) ? (Document) options.get(MongoDBCollection.SORT) : null;
        if (sort != null) {
//            cursor.sort(sort);
            findIterable.sort(sort);
        }

        if (options != null && options.containsKey(MongoDBCollection.BATCH_SIZE)) {
//            cursor.batchSize(options.getInt(MongoDBCollection.BATCH_SIZE, 20));
            findIterable.batchSize(options.getInt(MongoDBCollection.BATCH_SIZE, 20));
        }

        if (options != null && options.containsKey(MongoDBCollection.TIMEOUT)) {
//            cursor.maxTime(options.getLong(MongoDBCollection.TIMEOUT), TimeUnit.MILLISECONDS);
            findIterable.maxTime(options.getLong(MongoDBCollection.TIMEOUT), TimeUnit.MILLISECONDS);
        }

        return findIterable;
    }

    public AggregateIterable aggregate(List<Document> operations, QueryOptions options) {
        return (operations.size() > 0) ? dbCollection.aggregate(operations) : null;
    }

    /**
     * This method insert a single document into a collection. Params w and wtimeout are read from QueryOptions.
     *
     * @param document
     * @param options
     * @return
     */
    public void insert(Document document, QueryOptions options) {
        int writeConcern = 1;
        int ms = 0;
        if (options != null && (options.containsKey("w") || options.containsKey("wtimeout"))) {
            // Some info about params: http://api.mongodb.org/java/current/com/mongodb/WriteConcern.html
//            return dbCollection.insert(dbObject, new WriteConcern(options.getInt("w", 1), options.getInt("wtimeout", 0)));
            writeConcern = options.getInt("w", 1);
            ms = options.getInt("wtimeout", 0);
        }
        dbCollection.withWriteConcern(new WriteConcern(writeConcern, ms));
        dbCollection.insertOne(document);
    }

    /**
     * This method insert a list of documents into a collection. Params w and wtimeout are read from QueryOptions.
     *
     * @param documentList
     * @param options
     * @return
     */
    public BulkWriteResult insert(List<Document> documentList, QueryOptions options) {
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

        return dbCollection.bulkWrite(actions, new BulkWriteOptions().ordered(false));
    }


    public UpdateResult update(Document query, Document updates, boolean upsert, boolean multi) {
        UpdateOptions updateOptions = new UpdateOptions().upsert(upsert);
        if (multi) {
            return dbCollection.updateMany(query, updates, updateOptions);
        } else {
            return dbCollection.updateOne(query, updates, updateOptions);
        }
    }

    public BulkWriteResult update(List<Document> documentList, List<Document> updatesList, boolean upsert, boolean multi) {
        if (documentList.size() != updatesList.size()) {
            throw new IndexOutOfBoundsException("QueryList.size and UpdatesList must be the same size");
        }

        Iterator<Document> queryIterator = documentList.iterator();
        Iterator<Document> updateIterator = updatesList.iterator();

        List<WriteModel<Document>> actions = new ArrayList<>(documentList.size());
        UpdateOptions updateOptions = new UpdateOptions().upsert(upsert);

        while (queryIterator.hasNext()) {
            Document query = queryIterator.next();
            Document update = updateIterator.next();

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
        return dbCollection.bulkWrite(actions, new BulkWriteOptions().ordered(false));
    }


    public DeleteResult remove(Document query) {
        return dbCollection.deleteMany(query);
    }

    public DeleteResult remove(Document query, boolean multi) {
        if (multi) {
            return dbCollection.deleteMany(query);
        } else {
            return dbCollection.deleteOne(query);
        }
    }

    public BulkWriteResult remove(List<Document> queryList, boolean multi) {
        List<WriteModel<Document>> actions = new ArrayList<>(queryList.size());
        if (multi) {
            for (Document document : queryList) {
                actions.add(new DeleteManyModel<>(document));
            }
        } else {
            for (Document document : queryList) {
                actions.add(new DeleteOneModel<>(document));
            }
        }
        return dbCollection.bulkWrite(actions, new BulkWriteOptions().ordered(false));
    }


    public Document findAndModify(Document query, Document projection, Document sort, Document update, QueryOptions options) {
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
            return dbCollection.findOneAndReplace(query, update, findOneAndReplaceOptions);
        } else {
            FindOneAndUpdateOptions findOneAndUpdateOptions = new FindOneAndUpdateOptions()
                    .sort(sort)
                    .projection(projection)
                    .upsert(upsert)
                    .returnDocument(returnNew ? ReturnDocument.AFTER : ReturnDocument.BEFORE);
            return dbCollection.findOneAndUpdate(query, update, findOneAndUpdateOptions);
        }
//        return dbCollection.findOneAndUpdate(query, projection, sort, remove, update, returnNew, upsert);
    }


    public void createIndex(Document keys, IndexOptions options) {
        dbCollection.createIndex(keys, options);
    }

    public List<Document> getIndex() {
        return dbCollection.listIndexes().into(new ArrayList<>());
    }

    public void dropIndex(Document keys) {
        dbCollection.dropIndex(keys);
    }


    private Document getProjection(Document projection, QueryOptions options) {
        // Select which fields are excluded and included in the query
        if (projection == null) {
            projection = new Document();
        }
        projection.put("_id", 0);

        if (options != null) {
            // Read and process 'include'/'exclude'/'elemMatch' field from 'options' object
            List<String> includeStringList = options.getAsStringList(MongoDBCollection.INCLUDE, ",");
            if (includeStringList != null && includeStringList.size() > 0) {
                for (Object field : includeStringList) {
                    projection.put(field.toString(), 1);
                }
            } else {
                List<String> excludeStringList = options.getAsStringList(MongoDBCollection.EXCLUDE, ",");
                if (excludeStringList != null && excludeStringList.size() > 0) {
                    for (Object field : excludeStringList) {
                        projection.put(field.toString(), 0);
                    }
                }
            }
            Document elemMatch = (Document) options.get(MongoDBCollection.ELEM_MATCH);
            if (elemMatch != null) {
                String field = (String) elemMatch.keySet().toArray()[0];
                projection.put(field, elemMatch.get(field));
            }
        }
        return projection;
    }

}
