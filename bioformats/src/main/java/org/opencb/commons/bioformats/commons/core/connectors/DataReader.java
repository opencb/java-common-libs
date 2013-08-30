package org.opencb.commons.bioformats.commons.core.connectors;

import org.opencb.commons.bioformats.commons.exception.FileFormatException;

import java.io.IOException;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: aaleman
 * Date: 8/30/13
 * Time: 12:20 PM
 * To change this template use File | Settings | File Templates.
 */
public interface DataReader<T> {

    public boolean open() throws IOException;

    public boolean close() throws IOException;

    public boolean pre() throws IOException, FileFormatException;

    public boolean post();

    public T read();

    public List<T> read(int batchSize);
}
