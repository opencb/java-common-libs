package org.opencb.commons.run;

import org.opencb.commons.io.DataReader;
import org.opencb.commons.io.DataWriter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.*;
import java.util.function.Supplier;

/**
 * Created by hpccoll1 on 26/02/15.
 */
public class ParallelTaskRunner<I, O> {


    public static final int TIMEOUT_CHECK = 1;
    private static final int MAX_SHUTDOWN_RETRIES = 30;

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
    private BlockingQueue<Batch<O>> writeBlockingQueue;

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

//    protected static Logger logger = LoggerFactory.getLogger(SimpleThreadRunner.class);

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
            return 0;
        }
    }

    /**
     * @param reader Unique DataReader. If null, empty batches will be generated
     * @param task   Task to be used. Will be used the same instance in all threads
     * @param writer Unique DataWriter. If null, data generated by the task will be lost.
     * @param config configuration.
     * @throws IllegalArgumentException Exception.
     */
    public ParallelTaskRunner(DataReader<I> reader, TaskWithException<I, O, ?> task, DataWriter<O> writer, Config config)
            throws IllegalArgumentException {
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
                              DataWriter<O> writer, Config config)
        throws IllegalArgumentException {
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
    public ParallelTaskRunner(DataReader<I> reader, List<? extends TaskWithException<I, O, ?>> tasks, DataWriter<O> writer, Config config)
            throws IllegalArgumentException {
        this.config = config;
        this.reader = reader;
        this.writer = writer;
        this.tasks = new ArrayList<>(tasks);

        check();
    }

    private void check() throws IllegalArgumentException {
        if (tasks == null || tasks.isEmpty()) {
            throw new IllegalArgumentException("Must provide at least one task");
        }
        if (tasks.size() != config.numTasks) {
            //WARN!!
            return;
        }
    }

    private void init() {
        finishedTasks = 0;
        if (reader != null) {
            readBlockingQueue = new ArrayBlockingQueue<>(config.capacity);
        }

        if (writer != null) {
            writeBlockingQueue = new ArrayBlockingQueue<>(config.capacity);
        }

        executorService = Executors.newFixedThreadPool(tasks.size() + (writer == null ? 0 : 1));
        futureTasks = new ArrayList<Future>(); // assume no parallel access to this list
        exceptions = Collections.synchronizedList(new LinkedList<>());
        interruptions = Collections.synchronizedList(new LinkedList<>());
    }

    public void run() throws ExecutionException {
        long start = System.nanoTime();
        //If there is any InterruptionException, finish as quick as possible.
        boolean interrupted = false;
        init();

        if (reader != null) {
            reader.open();
            reader.pre();
        }

        if (writer != null) {
            writer.open();
            writer.pre();
        }

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
                    executorService.awaitTermination(Long.MAX_VALUE, TimeUnit.DAYS); // TODO further action - this is not good!!!
                } catch (InterruptedException e) {
                    interruptions.add(e);
                    interrupted = true;
                    e.printStackTrace();
                }
            }
        } catch (TimeoutException e) {
            exceptions.add(e);
            e.printStackTrace();
        } finally {
            if (!executorService.isShutdown()) {
                executorService.shutdownNow(); // shut down now if not done so (e.g. execption)
            }
        }

        if (interrupted) {
            // If interrupted, quick close
            if (!executorService.isTerminated()) {
                executorService.shutdownNow();
                for (Future future : futureTasks) {
                    future.cancel(true);
                }
            }
        } else {
            //Avoid execute POST and CLOSE if the threads are still alive.
            int shutdownRetries = 0;
            try {
                while (!executorService.isTerminated() && shutdownRetries < MAX_SHUTDOWN_RETRIES) {
                    shutdownRetries++;
                    Thread.sleep(1000);
                    System.err.println("Executor is not terminated!! Shutdown now! - " + shutdownRetries);
                    executorService.shutdownNow();
                    for (Future future : futureTasks) {
                        future.cancel(true);
                    }
                }
            } catch (InterruptedException e) {
                // Stop trying to stop the ExecutorService
                interruptions.add(e);
                e.printStackTrace();
                interrupted = true;
            }
        }

        for (TaskWithException<I, O, ?> task : tasks) {
            task.post();
        }

        if (reader != null) {
            reader.post();
            reader.close();
        }

        if (writer != null) {
            writer.post();
            writer.close();
        }


        if (reader != null) {
            System.err.println("read:  timeReading                  = " + timeReading / 1000000000.0 + "s");
            System.err.println("read:  timeBlockedAtPutRead         = " + timeBlockedAtPutRead / 1000000000.0 + "s");
            System.err.println("task;  timeBlockedAtTakeRead        = " + timeBlockedAtTakeRead / 1000000000.0 + "s");
        }

        System.err.println("task;  timeTaskApply                = " + timeTaskApply / 1000000000.0 + "s");

        if (writer != null) {
            System.err.println("task;  timeBlockedAtPutWrite        = " + timeBlockedAtPutWrite / 1000000000.0 + "s");
            System.err.println("write: timeBlockedWatingDataToWrite = " + timeBlockedAtTakeWrite / 1000000000.0 + "s");
            System.err.println("write: timeWriting                  = " + timeWriting / 1000000000.0 + "s");
        }

        System.err.println("total:                              = " + (System.nanoTime() - start) / 1000000000.0 + "s");

        if (config.abortOnFail && !exceptions.isEmpty()) {
            throw new ExecutionException("Error while running ParallelTaskRunner. Found " + exceptions.size()
                    + " exceptions.", exceptions.get(0));
        }
        if (interrupted) {
            // FIXME: Throw InterruptionException
            Thread.currentThread().interrupt(); // Restore interruption flag
            throw new ExecutionException("Error while running ParallelTaskRunner. Found " + interruptions.size()
                    + " interruptions.", interruptions.get(0));
        }
    }

    public List<Exception> getExceptions() {
        return exceptions;
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
                //System.out.println("reader: prePut readBlockingQueue " + readBlockingQueue.size());
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
                    System.err.println("Abort read thread on fail");
                    break;
                }
                //System.out.println("reader: preRead");
                batch = readBatch();
                //System.out.println("reader: batch.size = " + batch.size());
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

    private Batch<I> readBatch() {
        long start;
        Batch<I> batch;
        start = System.nanoTime();
        int position = numBatches++;
        try {
            batch = new Batch<I>(reader.read(config.batchSize), position);
        } catch (Exception e) {
            System.err.println("Error reading batch " + position + "" + e.toString());
            e.printStackTrace();
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
                    //System.out.println("task: apply");
                    start = System.nanoTime();
                    try {
                        batchResult = task.apply(batch.batch);
                    } catch (Exception e) {
                        System.err.println("Error processing batch " + batch.position + "");
                        e.printStackTrace();
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
                        System.err.println("Abort task thread on fail");
                        break;
                    }

                    start = System.nanoTime();
                    if (writeBlockingQueue != null) {
                        writeBlockingQueue.put(new Batch<O>(batchResult, batch.position));
                    }
                    //System.out.println("task: apply done");
                    threadTimeBlockedAtSendWrite += System.nanoTime() - start;
                    batch = getBatch();
                }
                // Drain won't be called if the ParallelTaskRunner is interrupted.
                List<O> drain = task.drain(); // empty the system
                if (null != drain && !drain.isEmpty() && writeBlockingQueue != null) {
                    // submit final batch received from draining
                    writeBlockingQueue.put(new Batch<O>(drain, batch.position + 1));
                }
            } catch (InterruptedException e) {
                System.err.println("Catch InterruptedException " + e);
                throw e;
            } finally {
                synchronized (tasks) {
                    timeBlockedAtPutWrite += threadTimeBlockedAtSendWrite;
                    timeTaskApply += threadTimeTaskApply;
                    timeBlockedAtTakeRead += threadTimeBlockedAtTakeRead;
                    finishedTasks++;
                    if (tasks.size() == finishedTasks) {
                        if (writeBlockingQueue != null) {
                            // Offer, instead of put, to avoid blocking
                            writeBlockingQueue.offer(POISON_PILL);
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
                //System.out.println("task: readBlockingQueue = " + readBlockingQueue.size() + " batch.size : "
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
//                    System.out.println("writer: write");
                    try {
                        dataWriter.write(batch.batch);
                    } catch (Exception e) {
                        System.err.println("Error writing batch " + batch.position + "");
                        e.printStackTrace();
                        exceptions.add(e);
                    }

                    if (isAbortPending()) {
                        //Some error happen. Abort
                        System.err.println("Abort writing thread on fail");
                        break;
                    }

//                    System.out.println("writer: wrote");
                    timeWriting += System.nanoTime() - start;
                    batch = getBatch();
                }
            } catch (InterruptedException e) {
                System.err.println("Catch InterruptedException " + e);
                throw e;
            }
            return null;
        }

        private Batch<O> getBatch() throws InterruptedException {
//                System.out.println("writer: writeBlockingQueue = " + writeBlockingQueue.size());
            long start = System.nanoTime();
            Batch<O> batch = writeBlockingQueue.take();
            timeBlockedAtTakeWrite += System.nanoTime() - start;
            if (batch == POISON_PILL) {
//                logger.debug("writer: POISON_PILL");
                writeBlockingQueue.put(POISON_PILL);
            }
            return batch;
        }
    }

    private boolean isAbortPending() {
        return config.abortOnFail && !exceptions.isEmpty() || !interruptions.isEmpty();
    }

}
