package org.opencb.commons.run;

import org.opencb.commons.io.DataReader;
import org.opencb.commons.io.DataWriter;

import java.util.*;
import java.util.concurrent.*;
import java.util.function.Function;

/**
 * Created by hpccoll1 on 26/02/15.
 */
public class ParallelTaskRunner<I,O> {

    @FunctionalInterface
    static public interface BatchFunction<T, R> {
        List<R> apply(List<T> t);
    }

    final Batch POISON_PILL = new Batch(Collections.emptyList(), -1);

    private final DataReader<I> reader;
    private final DataWriter<O> writer;
    private final List<Function<List<I>, List<O>>> tasks;
    private final Config config;
    private ExecutorService executorService;
    BlockingQueue<Batch<I>> readBlockingQueue;
    BlockingQueue<Batch<O>> writeBlockingQueue;

//    protected static Logger logger = LoggerFactory.getLogger(SimpleThreadRunner.class);

    public static class Config {
        public Config(int numTasks, int batchSize, int capacity, boolean sorted) {
            this.numTasks = numTasks;
            this.batchSize = batchSize;
            this.capacity = capacity;
            this.sorted = sorted;
        }

        int numTasks;
        int batchSize;
        int capacity;
        boolean sorted;
//        int timeout;
    }

    private static class Batch<T> implements Comparable<Batch<T>> {
        final List<T> batch;
        final int position;

        private Batch(List<T> batch, int position) {
            this.batch = batch;
            this.position = position;
        }

        @Override
        public int compareTo(Batch<T> o) {
            return 0;
        }
    }

    public ParallelTaskRunner(DataReader<I> reader, Function<List<I>, List<O>> task, DataWriter<O> writer, Config config) throws Exception {
        this.config = config;
        this.reader = reader;
        this.writer = writer;
        this.tasks = new ArrayList<>(config.capacity);
        for (int i = 0; i < config.numTasks; i++) {
            tasks.add(task);
        }

        check();
    }

    public ParallelTaskRunner(DataReader<I> reader, List<Function<List<I>, List<O>>> tasks, DataWriter<O> writer, Config config) throws Exception {
        this.config = config;
        this.reader = reader;
        this.writer = writer;
        this.tasks = tasks;

        check();
    }

    public void check() throws Exception {
        if (tasks.isEmpty()) {
            //ERROR!!
        }
        if (tasks.size() != config.numTasks) {
            //WARN!!
        }
    }

    public void init(){

        readBlockingQueue = new ArrayBlockingQueue<>(config.capacity);

        if (writer != null) {
            writeBlockingQueue = new ArrayBlockingQueue<>(config.capacity);
        }

        executorService = Executors.newFixedThreadPool(1 + tasks.size() + (writer == null? 0 : 1));
    }

