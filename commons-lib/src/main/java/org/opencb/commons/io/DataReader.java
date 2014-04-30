package org.opencb.commons.io;

import java.util.List;

/**
 * @author Alejandro Aleman Ramos <aaleman@cipf.es>
 * @author Cristina Yenyxe Gonzalez Garcia <cyenyxe@ebi.ac.uk>
 */
public interface DataReader<T> {

    public boolean open();

    public boolean close();

    public boolean pre();

    public boolean post();

    public T read();

    public List<T> read(int batchSize);
}
