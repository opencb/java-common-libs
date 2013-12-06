package org.opencb.commons.bioformats.variant.vcf4.io.writers.stats;

import org.opencb.commons.bioformats.variant.utils.stats.*;
import org.opencb.commons.io.DataWriter;

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

    boolean writeVariantStats(List<VariantStats> data);

    boolean writeGlobalStats(VariantGlobalStats variantGlobalStats);

    boolean writeSampleStats(VariantSampleStats variantSampleStats);

    boolean writeSampleGroupStats(VariantSampleGroupStats variantSampleGroupStats) throws IOException;

    boolean writeVariantGroupStats(VariantGroupStats groupStats) throws IOException;

}
