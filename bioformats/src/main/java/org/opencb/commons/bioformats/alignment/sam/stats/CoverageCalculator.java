package org.opencb.commons.bioformats.alignment.sam.stats;

import net.sf.samtools.SAMRecord;
import org.opencb.commons.bioformats.alignment.AlignmentCoverage;

import java.io.PrintStream;
import java.util.LinkedList;
import java.util.List;


/**
 * Created with IntelliJ IDEA.
 * User: jcoll
 * Date: 11/21/13
 * Time: 5:48 PM
 * To change this template use File | Settings | File Templates.
 */
public class CoverageCalculator {

    private IncrementalList list;
    private int nucleotideIndex;
    private int actualCoverage;
    private int chunkSize;
    private int nextChunkSize;
    private int chunkCoverage;
    private String sampleName;
    private String recordName;
    private List<AlignmentCoverage> coverageList;


    public CoverageCalculator() {
        this.list = new IncrementalList();
        nucleotideIndex = 0;
        actualCoverage = 0;
        chunkSize = 50000;
        chunkCoverage = 0;
        nextChunkSize = chunkSize;
        recordName = "";
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
            nucleotideIndex++;
            if((coverageDecrement = list.decrement()) != 0){
                actualCoverage-=coverageDecrement;
            }
            if(nucleotideIndex == nextChunkSize){
                nextChunkSize += chunkSize;
                coverageList.add(new AlignmentCoverage(recordName, nucleotideIndex-chunkSize, nucleotideIndex, (float) 1.0*chunkCoverage/chunkSize));
                //if(actualCoverage!=0){
                //writer.println(recordName + "_" + nucleotideIndex/chunkSize + "_" + chunkSize + ":cov:" + sampleName + "=" + 1.0*chunkCoverage/chunkSize);
                //}

                chunkCoverage = 0;
            }
            chunkCoverage += actualCoverage;
        }

        return nucleotideIndex;
    }

    private int coverage(SAMRecord record){
        int position = record.getAlignmentStart();
        int size = record.getReadLength();
        String recordName = record.getReferenceName();

        if(recordName != this.recordName){ //hemos cambiado de cromosoma FIXME seguro que es el nombre de cromosoma?

            coverage(nucleotideIndex+list.getTotalCount()+1);   // empty the list

            System.out.println("recordName = " + recordName);

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
            this.recordName = recordName;
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
    public void setSampleName(String sampleName) {
        this.sampleName = sampleName;
    }
}
