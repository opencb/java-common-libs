package org.opencb.commons.bioformats.commons.core.connectors.variant.writers;

import org.opencb.commons.bioformats.commons.core.connectors.DataWriter;
import org.opencb.commons.bioformats.commons.core.variant.vcf4.VcfRecord;
import org.opencb.commons.bioformats.commons.core.vcfstats.*;

import java.io.IOException;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: aaleman
 * Date: 9/2/13
 * Time: 5:13 PM
 * To change this template use File | Settings | File Templates.
 */
public interface VcfDataWriter extends DataWriter {

    boolean writeVariantStats(List<VcfRecordStat> data);

    boolean writeGlobalStats(VcfGlobalStat globalStats);

    boolean writeSampleStats(VcfSampleStat vcfSampleStat);

    boolean writeSampleGroupStats(VcfSampleGroupStats vcfSampleGroupStats) throws IOException;

    boolean writeVariantGroupStats(VcfVariantGroupStat groupStats) throws IOException;

    boolean writeVariantIndex(List<VcfRecord> data);

}
