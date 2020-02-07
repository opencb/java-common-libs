package org.opencb.commons.datastore.mongodb;

import com.mongodb.client.MongoCursor;

import java.io.Closeable;
import java.util.Iterator;

public class MongoDBIterator<E> implements Iterator<E>, Closeable {

    private MongoCursor<E> iterator;
    private long numMatches;

    public MongoDBIterator(MongoCursor<E> iterator, long numMatches) {
        this.iterator = iterator;
        this.numMatches = numMatches;
    }

    @Override
    public boolean hasNext() {
        return iterator.hasNext();
    }

    @Override
    public E next() {
        return iterator.next();
    }

    public long getNumMatches() {
        return numMatches;
    }

    @Override
    public void close() {
        iterator.close();
    }
}
