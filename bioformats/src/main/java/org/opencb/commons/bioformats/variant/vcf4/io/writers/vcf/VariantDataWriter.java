package org.opencb.commons.bioformats.variant.vcf4.io.writers.vcf;

import org.opencb.commons.bioformats.commons.DataWriter;
import org.opencb.commons.bioformats.variant.vcf4.VcfRecord;

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
