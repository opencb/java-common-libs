package org.opencb.commons.bioformats.alignment.tasks;

import org.opencb.commons.bioformats.alignment.Alignment;
import org.opencb.commons.bioformats.alignment.AlignmentHelper;
import org.opencb.commons.bioformats.alignment.AlignmentRegion;
import org.opencb.commons.bioformats.alignment.ShortReferenceSequenceException;
import org.opencb.commons.bioformats.feature.Region;
import org.opencb.commons.containers.map.QueryOptions;
import org.opencb.commons.run.Task;

import java.io.IOException;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: jcoll
 * Date: 27/03/14
 * Time: 13:00
 */

public class AlignmentRegionCompactorTask extends Task<AlignmentRegion> {


    public AlignmentRegionCompactorTask(){
    }


    @Override
    public boolean apply(List<AlignmentRegion> batch) throws IOException {

        for(AlignmentRegion alignmentRegion : batch){
            Long start = alignmentRegion.getStart();
            Region region = alignmentRegion.getRegion();
            String sequence = AlignmentHelper.getSequence(region, new QueryOptions());
            System.out.println("Asking for sequence: " + region.toString() + " size = " + (region.getEnd()-region.getStart()));
            for(Alignment alignment : alignmentRegion.getAlignments()){
                try {
                    AlignmentHelper.completeDifferencesFromReference(alignment,sequence, start);
                } catch (ShortReferenceSequenceException e) {
                    System.out.println("[ERROR] NOT ENOUGH REFERENCE SEQUENCE!!");
                    e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                }
            }
        }
        return true;
    }
}
