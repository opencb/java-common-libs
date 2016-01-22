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
import org.bson.conversions.Bson;
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

    public long count(Bson query) {
//        CountOptions c = new CountOptions().
        return dbCollection.count(query);
    }


    public DistinctIterable<Document> distinct(String key) {
        return distinct(key, null, Document.class);
    }

    public <T> DistinctIterable distinct(String key, Class<T> resultClass) {
        return distinct(key, null, resultClass);
    }

    public <T> DistinctIterable<T> distinct(String key, Bson query, Class<T> resultClass) {
        return dbCollection.distinct(key, query, resultClass);
    }


    public FindIterable<Document> find(Bson query, QueryOptions options) {
        return find(query, null, options);
    }

    public FindIterable<Document> find(Bson query, Bson projection, QueryOptions options) {

        if (projection == null) {
            projection = getProjection(projection, options);
        }

        FindIterable<Document> findIterable = dbCollection.find(query).projection(projection);

        int limit = (options != null) ? options.getInt(MongoDBCollection.LIMIT, 0) : 0;
        if (limit > 0) {
            findIterable.limit(limit);
        }

        int skip = (options != null) ? options.getInt(MongoDBCollection.SKIP, 0) : 0;
        if (skip > 0) {
            findIterable.skip(skip);
        }

        Bson sort = (options != null) ? (Bson) options.get(MongoDBCollection.SORT) : null;
        if (sort != null) {
            findIterable.sort(sort);
        }

        if (options != null && options.containsKey(MongoDBCollection.BATCH_SIZE)) {
            findIterable.batchSize(options.getInt(MongoDBCollection.BATCH_SIZE, 20));
        }

        if (options != null && options.containsKey(MongoDBCollection.TIMEOUT)) {
            findIterable.maxTime(options.getLong(MongoDBCollection.TIMEOUT), TimeUnit.MILLISECONDS);
        }

        return findIterable;
    }

    public AggregateIterable aggregate(List<Bson> operations, QueryOptions options) {
        return (operations.size() > 0) ? dbCollection.aggregate(operations) : null;
    }

    /**
     * This method insert a single document into a collection. Params w and wtimeout are read from QueryOptions.
     *
     * @param document The new document to be inserted
     * @param options  Some options like timeout
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
     * @param documentList The new list of documents to be inserted
     * @param options      Some options like timeout
     * @return A BulkWriteResult from MongoDB API
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


    public UpdateResult update(Bson query, Bson updates, boolean upsert, boolean multi) {
        UpdateOptions updateOptions = new UpdateOptions().upsert(upsert);
        if (multi) {
            return dbCollection.updateMany(query, updates, updateOptions);
        } else {
            return dbCollection.updateOne(query, updates, updateOptions);
        }
    }

    public BulkWriteResult update(List<Bson> documentList, List<Bson> updatesList, boolean upsert, boolean multi) {
        if (documentList.size() != updatesList.size()) {
            throw new IndexOutOfBoundsException("QueryList.size and UpdatesList must be the same size");
        }

        Iterator<Bson> queryIterator = documentList.iterator();
        Iterator<Bson> updateIterator = updatesList.iterator();

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
        return dbCollection.bulkWrite(actions, new BulkWriteOptions().ordered(false));
    }


    public DeleteResult remove(Bson query) {
        return dbCollection.deleteMany(query);
    }

    public DeleteResult remove(Bson query, boolean multi) {
        if (multi) {
            return dbCollection.deleteMany(query);
        } else {
            return dbCollection.deleteOne(query);
        }
    }

    public BulkWriteResult remove(List<Bson> queryList, boolean multi) {
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
        return dbCollection.bulkWrite(actions, new BulkWriteOptions().ordered(false));
    }


    public Document findAndUpdate(Bson query, Bson projection, Bson sort, Bson update, QueryOptions options) {
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
        return dbCollection.findOneAndUpdate(query, update, findOneAndUpdateOptions);
    }

    public Document findAndModify(Bson query, Bson projection, Bson sort, Document update, QueryOptions options) {
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

    public void createIndex(Bson keys, IndexOptions options) {
        dbCollection.createIndex(keys, options);
    }

    public List<Bson> getIndex() {
        return dbCollection.listIndexes().into(new ArrayList<>());
    }

    public void dropIndex(Bson keys) {
        dbCollection.dropIndex(keys);
    }


    private Bson getProjection(Bson projection, QueryOptions options) {
        Bson projectionResult = null;
        List<Bson> projections = new ArrayList<>();

        // It is too risky to merge projections, if projection alrady exists we return it as it is, otherwise we create a new one.
        if (projection != null) {
//            projections.add(projection);
            return projection;
        }

        if (options != null) {
            // Select which fields are excluded and included in the query
            // Read and process 'include'/'exclude'/'elemMatch' field from 'options' object

            Bson include = null;
            if (options.containsKey(MongoDBCollection.INCLUDE)) {
                Object includeObject = options.get(MongoDBCollection.INCLUDE);
                if (includeObject != null) {
                    if (includeObject instanceof Bson) {
                        include = (Bson) includeObject;
                    } else {
                        List<String> includeStringList = options.getAsStringList(MongoDBCollection.INCLUDE, ",");
                        if (includeStringList != null && includeStringList.size() > 0) {
                            include = Projections.include(includeStringList);
                        }
                    }
                }
            }

            Bson exclude = null;
            boolean excludeId = false;
            if (options.containsKey(MongoDBCollection.EXCLUDE)) {
                Object excludeObject = options.get(MongoDBCollection.EXCLUDE);
                if (excludeObject != null) {
                    if (excludeObject instanceof Bson) {
                        exclude = (Bson) excludeObject;
                    } else {
                        List<String> excludeStringList = options.getAsStringList(MongoDBCollection.EXCLUDE, ",");
                        if (excludeStringList != null && excludeStringList.size() > 0) {
                            exclude = Projections.exclude(excludeStringList);
                            excludeId = excludeStringList.contains("_id");
                        }
                    }
                }
            }

            // If both include and exclude exist we only add include
            if (include != null) {
                projections.add(include);
                // MongoDB allows to exclude _id when include is present
                if (excludeId) {
                    projections.add(Projections.excludeId());
                }
            } else {
                if (exclude != null) {
                    projections.add(exclude);
                }
            }


            if (options.containsKey(MongoDBCollection.ELEM_MATCH)) {
                Object elemMatch = options.get(MongoDBCollection.ELEM_MATCH);
                if (elemMatch != null && elemMatch instanceof Bson) {
                    projections.add((Bson) elemMatch);
                }
            }

//            List<String> includeStringList = options.getAsStringList(MongoDBCollection.INCLUDE, ",");
//            if (includeStringList != null && includeStringList.size() > 0) {
//                projections.add(Projections.include(includeStringList));
////                for (Object field : includeStringList) {
////                    projection.put(field.toString(), 1);
////                }
//            } else {
//                List<String> excludeStringList = options.getAsStringList(MongoDBCollection.EXCLUDE, ",");
//                if (excludeStringList != null && excludeStringList.size() > 0) {
//                    projections.add(Projections.exclude(excludeStringList));
////                    for (Object field : excludeStringList) {
////                        projection.put(field.toString(), 0);
////                    }
//                }
//            }
        }

        if (projections.size() > 0) {
            projectionResult = Projections.fields(projections);
        }

        return projectionResult;
    }

}
