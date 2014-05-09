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
        alignmentDataWriter.open();
        return false;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public boolean close() {
        alignmentDataWriter.close();
        return false;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public boolean pre() {
        alignmentDataWriter.pre();
        return false;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public boolean post() {
        alignmentDataWriter.post();
        return false;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public boolean write(AlignmentRegion elem) {
        // get reference sequence
        for (Alignment alignment : elem.getAlignments()) {
            alignmentDataWriter.write(alignment);
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
