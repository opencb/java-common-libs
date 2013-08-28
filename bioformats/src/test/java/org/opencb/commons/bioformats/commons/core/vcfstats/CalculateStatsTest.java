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

    @Rule
    public TestName name = new TestName();


    @Before
    public void setUp() throws Exception {
        start = System.currentTimeMillis();
        vcf = new Vcf4Reader("/Users/aleman/tmp/file.vcf");


    }

    @After
    public void tearDown() throws Exception {
        end = System.currentTimeMillis();
        vcf.close();
        System.out.println("Time " + name.getMethodName() + ": " + (end - start));

    }

    @Test
    public void testCalculateStatsList() throws Exception {

        List<VcfRecord> list_vcf_records = vcf.readAll();
        Pedigree ped = new Pedigree("/Users/aleman/tmp/file.ped");


        List<VcfRecordStat> list_vcf_stats = CalculateStats.calculateStats(list_vcf_records, vcf.getSampleNames(), ped);
        printListStats(list_vcf_stats);

    }


    private void printListStats(List<VcfRecordStat> list) throws FileNotFoundException {

        File file = new File("/Users/aleman/tmp/vcf.stats");
        PrintWriter pw = new PrintWriter(file);

        pw.append(String.format("%-5s%-5s%-5s%-10s%-10s%-10s" +
                "%-10s%-10s%-10s%-15s%-30s%-10s%-10s%-10s\n",
                "Chr", "Pos", "Ref", "Alt", "Maf", "Mgf",
                "NumAll.", "Miss All.", "Miss Gt", "All. Count", "Gt count", "Trans", "Transv",
                "Mend Error"));
        for (VcfRecordStat v : list) {
            pw.append(String.format("%-5s%-5d%-5s%-10s%-10s%-10" +
                    "s%-10d%-10d%-10d%-15s%-30s%-10d%-10d%-10d\n",
                    v.getChromosome(),
                    v.getPosition(),
                    v.getRef_alleles(),
                    Arrays.toString(v.getAltAlleles()),
                    v.getMafAllele(),
                    v.getMgfAllele(),
                    v.getNumAlleles(),
                    v.getMissingAlleles(),
                    v.getMissingGenotypes(),
                    Arrays.toString(v.getAllelesCount()),
                    v.getGenotypes(),
                    v.getTransitionsCount(),
                    v.getTransversionsCount(),
                    v.getMendelinanErrors()
            ));
        }

        pw.close();

    }
}
