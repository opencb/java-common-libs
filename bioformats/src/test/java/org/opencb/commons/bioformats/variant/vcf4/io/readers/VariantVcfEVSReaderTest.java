package org.opencb.commons.bioformats.variant.vcf4.io.readers;

import org.junit.Test;
import org.opencb.commons.bioformats.variant.Variant;
import org.opencb.commons.bioformats.variant.utils.stats.VariantStats;
import org.opencb.commons.test.GenericTest;

import static org.junit.Assert.assertTrue;

/**
 * @author Alejandro Aleman Ramos <aaleman@cipf.es>
 */
public class VariantVcfEVSReaderTest extends GenericTest {


    private String inputFile = getClass().getResource("/evs.vcf.gz").getFile();

    @Test
    public void testRead() throws Exception {

        VariantReader reader = new VariantVcfEVSReader(inputFile);
        Variant variant;

        assertTrue(reader.open());
        assertTrue(reader.pre());

        variant = reader.read();

        float maxMaf = Float.MIN_VALUE;
        int i = 0;
        while (variant != null) {

            variant = reader.read();

        }

        assertTrue(reader.post());
        assertTrue(reader.close());

    }
}
