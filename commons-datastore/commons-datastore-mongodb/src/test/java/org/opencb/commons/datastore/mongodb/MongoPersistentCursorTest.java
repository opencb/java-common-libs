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
import com.mongodb.client.AggregateIterable;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.model.Filters;
import org.bson.Document;
import org.bson.conversions.Bson;
import org.bson.types.ObjectId;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import org.opencb.commons.datastore.core.QueryOptions;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Tests for {@link MongoPersistentCursor} in both find and aggregation pipeline modes.
 */
public class MongoPersistentCursorTest {

    private static MongoDataStoreManager mongoDataStoreManager;
    private static MongoDataStore mongoDataStore;
    private static MongoDBCollection collection;

    private static final int NUM_DOCS = 2000;
    /** Number of docs fetched before the simulated cursor expiry fires. */
    private static final int FAIL_AFTER = 300;

    @BeforeClass
    public static void beforeClass() {
        mongoDataStoreManager = new MongoDataStoreManager("localhost", 27017);
        mongoDataStoreManager.get("persistent_cursor_test_db");   // register in map first
        mongoDataStoreManager.drop("persistent_cursor_test_db");  // now the drop takes effect
        mongoDataStore = mongoDataStoreManager.get("persistent_cursor_test_db");
        collection = mongoDataStore.getCollection("test");

        for (int i = 0; i < NUM_DOCS; i++) {
            Document doc = new Document("value", i).append("group", i % 4);
            collection.nativeQuery().insert(doc, null);
        }
    }

    @AfterClass
    public static void afterClass() {
        mongoDataStore.close();
    }

    // -------------------------------------------------------------------------
    // Find mode
    // -------------------------------------------------------------------------

    @Test
    public void testFindModeReturnsAllDocuments() {
        MongoPersistentCursor cursor = new MongoPersistentCursor(collection, new Document(), null, new QueryOptions());
        int count = 0;
        while (cursor.hasNext()) {
            cursor.next();
            count++;
        }
        cursor.close();

        assertEquals(NUM_DOCS, count);
        assertEquals(NUM_DOCS, cursor.getCount());
        assertEquals(0, cursor.getNumExceptions());
    }

    @Test
    public void testFindModeWithFilter() {
        // Documents where group == 0: indices 0, 4, 8, ... -> NUM_DOCS/4 documents
        MongoPersistentCursor cursor = new MongoPersistentCursor(
                collection,
                Filters.eq("group", 0),
                null,
                new QueryOptions());

        List<Document> results = new ArrayList<>();
        while (cursor.hasNext()) {
            results.add(cursor.next());
        }
        cursor.close();

        assertEquals(NUM_DOCS / 4, results.size());
        for (Document doc : results) {
            assertEquals(0, doc.getInteger("group").intValue());
        }
    }

    @Test
    public void testFindModeWithLimit() {
        int limit = 7;
        QueryOptions options = new QueryOptions(QueryOptions.LIMIT, limit);
        MongoPersistentCursor cursor = new MongoPersistentCursor(collection, new Document(), null, options);

        int count = 0;
        while (cursor.hasNext()) {
            cursor.next();
            count++;
        }
        cursor.close();

        assertEquals(limit, count);
    }

    @Test
    public void testFindModeTracksLastId() {
        MongoPersistentCursor cursor = new MongoPersistentCursor(collection, new Document(), null, new QueryOptions());

        assertNull(cursor.getLastId());

        Document first = cursor.next();
        assertEquals(first.get("_id"), cursor.getLastId());

        Document second = cursor.next();
        assertEquals(second.get("_id"), cursor.getLastId());

        cursor.close();
    }

    @Test
    public void testFindModeDocumentsAreOrderedById() {
        MongoPersistentCursor cursor = new MongoPersistentCursor(collection, new Document(), null, new QueryOptions());

        ObjectId previous = null;
        while (cursor.hasNext()) {
            Document doc = cursor.next();
            ObjectId current = doc.getObjectId("_id");
            if (previous != null) {
                assertTrue("Documents must be in ascending _id order", current.compareTo(previous) > 0);
            }
            previous = current;
        }
        cursor.close();
    }

