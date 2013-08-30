package org.opencb.commons.bioformats.commons.core.connectors;

import java.io.IOException;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: aaleman
 * Date: 8/30/13
 * Time: 12:22 PM
 * To change this template use File | Settings | File Templates.
 */
public interface DataWriter<T> {

    public boolean open() throws IOException;

    public boolean close();

    public boolean pre();

    public boolean post();

    public boolean write(T data);
    public boolean write(List<T> data);
}
