package org.opencb.commons.datastore.mongodb;

import com.mongodb.client.FindIterable;

import java.io.Closeable;
import java.io.IOException;
import java.util.Iterator;

public class MongoDBIterator<E> implements Iterator<E>, Closeable {

    private FindIterable<E> iterator;
    private long numMatches;

    public MongoDBIterator(FindIterable<E> iterator, long numMatches) {
        this.iterator = iterator;
        this.numMatches = numMatches;
    }

    @Override
    public boolean hasNext() {
        return iterator.iterator().hasNext();
    }

    @Override
    public E next() {
        return iterator.iterator().next();
    }

    public FindIterable<E> getIterable() {
        return iterator;
    }

    public long getNumMatches() {
        return numMatches;
    }

    @Override
    public void close() throws IOException {
        iterator.iterator().close();
    }
}
