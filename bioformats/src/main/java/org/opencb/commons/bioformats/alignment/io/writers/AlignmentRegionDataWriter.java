package org.opencb.commons.bioformats.alignment.io.writers;

import net.sf.samtools.SAMFileHeader;
import org.opencb.commons.bioformats.alignment.Alignment;
import org.opencb.commons.bioformats.alignment.AlignmentRegion;
import org.opencb.commons.io.DataWriter;

import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: jcoll
 * Date: 2/7/14
 * Time: 5:43 PM
 *
 */
public class AlignmentRegionDataWriter implements DataWriter<AlignmentRegion> {
    private AlignmentDataWriter<Alignment, SAMFileHeader> alignmentDataWriter;


    public AlignmentRegionDataWriter(AlignmentDataWriter<Alignment, SAMFileHeader> alignmentDataWriter) {
        this.alignmentDataWriter = alignmentDataWriter;
    }

    @Override
    public boolean open() {
        return alignmentDataWriter.open();
    }

    @Override
    public boolean close() {
        return alignmentDataWriter.close();
    }

    @Override
    public boolean pre() {
        return alignmentDataWriter.pre();
    }

    @Override
    public boolean post() {
        return alignmentDataWriter.post();
    }

    @Override
    public boolean write(AlignmentRegion elem) {
        // get reference sequence
        for (Alignment alignment : elem.getAlignments()) {
            if(!alignmentDataWriter.write(alignment)){
                return false;
            }
        }

        return true;
    }

    @Override
    public boolean write(List<AlignmentRegion> batch) {
        for(AlignmentRegion r : batch){
            if(!write(r))
                return false;
        }
        return true;
    }
}
