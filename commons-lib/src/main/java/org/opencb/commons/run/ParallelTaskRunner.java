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

package org.opencb.commons.run;

import org.apache.commons.lang3.StringUtils;
import org.opencb.commons.io.DataReader;
import org.opencb.commons.io.DataWriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.DecimalFormat;
import java.util.*;
import java.util.concurrent.*;
import java.util.function.Supplier;

/**
 * Created by hpccoll1 on 26/02/15.
 *
 * {@link DataReader} Producer , {@link org.opencb.commons.run.Task} Worker, {@link DataWriter}Consumer
 *         ___           ___
 *         |_|  -> T ->  |_|
 *   R ->  |_|  -> T ->  |_| -> W
 *         |_|  -> T ->  |_|
 *
 * Sorted runner:
 * Require reader, tasks and writer.
 * For each batch read by the reader, a new future will be created
 * and added to the queue. This will ensure the sorted output. The
 * worker threads will complete the future actions
 * ({@link CompletableFuture::complete}), taking them from the
 * CompletableFuture map. The writer will take tasks from the
 * queue, and will {@link Future::get} the batch from the future.
 * If the batch was not read, the thread will be blocked reading
 * from the queue. If the batch was not processed, will be blocked
 * by the future.
 */
public class ParallelTaskRunner<I, O> {


    public static final int TIMEOUT_CHECK = 1;
    private static final int EXTRA_AWAIT_TERMINATION_TIMEOUT = 1000;
    private static final int RETRY_AWAIT_TERMINATION_TIMEOUT = 50;
    private static final int MAX_SHUTDOWN_RETRIES = 300;
    private static final DecimalFormat DECIMAL_FORMAT = new DecimalFormat("0.000");
    private long startTime;
    private boolean interrupted;

    @FunctionalInterface
    @Deprecated
    /**
     * @deprecated Use {@link org.opencb.commons.run.Task}
     */
    public interface Task<T, R> extends TaskWithException<T, R, RuntimeException> {
    }

    @FunctionalInterface
    @Deprecated
    /**
     * @deprecated Use {@link org.opencb.commons.run.Task}
     */
    public interface TaskWithException<T, R, E extends Exception> extends org.opencb.commons.run.Task<T, R> {
    }

    @SuppressWarnings("unchecked")
    private static final Batch POISON_PILL = new Batch(Collections.emptyList(), -1);

    private final DataReader<I> reader;
    private final DataWriter<O> writer;
    private final List<org.opencb.commons.run.Task<I, O>> tasks;
    private final Config config;

    private final List<TaskRunnable> taskRunnables = new ArrayList<>();

    private ExecutorService executorService;
    private BlockingQueue<Batch<I>> readBlockingQueue;
    // Unsorted blocking queue
    private BlockingQueue<Batch<O>> writeBlockingQueue;
    // Sorted blocking queue.
    private BlockingQueue<Future<Batch<O>>> writeBlockingQueueFuture;
    private Map<Integer, CompletableFuture<Batch<O>>> writeBlockingQueueFutureMap;

    private int numBatches = 0;
    private int finishedTasks = 0;
    private long timeBlockedAtPutRead = 0;
    private long timeBlockedAtTakeRead = 0;
    private long timeBlockedAtPutWrite = 0;
    private long timeBlockedAtTakeWrite = 0;
    private long timeReading = 0;
    private long timeTaskApply = 0;
    private long timeWriting;

    private List<Future> futureTasks;
    private List<Throwable> exceptions;
    private List<Error> errors;
    // Main thread interruptions
    private List<InterruptedException> interruptions;

    protected static Logger logger = LoggerFactory.getLogger(ParallelTaskRunner.class);

    public static class Config {
        @Deprecated
        public Config(int numTasks, int batchSize, int capacity, boolean sorted) {
            this(numTasks, batchSize, capacity, true, sorted);
        }

        @Deprecated
        public Config(int numTasks, int batchSize, int capacity, boolean abortOnFail, boolean sorted) {
            this(numTasks, batchSize, capacity, abortOnFail, sorted, 500);
        }

        @Deprecated
        public Config(int numTasks, int batchSize, int capacity, boolean abortOnFail, boolean sorted, int readQueuePutTimeoutSeconds) {
            this.numTasks = numTasks;
            this.batchSize = batchSize;
            this.capacity = capacity;
            this.abortOnFail = abortOnFail;
            this.sorted = sorted;
            this.readQueuePutTimeoutSeconds = readQueuePutTimeoutSeconds;
        }

