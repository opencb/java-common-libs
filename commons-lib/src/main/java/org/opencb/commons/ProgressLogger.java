/*
 * Copyright 2015-2016 OpenCB
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

package org.opencb.commons;

import org.opencb.commons.run.Task;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.DecimalFormat;
import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * Log the progress of a long operation.
 * <p>
 * Created on 13/04/16
 *
 * @author Jacobo Coll &lt;jacobo167@gmail.com&gt;
 */
public class ProgressLogger {

    private static final int DEFAULT_BATCH_SIZE = 5000;
    private static final int MIN_BATCH_SIZE = 200;
    private static final DecimalFormat DECIMAL_FORMAT = new DecimalFormat("0.0%");

    private final String message;
    private final int numLinesLog;
    private long totalCount;
    private boolean isApproximated; // Total count is an approximated value
    private final AtomicReference<Future<Long>> futureTotalCount = new AtomicReference<>();
    private final AtomicLong count;

    private double batchSize;

    private Logger logger = LoggerFactory.getLogger(ProgressLogger.class);

    public ProgressLogger(String message) {
        this(message, 0, null, 200);
    }

    public ProgressLogger(String message, long totalCount) {
        this(message, totalCount, null, 200);
    }

    public ProgressLogger(String message, long totalCount, int numLinesLog) {
        this(message, totalCount, null, numLinesLog);
    }

    public ProgressLogger(String message, Future<Long> futureTotalCount) {
        this(message, 0, futureTotalCount, 200);
    }

    public ProgressLogger(String message, Future<Long> futureTotalCount, int numLinesLog) {
        this(message, 0, futureTotalCount, numLinesLog);
    }

    /**
     * @param message            Starting message of the logger. Common for all the lines
     * @param totalCountCallable Callable function to get asynchronously the total count of elements
     * @param numLinesLog        Number of lines to print
     */
    public ProgressLogger(String message, Callable<Long> totalCountCallable, int numLinesLog) {
        this(message, 0, getFuture(totalCountCallable), numLinesLog);
    }

    private ProgressLogger(String message, long totalCount, Future<Long> futureTotalCount, int numLinesLog) {
        if (message.endsWith(" ")) {
            this.message = message;
        } else {
            this.message = message + " ";
        }
        this.numLinesLog = numLinesLog;
        this.totalCount = totalCount;
        this.futureTotalCount.set(futureTotalCount);
        this.count = new AtomicLong();
        if (totalCount == 0) {
            batchSize = DEFAULT_BATCH_SIZE;
        } else {
            updateBatchSize();
        }
        isApproximated = false;
    }


    public ProgressLogger setLogger(Logger logger) {
        this.logger = logger;
        return this;
    }

    public ProgressLogger setBatchSize(int batchSize) {
        this.batchSize = batchSize;
        return this;
    }

    public ProgressLogger setApproximateTotalCount(long aproximateTotalCount) {
        isApproximated = true;
        this.totalCount = aproximateTotalCount;
        updateBatchSize();
        return this;
    }

    public void increment(long delta) {
        increment(delta, "", null);
    }

    public void increment(long delta, String message) {
        increment(delta, message, null);
    }

    public void increment(long delta, Supplier<String> supplier) {
        increment(delta, null, supplier);
    }

    private void increment(long delta, String message, Supplier<String> supplier) {
        long previousCount = count.getAndAdd(delta);
        long count = previousCount + delta;

        updateFutureTotalCount();
        if ((int) (previousCount / batchSize) != (int) (count / batchSize) || count == totalCount && delta > 0) {
            log(count, supplier == null ? message : supplier.get());
        }
    }

    protected synchronized void log(long count, String extraMessage) {
        long totalCount = getTotalCount();

        StringBuilder sb = new StringBuilder(message).append(count);
        if (totalCount > 0) {
            if (isApproximated) {
                sb.append("/~");
            } else {
                sb.append('/');
            }
            sb.append(totalCount).append(' ').append(DECIMAL_FORMAT.format(((float) (count)) / totalCount));
        }
        if (!extraMessage.isEmpty() && (!extraMessage.startsWith(" ") && !extraMessage.startsWith(",") && !extraMessage.startsWith("."))) {
            sb.append(' ');
        }
        sb.append(extraMessage);

        print(sb.toString());
    }

    protected void print(String m) {
        logger.info(m);
    }

    private void updateFutureTotalCount() {
        Future<Long> future = futureTotalCount.get();
        if (future != null) {
            if (future.isDone()) {
                try {
                    totalCount = future.get();
                    updateBatchSize();
                    isApproximated = false;
                } catch (InterruptedException | ExecutionException ignore) {
                    logger.warn("There was a problem calculating the total number of elements");
                } finally {
                    futureTotalCount.set(null);
                }
            }
        }
    }

    private long getTotalCount() {
        return this.totalCount;
    }

    private void updateBatchSize() {
        batchSize = Math.max((double) totalCount / numLinesLog, MIN_BATCH_SIZE);
    }

    private static Future<Long> getFuture(Callable<Long> totalCountCallable) {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        Future<Long> future = executor.submit(totalCountCallable);
        executor.shutdown();
        return future;
    }

    public <T> Task<T, T> asTask() {
        return asTask(null);
    }

    public <T> Task<T, T> asTask(Function<T, String> messageBuilder) {
        return new Task<T, T>() {
            @Override
            public List<T> apply(List<T> batch) throws Exception {
                if (batch == null || batch.isEmpty()) {
                    return batch;
                }
                increment(batch.size(), () -> {
                    T lastElement = batch.get(batch.size() - 1);
                    return messageBuilder.apply(lastElement);
                });
                return batch;
            }
        };
    }
}
