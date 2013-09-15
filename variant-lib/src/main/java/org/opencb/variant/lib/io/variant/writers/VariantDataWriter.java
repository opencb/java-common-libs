package org.opencb.variant.lib.io.variant.writers;

import org.opencb.variant.lib.core.formats.VcfRecord;
import org.opencb.variant.lib.io.DataWriter;

import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: aleman
 * Date: 9/15/13
 * Time: 3:40 PM
 * To change this template use File | Settings | File Templates.
 */
public interface VariantDataWriter extends DataWriter {
    void writeVcfHeader(String header);

    void writeBatch(List<VcfRecord> batch);
}
