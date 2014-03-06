package org.opencb.commons.io;

import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: aaleman
 * Date: 8/30/13
 * Time: 12:20 PM
 * To change this template use File | Settings | File Templates.
 */
public interface DataReader<T> extends DataManager {

    public T read();

    public List<T> read(int batchSize);
}
