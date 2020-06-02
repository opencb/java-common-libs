package org.opencb.commons.datastore.mongodb;

import com.mongodb.client.MongoCursor;
import org.bson.Document;
import org.opencb.commons.datastore.core.ComplexTypeConverter;

import java.io.Closeable;
import java.util.Iterator;

public class MongoDBIterator<E> implements Iterator<E>, Closeable {

    private MongoCursor<Document> iterator;
    private ComplexTypeConverter<E, Document> converter;
    private long numMatches;

    public MongoDBIterator(MongoCursor<Document> iterator, long numMatches) {
        this(iterator, null, numMatches);
    }

    public MongoDBIterator(MongoCursor<Document> iterator, ComplexTypeConverter<E, Document> converter, long numMatches) {
        this.iterator = iterator;
        this.converter = converter;
        this.numMatches = numMatches;
    }

    @Override
    public boolean hasNext() {
        return iterator != null && iterator.hasNext();
    }

    @Override
    public E next() {
        Document next = iterator.next();

        if (converter != null) {
            return converter.convertToDataModelType(next);
        } else {
            return (E) next;
        }
    }

    public long getNumMatches() {
        return numMatches;
    }

    @Override
    public void close() {
        if (iterator != null) {
            iterator.close();
        }
    }
}
