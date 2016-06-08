package org.opencb.commons.datastore.mongodb;

import com.mongodb.MongoCursorNotFoundException;
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

import java.io.Closeable;
import java.io.IOException;
import java.util.Iterator;

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
public class MongoPersistentCursor implements Iterator<Document>, Closeable {

    private final QueryOptions options;
    private final Bson query;
    private final Bson projection;
    private MongoDBCollection collection;

    private MongoCursor<Document> iterator;

    private int count;
    private int catchedExceptions;
    private Object lastId;

    protected static Logger logger = LoggerFactory.getLogger(MongoPersistentCursor.class);
    private int batchSize = 0; // Default 0 which indicates that the server chooses an appropriate batch size

    public MongoPersistentCursor(MongoDBCollection collection, Bson query, Bson projection, QueryOptions options) {
        this.options = options;
        this.query = query;
        this.projection = projection;
        this.collection = collection;

        if (options != null) {
            batchSize = options.getInt(MongoDBCollection.BATCH_SIZE, 0);
        }

        reset();
    }

    public void reset() {
        count = 0;
        catchedExceptions = 0;
        resume(null);
    }

    public void resume(Object lastObjectId) {
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
        iterator = iterable
                .batchSize(batchSize)
                .iterator();
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

    public int getCatchedExceptions() {
        return catchedExceptions;
    }

    public MongoPersistentCursor setBatchSize(int batchSize) {
        this.batchSize = batchSize;
        return this;
    }

    private void error(MongoCursorNotFoundException e) {
        logger.warn("Resuming after " + MongoCursorNotFoundException.class, e);
        catchedExceptions++;
        resume(lastId);
    }

    @Override
    public boolean hasNext() {
        try {
            return iterator.hasNext();
        } catch (MongoCursorNotFoundException e) {
            error(e);
            return iterator.hasNext();
        }
    }

    @Override
    public Document next() {
        Document next;
        try {
            next = iterator.next();
        } catch (MongoCursorNotFoundException e) {
            error(e);
            next = iterator.next();
        }
        count++;
        lastId = next.get("_id");
        return next;
    }

    @Override
    public void close() throws IOException {
        iterator.close();
    }
}
