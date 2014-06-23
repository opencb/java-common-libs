package org.opencb.commons.bioformats.alignment.io.writers;


import org.opencb.commons.io.DataWriter;

import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: jcoll
 * Date: 12/3/13
 * Time: 5:17 PM
 * To change this template use File | Settings | File Templates.
 */
public interface AlignmentDataWriter<T, H> extends DataWriter<T> {

    boolean writeHeader(H head);

}
