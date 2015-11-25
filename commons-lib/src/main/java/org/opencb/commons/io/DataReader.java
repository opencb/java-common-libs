package org.opencb.commons.io;

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
}
