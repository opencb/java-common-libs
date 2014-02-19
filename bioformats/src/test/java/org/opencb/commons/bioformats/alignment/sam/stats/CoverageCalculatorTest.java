package org.opencb.commons.bioformats.alignment.sam.stats;

import org.junit.Ignore;
import org.junit.Test;
import org.opencb.commons.bioformats.alignment.sam.io.AlignmentSamDataReader;
import org.opencb.commons.bioformats.alignment.stats.AlignmentCoverage;
//import org.opencb.commons.bioformats.alignment.stats.AlignmentCoverageCalculator;
import org.opencb.commons.test.GenericTest;

import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.List;

/**
* Created with IntelliJ IDEA.
* User: jcoll
* Date: 12/5/13
* Time: 6:10 PM
* To change this template use File | Settings | File Templates.
*/
public class CoverageCalculatorTest extends GenericTest {

    private String filenameOrdered = "/home/jcoll/Documents/alignments_sorted.bam";
    @Ignore
    @Test
    public void cadenas()
    {
        String cad = "12345";

        System.out.println("test cadenas");

        String sub = cad.substring(2, 4);
        System.out.println(sub);

        sub = cad.substring(2, 5);
        System.out.println(sub);
    }
    @Ignore
    @Test
    public void ElTest() throws FileNotFoundException, UnsupportedEncodingException {
//        AlignmentSamDataReader reader = new AlignmentSamDataReader(filenameOrdered);
//        AlignmentCoverageCalculator c = new AlignmentCoverageCalculator(2000);
//
//        reader.open();
//        reader.pre();
//
//        List<AlignmentCoverage> l = c.getCoverage(reader.read(1000));
//        for(AlignmentCoverage cov : l){
//            System.out.println(cov.getChromosome()+":"+cov.getStart()+" "+cov.getCoverage());
//        }
//
//        reader.post();
//        reader.close();
    }

}
