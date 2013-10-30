package org.opencb.commons.bioformats.variant.vcf4.io;

import org.opencb.commons.bioformats.variant.vcf4.io.writers.effect.VariantEffectDataWriter;
import org.opencb.commons.bioformats.variant.vcf4.io.writers.index.VariantDataWriter;
import org.opencb.commons.bioformats.variant.vcf4.io.writers.stats.VariantStatsDataWriter;

/**
 * Created with IntelliJ IDEA.
 * User: aaleman
 * Date: 10/30/13
 * Time: 3:01 PM
 * To change this template use File | Settings | File Templates.
 */
public interface VariantDBWriter<T> extends VariantDataWriter<T>, VariantStatsDataWriter, VariantEffectDataWriter {
}
