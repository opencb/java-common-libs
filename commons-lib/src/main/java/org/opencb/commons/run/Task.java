package org.opencb.commons.run;

import org.opencb.commons.io.DataWriter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Created on 13/02/18.
 *
 * @author Jacobo Coll &lt;jacobo167@gmail.com&gt;
 */
@FunctionalInterface
public interface Task<T, R> {
    default void pre() throws Exception {
    }

    List<R> apply(List<T> batch) throws Exception;

    default List<R> drain() throws Exception {
        return Collections.emptyList();
    }

    default void post() throws Exception {
    }

    /**
     * Use to concatenate Tasks.
     *
     * task1.then(task2).then(task3);
     *
     * @param nextTask  Task to concatenate
     * @param <NR>      New return type.
     * @return          Task that concatenates the current and the given task.
     */
    default <NR> Task<T, NR> then(Task<R, NR> nextTask) {
        Task<T, R> thisTask = this;
        return new Task<T, NR>() {
            @Override
            public void pre() throws Exception {
                thisTask.pre();
                nextTask.pre();
            }

            @Override
            public List<NR> apply(List<T> batch) throws Exception {
                List<R> apply1 = thisTask.apply(batch);
                return nextTask.apply(apply1);
            }

            @Override
            public List<NR> drain() throws Exception {
                // Drain both tasks
                List<R> drain1 = thisTask.drain();
                // Create new list, in case it is not modifiable
                List<NR> batch2 = new ArrayList<>(nextTask.apply(drain1));
                List<NR> drain2 = nextTask.drain();
                batch2.addAll(drain2);
                return batch2;
            }

            @Override
            public void post() throws Exception {
                thisTask.post();
                nextTask.post();
            }
        };
    }

    /**
     * Use to concatenate a DataWriter as a task. Allows parallel writing.
     *
     * task1.then(writer);
     *
     * @param writer    Write step to concatenate
     * @return          Task that concatenates the current task with the given writer.
     */
    default Task<T, R> then(DataWriter<R> writer) {
        AtomicBoolean pre = new AtomicBoolean(false);
        AtomicBoolean post = new AtomicBoolean(false);
        Task<T, R> task = this;
        return new Task<T, R>() {
            @Override
            public void pre() throws Exception {
                if (!pre.getAndSet(true)) {
                    writer.open();
                    writer.pre();
                }
                task.pre();
            }

            @Override
            public List<R> apply(List<T> batch) throws Exception {
                List<R> batch2 = task.apply(batch);
                writer.write(batch2);
                return batch2;
            }

            @Override
            public List<R> drain() throws Exception {
                // Drain and write
                List<R> drain = task.drain();
                writer.write(drain);
                return drain;
            }

            @Override
            public void post() throws Exception {
                task.post();
                if (!post.getAndSet(true)) {
                    writer.post();
                    writer.close();
                }
            }
        };
    }
}
