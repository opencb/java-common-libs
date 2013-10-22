package org.opencb.commons.bioformats.variant.vcf4.io.writers.stats;

import org.opencb.commons.bioformats.commons.DataWriter;
import org.opencb.commons.bioformats.variant.vcf4.VariantEffect;
import org.opencb.commons.bioformats.variant.vcf4.stats.*;

import java.io.IOException;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: aaleman
 * Date: 9/2/13
 * Time: 5:13 PM
 * To change this template use File | Settings | File Templates.
 */
public interface VariantStatsDataWriter extends DataWriter {

    boolean writeVariantStats(List<VcfVariantStat> data);

    boolean writeGlobalStats(VcfGlobalStat globalStats);

    boolean writeSampleStats(VcfSampleStat vcfSampleStat);

    boolean writeSampleGroupStats(VcfSampleGroupStat vcfSampleGroupStat) throws IOException;

    boolean writeVariantGroupStats(VcfVariantGroupStat groupStats) throws IOException;

    boolean writeVariantEffect(List<VariantEffect> batchEffect);
}
