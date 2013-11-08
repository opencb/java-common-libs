package org.opencb.commons.bioformats.variant.vcf4.io;

import org.opencb.commons.bioformats.variant.VariantStudy;
import org.opencb.commons.bioformats.variant.vcf4.io.writers.effect.VariantEffectDataWriter;
import org.opencb.commons.bioformats.variant.vcf4.io.writers.index.VariantDataWriter;
import org.opencb.commons.bioformats.variant.vcf4.io.writers.stats.VariantStatsDataWriter;

/**
 * @author Alejandro Aleman Ramos
 * @author Cristina Yenyxe Gonzalez Garcia
 */
public interface VariantDBWriter<T> extends VariantDataWriter<T>, VariantStatsDataWriter, VariantEffectDataWriter {
    
    boolean writeStudy(VariantStudy study);
    
}
