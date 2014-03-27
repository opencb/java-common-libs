package org.opencb.commons.bioformats.alignment.stats;

import org.opencb.commons.bioformats.alignment.Alignment;
import org.opencb.commons.bioformats.alignment.AlignmentHelper;
import org.opencb.commons.bioformats.alignment.AlignmentRegion;
import org.opencb.commons.containers.map.QueryOptions;
import org.opencb.commons.run.Task;

import java.io.IOException;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: jacobo
 * Date: 27/03/14
 * Time: 13:00
 * To change this template use File | Settings | File Templates.
 */

public class AlignmentRegionCompactorTask extends Task<AlignmentRegion> {


    private AlignmentRegionCompactorTask(){
    }


    @Override
    public boolean apply(List<AlignmentRegion> batch) throws IOException {

        for(AlignmentRegion alignmentRegion : batch){
            Long start = alignmentRegion.getStart();
            String sequence = AlignmentHelper.getSequence(alignmentRegion.getRegion(), new QueryOptions());

            for(Alignment alignment : alignmentRegion.getAlignments()){
                if(!alignment.completeDifferences(sequence, start)){
                    //TODO: Catch this.
                    //The sequence was not enough
                }
            }
        }
        return true;
    }
}
