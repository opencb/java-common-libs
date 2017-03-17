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

import com.mongodb.MongoCursorNotFoundException;
import com.mongodb.ServerAddress;
import com.mongodb.ServerCursor;
import com.mongodb.annotations.NotThreadSafe;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Sorts;
import org.bson.Document;
import org.bson.conversions.Bson;
import org.opencb.commons.datastore.core.QueryOptions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * MongoDBCursor wrapper for queries that require a long time to process the results.
 * Avoids {@link MongoCursorNotFoundException}.
 * Will sort the results in natural order, to ensure that any value is returned twice.
 * Will fail if the projection excludes the "_id" field.
 * Can deal with a server shutdown that releases the cursor descriptor, but not with a {@link com.mongodb.MongoSocketOpenException}
 *
 * Created on 08/06/16
 *
 * @author Jacobo Coll &lt;jacobo167@gmail.com&gt;
 */
@NotThreadSafe
public class MongoPersistentCursor implements MongoCursor<Document> {

    private final QueryOptions options;
    private final Bson query;
    private final Bson projection;
    private MongoDBCollection collection;

    private MongoCursor<Document> mongoCursor;
    private int count;
    private int exceptions;
    private Object lastId;

    protected static Logger logger = LoggerFactory.getLogger(MongoPersistentCursor.class);
    private int batchSize = 0; // Default 0 which indicates that the server chooses an appropriate batch size
    private int limit = 0;
    private int skip = 0;

    public MongoPersistentCursor(MongoDBCollection collection, Bson query, Bson projection, QueryOptions options) {
        this(collection, query, projection, options,
                options != null ? options.getInt(MongoDBCollection.BATCH_SIZE, 0) : 0,
                options != null ? options.getInt(QueryOptions.LIMIT, 0) : 0,
                options != null ? options.getInt(QueryOptions.SKIP, 0) : 0);
    }

    public MongoPersistentCursor(MongoDBCollection collection, Bson query, Bson projection, QueryOptions options,
                                 int batchSize, int limit, int skip) {
        this.options = options;
        this.query = query;
        this.projection = projection;
        this.collection = collection;

        if (batchSize > 0) {
            this.batchSize = batchSize;
        }
        if (limit > 0) {
            this.limit = limit;
        }
        if (skip > 0) {
            this.skip = skip;
        }

        reset();
    }

    protected void reset() {
        count = 0;
        exceptions = 0;
        resume(null);
    }

    protected MongoPersistentCursor resume(Object lastObjectId) {
        Bson query;
        if (lastObjectId != null) {
            query = Filters.and(Filters.gt("_id", lastObjectId), this.query);
        } else {
            query = this.query;
        }
        FindIterable<Document> iterable = newFindIterable(query, this.projection, this.options);
        if (!options.containsKey(QueryOptions.SORT)) {
            iterable.sort(Sorts.ascending("$natural"));
        }
        mongoCursor = iterable
                .batchSize(batchSize)
                .limit(limit)
                .skip(skip)
                .iterator();
        return this;
    }

    protected FindIterable<Document> newFindIterable(Bson query, Bson projection, QueryOptions options) {
        return this.collection.nativeQuery().find(query, projection, options);
    }

    public Object getLastId() {
        return lastId;
    }

    public int getCount() {
        return count;
    }

    public int getBatchSize() {
        return batchSize;
    }

    public int getNumExceptions() {
        return exceptions;
    }

    public MongoPersistentCursor setBatchSize(int batchSize) {
        this.batchSize = batchSize;
        return this;
    }

    private void error(MongoCursorNotFoundException e) {
        logger.warn("Resuming after " + MongoCursorNotFoundException.class, e);
        exceptions++;
        resume(lastId);
    }

    @Override
    public boolean hasNext() {
        try {
            return mongoCursor.hasNext();
        } catch (MongoCursorNotFoundException e) {
            error(e);
            return mongoCursor.hasNext();
        }
    }

    @Override
    public Document next() {
        Document next;
        try {
            next = mongoCursor.next();
        } catch (MongoCursorNotFoundException e) {
            error(e);
            next = mongoCursor.next();
        }
        count++;
        lastId = next.get("_id");
        return next;
    }

    @Override
    public Document tryNext() {
        Document next;
        try {
            next = mongoCursor.tryNext();
        } catch (MongoCursorNotFoundException e) {
            error(e);
            next = mongoCursor.tryNext();
        }
        if (next != null) {
            count++;
            lastId = next.get("_id");
        }
        return next;
    }

    @Override
    public ServerCursor getServerCursor() {
        return mongoCursor.getServerCursor();
    }

    @Override
    public ServerAddress getServerAddress() {
        return mongoCursor.getServerAddress();
    }

    @Override
    public void close() {
        mongoCursor.close();
    }
}
