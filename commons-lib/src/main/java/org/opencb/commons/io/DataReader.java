package org.opencb.commons.io;

import java.util.List;

/**
 * @author Alejandro Aleman Ramos <aaleman@cipf.es>
 * @author Cristina Yenyxe Gonzalez Garcia <cyenyxe@ebi.ac.uk>
 */
@FunctionalInterface
public interface DataReader<T> {

    default public boolean open() {return true;}

    default public boolean close() {return true;}

    default public boolean pre() {return true;}

    default public boolean post() {return true;}

    default public List<T> read() {return read(1);}

    public List<T> read(int batchSize);
}
