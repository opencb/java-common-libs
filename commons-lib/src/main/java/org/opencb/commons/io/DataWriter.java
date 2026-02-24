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

package org.opencb.commons.io;

import org.opencb.commons.run.Task;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Created with IntelliJ IDEA.
 * User: aaleman
 * Date: 8/30/13
 * Time: 12:22 PM
 * To change this template use File | Settings | File Templates.
 */
@FunctionalInterface
public interface DataWriter<T> {

    default boolean open() {
        return true;
    }

    default boolean close() {
        return true;
    }

    default boolean pre() {
        return true;
    }

    default boolean post() {
        return true;
    }

    default boolean write(T elem) {
        return write(Collections.singletonList(elem));
    }

    boolean write(List<T> batch);

    default Task<T, T> asTask() {
        return asTask(false);
    }

    /**
     * Create a Task from the DataWriter.
     *
     * @param synchronizeWrites Do not execute parallel writes.
     * @return                  Task
     */
    default Task<T, T> asTask(boolean synchronizeWrites) {
        AtomicBoolean pre = new AtomicBoolean(false);
        AtomicBoolean post = new AtomicBoolean(false);
        return new Task<T, T>() {
            @Override
            public void pre() throws Exception {
                if (!pre.getAndSet(true)) {
                    DataWriter.this.open();
                    DataWriter.this.pre();
                }
            }

            @Override
            public List<T> apply(List<T> batch) throws Exception {
                if (synchronizeWrites) {
                    synchronized (pre) {
                        DataWriter.this.write(batch);
                    }
                } else {
                    DataWriter.this.write(batch);
                }
                return batch;
            }

            @Override
            public void post() throws Exception {
                if (!post.getAndSet(true)) {
                    DataWriter.this.post();
                    DataWriter.this.close();
                }
            }
        };
    }

    /**
     * Fan out to two writers receiving the same batches sequentially.
     *
     * @param dw1  First writer
     * @param dw2  Second writer
     * @param <T>  Batch element type
     * @return     Composite writer that writes to both dw1 and dw2 in sequence.
     */
    static <T> DataWriter<T> tee(DataWriter<T> dw1, DataWriter<T> dw2) {
        return dw1.then(dw2.asTask());
    }

    /**
     * Fan out to two writers receiving the same batches.
     *
     * <p>When {@code parallel=true} each writer runs in its own background thread.
     * Batches are enqueued from the caller thread; the background threads consume and write.
     * Any exception thrown by a background thread is rethrown from {@link #post()}.
     *
     * @param dw1      First writer
     * @param dw2      Second writer
     * @param parallel Whether to run each writer in its own background thread
     * @param <T>      Batch element type
     * @return         Composite writer that writes to both dw1 and dw2.
     */
    static <T> DataWriter<T> tee(DataWriter<T> dw1, DataWriter<T> dw2, boolean parallel) {
        if (!parallel) {
            return tee(dw1, dw2);
        }
        return new DataWriter<T>() {
            private final BlockingQueue<Optional<List<T>>> queue1 = new LinkedBlockingQueue<>();
            private final BlockingQueue<Optional<List<T>>> queue2 = new LinkedBlockingQueue<>();
            private Thread thread1;
            private Thread thread2;
            private volatile Throwable error1;
            private volatile Throwable error2;

            @Override
            public boolean pre() {
                thread1 = new Thread(() -> {
                    try {
                        dw1.open();
                        dw1.pre();
                        Optional<List<T>> item = queue1.take();
                        while (item.isPresent()) {
                            dw1.write(item.get());
                            item = queue1.take();
                        }
                        dw1.post();
                        dw1.close();
                    } catch (Throwable t) {
                        error1 = t;
                    }
                }, Thread.currentThread().getName() + "writer-1");
                thread2 = new Thread(() -> {
                    try {
                        dw2.open();
                        dw2.pre();
                        Optional<List<T>> item = queue2.take();
                        while (item.isPresent()) {
                            dw2.write(item.get());
                            item = queue2.take();
                        }
                        dw2.post();
                        dw2.close();
                    } catch (Throwable t) {
                        error2 = t;
                    }
                }, Thread.currentThread().getName() + "writer-2");
                thread1.start();
                thread2.start();
                return true;
            }

            @Override
            public boolean write(List<T> batch) {
                if (error1 != null || error2 != null) {
                    throw new RuntimeException("Tee background writer has failed");
                }
                queue1.add(Optional.of(batch));
                queue2.add(Optional.of(batch));
                return true;
            }

            @Override
            public boolean post() {
                queue1.add(Optional.empty());
                queue2.add(Optional.empty());
                try {
                    thread1.join();
                    thread2.join();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
                if (error1 != null) {
                    throw new RuntimeException("Error in tee background writer 1", error1);
                }
                if (error2 != null) {
                    throw new RuntimeException("Error in tee background writer 2", error2);
                }
                return true;
            }
        };
    }

    default DataWriter<T> then(DataWriter<T> nextWriter) {
        return then(nextWriter.asTask());
    }

    default DataWriter<T> then(Task<T, ?> nextTask) {
        return new DataWriter<T>() {
            @Override
            public boolean open() {
                return DataWriter.this.open();
            }

            @Override
            public boolean pre() {
                boolean res = DataWriter.this.pre();
                try {
                    nextTask.pre();
                } catch (RuntimeException e) {
                    throw e;
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
                return res;
            }

            @Override
            public boolean close() {
                return DataWriter.this.close();
            }

            @Override
            public boolean post() {
                boolean res = DataWriter.this.post();
                try {
                    nextTask.post();
                } catch (RuntimeException e) {
                    throw e;
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
                return res;
            }

            @Override
            public boolean write(List<T> batch) {
                boolean res = DataWriter.this.write(batch);
                try {
                    nextTask.apply(batch);
                    return res;
                } catch (RuntimeException e) {
                    throw e;
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }
        };
    }

}
