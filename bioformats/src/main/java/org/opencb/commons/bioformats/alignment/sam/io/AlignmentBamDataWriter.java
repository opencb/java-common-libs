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
public class AlignmentBamDataWriter extends AlignmentSamDataWriter  {


    public AlignmentBamDataWriter(String filename, SAMFileHeader header) {
        super(filename, header);
    }

    public AlignmentBamDataWriter(String filename, AlignmentSamDataReader reader) {
        super(filename, reader);
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

}
