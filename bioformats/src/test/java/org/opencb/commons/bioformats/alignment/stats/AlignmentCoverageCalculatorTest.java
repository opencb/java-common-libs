package org.opencb.commons.bioformats.alignment.stats;

import org.junit.Ignore;
import org.junit.Test;
import org.opencb.commons.bioformats.alignment.AlignmentRegion;
import org.opencb.commons.bioformats.alignment.io.readers.AlignmentRegionDataReader;
import org.opencb.commons.bioformats.alignment.sam.io.AlignmentBamDataReader;
import org.opencb.commons.io.DataWriter;
import org.opencb.commons.run.Runner;
import org.opencb.commons.run.Task;
import org.opencb.commons.test.GenericTest;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: jcoll
 * Date: 2/3/14
 * Time: 12:43 PM
 * To change this template use File | Settings | File Templates.
 */

public class AlignmentCoverageCalculatorTest extends GenericTest {

    @Ignore
    private class fakeWriter implements  DataWriter<AlignmentRegion>{
        @Override
        public boolean open() {
            return true;
        }

        @Override
        public boolean close() {
            return true;
        }

        @Override
        public boolean pre() {
            return true;
        }

        @Override
        public boolean post() {
            return true;
        }

        @Override
        public boolean write(AlignmentRegion elem) {
            if(elem == null){
                return false;
            }

            int size = (int)(elem.getCoverage().getEnd() - elem.getCoverage().getStart());
            short[] all = elem.getCoverage().getAll();
            System.out.println("ELEMENTOS :" + size + " (" + elem.getCoverage().getStart() + " - " + elem.getCoverage().getEnd() + ")");
           /* for(int i = 0; i < size; i++){
                System.out.println(
                        "("                                 +
                        (i+elem.getCoverage().getStart())   +
                        ", " + all[i]                       +
                        ", " + elem.getCoverage().getA()[i] +
                        ", " + elem.getCoverage().getC()[i] +
                        ", " + elem.getCoverage().getG()[i] +
                        ", " + elem.getCoverage().getT()[i] +
                        ")"
                );
            }*/

            float coverage;
            for(MeanCoverage meanCoverage : elem.getMeanCoverage()){
                for(int i = 0; i < meanCoverage.getCoverage().length; i++){
                    coverage = meanCoverage.getCoverage()[i];
                    System.out.println(elem.getChromosome() + "_" + (meanCoverage.getInitPosition()+i) + "_" + meanCoverage.getName() + " = " + coverage);
                }
            }
            return true;
        }

        @Override
        public boolean write(List<AlignmentRegion> batch) {
            for(AlignmentRegion alignmentRegion : batch){
                write(alignmentRegion);
            }
            return true;
        }
    }

    @Ignore
    @Test
    public void testAlignmentCoverageCalculatorTask(){

//        AlignmentBamDataReader alignmentBamDataReader = new AlignmentBamDataReader("/home/jcoll/Documents/alignments_small2.sam");
//        AlignmentBamDataReader alignmentBamDataReader = new AlignmentBamDataReader("/home/jcoll/Documents/alignments_sorted.bam");
        AlignmentBamDataReader alignmentBamDataReader = new AlignmentBamDataReader("/home/jcoll/Documents/HG00096.chrom20.ILLUMINA.bwa.GBR.low_coverage.20120522.bam");
        AlignmentRegionDataReader alignmentRegionDataReader = new AlignmentRegionDataReader(alignmentBamDataReader, 5000);

        List<Task<AlignmentRegion>> tasks = new LinkedList<>();
        AlignmentCoverageCalculatorTask alignmentCoverageCalculatorTask = new AlignmentCoverageCalculatorTask();
        alignmentCoverageCalculatorTask.addMeanCoverageCalculator(10000, "10K");
        alignmentCoverageCalculatorTask.addMeanCoverageCalculator(1000, "1K");
       // alignmentCoverageCalculatorTask.addMeanCoverageCalculator(100, ".1K");
       // alignmentCoverageCalculatorTask.addMeanCoverageCalculator(10, "10");

        tasks.add(alignmentCoverageCalculatorTask);

        List<DataWriter<AlignmentRegion>> writers = new LinkedList<>();
        writers.add(new fakeWriter());

        Runner<AlignmentRegion> runner = new Runner<>(alignmentRegionDataReader, writers, tasks, 1);

        runner.setBatchSize(1);

        try {
            runner.run();
        } catch (IOException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
    }



}
