package org.opencb.commons.bioformats.alignment.sam.io;

import org.junit.Test;
import static org.junit.Assert.*;
import org.opencb.commons.bioformats.alignment.Alignment;
import org.opencb.commons.bioformats.alignment.AlignmentRegion;
import org.opencb.commons.bioformats.alignment.io.readers.AlignmentRegionDataReader;
import org.opencb.commons.bioformats.alignment.io.writers.AlignmentRegionDataWriter;
import org.opencb.commons.test.GenericTest;

import java.io.IOException;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: jcoll
 * Date: 5/9/14
 * Time: 11:49 AM
 * To change this template use File | Settings | File Templates.
 */
public class AlignmentSamDataWriterTest extends GenericTest {

    @Test
    public void readWriteTest() {

        AlignmentSamDataReader reader = new AlignmentSamDataReader("/tmp/small.sam");
        AlignmentSamDataWriter writer = new AlignmentSamDataWriter("/tmp/readWriteTestOutput.sam", reader);

        if(!reader.open()){
            System.out.println("File not found. Test not launched.");
            return;
        }

        writer.open();

        reader.pre();
        writer.pre();


        List<Alignment> alignmentList;
        while(!(alignmentList = reader.read(1000)).isEmpty()) {
            assertTrue(writer.write(alignmentList));
        }


        reader.post();
        writer.post();

        reader.close();
        writer.close();
    }


    @Test
    public void readWriteRegionTest() {
        AlignmentSamDataReader samReader = new AlignmentSamDataReader("/tmp/small.sam");
        AlignmentSamDataWriter samWriter = new AlignmentSamDataWriter("/tmp/readWriteTestOutput.sam", samReader);
        AlignmentRegionDataReader reader = new AlignmentRegionDataReader(samReader);
        AlignmentRegionDataWriter writer = new AlignmentRegionDataWriter(samWriter);

        if(!reader.open()){
            System.out.println("File not found. Test not launched.");
            return;
        }

        writer.open();

        reader.pre();
        writer.pre();


        List<AlignmentRegion> alignmentRegionList;
        while(!(alignmentRegionList = reader.read(1)).isEmpty()) {
            assertTrue(writer.write(alignmentRegionList));
        }


        reader.post();
        writer.post();

        reader.close();
        writer.close();

    }
}
