package org.opencb.variant.lib.io.ped.writers;


import org.opencb.variant.lib.core.formats.Pedigree;
import org.opencb.variant.lib.io.DataWriter;

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