        public static Builder builder() {
            return new Builder();
        }

        public static class Builder {
            private int numTasks = 6;
            private int batchSize = 50;
            private int capacity = -1;
            private boolean sorted = false;
            private boolean abortOnFail = true;
            private int readQueuePutTimeoutSeconds = 500;

            public Builder setNumTasks(int numTasks) {
                this.numTasks = numTasks;
                return this;
            }

            public Builder setBatchSize(int batchSize) {
                this.batchSize = batchSize;
                return this;
            }

            public Builder setCapacity(int capacity) {
                this.capacity = capacity;
                return this;
            }

            public Builder setSorted(boolean sorted) {
                this.sorted = sorted;
                return this;
            }

            public Builder setAbortOnFail(boolean abortOnFail) {
                this.abortOnFail = abortOnFail;
                return this;
            }

            public Builder setReadQueuePutTimeout(int readQueuePutTimeoutInSeconds) {
                return setReadQueuePutTimeout(readQueuePutTimeoutInSeconds, TimeUnit.SECONDS);
            }

            public Builder setReadQueuePutTimeout(int readQueuePutTimeout, TimeUnit timeUnit) {
                this.readQueuePutTimeoutSeconds = (int) timeUnit.toSeconds(readQueuePutTimeout);
                return this;
            }

            public ParallelTaskRunner.Config build() {
                if (capacity < 0) {
                    capacity = numTasks * 2;
                }
                return new ParallelTaskRunner.Config(numTasks, batchSize, capacity, abortOnFail, sorted, readQueuePutTimeoutSeconds);
            }
        }

        private final int numTasks;
        private final int batchSize;
        private final int capacity;
        private final boolean abortOnFail;
        private final boolean sorted;
        private final int readQueuePutTimeoutSeconds;

        public int getNumTasks() {
            return numTasks;
        }

        public int getBatchSize() {
            return batchSize;
        }

        public int getCapacity() {
            return capacity;
        }

        public boolean isAbortOnFail() {
            return abortOnFail;
        }

        public boolean isSorted() {
            return sorted;
        }

        public int getReadQueuePutTimeout() {
            return getReadQueuePutTimeout(TimeUnit.SECONDS);
        }

        public int getReadQueuePutTimeout(TimeUnit timeUnit) {
            return (int) timeUnit.convert(readQueuePutTimeoutSeconds, TimeUnit.SECONDS);
        }
    }

    private static final class Batch<T> implements Comparable<Batch<T>> {
        private final List<T> batch;
        private final int position;

        private Batch(List<T> batch, int position) {
            this.batch = batch;
            this.position = position;
        }

        @Override
        public int compareTo(Batch<T> o) {
            return Integer.compare(position, o.position);
        }
    }

    /**
     * @param reader Unique DataReader. If null, empty batches will be generated
     * @param task   Task to be used. Will be used the same instance in all threads
     * @param writer Unique DataWriter. If null, data generated by the task will be lost.
     * @param config configuration.
     * @throws IllegalArgumentException Exception.
     */
    public ParallelTaskRunner(DataReader<I> reader, org.opencb.commons.run.Task<I, O> task, DataWriter<O> writer, Config config) {
        this.config = config;
        this.reader = reader;
        this.writer = writer;
        this.tasks = new ArrayList<>(config.numTasks);
        for (int i = 0; i < config.numTasks; i++) {
            tasks.add(task);
        }

        check();
    }

    /**
     * @param reader       Unique DataReader. If null, empty batches will be generated.
     * @param taskSupplier TaskGenerator. Will generate a new task for each thread.
     * @param writer       Unique DataWriter. If null, data generated by the task will be lost.
     * @param config configuration.
     * @throws IllegalArgumentException Exception.
     */
    public ParallelTaskRunner(DataReader<I> reader, Supplier<? extends org.opencb.commons.run.Task<I, O>> taskSupplier,
                              DataWriter<O> writer, Config config) {
        this.config = config;
        this.reader = reader;
        this.writer = writer;
        this.tasks = new ArrayList<>(config.numTasks);
        for (int i = 0; i < config.numTasks; i++) {
            tasks.add(taskSupplier.get());
        }

        check();
        interrupted = false;
    }

