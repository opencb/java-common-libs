package org.opencb.commons.bioformats.variant.vcf4.io.writers.stats;

import org.opencb.commons.bioformats.commons.DataWriter;
import org.opencb.commons.bioformats.variant.utils.stats.*;

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

    boolean writeVariantStats(List<VariantStat> data);

    boolean writeGlobalStats(GlobalStat globalStats);

    boolean writeSampleStats(SampleStat sampleStat);

    boolean writeSampleGroupStats(SampleGroupStat sampleGroupStat) throws IOException;

    boolean writeVariantGroupStats(VariantGroupStat groupStats) throws IOException;

}
