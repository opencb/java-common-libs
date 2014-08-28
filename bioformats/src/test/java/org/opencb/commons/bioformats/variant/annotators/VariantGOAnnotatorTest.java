package org.opencb.commons.bioformats.variant.annotators;

import org.junit.Test;
import org.opencb.commons.bioformats.variant.Variant;
import org.opencb.commons.bioformats.variant.vcf4.io.readers.VariantReader;
import org.opencb.commons.bioformats.variant.vcf4.io.readers.VariantVcfReader;
import org.opencb.commons.bioformats.variant.vcf4.io.writers.VariantVcfDataWriter;
import org.opencb.commons.bioformats.variant.vcf4.io.writers.VariantWriter;

import java.util.List;
import java.util.TreeMap;
import java.util.TreeSet;

import static org.junit.Assert.*;

public class VariantGOAnnotatorTest {

    @Test
    public void testAnnot() throws Exception {


//        String inputFile = getClass().getResource("/variant-test-file.vcf.gz").getFile();
//        String output = "/tmp/out.vcf";
//
//        VariantReader reader = new VariantVcfReader(inputFile);
//        VariantWriter writer = new VariantVcfDataWriter(reader, output);
//
//        VariantAnnotator goAnnotator = new VariantGOAnnotator();
//        VariantAnnotator geneAnnotator = new VariantGeneNameAnnotator();
//        reader.open();
//        writer.open();
//        reader.pre();
//        writer.pre();
//
//        List<Variant> batch = reader.read(500);
//
//        while (!batch.isEmpty()) {
//
//            geneAnnotator.annot(batch);
//            goAnnotator.annot(batch);
//
//            writer.write(batch);
//
//            batch.clear();
//            batch = reader.read(500);
//        }
//
//        reader.post();
//        writer.post();
//        reader.close();
//        writer.close();
//
//        assertTrue(1 == 1);
        this.prueba();

    }

    public void prueba(){
        TreeSet<String> x = new TreeSet<>();
        x.add("ZZ");
        DefaultTreeN

    }
}