    /**
     * @param reader Unique DataReader. If null, empty batches will be generated
     * @param tasks  Generated Tasks. Each task will be used in one thread. Will use tasks.size() as "numTasks".
     * @param writer Unique DataWriter. If null, data generated by the task will be lost.
     * @param config configuration.
     * @throws IllegalArgumentException Exception.
     */
    public ParallelTaskRunner(DataReader<I> reader, List<? extends org.opencb.commons.run.Task<I, O>> tasks,
                              DataWriter<O> writer, Config config) {
        this.config = config;
        this.reader = reader;
        this.writer = writer;
        this.tasks = new ArrayList<>(tasks);

        check();
        interrupted = false;
    }

    private void check()  {
        if (reader == null && config.sorted) {
            throw new IllegalArgumentException("Unable to execute a sorted ParallelTaskRunner without a reader!!");
        }
        if (writer == null && config.sorted) {
            throw new IllegalArgumentException("Unable to execute a sorted ParallelTaskRunner without a writer!!");
        }
        if (tasks == null || tasks.isEmpty()) {
            throw new IllegalArgumentException("Must provide at least one task");
        }
        if (tasks.size() != config.numTasks) {
            logger.warn("Different number of provided tasks ({}) than numTasks in configuration ({})", tasks.size(), config.numTasks);
        }
        return;
    }

    private void init() {
        startTime = System.nanoTime();
        interrupted = false;
        finishedTasks = 0;
        if (reader != null) {
            readBlockingQueue = new ArrayBlockingQueue<>(config.capacity);
        }

        if (writer != null) {
            if (config.sorted) {
                writeBlockingQueueFuture = new ArrayBlockingQueue<>(config.capacity);
                writeBlockingQueueFutureMap = new ConcurrentHashMap<>();
            } else {
                writeBlockingQueue = new ArrayBlockingQueue<>(config.capacity);
            }
        }

        executorService = Executors.newFixedThreadPool(tasks.size() + (writer == null ? 0 : 1));
        futureTasks = new ArrayList<Future>(); // assume no parallel access to this list
        exceptions = Collections.synchronizedList(new LinkedList<>());
        interruptions = Collections.synchronizedList(new LinkedList<>());
    }

    public void run() throws ExecutionException {
        try {
            run(Long.MAX_VALUE, TimeUnit.DAYS);
        } catch (InterruptedException e) {
            throw buildExecutionException("Error while running ParallelTaskRunner. Found " + interruptions.size()
                    + " interruptions.", interruptions);
        }
    }

    public void run(long timeout, TimeUnit unit) throws ExecutionException, InterruptedException {
        //If there is any InterruptionException, finish as quick as possible.
        init();

        Thread hook = new Thread(() -> {
            logger.warn("Shutdown hook called! Aborting ParallelTaskRunner execution!");
            logTimes();
        });
        Runtime.getRuntime().addShutdownHook(hook);
        try {
            start(timeout, unit);
        } finally {
            Runtime.getRuntime().removeShutdownHook(hook);
            logTimes();
        }

        if (config.abortOnFail && !exceptions.isEmpty()) {
            throw buildExecutionException("Error while running ParallelTaskRunner. Found " + exceptions.size() + " exceptions.",
                    exceptions);
        }
        if (interrupted) {
            throw interruptions.get(0);
        }
    }

