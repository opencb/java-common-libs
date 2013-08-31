package org.opencb.commons.bioformats.commons.core.vcfindex;

import org.opencb.commons.bioformats.commons.core.connectors.variant.VcfDataReader;
import org.opencb.commons.bioformats.commons.core.connectors.variant.VcfIndexDataWriter;
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

    public static void runner(VcfDataReader vcfReader, VcfIndexDataWriter vcfWriter) throws IOException, FileFormatException {

        int batchSize = 1000;
        List<VcfRecord> batch;

        vcfReader.open();
        vcfWriter.open();

        vcfReader.pre();
        vcfWriter.pre();


        batch = vcfReader.read(batchSize);

        while(!batch.isEmpty()){

            vcfWriter.write(batch);

            batch = vcfReader.read(batchSize);
        }


        vcfReader.post();
        vcfWriter.post();

        vcfReader.close();
        vcfWriter.close();


    }
}
