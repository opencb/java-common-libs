package org.opencb.commons.run;

import org.junit.Assert;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;

public class TaskTest {

    // ===== forEach(Function) =====

    @Test
    public void testForEachFunctionTransforms() throws Exception {
        Task<String, Integer> task = Task.forEach(String::length);
        List<Integer> result = task.apply(Arrays.asList("a", "bb", "ccc"));
        Assert.assertEquals(Arrays.asList(1, 2, 3), result);
    }

    @Test
    public void testForEachFunctionFiltersNullReturns() throws Exception {
        // Elements where the function returns null are excluded from the output
        Task<String, Integer> task = Task.forEach(s -> s.startsWith("a") ? s.length() : null);
        List<Integer> result = task.apply(Arrays.asList("abc", "xyz", "ab"));
        Assert.assertEquals(Arrays.asList(3, 2), result);
    }

    @Test
    public void testForEachFunctionAllNullReturnsEmptyList() throws Exception {
        Task<String, Integer> task = Task.forEach(s -> null);
        List<Integer> result = task.apply(Arrays.asList("a", "b"));
        Assert.assertEquals(Collections.emptyList(), result);
    }

    @Test
    public void testForEachFunctionEmptyBatch() throws Exception {
        Task<String, Integer> task = Task.forEach(String::length);
        Assert.assertEquals(Collections.emptyList(), task.apply(Collections.emptyList()));
    }

    @Test
    public void testForEachFunctionNullBatch() throws Exception {
        Task<String, Integer> task = Task.forEach(String::length);
        Assert.assertEquals(Collections.emptyList(), task.apply(null));
    }

    // ===== forEach(Consumer) =====

    @Test
    public void testForEachConsumerRunsOnEachElementAndPassesThrough() throws Exception {
        List<String> visited = new ArrayList<>();
        Task<String, String> task = Task.forEach((Consumer<String>) s -> { visited.add(s); });
        List<String> input = Arrays.asList("a", "b", "c");

        List<String> result = task.apply(input);

        Assert.assertEquals(input, visited);   // consumer received every element
        Assert.assertEquals(input, result);    // original batch passed through unchanged
    }

    @Test
    public void testForEachConsumerEmptyBatch() throws Exception {
        Task<String, String> task = Task.forEach((Consumer<String>) s -> {});
        Assert.assertEquals(Collections.emptyList(), task.apply(Collections.emptyList()));
    }

    @Test
    public void testForEachConsumerNullBatch() throws Exception {
        Task<String, String> task = Task.forEach((Consumer<String>) s -> {});
        Assert.assertEquals(Collections.emptyList(), task.apply(null));
    }

    // ===== then(Task) =====

    @Test
    public void testThenChainsOutputToNextTaskInput() throws Exception {
        Task<String, Integer> lengths = Task.forEach(String::length);
        Task<Integer, String> labels  = Task.forEach(i -> "len=" + i);

        List<String> result = lengths.then(labels).apply(Arrays.asList("hello", "hi"));
        Assert.assertEquals(Arrays.asList("len=5", "len=2"), result);
    }

    @Test
    public void testThenPreAndPostCalledOnBothTasks() throws Exception {
        AtomicBoolean pre1  = new AtomicBoolean();
        AtomicBoolean post1 = new AtomicBoolean();
        AtomicBoolean pre2  = new AtomicBoolean();
        AtomicBoolean post2 = new AtomicBoolean();

        Task<String, String> task1 = new Task<String, String>() {
            @Override public void pre()                       { pre1.set(true); }
            @Override public List<String> apply(List<String> b) { return b; }
            @Override public void post()                      { post1.set(true); }
        };
        Task<String, String> task2 = new Task<String, String>() {
            @Override public void pre()                       { pre2.set(true); }
            @Override public List<String> apply(List<String> b) { return b; }
            @Override public void post()                      { post2.set(true); }
        };

        Task<String, String> combined = task1.then(task2);
        combined.pre();
        combined.post();

        Assert.assertTrue(pre1.get());
        Assert.assertTrue(pre2.get());
        Assert.assertTrue(post1.get());
        Assert.assertTrue(post2.get());
    }