    @Test
    public void testFindModeResumesAfterCursorException() {
        MongoPersistentCursor cursor = new FailingCursorPersistentCursor(
                collection, new Document(), null, new QueryOptions(), FAIL_AFTER, 1);

        Set<Integer> values = drainValues(cursor);

        assertCompleteValueSet(values);
        assertEquals(NUM_DOCS, cursor.getCount());
        assertEquals(1, cursor.getNumExceptions());
    }

    @Test
    public void testFindModeResumesMultipleTimes() {
        int maxFailures = 3;
        MongoPersistentCursor cursor = new FailingCursorPersistentCursor(
                collection, new Document(), null, new QueryOptions(), FAIL_AFTER, maxFailures);

        Set<Integer> values = drainValues(cursor);

        assertCompleteValueSet(values);
        assertEquals(NUM_DOCS, cursor.getCount());
        assertEquals(maxFailures, cursor.getNumExceptions());
    }

    // -------------------------------------------------------------------------
    // Aggregation pipeline mode
    // -------------------------------------------------------------------------

    @Test
    public void testAggregationModeReturnsAllDocuments() {
        List<Bson> pipeline = Arrays.asList(
                new Document("$match", new Document())
        );

        MongoPersistentCursor cursor = new MongoPersistentCursor(collection, pipeline, new QueryOptions());

        int count = 0;
        while (cursor.hasNext()) {
            cursor.next();
            count++;
        }
        cursor.close();

        assertEquals(NUM_DOCS, count);
        assertEquals(NUM_DOCS, cursor.getCount());
        assertEquals(0, cursor.getNumExceptions());
    }

    @Test
    public void testAggregationModeWithMatchFilter() {
        // group == 1: NUM_DOCS/4 documents
        List<Bson> pipeline = Arrays.asList(
                new Document("$match", new Document("group", 1))
        );

        MongoPersistentCursor cursor = new MongoPersistentCursor(collection, pipeline, new QueryOptions());

        List<Document> results = new ArrayList<>();
        while (cursor.hasNext()) {
            results.add(cursor.next());
        }
        cursor.close();

        assertEquals(NUM_DOCS / 4, results.size());
        for (Document doc : results) {
            assertEquals(1, doc.getInteger("group").intValue());
        }
    }

    @Test
    public void testAggregationModeWithLimit() {
        int limit = 6;
        QueryOptions options = new QueryOptions(QueryOptions.LIMIT, limit);
        List<Bson> pipeline = Arrays.asList(
                new Document("$match", new Document())
        );

        MongoPersistentCursor cursor = new MongoPersistentCursor(collection, pipeline, options);

        int count = 0;
        while (cursor.hasNext()) {
            cursor.next();
            count++;
        }
        cursor.close();

        assertEquals(limit, count);
    }

    @Test
    public void testAggregationModeTracksLastId() {
        List<Bson> pipeline = Arrays.asList(
                new Document("$match", new Document())
        );

        MongoPersistentCursor cursor = new MongoPersistentCursor(collection, pipeline, new QueryOptions());

        assertNull(cursor.getLastId());

        Document first = cursor.next();
        assertEquals(first.get("_id"), cursor.getLastId());

        Document second = cursor.next();
        assertEquals(second.get("_id"), cursor.getLastId());

        cursor.close();
    }

    @Test
    public void testAggregationModeDocumentsAreOrderedById() {
        List<Bson> pipeline = Arrays.asList(
                new Document("$match", new Document())
        );

        MongoPersistentCursor cursor = new MongoPersistentCursor(collection, pipeline, new QueryOptions());

        ObjectId previous = null;
        while (cursor.hasNext()) {
            Document doc = cursor.next();
            ObjectId current = doc.getObjectId("_id");
            if (previous != null) {
                assertTrue("Aggregation pipeline documents must be in ascending _id order",
                        current.compareTo(previous) > 0);
            }
            previous = current;
        }
        cursor.close();
    }

