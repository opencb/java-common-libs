package org.opencb.commons.bioformats.alignment.io.writers;

import net.sf.samtools.BAMIndexer;
import net.sf.samtools.SAMFileHeader;
import net.sf.samtools.SAMRecord;
import org.opencb.commons.bioformats.alignment.io.readers.AlignmentDataReader;
import org.opencb.commons.bioformats.alignment.sam.io.AlignmentSamDataReader;

import java.io.File;
import java.util.List;

/**
 * Created by aaleman on 12/7/13.
 */
public class AlignmentTabixDataWriter implements AlignmentDataWriter<SAMRecord,SAMFileHeader> {

    private BAMIndexer indexer;
    private String filename;
   private File out;
    private AlignmentSamDataReader reader;

    public AlignmentTabixDataWriter(String bamFile, AlignmentDataReader reader) {

        filename = bamFile + ".bai";
this.reader = (AlignmentSamDataReader) reader;


    }

    @Override
    public boolean write(SAMRecord element) {
        return false;
    }

    @Override
    public boolean write(List<SAMRecord> batch) {

        for(SAMRecord record: batch){
            indexer.processAlignment(record);
        }
        return false;
    }

    @Override
    public boolean writeHeader(SAMFileHeader head) {
        return false;
    }

    @Override
    public boolean open() {

        out = new File(filename);
        return true;
    }

    @Override
    public boolean close() {
        indexer.finish();
        return false;
    }

    @Override
    public boolean pre() {
        indexer = new BAMIndexer(out, reader.getHeader());

        return true;
    }

    @Override
    public boolean post() {

        return false;
    }
}
