package org.opencb.commons.bioformats.alignment.sam.io;

import net.sf.samtools.BAMFileWriter;
import net.sf.samtools.SAMFileHeader;
import net.sf.samtools.SAMRecord;
import org.opencb.commons.bioformats.alignment.io.writers.AlignmentDataWriter;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: jcoll
 * Date: 12/3/13
 * Time: 5:33 PM
 * To change this template use File | Settings | File Templates.
 */
public class AlignmentBamDataWriter implements AlignmentDataWriter<SAMRecord, SAMFileHeader>  {

    public BAMFileWriter writer;
    private String filename;

    public AlignmentBamDataWriter(String filename) {
        this.filename = filename;
    }

    @Override
    public boolean open() {
        Path path;
        File file;
        path = Paths.get(this.filename);
        file = path.toFile();

        writer = new BAMFileWriter(file);

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
        writer.addAlignment(element);

        return true;
    }

    @Override
    public boolean write(List<SAMRecord> batch) {
        for(SAMRecord element : batch){
            write(element);
        }
        return true;
    }

    @Override
    public boolean writeHeader(SAMFileHeader h) {
        writer.setSortOrder(h.getSortOrder(), true);
        writer.setHeader(h);

        return true;
    }
}
