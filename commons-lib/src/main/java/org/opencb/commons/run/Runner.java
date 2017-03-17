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

import org.opencb.commons.io.DataReader;
import org.opencb.commons.io.DataWriter;

import java.io.IOException;
import java.util.List;

/**
 * @author Alejandro Aleman Ramos <aaleman@cipf.es>
 */
public class Runner<T> {

    protected DataReader<T> reader;
    protected List<? extends DataWriter<T>> writers;
    protected List<Task<T>> tasks;
    protected int batchSize;
    protected int threads;

    public Runner(DataReader<T> reader, List<? extends DataWriter<T>> writers, List<Task<T>> tasks, int batchSize) {
        this.reader = reader;
        this.writers = writers;
        this.tasks = tasks;
        this.batchSize = batchSize;
    }

    public Runner(DataReader<T> reader, List<? extends DataWriter<T>> writers, List<Task<T>> tasks) {
        this(reader, writers, tasks, 1000);
    }

    public void pre() throws IOException {
    }

    public void post() throws IOException {
    }

    public void launch(List<T> batch) throws IOException {

        for (Task<T> t : tasks) {
            t.apply(batch);
        }

    }

    public void launchPre() throws IOException {
        for (Task<T> t : tasks) {
            t.pre();
        }
    }

    public void launchPost() throws IOException {
        for (Task<T> t : tasks) {
            t.post();
        }
    }

    public void writerInit() {

        for (DataWriter<T> dw : writers) {
            dw.open();
            dw.pre();
        }

    }

    public void writerClose() {
        for (DataWriter<T> dw : writers) {
            dw.post();
            dw.close();
        }
    }

    public void run() throws IOException {
        List<T> batch;

        this.readerInit();
        this.writerInit();

        this.launchPre();

        batch = reader.read(batchSize);
        while (!batch.isEmpty()) {

            this.launch(batch);

            for (DataWriter<T> dw : writers) {
                dw.write(batch);
            }

            batch.clear();
            batch = reader.read(batchSize);
        }

        this.launchPost();

        this.readerClose();
        this.writerClose();

    }

    protected void readerClose() {
        this.reader.post();
        this.reader.close();

    }

    protected void readerInit() {
        this.reader.open();
        this.reader.pre();

    }

    public int getBatchSize() {
        return batchSize;
    }

    public void setBatchSize(int batchSize) {
        this.batchSize = batchSize;
    }

}
