package org.opencb.commons.bioformats.alignment.sam.io;

import net.sf.samtools.SAMFileHeader;
import net.sf.samtools.SAMFileReader;
import net.sf.samtools.SAMRecord;
import net.sf.samtools.SAMRecordIterator;
import org.opencb.commons.bioformats.alignment.Alignment;
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
public class AlignmentSamDataReader implements AlignmentDataReader<SAMFileHeader> {

    private String filename;
    private SAMFileReader reader;
    public SAMFileHeader header;
    private SAMRecordIterator iterator;
    private boolean enableFileSource;

    public AlignmentSamDataReader(String filename){
        this(filename,false);
    }
    public AlignmentSamDataReader(String filename, boolean enableFileSource) {
        this.filename = filename;
        this.enableFileSource = enableFileSource;
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
        if(enableFileSource){
            reader.enableFileSource(true);
        }
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
    public Alignment read() {

        SAMRecord record = null;
        Alignment alignment = null;
        if(iterator.hasNext()){
            record = iterator.next();
            alignment = new Alignment(record, null);
            /*
            alignment = new Alignment(record.getReadName(), record.getReferenceName(), record.getAlignmentStart(), record.getAlignmentEnd(),
                record.getUnclippedStart(), record.getUnclippedEnd(), record.getReadLength(),
                record.getMappingQuality(), record.getBaseQualityString(),
                record.getMateReferenceName(), record.getMateAlignmentStart(),
                record.getInferredInsertSize(), record.getFlags(),
                AlignmentHelper.getDifferencesFromCigar(record,null), null);
                */
//            Alignment alignment = new Alignment(record, null, record.getReadString());
            alignment.setReadSequence(record.getReadBases());
        }
        return alignment;

    }

    @Override
    public List<Alignment> read(int batchSize) {
        List<Alignment> listRecords = new ArrayList<>(batchSize);
        Alignment alignment;

        for (int i = 0; (i < batchSize) && (alignment = this.read()) != null; i++) {
            listRecords.add(alignment);
        }

        return listRecords;
    }

    public SAMFileHeader getHeader(){
        return header;
    }

}
