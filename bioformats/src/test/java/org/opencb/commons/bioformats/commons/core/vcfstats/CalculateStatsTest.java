package org.opencb.commons.bioformats.commons.core.vcfstats;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestName;
import org.opencb.commons.bioformats.commons.core.feature.Pedigree;
import org.opencb.commons.bioformats.commons.core.variant.io.Vcf4Reader;
import org.opencb.commons.bioformats.commons.core.variant.vcf4.VcfRecord;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: aaleman
 * Date: 8/26/13
 * Time: 1:12 PM
 * To change this template use File | Settings | File Templates.
 */
public class CalculateStatsTest {

    private Long start, end;
    private Vcf4Reader vcf;
    private String vcfFileName = "/Users/aleman/tmp/big.vcf";
    private String pedFileName = "/Users/aleman/tmp/big.ped";


    @Rule
    public TestName name = new TestName();


    @Before
    public void setUp() throws Exception {
        start = System.currentTimeMillis();


    }

    @After
    public void tearDown() throws Exception {
        end = System.currentTimeMillis();
        System.out.println("Time " + name.getMethodName() + ": " + (end - start));

    }

    @Test
    public void testCalculateStatsList() throws Exception {


        CalculateStats.runner(vcfFileName, pedFileName);

        // printListStats(list_vcf_stats);

    }



}
