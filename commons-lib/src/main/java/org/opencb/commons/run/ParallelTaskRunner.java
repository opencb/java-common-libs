package org.opencb.commons.run;

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
 * {@link DataReader} Producer , {@link Task} Worker, {@link DataWriter}Consumer
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
    private static final DecimalFormat DECIMAL_FORMAT = new DecimalFormat("#.###");

    @FunctionalInterface
    public interface Task<T, R> extends TaskWithException<T, R, RuntimeException> {
    }

    @FunctionalInterface
    public interface TaskWithException<T, R, E extends Exception> {
        default void pre() {
        }

        List<R> apply(List<T> batch) throws E;

        default List<R> drain() {
            return Collections.emptyList();
        }

        default void post() {
        }
    }

    @SuppressWarnings("unchecked")
    private static final Batch POISON_PILL = new Batch(Collections.emptyList(), -1);

    private final DataReader<I> reader;
    private final DataWriter<O> writer;
    private final List<TaskWithException<I, O, ?>> tasks;
    private final Config config;

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
    private List<Exception> exceptions;
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
        public Config(int numTasks, int batchSize, int capacity, boolean abortOnFail, boolean sorted, int readQueuePutTimeout) {
            this.numTasks = numTasks;
            this.batchSize = batchSize;
            this.capacity = capacity;
            this.abortOnFail = abortOnFail;
            this.sorted = sorted;
            this.readQueuePutTimeout = readQueuePutTimeout;
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
            private int readQueuePutTimeout = 500;

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

            public Builder setReadQueuePutTimeout(int readQueuePutTimeout) {
                this.readQueuePutTimeout = readQueuePutTimeout;
                return this;
            }

            public ParallelTaskRunner.Config build() {
                if (capacity < 0) {
                    capacity = numTasks * 2;
                }
                return new ParallelTaskRunner.Config(numTasks, batchSize, capacity, abortOnFail, sorted, readQueuePutTimeout);
            }
        }

        private final int numTasks;
        private final int batchSize;
        private final int capacity;
        private final boolean abortOnFail;
        private final boolean sorted;
        private final int readQueuePutTimeout;
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
    public ParallelTaskRunner(DataReader<I> reader, TaskWithException<I, O, ?> task, DataWriter<O> writer, Config config) {
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
    public ParallelTaskRunner(DataReader<I> reader, Supplier<? extends TaskWithException<I, O, ?>> taskSupplier,
                              DataWriter<O> writer, Config config) {
        this.config = config;
        this.reader = reader;
        this.writer = writer;
        this.tasks = new ArrayList<>(config.numTasks);
        for (int i = 0; i < config.numTasks; i++) {
            tasks.add(taskSupplier.get());
        }

        check();
    }

    /**
     * @param reader Unique DataReader. If null, empty batches will be generated
     * @param tasks  Generated Tasks. Each task will be used in one thread. Will use tasks.size() as "numTasks".
     * @param writer Unique DataWriter. If null, data generated by the task will be lost.
     * @param config configuration.
     * @throws IllegalArgumentException Exception.
     */
    public ParallelTaskRunner(DataReader<I> reader, List<? extends TaskWithException<I, O, ?>> tasks, DataWriter<O> writer, Config config) {
        this.config = config;
        this.reader = reader;
        this.writer = writer;
        this.tasks = new ArrayList<>(tasks);

        check();
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
            throw new ExecutionException("Error while running ParallelTaskRunner. Found " + interruptions.size()
                    + " interruptions.", interruptions.get(0));
        }
    }

    public void run(long timeout, TimeUnit unit) throws ExecutionException, InterruptedException {
        long start = System.nanoTime();
        //If there is any InterruptionException, finish as quick as possible.
        boolean interrupted = false;
        init();

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

        for (TaskWithException<I, O, ?> task : tasks) {
            task.pre();
        }

        for (TaskWithException<I, O, ?> task : tasks) {
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
            for (TaskWithException<I, O, ?> task : tasks) {
                task.post();
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

        if (reader != null) {
            logger.info("read:  timeReading                  = " + prettyTime(timeReading) + "s");
            logger.info("read:  timeBlockedAtPutRead         = " + prettyTime(timeBlockedAtPutRead) + "s");
            logger.info("task;  timeBlockedAtTakeRead        = " + prettyTime(timeBlockedAtTakeRead) + "s");
        }

        logger.info("task;  timeTaskApply                = " + prettyTime(timeTaskApply) + "s");

        if (writer != null) {
            logger.info("task;  timeBlockedAtPutWrite        = " + prettyTime(timeBlockedAtPutWrite) + "s");
            logger.info("write: timeBlockedWatingDataToWrite = " + prettyTime(timeBlockedAtTakeWrite) + "s");
            logger.info("write: timeWriting                  = " + prettyTime(timeWriting) + "s");
        }

        logger.info("total:                              = " + prettyTime(System.nanoTime() - start) + "s");

        if (config.abortOnFail && !exceptions.isEmpty()) {
            throw new ExecutionException("Error while running ParallelTaskRunner. Found " + exceptions.size()
                    + " exceptions.", exceptions.get(0));
        }
        if (interrupted) {
            throw interruptions.get(0);
        }
    }

    private String prettyTime(long time) {
        return DECIMAL_FORMAT.format(TimeUnit.NANOSECONDS.toMillis(time) / 1000.0);
    }

    public List<Exception> getExceptions() {
        return exceptions;
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

    private void doSubmit(Callable taskRunnable) {
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
                    writeBlockingQueueFuture.put(completableFuture);
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
                    if (!isJobsRunning()) {
                        throw new IllegalStateException(String.format("No runners but queue with %s items!!!", readBlockingQueue.size()));
                    }
                    // check if something failed
                    if ((++cntloop) > config.readQueuePutTimeout / TIMEOUT_CHECK) {
                        // something went wrong!!!
                        throw new TimeoutException(String.format("Queue got stuck with %s items!!!", readBlockingQueue.size()));
                    }

                }
                timeBlockedAtPutRead += System.nanoTime() - start;
                if (isAbortPending()) {
                    //Some error happen. Abort
                    logger.warn("Abort read thread on fail");
                    break;
                }
                //logger.trace("reader: preRead");
                batch = readBatch();
                //logger.trace("reader: batch.size = " + batch.size());
            }
            //logger.debug("reader: POISON_PILL");
            readBlockingQueue.put(POISON_PILL);
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

    class TaskRunnable implements Callable<Void> {

        private final TaskWithException<I, O, ?> task;

        private long threadTimeBlockedAtTakeRead = 0;
        private long threadTimeBlockedAtSendWrite = 0;
        private long threadTimeTaskApply = 0;

        TaskRunnable(TaskWithException<I, O, ?> task) {
            this.task = task;
        }

        @Override
        public Void call() throws InterruptedException {
            try {
                Batch<I> batch = getBatch();

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
                        writeBlockingQueue.put(new Batch<O>(batchResult, batch.position));
                    } else if (writeBlockingQueueFuture != null) {
                        CompletableFuture<Batch<O>> future = writeBlockingQueueFutureMap.get(batch.position);
                        future.complete(new Batch<O>(batchResult, batch.position));
                    }
                    //logger.trace("task: apply done");
                    threadTimeBlockedAtSendWrite += System.nanoTime() - start;
                    batch = getBatch();
                }
                // Drain won't be called if the ParallelTaskRunner is interrupted.
                List<O> drain = task.drain(); // empty the system
                if (null != drain && !drain.isEmpty()) {
                    if (writeBlockingQueue != null) {
                        // submit final batch received from draining
                        writeBlockingQueue.put(new Batch<>(drain, batch.position + 1));
                    } else if (writeBlockingQueueFuture != null) {
                        // Sorted PTR should not have to drain!
                        CompletableFuture<Batch<O>> future = new CompletableFuture<>();
                        future.complete(new Batch<O>(batchResult, batch.position + 1));
                        writeBlockingQueueFuture.put(future);
                    }
                }
            } catch (RuntimeException e) {
                exceptions.add(e);
            } catch (InterruptedException e) {
                logger.warn("Catch InterruptedException " + e);
                throw e;
            } finally {
                synchronized (tasks) {
                    timeBlockedAtPutWrite += threadTimeBlockedAtSendWrite;
                    timeTaskApply += threadTimeTaskApply;
                    timeBlockedAtTakeRead += threadTimeBlockedAtTakeRead;
                    finishedTasks++;
                    if (allTasksFinished()) {
                        if (writeBlockingQueue != null) {
                            // Offer, instead of put, to avoid blocking
                            boolean offerPoisonPill = writeBlockingQueue.offer(POISON_PILL);
//                            if (!offerPoisonPill) {
//                                logger.trace("Offer POISON_PILL failed!");
//                            }
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
            return null;
        }

        private Batch<I> getBatch() throws InterruptedException {
            Batch<I> batch;
            if (readBlockingQueue == null) {
                return new Batch<>(Collections.<I>emptyList(), numBatches++);
            } else {
                long start = System.nanoTime();
                batch = readBlockingQueue.take();
                threadTimeBlockedAtTakeRead += start - System.currentTimeMillis();
                //logger.trace("task: readBlockingQueue = " + readBlockingQueue.size() + " batch.size : "
                // + batch.size() + " : " + batchSize);
                if (batch == POISON_PILL) {
                    //logger.debug("task: POISON_PILL");
                    readBlockingQueue.put(POISON_PILL);
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
                        Future<Batch<O>> future = writeBlockingQueueFuture.take();
                        batch = future.get();
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
                    writeBlockingQueue.put(POISON_PILL);
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

}
