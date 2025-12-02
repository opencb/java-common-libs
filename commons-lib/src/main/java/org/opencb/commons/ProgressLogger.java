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

import org.apache.commons.lang3.tuple.Pair;
import org.opencb.commons.run.Task;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.DecimalFormat;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * Log the progress of a long operation.
 *
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
    private final long logFrequencyMillis;
    private boolean progressRateEnabled = true;
    private long progressRateWindowSizeSeconds;
    private boolean progressRateMillionHours = false; // If true, progress rate is in millions of elements per hour
    private long totalCount;
    private boolean isApproximated; // Total count is an approximated value
    private final AtomicReference<Future<Long>> futureTotalCount = new AtomicReference<>();
    private final AtomicLong count;
    private final long startTime;
    private final LinkedList<Pair<Long, Long>> times = new LinkedList<>();

    private double batchSize;

    private Logger logger = LoggerFactory.getLogger(ProgressLogger.class);

    public ProgressLogger(String message) {
        this(message, 0, null, 200, 0);
    }

    public ProgressLogger(String message, long logFrequency, TimeUnit timeUnit) {
        this(message, 0, null, 0, timeUnit.toMillis(logFrequency));
    }

    public ProgressLogger(String message, long totalCount) {
        this(message, totalCount, null, 200, 0);
    }

    public ProgressLogger(String message, long totalCount, int numLinesLog) {
        this(message, totalCount, null, numLinesLog, 0);
    }

    public ProgressLogger(String message, Future<Long> futureTotalCount) {
        this(message, 0, futureTotalCount, 200, 0);
    }

    public ProgressLogger(String message, Future<Long> futureTotalCount, int numLinesLog) {
        this(message, 0, futureTotalCount, numLinesLog, 0);
    }

    /**
     *
     * @param message               Starting message of the logger. Common for all the lines
     * @param totalCountCallable    Callable function to get asynchronously the total count of elements
     * @param numLinesLog           Number of lines to print
     */
    public ProgressLogger(String message, Callable<Long> totalCountCallable, int numLinesLog) {
        this(message, 0, getFuture(totalCountCallable), numLinesLog, 0);
    }

    private ProgressLogger(String message, long totalCount, Future<Long> futureTotalCount, int numLinesLog, long logFrequencyMillis) {
        if (message.endsWith(" ")) {
            this.message = message;
        } else {
            this.message = message + " ";
        }
        this.numLinesLog = numLinesLog;
        this.totalCount = totalCount;
        this.futureTotalCount.set(futureTotalCount);
        this.count = new AtomicLong();
        if (logFrequencyMillis > 0) {
            this.logFrequencyMillis = logFrequencyMillis;
            batchSize = 0;
        } else {
            // Avoid not logging for too long. Log at least once a minute by default
            this.logFrequencyMillis = TimeUnit.MINUTES.toMillis(1);
            if (totalCount == 0) {
                batchSize = DEFAULT_BATCH_SIZE;
            } else {
                updateBatchSize();
            }
        }
        isApproximated = false;
        startTime = System.currentTimeMillis();
        progressRateWindowSizeSeconds = 60;
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

    public ProgressLogger setProgressRateWindowSize(int progressRateWindowSize, TimeUnit timeUnit) {
        this.progressRateWindowSizeSeconds = timeUnit.toSeconds(progressRateWindowSize);
        return this;
    }

    public ProgressLogger setProgressRateAtMillionsPerHours() {
        return setProgressRateAtMillionsPerHours(true);
    }

    public ProgressLogger setProgressRateAtMillionsPerHours(boolean progressRateMillionHours) {
        this.progressRateMillionHours = progressRateMillionHours;
        return this;
    }

    public ProgressLogger disableProgressRate() {
        this.progressRateEnabled = false;
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
        long currentTimeMillis = System.currentTimeMillis();
        if (shouldLog(delta, previousCount, count, currentTimeMillis)) {
            log(count, supplier == null ? message : supplier.get(), currentTimeMillis);
        }
    }

    private boolean shouldLog(long delta, long previousCount, long count, long currentTimeMillis) {
        if (batchSize > 0) {
            if ((int) (previousCount / batchSize) != (int) (count / batchSize)) {
                return true;
            }
        }
        if (logFrequencyMillis > 0) {
            long lastLogTime = times.isEmpty() ? startTime : times.getLast().getRight();
            if (currentTimeMillis - lastLogTime > logFrequencyMillis) {
                return true;
            }
        }
        if (count == totalCount && delta > 0) {
            return true;
        }
        return false;
    }

    protected synchronized void log(long count, String extraMessage, long currentTimeMillis) {
        times.add(Pair.of(count, currentTimeMillis));
        if (times.size() > 5 && times.get(0).getRight() < currentTimeMillis - progressRateWindowSizeSeconds * 1000) {
            // Remove old points that are outside the progress rate window
            times.removeFirst();
        }
        long totalCount = this.totalCount;

        StringBuilder sb = new StringBuilder(message).append(count);
        if (totalCount > 0) {
            if (isApproximated) {
                sb.append("/~");
            } else {
                sb.append('/');
            }
            sb.append(totalCount).append(' ').append(DECIMAL_FORMAT.format(((float) (count)) / totalCount));
        }
        if (progressRateEnabled) {
            float elapsedTime = (float) (currentTimeMillis - startTime) / 1000;
            float progressRate = count / elapsedTime; // elements per second
            boolean addRelativeTime = times.size() > 5 && elapsedTime > progressRateWindowSizeSeconds;
            float relativeTime;
            float relativeProgressRate; // elements per second
            if (addRelativeTime) {
                int idx = 5;
                do {
                    Pair<Long, Long> relativePoint = times.get(times.size() - idx);
                    relativeTime = (float) (currentTimeMillis - relativePoint.getRight()) / 1000;
                    relativeProgressRate = (count - relativePoint.getLeft()) / relativeTime;
                } while (relativeTime < progressRateWindowSizeSeconds && idx++ < times.size());

            } else {
                relativeTime = 0;
                relativeProgressRate = 0;
            }
            String progressRateUnits;
            String rateFormat;
            if (progressRateMillionHours) {
                progressRateUnits = "M/h";
                rateFormat = "%.2f";
                progressRate = (progressRate / 1_000_000) * 3600; // Convert to millions per hour
                relativeProgressRate = (relativeProgressRate / 1_000_000) * 3600; // Convert to millions per hour
            } else {
                progressRateUnits = "elements/s";
                rateFormat = "%.0f";
            }
            sb.append(" in ")
                    .append(String.format("%.2f", elapsedTime)).append("s (")
                    .append(String.format(rateFormat, progressRate)).append(" " + progressRateUnits + ")");
            if (addRelativeTime) {
                sb.append(", (")
                        .append(String.format(rateFormat, relativeProgressRate)).append(" " + progressRateUnits + " in last ")
                        .append(String.format("%.2f", relativeTime)).append("s")
                        .append(')');
            }
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

    private void updateBatchSize() {
        batchSize = Math.max((double) totalCount / numLinesLog, MIN_BATCH_SIZE);
    }

    private static Future<Long> getFuture(Callable<Long> totalCountCallable) {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        Future<Long> future = executor.submit(totalCountCallable);
        executor.shutdown();
        return future;
    }

    public long getCount() {
        return count.get();
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
