package org.opencb.commons.io;

/**
 * Created with IntelliJ IDEA.
 * User: jcoll
 * Date: 3/6/14
 * Time: 7:04 PM
 * To change this template use File | Settings | File Templates.
 */
public interface DataManager {

    public boolean open();

    public boolean close();

    public boolean pre();

    public boolean post();


}