    private void start(long timeout, TimeUnit unit) throws ExecutionException {
        long auxTime = System.nanoTime();
        if (reader != null) {
            reader.open();
            reader.pre();
        }
        timeReading += System.nanoTime() - auxTime;

        auxTime = System.nanoTime();
        if (writer != null) {
            writer.open();
            writer.pre();
        }
        timeWriting += System.nanoTime() - auxTime;

        for (org.opencb.commons.run.Task<I, O> task : tasks) {
            try {
                task.pre();
            } catch (Exception e) {
                // TODO: Improve exception handler
                throw new ExecutionException(e);
            }
        }

        for (org.opencb.commons.run.Task<I, O> task : tasks) {
            doSubmit(new TaskRunnable(task));
        }
        if (writer != null) {
            doSubmit(new WriterRunnable(writer));
        }
        try {
            if (reader != null) {
                interrupted = readLoop();  //Use the main thread for reading
            }

            executorService.shutdown();
            // If interrupted, do not await for termination.
            if (!interrupted) {
                try {
                    executorService.awaitTermination(timeout, unit); // TODO further action - this is not good!!!
                } catch (InterruptedException e) {
                    interruptions.add(e);
                    interrupted = true;
                    logger.warn("Catch interrupted exception!", e);
                }
            }
        } catch (TimeoutException e) {
            exceptions.add(e);
            logger.warn("Catch interrupted exception!", e);
        } finally {
            if (!executorService.isShutdown()) {
                executorService.shutdownNow(); // shut down now if not done so (e.g. execption)
            }
        }

        //Avoid execute POST and CLOSE if the threads are still alive.
        int shutdownRetries = 0;
        try {
            // Wait extra time
            if (!executorService.isTerminated()) {
                executorService.awaitTermination(EXTRA_AWAIT_TERMINATION_TIMEOUT, TimeUnit.MILLISECONDS);
            }
            while (!executorService.isTerminated() && shutdownRetries < MAX_SHUTDOWN_RETRIES) {
                shutdownRetries++;
                executorService.awaitTermination(RETRY_AWAIT_TERMINATION_TIMEOUT, TimeUnit.MILLISECONDS);
                logger.debug("Executor is not terminated!! Shutdown now! - " + shutdownRetries);
                executorService.shutdownNow();
                for (Future future : futureTasks) {
                    future.cancel(true);
                }
            }
        } catch (InterruptedException e) {
            // Stop trying to stop the ExecutorService
            interruptions.add(e);
            logger.warn("Catch interrupted exception!", e);
            interrupted = true;
        }

        // If interrupted, skip POST steps. Only close.

        if (!interrupted) {
            for (org.opencb.commons.run.Task<I, O> task : tasks) {
                try {
                    task.post();
                } catch (Exception e) {
                    // TODO: Improve exception handler
                    throw new ExecutionException(e);
                }
            }
        }
        auxTime = System.nanoTime();
        if (reader != null) {
            if (!interrupted) {
                reader.post();
            }
            reader.close();
        }
        timeReading += System.nanoTime() - auxTime;

        auxTime = System.nanoTime();
        if (writer != null) {
            if (!interrupted) {
                writer.post();
            }
            writer.close();
        }
        timeWriting += System.nanoTime() - auxTime;
    }

    public void logTimes() {
        logger.info(toString());
        if (reader != null) {
            logger.info("read:  timeReading                  = " + durationToString(timeReading));
            logger.info("read:  timeBlockedAtPutRead         = " + durationToString(timeBlockedAtPutRead));
            logger.info("task:  timeBlockedAtTakeRead        = " + durationToString(timeBlockedAtTakeRead) + " (total)"
                    + ",   ~" + durationToString(timeBlockedAtTakeRead / config.numTasks) + " (per thread)");
        }

        logger.info("task:  timeTaskApply                = " + durationToString(timeTaskApply) + " (total)"
                + ",   ~" + durationToString(timeTaskApply / config.numTasks) + " (per thread)");

        if (writer != null) {
            logger.info("task:  timeBlockedAtPutWrite        = " + durationToString(timeBlockedAtPutWrite) + " (total)"
                    + ",   ~" + durationToString(timeBlockedAtPutWrite / config.numTasks) + " (per thread)");
            logger.info("write: timeBlockedWaitingDataToWrite = " + durationToString(timeBlockedAtTakeWrite));
            logger.info("write: timeWriting                  = " + durationToString(timeWriting));
        }

        logger.info("total:                              = " + durationToString(System.nanoTime() - startTime));
    }

    private ExecutionException buildExecutionException(String message, List<? extends Throwable> exceptions) {
        ExecutionException executionException;
        if (exceptions.size() == 1) {
            executionException = new ExecutionException(message, exceptions.get(0));
        } else {
            executionException = new ExecutionException(message, null);
            for (Throwable exception : exceptions) {
                executionException.addSuppressed(exception);
            }
        }
        return executionException;
    }

    private static String durationToString(long durationInNanos) {
        long durationInMillis = TimeUnit.NANOSECONDS.toMillis(durationInNanos);
        long durationInSeconds = Math.round(durationInMillis / 1000.0);
        long h = durationInSeconds / 3600;
        long m = (durationInSeconds % 3600) / 60;
        long s = durationInSeconds % 60;
        return (DECIMAL_FORMAT.format(durationInMillis / 1000.0)) + "s [ " + StringUtils.leftPad(String.valueOf(h), 2, '0') + ':'
                + StringUtils.leftPad(String.valueOf(m), 2, '0') + ':'
                + StringUtils.leftPad(String.valueOf(s), 2, '0') + " ]";
    }


    private String prettyTime(long time) {
        return DECIMAL_FORMAT.format(TimeUnit.NANOSECONDS.toMillis(time) / 1000.0);
    }

    public List<Throwable> getExceptions() {
        return exceptions;
    }

    public List<Error> getErrors() {
        return errors;
    }

