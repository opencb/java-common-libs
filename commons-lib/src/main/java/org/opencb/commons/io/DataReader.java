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

import com.google.common.base.Throwables;
import org.opencb.commons.run.Task;

import java.util.List;

/**
 * @author Alejandro Aleman Ramos <aaleman@cipf.es>
 * @author Cristina Yenyxe Gonzalez Garcia <cyenyxe@ebi.ac.uk>
 */
@FunctionalInterface
public interface DataReader<T> {

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

    default List<T> read() {
        return read(1);
    }

    List<T> read(int batchSize);


    default <O> DataReader<O> then(Task<T, O> task) {
        return new DataReader<O>() {
            @Override
            public boolean open() {
                return DataReader.this.open();
            }

            @Override
            public boolean pre() {
                try {
                    task.pre();
                } catch (Exception e) {
                    throw Throwables.propagate(e);
                }
                return DataReader.this.pre();
            }

            @Override
            public boolean close() {
                return DataReader.this.close();
            }

            @Override
            public boolean post() {
                try {
                    task.post();
                } catch (Exception e) {
                    throw Throwables.propagate(e);
                }
                return DataReader.this.post();
            }

            @Override
            public List<O> read(int batch) {
                try {
                    List<T> read = DataReader.this.read(batch);
                    // Iterate while reader has data
                    // Exit when reader is exhausted, or task produces data
                    while (read != null && !read.isEmpty()) {
                        List<O> apply = task.apply(read);
                        if (apply != null && !apply.isEmpty()) {
                            // Valid task apply.
                            return apply;
                        } else {
                            // Empty task apply. Read more data
                            read = DataReader.this.read(batch);
                        }
                    }
                    // Reader is exhausted. Drain the task.
                    return task.drain();
                } catch (Exception e) {
                    // TODO: Reader should throw any exception
                    throw Throwables.propagate(e);
                }
            }
        };
    }

}
