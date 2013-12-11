package org.opencb.commons.bioformats.alignment;

import net.sf.samtools.SAMFileHeader;
import net.sf.samtools.SAMRecord;
import org.opencb.commons.bioformats.alignment.io.readers.AlignmentDataReader;
import org.opencb.commons.io.DataWriter;
import org.opencb.commons.run.Runner;

/**
 * Created with IntelliJ IDEA.
 * User: jcoll
 * Date: 12/5/13
 * Time: 5:19 PM
 * To change this template use File | Settings | File Templates.
 */
public abstract class AlignmentRunner extends Runner<AlignmentDataReader<SAMRecord,SAMFileHeader>,DataWriter,SAMRecord> {
    public AlignmentRunner(AlignmentDataReader reader, DataWriter writer, Runner prev) {
        super(reader, writer, prev);
    }

    public AlignmentRunner(AlignmentDataReader reader, DataWriter writer) {
        super(reader, writer);
    }
}
