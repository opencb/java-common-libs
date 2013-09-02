package org.opencb.commons.bioformats.commons.core.vcfindex;

import org.opencb.commons.bioformats.commons.core.connectors.variant.readers.VcfDataReader;
import org.opencb.commons.bioformats.commons.core.connectors.variant.writers.VcfDataWriter;
import org.opencb.commons.bioformats.commons.core.variant.vcf4.VcfRecord;
import org.opencb.commons.bioformats.commons.exception.FileFormatException;

import java.io.IOException;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: aleman
 * Date: 8/30/13
 * Time: 7:29 PM
 * To change this template use File | Settings | File Templates.
 */
public class VcfIndex {

    public static void runner(VcfDataReader vcfReader, VcfDataWriter vcfWriter) throws IOException, FileFormatException {

        int batchSize = 1000;
        List<VcfRecord> batch;

        vcfReader.open();
        vcfWriter.open();

        vcfReader.pre();
        vcfWriter.indexPre();

        batch = vcfReader.read(batchSize);

        while(!batch.isEmpty()){

            vcfWriter.writeVariantIndex(batch);

            batch = vcfReader.read(batchSize);
        }

        vcfReader.post();
        vcfWriter.indexPost();

        vcfReader.close();
        vcfWriter.close();


    }
}