    public long getTimeBlockedAtPutRead(TimeUnit unit) {
        return TimeUnit.NANOSECONDS.convert(timeBlockedAtPutRead, unit);
    }

    public long getTimeBlockedAtTakeRead(TimeUnit unit) {
        return TimeUnit.NANOSECONDS.convert(timeBlockedAtTakeRead, unit);
    }

    public long getTimeBlockedAtPutWrite(TimeUnit unit) {
        return TimeUnit.NANOSECONDS.convert(timeBlockedAtPutWrite, unit);
    }

    public long getTimeBlockedAtTakeWrite(TimeUnit unit) {
        return TimeUnit.NANOSECONDS.convert(timeBlockedAtTakeWrite, unit);
    }

    public long getTimeReading(TimeUnit unit) {
        return TimeUnit.NANOSECONDS.convert(timeReading, unit);
    }

    public long getTimeTaskApply(TimeUnit unit) {
        return TimeUnit.NANOSECONDS.convert(timeTaskApply, unit);
    }

    public long getTimeWriting(TimeUnit unit) {
        return TimeUnit.NANOSECONDS.convert(timeWriting, unit);
    }

    private void doSubmit(TaskRunnable taskRunnable) {
        Future ftask = executorService.submit(taskRunnable);
        futureTasks.add(ftask);
        taskRunnables.add(taskRunnable);
    }

    private void doSubmit(WriterRunnable taskRunnable) {
        Future ftask = executorService.submit(taskRunnable);
        futureTasks.add(ftask);
    }

    /**
     *
     * @return Returns if the tread has been interrupted
     * @throws TimeoutException
     * @throws ExecutionException
     */
    private boolean readLoop() throws TimeoutException, ExecutionException {
        try {
            long start;
            Batch<I> batch;

            batch = readBatch();

            while (batch.batch != null && !batch.batch.isEmpty()) {

                // If sorted, add futures in a sorted way to the writer queue
                if (config.sorted) {
                    CompletableFuture<Batch<O>> completableFuture = new CompletableFuture<>();
                    while (!writeBlockingQueueFuture.offer(completableFuture, TIMEOUT_CHECK, TimeUnit.SECONDS)) {
                        if (isAbortPending()) {
                            break;
                        }
                        if (Thread.currentThread().isInterrupted()) {
                            // Break loop if thread is interrupted
                            break;
                        }
                    }
                    writeBlockingQueueFutureMap.put(batch.position, completableFuture);
                }

                //logger.trace("reader: prePut readBlockingQueue " + readBlockingQueue.size());
                start = System.nanoTime();
                int cntloop = 0;
                // continues lock of queue if jobs fail - check what's happening!!!
                while (!readBlockingQueue.offer(batch, TIMEOUT_CHECK, TimeUnit.SECONDS)) {
                    if (Thread.currentThread().isInterrupted()) {
                        // Break loop if thread is interrupted
                        break;
                    }
                    if (isAbortPending()) {
                        // Break loop if aborting
                        break;
                    }
                    if (!isJobsRunning()) {
                        securePrintStatus();
                        throw new IllegalStateException(String.format("No runners but queue with %s items!!!", readBlockingQueue.size()));
                    }
                    // check if something failed
                    if ((++cntloop) > config.readQueuePutTimeoutSeconds / TIMEOUT_CHECK) {
                        securePrintStatus();
                        // something went wrong!!!
                        throw new TimeoutException(String.format("Queue got stuck with %s items!!!", readBlockingQueue.size()));
                    }

                }
                timeBlockedAtPutRead += System.nanoTime() - start;
                if (isAbortPending()) {
                    //Some error happen. Abort
                    logger.warn("Abort read thread on fail. Clear read queue and insert poison pill.");
                    readBlockingQueue.clear();
                    break;
                }
                //logger.trace("reader: preRead");
                batch = readBatch();
                //logger.trace("reader: batch.size = " + batch.size());
            }
            //logger.debug("reader: POISON_PILL");
            while (!readBlockingQueue.offer(POISON_PILL, TIMEOUT_CHECK, TimeUnit.SECONDS)) {
                if (isAbortPending()) {
                    logger.warn("Abort read thread on fail. Clear read queue and insert poison pill.");
                    readBlockingQueue.clear();
                }
            }
        } catch (InterruptedException e) {
            interruptions.add(e);
            e.printStackTrace();
            return true;
        }
        return false;
    }