    @Test
    public void testThenDrainFromFirstTaskFeedsIntoSecond() throws Exception {
        // task1 drains an extra element; task2 uppercases everything
        Task<String, String> task1 = new Task<String, String>() {
            @Override public List<String> apply(List<String> b)  { return b; }
            @Override public List<String> drain()                { return Collections.singletonList("drained"); }
        };
        Task<String, String> task2 = Task.forEach(s -> {
            return s.toUpperCase();
        });

        // drain1 = ["drained"] → task2.apply(drain1) = ["DRAINED"], task2.drain() = []
        List<String> drain = task1.then(task2).drain();
        Assert.assertEquals(Collections.singletonList("DRAINED"), drain);
    }

    @Test
    public void testThenDrainCombinesBothDrains() throws Exception {
        // Both tasks drain something; the combined result should include both contributions
        Task<String, String> task1 = new Task<String, String>() {
            @Override public List<String> apply(List<String> b) { return b; }
            @Override public List<String> drain()               { return Collections.singletonList("from-task1"); }
        };
        Task<String, String> task2 = new Task<String, String>() {
            @Override public List<String> apply(List<String> b) { return b; }
            @Override public List<String> drain()               { return Collections.singletonList("from-task2"); }
        };

        // task2.apply(["from-task1"]) = ["from-task1"], task2.drain() = ["from-task2"]
        List<String> drain = task1.then(task2).drain();
        Assert.assertEquals(Arrays.asList("from-task1", "from-task2"), drain);
    }

    // ===== tee(mainTask, sideTask) =====

    @Test
    public void testTeeMainOutputPropagated() throws Exception {
        Task<String, Integer> main = Task.forEach(String::length);
        Task<String, String> side  = Task.forEach(s -> {
            return s.toUpperCase();
        }); // output discarded

        List<Integer> result = Task.tee(main, side).apply(Arrays.asList("hello", "world"));
        Assert.assertEquals(Arrays.asList(5, 5), result);
    }

    @Test
    public void testTeeSideTaskReceivesSameInputAsBatch() throws Exception {
        List<String> sideInput = new ArrayList<>();
        Task<String, Integer> main = Task.forEach(String::length);
        Task<String, String> side  = Task.forEach(s -> { sideInput.add(s); return s; });

        Task.tee(main, side).apply(Arrays.asList("hello", "world"));
        Assert.assertEquals(Arrays.asList("hello", "world"), sideInput);
    }

    @Test
    public void testTeePrePostAndDrainCalledOnBothTasks() throws Exception {
        AtomicBoolean preMain  = new AtomicBoolean();
        AtomicBoolean postMain = new AtomicBoolean();
        AtomicBoolean preSide  = new AtomicBoolean();
        AtomicBoolean postSide = new AtomicBoolean();
        AtomicBoolean drainSide = new AtomicBoolean();

        Task<String, String> main = new Task<String, String>() {
            @Override public void pre()                         { preMain.set(true); }
            @Override public List<String> apply(List<String> b) { return b; }
            @Override public void post()                        { postMain.set(true); }
        };
        Task<String, String> side = new Task<String, String>() {
            @Override public void pre()                         { preSide.set(true); }
            @Override public List<String> apply(List<String> b) { return b; }
            @Override public List<String> drain()               { drainSide.set(true); return Collections.emptyList(); }
            @Override public void post()                        { postSide.set(true); }
        };

        Task<String, String> tee = Task.tee(main, side);
        tee.pre();
        tee.drain();
        tee.post();

        Assert.assertTrue(preMain.get());
        Assert.assertTrue(preSide.get());
        Assert.assertTrue(postMain.get());
        Assert.assertTrue(postSide.get());
        Assert.assertTrue(drainSide.get());
    }

    @Test
    public void testTeeSideExceptionPropagates() throws Exception {
        Task<String, Integer> main = Task.forEach(String::length);
        Task<String, String> side  = batch -> { throw new RuntimeException("side failed"); };

        try {
            Task.tee(main, side).apply(Collections.singletonList("x"));
            Assert.fail("Expected exception from side task");
        } catch (RuntimeException e) {
            Assert.assertEquals("side failed", e.getMessage());
        }
    }

    // ===== join (deprecated alias for tee) =====

    @Test
    public void testJoinDelegatesToTee() throws Exception {
        List<String> sideInput = new ArrayList<>();
        Task<String, Integer> main = Task.forEach(String::length);
        Task<String, String> side  = Task.forEach(s -> { sideInput.add(s); return s; });

        @SuppressWarnings("deprecation")
        List<Integer> result = Task.join(main, side).apply(Arrays.asList("abc", "de"));

        Assert.assertEquals(Arrays.asList(3, 2), result);
        Assert.assertEquals(Arrays.asList("abc", "de"), sideInput);
    }
}
