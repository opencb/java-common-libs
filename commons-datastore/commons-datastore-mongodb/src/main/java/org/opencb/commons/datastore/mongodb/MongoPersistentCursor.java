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
import com.mongodb.client.AggregateIterable;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Sorts;
import org.bson.BsonDocument;
import org.bson.Document;
import org.bson.conversions.Bson;
import org.opencb.commons.datastore.core.QueryOptions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

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
    private final List<Bson> pipeline;
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
        this.pipeline = null;
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

    /**
     * Create a persistent cursor backed by an aggregation pipeline.
     * The pipeline's first stage should be a {@code $match} stage so that the resume filter
     * ({@code _id > lastId}) can be merged into it efficiently.
     * The cursor will automatically add a {@code $sort: {_id: 1}} stage when no sort is present,
     * ensuring deterministic ordering required for reliable resume.
     * Will fail if the {@code $project} stage excludes the {@code _id} field.
     *
     * @param collection MongoDB collection to run the pipeline against.
     * @param pipeline   Aggregation pipeline. Must not be empty and should start with {@code $match}.
     * @param options    Query options (BATCH_SIZE, LIMIT, SKIP are read from here).
     */
    public MongoPersistentCursor(MongoDBCollection collection, List<Bson> pipeline, QueryOptions options) {
        this(collection, pipeline, options,
                options != null ? options.getInt(MongoDBCollection.BATCH_SIZE, 0) : 0,
                options != null ? options.getInt(QueryOptions.LIMIT, 0) : 0,
                options != null ? options.getInt(QueryOptions.SKIP, 0) : 0);
    }

    /**
     * Create a persistent cursor backed by an aggregation pipeline with explicit pagination parameters.
     *
     * @param collection MongoDB collection to run the pipeline against.
     * @param pipeline   Aggregation pipeline. Must not be empty and should start with {@code $match}.
     * @param options    Query options (used for sort detection).
     * @param batchSize  MongoDB cursor batch size (0 = server default).
     * @param limit      Maximum number of documents to return (0 = unlimited).
     * @param skip       Number of documents to skip at the start (0 = none).
     */
    public MongoPersistentCursor(MongoDBCollection collection, List<Bson> pipeline, QueryOptions options,
                                 int batchSize, int limit, int skip) {
        this.options = options;
        this.query = null;
        this.projection = null;
        this.pipeline = new ArrayList<>(pipeline);
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
        if (pipeline != null) {
            resumeAggregate(lastObjectId);
        } else {
            resumeFind(lastObjectId);
        }
        return this;
    }

    private void resumeFind(Object lastObjectId) {
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
                .skip(lastObjectId == null ? skip : 0)
                .iterator();
    }

    /**
     * Build and execute an aggregation pipeline cursor, injecting a resume filter and ensuring
     * deterministic {@code _id} ordering when resuming after a cursor expiry.
     */
    private void resumeAggregate(Object lastObjectId) {
        List<Bson> activePipeline = new ArrayList<>(pipeline);

        // Inject _id > lastId into the first $match stage so MongoDB can use the index.
        if (lastObjectId != null) {
            Bson resumeFilter = Filters.gt("_id", lastObjectId);

            BsonDocument stage = activePipeline.get(0).toBsonDocument();

            if (!activePipeline.isEmpty() && stage.containsKey("$match")) {
                BsonDocument existingMatch = stage.get("$match").asDocument();
                activePipeline.set(0, new Document("$match", Filters.and(existingMatch, resumeFilter)));
            } else {
                activePipeline.add(0, new Document("$match", resumeFilter));
            }
        }

        // Ensure a $sort: {_id: 1} stage is present so that any resume starts past the right point.
        // Only added when no explicit sort stage exists and no sort is specified in options.
        boolean hasSortStage = activePipeline.stream().anyMatch(s -> s.toBsonDocument().containsKey("$sort"));
        if (!hasSortStage && (options == null || !options.containsKey(QueryOptions.SORT))) {
            // Insert before any existing $skip or $limit stages.
            int insertPos = activePipeline.size();
            for (int i = 0; i < activePipeline.size(); i++) {
                BsonDocument stage = activePipeline.get(i).toBsonDocument();
                if (stage.containsKey("$skip") || stage.containsKey("$limit")) {
                    insertPos = i;
                    break;
                }
            }
            activePipeline.add(insertPos, new Document("$sort", new Document("_id", 1)));
        }

        if (skip > 0 && lastObjectId == null) {
            activePipeline.add(new Document("$skip", skip));
        }
        if (limit > 0) {
            activePipeline.add(new Document("$limit", limit));
        }

        AggregateIterable<Document> iterable = newAggregateIterable(activePipeline);
        if (batchSize > 0) {
            iterable.batchSize(batchSize);
        }
        mongoCursor = iterable.iterator();
    }

    protected FindIterable<Document> newFindIterable(Bson query, Bson projection, QueryOptions options) {
        return this.collection.nativeQuery().nativeFind(null, query, projection, options);
    }

    protected AggregateIterable<Document> newAggregateIterable(List<Bson> activePipeline) {
        return collection.nativeQuery().getDbCollection().aggregate(activePipeline);
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
    public int available() {
        return this.mongoCursor.available();
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
