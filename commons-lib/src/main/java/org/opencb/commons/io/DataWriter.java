package org.opencb.commons.io;

import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: aaleman
 * Date: 8/30/13
 * Time: 12:22 PM
 * To change this template use File | Settings | File Templates.
 */
public interface DataWriter<T> {

    public boolean open();

    public boolean close();

    public boolean pre();

    public boolean post();

    public boolean write(T elem);

    public boolean write(List<T> batch);

}
