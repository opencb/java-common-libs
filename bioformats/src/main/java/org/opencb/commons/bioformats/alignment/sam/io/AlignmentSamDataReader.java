package org.opencb.commons.bioformats.alignment.sam.io;

import net.sf.samtools.SAMFileHeader;
import net.sf.samtools.SAMFileReader;
import net.sf.samtools.SAMRecord;
import net.sf.samtools.SAMRecordIterator;
import org.opencb.commons.bioformats.alignment.io.readers.AlignmentDataReader;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: imedina
 * Date: 10/30/13
 * Time: 5:12 PM
 * To change this template use File | Settings | File Templates.
 */
public class AlignmentSamDataReader implements AlignmentDataReader<SAMRecord, SAMFileHeader> {

    private String filename;
    private SAMFileReader reader;
    public SAMFileHeader header;
    private SAMRecordIterator iterator;

    public AlignmentSamDataReader(String filename) {
        this.filename = filename;
    }

    @Override
    public boolean open() {

        Path path;
        File file;
        path = Paths.get(this.filename);
        if(!Files.exists(path))
            return false;
        file = path.toFile();

        reader = new SAMFileReader(file);
        reader.setValidationStringency(SAMFileReader.ValidationStringency.LENIENT);
        iterator = reader.iterator();

        return true;
    }

    @Override
    public boolean close() {
        reader.close();
        return true;
    }

    @Override
    public boolean pre() {
        header = reader.getFileHeader();
        return true;
    }

    @Override
    public boolean post() {
        return true;
    }

    @Override
    public SAMRecord read() {

        SAMRecord record = null;

        if(iterator.hasNext()){
            record = iterator.next();
        }
        return record;

    }

    @Override
    public List<SAMRecord> read(int batchSize) {
        List<SAMRecord> listRecords = new ArrayList<>(batchSize);
        SAMRecord record;

        for (int i = 0; (i < batchSize) && (record = this.read()) != null; i++) {
            listRecords.add(record);
        }

        return listRecords;
    }

    public SAMFileHeader getHeader(){
        return header;
    }

}
