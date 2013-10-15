package org.opencb.variant.lib.io.variant.writers.index;

import org.opencb.variant.lib.core.formats.VcfRecord;
import org.opencb.variant.lib.io.DataWriter;

import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: aaleman
 * Date: 10/15/13
 * Time: 9:32 AM
 * To change this template use File | Settings | File Templates.
 */
public interface VariantIndexDataWriter extends DataWriter {

    boolean writeVariantIndex(List<VcfRecord> data);

}
