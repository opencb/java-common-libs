package org.opencb.commons.bioformats.alignment.tasks;

import org.opencb.commons.bioformats.alignment.Alignment;
import org.opencb.commons.bioformats.alignment.AlignmentRegion;
import org.opencb.commons.bioformats.alignment.stats.MeanCoverage;
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

    private class NativeShortArrayList{
        private short[] array = null;
        private int size = 0;
        private int capacity = 0;

        private NativeShortArrayList(int capacity) {
            this.capacity = capacity;
            this.array = new short[capacity];
        }

        private NativeShortArrayList() {
            this.capacity = 0;
        }

        public void resize(int newSize){
            short[] newArray = new short[newSize];
            for(int i = 0; i < size; i++){
                newArray[i] = array[i];
            }
            array = newArray;
            capacity = newSize;
        }

        public void clear(){
            size = 0;
        }
        public void empty(){
            size = 0;
            array = null;
        }
        public short get(int position){
            return array[position];
        }

        public void add(short elem){
            array[size++] = elem;
        }
        private int size() {
            return size;
        }

        private short[] getArray() {
            return array;
        }
        private int getCapacity() {
            return capacity;
        }

    }


    NativeShortArrayList a;
    NativeShortArrayList c;
    NativeShortArrayList g;
    NativeShortArrayList t;
    NativeShortArrayList all;


    int savedSize;


    public AlignmentCoverageCalculatorTask() {
        setRegionCoverageSize(4000);
        a = new   NativeShortArrayList();
        c = new   NativeShortArrayList();
        g = new   NativeShortArrayList();
        t = new   NativeShortArrayList();
        all = new NativeShortArrayList();


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
            int totalSize = (int)(alignmentRegion.getEnd()-alignmentRegion.getStart());
            if(all.getCapacity() < totalSize){
                totalSize*=1.4;
                all.resize(totalSize);
                a.resize(totalSize);
                c.resize(totalSize);
                g.resize(totalSize);
                t.resize(totalSize);
            }
            savedSize = 0;

            /*
                Calculate Coverage
             */
            int i2 = 0;
            for(Alignment alignment : alignmentRegion.getAlignments()){
                coverage(alignment);
                i2++;
            }
            if(!alignmentRegion.isOverlapEnd()){
                saveCoverage(alignmentRegion.getEnd()+1);
                //end = alignmentRegion.getEnd();
                //reset();  // jmml
            }

            /*
                Create Region Coverage  //Todo jcoll: Profile this part
             */
            RegionCoverage regionCoverage = new RegionCoverage();
//            for(int i = 0; i < savedSize; i++){
//                regionCoverage.getA()[i] = a.get(i);
//                regionCoverage.getC()[i] = c.get(i);
//                regionCoverage.getG()[i] = g.get(i);
//                regionCoverage.getT()[i] = t.get(i);
//                regionCoverage.getAll()[i] = all.get(i);
//            }
            regionCoverage.setA(a.getArray());
            regionCoverage.setC(c.getArray());
            regionCoverage.setG(g.getArray());
            regionCoverage.setT(t.getArray());
            regionCoverage.setAll(all.getArray());



//            for(int i = 0; i < savedSize; i++){
//                regionCoverage.getA()[i] = a.get(i);
//            }for(int i = 0; i < savedSize; i++){
//                regionCoverage.getC()[i] = c.get(i);
//            }for(int i = 0; i < savedSize; i++){
//                regionCoverage.getG()[i] = g.get(i);
//            }for(int i = 0; i < savedSize; i++){
//                regionCoverage.getT()[i] = t.get(i);
//            }for(int i = 0; i < savedSize; i++){
//                regionCoverage.getAll()[i] = all.get(i);
//            }
            regionCoverage.setStart(coverageStart);
            regionCoverage.setEnd(coverageStart+savedSize);

//            System.out.println(end-coverageStart);
//            System.out.println(start-coverageStart);
//            System.out.println(savedSize);

         //   assert start-coverageStart == savedSize;  //TODO jcoll: Assert this
            alignmentRegion.setCoverage(regionCoverage);
            savedSize = 0;
            a.clear();
            c.clear();
            g.clear();
            t.clear();
            all.clear();

            /*
                Create Mean Coverage List
             */
            List<MeanCoverage> meanCoverageList = new ArrayList<>(meanCoverageCalculator.size());
            for(MeanCoverageCalculator aux: meanCoverageCalculator){
                meanCoverageList.add(aux.takeMeanCoverage(coverageStart));
            }
            alignmentRegion.setMeanCoverage(meanCoverageList);

            if(!alignmentRegion.isOverlapEnd()){
                end = alignmentRegion.getEnd();
                reset();
            }
        }
        return true;
    }

    private void saveCoverage(long endP){
        //Saves the actual coverage from start to end
        int pos;
        short auxAll;
        for(long i = start; i < endP; i++){
            pos = (int)(i & regionCoverageMask);

            a.add(/*savedSize,*/ coverage.getA()[pos]);
            auxAll = coverage.getA()[pos];
            coverage.getA()[pos] = 0;

            c.add(/*savedSize, */coverage.getC()[pos]);
            auxAll += coverage.getC()[pos];
            coverage.getC()[pos] = 0;

            g.add(/*savedSize, */coverage.getG()[pos]);
            auxAll += coverage.getG()[pos];
            coverage.getG()[pos] = 0;

            t.add(/*savedSize, */coverage.getT()[pos]);
            auxAll += coverage.getT()[pos];
            coverage.getT()[pos] = 0;

            all.add(/*savedSize, */auxAll);

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
            //System.out.println(alignment.getStart());
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
            assert alignment.getLength() == sequence.length;
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
            if(i < alignment.getLength()){ //TODO jj: Write a correct commentary
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
