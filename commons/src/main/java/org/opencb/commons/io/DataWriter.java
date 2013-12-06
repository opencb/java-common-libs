package org.opencb.commons.io;

/**
 * Created with IntelliJ IDEA.
 * User: aaleman
 * Date: 8/30/13
 * Time: 12:22 PM
 * To change this template use File | Settings | File Templates.
 */
public interface DataWriter {

    public boolean open();

    public boolean close();

    public boolean pre();

    public boolean post();

}
