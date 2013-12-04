package org.opencb.commons.bioformats.alignment.io;


import org.opencb.commons.bioformats.commons.DataWriter;

import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: jcoll
 * Date: 12/3/13
 * Time: 5:17 PM
 * To change this template use File | Settings | File Templates.
 */
public interface AlignmentDataWriter<T, H> extends DataWriter {

    boolean write(T element);

    boolean writeBatch(List<T> batch);

    boolean writeHeader(H head);

}
