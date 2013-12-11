package org.opencb.commons.bioformats.alignment.stats;

import net.sf.samtools.SAMRecord;
import org.opencb.commons.containers.list.IncrementalList;

import java.util.LinkedList;
import java.util.List;


/**
 * Created with IntelliJ IDEA.
 * User: jcoll
 * Date: 11/21/13
 * Time: 5:48 PM
 * To change this template use File | Settings | File Templates.
 */
public class AlignmentCoverageCalculator {

    private IncrementalList list;
    private int nucleotideIndex;
    private int actualCoverage;
    private int chunkSize;
    private int nextChunkSize;
    private int chunkCoverage;
    private String referenceName;
    private List<AlignmentCoverage> coverageList;


    public AlignmentCoverageCalculator() {
        this.list = new IncrementalList();
        this.nucleotideIndex = 0;
        this.actualCoverage = 0;
        this.chunkCoverage = 0;
        this.referenceName = "";
        this.setChunkSize(50000);

    }

    public AlignmentCoverageCalculator(int chunkSize)
    {
        this();
        this.setChunkSize(chunkSize);
    }

    public List<AlignmentCoverage> getCoverage(List<SAMRecord> batch){
        coverageList = new LinkedList<AlignmentCoverage>();
        for(SAMRecord record: batch){
            coverage(record);
        }

        return coverageList;
    }

    private int coverage(int end){
        int coverageDecrement;
        while(nucleotideIndex < end){
            if((coverageDecrement = list.decrement()) != 0){
                actualCoverage-=coverageDecrement;
            }
            if(nucleotideIndex == nextChunkSize){
                nextChunkSize += chunkSize;
                coverageList.add(new AlignmentCoverage(referenceName, nucleotideIndex-chunkSize, nucleotideIndex, (float) 1.0*chunkCoverage/chunkSize));
                //if(actualCoverage!=0){
                //writer.println(recordName + "_" + nucleotideIndex/chunkSize + "_" + chunkSize + ":cov:" + sampleName + "=" + 1.0*chunkCoverage/chunkSize);
                //}

                chunkCoverage = 0;
            }
//            if(actualCoverage!=0){
//                writer.println(recordName + "\t" + nucleotideIndex + "\t" + actualCoverage);
//            }
            chunkCoverage += actualCoverage;
            nucleotideIndex++;
        }

        return nucleotideIndex;
    }

    private int coverage(SAMRecord record){
        int position = record.getAlignmentStart();
        int size = record.getReadLength();
        String referenceName = record.getReferenceName();

        if(referenceName != this.referenceName){ //hemos cambiado de cromosoma FIXME seguro que es el nombre de cromosoma?

            coverage(nucleotideIndex+list.getTotalCount()+1);   // empty the list

            //Verbose Mode
            //System.out.println("recordName = " + recordName);

            /* LAST CHUNK //fixme
            int lastChunkSize = (nucleotideIndex%chunkSize);
            if(lastChunkSize != 0)
                writer.println("#" + rname + " " + nucleotideIndex + " " + 1.0*chunkCoverage/lastChunkSize);
            */

            //assert(actualCoverage == 0);
            nextChunkSize = chunkSize;
            actualCoverage = 0;
            nucleotideIndex = 0;
            chunkCoverage = 0;
            this.referenceName = referenceName;
        }
        coverage(position);

        list.reverseInsert(size);
        actualCoverage ++;

        return nucleotideIndex;
    }

    public void setChunkSize(int chunkSize) {
        this.chunkSize = chunkSize;
        this.nextChunkSize = chunkSize;
    }

}