    private boolean isJobsRunning() throws InterruptedException, ExecutionException {

        List<Future> fList = new ArrayList<Future>(this.futureTasks);
        for (int i = 0; i < fList.size(); i++) {
            Future f = fList.get(i);
            if (f.isCancelled()) {
                this.futureTasks.remove(f);
            } else if (f.isDone()) {
                this.futureTasks.remove(f);
                f.get(); // check for exceptions
            }
        }
        return !this.futureTasks.isEmpty();
    }

    /**
     * Check if all the worker threads have finished.
     * @return true if all tasks have finished
     */
    private boolean allTasksFinished() {
        return tasks.size() == finishedTasks;
    }

    private Batch<I> readBatch() {
        long start;
        Batch<I> batch;
        start = System.nanoTime();
        int position = numBatches++;
        try {
            batch = new Batch<>(reader.read(config.batchSize), position);
        } catch (Exception e) {
            logger.error("Error reading batch " + position, e);
            batch = POISON_PILL;
            exceptions.add(e);
        }
        timeReading += System.nanoTime() - start;
        return batch;
    }

    enum TaskRunnableStatus {
        UNSTARTED,
        READING_BATCH_FROM_QUEUE,
        PROCESSING_BATCH,
        DRAINING_TASK,
        WRITING_BATCH_TO_QUEUE,
        WRITING_POISON_PILL_TO_QUEUE,
        STOPPED
    }

    class TaskRunnable implements Callable<Void> {

        private final org.opencb.commons.run.Task<I, O> task;

        private long threadTimeBlockedAtTakeRead = 0;
        private long threadTimeBlockedAtSendWrite = 0;
        private long threadTimeTaskApply = 0;
        private Batch<I> batch;
        private String threadName;
        private TaskRunnableStatus status = TaskRunnableStatus.UNSTARTED;

        TaskRunnable(org.opencb.commons.run.Task<I, O> task) {
            this.task = task;
        }

