package org.opencb.commons.bioformats.alignment;

import net.sf.samtools.SAMRecord;
import org.opencb.commons.bioformats.alignment.stats.AlignmentCoverage;
import org.opencb.commons.bioformats.alignment.io.readers.AlignmentDataReader;
import org.opencb.commons.bioformats.alignment.io.writers.coverage.AlignmentCoverageDataWriter;
import org.opencb.commons.bioformats.alignment.stats.AlignmentCoverageCalculator;
import org.opencb.commons.io.DataWriter;

import java.io.IOException;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: jcoll
 * Date: 12/4/13
 * Time: 7:12 PM
 * To change this template use File | Settings | File Templates.
 */
public class AlignmentCoverageRunner extends AlignmentRunner {

    protected AlignmentCoverageCalculator coverageCalculator;

    public AlignmentCoverageRunner(AlignmentDataReader reader, DataWriter writer,int chunkSize, AlignmentRunner prev) {
        super(reader, writer, prev);
        coverageCalculator = new AlignmentCoverageCalculator(chunkSize);
    }

    public AlignmentCoverageRunner(AlignmentDataReader reader, DataWriter writer, int chunkSize) {
        super(reader, writer);
        coverageCalculator = new AlignmentCoverageCalculator(chunkSize);
    }

    public AlignmentCoverageRunner(AlignmentDataReader reader, DataWriter writer) {
        super(reader, writer);
        coverageCalculator = new AlignmentCoverageCalculator();
    }

    public AlignmentCoverageRunner(AlignmentDataReader reader, DataWriter writer, AlignmentRunner prev) {
        super(reader, writer, prev);
        coverageCalculator = new AlignmentCoverageCalculator();
    }

    @Override
    public List<SAMRecord> apply(List<SAMRecord> batch) throws IOException{

        if (writer != null) {
            List<AlignmentCoverage> list = coverageCalculator.getCoverage(batch);
            ((AlignmentCoverageDataWriter) writer).writeBatch(list);
        }

        return batch;
    }
}
