package org.opencb.commons.bioformats.alignment.stats;

import org.opencb.commons.bioformats.alignment.Alignment;
import org.opencb.commons.bioformats.alignment.AlignmentRegion;
import org.opencb.commons.bioformats.alignment.stats.RegionCoverage;
import org.opencb.commons.run.Task;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;


/**
 * Created with IntelliJ IDEA.
 * User: jcoll
 * Date: 11/21/13
 * Time: 5:48 PM
 * To change this template use File | Settings | File Templates.
 */
public class AlignmentCoverageCalculatorTask extends Task<AlignmentRegion> {

    private class MeanCoverageCalculator {
        private int accumulator;
        private long next;
        private int size;
        List<Float> savedMean;
        private int savedMeanSize;
        private String name;


        public MeanCoverageCalculator(){
            this(1000, "1K");
        }
        public MeanCoverageCalculator(int size, String name){
            this.accumulator = 0;
            this.next = 0;
            this.size = size;
            this.savedMean = new ArrayList<>();
            this.savedMeanSize = 0;
            this.name = name;
        }

        public void mean(long position, short all){
            accumulator += all;
            if(next == position){
                //System.out.println("POS: " + position + ", Accumulator = "+ accumulator + " Size: " + size + " Mean= " + ((float)accumulator)/size);
                savedMean.add(savedMeanSize,((float)accumulator)/size);
                savedMeanSize++;
                accumulator = 0;
                next+=size;
            }
        }

        public MeanCoverage takeMeanCoverage(long start){
            MeanCoverage meanCoverage = new MeanCoverage(size,name);
            meanCoverage.setCoverage(takeMeanCoverageArray());
            meanCoverage.setInitPosition((int)(start/size));

            return meanCoverage;
        }

        public float[] takeMeanCoverageArray(){
            float[] array = new float[savedMeanSize];
            for(int i = 0; i < savedMeanSize; i++){
                array[i] = savedMean.get(i);
            }
            savedMeanSize = 0;
            return array;
        }

        public void init(long position){
            this.next =((position)/size+1)*size;
        }

        private void setSize(int size) {
            this.size = size;
        }
        private void setName(String name){
            this.name = name;
        }
    }

    private List<MeanCoverageCalculator> meanCoverageCalculator;

    private long start, end;
    RegionCoverage coverage;
    int  regionCoverageSize;
    long regionCoverageMask;

    List<Short> a;
    List<Short> c;
    List<Short> g;
    List<Short> t;
    List<Short> all;
    int savedSize;


    public AlignmentCoverageCalculatorTask() {
        setRegionCoverageSize(4000);
        a = new ArrayList<>();
        c = new ArrayList<>();
        g = new ArrayList<>();
        t = new ArrayList<>();
        all = new ArrayList<>();

        meanCoverageCalculator = new ArrayList<>();

        reset();
//        addMeanCoverageCalculator(1000, "1K");

    }

    public void reset(){
        start = end = 0;
        savedSize = 0;
    }


    @Override
    public boolean apply(List<AlignmentRegion> batch) throws IOException {
        for(AlignmentRegion alignmentRegion : batch){
            if(alignmentRegion == null){
                continue;
            }

            /*
                Initialize
             */
            long coverageStart = start;
            if(start == 0){                 //Set Default value
                coverageStart = start = end = alignmentRegion.getStart();
                for(MeanCoverageCalculator aux : meanCoverageCalculator){
                    aux.init(start);
                }
            }
            savedSize = 0;

            /*
                Calculate Coverage
             */
            for(Alignment alignment : alignmentRegion.getAlignments()){
                coverage(alignment);
            }
            if(alignmentRegion.isChromosomeTail()){
                saveCoverage(alignmentRegion.getEnd()+1);
                //end = alignmentRegion.getEnd();
                //reset();  // jmml
            }

            /*
                Create Region Coverage
             */
            RegionCoverage regionCoverage = new RegionCoverage(savedSize);
            for(int i = 0; i < savedSize; i++){
                regionCoverage.getA()[i] = a.get(i);
                regionCoverage.getC()[i] = c.get(i);
                regionCoverage.getG()[i] = g.get(i);
                regionCoverage.getT()[i] = t.get(i);
                regionCoverage.getAll()[i] = all.get(i);
            }
            regionCoverage.setStart(coverageStart);
            regionCoverage.setEnd(end);
            alignmentRegion.setCoverage(regionCoverage);

            /*
                Create Mean Coverage List
             */
            List<MeanCoverage> meanCoverageList = new ArrayList<>(meanCoverageCalculator.size());
            for(MeanCoverageCalculator aux: meanCoverageCalculator){
                meanCoverageList.add(aux.takeMeanCoverage(coverageStart));
            }
            alignmentRegion.setMeanCoverage(meanCoverageList);

//            if(alignmentRegion.isChromosomeTail()){
//                reset();
//            }
        }
        return true;
    }