    public void run() {
        init();

        reader.open();
        reader.pre();

        if (writer != null) {
            writer.open();
            writer.pre();
        }

//        for (Function task : tasks) {
//            task.pre();
//        }

        for (Function<List<I>, List<O>> task : tasks) {
            TaskRunnable taskRunnable = new TaskRunnable(task);
            executorService.submit(taskRunnable);
        }
        if (writer != null) {
            executorService.submit(new WriterRunnable(writer));
        }

//        executorService.submit(new ReaderRunnable(reader)); //Run reader in a separated thread
        new ReaderRunnable(reader).run();                     //Use the main thread for reading

        executorService.shutdown();
        try {
            executorService.awaitTermination(Long.MAX_VALUE, TimeUnit.DAYS);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

//        for (Function task : tasks) {
//            task.post();
//        }

        reader.post();
        reader.close();

        if (writer != null) {
            writer.post();
            writer.close();
        }

    }
    class ReaderRunnable implements Runnable {

        final DataReader<I> dataReader;
        int numBatches = 0;
        ReaderRunnable(DataReader<I> dataReader) {
            this.dataReader = dataReader;
        }

        @Override
        public void run() {
            Batch<I> batch = new Batch<>(dataReader.read(config.batchSize), numBatches++);
//            System.out.println("reader: batch.size = " + batch.size());

            while (batch.batch != null && !batch.batch.isEmpty()) {
                try {
//                    System.out.println("reader: prePut readBlockingQueue " + readBlockingQueue.size());
                    readBlockingQueue.put(batch);
//                    System.out.println("reader: postPut");
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
//                System.out.println("reader: preRead");
                batch = new Batch<>(dataReader.read(config.batchSize), numBatches++);
//                System.out.println("reader: batch.size = " + batch.size());
            }
            try {
//                logger.debug("reader: POISON_PILL");
                readBlockingQueue.put(POISON_PILL);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    class TaskRunnable implements Runnable {

        private long timeBlockedAtSendWrite;
        private long timeTaskApply;

        final Function<List<I>, List<O>> task;

        TaskRunnable(Function<List<I>, List<O>> task) {
            this.task = task;
        }

        @Override
        public void run() {
            Batch<I> batch = POISON_PILL;
            long timeBlockedAtSendWrite = 0;
            long timeTaskApply = 0;
            try {
                batch = getBatch();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            while (!batch.batch.isEmpty()) {
                try {
                    long s;
//                        System.out.println("task: apply");
                    s = System.nanoTime();
                    List<O> batchResult = task.apply(batch.batch);
                    timeTaskApply += s - System.nanoTime();
//                    System.out.println("task: apply done " + writeBlockingQueue.size());

                    s = System.nanoTime();
                    if (writeBlockingQueue != null) {
                        writeBlockingQueue.put(new Batch<O>(batchResult, batch.position));
                    }
//                    System.out.println("task: apply done");
                    timeBlockedAtSendWrite += s - System.nanoTime();
                    batch = getBatch();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            synchronized (config) {
                this.timeBlockedAtSendWrite += timeBlockedAtSendWrite;
                this.timeTaskApply += timeTaskApply;
                finishedTasks++;
                if (tasks.size() == finishedTasks) {
//                    logger.debug("task; timeBlockedAtSendWrite = " + timeBlockedAtSendWrite / -1000000000.0 + "s");
//                    logger.debug("task; timeTaskApply = " + timeTaskApply / -1000000000.0 + "s");
                    if (writeBlockingQueue != null) {
                        try {
                            writeBlockingQueue.put(POISON_PILL);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }


        }
        private int finishedTasks = 0;
        private Batch<I> getBatch() throws InterruptedException {
            Batch<I> batch;
            batch = readBlockingQueue.take();
//                System.out.println("task: readBlockingQueue = " + readBlockingQueue.size() + " batch.size : " + batch.size() + " : " + batchSize);
            if (batch == POISON_PILL) {
//                logger.debug("task: POISON_PILL");
                readBlockingQueue.put(POISON_PILL);
            }
            return batch;
        }
    }

    class WriterRunnable implements Runnable {

        long timeBlockedWaitingDataToWrite = 0;
        final DataWriter<O> dataWriter;

        WriterRunnable(DataWriter<O> dataWriter) {
            this.dataWriter = dataWriter;
        }

        @Override
        public void run() {
            Batch<O> batch = POISON_PILL;
            try {
                batch = getBatch();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            long start, timeWriting = 0;
            while (batch != POISON_PILL) {
                try {
                    start = System.nanoTime();
//                    System.out.println("writer: write");
                    dataWriter.write(batch.batch);
//                    System.out.println("writer: wrote");
                    timeWriting += start - System.nanoTime();
                    batch = getBatch();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
//            logger.debug("write: timeWriting = " + timeWriting / -1000000000.0 + "start");
//            logger.debug("write: timeBlockedWatingDataToWrite = " + timeBlockedWatingDataToWrite / -1000000000.0 + "start");
        }

        private Batch<O> getBatch() throws InterruptedException {
//                System.out.println("writer: writeBlockingQueue = " + writeBlockingQueue.size());
            long s = System.nanoTime();
            Batch<O> batch = writeBlockingQueue.take();
            timeBlockedWaitingDataToWrite += s - System.nanoTime();
            if (batch == POISON_PILL) {
//                logger.debug("writer: POISON_PILL");
                writeBlockingQueue.put(POISON_PILL);
            }
            return batch;
        }
    }

}