    @Test
    public void testAggregationModeWithAddFieldsStage() {
        List<Bson> pipeline = Arrays.asList(
                new Document("$match", new Document()),
                new Document("$addFields", new Document("doubled",
                        new Document("$multiply", Arrays.asList("$value", 2))))
        );

        MongoPersistentCursor cursor = new MongoPersistentCursor(collection, pipeline, new QueryOptions());

        int count = 0;
        while (cursor.hasNext()) {
            Document doc = cursor.next();
            assertEquals(doc.getInteger("value") * 2, doc.getInteger("doubled").intValue());
            count++;
        }
        cursor.close();

        assertEquals(NUM_DOCS, count);
    }

    @Test
    public void testAggregationModeResumesAfterCursorException() {
        List<Bson> pipeline = Arrays.asList(new Document("$match", new Document()));

        MongoPersistentCursor cursor = new FailingCursorPersistentCursor(
                collection, pipeline, new QueryOptions(), FAIL_AFTER, 1);

        Set<Integer> values = drainValues(cursor);

        assertCompleteValueSet(values);
        assertEquals(NUM_DOCS, cursor.getCount());
        assertEquals(1, cursor.getNumExceptions());
    }

    @Test
    public void testAggregationModeResumesMultipleTimes() {
        int maxFailures = 3;
        List<Bson> pipeline = Arrays.asList(new Document("$match", new Document()));

        MongoPersistentCursor cursor = new FailingCursorPersistentCursor(
                collection, pipeline, new QueryOptions(), FAIL_AFTER, maxFailures);

        Set<Integer> values = drainValues(cursor);

        assertCompleteValueSet(values);
        assertEquals(NUM_DOCS, cursor.getCount());
        assertEquals(maxFailures, cursor.getNumExceptions());
    }

    // -------------------------------------------------------------------------
    // #1 – Exception fires before the first document (lastId is null on resume)
    // -------------------------------------------------------------------------

    @Test
    public void testFindModeResumesWhenExceptionBeforeFirstDocument() {
        MongoPersistentCursor cursor = new FailingCursorPersistentCursor(
                collection, new Document(), null, new QueryOptions(), 0, 1);

        Set<Integer> values = drainValues(cursor);

        assertCompleteValueSet(values);
        assertEquals(NUM_DOCS, cursor.getCount());
        assertEquals(1, cursor.getNumExceptions());
    }

    @Test
    public void testAggregationModeResumesWhenExceptionBeforeFirstDocument() {
        List<Bson> pipeline = Arrays.asList(new Document("$match", new Document()));

        MongoPersistentCursor cursor = new FailingCursorPersistentCursor(
                collection, pipeline, new QueryOptions(), 0, 1);

        Set<Integer> values = drainValues(cursor);

        assertCompleteValueSet(values);
        assertEquals(NUM_DOCS, cursor.getCount());
        assertEquals(1, cursor.getNumExceptions());
    }

    // -------------------------------------------------------------------------
    // #2 – Exception fires after the second-to-last document
    // -------------------------------------------------------------------------

    @Test
    public void testFindModeResumesWhenExceptionAfterLastDocument() {
        // failAfter = NUM_DOCS-1: the failing cursor returns all but the last doc,
        // throws, then the resume cursor returns that final document.
        MongoPersistentCursor cursor = new FailingCursorPersistentCursor(
                collection, new Document(), null, new QueryOptions(), NUM_DOCS - 1, 1);

        Set<Integer> values = drainValues(cursor);

        assertCompleteValueSet(values);
        assertEquals(NUM_DOCS, cursor.getCount());
        assertEquals(1, cursor.getNumExceptions());
    }

    @Test
    public void testAggregationModeResumesWhenExceptionAfterLastDocument() {
        List<Bson> pipeline = Arrays.asList(new Document("$match", new Document()));

        MongoPersistentCursor cursor = new FailingCursorPersistentCursor(
                collection, pipeline, new QueryOptions(), NUM_DOCS - 1, 1);

        Set<Integer> values = drainValues(cursor);

        assertCompleteValueSet(values);
        assertEquals(NUM_DOCS, cursor.getCount());
        assertEquals(1, cursor.getNumExceptions());
    }

