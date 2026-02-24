package org.opencb.commons.io;

import org.junit.Assert;
import org.junit.Test;
import org.opencb.commons.run.Task;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

public class DataWriterTest {

    // ===== tee(dw1, dw2) — sequential =====

    @Test
    public void testTeeSequentialBothReceiveSameBatches() {
        List<List<String>> received1 = new ArrayList<>();
        List<List<String>> received2 = new ArrayList<>();
        DataWriter<String> dw1 = b -> { received1.add(new ArrayList<>(b)); return true; };
        DataWriter<String> dw2 = b -> { received2.add(new ArrayList<>(b)); return true; };

        DataWriter<String> tee = DataWriter.tee(dw1, dw2);
        tee.write(Arrays.asList("a", "b"));
        tee.write(Collections.singletonList("c"));

        Assert.assertEquals(received1, received2);
        Assert.assertEquals(2, received1.size());
        Assert.assertEquals(Arrays.asList("a", "b"), received1.get(0));
        Assert.assertEquals(Collections.singletonList("c"), received1.get(1));
    }

    @Test
    public void testTeeSequentialLifecycleCalledOnBothWriters() {
        AtomicBoolean pre1   = new AtomicBoolean();
        AtomicBoolean post1  = new AtomicBoolean();
        AtomicBoolean pre2   = new AtomicBoolean();
        AtomicBoolean post2  = new AtomicBoolean();

        DataWriter<String> dw1 = new DataWriter<String>() {
            @Override public boolean pre()              { pre1.set(true);  return true; }
            @Override public boolean write(List<String> b) { return true; }
            @Override public boolean post()             { post1.set(true); return true; }
        };
        DataWriter<String> dw2 = new DataWriter<String>() {
            @Override public boolean pre()              { pre2.set(true);  return true; }
            @Override public boolean write(List<String> b) { return true; }
            @Override public boolean post()             { post2.set(true); return true; }
        };

        DataWriter<String> tee = DataWriter.tee(dw1, dw2);
        tee.pre();
        tee.write(Collections.singletonList("x"));
        tee.post();

        Assert.assertTrue(pre1.get());
        Assert.assertTrue(pre2.get());
        Assert.assertTrue(post1.get());
        Assert.assertTrue(post2.get());
    }

    // ===== tee(dw1, dw2, false) — explicit non-parallel =====

    @Test
    public void testTeeNonParallelEquivalentToSequential() {
        List<List<String>> received1 = new ArrayList<>();
        List<List<String>> received2 = new ArrayList<>();
        DataWriter<String> dw1 = b -> { received1.add(new ArrayList<>(b)); return true; };
        DataWriter<String> dw2 = b -> { received2.add(new ArrayList<>(b)); return true; };

        DataWriter<String> tee = DataWriter.tee(dw1, dw2, false);
        tee.write(Arrays.asList("x", "y"));

        Assert.assertEquals(received1, received2);
        Assert.assertEquals(1, received1.size());
    }

    // ===== tee(dw1, dw2, true) — parallel =====

    @Test
    public void testTeeParallelBothReceiveAllBatches() throws Exception {
        List<String> received1 = Collections.synchronizedList(new ArrayList<>());
        List<String> received2 = Collections.synchronizedList(new ArrayList<>());
        DataWriter<String> dw1 = b -> { received1.addAll(b); return true; };
        DataWriter<String> dw2 = b -> { received2.addAll(b); return true; };

        DataWriter<String> tee = DataWriter.tee(dw1, dw2, true);
        tee.pre();
        tee.write(Arrays.asList("a", "b", "c"));
        tee.write(Arrays.asList("d", "e"));
        tee.post(); // blocks until both background threads complete

        List<String> expected = Arrays.asList("a", "b", "c", "d", "e");
        Assert.assertEquals(expected, received1);
        Assert.assertEquals(expected, received2);
    }

    @Test
    public void testTeeParallelOpenPrePostCloseCalledOnBothWriters() throws Exception {
        AtomicBoolean open1  = new AtomicBoolean();
        AtomicBoolean pre1   = new AtomicBoolean();
        AtomicBoolean post1  = new AtomicBoolean();
        AtomicBoolean close1 = new AtomicBoolean();
        AtomicBoolean open2  = new AtomicBoolean();
        AtomicBoolean pre2   = new AtomicBoolean();
        AtomicBoolean post2  = new AtomicBoolean();
        AtomicBoolean close2 = new AtomicBoolean();

        DataWriter<String> dw1 = new DataWriter<String>() {
            @Override public boolean open()             { open1.set(true);  return true; }
            @Override public boolean pre()              { pre1.set(true);   return true; }
            @Override public boolean write(List<String> b) { return true; }
            @Override public boolean post()             { post1.set(true);  return true; }
            @Override public boolean close()            { close1.set(true); return true; }
        };
        DataWriter<String> dw2 = new DataWriter<String>() {
            @Override public boolean open()             { open2.set(true);  return true; }
            @Override public boolean pre()              { pre2.set(true);   return true; }
            @Override public boolean write(List<String> b) { return true; }
            @Override public boolean post()             { post2.set(true);  return true; }
            @Override public boolean close()            { close2.set(true); return true; }
        };

        DataWriter<String> tee = DataWriter.tee(dw1, dw2, true);
        tee.pre();
        tee.write(Collections.singletonList("x"));
        tee.post(); // joins background threads → all lifecycle steps completed

        Assert.assertTrue(open1.get());
        Assert.assertTrue(pre1.get());
        Assert.assertTrue(post1.get());
        Assert.assertTrue(close1.get());
        Assert.assertTrue(open2.get());
        Assert.assertTrue(pre2.get());
        Assert.assertTrue(post2.get());
        Assert.assertTrue(close2.get());
    }

