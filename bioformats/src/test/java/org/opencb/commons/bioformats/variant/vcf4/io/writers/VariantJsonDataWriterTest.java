package org.opencb.commons.bioformats.variant.vcf4.io.writers;

import com.google.common.collect.Lists;
import org.junit.Test;
import org.opencb.commons.bioformats.variant.Variant;
import org.opencb.commons.bioformats.variant.VariantSource;
import org.opencb.commons.bioformats.variant.effect.EffectCalculator;
import org.opencb.commons.bioformats.variant.stats.StatsCalculator;
import org.opencb.commons.bioformats.variant.vcf4.io.readers.VariantReader;
import org.opencb.commons.bioformats.variant.vcf4.io.readers.VariantVcfReader;

import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertTrue;

/**
 * @author Alejandro Aleman Ramos <aaleman@cipf.es>
 */
public class VariantJsonDataWriterTest {
    private String inputFile = getClass().getResource("/variant-test-file.vcf.gz").getFile();


    @Test
    public void testName() throws Exception {

        VariantReader reader = new VariantVcfReader(inputFile);
        VariantWriter writer = new VariantJsonDataWriter(new VariantSource("TEST", "ALIAS", "DESC", null, null), "/home/aaleman/Documents/pruebas/json/out.json");

        writer.includeStats(true);
        writer.includeSamples(true);
        writer.includeEffect(true);

        Variant variant;

        assertTrue(reader.open());
        assertTrue(reader.pre());
        assertTrue(writer.open());
        assertTrue(writer.pre());


        variant = reader.read();

        while (variant != null) {

            variant = reader.read();

            if (variant != null) {
                List<String> sampleNames = Lists.newArrayList(variant.getSampleNames());
                variant.setStats(StatsCalculator.variantStats(Arrays.asList(variant), sampleNames, null).get(0));
//                variant.setEffect(EffectCalculator.getEffects(Arrays.asList(variant)));


                writer.write(variant);
            }

        }

        assertTrue(reader.post());
        assertTrue(writer.post());

        assertTrue(reader.close());
        assertTrue(writer.close());

    }
}
