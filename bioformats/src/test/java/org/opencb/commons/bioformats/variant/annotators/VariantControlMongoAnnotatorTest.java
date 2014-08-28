package org.opencb.commons.bioformats.variant.annotators;

import org.junit.Test;
import org.opencb.commons.bioformats.variant.Variant;
import org.opencb.commons.bioformats.variant.vcf4.io.readers.VariantReader;
import org.opencb.commons.bioformats.variant.vcf4.io.readers.VariantVcfReader;
import org.opencb.commons.bioformats.variant.vcf4.io.writers.VariantVcfDataWriter;
import org.opencb.commons.bioformats.variant.vcf4.io.writers.VariantWriter;
import org.opencb.commons.io.DataReader;
import org.opencb.commons.io.DataWriter;
import org.opencb.commons.test.GenericTest;

import java.util.List;

import static org.junit.Assert.assertTrue;

/**
 * @author Alejandro Aleman Ramos <aaleman@cipf.es>
 */
public class VariantControlMongoAnnotatorTest extends GenericTest {
    @Test
    public void testAnnot() throws Exception {

        String inputFile = getClass().getResource("/variant-test-file.vcf.gz").getFile();
        String output = "/tmp/out.vcf";

        VariantReader reader = new VariantVcfReader(inputFile);
        VariantWriter writer = new VariantVcfDataWriter(reader, output);

        VariantAnnotator control = new VariantControlMongoAnnotator();
        VariantAnnotator gene = new VariantGeneNameAnnotator();
        VariantConsequenceTypeAnnotator ct = new VariantConsequenceTypeAnnotator();

        reader.open();
        writer.open();
        reader.pre();
        writer.pre();

        List<Variant> batch = reader.read(100);

        while (!batch.isEmpty()) {


            control.annot(batch);
            gene.annot(batch);
            ct.annot(batch);

            writer.write(batch);

            batch.clear();
            batch = reader.read(10);
        }

        reader.post();
        writer.post();
        reader.close();
        writer.close();

        assertTrue(1 == 1);
    }
}
