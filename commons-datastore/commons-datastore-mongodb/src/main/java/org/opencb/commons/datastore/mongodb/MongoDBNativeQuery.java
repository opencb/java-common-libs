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

import com.mongodb.*;

import java.util.Iterator;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.opencb.commons.datastore.core.QueryOptions;


/**
 * Created by imedina on 28/03/14.
 */
public class MongoDBNativeQuery {

    private final DBCollection dbCollection;

    MongoDBNativeQuery(DBCollection dbCollection) {
        this.dbCollection = dbCollection;
    }

    public long count() {
        long result = dbCollection.count();
        return result;
    }

    public long count(DBObject query) {
        long result = dbCollection.count(query);
        return result;
    }

    public List distinct(String key) {
        return distinct(key, null);
    }

    public List distinct(String key, DBObject query) {
        List result = dbCollection.distinct(key, query);
        return result;
    }

    public DBCursor find(DBObject query, QueryOptions options) {
        return find(query, null, options);
    }

    public DBCursor find(DBObject query, DBObject projection, QueryOptions options) {
        DBCursor cursor;

        if(projection == null) {
            projection = getProjection(projection, options);
        }
        cursor = dbCollection.find(query, projection);

        int limit = (options != null) ? options.getInt(MongoDBCollection.LIMIT, 0) : 0;
        if (limit > 0) {
            cursor.limit(limit);
        }

        int skip = (options != null) ? options.getInt(MongoDBCollection.SKIP, 0) : 0;
        if (skip > 0) {
            cursor.skip(skip);
        }

        BasicDBObject sort = (options != null) ? (BasicDBObject) options.get(MongoDBCollection.SORT) : null;
        if (sort != null) {
            cursor.sort(sort);
        }

        if (options != null && options.containsKey(MongoDBCollection.BATCH_SIZE)) {
            cursor.batchSize(options.getInt(MongoDBCollection.BATCH_SIZE, 20));
        }

        if (options != null && options.containsKey(MongoDBCollection.TIMEOUT)) {
            cursor.maxTime(options.getLong(MongoDBCollection.TIMEOUT), TimeUnit.MILLISECONDS);
        }

        return cursor;
    }

    public AggregationOutput aggregate(List<DBObject> operations, QueryOptions options) {
        return (operations.size() > 0) ? dbCollection.aggregate(operations) : null;
    }

    /**
     * This method insert a single document into a collection. Params w and wtimeout are read from QueryOptions.
     * @param dbObject
     * @param options
     * @return
     */
    public WriteResult insert(DBObject dbObject, QueryOptions options) {
        if(options != null && (options.containsKey("w") || options.containsKey("wtimeout"))) {
            // Some info about params: http://api.mongodb.org/java/current/com/mongodb/WriteConcern.html
            return dbCollection.insert(dbObject, new WriteConcern(options.getInt("w", 1),
                    options.getInt("wtimeout", 0)));
        }else {
            return dbCollection.insert(dbObject);
        }
    }

    /**
     * This method insert a list of documents into a collection. Params w and wtimeout are read from QueryOptions.
     * @param dbObjectList
     * @param options
     * @return
     */
    public BulkWriteResult insert(List<DBObject> dbObjectList, QueryOptions options) {
        // Let's prepare the Bulk object
        BulkWriteOperation bulk = dbCollection.initializeUnorderedBulkOperation();
        for (DBObject document : dbObjectList) {
            bulk.insert(document);
        }

        if(options != null && (options.containsKey("w") || options.containsKey("wtimeout"))) {
            // Some info about params: http://api.mongodb.org/java/current/com/mongodb/WriteConcern.html
            return bulk.execute(new WriteConcern(options.getInt("w", 1), options.getInt("wtimeout", 0)));
        }else {
            return bulk.execute();
        }
    }

    public WriteResult update(DBObject object, DBObject updates, boolean upsert, boolean multi) {
        return dbCollection.update(object, updates, upsert, multi);
    }

    public BulkWriteResult update(List<DBObject> queryList, List<DBObject> updatesList, boolean upsert, boolean multi) {
        if (queryList.size() != updatesList.size()) {
            throw new IndexOutOfBoundsException("QueryList.size and UpdatesList must be the same size");
        }

        BulkWriteOperation bulk = dbCollection.initializeUnorderedBulkOperation();
        Iterator<DBObject> queryIterator = queryList.iterator();
        Iterator<DBObject> updateIterator = updatesList.iterator();

        while (queryIterator.hasNext()) {
            DBObject query = queryIterator.next();
            DBObject update = updateIterator.next();

            BulkWriteRequestBuilder builder = bulk.find(query);
            if (upsert) {
                if (multi) {
                    builder.upsert().update(update);
                } else {
                    builder.upsert().updateOne(update);
                }
            } else {
                if (multi) {
                    builder.update(update);
                } else {
                    builder.updateOne(update);
                }
            }
        }
        return bulk.execute();
    }

    public WriteResult remove(DBObject query) {
        return dbCollection.remove(query);
    }

    public BulkWriteResult remove(List<DBObject> queryList, boolean multi) {
        BulkWriteOperation bulk = dbCollection.initializeUnorderedBulkOperation();
        for (DBObject query : queryList) {
            BulkWriteRequestBuilder builder = bulk.find(query);
            if (multi) {
                builder.remove();
            } else {
                builder.removeOne();
            }
        }
        return bulk.execute();
    }

    public DBObject findAndModify(DBObject query, DBObject projection, DBObject sort, DBObject update, QueryOptions options) {
        boolean remove = false;
        boolean returnNew = false;
        boolean upsert = false;

        if(options != null) {
            if(projection == null) {
                projection = getProjection(projection, options);
            }
            remove = options.getBoolean("remove", false);
            returnNew = options.getBoolean("returnNew", false);
            upsert = options.getBoolean("upsert", false);
        }
        return dbCollection.findAndModify(query, projection, sort, remove, update, returnNew, upsert);
    }

    public void createIndex(DBObject keys, DBObject options) {
        dbCollection.createIndex(keys, options);
    }

    public List<DBObject> getIndex() {
        return dbCollection.getIndexInfo();
    }

    public void dropIndex(DBObject keys) {
        dbCollection.dropIndex(keys);
    }

    private DBObject getProjection(DBObject projection, QueryOptions options) {
        // Select which fields are excluded and included in the query
//      DBObject returnFields = null;
//      returnFields = new BasicDBObject("_id", 0);
        if(projection == null) {
            projection = new BasicDBObject();
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
            BasicDBObject elemMatch  = (BasicDBObject) options.get(MongoDBCollection.ELEM_MATCH);
            if (elemMatch != null) {
                String field = (String) elemMatch.keySet().toArray()[0];
                projection.put(field, elemMatch.get(field));
            }
        }
        return projection;
    }

}