    // -------------------------------------------------------------------------
    // #3 – Aggregation pipeline without a leading $match stage
    //      Tests the branch in resumeAggregate that prepends the resume filter
    //      instead of merging it into an existing $match.
    // -------------------------------------------------------------------------

    @Test
    public void testAggregationModeResumesWithoutLeadingMatchStage() {
        // No $match, no $sort – resumeAggregate must prepend $match:{_id:{$gt:lastId}}
        // and inject $sort:{_id:1} on every iteration including resumes.
        List<Bson> pipeline = Arrays.asList(
                new Document("$addFields", new Document("doubled",
                        new Document("$multiply", Arrays.asList("$value", 2))))
        );

        MongoPersistentCursor cursor = new FailingCursorPersistentCursor(
                collection, pipeline, new QueryOptions(), FAIL_AFTER, 1);

        Set<Integer> values = drainValues(cursor);

        assertCompleteValueSet(values);
        assertEquals(NUM_DOCS, cursor.getCount());
        assertEquals(1, cursor.getNumExceptions());
    }

    // -------------------------------------------------------------------------
    // #5 – skip must not be re-applied after a cursor resume
    // -------------------------------------------------------------------------

    @Test
    public void testFindModeSkipNotReappliedOnResume() {
        int skip = 5;
        QueryOptions options = new QueryOptions(QueryOptions.SKIP, skip);
        MongoPersistentCursor cursor = new FailingCursorPersistentCursor(
                collection, new Document(), null, options, FAIL_AFTER, 1);

        Set<Integer> values = drainValues(cursor);

        Set<Integer> expected = new HashSet<>();
        for (int i = skip; i < NUM_DOCS; i++) {
            expected.add(i);
        }
        assertEquals("skip must apply only at the start, not on each resume", expected, values);
        assertEquals(1, cursor.getNumExceptions());
    }

    @Test
    public void testAggregationModeSkipNotReappliedOnResume() {
        int skip = 5;
        QueryOptions options = new QueryOptions(QueryOptions.SKIP, skip);
        List<Bson> pipeline = Arrays.asList(new Document("$match", new Document()));

        MongoPersistentCursor cursor = new FailingCursorPersistentCursor(
                collection, pipeline, options, FAIL_AFTER, 1);

        Set<Integer> values = drainValues(cursor);

        Set<Integer> expected = new HashSet<>();
        for (int i = skip; i < NUM_DOCS; i++) {
            expected.add(i);
        }
        assertEquals("skip must apply only at the start, not on each resume", expected, values);
        assertEquals(1, cursor.getNumExceptions());
    }

    // -------------------------------------------------------------------------
    // Real cursor expiry (manual integration test — requires a live MongoDB)
    // -------------------------------------------------------------------------

