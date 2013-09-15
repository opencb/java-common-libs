package org.opencb.variant.lib.io.variant.writers;

import org.opencb.variant.lib.core.formats.*;
import org.opencb.variant.lib.io.DataWriter;

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

    boolean writeVariantIndex(List<VcfRecord> data);

    boolean writeVariantEffect(List<VariantEffect> batchEffect);
}
