package org.opencb.variant.lib.io;

import org.opencb.variant.lib.annot.Annot;
import org.opencb.variant.lib.core.formats.VcfRecord;
import org.opencb.variant.lib.io.variant.annotators.VcfAnnotator;
import org.opencb.variant.lib.io.variant.readers.VariantDataReader;
import org.opencb.variant.lib.io.variant.readers.VariantVcfDataReader;
import org.opencb.variant.lib.io.variant.writers.VariantDataWriter;
import org.opencb.variant.lib.io.variant.writers.VariantVcfDataWriter;

import java.util.HashMap;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: aleman
 * Date: 9/13/13
 * Time: 8:01 PM
 * To change this template use File | Settings | File Templates.
 */
public class VariantAnnotRunner {

    private VariantDataReader vcfReader;
    private VariantDataWriter vcfWriter;
    private List<VcfAnnotator> annots;


    public VariantAnnotRunner() {
    }

    public VariantAnnotRunner(String vcfFileName, String vcfOutFilename) {
        this();

        vcfReader = new VariantVcfDataReader(vcfFileName);
        vcfWriter = new VariantVcfDataWriter(vcfOutFilename);

    }

    public void run() {

        int batchSize = 1000;
        int cont = 1;
        List<VcfRecord> batch;

        vcfReader.open();
        vcfWriter.open();

        vcfReader.pre();
        vcfReader.post();

        batch = vcfReader.read(batchSize);

        vcfWriter.writeVcfHeader(vcfReader.getHeader());

        while (!batch.isEmpty()) {

            System.out.println("Batch: " + cont++);

            Annot.applyAnnotations(batch, this.annots);

            vcfWriter.writeBatch(batch);

            batch = vcfReader.read(batchSize);

        }

        vcfReader.post();
        vcfWriter.post();

        vcfReader.close();
        vcfWriter.close();

    }

    public void annotations(List<VcfAnnotator> listAnnots) {
        this.annots = listAnnots;
    }
}