    /**
     * Verifies that the cursor recovers transparently from a real server-side
     * {@link MongoCursorNotFoundException}.
     *
     * <p>Temporarily reduces {@code cursorTimeoutMillis} to 5 s so that the
     * deliberate sleep is short.  A small {@code batchSize} forces MongoDB to
     * open a real server cursor (without it, all documents may be returned in
     * a single batch and no cursor would exist to expire).
     *
     * <p>Run manually:
     * <pre>
     *   mvn test -pl commons-datastore/commons-datastore-mongodb \
     *            -Dtest=MongoPersistentCursorTest#testRealCursorExpiry \
     *            -Dsurefire.failIfNoSpecifiedTests=false
     * </pre>
     */
    @Test
    @Ignore
    public void testRealCursorExpiry() throws Exception {
        int cursorTimeoutMs = 1_000;
        int sleepMs         = 60_000;

        // Reduce cursor TTL so the sleep stays short.
        mongoDataStore.getMongoClient().getDatabase("admin")
                .runCommand(new Document("setParameter", 1).append("cursorTimeoutMillis", cursorTimeoutMs));
        try {
            // batchSize forces a real server-side cursor; without it every document
            // may arrive in one batch and the cursor never exists on the server.
            QueryOptions options = new QueryOptions(MongoDBCollection.BATCH_SIZE, 100);
            MongoPersistentCursor cursor = new MongoPersistentCursor(collection, new Document(), null, options);

            Set<Integer> values = new HashSet<>();
            int count = 0;
            while (cursor.hasNext()) {
                Document doc = cursor.next();
                assertTrue("Duplicate value " + doc.getInteger("value"), values.add(doc.getInteger("value")));
                count++;

                if (count == 10) {
                    // Let the server-side cursor expire.
                    System.out.println("Sleeping " + sleepMs + " ms to expire the server cursor after _id=" + doc.get("_id"));
                    Thread.sleep(sleepMs);
                    System.out.println("Woke up. Calling cursor.next() to exercise the next() recovery path...");

                    // Call next() directly so we cover the MongoCursorNotFoundException
                    // catch block inside MongoPersistentCursor.next(), not just hasNext().
                    Document resumed = cursor.next();
                    assertTrue("Duplicate value after resume", values.add(resumed.getInteger("value")));
                    System.out.println("Recovered. First doc after expiry: _id=" + resumed.get("_id"));
                }
            }
            cursor.close();

            assertCompleteValueSet(values);
            assertEquals(NUM_DOCS, cursor.getCount());
            assertEquals(1, cursor.getNumExceptions());
        } finally {
            // Restore MongoDB default cursor timeout (10 minutes).
            mongoDataStore.getMongoClient().getDatabase("admin")
                    .runCommand(new Document("setParameter", 1).append("cursorTimeoutMillis", 600_000));
        }
    }

    // =========================================================================
    // Helpers
    // =========================================================================

    /**
     * Drains the cursor into a set of {@code value} integers and closes it.
     * Using a Set detects duplicates (add returns false) and, combined with
     * {@link #assertCompleteValueSet}, detects skipped documents.
     */
    private static Set<Integer> drainValues(MongoPersistentCursor cursor) {
        Set<Integer> values = new HashSet<>();
        while (cursor.hasNext()) {
            Document doc = cursor.next();
            int value = doc.getInteger("value");
            assertTrue("Duplicate value " + value + " returned by cursor", values.add(value));
        }
        cursor.close();
        return values;
    }

    /**
     * Asserts that the set is exactly {@code {0, 1, …, NUM_DOCS-1}}: every document
     * was returned exactly once with no gaps.
     */
    private static void assertCompleteValueSet(Set<Integer> values) {
        Set<Integer> expected = new HashSet<>();
        for (int i = 0; i < NUM_DOCS; i++) {
            expected.add(i);
        }
        assertEquals("Cursor must return every document exactly once", expected, values);
    }

    /**
     * Subclass that injects a {@link FailingMongoCursor} for the first {@code maxFailures}
     * resume calls, simulating {@link MongoCursorNotFoundException} mid-iteration.
     *
     * <p>The {@code initialized} flag is false while {@code super()} runs (Java guarantees that
     * subclass fields are still at their defaults during the superclass constructor), so the first
     * cursor created during construction uses the real MongoDB cursor.  Once our constructor body
     * sets {@code initialized = true} and calls {@link #reset()}, subsequent calls to
     * {@code newFindIterable} / {@code newAggregateIterable} wrap the cursor with a failing one.
     */
    private static class FailingCursorPersistentCursor extends MongoPersistentCursor {

        private final int failAfter;
        private int failuresLeft;
        /** False while super() runs so the constructor-time reset uses a real cursor. */
        private boolean initialized;

        // Find mode
        FailingCursorPersistentCursor(MongoDBCollection collection, Bson query, Bson projection,
                                      QueryOptions options, int failAfter, int maxFailures) {
            super(collection, query, projection, options);
            this.failAfter = failAfter;
            this.failuresLeft = maxFailures;
            this.initialized = true;
            reset(); // restart fresh – this time newFindIterable wraps with a failing cursor
        }

