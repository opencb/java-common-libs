package org.opencb.commons.bioformats.pedigree.io.writers;


import org.opencb.commons.bioformats.commons.DataWriter;
import org.opencb.commons.bioformats.pedigree.Pedigree;

/**
 * Created with IntelliJ IDEA.
 * User: aleman
 * Date: 9/1/13
 * Time: 12:54 PM
 * To change this template use File | Settings | File Templates.
 */
public interface PedDataWriter extends DataWriter {

    boolean write(Pedigree data);
}