        @Override
        public Void call() throws InterruptedException {
            try {
                threadName = Thread.currentThread().getName();
                batch = getBatch();

                List<O> batchResult = null;
                /**
                 *  Exit situations:
                 *      batch == POISON_PILL    -> The reader thread finish reading. Send poison pill.
                 *      batchResult.isEmpty()   -> If there is no reader thread, and the last batch was empty.
                 *      !exceptions.isEmpty()   -> If there is any exception, abort. Requires Config.abortOnFail == true
                 */
                while (batch != POISON_PILL) {
                    if (Thread.currentThread().isInterrupted()) {
                        // Break loop if thread is interrupted
                        break;
                    }
                    long start;
                    //logger.trace("task: apply");
                    start = System.nanoTime();
                    try {
                        status = TaskRunnableStatus.PROCESSING_BATCH;
                        batchResult = task.apply(batch.batch);
                    } catch (Exception e) {
                        logger.error("Error processing batch " + batch.position, e);
                        batchResult = null;
                        exceptions.add(e);
                    }
                    threadTimeTaskApply += System.nanoTime() - start;

                    if (readBlockingQueue == null && batchResult != null && batchResult.isEmpty()) {
                        //There is no readers and the last batch is empty
                        break;
                    }
                    if (isAbortPending()) {
                        //Some error happen. Abort
                        logger.warn("Abort task thread on fail");
                        break;
                    }

                    start = System.nanoTime();
                    if (writeBlockingQueue != null) {
                        status = TaskRunnableStatus.WRITING_BATCH_TO_QUEUE;
                        while (!writeBlockingQueue.offer(new Batch<O>(batchResult, batch.position), 1, TimeUnit.SECONDS)) {
                            if (isAbortPending()) {
                                //Some error happen. Abort
                                logger.warn("Abort task thread on fail");
                                break;
                            }
                        }
                    } else if (writeBlockingQueueFuture != null) {
                        status = TaskRunnableStatus.WRITING_BATCH_TO_QUEUE;
                        CompletableFuture<Batch<O>> future = writeBlockingQueueFutureMap.get(batch.position);
                        future.complete(new Batch<O>(batchResult, batch.position));
                    }
                    //logger.trace("task: apply done");
                    threadTimeBlockedAtSendWrite += System.nanoTime() - start;
                    batch = getBatch();
                }
                // Drain won't be called if the ParallelTaskRunner is interrupted.
                List<O> drain; // empty the system
                try {
                    status = TaskRunnableStatus.DRAINING_TASK;
                    drain = task.drain();
                } catch (Exception e) {
                    drain = null;
                    logger.error("Error draining task", e);
                    exceptions.add(e);
                }
                if (null != drain && !drain.isEmpty()) {
                    if (writeBlockingQueue != null) {
                        status = TaskRunnableStatus.WRITING_BATCH_TO_QUEUE;
                        // submit final batch received from draining
                        Batch<O> drainBatch = new Batch<>(drain, batch.position + 1);
                        while (!writeBlockingQueue.offer(drainBatch, TIMEOUT_CHECK, TimeUnit.SECONDS)) {
                            if (isAbortPending()) {
                                logger.warn("Abort task thread on fail");
                                break;
                            }
                        }
                    } else if (writeBlockingQueueFuture != null) {
                        status = TaskRunnableStatus.WRITING_BATCH_TO_QUEUE;
                        // Sorted PTR should not have to drain!
                        CompletableFuture<Batch<O>> future = new CompletableFuture<>();
                        future.complete(new Batch<O>(batchResult, batch.position + 1));
                        while (!writeBlockingQueueFuture.offer(future, TIMEOUT_CHECK, TimeUnit.SECONDS)) {
                            if (isAbortPending()) {
                                logger.warn("Abort task thread on fail");
                                break;
                            }
                        }
                    }
                }
            } catch (Error e) {
                exceptions.add(e);
                errors.add(e);
            } catch (RuntimeException e) {
                exceptions.add(e);
            } catch (InterruptedException e) {
                logger.warn("Catch InterruptedException " + e);
                throw e;
            } finally {
                status = TaskRunnableStatus.WRITING_POISON_PILL_TO_QUEUE;
                synchronized (tasks) {
                    timeBlockedAtPutWrite += threadTimeBlockedAtSendWrite;
                    timeTaskApply += threadTimeTaskApply;
                    timeBlockedAtTakeRead += threadTimeBlockedAtTakeRead;
                    finishedTasks++;
                    if (allTasksFinished()) {
                        if (writeBlockingQueue != null) {
                            // Offer, instead of put, to avoid blocking
                            if (!writeBlockingQueue.contains(POISON_PILL)) {
                                boolean offerPoisonPill = writeBlockingQueue.offer(POISON_PILL);
//                            if (!offerPoisonPill) {
//                                logger.trace("Offer POISON_PILL failed!");
//                            }
                            }
                        } else if (writeBlockingQueueFuture != null) {
                            CompletableFuture<Batch<O>> future = new CompletableFuture<>();
                            future.complete(POISON_PILL);
                            writeBlockingQueueFuture.offer(future);
                            for (Map.Entry<Integer, CompletableFuture<Batch<O>>> entry : writeBlockingQueueFutureMap.entrySet()) {
                                entry.getValue().complete(POISON_PILL);
                            }
                        }
                    }
                }
            }
            status = TaskRunnableStatus.STOPPED;
            return null;
        }

        public void printStatus(boolean printBatchElements) {
            // Copy to avoid concurrent changes on the batch
            Batch<I> batch = this.batch;
            TaskRunnableStatus status = this.status;

            if (batch == null) {
                logger.info("TaskRunner [{}] Status: '{}', empty batch (null)", threadName, status);
            } else if (batch == POISON_PILL) {
                logger.info("TaskRunner [{}] Status: '{}', empty batch (POISON_PILL)", threadName, status);
            } else {
                int size = batch.batch == null ? 0 : batch.batch.size();
                logger.info("TaskRunner [{}] Status: '{}', batch number {}/{} with {} elements:", threadName, status,
                        batch.position, numBatches, size);
                if (printBatchElements && (status == TaskRunnableStatus.PROCESSING_BATCH || status == TaskRunnableStatus.DRAINING_TASK)) {
                    if (batch.batch != null) {
                        int i = 0;
                        for (I element : batch.batch) {
                            logger.info("   [{}] : {}", i, element);
                            i++;
                        }
                    }
                }
            }
        }

        private Batch<I> getBatch() throws InterruptedException {
            status = TaskRunnableStatus.READING_BATCH_FROM_QUEUE;
            Batch<I> batch;
            if (readBlockingQueue == null) {
                return new Batch<>(Collections.<I>emptyList(), numBatches++);
            } else {
                long start = System.nanoTime();
                batch = readBlockingQueue.take();
                threadTimeBlockedAtTakeRead += (System.nanoTime() - start);
                //logger.trace("task: readBlockingQueue = " + readBlockingQueue.size() + " batch.size : "
                // + batch.size() + " : " + batchSize);
                if (batch == POISON_PILL) {
                    //logger.debug("task: POISON_PILL");
                    while (!readBlockingQueue.offer(POISON_PILL, TIMEOUT_CHECK, TimeUnit.SECONDS)) {
                        if (isAbortPending()) {
                            logger.warn("Abort task thread on fail");
                            break;
                        }
                    }
                }
                return batch;
            }
        }
    }

