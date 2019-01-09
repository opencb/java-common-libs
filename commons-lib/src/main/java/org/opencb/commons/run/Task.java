package org.opencb.commons.run;

import org.opencb.commons.io.DataWriter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

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
        return then(writer.asTask(false));
    }


    /**
     * Use to execute multiple Tasks with the same input.
     * Only the output of the main task will be propagated.
     *
     * task = Task.join(task1, task2);
     *
     * @param mainTask    Main task to propagate
     * @param otherTask   Task to execute with the same input. The output will be lost.
     * @param <T>         Input type.
     * @param <R>         Return type.
     * @return            Task that runs both tasks with the same input.
     */
    static <T, R> Task<T, R> join(Task<T, R> mainTask, Task<T, ?> otherTask) {
        return new Task<T, R>() {
            @Override
            public void pre() throws Exception {
                mainTask.pre();
                otherTask.pre();
            }

            @Override
            public List<R> apply(List<T> batch) throws Exception {
                List<R> apply1 = mainTask.apply(batch);
                otherTask.apply(batch); // ignore output
                return apply1;
            }

            @Override
            public List<R> drain() throws Exception {
                // Drain both tasks
                List<R> drain1 = mainTask.drain();
                otherTask.drain(); // ignore output

                // Return drain1
                return drain1;
            }

            @Override
            public void post() throws Exception {
                mainTask.post();
                otherTask.post();
            }
        };
    }

}
