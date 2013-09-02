package org.opencb.commons.bioformats.commons.core.connectors;

/**
 * Created with IntelliJ IDEA.
 * User: aaleman
 * Date: 9/2/13
 * Time: 6:03 PM
 * To change this template use File | Settings | File Templates.
 */
public interface Runner {

    Runner filter();
    Runner parallel();
    Runner run();
}
