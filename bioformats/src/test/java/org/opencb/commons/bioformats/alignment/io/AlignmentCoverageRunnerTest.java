package org.opencb.commons.bioformats.alignment.io;

import net.sf.samtools.SAMRecord;
import org.junit.Ignore;
import org.junit.Test;
import org.opencb.commons.bioformats.alignment.io.readers.AlignmentDataReader;
import org.opencb.commons.bioformats.alignment.io.writers.AlignmentDataWriter;
import org.opencb.commons.bioformats.alignment.io.writers.AlignmentTabixDataWriter;
import org.opencb.commons.bioformats.alignment.sam.io.AlignmentBamDataReader;
import org.opencb.commons.test.GenericTest;

import java.util.List;

/**
 * Created by aaleman on 12/8/13.
 */
public class AlignmentCoverageRunnerTest extends GenericTest {

    private String bamFile = "/home/aaleman/tmp/bam/file.bam";

    @Test
    public void test(){

        AlignmentDataReader reader = new AlignmentBamDataReader(bamFile, true);
        AlignmentDataWriter writer = new AlignmentTabixDataWriter(bamFile, reader);

        List<SAMRecord> batch;

        int cont = 0;
        if(reader.open() == false){
            System.out.println("File ("+bamFile+") missing. Skipping test.");
            return;
        }
        reader.pre();

        writer.open();
        writer.pre();


        batch = reader.read(1000);
        while (!batch.isEmpty()) {

            System.out.println("Batch: " + cont++);

            writer.writeBatch(batch);

            batch.clear();
            batch = reader.read(1000);

        }


        reader.post();
        reader.close();

        writer.post();
        writer.close();

    }
}
