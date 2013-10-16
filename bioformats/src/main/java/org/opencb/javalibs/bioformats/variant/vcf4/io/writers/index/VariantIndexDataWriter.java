package org.opencb.javalibs.bioformats.variant.vcf4.io.writers.index;


import org.opencb.javalibs.bioformats.commons.DataWriter;
import org.opencb.javalibs.bioformats.variant.vcf4.VcfRecord;

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
