package org.opencb.commons.bioformats.alignment.io.readers;

import org.opencb.commons.bioformats.commons.DataReader;

/**
 * Created with IntelliJ IDEA.
 * User: jcoll
 * Date: 12/3/13
 * Time: 5:17 PM
 * To change this template use File | Settings | File Templates.
 */
public interface AlignmentDataReader<T, H> extends DataReader<T> {

    public H getHeader();
}
