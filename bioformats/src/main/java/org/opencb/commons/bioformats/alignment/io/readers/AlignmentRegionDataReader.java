package org.opencb.commons.bioformats.alignment.io.readers;

import net.sf.samtools.SAMFileHeader;
import org.opencb.commons.bioformats.alignment.Alignment;
import org.opencb.commons.bioformats.alignment.AlignmentRegion;
import org.opencb.commons.io.DataReader;

import java.util.LinkedList;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: jcoll
 * Date: 2/3/14
 * Time: 3:58 PM
 */
public class AlignmentRegionDataReader implements DataReader<AlignmentRegion> {

    private AlignmentDataReader<SAMFileHeader> alignmentDataReader;
    private Alignment prevAlignment;
    private int chunkSize;  //Max number of alignments in one AlignmentRegion.
    private int maxSequenceSize; //Maximum size for the total sequence. Count from the start of the first alignment to the end of the last alignment.

    private static final int defaultChunkSize = 2000;
    private static final int defaultMaxSequenceSize = 100000;

    public AlignmentRegionDataReader(AlignmentDataReader<SAMFileHeader> alignmentDataReader){
        this(alignmentDataReader, defaultChunkSize);
    }

    public AlignmentRegionDataReader(AlignmentDataReader<SAMFileHeader> alignmentDataReader, int chunkSize){
        this(alignmentDataReader, chunkSize, defaultMaxSequenceSize);
    }
    public AlignmentRegionDataReader(AlignmentDataReader<SAMFileHeader> alignmentDataReader, int chunkSize, int maxSequenceSize){
        this.alignmentDataReader = alignmentDataReader;
        this.prevAlignment = null;
        this.chunkSize = chunkSize;
        this.maxSequenceSize = maxSequenceSize;
    }


    @Override
    public boolean open() {
        return alignmentDataReader.open();
    }

    @Override
    public boolean close() {
        return alignmentDataReader.close();
    }

    @Override
    public boolean pre() {
        return alignmentDataReader.pre();
    }

    @Override
    public boolean post() {
        return alignmentDataReader.post();
    }

    @Override
    public AlignmentRegion read() {
        List<Alignment> alignmentList = new LinkedList<>();
        String chromosome;
        long start;
        long end;   //To have the correct "end" value,
        boolean overlappedEnd = true;
        String referenceSequence = null;

        //First initialisation
        if(prevAlignment == null){
            prevAlignment = alignmentDataReader.read();
            if(prevAlignment == null){  //Empty source
                return null;
            }
        }

        //Properties for the whole AlignmentRegion
        chromosome = prevAlignment.getChromosome();
        start = prevAlignment.getUnclippedStart();
        if((prevAlignment.getFlags() & Alignment.SEGMENT_UNMAPPED) == 0){
            end = prevAlignment.getUnclippedEnd();
        } else {
            end = start;
        }


        for(int i = 0; i < chunkSize; i++){
            alignmentList.add(prevAlignment);   //The prevAlignment is ready to be added.
            if((prevAlignment.getFlags() & Alignment.SEGMENT_UNMAPPED) == 0){
                if(end < prevAlignment.getUnclippedEnd()){
                    end = prevAlignment.getUnclippedEnd();  //Update the end only if is a valid segment.
                }
            }

            //Read new alignment.
            prevAlignment = alignmentDataReader.read();

            //First stop condition: End of the chromosome or file
            if(prevAlignment == null || !chromosome.equals(prevAlignment.getChromosome())){
                overlappedEnd = false;
                break;  //Break when read alignments from other chromosome or if is the last element
            }

            //Second stop condition: Too big Region.
            if((prevAlignment.getFlags() & Alignment.SEGMENT_UNMAPPED) == 0){
                if((prevAlignment.getUnclippedEnd() - start) > maxSequenceSize){
                    if(prevAlignment.getUnclippedStart() > end){
                        //The start of the prevAlignment doesn't overlap with the end of the last inserted Alignment
                        overlappedEnd = false;
                    }
                    break;
                }
            }

        }

        if(prevAlignment != null){
            //System.out.println("(prevAlignment.getUnclippedEnd() - start) = " +(prevAlignment.getUnclippedEnd() - start) + " overlappedEnd = " + overlappedEnd);
            //System.out.println("(alignmentList.get(alignmentList(size)-1).getEnd()) = " + (alignmentList.get(alignmentList.size()-1).getEnd()) + " start " + start + " i " + i);
            System.out.println("(alignmentList.get(alignmentList(size)-1).getUnclippedEnd() - start) = " + (alignmentList.get(alignmentList.size()-1).getUnclippedEnd() - start));
         }
        System.out.println("start = " + start + ", end = " + end + ", size = " + (end - start));



        AlignmentRegion alignmentRegion = new AlignmentRegion(alignmentList);
        alignmentRegion.setOverlapEnd(overlappedEnd);

        alignmentRegion.setStart(start);
        alignmentRegion.setEnd(end);


/*
        if(externalReferenceSequence){
            try {
                referenceSequence = AlignmentHelper.getSequence(new Region(chromosome, start, start+maxSequenceSize), new QueryOptions());
                for(Alignment alignment : alignmentRegion.getAlignments()){
                    alignment.completeDifferences(referenceSequence, (int)(alignment.getStart() - start));
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
*/
//        for(Alignment al : alignmentList){        //Debug
//            for(byte b : al.getReadSequence()){
//                System.out.print((char) b);
//            }
//            System.out.println( " <<< " + al.getStart());
//        }


//        System.out.println("Read " + alignmentRegion.getAlignments().size() +
//                " Start: " + alignmentRegion.getAlignments().get(0).getStart() +
//                " End " + alignmentRegion.getAlignments().get(alignmentRegion.getAlignments().size()-1).getStart()  +
//                " Size " +  (alignmentRegion.getAlignments().get(alignmentRegion.getAlignments().size()-1).getStart()-alignmentRegion.getAlignments().get(0).getStart() )
//
//        );
        return alignmentRegion;
    }


    @Override
    public List<AlignmentRegion> read(int batchSize) {
        List<AlignmentRegion> alignmentRegionList = new LinkedList<>();
//        for(int i = 0; i < batchSize; i++){
//            alignmentRegionList.add(read());
//        }
        AlignmentRegion alignmentRegion;
        for(int i = 0; i < batchSize; i++){
            alignmentRegion = read();
            if(alignmentRegion != null){
                alignmentRegionList.add(alignmentRegion);
            }
        }
        return alignmentRegionList;
    }

    /**
     * Set maximum size for the AlignmentRegion from the start of the first alignment, to the end of the last alignment.
     *
     * @param maxSequenceSize Maximum size
     */
    public void setMaxSequenceSize(int maxSequenceSize) {
        this.maxSequenceSize = maxSequenceSize;
    }

    public int getChunkSize() {
        return chunkSize;
    }

    /**
     * Set maximum number of Alignments in the AlignmentRegion result
     *
     * @param chunkSize Chunk Size
     */
    public void setChunkSize(int chunkSize) {
        this.chunkSize = chunkSize;
    }

}