        // Aggregate mode
        FailingCursorPersistentCursor(MongoDBCollection collection, List<Bson> pipeline,
                                      QueryOptions options, int failAfter, int maxFailures) {
            super(collection, pipeline, options);
            this.failAfter = failAfter;
            this.failuresLeft = maxFailures;
            this.initialized = true;
            reset(); // restart fresh – this time newAggregateIterable wraps with a failing cursor
        }

        @Override
        protected FindIterable<Document> newFindIterable(Bson query, Bson projection, QueryOptions options) {
            FindIterable<Document> real = super.newFindIterable(query, projection, options);
            if (!initialized || failuresLeft == 0) {
                return real;
            }
            failuresLeft--;
            return wrapFind(real);
        }

        @Override
        protected AggregateIterable<Document> newAggregateIterable(List<Bson> activePipeline) {
            AggregateIterable<Document> real = super.newAggregateIterable(activePipeline);
            if (!initialized || failuresLeft == 0) {
                return real;
            }
            failuresLeft--;
            return wrapAggregate(real);
        }

        private FindIterable<Document> wrapFind(FindIterable<Document> real) {
            // Fluent calls are forwarded to `real` so that sort/skip/limit are applied
            // before real.iterator() is opened — important for tests that use skip/limit.
            FindIterable<Document> mockIterable = mock(FindIterable.class);
            when(mockIterable.sort(any(Bson.class))).thenAnswer(inv -> { real.sort(inv.getArgument(0)); return mockIterable; });
            when(mockIterable.batchSize(anyInt())).thenAnswer(inv -> { real.batchSize(inv.<Integer>getArgument(0)); return mockIterable; });
            when(mockIterable.limit(anyInt())).thenAnswer(inv -> { real.limit(inv.<Integer>getArgument(0)); return mockIterable; });
            when(mockIterable.skip(anyInt())).thenAnswer(inv -> { real.skip(inv.<Integer>getArgument(0)); return mockIterable; });
            when(mockIterable.iterator()).thenAnswer(inv -> new FailingMongoCursor(real.iterator(), failAfter));
            return mockIterable;
        }

        private AggregateIterable<Document> wrapAggregate(AggregateIterable<Document> real) {
            AggregateIterable<Document> mockIterable = mock(AggregateIterable.class);
            when(mockIterable.batchSize(anyInt())).thenAnswer(inv -> { real.batchSize(inv.<Integer>getArgument(0)); return mockIterable; });
            when(mockIterable.iterator()).thenAnswer(inv -> new FailingMongoCursor(real.iterator(), failAfter));
            return mockIterable;
        }
    }

    /**
     * Wraps a real {@link MongoCursor} and throws {@link MongoCursorNotFoundException} after
     * exactly {@code failAfter} documents have been returned via {@link #next()}.
     */
    private static class FailingMongoCursor implements MongoCursor<Document> {

        private final MongoCursor<Document> delegate;
        private int remaining;

        FailingMongoCursor(MongoCursor<Document> delegate, int failAfter) {
            this.delegate = delegate;
            this.remaining = failAfter;
        }

        @Override
        public boolean hasNext() {
            if (remaining == 0) {
                throw new MongoCursorNotFoundException(0xDEADBEEFL, new ServerAddress());
            }
            return delegate.hasNext();
        }

        @Override
        public Document next() {
            remaining--;
            return delegate.next();
        }

        @Override
        public Document tryNext() {
            if (remaining == 0) {
                throw new MongoCursorNotFoundException(0xDEADBEEFL, new ServerAddress());
            }
            Document doc = delegate.tryNext();
            if (doc != null) {
                remaining--;
            }
            return doc;
        }

        @Override
        public int available() {
            return delegate.available();
        }

        @Override
        public ServerCursor getServerCursor() {
            return delegate.getServerCursor();
        }

        @Override
        public ServerAddress getServerAddress() {
            return delegate.getServerAddress();
        }

        @Override
        public void close() {
            delegate.close();
        }
    }
}
