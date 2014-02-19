package org.opencb.commons.bioformats.alignment.sam.io;

import net.sf.samtools.SAMFileHeader;
import net.sf.samtools.SAMRecord;
import net.sf.samtools.SAMTextWriter;
import org.opencb.commons.bioformats.alignment.io.writers.AlignmentDataWriter;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: jcoll
 * Date: 12/3/13
 * Time: 5:31 PM
 * To change this template use File | Settings | File Templates.
 */
public class AlignmentSamDataWriter implements AlignmentDataWriter<SAMRecord, SAMFileHeader> {

    private SAMTextWriter writer;
    private String filename;

    public AlignmentSamDataWriter(String filename) {
        this.filename = filename;
    }

    @Override
    public boolean open() {
        Path path;
        File file;
        path = Paths.get(this.filename);
        file = path.toFile();

        this.writer = new SAMTextWriter(file);

        return true;
    }

    @Override
    public boolean close() {
        writer.close();
        return true;
    }

    @Override
    public boolean pre() {
        return true;
    }

    @Override
    public boolean post() {
        return true;
    }

    @Override
    public boolean write(SAMRecord element) {
        writer.writeAlignment(element);
        return true;
    }

    @Override
    public boolean write(List<SAMRecord> batch) {
        for(SAMRecord r : batch){
            write(r);
        }
        return true;
    }


    @Override
    public boolean writeHeader(SAMFileHeader head) {
        writer.setSortOrder(head.getSortOrder(), true);
        writer.setHeader(head);
        return true;
    }
}