    private void saveCoverage(long endP){
        //Saves the actual coverage from start to end
        int pos;
        short auxAll;
        for(long i = start; i < endP; i++){
            pos = (int)(i & regionCoverageMask);

            a.add(savedSize, coverage.getA()[pos]);
            auxAll = coverage.getA()[pos];
            coverage.getA()[pos] = 0;

            c.add(savedSize, coverage.getC()[pos]);
            auxAll += coverage.getC()[pos];
            coverage.getC()[pos] = 0;

            g.add(savedSize, coverage.getG()[pos]);
            auxAll += coverage.getG()[pos];
            coverage.getG()[pos] = 0;

            t.add(savedSize, coverage.getT()[pos]);
            auxAll += coverage.getT()[pos];
            coverage.getT()[pos] = 0;

            all.add(savedSize, auxAll);

            for(MeanCoverageCalculator aux : meanCoverageCalculator){
                aux.mean(i,auxAll);
            }
            savedSize++;
        }
        start = endP;
    }

    private int coverage(Alignment alignment){
        if(alignment.getLength() > regionCoverageSize){
            setRegionCoverageSize(alignment.getLength());
        }
        if(alignment.getStart() > end){
            saveCoverage(end+1);  //Save to the end
            saveCoverage(alignment.getStart());  //Save zeros to the start
            System.out.println(alignment.getStart());
        } else {
            saveCoverage(alignment.getStart());
        }
        start = alignment.getStart();
        if(alignment.getEnd()>end){
            end = alignment.getEnd();
        }
        byte[] sequence = alignment.getReadSequence();


        Iterator<Alignment.AlignmentDifference> diferencesIterator = alignment.getDifferences().iterator();
        Alignment.AlignmentDifference alignmentDifference = diferencesIterator.hasNext()? diferencesIterator.next():null;
        int offset = 0; // offset caused by insertions and deletions
        for(int i = 0; i < alignment.getLength(); i++) {
            //        for(int i = 0; i < sequence.length; i++) {    //TODO jcoll: Analyze this case
            if (alignmentDifference != null) {  // if there are remaining differences
                if(alignmentDifference.getPos() == i) {
                    switch(alignmentDifference.getOp()){
                        case Alignment.AlignmentDifference.INSERTION:
                            i += alignmentDifference.getLength();
                            offset -= alignmentDifference.getLength();
                            break;
                        case Alignment.AlignmentDifference.DELETION:
                            offset += alignmentDifference.getLength();
                            break;
                        case Alignment.AlignmentDifference.MISMATCH:
                        case Alignment.AlignmentDifference.SKIPPED_REGION:
                        case Alignment.AlignmentDifference.SOFT_CLIPPING:
                        case Alignment.AlignmentDifference.HARD_CLIPPING:
                        case Alignment.AlignmentDifference.PADDING:
                        default:
                            break;
                    }
                    if (diferencesIterator.hasNext()) {
                        alignmentDifference = diferencesIterator.next();
                    } else {
                        alignmentDifference = null;
                    }
                }
            }
            switch (sequence[i]) {
                case 'A':
                    coverage.getA()[(int) ((i + offset + start) & regionCoverageMask)]++;
                    break;
                case 'C':
                    coverage.getC()[(int) ((i + offset + start) & regionCoverageMask)]++;
                    break;
                case 'G':
                    coverage.getG()[(int) ((i + offset + start) & regionCoverageMask)]++;
                    break;
                case 'T':
                    coverage.getT()[(int) ((i + offset + start) & regionCoverageMask)]++;
                    break;
                default:
                    //TODO jcoll: Analyze this case
                    break;
            }
        }

        return 0;
    }

    /**
     * Set size to the nearest upper 2^n number for quick modulus operation
     *
     * @param size
     */
    public void setRegionCoverageSize(int size){
        if(size < 0){
            return;
        }
        int lg = (int)Math.ceil(Math.log(size)/Math.log(2));
        //int lg = 31 - Integer.numberOfLeadingZeros(size);
        int newRegionCoverageSize = 1 << lg;
        int newRegionCoverageMask = newRegionCoverageSize - 1;
        RegionCoverage newCoverage = new RegionCoverage(newRegionCoverageSize);

        if(coverage != null){
            for(int i = 0; i < (end-start); i++){
                newCoverage.getA()[(int)((start+i)&newRegionCoverageMask)] = coverage.getA()[(int)((start+i)&regionCoverageMask)];
            }
        }

        regionCoverageSize = newRegionCoverageSize;
        regionCoverageMask = newRegionCoverageMask;
        coverage = newCoverage;
//        System.out.println("Region Coverage Mask : " + regionCoverageMask);
    }

    public void addMeanCoverageCalculator(int size, String name) {
        this.meanCoverageCalculator.add(new MeanCoverageCalculator(size, name));
    }


}