    @Test(expected = RuntimeException.class)
    public void testTeeParallelExceptionInBackgroundWriterRethrownOnPost() throws Exception {
        // dw1 always throws; the error must surface when post() joins the background thread
        DataWriter<String> failing = b -> { throw new RuntimeException("write failed"); };
        DataWriter<String> ok      = b -> true;

        DataWriter<String> tee = DataWriter.tee(failing, ok, true);
        tee.pre();
        tee.write(Collections.singletonList("x"));
        tee.post(); // background thread for 'failing' set error1; post() must rethrow it
    }

    @Test(expected = RuntimeException.class)
    public void testTeeParallelExceptionDetectedOnSubsequentWrite() throws Exception {
        // After the background thread has failed, the next write() should detect it
        DataWriter<String> failing = b -> { throw new RuntimeException("write failed"); };
        DataWriter<String> ok      = b -> true;

        DataWriter<String> tee = DataWriter.tee(failing, ok, true);
        tee.pre();
        tee.write(Collections.singletonList("trigger-failure"));
        Thread.sleep(200); // wait for background thread to process and set the error
        tee.write(Collections.singletonList("should-throw")); // error already set → throws
    }

    // ===== asTask() =====

    @Test
    public void testAsTaskDelegatesFullLifecycle() throws Exception {
        AtomicBoolean opened  = new AtomicBoolean();
        AtomicBoolean pre     = new AtomicBoolean();
        AtomicBoolean post    = new AtomicBoolean();
        AtomicBoolean closed  = new AtomicBoolean();
        List<List<String>> written = new ArrayList<>();

        DataWriter<String> dw = new DataWriter<String>() {
            @Override public boolean open()             { opened.set(true); return true; }
            @Override public boolean pre()              { pre.set(true);    return true; }
            @Override public boolean write(List<String> b) { written.add(new ArrayList<>(b)); return true; }
            @Override public boolean post()             { post.set(true);   return true; }
            @Override public boolean close()            { closed.set(true); return true; }
        };

        Task<String, String> task = dw.asTask();
        task.pre();
        List<String> result = task.apply(Arrays.asList("x", "y"));
        task.post();

        Assert.assertTrue(opened.get());
        Assert.assertTrue(pre.get());
        Assert.assertTrue(post.get());
        Assert.assertTrue(closed.get());
        Assert.assertEquals(1, written.size());
        Assert.assertEquals(Arrays.asList("x", "y"), result); // batch passed through
    }

    @Test
    public void testAsTaskPreAndPostAreIdempotent() throws Exception {
        // open/pre are called only once even if task.pre() is invoked multiple times,
        // same for post/close.
        AtomicInteger preCount  = new AtomicInteger();
        AtomicInteger postCount = new AtomicInteger();

        DataWriter<String> dw = new DataWriter<String>() {
            @Override public boolean pre()              { preCount.incrementAndGet();  return true; }
            @Override public boolean write(List<String> b) { return true; }
            @Override public boolean post()             { postCount.incrementAndGet(); return true; }
        };

        Task<String, String> task = dw.asTask();
        task.pre();
        task.pre();
        task.post();
        task.post();

        Assert.assertEquals(1, preCount.get());
        Assert.assertEquals(1, postCount.get());
    }

    // ===== then(DataWriter) =====

    @Test
    public void testThenDataWriterBothWritersReceiveSameBatches() {
        List<String> received1 = new ArrayList<>();
        List<String> received2 = new ArrayList<>();
        DataWriter<String> dw1 = b -> { received1.addAll(b); return true; };
        DataWriter<String> dw2 = b -> { received2.addAll(b); return true; };

        DataWriter<String> chained = dw1.then(dw2);
        chained.write(Arrays.asList("a", "b"));
        chained.write(Collections.singletonList("c"));

        Assert.assertEquals(received1, received2);
        Assert.assertEquals(Arrays.asList("a", "b", "c"), received1);
    }

    @Test
    public void testThenDataWriterLifecycleCalledOnBoth() {
        AtomicBoolean pre1  = new AtomicBoolean();
        AtomicBoolean post1 = new AtomicBoolean();
        AtomicBoolean pre2  = new AtomicBoolean();
        AtomicBoolean post2 = new AtomicBoolean();

        DataWriter<String> dw1 = new DataWriter<String>() {
            @Override public boolean pre()              { pre1.set(true);  return true; }
            @Override public boolean write(List<String> b) { return true; }
            @Override public boolean post()             { post1.set(true); return true; }
        };
        DataWriter<String> dw2 = new DataWriter<String>() {
            @Override public boolean pre()              { pre2.set(true);  return true; }
            @Override public boolean write(List<String> b) { return true; }
            @Override public boolean post()             { post2.set(true); return true; }
        };

        DataWriter<String> chained = dw1.then(dw2);
        chained.pre();
        chained.write(Collections.singletonList("x"));
        chained.post();

        Assert.assertTrue(pre1.get());
        Assert.assertTrue(pre2.get());
        Assert.assertTrue(post1.get());
        Assert.assertTrue(post2.get());
    }
}
