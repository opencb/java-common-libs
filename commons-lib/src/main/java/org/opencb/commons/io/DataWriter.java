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
