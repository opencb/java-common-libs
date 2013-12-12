package org.opencb.commons.run;


import org.opencb.commons.io.DataReader;
import org.opencb.commons.io.DataWriter;

import java.io.IOException;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: aaleman
 * Date: 12/3/13
 * Time: 10:04 AM
 * To change this template use File | Settings | File Templates.
 */
public abstract class Runner<R extends DataReader<E>, W extends DataWriter, E> {

    protected R reader;
    protected W writer;
    protected Runner prev;
    protected int batchSize;


    public Runner(R reader, W writer, Runner prev) {
        this(reader, writer);
        this.prev = prev;
    }

    public Runner(R reader, W writer) {
        this.reader = reader;
        this.writer = writer;
        this.batchSize = 1000;
    }

    public abstract List<E> apply(List<E> batch) throws IOException;

    public void pre() throws IOException {
    }

    public void post() throws IOException {
    }

    public List<E> launch(List<E> batch) throws IOException {

        if (prev != null) {
            batch = prev.launch(batch);
        }

        batch = this.apply(batch);
        return batch;
    }

    public void launchPre() throws IOException {
        if (prev != null) {
            prev.launchPre();
        }
        this.pre();
    }

    public void launchPost() throws IOException {
        if (prev != null) {
            prev.launchPost();
        }
        this.post();
    }

    public void writerPre() {
        if (prev != null) {
            prev.writerPre();
        }
        if (writer != null)
            writer.pre();
    }

    public void writerOpen() {
        if (prev != null) {
            prev.writerOpen();
        }
        if (writer != null)
            writer.open();
    }

    public void writerPost() {
        if (prev != null) {
            prev.writerPost();
        }
        if (writer != null)
            writer.post();
    }

    public void writerClose() {
        if (prev != null) {
            prev.writerClose();
        }
        if (writer != null)
            writer.close();
    }

    public void run() throws IOException {
        List<E> batch;

        int cont = 0;
        reader.open();
        reader.pre();

        this.writerOpen();
        this.writerPre();

        this.launchPre();

        batch = reader.read(batchSize);
        while (!batch.isEmpty()) {

            batch = this.launch(batch);
            batch.clear();
            batch = reader.read(batchSize);

        }

        this.launchPost();

        reader.post();
        reader.close();

        this.writerPost();
        this.writerClose();

    }

    public int getBatchSize() {
        return batchSize;
    }

    public void setBatchSize(int batchSize) {
        this.batchSize = batchSize;
    }
}