    class WriterRunnable implements Callable<Void> {

        private final DataWriter<O> dataWriter;

        WriterRunnable(DataWriter<O> dataWriter) {
            this.dataWriter = dataWriter;
        }

        @Override
        public Void call() throws InterruptedException {
            try {
                Batch<O> batch = getBatch();
                long start;
                while (batch != POISON_PILL) {
                    start = System.nanoTime();
//                    logger.trace("writer: write");
                    try {
                        dataWriter.write(batch.batch);
                    } catch (Exception e) {
                        logger.error("Error writing batch " + batch.position, e);
                        exceptions.add(e);
                    } catch (Error e) {
                        errors.add(e);
                        exceptions.add(e);
                        throw e;
                    }

                    if (isAbortPending()) {
                        //Some error happen. Abort
                        logger.warn("Abort writing thread on fail");
                        break;
                    }

//                    logger.trace("writer: wrote");
                    timeWriting += System.nanoTime() - start;
                    batch = getBatch();
                }
            } catch (InterruptedException e) {
                logger.warn("Catch InterruptedException ", e);
                throw e;
            }
            return null;
        }

        private Batch<O> getBatch() throws InterruptedException {
//                logger.trace("writer: writeBlockingQueue = " + writeBlockingQueue.size());
            long start = System.nanoTime();
            Batch<O> batch = null;
            if (config.sorted) {
                try {
                    while (batch == null) {
                        if (allTasksFinished() && writeBlockingQueueFuture.isEmpty()) {
                            batch = POISON_PILL;
                        } else {
                            Future<Batch<O>> future = writeBlockingQueueFuture.take();
                            batch = future.get();
                        }
                    }
                    writeBlockingQueueFutureMap.remove(batch.position);
                } catch (ExecutionException e) {
                    // Impossible!
                    throw new IllegalStateException(e);
                }
            } else {
                // WriteBlockingQueue may be empty if queue was full when offering the poison_pill
                if (allTasksFinished() && writeBlockingQueue.isEmpty()) {
                    batch = POISON_PILL;
                } else {
                    batch = writeBlockingQueue.take();
                }
            }
            timeBlockedAtTakeWrite += System.nanoTime() - start;
            if (batch == POISON_PILL) {
//                logger.debug("writer: POISON_PILL");
                if (writeBlockingQueue != null) {
                    if (!writeBlockingQueue.contains(POISON_PILL)) {
                        synchronized (tasks) {
                            if (!writeBlockingQueue.contains(POISON_PILL)) {
                                while (!writeBlockingQueue.offer(POISON_PILL, TIMEOUT_CHECK, TimeUnit.SECONDS)) {
                                    if (isAbortPending()) {
                                        logger.warn("Abort writing thread on fail");
                                        break;
                                    }
                                }
                            }
                        }
                    }
                } else if (writeBlockingQueueFuture != null) {
                    CompletableFuture<Batch<O>> future = new CompletableFuture<>();
                    future.complete(POISON_PILL);
                    writeBlockingQueueFuture.offer(future);
                    for (Map.Entry<Integer, CompletableFuture<Batch<O>>> entry : writeBlockingQueueFutureMap.entrySet()) {
                        entry.getValue().complete(POISON_PILL);
                    }
                }
            }
            return batch;
        }
    }

    private boolean isAbortPending() {
        return config.abortOnFail && !exceptions.isEmpty() || !interruptions.isEmpty();
    }

    private void securePrintStatus() {
        try {
            printStatus(true);
        } catch (Exception e) {
            logger.info("Error printing status", e);
        }
    }

    public void printStatus(boolean printBatchElements) {
        logger.info(toString());
        logger.info("Num processed batches: " + numBatches);
        for (TaskRunnable taskRunnable : taskRunnables) {
            taskRunnable.printStatus(printBatchElements);
        }
    }

    @Override
    public String toString() {
        return "Parallel Task Runner ["
                + (reader == null ? "" : "1 reader thread" + (writer == null ? " and " : ", "))
                + taskRunnables.size() + " task threads"
                + (writer == null ? "" : " and 1 writer thread]");
    }

}
