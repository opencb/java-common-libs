package org.opencb.commons.bioformats.alignment;

import net.sf.samtools.SAMFileHeader;
import org.opencb.commons.bioformats.alignment.io.readers.AlignmentDataReader;
import org.opencb.commons.io.DataReader;

import java.util.LinkedList;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: jcoll
 * Date: 2/3/14
 * Time: 3:58 PM
 * To change this template use File | Settings | File Templates.
 */
public class AlignmentRegionDataReader implements DataReader<AlignmentRegion> {

    private AlignmentDataReader<SAMFileHeader> alignmentDataReader;
    private Alignment prevAlignment;
    private int chunkSize;

    public AlignmentRegionDataReader(AlignmentDataReader<SAMFileHeader> alignmentDataReader){
        this(alignmentDataReader, 2000);
    }
    public AlignmentRegionDataReader(AlignmentDataReader<SAMFileHeader> alignmentDataReader, int chunkSize){
        this.alignmentDataReader = alignmentDataReader;
        this.prevAlignment = null;
        this.chunkSize = chunkSize;
    }

    @Override
    public boolean open() {
        alignmentDataReader.open();
        return true;
    }

    @Override
    public boolean close() {
        alignmentDataReader.close();
        return true;
    }

    @Override
    public boolean pre() {
        alignmentDataReader.pre();
        return true;
    }

    @Override
    public boolean post() {
        alignmentDataReader.post();
        return true;
    }

    @Override
    public AlignmentRegion read() {
        List<Alignment> alignmentList = new LinkedList<>();
        String chromosome;

        if(prevAlignment == null){
            prevAlignment = alignmentDataReader.read();
            if(prevAlignment == null){  //Empty source
                return null;
            }
        }
        alignmentList.add(prevAlignment);
        chromosome = prevAlignment.getChromosome();


        int i;
        for(i = 1; i < chunkSize; i++){        //Start in 1 because the prevAlignment is inserted
            prevAlignment = alignmentDataReader.read();
            if(prevAlignment == null || !chromosome.equals(prevAlignment.getChromosome())){
                break;  //Break when read alignments from other chromosome or if is the last element
            }
            alignmentList.add(prevAlignment);
        }

        AlignmentRegion alignmentRegion = new AlignmentRegion(alignmentList);

        if(i == chunkSize){    //If we got all the alignments
            prevAlignment = alignmentDataReader.read();
            //If the next alignment is in the same chromosome, it isn't the last Chromosome Region
            alignmentRegion.setChromosomeTail(!chromosome.equals(prevAlignment.getChromosome()));
        } else {    //If we get the last alignment in chromosome or in source
            alignmentRegion.setChromosomeTail(true);
        }

//        for(Alignment al : alignmentList){        //Depuration
//            for(byte b : al.getReadSequence()){
//                System.out.print((char) b);
//            }
//            System.out.println( " <<< " + al.getStart());
//        }
//        System.out.println("Leido un Region de tamaño"+i);
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
}
