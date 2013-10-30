package org.opencb.commons.bioformats.variant.vcf4.io.writers.index;

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
public interface VariantDataWriter<T> extends DataWriter {
    boolean writeHeader(String header);

    boolean writeBatch(List<T> batch);
}
